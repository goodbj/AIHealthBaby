"""提醒业务（Issue #181 · M2 提醒调度）。

- dispatch_due_reminders：把到期 pending 提醒置 sent，触发 after_reminder_sent 钩子
- red_dot_count：站内红点统计
- create_reminder：CRUD 的 C
"""
from __future__ import annotations

import logging
from datetime import datetime, timezone

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from ..hooks import trigger_hook
from ..models import Reminder

_LOG = logging.getLogger("ykb.reminders")

# 单轮派发上限：防止历史积压一次性打爆钩子/日志
DISPATCH_BATCH = 100


async def dispatch_due_reminders(session: AsyncSession, now: datetime | None = None) -> list[dict]:
    """把 scheduled_time <= now 的 pending 提醒置 sent 并触发钩子。

    返回派发清单 [{id, user_id, content}]（供调度器日志与测试断言）。
    状态机：pending → sent（幂等：sent/cancelled 不重复派发）。
    并发安全：靠 status='pending' 条件提交，重复调度不会二次派发同一条。
    """
    now = now or datetime.now(timezone.utc)
    due = (
        await session.execute(
            select(Reminder)
            .where(
                Reminder.status == "pending",
                Reminder.scheduled_time <= now,
            )
            .order_by(Reminder.scheduled_time.asc())
            .limit(DISPATCH_BATCH)
        )
    ).scalars().all()

    dispatched: list[dict] = []
    for rem in due:
        rem.status = "sent"
        rem.sent_at = now
        dispatched.append({"id": rem.id, "user_id": rem.user_id, "content": rem.content})
    if dispatched:
        await session.commit()
        for item in dispatched:
            await trigger_hook(
                "after_reminder_sent",
                reminder_id=item["id"],
                user_id=item["user_id"],
                content=item["content"],
            )
        _LOG.info("dispatched %d due reminders", len(dispatched))
    return dispatched


async def create_reminder(session: AsyncSession, user_id: str, **fields) -> Reminder:
    """建提醒（CRUD 的 C），默认 pending。"""
    rem = Reminder(user_id=user_id, **fields)
    session.add(rem)
    await session.commit()
    await session.refresh(rem)
    return rem


async def red_dot_count(session: AsyncSession, user_id: str) -> int:
    """站内红点数：该用户 sent 且未读（read_at 为空）的提醒条数。"""
    rows = (
        await session.execute(
            select(Reminder).where(
                Reminder.user_id == user_id,
                Reminder.status == "sent",
                Reminder.read_at.is_(None),
            )
        )
    ).scalars().all()
    return len(rows)
