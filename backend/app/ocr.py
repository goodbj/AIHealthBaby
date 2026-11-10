"""OCR 引擎封装（M1，Issue #180）。

PaddleOCR 本地方案落地：采用 RapidOCR（PaddleOCR PP-OCRv4 官方模型的
ONNX Runtime 推理版），免装 paddlepaddle 大件、CPU 即可推理，
中文票据/化验单识别效果与 PaddleOCR 一致（模型同源）。

设计要点：
- 惰性单例加载（首次调用才载模型，约 2s），进程内复用；
- `recognize(path)` 返回按行拼接的原始文本与逐行结果；
- 引擎缺失/初始化失败时抛 OCRError，由路由层转业务错误码；
- 后续切云 OCR（腾讯云）只需在本文件再加一个实现类。
"""
from __future__ import annotations

import abc
import threading
from pathlib import Path


class OCRError(RuntimeError):
    """OCR 引擎不可用或识别失败。"""


class BaseOCR(abc.ABC):
    """OCR 统一抽象接口：切换实现只需继承 recognize。"""

    @abc.abstractmethod
    def recognize(self, image_path: str | Path) -> tuple[str, list[str]]:
        """识别图片，返回 (拼接全文, 逐行文本列表)。"""


class RapidOCREngine(BaseOCR):
    """RapidOCR 实现（PaddleOCR PP-OCR 模型，ONNX 本地推理）。"""

    def __init__(self) -> None:
        self._engine = None
        self._lock = threading.Lock()

    def _get_engine(self):
        if self._engine is None:
            with self._lock:  # 双检锁：并发首调只加载一次
                if self._engine is None:
                    try:
                        from rapidocr_onnxruntime import RapidOCR
                    except ImportError as e:  # pragma: no cover - 环境缺失
                        raise OCRError(
                            "OCR 引擎未安装：pip install rapidocr-onnxruntime"
                        ) from e
                    self._engine = RapidOCR()
        return self._engine

    def recognize(self, image_path: str | Path) -> tuple[str, list[str]]:
        path = Path(image_path)
        if not path.exists():
            raise OCRError(f"图片不存在：{path}")
        result, _ = self._get_engine()(str(path))
        lines = [item[1] for item in (result or []) if item and len(item) > 1 and item[1]]
        return "\n".join(lines), lines


_engine: BaseOCR | None = None


def get_ocr() -> BaseOCR:
    """取全局 OCR 引擎（惰性创建）。"""
    global _engine
    if _engine is None:
        _engine = RapidOCREngine()
    return _engine


def set_ocr(engine: BaseOCR | None) -> None:
    """替换/清空全局引擎（测试注入桩用）。"""
    global _engine
    _engine = engine
