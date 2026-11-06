"""测试 1-3：统一响应封装 / 健康检查 / 配置加载。"""
from __future__ import annotations

import json

from app.config import get_settings, load_llm_config
from app.routes import fail, ok


def test_health_returns_200(client):
    resp = client.get("/api/health")
    assert resp.status_code == 200
    body = resp.json()
    assert body["code"] == 0
    assert body["data"]["status"] == "healthy"
    assert "trace_id" in body


def test_ok_wrapper_shape():
    r = ok({"k": 1}, msg="done")
    body = json.loads(r.body)
    assert body["code"] == 0 and body["msg"] == "done" and body["data"] == {"k": 1}


def test_fail_wrapper_shape():
    r = fail(400, "bad")
    body = json.loads(r.body)
    assert body["code"] == 400 and body["msg"] == "bad"


def test_settings_defaults():
    s = get_settings()
    assert s.app_name == "YiKangBao"
    assert s.database_url.startswith("sqlite")


def test_load_llm_config_fallback(tmp_path, monkeypatch):
    monkeypatch.setattr("app.config.get_settings", lambda: type("S", (), {"llm_config_path": str(tmp_path / "nope.json")})())
    cfg = load_llm_config()
    assert cfg["active_model"] == "deepseek"
