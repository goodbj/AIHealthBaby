"""FastAPI 应用入口（M0 骨架）。

装配：配置、ORM、统一响应、钩子注册、APScheduler 生命周期。
业务子路由（auth/records/chat/reminders/indicators）在后续里程碑挂载。
"""
from __future__ import annotations

import logging

from fastapi import FastAPI

from .config import get_settings
from .database import init_db
from .hooks import (
    clear_hooks,
    log_after_record_parsed,
    log_after_reminder_sent,
    register_hook,
)
from .routes import api_router
from .scheduler import build_scheduler

logging.basicConfig(level=logging.INFO)
_LOG = logging.getLogger("ykb")

settings = get_settings()
app = FastAPI(title=settings.app_name, version="0.1.0-m0")
_scheduler = build_scheduler()


@app.on_event("startup")
async def _startup() -> None:
    await init_db()
    # 注册 M0 示例钩子（满足 PRD 4.3 至少 2 个示例钩子的要求）
    clear_hooks()
    register_hook("after_record_parsed", log_after_record_parsed)
    register_hook("after_reminder_sent", log_after_reminder_sent)
    _scheduler.start()
    _LOG.info("%s M0 启动完成（health/db/hooks/scheduler 就绪）", settings.app_name)


@app.on_event("shutdown")
async def _shutdown() -> None:
    if _scheduler.running:
        _scheduler.shutdown(wait=False)


app.include_router(api_router, prefix="/api")
