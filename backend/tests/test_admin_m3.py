"""M3 后台管理测试：鉴权 / 用户管理 / 记录修正留痕可复跑 / 低置信度报警 / 统计。

对齐验收标准（Issue #182）：修正留痕可复跑、报警可见、管理闭环全链路。
"""
from __future__ import annotations

import asyncio
import base64
import json
import time

import pytest
from sqlalchemy import select

from app.models import AuditLog, HealthRecord, User
from app.routes.admin import (
    hash_password,
    issue_token,
    parse_token,
    verify_password,
)


# ---------------- 单元：口令哈希与 JWT ----------------

def test_password_hash_roundtrip():
    """PBKDF2 哈希可验证、同口令不同盐、错口令拒验。"""
    stored = hash_password("s3cret")
    assert stored.startswith("pbkdf2$") and verify_password("s3cret", stored)
    assert not verify_password("wrong", stored)
    assert hash_password("s3cret") != stored  # 盐随机


def test_token_roundtrip_and_expiry():
    """JWT 签发/校验/过期拒绝。"""
    token = issue_token("admin-1", "super")
    claims = parse_token(token)
    assert claims and claims["sub"] == "admin-1" and claims["role"] == "super"
    # 篡改签名拒绝
    assert parse_token(token[:-3] + "abc") is None
    # 过期拒绝
    expired_payload = base64.urlsafe_b64encode(
        json.dumps({"sub": "a", "exp": int(time.time()) - 10}).encode()
    ).rstrip(b"=").decode()
    header = base64.urlsafe_b64encode(json.dumps({"alg": "HS256", "typ": "JWT"}).encode()).rstrip(b"=").decode()
    import hmac as _h
    import hashlib as _sh
    sig = base64.urlsafe_b64encode(
        _h.new(b"dev-secret-change-me", f"{header}.{expired_payload}".encode(), _sh.sha256).digest()
    ).rstrip(b"=").decode()
    assert parse_token(f"{header}.{expired_payload}.{sig}") is None


# ---------------- 夹具：登录拿 token + 造数据 ----------------

@pytest.fixture
def admin_token(client):
    """启动时已播种默认管理员 admin/admin123，登录拿 token。"""
    resp = client.post("/api/admin/login", json={"username": "admin", "password": "admin123"})
    body = resp.json()
    assert body["code"] == 0, f"默认管理员登录失败：{body}"
    return body["data"]["token"]


@pytest.fixture
def seeded(client, admin_token):
    """造两个用户 + 三条记录（含两条低置信度），返回 (u1_id, u2_id, 基线统计)。

    手机号带随机后缀：进程级共享内存库中多次运行不撞 users.phone UNIQUE。
    基线在造数**之前**取（共享内存库跨用例累积，断言一律用差值）。
    """
    import uuid as _uuid

    tag = _uuid.uuid4().hex[:8]
    from app.database import AsyncSessionLocal

    async def _seed(u1: User, u2: User, records: list[HealthRecord]):
        async with AsyncSessionLocal() as s:
            s.add_all([u1, u2])
            await s.flush()
            for r in records:
                r.user_id = u1.id if r.diagnosis in ("高血压", "待定诊断") else u2.id
            s.add_all(records)
            await s.commit()
            return u1.id, u2.id

    # 基线先行
    headers = {"Authorization": f"Bearer {admin_token}"}
    base = client.get("/api/admin/stats", headers=headers).json()["data"]

    u1 = User(phone=f"138{tag}001", name=f"张大爷{tag}")
    u2 = User(phone=f"138{tag}002", name="李大妈")
    records = [
        HealthRecord(diagnosis="高血压", confidence=0.95, status="confirmed"),
        HealthRecord(diagnosis="待定诊断", confidence=0.42, status="pending"),
        HealthRecord(diagnosis="血糖存疑", confidence=0.55, status="pending",
                     indicators=[{"name": "空腹血糖", "value": "9.8", "level": "偏高"}]),
    ]
    import asyncio
    u1_id, u2_id = asyncio.run(_seed(u1, u2, records))
    return u1_id, u2_id, base


AUTH_FAIL_CODES = (401,)


def _authed(client, token):
    return {"Authorization": f"Bearer {token}"}


# ---------------- 鉴权 ----------------

def test_admin_endpoints_require_token(client):
    """无凭证访问后台一律 1401。"""
    for path in ("/api/admin/users", "/api/admin/records", "/api/admin/alerts/low-confidence", "/api/admin/stats"):
        body = client.get(path).json()
        assert body["code"] == 1401, f"{path} 未拦截：{body}"


def test_login_wrong_password(client):
    body = client.post("/api/admin/login", json={"username": "admin", "password": "nope"}).json()
    assert body["code"] == 1401


# ---------------- 用户管理 ----------------

def test_user_list_search_and_pagination(client, admin_token, seeded):
    h = _authed(client, admin_token)
    _, u2_id, _ = seeded
    # 按 id 反查姓名（随机 tag 命名，进程内唯一）
    async def _name():
        from app.database import AsyncSessionLocal
        from app.models import User as U
        async with AsyncSessionLocal() as s:
            return (await s.execute(select(U).where(U.id == u2_id))).scalar_one().name
    name = asyncio.run(_name())
    body = client.get("/api/admin/users", headers=h, params={"keyword": name}).json()
    assert body["code"] == 0 and body["data"]["total"] == 1
    assert body["data"]["items"][0]["id"] == u2_id


def test_user_disable_enable_with_audit(client, admin_token, seeded):
    h = _authed(client, admin_token)
    u1_id, _, _ = seeded
    # 禁用
    body = client.post(f"/api/admin/users/{u1_id}/disable", headers=h).json()
    assert body["code"] == 0 and body["data"]["user"]["status"] == "disabled"
    # 重复禁用被拒
    assert client.post(f"/api/admin/users/{u1_id}/disable", headers=h).json()["code"] == 1409
    # 留痕可查
    logs = client.get("/api/admin/audit-logs", headers=h, params={"action": "user_disable"}).json()
    assert logs["data"]["total"] == 1 and logs["data"]["items"][0]["target_id"] == u1_id
    # 解禁
    body = client.post(f"/api/admin/users/{u1_id}/enable", headers=h).json()
    assert body["code"] == 0 and body["data"]["user"]["status"] == "active"


def test_user_export_csv(client, admin_token, seeded):
    resp = client.get("/api/admin/users/export", headers=_authed(client, admin_token))
    assert resp.status_code == 200
    text = resp.content.decode("utf-8-sig")
    assert text.startswith("id,phone,name,status,created_at") and "张大爷" in text


def test_user_timeline_orders_desc(client, admin_token, seeded):
    h = _authed(client, admin_token)
    from datetime import datetime

    from app.database import AsyncSessionLocal
    from app.models import HealthRecord as HR

    u1_id, _, _ = seeded

    async def _add():
        async with AsyncSessionLocal() as s:
            s.add(HR(user_id=u1_id, diagnosis="早期记录", confidence=0.9,
                     record_date=datetime(2026, 1, 1)))
            await s.commit()

    import asyncio
    asyncio.run(_add())

    body = client.get(f"/api/admin/users/{u1_id}/timeline", headers=h).json()
    assert body["code"] == 0 and len(body["data"]["timeline"]) >= 3
    dates = [t["record_date"] or t["created_at"] for t in body["data"]["timeline"]]
    assert dates == sorted(dates, reverse=True)


# ---------------- 病历解析记录：修正留痕可复跑（验收硬点）----------------

def test_record_list_and_detail(client, admin_token, seeded):
    h = _authed(client, admin_token)
    u1_id, _, _ = seeded
    body = client.get("/api/admin/records", headers=h, params={"user_id": u1_id}).json()
    assert body["code"] == 0 and body["data"]["total"] == 2
    rid = body["data"]["items"][0]["id"]
    detail = client.get(f"/api/admin/records/{rid}", headers=h).json()
    assert detail["code"] == 0 and detail["data"]["id"] == rid


def test_correct_leaves_audit_and_replay(client, admin_token, seeded):
    """验收核心：修正留痕 + corrected_by + 审计快照可复跑。"""
    h = _authed(client, admin_token)
    _, u2_id, _ = seeded
    rec = client.get("/api/admin/records", headers=h, params={"user_id": u2_id}).json()["data"]["items"][0]
    rid = rec["id"]

    # 空修改被拒
    assert client.post(f"/api/admin/records/{rid}/correct", headers=h, json={}).json()["code"] == 1400

    # 修正诊断 + 置信度
    body = client.post(
        f"/api/admin/records/{rid}/correct", headers=h,
        json={"diagnosis": "2型糖尿病", "confidence": 0.88},
    ).json()
    assert body["code"] == 0
    data = body["data"]
    assert data["record"]["status"] == "corrected"
    assert data["record"]["diagnosis"] == "2型糖尿病"
    audit_id = data["audit_id"]

    # corrected_by 留痕 + 审计快照落库
    logs = client.get("/api/admin/audit-logs", headers=h, params={"target_id": rid, "action": "record_correct"}).json()
    assert logs["data"]["total"] == 1
    log = logs["data"]["items"][0]
    assert log["before_snapshot"]["diagnosis"] == "血糖存疑"
    assert log["after_snapshot"]["diagnosis"] == "2型糖尿病"
    assert log["id"] == audit_id

    # 复跑：重放 after_snapshot，结果与快照一致
    replay = client.post(f"/api/admin/audit-logs/{audit_id}/replay", headers=h).json()
    assert replay["code"] == 0 and replay["data"]["matched"] is True
    # 复跑后记录仍是修正后状态
    detail = client.get(f"/api/admin/records/{rid}", headers=h).json()["data"]
    assert detail["diagnosis"] == "2型糖尿病" and detail["confidence"] == 0.88
    # 复跑也留痕（共 2 条 record_correct）
    logs2 = client.get("/api/admin/audit-logs", headers=h, params={"target_id": rid, "action": "record_correct"}).json()
    assert logs2["data"]["total"] == 2


def test_correct_record_date_and_replay(client, admin_token, seeded):
    """record_date 修正 + 复跑（ISO 字符串往返一致性）。"""
    h = _authed(client, admin_token)
    u1_id, _, _ = seeded
    rec = client.get("/api/admin/records", headers=h, params={"user_id": u1_id, "status": "pending"}).json()["data"]["items"][0]
    body = client.post(
        f"/api/admin/records/{rec['id']}/correct", headers=h,
        json={"record_date": "2026-10-01T08:30:00"},
    ).json()
    assert body["code"] == 0 and body["data"]["record"]["record_date"].startswith("2026-10-01T08:30:00")
    replay = client.post(f"/api/admin/audit-logs/{body['data']['audit_id']}/replay", headers=h).json()
    assert replay["code"] == 0 and replay["data"]["matched"] is True


# ---------------- 低置信度报警（验收硬点：报警可见）----------------

def test_low_confidence_alerts_visible_and_sorted(client, admin_token, seeded):
    h = _authed(client, admin_token)
    _, _, base = seeded
    body = client.get("/api/admin/alerts/low-confidence", headers=h).json()
    assert body["code"] == 0
    assert body["data"]["total"] == base["low_confidence_alerts"] + 2  # 本夹具新增 0.42 / 0.55
    confs = [r["confidence"] for r in body["data"]["items"]]
    assert confs == sorted(confs)  # 升序：最可疑排最前
    assert body["data"]["threshold"] == 0.7
    # 修正抬高置信度后，该条退出报警列表
    rid = body["data"]["items"][0]["id"]
    client.post(f"/api/admin/records/{rid}/correct", headers=h, json={"confidence": 0.9})
    after = client.get("/api/admin/alerts/low-confidence", headers=h).json()
    assert after["data"]["total"] == body["data"]["total"] - 1


# ---------------- Web 管理端与统计 ----------------

def test_admin_page_served(client):
    resp = client.get("/admin")
    assert resp.status_code == 200 and "颐康宝" in resp.text


def test_stats_endpoint(client, admin_token, seeded):
    h = _authed(client, admin_token)
    u1_id, u2_id, base = seeded
    body = client.get("/api/admin/stats", headers=h).json()
    assert body["code"] == 0
    assert body["data"]["users"]["total"] == base["users"]["total"] + 2
    assert body["data"]["records"]["total"] == base["records"]["total"] + 3
    assert body["data"]["low_confidence_alerts"] == base["low_confidence_alerts"] + 2


# ---------------- 直接 ORM 校验（不走 HTTP）----------------

async def test_audit_log_model_defaults(session):
    """AuditLog 模型默认值与查询（只认本用例落的那条）。"""
    marker = "model_defaults_marker"
    log = AuditLog(admin_id="a1", action=marker, target_type="record", target_id="r1",
                   before_snapshot={"diagnosis": "旧"}, after_snapshot={"diagnosis": "新"})
    session.add(log)
    await session.commit()
    await session.refresh(log)
    rows = (await session.execute(select(AuditLog).where(AuditLog.action == marker))).scalars().all()
    assert len(rows) == 1 and rows[0].before_snapshot["diagnosis"] == "旧"
