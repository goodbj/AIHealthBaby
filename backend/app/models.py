"""ORM 模型（PRD 第 3 节数据模型 → SQLAlchemy）。

M0 落地核心表：users / health_records / conversations / reminders。
盈利与分享相关表结构按 PRD 预留列定义（暂不做功能），不阻断 MVP。
多租户字段 tenant_id 预留（为日后 ToB 医院版）。
"""
from __future__ import annotations

import uuid
from datetime import datetime

from sqlalchemy import (
    DateTime,
    Float,
    ForeignKey,
    Integer,
    String,
    Text,
    func,
)
from sqlalchemy.orm import Mapped, mapped_column, relationship
from sqlalchemy.types import JSON

from .database import Base


def _uuid() -> str:
    return uuid.uuid4().hex


class User(Base):
    __tablename__ = "users"

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=_uuid)
    phone: Mapped[str] = mapped_column(String(32), unique=True, index=True)
    name: Mapped[str | None] = mapped_column(String(64), nullable=True)
    # 账号状态：active 正常 / disabled 已禁用（M3 后台管理）
    status: Mapped[str] = mapped_column(String(16), default="active", server_default="active")
    emergency_contact_phone: Mapped[str | None] = mapped_column(String(32), nullable=True)
    children_ids: Mapped[list] = mapped_column(JSON, default=list)
    tenant_id: Mapped[str | None] = mapped_column(String(64), index=True, nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())

    records: Mapped[list["HealthRecord"]] = relationship(back_populates="user")


class HealthRecord(Base):
    __tablename__ = "health_records"

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=_uuid)
    user_id: Mapped[str] = mapped_column(ForeignKey("users.id"), index=True)
    image_url: Mapped[str | None] = mapped_column(String(512), nullable=True)
    ocr_raw_text: Mapped[str | None] = mapped_column(Text, nullable=True)
    diagnosis: Mapped[str | None] = mapped_column(String(255), nullable=True)
    indicators: Mapped[list] = mapped_column(JSON, default=list)
    medications: Mapped[list] = mapped_column(JSON, default=list)
    record_date: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    confidence: Mapped[float] = mapped_column(Float, default=0.0)
    status: Mapped[str] = mapped_column(String(16), default="pending")  # pending/confirmed/corrected
    corrected_by: Mapped[str | None] = mapped_column(String(64), nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())

    user: Mapped["User"] = relationship(back_populates="records")


class Conversation(Base):
    __tablename__ = "conversations"

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=_uuid)
    user_id: Mapped[str] = mapped_column(ForeignKey("users.id"), index=True)
    question: Mapped[str] = mapped_column(Text)
    answer: Mapped[str] = mapped_column(Text)
    injected_context: Mapped[list] = mapped_column(JSON, default=list)
    model_used: Mapped[str | None] = mapped_column(String(64), nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())


class Reminder(Base):
    __tablename__ = "reminders"

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=_uuid)
    user_id: Mapped[str] = mapped_column(ForeignKey("users.id"), index=True)
    type: Mapped[str] = mapped_column(String(16), default="medication")  # medication/followup/diet
    content: Mapped[str] = mapped_column(Text)
    scheduled_time: Mapped[datetime] = mapped_column(DateTime)
    status: Mapped[str] = mapped_column(String(16), default="pending")  # pending/sent/cancelled
    sent_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    read_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)  # 站内红点：sent+未读计数
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())


class HealthIndicator(Base):
    __tablename__ = "health_indicators"

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=_uuid)
    user_id: Mapped[str] = mapped_column(ForeignKey("users.id"), index=True)
    type: Mapped[str] = mapped_column(String(32), index=True)
    value: Mapped[str] = mapped_column(String(64))
    record_date: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())


# ==================== M3 后台管理 ====================

class AdminUser(Base):
    """后台管理员（PRD 2.2：运营用 Web 管理端）。

    密码只存 PBKDF2 哈希（sha256_pw），绝不明文。
    """

    __tablename__ = "admin_users"

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=_uuid)
    username: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    password_hash: Mapped[str] = mapped_column(String(256))
    role: Mapped[str] = mapped_column(String(16), default="admin")  # admin/super
    status: Mapped[str] = mapped_column(String(16), default="active")
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())


class AuditLog(Base):
    """审计留痕（开发计划第 7 节：所有后台读取/修改留审计）。

    M3 核心：管理员修正病历记录时，修正前后的快照落这里，
    支撑「修正留痕可复跑」验收——按 log 记录重放修正，可复现同结果。
    """

    __tablename__ = "audit_logs"

    id: Mapped[str] = mapped_column(String(64), primary_key=True, default=_uuid)
    admin_id: Mapped[str] = mapped_column(String(64), index=True)  # 操作管理员
    action: Mapped[str] = mapped_column(String(64), index=True)  # record_correct / user_disable / ...
    target_type: Mapped[str] = mapped_column(String(32), default="record")
    target_id: Mapped[str] = mapped_column(String(64), index=True)  # 被操作对象 ID
    before_snapshot: Mapped[dict] = mapped_column(JSON, default=dict)
    after_snapshot: Mapped[dict] = mapped_column(JSON, default=dict)
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())
