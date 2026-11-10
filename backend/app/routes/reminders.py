"""提醒路由（Issue #181 · M2）：CRUD + 站内红点。

契约（对齐 README API 列表）：
- POST   /api/reminders                 建提醒
- GET    /api/reminders                 列表（可按 user_id/status 过滤）
- GET    /api/reminders/{id}            详情
- PATCH  /api/reminders/{id}            改内容/时间/取消
- DELETE /api/reminders/{id}            删除
- GET    /api/reminders/red-dot/count   红点数（sent 且未读）
- POST   /api/reminders/red-dot/read    全部已读（红点清零）
"""
from __future__ import annotations

from datetime import datetime, timezone
from typing import Any

from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from ..database import get_db
from ..models import Reminder
from .response import fail, ok

router = APIRouter(prefix="/reminders", tags=["reminders"])

VALID_TYPES = {"medication", "followup", "diet"}
VALID_STATUSES = {"pending", "sent", "cancelled"}


class ReminderCreate(BaseModel):
    """建提醒请求体。"""

    user_id: str
    type: str = "medication"
    content: str = Field(min_length=1)
    scheduled_time: datetime


class ReminderUpdate(BaseModel):
    """改提醒请求体（部分更新）。"""

    type: str | None = None
    content: str | None = Field(default=None, min_length=1)
    scheduled_time: datetime | None = None
    status: str | None = None  # pending/sent/cancelled


def _dump(rem: Reminder) -> dict[str, Any]:
    return {
        "id": rem.id,
        "user_id": rem.user_id,
        "type": rem.type,
        "content": rem.content,
        "scheduled_time": rem.scheduled_time.isoformat() if rem.scheduled_time else None,
        "status": rem.status,
        "sent_at": rem.sent_at.isoformat() if rem.sent_at else None,
        "read_at": rem.read_at.isoformat() if rem.read_at else None,
    }


@router.post("")
async def create_reminder(req: ReminderCreate, db: AsyncSession = Depends(get_db)) -> JSONResponse:
    """创建提醒（默认 pending）。"""
    if req.type not in VALID_TYPES:
        return fail(2001, f"type 须为 {sorted(VALID_TYPES)} 之一")
    if req.scheduled_time.tzinfo is not None:
        req.scheduled_time = req.scheduled_time.astimezone(timezone.utc).replace(tzinfo=None)
    rem = Reminder(
        user_id=req.user_id,
        type=req.type,
        content=req.content.strip(),
        scheduled_time=req.scheduled_time,
    )
    db.add(rem)
    await db.commit()
    await db.refresh(rem)
    return ok(_dump(rem), msg="提醒已创建")


@router.get("")
async def list_reminders(
    user_id: str | None = None,
    status: str | None = None,
    db: AsyncSession = Depends(get_db),
) -> JSONResponse:
    """提醒列表，可按 user_id / status 过滤。"""
    if status is not None and status not in VALID_STATUSES:
        return fail(2002, f"status 须为 {sorted(VALID_STATUSES)} 之一")
    stmt = select(Reminder).order_by(Reminder.scheduled_time.asc()).limit(200)
    if user_id is not None:
        stmt = stmt.where(Reminder.user_id == user_id)
    if status is not None:
        stmt = stmt.where(Reminder.status == status)
    rows = (await db.execute(stmt)).scalars().all()
    return ok([_dump(r) for r in rows])


@router.get("/red-dot/count")
async def red_dot_count(user_id: str, db: AsyncSession = Depends(get_db)) -> JSONResponse:
    """站内红点数：该用户 sent 且未读的提醒条数。"""
    from ..services.reminders import red_dot_count as _count

    return ok({"user_id": user_id, "count": await _count(db, user_id)})


@router.post("/red-dot/read")
async def mark_all_read(user_id: str, db: AsyncSession = Depends(get_db)) -> JSONResponse:
    """全部已读：sent 且未读的提醒 read_at 置当前时间，红点清零。"""
    now = datetime.now(timezone.utc).replace(tzinfo=None)
    rows = (
        await db.execute(
            select(Reminder).where(
                Reminder.user_id == user_id,
                Reminder.status == "sent",
                Reminder.read_at.is_(None),
            )
        )
    ).scalars().all()
    for rem in rows:
        rem.read_at = now
    await db.commit()
    return ok({"user_id": user_id, "marked": len(rows)})


@router.get("/{reminder_id}")
async def get_reminder(reminder_id: str, db: AsyncSession = Depends(get_db)) -> JSONResponse:
    """提醒详情。"""
    rem = await db.get(Reminder, reminder_id)
    if rem is None:
        return fail(2003, "提醒不存在")
    return ok(_dump(rem))


@router.patch("/{reminder_id}")
async def update_reminder(
    reminder_id: str, req: ReminderUpdate, db: AsyncSession = Depends(get_db)
) -> JSONResponse:
    """部分更新：内容/时间/类型/状态（如取消）。"""
    rem = await db.get(Reminder, reminder_id)
    if rem is None:
        return fail(2003, "提醒不存在")
    if req.type is not None and req.type not in VALID_TYPES:
        return fail(2001, f"type 须为 {sorted(VALID_TYPES)} 之一")
    if req.status is not None and req.status not in VALID_STATUSES:
        return fail(2002, f"status 须为 {sorted(VALID_STATUSES)} 之一")
    if req.type is not None:
        rem.type = req.type
    if req.content is not None:
        rem.content = req.content.strip()
    if req.scheduled_time is not None:
        st = req.scheduled_time
        rem.scheduled_time = st.astimezone(timezone.utc).replace(tzinfo=None) if st.tzinfo else st
    if req.status is not None:
        rem.status = req.status
    await db.commit()
    await db.refresh(rem)
    return ok(_dump(rem), msg="提醒已更新")


@router.delete("/{reminder_id}")
async def delete_reminder(reminder_id: str, db: AsyncSession = Depends(get_db)) -> JSONResponse:
    """删除提醒。"""
    rem = await db.get(Reminder, reminder_id)
    if rem is None:
        return fail(2003, "提醒不存在")
    await db.delete(rem)
    await db.commit()
    return ok({"id": reminder_id, "deleted": True}, msg="提醒已删除")
