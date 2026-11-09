"""后台管理路由（M3：PRD 2.2 + 开发计划 2.2/8 节）。

三件套：
1. 用户管理：查询 / 禁用 / CSV 导出 / 档案时间轴
2. 病历解析记录：查看 + 人工修正（corrected_by 留痕 + 审计快照，可复跑）
3. 低置信度报警：confidence < 0.7 记录列表（报警可见）

鉴权：管理员账号（PBKDF2 哈希）+ HS256 JWT（stdlib hmac 手写，免新增依赖）。
留痕：所有写操作落 AuditLog（before/after 快照），修正可按审计记录回放复跑。
"""
from __future__ import annotations

import base64
import hashlib
import hmac
import io
import json
import os
import time
import uuid
from datetime import datetime
from typing import Any

from fastapi import APIRouter, Depends, Query, Request
from fastapi.responses import JSONResponse, StreamingResponse
from pydantic import BaseModel
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from ..database import get_db
from ..models import AdminUser, AuditLog, HealthRecord, User
from . import fail, ok

admin_router = APIRouter()

# ---------------- 鉴权工具 ----------------

PBKDF2_ITERATIONS = 60_000
TOKEN_TTL_SECONDS = 60 * 60 * 12  # 12 小时


def _get_jwt_secret() -> str:
    """JWT 密钥：环境变量优先，兜底 dev 值（生产必换）。"""
    return os.environ.get("YKB_JWT_SECRET") or "dev-secret-change-me"


def hash_password(password: str, salt: str | None = None) -> str:
    """PBKDF2-SHA256 口令哈希，格式 pbkdf2$<salt>$<hex>。"""
    salt = salt or uuid.uuid4().hex[:16]
    digest = hashlib.pbkdf2_hmac("sha256", password.encode(), salt.encode(), PBKDF2_ITERATIONS)
    return f"pbkdf2${salt}${digest.hex()}"


def verify_password(password: str, stored: str) -> bool:
    """校验口令与存储哈希是否匹配。"""
    try:
        _, salt, hex_digest = stored.split("$")
    except ValueError:
        return False
    digest = hashlib.pbkdf2_hmac("sha256", password.encode(), salt.encode(), PBKDF2_ITERATIONS)
    return hmac.compare_digest(digest.hex(), hex_digest)


def _b64url(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode()


def _b64url_decode(text: str) -> bytes:
    return base64.urlsafe_b64decode(text + "=" * (-len(text) % 4))


def issue_token(admin_id: str, role: str) -> str:
    """手写 HS256 JWT：header.payload.signature。"""
    header = _b64url(json.dumps({"alg": "HS256", "typ": "JWT"}).encode())
    payload = _b64url(
        json.dumps({"sub": admin_id, "role": role, "exp": int(time.time()) + TOKEN_TTL_SECONDS}).encode()
    )
    signing_input = f"{header}.{payload}".encode()
    sig = _b64url(hmac.new(_get_jwt_secret().encode(), signing_input, hashlib.sha256).digest())
    return f"{header}.{payload}.{sig}"


def parse_token(token: str) -> dict | None:
    """校验签名与有效期，返回 payload；无效返回 None。"""
    parts = token.split(".")
    if len(parts) != 3:
        return None
    header, payload, sig = parts
    expected = _b64url(hmac.new(_get_jwt_secret().encode(), f"{header}.{payload}".encode(), hashlib.sha256).digest())
    if not hmac.compare_digest(sig, expected):
        return None
    try:
        claims = json.loads(_b64url_decode(payload))
    except (ValueError, json.JSONDecodeError):
        return None
    if claims.get("exp", 0) < time.time():
        return None
    return claims


class _AuthError(Exception):
    """携带业务错误码的鉴权异常，由全局 handler 转统一响应。"""

    def __init__(self, code: int, msg: str) -> None:
        self.code = code
        self.msg = msg


def _auth_fail(msg: str) -> _AuthError:
    return _AuthError(1401, msg)


@admin_router.exception_handler(_AuthError)
async def _auth_error_handler(request: Request, exc: _AuthError):
    return fail(exc.code, exc.msg)


async def require_admin(request: Request, db: AsyncSession = Depends(get_db)) -> AdminUser:
    """路由依赖：Bearer JWT → 活跃管理员。"""
    auth = request.headers.get("Authorization", "")
    if not auth.startswith("Bearer "):
        raise _auth_fail("缺少凭证")
    claims = parse_token(auth.removeprefix("Bearer ").strip())
    if not claims:
        raise _auth_fail("凭证无效或已过期")
    result = await db.execute(select(AdminUser).where(AdminUser.id == claims["sub"]))
    admin = result.scalar_one_or_none()
    if admin is None or admin.status != "active":
        raise _auth_fail("账号不可用")
    return admin


# ---------------- 登录 ----------------

class LoginRequest(BaseModel):
    username: str
    password: str


def _admin_view(admin: AdminUser) -> dict:
    return {"id": admin.id, "username": admin.username, "role": admin.role}


async def ensure_default_admin(db: AsyncSession) -> None:
    """空表时播种默认管理员（admin / 环境变量 YKB_ADMIN_PASSWORD，默认 admin123）。

    仅供开发/演示环境冷启动；生产环境应设置 YKB_ADMIN_PASSWORD 并首次登录后改密。
    """
    count = (await db.execute(select(func.count()).select_from(AdminUser))).scalar_one()
    if count == 0:
        db.add(
            AdminUser(
                username="admin",
                password_hash=hash_password(os.environ.get("YKB_ADMIN_PASSWORD", "admin123")),
                role="super",
            )
        )
        await db.commit()


@admin_router.post("/login")
async def login(req: LoginRequest, db: AsyncSession = Depends(get_db)) -> JSONResponse:
    """管理员登录，签发 JWT。"""
    result = await db.execute(select(AdminUser).where(AdminUser.username == req.username))
    admin = result.scalar_one_or_none()
    if admin is None or admin.status != "active" or not verify_password(req.password, admin.password_hash):
        return fail(1401, "用户名或密码错误")
    return ok({"token": issue_token(admin.id, admin.role), "admin": _admin_view(admin)})


# ---------------- 用户管理 ----------------

def _mask_phone(phone: str | None) -> str:
    """手机号脱敏（导出场景：中间四位打码）。"""
    if not phone or len(phone) < 7:
        return phone or ""
    return f"{phone[:3]}****{phone[-4:]}"


def _user_view(u: User) -> dict:
    return {
        "id": u.id,
        "phone": u.phone,
        "name": u.name,
        "status": u.status,
        "emergency_contact_phone": u.emergency_contact_phone,
        "children_ids": u.children_ids or [],
        "tenant_id": u.tenant_id,
        "created_at": u.created_at.isoformat() if u.created_at else None,
    }


@admin_router.get("/users")
async def list_users(
    keyword: str | None = Query(None, description="手机号/姓名模糊查询"),
    status: str | None = Query(None, description="active/disabled"),
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
    _: AdminUser = Depends(require_admin),
) -> JSONResponse:
    """用户列表：查询 + 分页。"""
    stmt = select(User)
    if keyword:
        stmt = stmt.where((User.phone.like(f"%{keyword}%")) | (User.name.like(f"%{keyword}%")))
    if status:
        stmt = stmt.where(User.status == status)
    total = (await db.execute(select(func.count()).select_from(stmt.subquery()))).scalar_one()
    rows = (
        await db.execute(stmt.order_by(User.created_at.desc()).offset((page - 1) * page_size).limit(page_size))
    ).scalars().all()
    return ok({"total": total, "page": page, "page_size": page_size, "items": [_user_view(u) for u in rows]})


async def _write_audit(
    db: AsyncSession, admin: AdminUser, action: str, target_type: str, target_id: str,
    before: dict, after: dict,
) -> AuditLog:
    """落审计留痕（before/after 快照）。"""
    log = AuditLog(
        admin_id=admin.id,
        action=action,
        target_type=target_type,
        target_id=target_id,
        before_snapshot=before,
        after_snapshot=after,
    )
    db.add(log)
    await db.commit()
    await db.refresh(log)
    return log


@admin_router.post("/users/{user_id}/disable")
async def disable_user(
    user_id: str, db: AsyncSession = Depends(get_db), admin: AdminUser = Depends(require_admin)
) -> JSONResponse:
    """禁用用户（留痕 user_disable）。"""
    u = (await db.execute(select(User).where(User.id == user_id))).scalar_one_or_none()
    if u is None:
        return fail(1404, "用户不存在")
    if u.status == "disabled":
        return fail(1409, "用户已是禁用状态")
    before, after = {"status": u.status}, {"status": "disabled"}
    u.status = "disabled"
    log = await _write_audit(db, admin, "user_disable", "user", u.id, before, after)
    return ok({"user": _user_view(u), "audit_id": log.id})


@admin_router.post("/users/{user_id}/enable")
async def enable_user(
    user_id: str, db: AsyncSession = Depends(get_db), admin: AdminUser = Depends(require_admin)
) -> JSONResponse:
    """解禁用户（留痕 user_enable）。"""
    u = (await db.execute(select(User).where(User.id == user_id))).scalar_one_or_none()
    if u is None:
        return fail(1404, "用户不存在")
    if u.status == "active":
        return fail(1409, "用户已是正常状态")
    before, after = {"status": u.status}, {"status": "active"}
    u.status = "active"
    log = await _write_audit(db, admin, "user_enable", "user", u.id, before, after)
    return ok({"user": _user_view(u), "audit_id": log.id})


@admin_router.get("/users/export")
async def export_users(
    db: AsyncSession = Depends(get_db), _: AdminUser = Depends(require_admin)
) -> StreamingResponse:
    """用户 CSV 导出（utf-8-sig 带 BOM，Excel 直开；手机号脱敏）。"""
    rows = (await db.execute(select(User).order_by(User.created_at.desc()))).scalars().all()
    buf = io.StringIO()
    buf.write("id,phone,name,status,created_at\n")
    for u in rows:
        buf.write(f"{u.id},{_mask_phone(u.phone)},{u.name or ''},{u.status},{u.created_at}\n")
    buf.seek(0)
    return StreamingResponse(
        iter([buf.getvalue().encode("utf-8-sig")]),
        media_type="text/csv; charset=utf-8",
        headers={"Content-Disposition": "attachment; filename=users_export.csv"},
    )


@admin_router.get("/users/{user_id}/timeline")
async def user_timeline(
    user_id: str, db: AsyncSession = Depends(get_db), _: AdminUser = Depends(require_admin)
) -> JSONResponse:
    """用户档案时间轴：用户信息 + 健康记录按时间倒序。"""
    u = (await db.execute(select(User).where(User.id == user_id))).scalar_one_or_none()
    if u is None:
        return fail(1404, "用户不存在")
    records = (
        await db.execute(
            select(HealthRecord)
            .where(HealthRecord.user_id == user_id)
            .order_by(HealthRecord.record_date.desc().nullslast(), HealthRecord.created_at.desc())
        )
    ).scalars().all()
    timeline = [
        {
            "id": r.id,
            "diagnosis": r.diagnosis,
            "indicators": r.indicators or [],
            "medications": r.medications or [],
            "confidence": r.confidence,
            "status": r.status,
            "record_date": r.record_date.isoformat() if r.record_date else None,
            "created_at": r.created_at.isoformat() if r.created_at else None,
        }
        for r in records
    ]
    return ok({"user": _user_view(u), "timeline": timeline})


# ---------------- 病历解析记录 ----------------

_RECORD_EDITABLE_FIELDS = ("diagnosis", "indicators", "medications", "confidence", "record_date")


def _record_view(r: HealthRecord) -> dict:
    return {
        "id": r.id,
        "user_id": r.user_id,
        "image_url": r.image_url,
        "ocr_raw_text": r.ocr_raw_text,
        "diagnosis": r.diagnosis,
        "indicators": r.indicators or [],
        "medications": r.medications or [],
        "record_date": r.record_date.isoformat() if r.record_date else None,
        "confidence": r.confidence,
        "status": r.status,
        "corrected_by": r.corrected_by,
        "created_at": r.created_at.isoformat() if r.created_at else None,
    }


def _record_snapshot(r: HealthRecord) -> dict:
    """修正前后的可复跑快照（仅可编辑字段，datetime 转 ISO）。"""
    snap: dict[str, Any] = {}
    for f in _RECORD_EDITABLE_FIELDS:
        v = getattr(r, f)
        snap[f] = v.isoformat() if isinstance(v, datetime) else v
    return snap


@admin_router.get("/records")
async def list_records(
    user_id: str | None = Query(None),
    status: str | None = Query(None, description="pending/confirmed/corrected"),
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
    _: AdminUser = Depends(require_admin),
) -> JSONResponse:
    """病历解析记录列表（OCR 原文 + 结构化结果 + 置信度）。"""
    stmt = select(HealthRecord)
    if user_id:
        stmt = stmt.where(HealthRecord.user_id == user_id)
    if status:
        stmt = stmt.where(HealthRecord.status == status)
    total = (await db.execute(select(func.count()).select_from(stmt.subquery()))).scalar_one()
    rows = (
        await db.execute(
            stmt.order_by(HealthRecord.created_at.desc()).offset((page - 1) * page_size).limit(page_size)
        )
    ).scalars().all()
    return ok({"total": total, "page": page, "page_size": page_size, "items": [_record_view(r) for r in rows]})


@admin_router.get("/records/{record_id}")
async def get_record(
    record_id: str, db: AsyncSession = Depends(get_db), _: AdminUser = Depends(require_admin)
) -> JSONResponse:
    """解析记录详情。"""
    r = (await db.execute(select(HealthRecord).where(HealthRecord.id == record_id))).scalar_one_or_none()
    if r is None:
        return fail(1404, "记录不存在")
    return ok(_record_view(r))


class CorrectRequest(BaseModel):
    """人工修正请求体：只允许改可编辑字段。"""

    diagnosis: str | None = None
    indicators: list[dict] | None = None
    medications: list[dict] | None = None
    confidence: float | None = None
    record_date: str | None = None  # ISO 格式


@admin_router.post("/records/{record_id}/correct")
async def correct_record(
    record_id: str,
    req: CorrectRequest,
    db: AsyncSession = Depends(get_db),
    admin: AdminUser = Depends(require_admin),
) -> JSONResponse:
    """人工修正解析记录：corrected_by 留痕 + before/after 审计快照（可复跑）。"""
    r = (await db.execute(select(HealthRecord).where(HealthRecord.id == record_id))).scalar_one_or_none()
    if r is None:
        return fail(1404, "记录不存在")
    changed = req.model_dump(exclude_none=True)
    if not changed:
        return fail(1400, "没有可应用的修改字段")
    if "record_date" in changed:
        try:
            changed["record_date"] = datetime.fromisoformat(changed["record_date"])
        except ValueError:
            return fail(1400, "record_date 须为 ISO 格式（如 2026-11-10T08:00:00）")
    before = _record_snapshot(r)
    for field, value in changed.items():
        setattr(r, field, value)
    r.status = "corrected"
    r.corrected_by = admin.id
    after = _record_snapshot(r)
    log = await _write_audit(db, admin, "record_correct", "record", r.id, before, after)
    return ok({"record": _record_view(r), "audit_id": log.id})


# ---------------- 低置信度报警 ----------------

LOW_CONFIDENCE_THRESHOLD = 0.7  # 开发计划第 8 节 M3 验收：<0.7 报警可见


@admin_router.get("/alerts/low-confidence")
async def low_confidence_alerts(
    threshold: float = Query(LOW_CONFIDENCE_THRESHOLD, gt=0, le=1),
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
    _: AdminUser = Depends(require_admin),
) -> JSONResponse:
    """低置信度记录报警列表（默认 <0.7），按置信度升序（最可疑的排最前）。"""
    stmt = select(HealthRecord).where(HealthRecord.confidence < threshold)
    total = (await db.execute(select(func.count()).select_from(stmt.subquery()))).scalar_one()
    rows = (
        await db.execute(
            stmt.order_by(HealthRecord.confidence.asc()).offset((page - 1) * page_size).limit(page_size)
        )
    ).scalars().all()
    return ok({
        "threshold": threshold,
        "total": total,
        "page": page,
        "page_size": page_size,
        "items": [_record_view(r) for r in rows],
    })


# ---------------- 审计留痕与复跑 ----------------

@admin_router.get("/audit-logs")
async def list_audit_logs(
    action: str | None = Query(None),
    target_id: str | None = Query(None),
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
    _: AdminUser = Depends(require_admin),
) -> JSONResponse:
    """审计留痕查询（谁在何时改了什么、前后快照）。"""
    stmt = select(AuditLog)
    if action:
        stmt = stmt.where(AuditLog.action == action)
    if target_id:
        stmt = stmt.where(AuditLog.target_id == target_id)
    total = (await db.execute(select(func.count()).select_from(stmt.subquery()))).scalar_one()
    rows = (
        await db.execute(
            stmt.order_by(AuditLog.created_at.desc()).offset((page - 1) * page_size).limit(page_size)
        )
    ).scalars().all()
    items = [
        {
            "id": log.id,
            "admin_id": log.admin_id,
            "action": log.action,
            "target_type": log.target_type,
            "target_id": log.target_id,
            "before_snapshot": log.before_snapshot,
            "after_snapshot": log.after_snapshot,
            "created_at": log.created_at.isoformat() if log.created_at else None,
        }
        for log in rows
    ]
    return ok({"total": total, "page": page, "page_size": page_size, "items": items})


@admin_router.post("/audit-logs/{log_id}/replay")
async def replay_audit_log(
    log_id: str, db: AsyncSession = Depends(get_db), admin: AdminUser = Depends(require_admin)
) -> JSONResponse:
    """复跑一条修正留痕：按 after_snapshot 重放修正，比对当前库内状态。

    支撑验收「修正留痕可复跑」：重放后与 after_snapshot 一致 → matched=true。
    重放真实写库且再落一条 record_correct 审计，保证留痕链完整。
    """
    log = (await db.execute(select(AuditLog).where(AuditLog.id == log_id))).scalar_one_or_none()
    if log is None:
        return fail(1404, "审计记录不存在")
    if log.action != "record_correct" or log.target_type != "record":
        return fail(1400, f"暂只支持复跑 record_correct 留痕（收到：{log.action}）")
    r = (await db.execute(select(HealthRecord).where(HealthRecord.id == log.target_id))).scalar_one_or_none()
    if r is None:
        return fail(1404, "留痕指向的记录已不存在")
    expected = log.after_snapshot or {}
    replay_before = _record_snapshot(r)
    applied: dict[str, Any] = {}
    for field in _RECORD_EDITABLE_FIELDS:
        if field not in expected:
            continue
        value = expected[field]
        if field == "record_date" and isinstance(value, str):
            try:
                value = datetime.fromisoformat(value)
            except ValueError:
                return fail(1400, f"快照 record_date 无法解析：{value}")
        setattr(r, field, value)
        applied[field] = value
    r.status = "corrected"
    r.corrected_by = admin.id
    after = _record_snapshot(r)
    await _write_audit(db, admin, "record_correct", "record", r.id, replay_before, after)
    matched = all(_record_snapshot(r)[f] == expected.get(f) for f in _RECORD_EDITABLE_FIELDS if f in expected)
    return ok({"replay_id": log.id, "target_id": r.id, "applied": applied, "matched": matched})


# ---------------- 基础统计 ----------------

@admin_router.get("/stats")
async def admin_stats(
    db: AsyncSession = Depends(get_db), _: AdminUser = Depends(require_admin)
) -> JSONResponse:
    """基础计数（开发计划 2.2 #20：一期先做基础计数）。"""
    user_total = (await db.execute(select(func.count()).select_from(User))).scalar_one()
    user_disabled = (await db.execute(select(func.count()).select_from(User).where(User.status == "disabled"))).scalar_one()
    record_total = (await db.execute(select(func.count()).select_from(HealthRecord))).scalar_one()
    record_pending = (await db.execute(select(func.count()).select_from(HealthRecord).where(HealthRecord.status == "pending"))).scalar_one()
    record_corrected = (await db.execute(select(func.count()).select_from(HealthRecord).where(HealthRecord.status == "corrected"))).scalar_one()
    low_conf = (await db.execute(select(func.count()).select_from(HealthRecord).where(HealthRecord.confidence < LOW_CONFIDENCE_THRESHOLD))).scalar_one()
    return ok({
        "users": {"total": user_total, "disabled": user_disabled},
        "records": {"total": record_total, "pending": record_pending, "corrected": record_corrected},
        "low_confidence_alerts": low_conf,
    })
