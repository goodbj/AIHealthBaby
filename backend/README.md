# 颐康宝后端（FastAPI）— M0 基础设施 + M2 对话提醒 + M3 后台管理

> 真源 PRD：`../doc/Readme.md` 第 5/7 节。开发计划：见 `知识库/08-AIHealthBaby-颐康宝/08_00_开发计划.md` 的里程碑表。

## 现状（M0 交付物）
- FastAPI 应用骨架（`app/main.py`），`uvicorn app.main:app` 可启动
- SQLAlchemy 2.x 异步 ORM 模型（users / health_records / conversations / reminders / health_indicators），多租户字段预留
- Alembic 迁移（`migrations/`），初始建表迁移已生成
- 配置体系（环境变量 `YKB_*` + `config/llm_config.json`），密钥不入库
- 大模型抽象层 `BaseLLM` + DeepSeek 默认实现（`app/llm.py`），可切换模型
- 事件钩子注册/触发机制（`app/hooks/`，内置 `after_record_parsed` / `after_reminder_sent` 两个示例）
- APScheduler 调度器骨架（`app/scheduler.py`）
- 统一响应封装 `{ code, msg, data, trace_id }`，`/api/health` 健康检查

## 安装与运行
```bash
cd backend
pip install -r requirements.txt
export YKB_DATABASE_URL="sqlite+aiosqlite:///./yikangbao.db"   # 开发；生产换 PostgreSQL
export DEEPSEEK_API_KEY="sk-..."                               # 可选，离线桩模式无需
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

## 配置大模型密钥
编辑 `config/llm_config.json`（或环境变量 `DEEPSEEK_API_KEY`）。
无密钥时进入**离线桩模式**：`chat()` 返回带免责声明的占位建议，`extract_medical_info()` 返回空结构。
接入真实 LLM 见 `EXTENSION.md`（待补）。

## 数据库迁移
```bash
PYTHONPATH=. alembic upgrade head      # 应用全部迁移
PYTHONPATH=. alembic revision --autogenerate -m "desc"   # 模型变更后生成迁移
```

## 测试
```bash
PYTHONPATH=. pytest -q      # 当前 48 用例全绿（M0 17 + M1/M2 + M3 15）
```

## M3 后台管理基础版（Issue #182）

- **Web 管理端**：服务启动后打开 `http://localhost:8000/admin`（单文件 `static/admin.html`，登录即用）
- **默认管理员**：`admin` / `YKB_ADMIN_PASSWORD`（默认 `admin123`，仅开发/演示；生产必设强口令并换 `YKB_JWT_SECRET`）
- **API 前缀** `/api/admin/*`，均需 Bearer JWT（登录 `POST /api/admin/login` 签发，12h 有效）：
  - 用户管理：`GET /users`（关键词/状态筛选分页）、`POST /users/{id}/disable|enable`、`GET /users/export`（CSV，手机号脱敏）、`GET /users/{id}/timeline`（档案时间轴）
  - 病历解析记录：`GET /records`、`GET /records/{id}`、`POST /records/{id}/correct`（人工修正，corrected_by 留痕 + before/after 审计快照）
  - 低置信度报警：`GET /alerts/low-confidence`（默认 confidence < 0.7，升序最可疑在前）
  - 审计留痕：`GET /audit-logs`、`POST /audit-logs/{id}/replay`（**复跑**：按 after_snapshot 重放修正并比对，matched=true 即复现同结果）
  - 基础统计：`GET /stats`
- **验收对照**（开发计划第 8 节 M3）：管理员修正一条记录 → `corrected_by` 留痕 ✓；低置信度报警可见 ✓；修正留痕可复跑（replay 端点 + `test_admin_m3.py` 覆盖）✓

## 与 Android 客户端联调
Android 端（`../android`）默认基地址 `http://10.0.2.2:8000/`（模拟器访问宿主机）。
API 契约对齐：`/api/auth`、`/api/records`、`/api/chat`、`/api/reminders`、`/api/indicators`、`/api/health`。
M0 仅落地 `/api/health` 与骨架，业务路由在 M1/M2 挂载。

## 已知缺口（M0 之后）
- 真实 LLM 在线调用（httpx 客户端）
- OCR 集成（PaddleOCR / 腾讯云）
- 业务路由（auth/records/chat/reminders）与 JWT 鉴权
- 文件加密存储（AES-256 at rest）
