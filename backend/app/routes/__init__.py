"""API 统一返回封装（PRD 6.3：所有 API 返回 { code, msg, data, trace_id }）。"""
from __future__ import annotations

import uuid
from typing import Any

from fastapi import APIRouter
from fastapi.responses import JSONResponse

api_router = APIRouter()


def ok(data: Any = None, msg: str = "ok") -> JSONResponse:
    """成功响应封装。"""
    return JSONResponse({"code": 0, "msg": msg, "data": data, "trace_id": uuid.uuid4().hex})


def fail(code: int, msg: str, data: Any = None) -> JSONResponse:
    """失败响应封装。"""
    return JSONResponse(
        {"code": code, "msg": msg, "data": data, "trace_id": uuid.uuid4().hex},
        status_code=200 if code < 500 else 500,
    )


@api_router.get("/health")
async def health() -> JSONResponse:
    """健康检查（M0 验收硬性指标：返回 200）。"""
    return ok({"status": "healthy", "service": "YiKangBao"})


# 模块级路由占位，便于后续 M1/M2 挂载各业务子路由。
__all__ = ["api_router", "ok", "fail", "health"]
