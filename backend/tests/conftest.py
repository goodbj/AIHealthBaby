"""测试公共夹具：提供测试用异步会话与应用客户端。"""
from __future__ import annotations

import os
import tempfile

import pytest
import pytest_asyncio
from fastapi.testclient import TestClient

os.environ.setdefault("YKB_DATABASE_URL", "sqlite+aiosqlite:///:memory:")


@pytest.fixture
def client():
    """同步测试客户端（TestClient 内部驱动事件循环）。"""
    from app.main import app

    with TestClient(app) as c:
        yield c


@pytest_asyncio.fixture
async def session():
    """内存库异步会话，自动建表。"""
    from app.database import AsyncSessionLocal, Base, engine, init_db

    await init_db()
    async with AsyncSessionLocal() as s:
        yield s
    await engine.dispose()
