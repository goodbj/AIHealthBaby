# 颐康宝扩展开发指南（EXTENSION.md）

> 给后续接手的工程师/智能体：怎么加模型、加路由、加钩子、加定时任务。
> PRD 全景见 `doc/Readme.md`，API 契约见 `doc/API.md`。

## 1. 接入新的大模型

LLM 抽象层在 `backend/app/llm.py`：`BaseLLM` 是接口，`_REGISTRY` 是模型注册表，
`get_llm(model_name=...)` 按 `config/llm_config.json` 的 `active_model` 取实现。

**接入步骤**（示例：接入通义千问）：

```python
# backend/app/llm.py
class QwenLLM(BaseLLM):
    """通义千问实现（OpenAI 兼容接口）。"""
    def chat(self, prompt: str, context: dict | None = None) -> str: ...
    def extract_medical_info(self, ocr_text: str) -> MedicalExtraction: ...
    def name(self) -> str: return "qwen"

register_llm("qwen", QwenLLM)
```

然后 `config/llm_config.json` 加 provider 并切 `active_model`：

```json
{
  "active_model": "qwen",
  "providers": {
    "deepseek": { "model": "deepseek-chat", "base_url": "https://api.deepseek.com/v1", "api_key": "" },
    "qwen":     { "model": "qwen-max", "base_url": "https://dashscope.aliyuncs.com/compatible-mode/v1", "api_key": "" }
  }
}
```

- `api_key` 留空时走离线桩（`_offline_reply`），方便本地无网开发。
- 请求级临时切换：`POST /api/chat` 传 `"model": "qwen"`。
- 也可用 `register_llm` 写在独立模块（避免改核心文件），import 即注册。

## 2. 加业务路由

1. 新建 `backend/app/routes/<域>.py`：

```python
from fastapi import APIRouter, Depends
from .response import ok, fail

router = APIRouter(prefix="/records", tags=["records"])

@router.get("")
async def list_records(db=Depends(get_db)):
    return ok([...])
```

2. 在 `backend/app/routes/__init__.py` 挂载：

```python
from .records import router as records_router
api_router.include_router(records_router)
```

**规矩**：
- 一律走 `ok()/fail()` 统一封装（`code/msg/data/trace_id`），业务错误码用 `1001~1999`，HTTP 保持 200；服务端错误 `500~999` 才映射 HTTP 500。
- 多租户：查询必须带 `user_id` 过滤（对齐 ORM 模型的多租户字段预留）。
- 医疗相关回复必须带免责声明。

## 3. 加事件钩子

钩子机制在 `backend/app/hooks/`（注册/触发解耦，PRD 4.3）。内置示例：
`after_record_parsed`（病历解析完触发）、`after_reminder_sent`（提醒下发后触发）。

```python
# backend/app/hooks/my_hooks.py
from . import register_hook

def on_record_parsed(record: dict) -> None:
    # 例如：解析完成后推送子女端通知
    ...

def install() -> None:
    register_hook("after_record_parsed", on_record_parsed)
```

在 `app/main.py` 的 `_startup` 里 `install()`（或做成 entry-point 自动发现）。
触发方在业务代码里 `await trigger("after_record_parsed", record)`。

## 4. 加定时任务

调度器在 `backend/app/scheduler.py`（APScheduler AsyncIOScheduler）。

```python
def build_scheduler():
    sched = AsyncIOScheduler()
    sched.add_job(daily_reminder_scan, "cron", hour=8, minute=0)   # 每天 8 点扫提醒
    return sched
```

生命周期由 `app/main.py` startup/shutdown 托管，路由里不用管启停。

## 5. 数据库迁移

```bash
cd backend
# 改 app/models.py 后：
PYTHONPATH=. alembic revision --autogenerate -m "add xxx table"
PYTHONPATH=. alembic upgrade head
```

SQLite（开发）→ PostgreSQL（生产）只换 `YKB_DATABASE_URL`，ORM 不用动。

## 6. Android 客户端联调

- 基地址：`android/local.properties` 写 `BASE_URL=http://10.0.2.2:8000/`（模拟器）；
  真机写局域网 IP。定义在 `di/AppModule.kt`，Retrofit 契约在 `data/api/YiKangBaoApi.kt`。
- 本地构建环境（Windows）：`android/setup_env.ps1` 一键装 JDK17 + Gradle 8.5 + SDK 34；
  或手动把 Temurin JDK 17 解压到 `android/tools/jdk`、Gradle 到 `android/tools/gradle`、
  cmdline-tools 到 `android/tools/android-sdk/cmdline-tools/latest`，再 sdkmanager 装
  `platform-tools`、`platforms;android-34`、`build-tools;34.0.0`。
- 打 release 包：`gradle assembleRelease`，产物在 `android/app/build/outputs/apk/release/`。

## 7. 压测与安全基线（改完代码后复测）

```bash
# 起服务后压测（目标 ≥100 QPS、错误率 0）
python backend/tests/load_test.py
# 静态安全扫描（目标 0 高危）
bandit -r backend/app
# 依赖漏洞审计（目标 0 已知漏洞）
pip-audit -r backend/requirements.txt
```

报告落 `doc/reports/`，命名带日期。
