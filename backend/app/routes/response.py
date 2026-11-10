"""统一响应封装（PRD 6.3：所有 API 返回 { code, msg, data, trace_id }）。

独立模块，避免 routes/__init__ 与子路由互相 import 成环。
"""
from __future__ import annotations

import uuid
from typing import Any

from fastapi.responses import JSONResponse


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
