# 颐康宝（YiKangBao）— 健康档案 AI 助手

面向 55+ 中老年群体的健康档案 AI 助手：拍照病历 → AI 自动结构化归档 → 语音对话给
个性化饮食 / 用药 / 复查建议 → 主动提醒，并支持子女远程陪伴、内容分享、积分与推广。

> 产品名在产品需求文档中为「颐康宝（暂定名）」。代码层统一使用 `YiKangBao`。

## 仓库现状

| 模块 | 状态 | 说明 |
|------|------|------|
| `doc/` | ✅ 已完成 | 需求文档（PRD）、扩展需求、商业计划书 |
| `android/` | 🟡 脚手架较完整 | Jetpack Compose 客户端（登录/语音/病历/对话/提醒），但**尚无后端可联调** |
| 后端（FastAPI） | ❌ 缺失 | PRD 第 7 节要求但未实现（LLM 抽象层 + DeepSeek + 钩子 + 调度器） |

## 目录结构

```
AIHealthBaby/
├── doc/            # 产品需求文档、扩展需求、商业计划书
├── android/        # Jetpack Compose 客户端（包名 com.yikangbao.app）
└── README.md       # 本文件
```

## Android 客户端

技术栈：Kotlin + Jetpack Compose + Hilt + Retrofit + ViewModel + Material3（苹果风主题）。

### 配置后端地址

后端基地址通过 `android/gradle.properties` 的 `BASE_URL` 配置，并可在
`android/local.properties`（已被 `.gitignore` 忽略，不入库）中按本机覆盖：

```
# android/local.properties
BASE_URL=http://10.0.2.2:8000/
```

默认值为 `http://10.0.2.2:8000/`（Android 模拟器访问宿主机）。定义位于
`android/app/src/main/java/com/yikangbao/app/di/AppModule.kt`。

### 构建

需要 Android SDK（compileSdk 34）与 JDK 17。

```bash
cd android
./gradlew assembleDebug      # Linux/macOS
gradlew.bat assembleDebug    # Windows
```

## 后端（待实现）

PRD 规划：Python 3.11 + FastAPI，SQLite（开发）/ PostgreSQL（生产），OCR 解析，
可切换大模型抽象层（默认 DeepSeek），事件钩子注册/触发机制，APScheduler 定时提醒。
API 契约以 `doc/Readme.md` 为准。

## 文档

- `doc/Readme.md` — 产品需求文档（功能、数据模型、API 设计、扩展钩子）
- `doc/Readme_Extended.md` — 扩展需求文档
- `doc/商业计划书.txt` — 商业计划书

## 备注

完整项目现状、风险与下一步计划见知识库看板
`AI_Memory/知识库/08-AIHealthBaby-颐康宝/08-AIHealthBaby-颐康宝-看板.md`。
