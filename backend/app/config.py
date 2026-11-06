"""应用配置：全部从环境变量或 llm_config.json 读取，不写死密钥。

M0 仅落地配置体系骨架；密钥（DeepSeek api_key 等）走环境变量注入，
绝不明文入库。生产环境相关开关（DB、存储、加密）均在此集中管理。
"""
from __future__ import annotations

import json
import os
from functools import lru_cache
from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict

DEFAULT_LLM_CONFIG_PATH = Path(__file__).resolve().parent.parent / "config" / "llm_config.json"


class Settings(BaseSettings):
    """全局配置，优先读环境变量（前缀 YKB_），兜底默认值。"""

    model_config = SettingsConfigDict(env_prefix="YKB_", extra="ignore")

    app_name: str = "YiKangBao"
    debug: bool = False

    # 数据库：开发 SQLite，生产 PostgreSQL
    database_url: str = "sqlite:///./yikangbao.db"

    # JWT / 鉴权
    jwt_secret: str = "dev-secret-change-me"
    jwt_algorithm: str = "HS256"
    access_token_expire_minutes: int = 60 * 24

    # 加密 at rest（病历图片 / 敏感字段）
    field_crypto_key: str = "dev-32-byte-key-for-aes256-0000000000"  # 32 bytes 占位，生产必换

    # 对象存储：本地模拟（生产切 OSS/COS）
    storage_root: str = "./storage"

    # LLM 配置路径
    llm_config_path: str = str(DEFAULT_LLM_CONFIG_PATH)

    # 并发 / 性能（PRD 6.2：初期 100 QPS）
    max_concurrency: int = 100


@lru_cache
def get_settings() -> Settings:
    """读取（并缓存）全局配置。"""
    return Settings()


def load_llm_config(path: str | None = None) -> dict:
    """读取 LLM 抽象层配置（active_model / api_key / base_url）。

    缺失文件时返回缺省占位，便于离线跑通骨架与测试。
    """
    cfg_path = Path(path or get_settings().llm_config_path)
    if not cfg_path.exists():
        return {"active_model": "deepseek", "providers": {}}
    with cfg_path.open("r", encoding="utf-8") as fh:
        return json.load(fh)
