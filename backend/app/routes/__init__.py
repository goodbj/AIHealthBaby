"""API 统一返回封装（PRD 6.3：所有 API 返回 { code, msg, data, trace_id }）。"""
from __future__ import annotations

import uuid
from typing import Any

from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession

from ..config import load_llm_config
from ..database import get_db
from ..llm import get_llm
from ..models import Conversation
from ..services.context import build_health_context, context_summary
from .response import fail, ok
from .reminders import router as reminders_router

api_router = APIRouter()
api_router.include_router(reminders_router)

# M3：后台管理子路由（用户管理/记录修正/低置信度报警/审计留痕）
from .admin import admin_router  # noqa: E402

api_router.include_router(admin_router, prefix="/admin")


class ChatRequest(BaseModel):
    """对话请求体（M2：注入用户健康档案上下文，落库留痕）。"""

    prompt: str
    user_id: str | None = None  # 缺省匿名：不注入档案，仅通用建议
    model: str | None = None


@api_router.post("/chat")
async def chat(req: ChatRequest, db: AsyncSession = Depends(get_db)) -> JSONResponse:
    """健康助手对话端点：注入健康档案上下文，回复统一附带免责声明。

    - 带 user_id：取最近病历/指标/用药 + 最近对话历史注入 LLM context
    - 注入留痕写 conversations.injected_context（验收可复跑：同请求同档案同注入）
    """
    if not req.prompt.strip():
        return fail(1001, "prompt 不能为空")
    llm = get_llm(model_name=req.model, config=load_llm_config())

    injected: list = []
    context: dict | None = None
    if req.user_id:
        ctx = await build_health_context(db, req.user_id)
        context = {"history": ctx["history"], "profile": ctx["profile"]}
        injected = context_summary(ctx)

    try:
        reply = llm.chat(req.prompt, context)
    except Exception:  # noqa: BLE001 — LLM 层异常兜底，不让 500 打到用户
        return fail(1002, "模型服务暂时不可用，请稍后再试")

    # 对话落库（匿名对话不落，user_id 非空才留痕）
    if req.user_id:
        conv = Conversation(
            user_id=req.user_id,
            question=req.prompt,
            answer=reply,
            injected_context=injected,
            model_used=llm.name(),
        )
        db.add(conv)
        await db.commit()

    return ok({"reply": reply, "model": llm.name(), "disclaimer": True, "injected": injected})


@api_router.get("/health")
async def health() -> JSONResponse:
    """健康检查（M0 验收硬性指标：返回 200）。"""
    return ok({"status": "healthy", "service": "YiKangBao"})


__all__ = ["api_router", "ok", "fail", "health", "chat"]
