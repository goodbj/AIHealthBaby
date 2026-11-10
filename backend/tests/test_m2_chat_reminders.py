"""M2 测试（Issue #181）：对话注入档案 + 提醒 CRUD/调度/红点。"""
from __future__ import annotations

from datetime import datetime, timedelta, timezone

from sqlalchemy import select

from app.models import Conversation, HealthIndicator, HealthRecord, Reminder, User
from app.services.context import build_health_context, context_summary
from app.services.reminders import dispatch_due_reminders, red_dot_count


async def _mk_user(session, phone="13611112222") -> User:
    u = User(phone=phone, name="M2测试")
    session.add(u)
    await session.commit()
    await session.refresh(u)
    return u


# --- 对话注入档案 ---


async def test_build_health_context_with_profile(session):
    """有档案用户：注入病历+指标+用药摘要。"""
    u = await _mk_user(session)
    session.add(
        HealthRecord(
            user_id=u.id,
            diagnosis="高血压",
            medications=[{"name": "氨氯地平", "dosage": "5mg", "frequency": "每日一次"}],
            indicators=[{"name": "收缩压", "value": "150"}],
            status="confirmed",
        )
    )
    session.add(HealthIndicator(user_id=u.id, type="blood_pressure", value="150/95"))
    await session.commit()

    ctx = await build_health_context(session, u.id)
    assert ctx["profile"] is not None
    assert ctx["profile"]["recent_records"][0]["diagnosis"] == "高血压"
    assert ctx["profile"]["latest_indicators"][0]["type"] == "blood_pressure"
    assert ctx["history"] == []
    summary = context_summary(ctx)
    assert summary[0]["profile_injected"] is True
    assert summary[0]["record_count"] == 1


async def test_build_health_context_without_profile(session):
    """无档案用户：profile 为 None，仍可回答通用建议。"""
    u = await _mk_user(session, phone="13633334444")
    ctx = await build_health_context(session, u.id)
    assert ctx["profile"] is None
    assert context_summary(ctx)[0]["profile_injected"] is False


async def test_build_health_context_history_order(session):
    """对话历史按时间正序展开（旧在前，问答成对）。"""
    u = await _mk_user(session, phone="13655556666")
    base = datetime(2026, 11, 1, 8, 0)
    for i, (q, a) in enumerate([("Q1", "A1"), ("Q2", "A2")]):
        session.add(
            Conversation(user_id=u.id, question=q, answer=a, injected_context=[], created_at=base + timedelta(days=i))
        )
    await session.commit()
    ctx = await build_health_context(session, u.id)
    assert [(m["role"], m["content"]) for m in ctx["history"]] == [
        ("user", "Q1"), ("assistant", "A1"), ("user", "Q2"), ("assistant", "A2"),
    ]


async def test_chat_api_injects_profile_and_persists(client, session):
    """端到端：/api/chat 带 user_id → 注入档案 + conversations 落库 + 免责声明。"""
    u = await _mk_user(session, phone="13677778888")
    session.add(HealthRecord(user_id=u.id, diagnosis="糖尿病", status="confirmed"))
    await session.commit()

    resp = client.post("/api/chat", json={"prompt": "晚饭吃什么好", "user_id": u.id})
    assert resp.status_code == 200
    body = resp.json()
    assert body["code"] == 0
    assert body["data"]["disclaimer"] is True
    assert body["data"]["injected"][0]["profile_injected"] is True
    assert body["data"]["injected"][0]["record_count"] == 1

    rows = (await session.execute(select(Conversation).where(Conversation.user_id == u.id))).scalars().all()
    assert len(rows) == 1
    assert rows[0].question == "晚饭吃什么好"
    assert rows[0].model_used


async def test_chat_api_anonymous_no_injection(client, session):
    """匿名对话：不注入档案、不落库，仍带免责声明。"""
    resp = client.post("/api/chat", json={"prompt": "怎么睡个好觉"})
    assert resp.status_code == 200
    body = resp.json()
    assert body["data"]["injected"] == []
    rows = (await session.execute(select(Conversation))).scalars().all()
    assert len(rows) == 0


async def test_chat_api_empty_prompt(client):
    resp = client.post("/api/chat", json={"prompt": "   "})
    assert resp.json()["code"] == 1001


# --- 提醒 CRUD + 红点 ---


def _payload(user_id: str, minutes_ahead: int = 60, **kw) -> dict:
    st = datetime.now(timezone.utc) + timedelta(minutes=minutes_ahead)
    return {"user_id": user_id, "type": "medication", "content": "阿司匹林早餐后", "scheduled_time": st.isoformat(), **kw}


async def test_reminder_crud_full_cycle(client, session):
    """建→查→改→删 全链路。"""
    u = await _mk_user(session, phone="13699990000")
    # C
    resp = client.post("/api/reminders", json=_payload(u.id))
    assert resp.json()["code"] == 0
    rid = resp.json()["data"]["id"]
    # R
    resp = client.get(f"/api/reminders/{rid}")
    assert resp.json()["data"]["status"] == "pending"
    # U
    resp = client.patch(f"/api/reminders/{rid}", json={"content": "阿司匹林午餐后", "status": "cancelled"})
    assert resp.json()["data"]["content"] == "阿司匹林午餐后"
    assert resp.json()["data"]["status"] == "cancelled"
    # D
    resp = client.delete(f"/api/reminders/{rid}")
    assert resp.json()["data"]["deleted"] is True
    assert (await session.get(Reminder, rid)) is None


async def test_reminder_list_filter(client, session):
    u = await _mk_user(session, phone="13612123434")
    session.add(Reminder(user_id=u.id, type="diet", content="少盐", scheduled_time=datetime(2026, 11, 11, 8, 0), status="sent"))
    await session.commit()
    resp = client.get("/api/reminders", params={"user_id": u.id, "status": "sent"})
    data = resp.json()["data"]
    assert len(data) == 1 and data[0]["content"] == "少盐"


async def test_reminder_invalid_type(client, session):
    u = await _mk_user(session, phone="13613134545")
    resp = client.post("/api/reminders", json=_payload(u.id, type="foo"))
    assert resp.json()["code"] == 2001


# --- 调度派发 + 红点 ---


async def test_dispatch_due_reminders_and_red_dot(session):
    """到期 pending → sent + 钩子；红点计数与已读清零。"""
    from app.hooks import clear_hooks, register_hook

    u = await _mk_user(session, phone="13614145656")
    past = datetime.now(timezone.utc).replace(tzinfo=None) - timedelta(minutes=5)
    future = datetime.now(timezone.utc).replace(tzinfo=None) + timedelta(minutes=5)
    r1 = Reminder(user_id=u.id, type="medication", content="降压药", scheduled_time=past)
    r2 = Reminder(user_id=u.id, type="followup", content="未到期", scheduled_time=future)
    session.add_all([r1, r2])
    await session.commit()

    fired: list[dict] = []
    register_hook("after_reminder_sent", lambda **kw: fired.append(kw))
    try:
        dispatched = await dispatch_due_reminders(session)
    finally:
        clear_hooks()

    assert [d["id"] for d in dispatched] == [r1.id]
    await session.refresh(r1)
    assert r1.status == "sent" and r1.sent_at is not None
    assert fired and fired[0]["reminder_id"] == r1.id

    # 红点：sent 未读 = 1
    assert await red_dot_count(session, u.id) == 1
    # 全部已读（走 API 路由同款逻辑，这里直接置已读）→ 清零
    now = datetime.now(timezone.utc).replace(tzinfo=None)
    unread = (
        await session.execute(
            select(Reminder).where(
                Reminder.user_id == u.id,
                Reminder.status == "sent",
                Reminder.read_at.is_(None),
            )
        )
    ).scalars().all()
    for rem in unread:
        rem.read_at = now
    await session.commit()
    assert await red_dot_count(session, u.id) == 0


async def test_dispatch_idempotent(session):
    """重复派发幂等：已 sent 的不二次派发。"""
    u = await _mk_user(session, phone="13615167878")
    past = datetime.now(timezone.utc).replace(tzinfo=None) - timedelta(minutes=5)
    session.add(Reminder(user_id=u.id, type="diet", content="控糖", scheduled_time=past))
    await session.commit()
    first = await dispatch_due_reminders(session)
    second = await dispatch_due_reminders(session)
    assert len(first) == 1 and second == []
