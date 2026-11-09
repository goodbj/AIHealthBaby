"""API 统一返回封装（PRD 6.3：所有 API 返回 { code, msg, data, trace_id }）。"""
from __future__ import annotations

import uuid
from typing import Any

from fastapi import APIRouter
from fastapi.responses import JSONResponse
from pydantic import BaseModel

from ..config import load_llm_config
from ..llm import get_llm

api_router = APIRouter()


def ok(data: Any = None, msg: str = "ok") -> JSONResponse:
    """成功响应封装。"""
    return JSONResponse({"code": 0, "msg": msg, "data": data, "trace_id": uuid.uuid4().hex})


def fail(code: int, msg: str, data: Any = None) -> JSONResponse:
    """失败响应封装。"""
    return JSONResponse(
        # 仅 5xx 服务端错误码（500~999）映射 HTTP 500；业务错误码（>=1000）保持 HTTP 200，
        # 由 body.code 区分（PRD 6.3 统一响应封装约定）。
        {"code": code, "msg": msg, "data": data, "trace_id": uuid.uuid4().hex},
        status_code=500 if 500 <= code < 1000 else 200,
    )


class ChatRequest(BaseModel):
    """对话请求体（M0：单轮对话，多轮上下文 M1 扩展）。"""

    prompt: str
    model: str | None = None


@api_router.post("/chat")
async def chat(req: ChatRequest) -> JSONResponse:
    """健康助手对话端点：LLM 回复统一附带免责声明。"""
    if not req.prompt.strip():
        return fail(1001, "prompt 不能为空")
    llm = get_llm(model_name=req.model, config=load_llm_config())
    try:
        reply = llm.chat(req.prompt)
    except Exception:  # noqa: BLE001 — LLM 层异常兜底，不让 500 打到用户
        return fail(1002, "模型服务暂时不可用，请稍后再试")
    return ok({"reply": reply, "model": llm.name(), "disclaimer": True})


@api_router.get("/health")
async def health() -> JSONResponse:
    """健康检查（M0 验收硬性指标：返回 200）。"""
    return ok({"status": "healthy", "service": "YiKangBao"})


# M3：后台管理子路由挂载（用户管理/记录修正/低置信度报警/审计留痕）
from .admin import admin_router

api_router.include_router(admin_router, prefix="/admin")

# 模块级路由占位，便于后续 M1/M2 挂载各业务子路由。
__all__ = ["api_router", "ok", "fail", "health", "chat"]
