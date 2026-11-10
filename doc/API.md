# 颐康宝 API 文档（M4）

> 基准：`doc/Readme.md` PRD 6.3/7 节 API 契约；实现：`backend/app/routes/`。
> 与 FastAPI 自动生成的 OpenAPI（`http://<host>:8000/openapi.json`）同步，本文档为人工核对版。

## 通用约定

- **Base URL**：`http://<host>:8000/api`
- **统一响应封装**（PRD 6.3）：

```json
{ "code": 0, "msg": "ok", "data": { }, "trace_id": "<32位hex>" }
```

- `code = 0` 成功；`1001~1999` 业务错误（HTTP 200）；`500~999` 服务端错误（HTTP 500）。
- 常见业务码：`1001` 参数缺失/为空、`1002` 模型服务暂不可用、`4010` 管理端鉴权失败。

## 1. 健康检查

### `GET /api/health`

无鉴权。服务与数据库连通性探针（K8s/网关健康检查用）。

**响应示例**

```json
{ "code": 0, "msg": "ok", "data": { "status": "healthy", "service": "YiKangBao" }, "trace_id": "..." }
```

## 2. 健康助手对话

### `POST /api/chat`

单轮对话。LLM 回复统一附带免责声明；无 `DEEPSEEK_API_KEY` 时进入离线桩模式（返回占位建议）。

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| prompt | string | 是 | 用户输入，空/纯空白返回 `1001` |
| model | string \| null | 否 | 模型名（默认 `deepseek`） |

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| reply | string | 助手回复（含免责声明） |
| model | string | 实际应答模型名 |
| disclaimer | bool | 恒为 `true` |

## 3. 提醒（reminders）

前缀 `/api/reminders`，标签 `reminders`。多租户：按 `user_id` 隔离。

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/reminders` | 创建提醒 |
| GET | `/api/reminders` | 分页列表（`user_id` 必填） |
| GET | `/api/reminders/red-dot/count` | 未读红点计数 |
| POST | `/api/reminders/red-dot/read` | 全部标记已读 |
| GET | `/api/reminders/{reminder_id}` | 详情 |
| PATCH | `/api/reminders/{reminder_id}` | 更新 |
| DELETE | `/api/reminders/{reminder_id}` | 删除 |

## 4. 后台管理（admin，M3）

前缀 `/api/admin`。**JWT 鉴权**：除 `/login` 外均需 `Authorization: Bearer <token>`
（HS256，签发于 `/login`，有效期见 `YKB_ACCESS_TOKEN_EXPIRE_MINUTES`，默认 24h）。

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/login` | 管理员登录，签发 token |
| GET | `/api/admin/users` | 用户分页列表 |
| POST | `/api/admin/users/{user_id}/disable` | 禁用用户 |
| POST | `/api/admin/users/{user_id}/enable` | 启用用户 |
| GET | `/api/admin/users/export` | 用户导出 |
| GET | `/api/admin/users/{user_id}/timeline` | 用户行为时间线 |
| GET | `/api/admin/records` | 健康记录分页列表 |
| GET | `/api/admin/records/{record_id}` | 记录详情 |
| POST | `/api/admin/records/{record_id}/correct` | 人工修正低置信度字段（留审计痕） |
| GET | `/api/admin/alerts/low-confidence` | 低置信度记录报警列表 |
| GET | `/api/admin/audit-logs` | 审计日志分页列表 |
| POST | `/api/admin/audit-logs/{log_id}/replay` | 审计日志重放 |
| GET | `/api/admin/stats` | 后台统计概览 |

## 5. 配置项（环境变量，前缀 `YKB_`）

| 变量 | 默认 | 说明 |
|------|------|------|
| `YKB_DATABASE_URL` | `sqlite:///./yikangbao.db` | 开发 SQLite；生产 PostgreSQL |
| `YKB_JWT_SECRET` | `dev-secret-change-me` | 生产必换 |
| `YKB_ACCESS_TOKEN_EXPIRE_MINUTES` | `1440` | 管理 token 有效期（分钟） |
| `YKB_FIELD_CRYPTO_KEY` | 占位值 | 32 字节，生产必换 |
| `DEEPSEEK_API_KEY` | 空 | 空 = 离线桩模式 |

## 6. 性能与安全基线（M4）

- **压测**：`/api/health` 267 QPS、`/api/chat` 216 QPS（100 并发 15s，0 错误），
  达成 PRD 6.2「初期 100 QPS」目标。报告：`doc/reports/`。
- **安全扫描**：bandit 静态扫描 0 问题（0 高危）、pip-audit 依赖审计 0 已知漏洞。
- 密钥不入库：全部走环境变量或 `config/llm_config.json`（该文件不入库的 key 留空）。
