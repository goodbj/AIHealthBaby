"""事件钩子注册与触发机制（PRD 第 4.3 节）。

提供进程内的事件监听注册表：业务代码在关键时机触发钩子，
后续可注册自定义回调（第三方分析、日志、同步子女端等）。
M0 内置两个示例钩子：after_record_parsed、after_reminder_sent。
"""
from __future__ import annotations

import logging
from collections import defaultdict
from collections.abc import Awaitable, Callable
from typing import Any

HookFn = Callable[..., Any] | Callable[..., Awaitable[Any]]

_LOG = logging.getLogger("ykb.hooks")

_REGISTRY: dict[str, list[HookFn]] = defaultdict(list)


def register_hook(event: str, fn: HookFn) -> None:
    """注册一个事件回调。同名事件可挂多个回调，按注册顺序触发。"""
    _REGISTRY[event].append(fn)
    _LOG.info("hook registered: %s -> %s", event, getattr(fn, "__name__", repr(fn)))


async def trigger_hook(event: str, **payload: Any) -> list[Any]:
    """触发事件，依次执行所有注册的回调。

    单个回调异常被捕获并记录，避免一个钩子失败拖垮主流程。
    返回各回调的执行结果列表。
    """
    results: list[Any] = []
    for fn in _REGISTRY.get(event, []):
        try:
            res = fn(**payload)
            if hasattr(res, "__await__"):
                res = await res
            results.append(res)
        except Exception as exc:  # noqa: BLE001
            _LOG.exception("hook %s failed: %s", event, exc)
    return results


def clear_hooks() -> None:
    """清空所有钩子（主要用于测试隔离）。"""
    _REGISTRY.clear()


# --- M0 示例钩子实现（至少 2 个，满足 PRD 要求） ---


def log_after_record_parsed(**payload: Any) -> dict:
    """示例钩子：病历解析完成后记录审计日志。"""
    record_id = payload.get("record_id")
    confidence = payload.get("confidence")
    _LOG.info("hook[after_record_parsed] record=%s confidence=%s", record_id, confidence)
    return {"event": "after_record_parsed", "record_id": record_id}


def log_after_reminder_sent(**payload: Any) -> dict:
    """示例钩子：提醒发送后记录统计日志。"""
    reminder_id = payload.get("reminder_id")
    _LOG.info("hook[after_reminder_sent] reminder=%s", reminder_id)
    return {"event": "after_reminder_sent", "reminder_id": reminder_id}
