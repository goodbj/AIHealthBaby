"""健康档案上下文组装（Issue #181 · M2 对话注入档案）。

对话前取用户最近健康档案（病历/指标/用药）与最近对话历史，
拼装为 LLM context；回答范围限饮食/运动/作息/复查建议。
"""
from __future__ import annotations

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from ..models import Conversation, HealthIndicator, HealthRecord

MAX_RECORDS = 5
MAX_INDICATORS = 10
MAX_HISTORY = 5


async def build_health_context(session: AsyncSession, user_id: str) -> dict:
    """取用户最近健康档案与对话历史，组装 LLM 注入上下文。

    返回 {"profile": {...} | None, "history": [...]}；
    用户无档案时 profile 为 None（仍可回答通用健康建议）。
    """
    profile: dict | None = None
    records = (
        await session.execute(
            select(HealthRecord)
            .where(HealthRecord.user_id == user_id)
            .order_by(HealthRecord.created_at.desc())
            .limit(MAX_RECORDS)
        )
    ).scalars().all()

    if records:
        indicators = (
            await session.execute(
                select(HealthIndicator)
                .where(HealthIndicator.user_id == user_id)
                .order_by(HealthIndicator.record_date.desc())
                .limit(MAX_INDICATORS)
            )
        ).scalars().all()

        profile = {
            "recent_records": [
                {
                    "diagnosis": r.diagnosis,
                    "medications": r.medications,
                    "indicators": r.indicators,
                    "record_date": r.record_date.isoformat() if r.record_date else None,
                    "status": r.status,
                }
                for r in records
            ],
            "latest_indicators": [
                {"type": i.type, "value": i.value, "record_date": i.record_date.isoformat() if i.record_date else None}
                for i in indicators
            ],
        }

    history_rows = (
        await session.execute(
            select(Conversation)
            .where(Conversation.user_id == user_id)
            .order_by(Conversation.created_at.desc())
            .limit(MAX_HISTORY)
        )
    ).scalars().all()
    # 旧对话在前，按问答成对展开，符合时序
    history: list[dict] = []
    for c in reversed(history_rows):
        history.append({"role": "user", "content": c.question})
        history.append({"role": "assistant", "content": c.answer})

    return {"profile": profile, "history": history}


def context_summary(ctx: dict) -> list:
    """注入上下文的留痕摘要（写进 conversations.injected_context）。"""
    profile = ctx.get("profile")
    return [
        {
            "profile_injected": profile is not None,
            "record_count": len(profile["recent_records"]) if profile else 0,
            "indicator_count": len(profile["latest_indicators"]) if profile else 0,
            "history_turns": len(ctx.get("history", [])),
        }
    ]
