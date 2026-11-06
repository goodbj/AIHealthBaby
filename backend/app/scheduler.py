"""后台任务调度器（APScheduler）。

M0 落地调度器骨架：用于后续定时提醒（用药/复诊/饮食）。
当前挂载一个占位心跳任务，证明调度器可按期触发；
真实提醒投递在 M2 接入 reminders 表后启用。
"""
from __future__ import annotations

import logging

from apscheduler.schedulers.asyncio import AsyncIOScheduler

_LOG = logging.getLogger("ykb.scheduler")


def _heartbeat() -> None:
    _LOG.info("scheduler heartbeat tick")


def build_scheduler() -> AsyncIOScheduler:
    """构建并配置调度器（不启动）。"""
    sched = AsyncIOScheduler(timezone="Asia/Shanghai")
    sched.add_job(_heartbeat, "interval", minutes=30, id="heartbeat")
    return sched
