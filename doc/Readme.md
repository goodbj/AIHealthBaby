好的，我将根据你的原始需求和我提出的扩展建议，整理成一份**结构完整、可直接交给AI智能体进行编码的需求文档**。文档会包含功能模块、数据模型、API设计、扩展接口要求，并明确要求生成**可直接运行的代码**。

---

# 项目需求文档 —— 健康档案AI助手（面向中老年群体）

## 1. 项目概述

### 1.1 项目名称
**颐康宝**（暂定名）—— 健康档案AI助手

### 1.2 目标用户
- 注重健康的中老年人（55岁以上）
- 其子女（辅助角色）

### 1.3 核心价值
- 上传病历照片 → AI自动提取结构化健康档案
- 日常语音对话 → AI结合个人档案给出个性化饮食/运动/用药提醒
- 主动关怀提醒 → 提升用户粘性和健康依从性

### 1.4 技术形态
- 前端：微信小程序 + 可选App（H5封装）
- 后台管理系统：Web管理端
- 数据库：云数据库
- AI能力：支持切换多个大模型（自选接入）

---

## 2. 功能需求

### 2.1 用户端（小程序/App）

#### 2.1.1 用户注册与登录
- 手机号一键登录
- 子女可帮老人注册（验证关系：老人手机号验证码确认）
- 支持绑定紧急联系人

#### 2.1.2 语音优先交互
- 首页大按钮：“按住说话”
- 支持语音转文字提问 + 文字输入备选
- 常见指令示例：
  - “我要上传病例”
  - “今天血压有点高”
  - “我吃了什么可以降血糖”

#### 2.1.3 病历上传与解析归档
- 拍照/从相册选择病历图片（化验单、出院小结、处方）
- 调用OCR + 医疗大模型提取以下字段：
  - 诊断名称
  - 检查指标（如血压150/95，血糖7.2）
  - 异常提示（偏高/偏低/正常）
  - 用药名称 + 用法用量
  - 检查日期、医院名称
- 提取结果展示并让用户语音确认/修改
- 自动归入用户的时间轴档案

#### 2.1.4 个人健康档案库（时间轴）
- 按时间倒序展示所有历史记录
- 每一条记录包含：
  - 原始病历照片（缩略图）
  - 结构化摘要
  - 对应AI建议链接
- 支持按病种筛选（高血压/糖尿病/冠心病等）

#### 2.1.5 AI对话与个性化建议
- 每次对话自动注入用户档案中**相关的近期记录**（如最近一次血压、正在服用的药物）
- 提问示例：
  - “我现在能吃什么水果？”
  - “今天有点头晕怎么办？”
- AI回答限制范围：饮食、运动、作息、复查提醒，禁止推荐处方药/治疗方案
- 每次回答底部固定显示：“建议咨询您的医生”

#### 2.1.6 主动提醒（通知）
- 根据档案数据自动生成提醒：
  - 用药提醒（如“阿司匹林，早餐后服用”）
  - 复诊提醒（根据出院小结建议）
  - 饮食建议（如“您有高血脂，今日少油”）
- 通知方式：微信订阅消息 + 小程序内红点

#### 2.1.7 子女远程陪伴（免费基础版）
- 老人授权后，子女可查看：
  - 最近7天健康档案摘要
  - 最近3条AI对话摘要
  - 紧急联系人信息
- 升级付费版：子女每周收到《健康周报》+ 异常指标预警

### 2.2 后台管理系统（Web端）

#### 2.2.1 用户管理
- 查询、禁用、导出用户列表
- 查看用户档案完整时间轴

#### 2.2.2 病历解析记录管理
- 查看所有OCR+AI解析原始记录
- 人工修正解析结果（防AI错误）
- 异常解析报警（提取置信度<0.7）

#### 2.2.3 广告与内容管理
- 配置首页banner广告（医院/体检/陪护）
- 配置AI回复底部的推荐卡片（非强制，用户点击跳转）

#### 2.2.4 数据统计
- 日活/留存/上传病历数
- 常用问题TOP10
- 异常指标触发提醒次数

### 2.3 盈利模块（预留接口，初期可关闭）

| 模块 | 接口预留形式 | 是否纳入MVP |
|------|-------------|-------------|
| 医院推荐（CPS） | 跳转链接 + 回调参数 | 否 |
| 陪护/养老院推荐 | 信息流卡片 + 表单提交 | 否 |
| 保险导流 | H5外链 | 否 |
| 遗嘱预约 | 表单收集 + 转人工 | 否 |
| 子女周报订阅 | 微信支付订阅 | 否（但预留付费逻辑） |

---

## 3. 数据模型

### 3.1 用户表 `users`
| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 主键 |
| phone | string | 登录手机号 |
| name | string | 昵称 |
| emergency_contact_phone | string | 紧急联系人 |
| children_ids | array | 授权子女user_id列表 |
| created_at | timestamp | |

### 3.2 健康档案表 `health_records`
| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | |
| user_id | string | 外键 |
| image_url | string | 原始病历图片地址 |
| ocr_raw_text | text | OCR原始返回文本 |
| diagnosis | string | AI提取的诊断 |
| indicators | json | `[{"name":"血压","value":"150/95","level":"high"}]` |
| medications | json | `[{"name":"二甲双胍","dosage":"0.5g","frequency":"bid"}]` |
| record_date | date | 病历上的日期 |
| confidence | float | 解析置信度0-1 |
| status | string | pending/confirmed/corrected |
| corrected_by | string | 后台人工修正管理员ID |

### 3.3 对话记录表 `conversations`
| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | |
| user_id | string | |
| question | text | 用户问题 |
| answer | text | AI回答 |
| injected_context | json | 本次注入的用户档案片段 |
| model_used | string | 使用的大模型名称 |
| created_at | timestamp | |

### 3.4 提醒任务表 `reminders`
| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | |
| user_id | string | |
| type | string | medication/followup/diet |
| content | text | 提醒内容 |
| scheduled_time | timestamp | 计划提醒时间 |
| status | string | pending/sent/cancelled |

### 3.5 广告/推荐位表 `promotions`
| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | |
| position | string | home_banner/ai_suggestion_card |
| title | string | |
| image_url | string | |
| link_url | string | 跳转链接 |
| callback_url | string | 点击回调统计接口 |

---

## 4. 核心API接口设计（留出扩展钩子）

### 4.1 用户端接口

#### 4.1.1 上传病历
```
POST /api/upload/medical_record
Headers: Authorization: Bearer {token}
Body: multipart/form-data { image, user_id }
Response: {
  record_id: "rec_123",
  extracted_fields: {...},   // AI解析结果
  confidence: 0.92,
  user_confirm_needed: true  // 是否需用户确认
}
```

#### 4.1.2 确认/修正解析结果
```
PUT /api/health_records/{record_id}
Body: { confirmed_fields: {...} }
钩子触发: 更新档案后，重新评估用户的提醒任务
```

#### 4.1.3 AI对话
```
POST /api/chat
Body: {
  user_id,
  question,
  model_name?  // 可选，指定用哪个模型
}
Response: {
  answer,
  injected_context_summary,  // 本次用了哪几条档案记录
  model_used
}
扩展点: 支持传入 custom_prompt 参数，覆盖默认prompt
```

#### 4.1.4 获取主动提醒列表
```
GET /api/reminders?user_id={}&status=pending
```

### 4.2 后台管理接口

#### 4.2.1 人工修正解析
```
POST /admin/health_records/correct
Body: { record_id, corrected_fields, admin_id }
```

#### 4.2.2 广告位配置
```
POST /admin/promotions
Body: { position, title, image_url, link_url, callback_url }
```

#### 4.2.3 统计查询
```
GET /admin/stats/daily?date=2026-06-10
返回: dau, upload_count, top_questions: [...]
```

### 4.3 扩展接口要求（钩子）

| 钩子名称 | 触发时机 | 用途 |
|---------|----------|------|
| `after_record_parsed` | 病历解析完成后 | 触发第三方分析服务 |
| `before_chat_response` | AI回答前 | 修改注入的档案内容 |
| `after_reminder_sent` | 提醒发送后 | 统计、日志 |
| `on_user_confirm_record` | 用户确认解析 | 同步到子女端 |

实现方式：在代码中预留**事件监听注册机制**，允许后续添加自定义回调。

---

## 5. 自选大模型接入接口（抽象层）

要求代码中实现**大模型抽象类**，后续只需实现该接口即可切换：

```python
class BaseLLM:
    def __init__(self, config: dict):
        self.config = config

    def chat(self, prompt: str, context: dict = None) -> str:
        raise NotImplementedError

    def extract_medical_info(self, ocr_text: str) -> dict:
        """从OCR文本中提取病历结构化信息"""
        raise NotImplementedError

    def name(self) -> str:
        """返回模型名称，如 deepseek-v3, gpt-4, qwen-max"""
        raise NotImplementedError
```

**已预留的接入方式**：
- 配置文件 `llm_config.json` 中设置 `active_model` 和对应 `api_key`、`base_url`
- 运行时通过API参数 `?model_name=xxx` 临时切换

**默认实现**：DeepSeek（低成本，长上下文）

---

## 6. 非功能性要求

### 6.1 安全与隐私
- 病历图片存储：加密存储，仅用户本人和授权子女可看
- AI调用日志：记录每次对话注入的档案内容，可审计
- 数据导出：用户可一键导出所有档案（PDF或JSON）

### 6.2 性能
- 病历解析：上传到返回结构化结果 ≤ 5秒（OCR+大模型调用）
- 对话首字响应 ≤ 2秒
- 支持并发：初期100 QPS

### 6.3 扩展性
- 所有API返回格式统一：`{ code, msg, data, trace_id }`
- 数据库设计支持多租户（为日后ToB医院版预留）
- 预留 Webhook 配置表，支持第三方订阅事件

---

## 7. 对AI编码智能体的明确指令

请生成满足以下要求的完整代码：

1. **技术栈**：
   - 后端：Python 3.11 + FastAPI
   - 数据库：SQLite（开发）/ PostgreSQL（生产）
   - 对象存储：本地模拟（可替换为阿里云OSS/腾讯云COS）
   - OCR：调用开源库（如paddleocr）或云服务HTTP接口

2. **必须包含**：
   - 上述所有用户端API
   - 后台管理基础API（用户管理、记录修正、统计）
   - 大模型抽象层 + DeepSeek具体实现
   - 事件钩子注册与触发机制（简单实现，至少2个示例钩子）
   - 启动一个后台任务调度器（用于定时提醒，可用APScheduler）

3. **文档要求**：
   - 根目录 `README.md`：如何安装、配置LLM密钥、运行
   - `API.md`：所有接口的curl示例
   - `EXTENSION.md`：如何添加新的大模型、如何注册新钩子

4. **代码风格**：
   - 模块化（routes/ services/ models/ hooks/）
   - 每个函数有docstring
   - 配置全部从环境变量或config文件读取

5. **输出格式**：
   - 直接输出所有代码文件，按目录结构展示（每个文件以 `## File: 路径` 开始）
   - 确保代码可以直接复制到项目中运行

---

