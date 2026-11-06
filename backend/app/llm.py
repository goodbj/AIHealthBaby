"""大模型抽象层（PRD 第 5 节）。

定义 BaseLLM 抽象类，后续只需实现接口即可切换模型。
默认实现 DeepSeek（低成本、长上下文）。运行时可通过 model_name 临时切换。
接入方式：llm_config.json 配置 active_model + api_key + base_url。
"""
from __future__ import annotations

import abc
import os
from dataclasses import dataclass


@dataclass
class MedicalExtraction:
    """从 OCR 文本中解析出的结构化病历信息。"""

    diagnosis: str = ""
    indicators: list[dict] = None  # [{"name","value","level"}]
    medications: list[dict] = None  # [{"name","dosage","frequency"}]
    confidence: float = 0.0

    def __post_init__(self) -> None:
        self.indicators = self.indicators or []
        self.medications = self.medications or []


class BaseLLM(abc.ABC):
    """大模型统一抽象接口。切换模型只需继承并实现以下方法。"""

    def __init__(self, config: dict | None = None) -> None:
        self.config = config or {}

    @abc.abstractmethod
    def chat(self, prompt: str, context: dict | None = None) -> str:
        """基于 prompt（与可选 context）生成对话回复。"""

    @abc.abstractmethod
    def extract_medical_info(self, ocr_text: str) -> MedicalExtraction:
        """从 OCR 文本中提取病历结构化信息。"""

    @abc.abstractmethod
    def name(self) -> str:
        """返回模型名称，如 deepseek-v3 / gpt-4 / qwen-max。"""


class DeepSeekLLM(BaseLLM):
    """DeepSeek 默认实现（HTTP 调用 OpenAI 兼容接口）。

    api_key / base_url 来自 llm_config.json 或环境变量 DEEPSEEK_API_KEY。
    无密钥时进入离线桩模式，返回可预测的占位结果——保证 M0 后端可零依赖跑通与测试。
    """

    def __init__(self, config: dict | None = None) -> None:
        super().__init__(config or {})
        self.api_key = self.config.get("api_key") or os.environ.get("DEEPSEEK_API_KEY", "")
        self.base_url = self.config.get("base_url") or "https://api.deepseek.com/v1"
        self.model = self.config.get("model") or "deepseek-chat"
        self._online = bool(self.api_key)

    def chat(self, prompt: str, context: dict | None = None) -> str:
        if not self._online:
            return (
                f"[离线桩] 关于「{prompt[:20]}」的饮食/运动/作息建议："
                "请保持规律作息、低盐低脂饮食，并遵医嘱复查。建议咨询您的医生。"
            )
        # 在线调用逻辑（M0 不强制联网，留接口）
        raise NotImplementedError("DeepSeek 在线调用待接入 httpx 客户端")

    def extract_medical_info(self, ocr_text: str) -> MedicalExtraction:
        # M0 离线桩：结构化解析在 M1 接入 OCR + 真实 LLM 后替换
        return MedicalExtraction(
            diagnosis="",
            indicators=[],
            medications=[],
            confidence=0.0,
        )

    def name(self) -> str:
        return "deepseek-chat"


_REGISTRY: dict[str, type[BaseLLM]] = {"deepseek": DeepSeekLLM}


def register_llm(name: str, cls: type[BaseLLM]) -> None:
    """注册新的大模型实现（见 EXTENSION.md）。"""
    _REGISTRY[name] = cls


def get_llm(model_name: str | None = None, config: dict | None = None) -> BaseLLM:
    """按名称取得模型实例；缺省取配置中的 active_model。"""
    cfg = config or {}
    name = model_name or cfg.get("active_model") or "deepseek"
    cls = _REGISTRY.get(name, DeepSeekLLM)
    provider_cfg = (cfg.get("providers") or {}).get(name, {})
    return cls({**(provider_cfg or {}), **(config or {})})
