"""测试 10-13：ORM 模型落库 / 调度器构建 / 异步会话。"""
from __future__ import annotations

from sqlalchemy import select

from app.models import HealthRecord, Reminder, User
from app.scheduler import build_scheduler


async def test_user_insert_and_query(session):
    u = User(phone="13800138000", name="测试老人")
    session.add(u)
    await session.commit()
    await session.refresh(u)
    rows = (await session.execute(select(User).where(User.phone == "13800138000"))).scalars().all()
    assert len(rows) == 1 and rows[0].id == u.id


async def test_health_record_defaults(session):
    u = User(phone="13900139000")
    session.add(u)
    await session.commit()
    rec = HealthRecord(user_id=u.id, diagnosis="高血压", confidence=0.92, status="pending")
    session.add(rec)
    await session.commit()
    await session.refresh(rec)
    assert rec.status == "pending" and rec.indicators == []


async def test_reminder_model(session):
    u = User(phone="13700137000")
    session.add(u)
    await session.commit()
    rem = Reminder(user_id=u.id, type="medication", content="阿司匹林早餐后", scheduled_time=__import__("datetime").datetime(2026, 11, 10, 8, 0))
    session.add(rem)
    await session.commit()
    await session.refresh(rem)
    assert rem.status == "pending"


def test_scheduler_builds_with_jobs():
    sched = build_scheduler()
    assert sched.get_job("heartbeat") is not None
    if sched.running:
        sched.shutdown(wait=False)
