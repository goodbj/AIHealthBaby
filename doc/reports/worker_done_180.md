worker_done

**Issue #180 [部门-08][P1][KPI] M1病历闭环端到端 — 交付完成**（提交 43b1c82，分支 AIHealthBaby_08_00 已推 origin）

## 验收对照（开发计划第 8 节 M1）

| 验收项 | 结果 |
|--------|------|
| 端到端可复跑 | ✅ 后端 59 pytest 全绿（新增 11 例）+ 真服务实测：multipart 上传化验单→RapidOCR 本地识别→结构化落库→确认修正→时间轴→原图回读，**全链路实测 2.64s（验收线 ≤5s）** |
| Android 真机演示视频 | ⚠️ 部分：debug 15.5M / release 10.5M APK 本地打包 BUILD SUCCESSFUL 可安装；本机 adb 无连接设备、无模拟器镜像，视频待真机接入补录（不阻塞功能验收，见缺口） |
| M1 交付记录落库 | ✅ 双落库：仓库 doc/reports/M1交付记录.md + 记忆库 |

## 产物磁盘绝对路径

- 交付记录（仓库）：`E:/AI/orca-worktrees/08_AIHealthBaby/AIHealthBaby_08_00/doc/reports/M1交付记录.md`
- 交付记录（记忆库）：`E:/AI/AI_Memory/知识库/08-AIHealthBaby-颐康宝/M1交付记录.md`
- API 契约（已补 records/auth 章节）：`E:/AI/orca-worktrees/08_AIHealthBaby/AIHealthBaby_08_00/doc/API.md`
- APK：`E:/AI/orca-worktrees/08_AIHealthBaby/AIHealthBaby_08_00/android/app/build/outputs/apk/debug/app-debug.apk`（release：同目录 app-release-unsigned.apk）

## 交付摘要

- **后端**：`app/ocr.py`（RapidOCR=PaddleOCR PP-OCR 模型 ONNX 本地推理，抽象层可切云）；`/api/records/*` 六端点（上传 multipart ≤5 图→OCR→LLM→pending、时间轴 list、详情含 OCR 原文、confirm 确认修正→confirmed、delete、原图）；`/api/auth/login` 演示一键登录（固定验证码）；`extract_medical_info` 升级为 DeepSeek JSON mode 在线提取 + 离线正则桩兜底（无密钥链路不断）。
- **Android**：API 层全量对齐统一响应封装；拍照（FileProvider）/相册上传→解析确认弹层（诊断可改+指标+OCR 原文回显）→详情页确认归档；一键登录入口；debug/release APK 打包通过。
- **复跑**：`cd backend && pip install -r requirements.txt && uvicorn app.main:app`；`POST /api/auth/login {"phone":"138xxxxxxxx","code":"123456"}` → `POST /api/records/upload`。

## 已知缺口（诚实交底）

1. 真机演示视频待补（APK 已可装可跑，需一台接入设备）。
2. 病历图片加密 at rest + 签名 URL → M4（开发计划既定）。
3. 离线正则桩对复杂版式精度有限（在线 DeepSeek 为准）。

请 00_01 验收。
