"""数据库引擎与会话（SQLAlchemy 2.x）。

开发用 SQLite；生产通过 YKB_DATABASE_URL 切 PostgreSQL。
全程用 AsyncSession，多租户字段 tenant_id 在模型中预留。
"""
from __future__ import annotations

from collections.abc import AsyncGenerator

from sqlalchemy.ext.asyncio import (
    AsyncSession,
    async_sessionmaker,
    create_async_engine,
)
from sqlalchemy.orm import DeclarativeBase

from .config import get_settings


class Base(DeclarativeBase):
    """所有 ORM 模型的基类。"""


def _make_engine():
    url = get_settings().database_url
    # 裸 sqlite:// 才补 aiosqlite 异步驱动前缀；已带驱动的不重复处理
    if url.startswith("sqlite://"):
        url = url.replace("sqlite://", "sqlite+aiosqlite://", 1)
    connect_args = {"check_same_thread": False} if url.startswith("sqlite") else {}
    return create_async_engine(url, echo=get_settings().debug, future=True, connect_args=connect_args)


engine = _make_engine()
AsyncSessionLocal = async_sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)


async def get_db() -> AsyncGenerator[AsyncSession, None]:
    """FastAPI 依赖：提供请求级异步会话。"""
    async with AsyncSessionLocal() as session:
        yield session


async def init_db() -> None:
    """创建所有表（开发期用；生产走 Alembic 迁移）。"""
    # 延迟导入模型，确保元数据注册完整
    from . import models  # noqa: F401

    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
