"""用户登录路由（M1 联调配套，Issue #180）。

PRD 2.1「手机号一键登录」的 MVP 版：演示模式验证码（YKB_DEMO_SMS_CODE，
默认 123456），get-or-create 用户，签发 HS256 JWT（复用 admin 基建）。
生产环境接短信网关后替换校验逻辑，接口契约不变。

契约对齐 Android `AuthRepositoryImpl.kt` / `AuthModels.kt`：
- POST /api/auth/login      {phone, code} → {token, userId, phone, name}
- GET  /api/user/profile    Bearer token → 用户档案视图
"""
from __future__ import annotations

import os
import re

from fastapi import APIRouter, Depends, Header
from pydantic import BaseModel
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from ..database import get_db
from ..models import User
from .admin import issue_token, parse_token
from .response import fail, ok

router = APIRouter(tags=["auth"])

_PHONE_RE = re.compile(r"^1[3-9]\d{9}$")


class LoginRequest(BaseModel):
    phone: str
    code: str


@router.post("/auth/login")
async def login(req: LoginRequest, db: AsyncSession = Depends(get_db)) -> dict:
    """演示一键登录：验证码校验（演示码）→ get-or-create 用户 → 签 JWT。"""
    if not _PHONE_RE.match(req.phone):
        return fail(1001, "手机号格式不对")
    demo_code = os.environ.get("YKB_DEMO_SMS_CODE", "123456")
    if req.code != demo_code:
        return fail(1001, "验证码错误（演示模式固定验证码，见 YKB_DEMO_SMS_CODE）")

    u = (await db.execute(select(User).where(User.phone == req.phone))).scalar_one_or_none()
    if u is None:
        u = User(phone=req.phone, name=f"用户{req.phone[-4:]}")
        db.add(u)
        await db.commit()
        await db.refresh(u)
    return ok({
        "token": issue_token(u.id, "user"),
        "userId": u.id,
        "phone": u.phone,
        "name": u.name,
    })


def _user_view(u: User) -> dict:
    """用户视图（字段名对齐 Android User 模型）。"""
    return {
        "id": u.id,
        "phone": u.phone,
        "name": u.name,
        "avatarUrl": None,
        "gender": None,
        "birthDate": None,
        "status": u.status,
        "createdAt": u.created_at.strftime("%Y-%m-%d %H:%M:%S") if u.created_at else "",
    }


@router.get("/user/profile")
async def profile(
    authorization: str = Header(default=""),
    db: AsyncSession = Depends(get_db),
) -> dict:
    """当前用户档案（Bearer JWT）。"""
    token = authorization.removeprefix("Bearer ").strip()
    claims = parse_token(token)
    if claims is None or claims.get("role") != "user":
        return fail(4010, "登录已过期，请重新登录")
    u = (await db.execute(select(User).where(User.id == claims["sub"]))).scalar_one_or_none()
    if u is None:
        return fail(1001, "用户不存在")
    return ok(_user_view(u))
