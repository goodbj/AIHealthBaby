"""测试 4-9：LLM 抽象层与钩子机制。"""
from __future__ import annotations

import asyncio

from app.hooks import (
    clear_hooks,
    log_after_record_parsed,
    log_after_reminder_sent,
    register_hook,
    trigger_hook,
)
from app.llm import DeepSeekLLM, MedicalExtraction, get_llm, register_llm


def test_deepseek_offline_chat():
    llm = DeepSeekLLM({"api_key": ""})
    out = llm.chat("今天血压有点高")
    assert "建议咨询您的医生" in out  # 免责声明必须存在


def test_deepseek_name():
    assert DeepSeekLLM().name() == "deepseek-chat"


def test_extract_medical_info_offline():
    ex: MedicalExtraction = DeepSeekLLM().extract_medical_info("化验单文本")
    assert ex.confidence == 0.0 and ex.indicators == []


def test_get_llm_default_and_switch():
    llm = get_llm()
    assert llm.name() == "deepseek-chat"
    llm2 = get_llm(model_name="deepseek")
    assert llm2.name() == "deepseek-chat"


def test_register_and_trigger_hook():
    clear_hooks()
    seen = []

    def my_hook(**kw):
        seen.append(kw.get("record_id"))
        return "acked"

    register_hook("after_record_parsed", my_hook)
    results = asyncio.run(trigger_hook("after_record_parsed", record_id="rec_1"))
    assert seen == ["rec_1"]
    assert results == ["acked"]


async def test_hook_example_after_record_parsed():
    clear_hooks()
    register_hook("after_record_parsed", log_after_record_parsed)
    res = await trigger_hook("after_record_parsed", record_id="r1", confidence=0.9)
    assert res and res[0]["event"] == "after_record_parsed"


async def test_hook_example_after_reminder_sent():
    clear_hooks()
    register_hook("after_reminder_sent", log_after_reminder_sent)
    res = await trigger_hook("after_reminder_sent", reminder_id="rem_1")
    assert res and res[0]["event"] == "after_reminder_sent"


def test_register_llm_extension():
    class FakeLLM(DeepSeekLLM):
        def name(self):
            return "fake-model"

    register_llm("fake", FakeLLM)
    assert get_llm(model_name="fake").name() == "fake-model"
