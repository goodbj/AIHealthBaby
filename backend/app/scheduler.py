"""后台任务调度器（APScheduler，Issue #181 · M2 提醒调度）。

定时任务：
- dispatch_due_reminders：每 30 秒扫一次到期 pending 提醒，置 sent + 触发钩子（红点亮起）
- heartbeat：30 分钟一次，证明调度器存活
"""
from __future__ import annotations

import logging

from apscheduler.schedulers.asyncio import AsyncIOScheduler

from .database import AsyncSessionLocal
from .services.reminders import dispatch_due_reminders

_LOG = logging.getLogger("ykb.scheduler")


def _heartbeat() -> None:
    _LOG.info("scheduler heartbeat tick")


async def _dispatch_job() -> None:
    """到期提醒派发任务（独立会话，不占请求连接池）。"""
    async with AsyncSessionLocal() as session:
        try:
            await dispatch_due_reminders(session)
        except Exception:  # noqa: BLE001 — 调度任务异常不让进程挂
            _LOG.exception("dispatch_due_reminders failed")


def build_scheduler() -> AsyncIOScheduler:
    """构建并配置调度器（不启动）。"""
    sched = AsyncIOScheduler(timezone="Asia/Shanghai")
    sched.add_job(_dispatch_job, "interval", seconds=30, id="dispatch_due_reminders")
    sched.add_job(_heartbeat, "interval", minutes=30, id="heartbeat")
    return sched
