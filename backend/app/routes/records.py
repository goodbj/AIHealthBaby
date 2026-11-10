"""病历闭环路由（M1，Issue #180）。

核心链路：拍照/相册上传 → OCR（RapidOCR 本地）→ LLM 结构化提取 →
用户确认修正 → 时间轴展示。

契约对齐 Android `YiKangBaoApi.kt`（PRD 6.3 统一响应封装）：
- POST /api/records/upload   multipart 多图上传，OCR+LLM 解析落库
- GET  /api/records/list     按创建时间倒序分页
- GET  /api/records/{id}     详情（含 OCR 原文，供确认修正页回显）
- POST /api/records/{id}/confirm  用户确认/修正结构化结果，status→confirmed
- DELETE /api/records/{id}   删除记录（顺带删图）
- GET  /api/records/{id}/image   查看原图（M1 本地明文，M4 换加密+签名 URL）
"""
from __future__ import annotations

import shutil
import uuid
from datetime import datetime
from pathlib import Path

from fastapi import APIRouter, Depends, File, Form, UploadFile
from fastapi.responses import FileResponse, JSONResponse
from pydantic import BaseModel
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from ..config import get_settings, load_llm_config
from ..database import get_db
from ..hooks import trigger_hook
from ..llm import MedicalExtraction, get_llm
from ..models import HealthRecord, User
from ..ocr import OCRError, get_ocr
from .response import fail, ok

router = APIRouter(tags=["records"])

_ALLOWED_EXT = {".jpg", ".jpeg", ".png", ".bmp", ".webp"}
_MAX_IMAGES = 5


class ConfirmRequest(BaseModel):
    """确认修正请求体：字段可空，空则保持原值。"""

    diagnosis: str | None = None
    indicators: list[dict] | None = None
    medications: list[dict] | None = None
    record_date: str | None = None


def _storage_dir() -> Path:
    """病历图片存储目录（本地模拟对象存储，生产切 OSS）。"""
    root = Path(get_settings().storage_root)
    (root / "records").mkdir(parents=True, exist_ok=True)
    return root / "records"


def _parse_record_date(raw: str | None) -> datetime | None:
    """宽松解析「2026-11-01 / 2026/11/01 08:00 / 2026年11月1日」。"""
    if not raw:
        return None
    import re

    m = re.search(r"(\d{4})[年/\-.](\d{1,2})[月/\-.](\d{1,2})", raw)
    if not m:
        return None
    try:
        return datetime(int(m.group(1)), int(m.group(2)), int(m.group(3)))
    except ValueError:
        return None


def _record_view(r: HealthRecord, *, with_raw: bool = False) -> dict:
    """记录视图（字段名对齐 Android HealthRecord 模型，camelCase）。"""
    return {
        "id": r.id,
        "userId": r.user_id,
        "recordType": "病历",
        "imageUrls": [r.image_url] if r.image_url else [],
        "ocrRawText": r.ocr_raw_text or "",
        "diagnosis": r.diagnosis or "",
        "indicators": r.indicators or [],
        "medications": r.medications or [],
        "hospitalName": None,
        "department": None,
        "doctorName": None,
        "recordDate": (r.record_date or r.created_at).strftime("%Y-%m-%d %H:%M") if (r.record_date or r.created_at) else "",
        "confidence": round(float(r.confidence or 0.0), 2),
        "status": r.status,
        "createdAt": r.created_at.strftime("%Y-%m-%d %H:%M:%S") if r.created_at else "",
    }


async def _get_user(db: AsyncSession, user_id: str | None) -> User | None:
    result = await db.execute(select(User).where(User.id == user_id)) if user_id else None
    return result.scalar_one_or_none() if result else None


@router.post("/upload")
async def upload_record(
    user_id: str = Form(...),
    images: list[UploadFile] = File(...),
    db: AsyncSession = Depends(get_db),
) -> JSONResponse:
    """上传病历照片（可多张），OCR → LLM 结构化 → 落库，全链路同步返回。

    验收指标：单图端到端 ≤5s（OCR ~1s + LLM 提取在线 ~2s / 离线桩 <0.1s）。
    """
    if not images:
        return fail(1001, "images 不能为空")
    user = await _get_user(db, user_id)
    if user is None:
        return fail(1001, f"用户不存在：{user_id}")

    # 1) 图片落盘（本地对象存储模拟；文件名隔离，扩展名白名单）
    saved: list[Path] = []
    for img in images[:_MAX_IMAGES]:
        ext = Path(img.filename or "upload.png").suffix.lower()
        if ext not in _ALLOWED_EXT:
            return fail(1001, f"不支持的图片格式：{ext or '(无后缀)'}")
        dest = _storage_dir() / f"{uuid.uuid4().hex}{ext}"
        with dest.open("wb") as fh:
            shutil.copyfileobj(img.file, fh)
        saved.append(dest)

    # 2) OCR（多图按行拼接）
    try:
        ocr = get_ocr()
        all_lines: list[str] = []
        for p in saved:
            _, lines = ocr.recognize(p)
            all_lines.extend(lines)
        ocr_text = "\n".join(all_lines)
    except OCRError as e:
        for p in saved:
            p.unlink(missing_ok=True)
        return fail(1002, f"OCR 失败：{e}")

    if not ocr_text.strip():
        for p in saved:
            p.unlink(missing_ok=True)
        return fail(1002, "未识别到文字，请重拍（确保光线充足、文字清晰）")

    # 3) LLM 结构化提取（在线 DeepSeek / 离线正则桩兜底）
    llm = get_llm(config=load_llm_config())
    try:
        extracted = llm.extract_medical_info(ocr_text)
    except Exception:  # noqa: BLE001 — LLM 层兜底
        extracted = MedicalExtraction()

    # 4) 落库（首图为主图；record_date 从 OCR 文本宽松解析）
    record = HealthRecord(
        user_id=user_id,
        image_url=f"/api/records/image/{saved[0].name}",
        ocr_raw_text=ocr_text,
        diagnosis=extracted.diagnosis or None,
        indicators=extracted.indicators,
        medications=extracted.medications,
        record_date=_parse_record_date(ocr_text),
        confidence=float(extracted.confidence or 0.0),
        status="pending",  # 待用户确认
    )
    db.add(record)
    await db.commit()
    await db.refresh(record)

    # 触发 after_record_parsed 钩子（M0 机制复用，log_after_record_parsed 挂载）
    await trigger_hook("after_record_parsed", record_id=record.id, user_id=user_id)

    return ok({
        **_record_view(record),
        "ocrRawText": ocr_text,
        "message": "解析完成，请确认修正后归档",
    })


@router.get("/list")
async def list_records(
    user_id: str,
    page: int = 1,
    page_size: int = 20,
    record_type: str | None = None,  # 预留按病种筛选；M1 先按时间轴
    db: AsyncSession = Depends(get_db),
) -> JSONResponse:
    """健康档案时间轴列表（按 record_date/created_at 倒序，旧档在下）。"""
    if page < 1 or page_size < 1 or page_size > 100:
        return fail(1001, "page/page_size 非法")
    stmt = (
        select(HealthRecord)
        .where(HealthRecord.user_id == user_id)
        .order_by(func.coalesce(HealthRecord.record_date, HealthRecord.created_at).desc())
        .offset((page - 1) * page_size)
        .limit(page_size)
    )
    records = (await db.execute(stmt)).scalars().all()
    return ok([_record_view(r) for r in records])


@router.get("/image/{filename}")
async def get_image(filename: str, db: AsyncSession = Depends(get_db)) -> FileResponse:
    """查看病历原图（M1 本地明文直出；M4 计划换 AES-256 加密 + 短签名 URL）。"""
    path = _storage_dir() / Path(filename).name  # 防 ../ 目录穿越
    if not path.exists():
        return JSONResponse({"code": 1001, "msg": "图片不存在", "data": None, "trace_id": ""}, status_code=404)
    return FileResponse(path)


@router.get("/{record_id}")
async def get_record(record_id: str, db: AsyncSession = Depends(get_db)) -> JSONResponse:
    """记录详情（含 OCR 原文，供确认修正页回显）。"""
    r = (await db.execute(select(HealthRecord).where(HealthRecord.id == record_id))).scalar_one_or_none()
    if r is None:
        return fail(1001, f"记录不存在：{record_id}")
    return ok(_record_view(r, with_raw=True))


@router.post("/{record_id}/confirm")
async def confirm_record(record_id: str, req: ConfirmRequest, db: AsyncSession = Depends(get_db)) -> JSONResponse:
    """用户确认/修正结构化结果：覆盖 diagnosis/indicators/medications，status→confirmed。"""
    r = (await db.execute(select(HealthRecord).where(HealthRecord.id == record_id))).scalar_one_or_none()
    if r is None:
        return fail(1001, f"记录不存在：{record_id}")
    if req.diagnosis is not None:
        r.diagnosis = req.diagnosis.strip() or None
    if req.indicators is not None:
        r.indicators = req.indicators
    if req.medications is not None:
        r.medications = req.medications
    if req.record_date:
        r.record_date = _parse_record_date(req.record_date) or r.record_date
    r.status = "confirmed"
    await db.commit()
    return ok(_record_view(r))


@router.delete("/{record_id}")
async def delete_record(record_id: str, db: AsyncSession = Depends(get_db)) -> JSONResponse:
    """删除记录（图片文件顺带清理）。"""
    r = (await db.execute(select(HealthRecord).where(HealthRecord.id == record_id))).scalar_one_or_none()
    if r is None:
        return fail(1001, f"记录不存在：{record_id}")
    if r.image_url and "/image/" in r.image_url:
        (_storage_dir() / Path(r.image_url.split("/")[-1]).name).unlink(missing_ok=True)
    await db.delete(r)
    await db.commit()
    return ok({"deleted": record_id})
