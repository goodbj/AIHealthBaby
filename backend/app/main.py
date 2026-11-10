"""FastAPI 应用入口（M0 骨架 + M3 后台管理）。

装配：配置、ORM、统一响应、钩子注册、APScheduler 生命周期、后台管理路由。
业务子路由（auth/records/chat/reminders/indicators）在后续里程碑挂载。
"""
from __future__ import annotations

import logging
from pathlib import Path

from fastapi import FastAPI
from fastapi.responses import FileResponse

from .config import get_settings
from .database import AsyncSessionLocal, init_db
from .hooks import (
    clear_hooks,
    log_after_record_parsed,
    log_after_reminder_sent,
    register_hook,
)
from .routes import api_router
from .routes.admin import _AuthError, auth_error_handler, ensure_default_admin
from .scheduler import build_scheduler

logging.basicConfig(level=logging.INFO)
_LOG = logging.getLogger("ykb")

settings = get_settings()
app = FastAPI(title=settings.app_name, version="0.2.0-m3")
_scheduler = build_scheduler()

# 后台管理鉴权异常 → 统一响应封装
app.add_exception_handler(_AuthError, auth_error_handler)

# M3 Web 管理端：单文件静态页，直接由后端服务（/admin）
_STATIC_DIR = Path(__file__).resolve().parent.parent / "static"


@app.get("/admin", include_in_schema=False)
async def admin_page() -> FileResponse:
    """M3 Web 管理端（backend/static/admin.html）。"""
    return FileResponse(_STATIC_DIR / "admin.html")


@app.on_event("startup")
async def _startup() -> None:
    await init_db()
    # M3：空表时播种默认管理员（admin / YKB_ADMIN_PASSWORD，默认 admin123）
    async with AsyncSessionLocal() as session:
        await ensure_default_admin(session)
    # 注册 M0 示例钩子（满足 PRD 4.3 至少 2 个示例钩子的要求）
    clear_hooks()
    register_hook("after_record_parsed", log_after_record_parsed)
    register_hook("after_reminder_sent", log_after_reminder_sent)
    _scheduler.start()
    _LOG.info("%s M0/M3 启动完成（health/db/hooks/scheduler/admin 就绪）", settings.app_name)


@app.on_event("shutdown")
async def _shutdown() -> None:
    if _scheduler.running:
        _scheduler.shutdown(wait=False)


app.include_router(api_router, prefix="/api")
