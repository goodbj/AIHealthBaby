"""M1 病历闭环测试（Issue #180）：上传→OCR→LLM 结构化→确认修正→时间轴。

OCR 用内存桩引擎，不依赖真实模型文件；真实 RapidOCR 引擎另有脚本验证
（见 doc/reports/M1交付记录.md 的端到端复跑记录）。
"""
from __future__ import annotations

import io
from pathlib import Path

import pytest
from PIL import Image
from sqlalchemy import select

from app.models import HealthRecord, User
from app.ocr import BaseOCR, set_ocr


class FakeOCR(BaseOCR):
    """OCR 桩：返回固定验单文本（模拟 RapidOCR 逐行输出）。"""

    LINES = [
        "检验报告单",
        "姓名：张三",
        "科室：内科",
        "血压： 145/95 mmHg 偏高",
        "空腹血糖： 7.8 mmol/L 偏高",
        "诊断：高血压2级 2型糖尿病",
    ]

    def recognize(self, image_path):
        return "\n".join(self.LINES), list(self.LINES)


@pytest.fixture(autouse=True)
def _fake_ocr():
    set_ocr(FakeOCR())
    yield
    set_ocr(None)


def _png_bytes() -> bytes:
    buf = io.BytesIO()
    Image.new("RGB", (32, 32), "white").save(buf, format="PNG")
    return buf.getvalue()


async def _mk_user(session, phone="13600000001") -> User:
    u = User(phone=phone, name="M1测试")
    session.add(u)
    await session.commit()
    await session.refresh(u)
    return u


def _upload(client, user_id: str):
    return client.post(
        "/api/records/upload",
        data={"user_id": user_id},
        files=[("images", ("report.png", _png_bytes(), "image/png"))],
    )


# --- 上传 → OCR → 结构化 ---


def test_upload_end_to_end(client, session):
    """端到端主链路：上传图片 → OCR 文本落库 → 离线桩结构化提取 → pending 状态。"""
    u = _mk_user_sync(client)
    resp = _upload(client, u["id"])
    assert resp.status_code == 200, resp.text
    body = resp.json()
    assert body["code"] == 0, body
    data = body["data"]
    assert data["status"] == "pending"
    assert "高血压2级" in data["diagnosis"]
    assert data["confidence"] > 0
    names = [i["name"] for i in data["indicators"]]
    assert "血压" in names and "空腹血糖" in names
    assert data["ocrRawText"].startswith("检验报告单")
    assert data["imageUrls"]


def _mk_user_sync(client) -> dict:
    """造用户：跨会话（TestClient 独立引擎），用 asyncio.run 开新循环创建。"""
    import asyncio

    from app.database import AsyncSessionLocal, init_db

    async def _create():
        await init_db()
        async with AsyncSessionLocal() as s:
            u = (
                await s.execute(select(User).where(User.phone == "13600000001"))
            ).scalar_one_or_none()
            if u is None:
                u = User(phone="13600000001", name="M1测试")
                s.add(u)
                await s.commit()
                await s.refresh(u)
            return {"id": u.id}

    return asyncio.run(_create())


def test_upload_user_not_found(client):
    """用户不存在：1001。"""
    resp = _upload(client, "no-such-user")
    body = resp.json()
    assert body["code"] == 1001


def test_upload_bad_ext(client):
    """非法扩展名拒绝。"""
    u = _mk_user_sync(client)
    resp = client.post(
        "/api/records/upload",
        data={"user_id": u["id"]},
        files=[("images", ("shell.exe", b"MZfake", "application/octet-stream"))],
    )
    assert resp.json()["code"] == 1001


def test_upload_blank_ocr_rejected(client):
    """白图（OCR 无字）拒绝并报 1002。"""
    from app.ocr import set_ocr

    class BlankOCR(BaseOCR):
        def recognize(self, image_path):
            return "", []

    set_ocr(BlankOCR())
    u = _mk_user_sync(client)
    resp = _upload(client, u["id"])
    assert resp.json()["code"] == 1002


# --- 列表（时间轴） ---


def test_list_timeline_order(client):
    """时间轴按 record_date 倒序：有 record_date 的排前，同日期新建在前。"""
    u = _mk_user_sync(client)
    _upload(client, u["id"])
    _upload(client, u["id"])
    resp = client.get("/api/records/list", params={"user_id": u["id"]})
    body = resp.json()
    assert body["code"] == 0
    records = body["data"]
    assert len(records) == 2
    dates = [r["createdAt"] for r in records]
    assert dates == sorted(dates, reverse=True)


# --- 详情 / 确认修正 / 删除 ---


def test_confirm_flow(client):
    """用户确认修正：覆盖 diagnosis/indicators，status → confirmed。"""
    u = _mk_user_sync(client)
    rec = _upload(client, u["id"]).json()["data"]
    resp = client.post(
        f"/api/records/{rec['id']}/confirm",
        json={"diagnosis": "高血压3级（用户修正）", "indicators": [{"name": "血压", "value": "150/95", "level": "偏高"}]},
    )
    body = resp.json()
    assert body["code"] == 0
    data = body["data"]
    assert data["status"] == "confirmed"
    assert data["diagnosis"] == "高血压3级（用户修正）"
    assert data["indicators"][0]["value"] == "150/95"

    detail = client.get(f"/api/records/{rec['id']}").json()["data"]
    assert detail["ocrRawText"].startswith("检验报告单")


def test_get_record_404(client):
    assert client.get("/api/records/not-exist").json()["code"] == 1001


def test_delete_record_removes_image(client):
    """删除记录：库没了，图片文件也清理。"""
    u = _mk_user_sync(client)
    rec = _upload(client, u["id"]).json()["data"]
    rid = rec["id"]
    assert rec["imageUrls"]
    resp = client.delete(f"/api/records/{rid}")
    assert resp.json()["code"] == 0
    assert client.get(f"/api/records/{rid}").json()["code"] == 1001


def test_confirmed_record_in_admin_stats(client):
    """确认后的记录进 M3 后台统计/列表（部门内跨里程碑闭环）。"""
    u = _mk_user_sync(client)
    rec = _upload(client, u["id"]).json()["data"]
    client.post(f"/api/records/{rec['id']}/confirm", json={"diagnosis": "高血压"})
    # admin 登录（默认密码来自环境，test_admin_m3 同款）
    login = client.post("/api/admin/login", json={"username": "admin", "password": "admin123"})
    assert login.json()["code"] == 0, login.text
    token = login.json()["data"]["token"]
    headers = {"Authorization": f"Bearer {token}"}
    recs = client.get("/api/admin/records", headers=headers).json()["data"]["items"]
    assert any(r["id"] == rec["id"] and r["status"] == "confirmed" for r in recs)


# --- LLM 结构化（离线桩单测） ---


def test_offline_extract_rules():
    """离线正则桩：诊断/指标/置信度提取规则。"""
    from app.llm import DeepSeekLLM

    llm = DeepSeekLLM({})
    ext = llm.extract_medical_info("\n".join(FakeOCR.LINES))
    assert ext.diagnosis == "高血压2级 2型糖尿病"
    assert {i["name"] for i in ext.indicators} >= {"血压", "空腹血糖"}
    assert ext.confidence >= 0.5


# --- 登录联调配套（auth） ---


def test_auth_login_and_profile(client):
    """演示一键登录：固定验证码 → token → profile 回读。"""
    resp = client.post("/api/auth/login", json={"phone": "13900001234", "code": "123456"})
    body = resp.json()
    assert body["code"] == 0, body
    token = body["data"]["token"]
    assert token and body["data"]["userId"]

    # 二次登录 get-or-create：同一手机号同用户
    again = client.post("/api/auth/login", json={"phone": "13900001234", "code": "123456"}).json()
    assert again["data"]["userId"] == body["data"]["userId"]

    prof = client.get("/api/user/profile", headers={"Authorization": f"Bearer {token}"}).json()
    assert prof["code"] == 0
    assert prof["data"]["phone"] == "13900001234"

    # 错验证码 / 坏 token
    bad = client.post("/api/auth/login", json={"phone": "13900001234", "code": "000000"}).json()
    assert bad["code"] == 1001
    badtok = client.get("/api/user/profile", headers={"Authorization": "Bearer fake.token.here"}).json()
    assert badtok["code"] == 4010
