# 颐康宝 - 健康档案AI助手（扩展版需求文档）

## 📱 项目概述

### 1.1 项目名称
**颐康宝** - 智能健康档案AI助手

### 1.2 目标用户
- **主要用户**：注重健康的中老年人（55岁以上）
- **辅助用户**：子女、家庭医生、护理人员
- **管理用户**：医疗机构、养老机构、社区服务中心

### 1.3 核心价值主张
1. **智能档案管理**：上传病历照片 → AI自动提取结构化健康档案
2. **个性化健康指导**：语音对话 → AI结合个人档案给出定制化建议
3. **主动健康关怀**：智能提醒 → 提升用户粘性和健康依从性
4. **家庭健康协同**：子女远程陪伴 → 家庭健康管理一体化
5. **医疗资源对接**：智能导诊 → 连接优质医疗资源
6. **健康数据洞察**：趋势分析 → 预测健康风险

### 1.4 技术形态
- **前端**：iOS原生App + Android原生App + 微信小程序 + H5网页版
- **跨平台框架**：Flutter / React Native（Android+iOS双平台）
- **后台管理系统**：Web管理端（React/Vue）
- **数据库**：PostgreSQL（主库）+ Redis（缓存）+ MongoDB（日志）
- **AI能力**：多模型切换（DeepSeek/GPT-4/Qwen/Claude）
- **对象存储**：阿里云OSS/腾讯云COS
- **消息推送**：APNs（iOS）+ 极光推送/小米推送（Android）+ 微信订阅消息 + 短信

---

## 🎨 苹果风格UI设计规范

### 2.1 设计理念
遵循Apple Human Interface Guidelines，打造简洁、优雅、易用的界面体验。

### 2.2 视觉设计系统

#### 2.2.1 色彩体系
```
主色调：
- 健康绿：#34C759（iOS系统绿）- 用于健康状态、成功提示
- 医疗蓝：#007AFF（iOS系统蓝）- 用于主要操作、链接
- 警示橙：#FF9500（iOS系统橙）- 用于警告、提醒
- 危险红：#FF3B30（iOS系统红）- 用于错误、紧急情况

中性色：
- 深色文字：#1C1C1E
- 次要文字：#3C3C43
- 辅助文字：#8E8E93
- 分割线：#C6C6C8
- 背景色：#F2F2F7
- 卡片背景：#FFFFFF

渐变色：
- 健康渐变：linear-gradient(135deg, #34C759 0%, #30D158 100%)
- 医疗渐变：linear-gradient(135deg, #007AFF 0%, #5AC8FA 100%)
- 温暖渐变：linear-gradient(135deg, #FF9500 0%, #FFCC00 100%)
```

#### 2.2.2 字体系统
```
标题字体：SF Pro Display
- 大标题：34pt，Bold
- 标题：28pt，Semibold
- 小标题：22pt，Semibold
- 导航标题：17pt，Semibold

正文字体：SF Pro Text
- 正文：17pt，Regular
- 辅助文字：15pt，Regular
- 说明文字：13pt，Regular
- 注释文字：11pt，Regular

特殊场景：
- 数字显示：SF Mono，48pt，Medium（用于血压、血糖等）
- 大按钮文字：20pt，Semibold
```

#### 2.2.3 间距系统
```
基础单位：8pt
- xs：4pt
- sm：8pt
- md：16pt
- lg：24pt
- xl：32pt
- xxl：48pt

卡片内边距：16pt
列表项高度：64pt
按钮高度：50pt
大按钮高度：80pt
```

#### 2.2.4 圆角系统
```
- 小圆角：8pt（按钮、标签）
- 中圆角：12pt（卡片、输入框）
- 大圆角：16pt（大卡片、弹窗）
- 超大圆角：24pt（底部弹出面板）
- 圆形：50%（头像、图标按钮）
```

#### 2.2.5 阴影系统
```
轻阴影：0 2pt 8pt rgba(0,0,0,0.08)
中阴影：0 4pt 16pt rgba(0,0,0,0.12)
重阴影：0 8pt 24pt rgba(0,0,0,0.16)
悬浮阴影：0 12pt 32pt rgba(0,0,0,0.20)
```

### 2.3 组件设计规范

#### 2.3.1 导航栏
```
高度：44pt（不含状态栏）
背景：磨砂玻璃效果（blur: 20pt）
标题居中，左侧返回按钮，右侧操作按钮
滚动时背景透明度渐变
```

#### 2.3.2 底部标签栏
```
高度：49pt（不含安全区域）
图标大小：25pt
文字大小：10pt
选中状态：主色调
未选中状态：灰色
背景：磨砂玻璃效果
```

#### 2.3.3 卡片组件
```
背景：白色
圆角：12pt
阴影：轻阴影
内边距：16pt
分割线：1pt，颜色#C6C6C8
点击效果：轻微缩放（scale: 0.98）
```

#### 2.3.4 按钮组件
```
主要按钮：
- 高度：50pt
- 圆角：12pt
- 背景：主色调渐变
- 文字：白色，17pt，Semibold
- 点击效果：轻微缩放 + 触觉反馈

次要按钮：
- 高度：50pt
- 圆角：12pt
- 边框：2pt，主色调
- 背景：透明
- 文字：主色调，17pt，Semibold

文字按钮：
- 无边框无背景
- 文字：主色调，17pt，Regular
- 点击效果：文字颜色加深

大按钮（语音按钮）：
- 尺寸：80pt × 80pt
- 圆角：圆形
- 背景：主色调渐变
- 图标：36pt
- 点击效果：脉冲动画 + 触觉反馈
```

#### 2.3.5 输入框组件
```
高度：50pt
圆角：12pt
边框：1pt，#C6C6C8（聚焦时变为主色调）
内边距：左右16pt
字体：17pt，Regular
占位符颜色：#8E8E93
清除按钮：右侧，灰色×图标
```

#### 2.3.6 列表组件
```
列表项高度：64pt
左侧图标：24pt，距离左边16pt
右侧箭头：12pt，距离右边16pt
分割线：左侧缩进60pt，右侧缩进16pt
点击效果：背景变灰
```

#### 2.3.7 弹窗组件
```
背景：磨砂玻璃 + 半透明黑色遮罩
卡片圆角：16pt
卡片宽度：屏幕宽度 - 64pt
标题：22pt，Semibold
内容：17pt，Regular
按钮：水平排列，等宽
动画：从中心缩放弹出
```

#### 2.3.8 底部弹出面板
```
背景：白色
圆角：顶部16pt
高度：自适应或固定高度
拖拽指示器：顶部居中，36pt × 5pt，灰色
动画：从底部滑入
支持手势下拉关闭
```

### 2.4 动画与交互

#### 2.4.1 过渡动画
```
页面切换：
- 推入：从右侧滑入，时长300ms
- 弹出：向右滑出，时长300ms
- 淡入淡出：透明度渐变，时长200ms

元素动画：
- 缩放：scale 0.95 → 1.0，时长150ms
- 弹性：spring动画，阻尼0.8
- 渐变：透明度 0 → 1，时长200ms

特殊动画：
- 脉冲：用于语音按钮，scale 1.0 → 1.1 → 1.0，循环
- 波纹：用于卡片点击，从点击位置扩散
- 骨架屏：灰色渐变动画，用于加载状态
```

#### 2.4.2 触觉反馈
```
轻反馈：UIImpactFeedbackGenerator(style: .light)
中反馈：UIImpactFeedbackGenerator(style: .medium)
重反馈：UIImpactFeedbackGenerator(style: .heavy)
选择反馈：UISelectionFeedbackGenerator()
成功反馈：UINotificationFeedbackGenerator(type: .success)
警告反馈：UINotificationFeedbackGenerator(type: .warning)
错误反馈：UINotificationFeedbackGenerator(type: .error)
```

#### 2.4.3 手势交互
```
点击：单击、双击、长按
滑动：上下左右滑动
拖拽：拖动元素
缩放：双指缩放
旋转：双指旋转
边缘滑动：从边缘滑入
```

### 2.5 无障碍设计

#### 2.5.1 VoiceOver支持
```
所有交互元素支持VoiceOver
提供清晰的accessibility label
合理的accessibility hint
正确的accessibility trait
```

#### 2.5.2 动态字体
```
支持iOS动态字体大小
最小字体：11pt
最大字体：23pt（辅助功能中可设置更大）
所有布局自适应字体大小变化
```

#### 2.5.3 色彩对比度
```
文字与背景对比度 ≥ 4.5:1（WCAG AA标准）
重要信息不仅依赖颜色区分
提供高对比度模式
```

#### 2.5.4 减少动画
```
尊重系统"减少动态效果"设置
提供静态替代方案
关键信息不依赖动画传达
```

### 2.6 对话优先交互设计

#### 2.6.1 交互理念
**核心理念**：对话框/语音输入是用户与应用交互的**主要方式**，导航树仅作为辅助工具。这一设计理念专为中老年用户设计，降低学习成本，实现"说什么就做什么"的极简体验。

#### 2.6.2 界面布局
```
┌─────────────────────────────────────┐
│  状态栏（时间、信号、电量）           │
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐   │
│  │     可选：顶部快捷指令区      │   │
│  │  [健康报告] [用药提醒] [挂号] │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │      对话内容区域            │   │
│  │      （消息气泡列表）         │   │
│  │                             │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  [文字输入框]  🎤  发送      │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │    🌟 大按钮：按住说话        │   │
│  └─────────────────────────────┘   │
│                                     │
├─────────────────────────────────────┤
│  底部导航（辅助）：                  │
│  [首页] [档案] [提醒] [我的]        │
└─────────────────────────────────────┘
```

#### 2.6.3 语音交互设计
```
主交互方式：
- 首页中央大按钮："按住说话"
- 语音识别实时转文字显示
- 松手自动发送
- AI即时语音/文字回复

语音指令示例：
- "帮我记录血压，80，120"
- "明早8点提醒我测血糖"
- "查看这周的体检报告"
- "挂一个心内科的号"
- "有什么忌口？我有糖尿病"
- "提醒我中午吃药"

反馈机制：
- 语音识别中：波形动画 + "正在聆听..."
- 识别成功：绿色对勾 + 自动发送
- 识别失败：红色提示 + 重新录音
- AI处理中：打字机动画
- AI回复：语音播报（可关闭）+ 文字显示
```

#### 2.6.4 对话交互设计
```
对话界面特点：
- 类似微信的简洁对话气泡
- 用户消息：右侧，蓝色背景
- AI回复：左侧，白色背景，支持语音播报
- 支持图片、卡片、按钮等富媒体回复

快捷指令（可自定义）：
- "📊 健康报告" → 生成健康周报
- "💊 用药提醒" → 设置用药提醒
- "🏥 预约挂号" → 打开挂号流程
- "📷 上传病历" → 打开相机/相册
- "📈 健康趋势" → 显示指标趋势图
- "❓ 帮助" → 显示可用指令列表

对话上下文：
- 支持多轮对话记忆
- 自动注入用户健康档案上下文
- 跨对话主题记忆（如记得用户有糖尿病）
```

#### 2.6.5 导航树设计（辅助角色）
```
导航树定位：
- 位于底部标签栏，不占用主屏空间
- 提供"以防万一"的备选操作方式
- 视觉上淡化处理，不喧宾夺主

底部导航栏：
┌────────┬────────┬────────┬────────┐
│  首页  │  档案  │  提醒  │  我的  │
│  🏠   │  📁   │  🔔   │  👤   │
└────────┴────────┴────────┴────────┘
- 图标简洁，文字说明清晰
- 选中状态有颜色高亮
- 点击进入功能列表页

功能列表页：
- 按类别展示可用功能
- 重要功能置顶
- 支持搜索过滤
- 可将常用功能添加至首页快捷区
```

#### 2.6.6 极简模式设计
```
适用场景：
- 视力不佳的用户
- 追求极简操作的用户
- 首次使用的引导流程

极简模式特点：
- 屏幕仅保留：大按钮 + 对话框
- 隐藏所有导航和复杂功能入口
- 语音指令直达结果
- 可随时切换到完整模式
- 一句"帮帮我"呼出所有功能
```

#### 2.6.7 多模态输入支持
```
输入方式优先级：
1. 语音输入（最高优先，推荐使用）
2. 文字输入（备选，随时可用）
3. 快捷指令按钮（辅助点击）
4. 导航树（最后备选）

智能补全：
- 输入时AI预测意图
- 显示推荐指令
- 上下文感知建议

输入方式切换：
- 点击麦克风图标 ↔ 点击键盘图标
- 长按说话 / 松手发送
- 语音输入过程中可随时转为文字输入
```

---

## 🚀 功能需求（扩展版）

### 3.1 用户端功能

#### 3.1.1 用户注册与登录
**基础功能：**
- 手机号一键登录（短信验证码）
- 微信一键登录
- Apple ID登录
- Face ID/Touch ID快速登录
- 子女代注册功能（老人手机号验证码确认）
- 紧急联系人绑定（最多5个）

**扩展功能：**
- 身份证OCR识别快速填入
- 医保卡绑定
- 电子健康卡对接
- 生物识别认证（Face ID/Touch ID）
- 多设备登录管理
- 登录设备信任列表
- 异地登录提醒
- 账号安全评分
- 隐私设置精细控制

#### 3.1.2 语音优先交互
**基础功能：**
- 首页大按钮："按住说话"
- 语音转文字实时显示
- 文字输入备选
- 常见指令快捷入口

**扩展功能：**
- 多方言语音识别（粤语、四川话、东北话等）
- 语音指令自定义（"小颐小颐"唤醒词）
- 离线语音识别基础功能
- 语音播报AI回答
- 语速、音色可调节
- 语音历史记录回放
- 语音收藏功能
- 语音转文字编辑
- 实时语音翻译（方言→普通话）
- 语音情绪识别
- 语音打断功能
- 环境噪音抑制
- 语音指令学习模式

#### 3.1.3 病历上传与智能解析
**基础功能：**
- 拍照上传病历
- 相册选择病历图片
- OCR文字识别
- AI结构化提取
- 用户确认/修改

**扩展功能：**
- 多页病历连续拍摄
- 自动裁剪和矫正
- 图片增强处理
- 批量上传解析
- 多种病历类型识别：
  - 门诊病历
  - 住院病历
  - 出院小结
  - 检查报告（CT、MRI、B超、X光）
  - 化验单（血常规、尿常规、生化全套）
  - 处方笺
  - 病理报告
  - 心电图报告
  - 体检报告
- 智能分类归档
- 关键指标高亮标注
- 异常值智能识别
- 历史对比分析
- 趋势图表生成
- PDF病历导入
- 电子病历对接（医院HIS系统）
- 病历翻译（医学术语→通俗解释）
- 病历分享（医生端、子女端）
- 病历打印服务
- 病历云备份
- 解析结果人工修正
- 解析质量评分
- 低置信度人工审核

#### 3.1.4 个人健康档案库
**基础功能：**
- 时间轴展示历史记录
- 按病种筛选
- 详情查看

**扩展功能：**
- 多维度分类：
  - 按时间（日/周/月/年）
  - 按病种（慢性病、急性病、体检）
  - 按医院
  - 按科室
  - 按检查类型
- 智能搜索（关键词、指标值、日期范围）
- 标签管理（自定义标签）
- 收藏夹功能
- 档案导出（PDF、Excel、JSON）
- 档案打印
- 档案分享（加密链接）
- 档案对比（不同时期对比）
- 档案统计（就医次数、花费统计）
- 档案完整性评分
- 缺失检查提醒
- 档案可视化（健康地图）
- 档案版本管理
- 档案回收站
- 批量操作（删除、移动、导出）

#### 3.1.5 实时健康监测
**基础功能：**
- 手动录入健康指标
- 指标历史记录

**扩展功能：**
- 多指标监测：
  - 血压（收缩压、舒张压、脉压差）
  - 血糖（空腹、餐后2小时、随机）
  - 心率（静息、运动后）
  - 体温
  - 血氧饱和度
  - 体重、BMI、体脂率
  - 腰围、臀围
  - 步数、运动时长
  - 睡眠时长、睡眠质量
  - 饮水量
  - 排便情况
- 智能设备数据接入：
  - Apple Watch
  - 华为手环/手表
  - 小米手环
  - 欧姆龙血压计
  - 强生血糖仪
  - 鱼跃医疗设备
  - 其他蓝牙/WiFi设备
- 自动数据同步
- 数据异常预警
- 趋势分析与预测
- 智能健康评分
- 健康报告生成（日/周/月）
- 指标目标设置
- 达标进度追踪
- 数据可视化图表
- 数据导出与分享
- 家庭成员数据对比
- 历史数据回溯
- 数据缺失提醒

#### 3.1.6 AI智能对话与建议
**基础功能：**
- 语音/文字提问
- AI个性化回答
- 档案上下文注入

**扩展功能：**
- 多轮对话记忆
- 对话主题分类
- 常见问题快捷入口
- AI回答质量评分
- AI回答收藏
- AI回答分享
- 专业术语解释
- 相关知识推荐
- 相关病历推荐
- 医生在线对接
- AI回答审核机制
- 敏感问题拦截
- 回答来源标注
- 多模型回答对比
- 回答历史记录
- 对话导出
- 个性化prompt设置
- AI角色切换（营养师、运动教练、心理顾问）
- 情绪识别与关怀
- 危机干预机制
- AI回答反馈机制
- 回答准确度评估

#### 3.1.7 智能提醒系统
**基础功能：**
- 用药提醒
- 复诊提醒
- 饮食建议

**扩展功能：**
- 多类型提醒：
  - 用药提醒（支持复杂用药方案）
  - 复诊提醒（自动计算下次复诊时间）
  - 检查提醒（定期检查项目）
  - 饮食提醒（个性化饮食建议）
  - 运动提醒（运动计划执行）
  - 喝水提醒（定时提醒喝水）
  - 睡眠提醒（作息规律提醒）
  - 测量提醒（定时测量血压血糖）
  - 疫苗接种提醒
  - 体检提醒
  - 生日提醒（医生、家人）
  - 纪念日提醒
- 智能提醒生成：
  - 根据病历自动生成
  - 根据季节变化调整
  - 根据天气变化调整
  - 根据健康数据动态调整
- 提醒方式：
  - App推送
  - 短信提醒
  - 电话提醒（紧急情况）
  - 微信订阅消息
  - 邮件提醒
  - 智能音箱播报
- 提醒管理：
  - 提醒时间自定义
  - 提醒频率设置
  - 提醒声音选择
  - 提醒振动模式
  - 免打扰时段
  - 提醒暂停/恢复
  - 提醒历史记录
  - 提醒完成率统计
- 家人协同提醒：
  - 子女代设置提醒
  - 提醒完成通知子女
  - 未完成提醒升级通知
  - 家人监督模式

#### 3.1.8 饮食营养管理
**基础功能：**
- 饮食建议

**扩展功能：**
- 饮食记录：
  - 拍照识别食物
  - 语音记录饮食
  - 文字搜索添加
  - 扫描食品条码
  - 快速选择常用食物
  - 批量添加
- 营养分析：
  - 热量计算
  - 营养成分分析（蛋白质、脂肪、碳水、维生素、矿物质）
  - 营养均衡评估
  - 营养缺口分析
  - 三大营养素比例
- 智能推荐：
  - 个性化食谱推荐
  - 根据疾病推荐禁忌食物
  - 根据季节推荐时令食物
  - 根据体质推荐适宜食物
  - 一周食谱规划
  - 智能菜谱生成
- 饮食报告：
  - 每日饮食报告
  - 每周饮食总结
  - 每月饮食趋势
  - 饮食评分
- 特殊饮食管理：
  - 糖尿病饮食
  - 高血压饮食
  - 高血脂饮食
  - 痛风饮食
  - 肾病饮食
  - 胃病饮食
  - 孕期饮食
- 食物库：
  - 10万+食物数据库
  - 常见菜品营养数据
  - 餐厅菜品数据
  - 自定义食物
- 饮食目标：
  - 减重目标
  - 增肌目标
  - 控糖目标
  - 健康维持目标

#### 3.1.9 运动健康管理
**基础功能：**
- 运动建议

**扩展功能：**
- 运动记录：
  - 自动记录（设备同步）
  - 手动记录
  - 运动类型选择（步行、跑步、游泳、骑行等）
  - 运动时长、距离、消耗热量
  - 运动轨迹记录
  - 运动心率监测
- 运动计划：
  - AI生成个性化运动计划
  - 计划执行进度
  - 计划调整建议
  - 休息日安排
- 运动指导：
  - 运动视频教程
  - 动作分解教学
  - 呼吸指导
  - 热身拉伸指导
  - 运动后放松指导
- 运动安全：
  - 根据健康状况推荐适宜运动
  - 运动强度控制
  - 心率区间指导
  - 运动禁忌提醒
  - 运动伤害预防
- 运动数据：
  - 步数统计
  - 运动时长统计
  - 消耗热量统计
  - 运动频率统计
  - 运动趋势图表
- 运动挑战：
  - 每日步数挑战
  - 运动打卡挑战
  - 好友运动PK
  - 运动成就系统

#### 3.1.10 用药管理
**基础功能：**
- 用药提醒

**扩展功能：**
- 药品管理：
  - 药品信息录入（拍照识别、手动输入）
  - 药品说明书识别
  - 药品库存管理
  - 药品过期提醒
  - 药品购买提醒
  - 药品相互作用检查
  - 药品禁忌提醒
- 用药记录：
  - 服药打卡
  - 漏服记录
  - 用药依从性统计
  - 用药效果记录
  - 不良反应记录
- 用药方案：
  - 复杂用药方案支持（多药品、多时段）
  - 用药周期管理
  - 用药调整记录
  - 医生调整用药方案
- 药品信息：
  - 药品详情查看
  - 药品说明书
  - 药品相互作用查询
  - 药品替代方案查询
  - 药品价格对比
  - 药品购买渠道推荐
- 用药报告：
  - 每日用药报告
  - 用药依从性报告
  - 用药效果评估
  - 用药趋势分析

#### 3.1.11 子女远程陪伴
**基础功能：**
- 查看老人健康摘要
- 查看对话摘要

**扩展功能：**
- 多角色管理：
  - 子女、配偶、家庭医生、护工
  - 不同角色不同权限
  - 权限精细控制
- 健康数据共享：
  - 实时健康数据同步
  - 异常指标预警推送
  - 健康报告定期推送
  - 紧急情况即时通知
- 远程关怀：
  - 远程设置提醒
  - 代预约挂号
  - 代购买药品
  - 代上传病历
  - 远程查看医嘱
- 家庭健康看板：
  - 家庭成员健康总览
  - 家庭健康事件时间线
  - 家庭健康报告
  - 家庭健康对比分析
- 互动功能：
  - 健康关怀留言
  - 语音关怀
  - 视频通话快捷入口
  - 健康任务分配
- 健康周报/月报：
  - 自动生成健康报告
  - 报告定制化
  - 报告分享（医生、家人）
  - 报告历史存档
- 紧急联系：
  - 一键呼叫紧急联系人
  - 紧急情况自动通知
  - 位置共享
  - 紧急求助按钮

#### 3.1.12 医疗服务对接
**基础功能：**
- 医院推荐

**扩展功能：**
- 智能导诊：
  - 症状智能分析
  - 科室智能推荐
  - 医生智能匹配
  - 就诊时机建议
- 在线预约：
  - 医院挂号预约
  - 体检预约
  - 检查预约
  - 疫苗接种预约
  - 多平台对接（微医、好大夫、挂号网）
- 在线问诊：
  - 图文问诊
  - 电话问诊
  - 视频问诊
  - 专家团队问诊
  - 开方购药一体化
- 医院导航：
  - 医院位置导航
  - 科室位置导航
  - 就诊流程指引
  - 停车场信息
- 医疗资源：
  - 医院信息查询
  - 医生信息查询
  - 科室信息查询
  - 医院评价系统
  - 医生评价系统
- 健康服务：
  - 上门护理预约
  - 居家康复服务
  - 中医理疗预约
  - 心理咨询预约
  - 营养咨询预约

#### 3.1.13 健康知识库
**基础功能：**
- 健康资讯

**扩展功能：**
- 知识内容：
  - 疾病百科
  - 药品百科
  - 检查百科
  - 症状百科
  - 饮食营养知识
  - 运动健身知识
  - 心理健康知识
  - 中医养生知识
- 内容形式：
  - 图文文章
  - 短视频科普
  - 音频科普
  - 直播课程
  - 互动问答
  - 3D人体模型
- 个性化推荐：
  - 根据健康状况推荐
  - 根据阅读历史推荐
  - 根据年龄性别推荐
  - 根据季节时令推荐
- 学习功能：
  - 健康课程
  - 学习进度追踪
  - 学习笔记
  - 学习测试
  - 学习证书
- 社区互动：
  - 经验分享
  - 问答社区
  - 专家答疑
  - 病友交流

#### 3.1.14 健康评估与风险预测
**基础功能：**
- 健康评分

**扩展功能：**
- 健康评估：
  - 整体健康评分
  - 心血管风险评估
  - 糖尿病风险评估
  - 骨质疏松风险评估
  - 跌倒风险评估
  - 认知功能评估
  - 抑郁焦虑评估
  - 营养状况评估
  - 生活质量评估
- 风险预测：
  - 疾病发病风险预测
  - 并发症风险预测
  - 急诊风险预测
  - 住院风险预测
- 评估工具：
  - 标准化量表（SF-36、GAD-7、PHQ-9等）
  - 自适应问卷
  - AI智能评估
- 评估报告：
  - 详细评估报告
  - 改善建议
  - 定期复评提醒
  - 历史评估对比

#### 3.1.15 紧急救助系统
**基础功能：**
- 紧急联系人

**扩展功能：**
- 紧急求助：
  - 一键SOS求助
  - 自动发送位置
  - 自动通知紧急联系人
  - 自动拨打120
  - 紧急医疗信息卡
- 跌倒检测：
  - 自动跌倒检测
  - 跌倒后自动求助
  - 跌倒位置记录
  - 跌倒历史分析
- 急救指导：
  - 常见急症处理指导
  - 心肺复苏指导
  - 海姆立克急救法
  - 止血包扎指导
  - 图文+视频指导
- 医疗信息卡：
  - 基本健康信息
  - 过敏史
  - 用药清单
  - 慢性病史
  - 紧急联系人
  - 医保信息
  - 血型信息
- 紧急预案：
  - 个人紧急预案制定
  - 家人紧急预案
  - 紧急联系人顺序
  - 紧急情况处理流程

#### 3.1.16 社交与社区
**基础功能：**
- 无

**扩展功能：**
- 病友社区：
  - 病友圈子
  - 经验分享
  - 互助问答
  - 情感支持
- 健康打卡：
  - 每日健康打卡
  - 打卡挑战
  - 打卡排行榜
  - 打卡奖励
- 好友系统：
  - 添加好友
  - 好友动态
  - 好友互动
  - 隐私设置
- 健康活动：
  - 线上健康活动
  - 线下健康讲座
  - 健康比赛
  - 健康公益

#### 3.1.17 游戏化健康激励
**基础功能：**
- 无

**扩展功能：**
- 积分系统：
  - 健康行为积分
  - 积分规则
  - 积分历史
  - 积分兑换
- 成就系统：
  - 健康成就徽章
  - 成就解锁
  - 成就展示
  - 成就奖励
- 等级系统：
  - 健康等级
  - 升级条件
  - 等级特权
  - 等级展示
- 挑战系统：
  - 个人健康挑战
  - 家庭健康挑战
  - 社区健康挑战
  - 挑战奖励
- 排行榜：
  - 步数排行榜
  - 健康评分排行榜
  - 打卡排行榜
  - 好友排行榜

#### 3.1.18 个人中心
**基础功能：**
- 个人信息管理

**扩展功能：**
- 个人信息：
  - 基本信息编辑
  - 头像设置
  - 健康档案完善
  - 家族病史记录
  - 过敏史记录
  - 手术史记录
  - 疫苗接种记录
- 账号设置：
  - 账号安全设置
  - 隐私设置
  - 通知设置
  - 字体大小设置
  - 语音设置
  - 主题设置
- 数据管理：
  - 数据导出
  - 数据备份
  - 数据恢复
  - 数据清除
  - 数据统计
- 设备管理：
  - 已连接设备
  - 设备数据同步
  - 设备解绑
- 家庭管理：
  - 家庭成员管理
  - 家庭健康看板
  - 家庭报告订阅
- 帮助与反馈：
  - 使用帮助
  - 常见问题
  - 意见反馈
  - 联系客服
  - 关于我们
- 会员中心：
  - 会员权益
  - 会员升级
  - 会员续费
  - 会员专属服务

### 3.2 后台管理系统功能

#### 3.2.1 用户管理
**基础功能：**
- 用户列表查询
- 用户禁用/启用
- 用户数据导出

**扩展功能：**
- 用户画像分析
- 用户行为追踪
- 用户活跃度分析
- 用户留存分析
- 用户价值评估
- 用户分群管理
- 用户标签管理
- 用户生命周期管理
- 用户流失预警
- 用户召回策略

#### 3.2.2 内容管理
**基础功能：**
- 广告位配置

**扩展功能：**
- 健康知识库管理
- 资讯文章管理
- 视频内容管理
- 课程内容管理
- 专题活动管理
- 内容审核机制
- 内容推荐算法配置
- 内容数据分析

#### 3.2.3 医疗资源管理
**基础功能：**
- 无

**扩展功能：**
- 医院信息管理
- 医生信息管理
- 科室信息管理
- 医疗服务管理
- 预约订单管理
- 问诊记录管理
- 医疗机构合作管理

#### 3.2.4 数据统计与分析
**基础功能：**
- 基础数据统计

**扩展功能：**
- 实时数据监控
- 用户行为分析
- 功能使用分析
- 健康数据分析
- AI对话分析
- 业务漏斗分析
- 自定义报表
- 数据可视化大屏
- 数据导出与分享
- 预测分析

#### 3.2.5 运营管理
**基础功能：**
- 无

**扩展功能：**
- 活动运营管理
- 推送消息管理
- 优惠券管理
- 积分规则管理
- 会员权益管理
- 客服工单管理
- 用户反馈管理
- 运营策略配置

#### 3.2.6 系统管理
**基础功能：**
- 无

**扩展功能：**
- 角色权限管理
- 管理员账号管理
- 操作日志审计
- 系统配置管理
- API接口管理
- 第三方服务配置
- 系统监控告警
- 数据备份恢复

---

## 💾 数据模型（扩展版）

### 4.1 用户相关表

#### 4.1.1 用户表 `users`
```sql
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    phone VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100),
    avatar_url TEXT,
    gender VARCHAR(10),
    birth_date DATE,
    id_card VARCHAR(18),
    emergency_contacts JSON,
    family_members JSON,
    health_profile JSON,
    preferences JSON,
    membership_level VARCHAR(20) DEFAULT 'free',
    membership_expire_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'active',
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(50),
    device_tokens JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 4.1.2 家庭关系表 `family_relations`
```sql
CREATE TABLE family_relations (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    member_id VARCHAR(36) NOT NULL,
    relation_type VARCHAR(20) NOT NULL,
    permission_level VARCHAR(20) DEFAULT 'view',
    is_approved BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (member_id) REFERENCES users(id)
);
```

### 4.2 健康档案相关表

#### 4.2.1 健康档案表 `health_records`
```sql
CREATE TABLE health_records (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    record_type VARCHAR(50) NOT NULL,
    image_urls JSON,
    ocr_raw_text TEXT,
    parsed_data JSON,
    diagnosis VARCHAR(200),
    indicators JSON,
    medications JSON,
    hospital_name VARCHAR(200),
    department VARCHAR(100),
    doctor_name VARCHAR(100),
    record_date DATE,
    confidence FLOAT,
    status VARCHAR(20) DEFAULT 'pending',
    tags JSON,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 4.2.2 健康指标表 `health_indicators`
```sql
CREATE TABLE health_indicators (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    indicator_type VARCHAR(50) NOT NULL,
    value DECIMAL(10,2),
    unit VARCHAR(20),
    level VARCHAR(20),
    measured_at TIMESTAMP,
    source VARCHAR(50),
    device_id VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 4.2.3 健康目标表 `health_goals`
```sql
CREATE TABLE health_goals (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    goal_type VARCHAR(50) NOT NULL,
    target_value DECIMAL(10,2),
    current_value DECIMAL(10,2),
    unit VARCHAR(20),
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) DEFAULT 'active',
    progress FLOAT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 4.3 生活方式管理表

#### 4.3.1 饮食记录表 `diet_records`
```sql
CREATE TABLE diet_records (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    meal_type VARCHAR(20) NOT NULL,
    foods JSON NOT NULL,
    total_calories DECIMAL(10,2),
    nutrients JSON,
    image_url TEXT,
    notes TEXT,
    ate_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 4.3.2 运动记录表 `exercise_records`
```sql
CREATE TABLE exercise_records (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    exercise_type VARCHAR(50) NOT NULL,
    duration INT,
    distance DECIMAL(10,2),
    calories DECIMAL(10,2),
    heart_rate_avg INT,
    heart_rate_max INT,
    gps_track JSON,
    source VARCHAR(50),
    device_id VARCHAR(100),
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 4.3.3 用药记录表 `medication_records`
```sql
CREATE TABLE medication_records (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    medication_name VARCHAR(200) NOT NULL,
    dosage VARCHAR(100),
    frequency VARCHAR(100),
    start_date DATE,
    end_date DATE,
    prescribed_by VARCHAR(100),
    prescribed_at DATE,
    stock_quantity INT,
    stock_unit VARCHAR(20),
    expire_date DATE,
    notes TEXT,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 4.3.4 服药打卡表 `medication_logs`
```sql
CREATE TABLE medication_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    medication_id VARCHAR(36) NOT NULL,
    scheduled_time TIMESTAMP,
    actual_time TIMESTAMP,
    status VARCHAR(20) DEFAULT 'pending',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (medication_id) REFERENCES medication_records(id)
);
```

### 4.4 AI对话相关表

#### 4.4.1 对话会话表 `chat_sessions`
```sql
CREATE TABLE chat_sessions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    title VARCHAR(200),
    context JSON,
    model_used VARCHAR(50),
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 4.4.2 对话消息表 `chat_messages`
```sql
CREATE TABLE chat_messages (
    id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    voice_url TEXT,
    injected_context JSON,
    model_used VARCHAR(50),
    tokens_used INT,
    response_time INT,
    rating INT,
    feedback TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES chat_sessions(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 4.5 提醒相关表

#### 4.5.1 提醒任务表 `reminders`
```sql
CREATE TABLE reminders (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    reminder_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    scheduled_time TIMESTAMP,
    repeat_rule JSON,
    notify_methods JSON,
    priority VARCHAR(20) DEFAULT 'normal',
    status VARCHAR(20) DEFAULT 'pending',
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 4.5.2 提醒执行记录表 `reminder_logs`
```sql
CREATE TABLE reminder_logs (
    id VARCHAR(36) PRIMARY KEY,
    reminder_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    executed_at TIMESTAMP,
    notify_method VARCHAR(20),
    status VARCHAR(20),
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reminder_id) REFERENCES reminders(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 4.6 医疗服务相关表

#### 4.6.1 医院信息表 `hospitals`
```sql
CREATE TABLE hospitals (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    level VARCHAR(50),
    type VARCHAR(50),
    address TEXT,
    location JSON,
    phone VARCHAR(50),
    website VARCHAR(200),
    departments JSON,
    rating FLOAT,
    introduction TEXT,
    images JSON,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 4.6.2 医生信息表 `doctors`
```sql
CREATE TABLE doctors (
    id VARCHAR(36) PRIMARY KEY,
    hospital_id VARCHAR(36),
    name VARCHAR(100) NOT NULL,
    title VARCHAR(100),
    department VARCHAR(100),
    specialty JSON,
    introduction TEXT,
    avatar_url TEXT,
    rating FLOAT,
    consultation_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(id)
);
```

#### 4.6.3 预约记录表 `appointments`
```sql
CREATE TABLE appointments (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    hospital_id VARCHAR(36),
    doctor_id VARCHAR(36),
    department VARCHAR(100),
    appointment_time TIMESTAMP,
    appointment_type VARCHAR(50),
    status VARCHAR(20) DEFAULT 'pending',
    notes TEXT,
    cancel_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);
```

### 4.7 内容相关表

#### 4.7.1 健康知识表 `health_knowledge`
```sql
CREATE TABLE health_knowledge (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(50),
    tags JSON,
    content_type VARCHAR(20),
    video_url TEXT,
    audio_url TEXT,
    images JSON,
    author VARCHAR(100),
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    share_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'published',
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 4.7.2 用户收藏表 `user_favorites`
```sql
CREATE TABLE user_favorites (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 4.8 社交相关表

#### 4.8.1 用户关系表 `user_relations`
```sql
CREATE TABLE user_relations (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    friend_id VARCHAR(36) NOT NULL,
    relation_type VARCHAR(20) DEFAULT 'friend',
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (friend_id) REFERENCES users(id)
);
```

#### 4.8.2 动态发布表 `posts`
```sql
CREATE TABLE posts (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    content TEXT,
    images JSON,
    video_url TEXT,
    post_type VARCHAR(20),
    tags JSON,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    share_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'published',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 4.9 游戏化相关表

#### 4.9.1 用户积分表 `user_points`
```sql
CREATE TABLE user_points (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    total_points INT DEFAULT 0,
    available_points INT DEFAULT 0,
    level INT DEFAULT 1,
    experience INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 4.9.2 积分记录表 `point_logs`
```sql
CREATE TABLE point_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    points INT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 4.9.3 成就表 `achievements`
```sql
CREATE TABLE achievements (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon_url TEXT,
    condition_type VARCHAR(50),
    condition_value JSON,
    points INT,
    level VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 4.9.4 用户成就表 `user_achievements`
```sql
CREATE TABLE user_achievements (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    achievement_id VARCHAR(36) NOT NULL,
    unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (achievement_id) REFERENCES achievements(id)
);
```

### 4.10 系统相关表

#### 4.10.1 操作日志表 `operation_logs`
```sql
CREATE TABLE operation_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    user_type VARCHAR(20),
    operation VARCHAR(100) NOT NULL,
    target_type VARCHAR(50),
    target_id VARCHAR(36),
    details JSON,
    ip_address VARCHAR(50),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 4.10.2 系统配置表 `system_configs`
```sql
CREATE TABLE system_configs (
    id VARCHAR(36) PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🔌 核心API接口设计（扩展版）

### 5.1 用户端API

#### 5.1.1 认证相关
```
POST /api/auth/login              # 手机号登录
POST /api/auth/login_wechat       # 微信登录
POST /api/auth/login_apple        # Apple ID登录
POST /api/auth/verify_code        # 发送验证码
POST /api/auth/refresh_token      # 刷新token
POST /api/auth/logout             # 登出
POST /api/auth/bind_biometric     # 绑定生物识别
POST /api/auth/biometric_login    # 生物识别登录
```

#### 5.1.2 用户信息相关
```
GET  /api/user/profile            # 获取用户信息
PUT  /api/user/profile            # 更新用户信息
GET  /api/user/health_profile     # 获取健康档案
PUT  /api/user/health_profile     # 更新健康档案
GET  /api/user/settings           # 获取用户设置
PUT  /api/user/settings           # 更新用户设置
POST /api/user/avatar             # 上传头像
```

#### 5.1.3 健康档案相关
```
POST /api/records/upload          # 上传病历
GET  /api/records/list            # 获取档案列表
GET  /api/records/:id             # 获取档案详情
PUT  /api/records/:id             # 更新档案
DELETE /api/records/:id           # 删除档案
POST /api/records/:id/confirm    # 确认解析结果
GET  /api/records/search         # 搜索档案
GET  /api/records/export         # 导出档案
```

#### 5.1.4 健康指标相关
```
POST /api/indicators             # 录入健康指标
GET  /api/indicators             # 获取指标列表
GET  /api/indicators/trend       # 获取指标趋势
GET  /api/indicators/analysis    # 指标分析报告
POST /api/indicators/sync_device # 同步设备数据
```

#### 5.1.5 饮食管理相关
```
POST /api/diet                   # 记录饮食
GET  /api/diet                   # 获取饮食记录
GET  /api/diet/analysis          # 饮食分析
GET  /api/diet/recommend         # 饮食推荐
POST /api/diet/recognize         # 食物识别
```

#### 5.1.6 运动管理相关
```
POST /api/exercise               # 记录运动
GET  /api/exercise               # 获取运动记录
GET  /api/exercise/plan          # 获取运动计划
POST /api/exercise/plan          # 创建运动计划
GET  /api/exercise/statistics    # 运动统计
```

#### 5.1.7 用药管理相关
```
POST /api/medications            # 添加药品
GET  /api/medications            # 获取药品列表
PUT  /api/medications/:id        # 更新药品信息
DELETE /api/medications/:id      # 删除药品
POST /api/medications/:id/take   # 服药打卡
GET  /api/medications/report     # 用药报告
```

#### 5.1.8 AI对话相关
```
POST /api/chat/sessions          # 创建对话会话
GET  /api/chat/sessions          # 获取会话列表
POST /api/chat/message           # 发送消息
GET  /api/chat/history           # 获取对话历史
POST /api/chat/message/:id/rate  # 评价回答
```

#### 5.1.9 提醒相关
```
POST /api/reminders              # 创建提醒
GET  /api/reminders              # 获取提醒列表
PUT  /api/reminders/:id          # 更新提醒
DELETE /api/reminders/:id        # 删除提醒
POST /api/reminders/:id/complete # 完成提醒
```

#### 5.1.10 家庭相关
```
POST /api/family/invite          # 邀请家庭成员
GET  /api/family/members         # 获取家庭成员
PUT  /api/family/:id/permission  # 设置权限
DELETE /api/family/:id           # 移除成员
GET  /api/family/dashboard       # 家庭健康看板
```

#### 5.1.11 医疗服务相关
```
GET  /api/hospitals              # 获取医院列表
GET  /api/hospitals/:id         # 医院详情
GET  /api/doctors                # 获取医生列表
GET  /api/doctors/:id            # 医生详情
POST /api/appointments           # 创建预约
GET  /api/appointments           # 获取预约列表
PUT  /api/appointments/:id      # 更新预约
DELETE /api/appointments/:id     # 取消预约
```

#### 5.1.12 知识库相关
```
GET  /api/knowledge              # 获取知识列表
GET  /api/knowledge/:id          # 知识详情
GET  /api/knowledge/search       # 搜索知识
GET  /api/knowledge/recommend   # 推荐知识
POST /api/knowledge/:id/like     # 点赞
POST /api/knowledge/:id/share    # 分享
```

#### 5.1.13 社交相关
```
POST /api/social/friend          # 添加好友
GET  /api/social/friends         # 好友列表
POST /api/social/post            # 发布动态
GET  /api/social/posts           # 获取动态
POST /api/social/post/:id/like   # 点赞动态
POST /api/social/post/:id/comment # 评论动态
```

#### 5.1.14 游戏化相关
```
GET  /api/gamification/points    # 获取积分
GET  /api/gamification/achievements # 获取成就
GET  /api/gamification/leaderboard # 排行榜
POST /api/gamification/checkin   # 打卡
```

#### 5.1.15 紧急救助相关
```
POST /api/emergency/sos          # 紧急求助
GET  /api/emergency/info         # 紧急医疗信息
POST /api/emergency/fall_report  # 跌倒报告
```

### 5.2 后台管理API

#### 5.2.1 用户管理
```
GET  /admin/users                # 用户列表
GET  /admin/users/:id            # 用户详情
PUT  /admin/users/:id/status     # 更新用户状态
GET  /admin/users/statistics     # 用户统计
GET  /admin/users/analysis      # 用户分析
```

#### 5.2.2 内容管理
```
GET  /admin/content              # 内容列表
POST /admin/content             # 创建内容
PUT  /admin/content/:id          # 更新内容
DELETE /admin/content/:id       # 删除内容
PUT  /admin/content/:id/audit   # 审核内容
```

#### 5.2.3 医疗资源管理
```
GET  /admin/hospitals            # 医院列表
POST /admin/hospitals            # 添加医院
PUT  /admin/hospitals/:id        # 更新医院
GET  /admin/doctors              # 医生列表
POST /admin/doctors              # 添加医生
PUT  /admin/doctors/:id          # 更新医生
```

#### 5.2.4 数据统计
```
GET  /admin/statistics/overview  # 数据概览
GET  /admin/statistics/users     # 用户统计
GET  /admin/statistics/health    # 健康数据统计
GET  /admin/statistics/ai        # AI使用统计
GET  /admin/statistics/custom    # 自定义报表
```

#### 5.2.5 系统管理
```
GET  /admin/system/config        # 系统配置
PUT  /admin/system/config        # 更新配置
GET  /admin/system/logs         # 操作日志
GET  /admin/system/monitor      # 系统监控
```

---

## 🤖 AI能力架构

### 6.1 大模型抽象层

```python
from abc import ABC, abstractmethod
from typing import Dict, List, Optional

class BaseLLM(ABC):
    """大模型基类"""
    
    def __init__(self, config: Dict):
        self.config = config
        self.model_name = config.get('model_name')
        self.api_key = config.get('api_key')
        self.base_url = config.get('base_url')
    
    @abstractmethod
    def chat(
        self, 
        messages: List[Dict], 
        temperature: float = 0.7,
        max_tokens: int = 2000,
        stream: bool = False
    ) -> str:
        """对话接口"""
        pass
    
    @abstractmethod
    def extract_medical_info(self, ocr_text: str) -> Dict:
        """从OCR文本提取医疗信息"""
        pass
    
    @abstractmethod
    def analyze_health_data(self, data: Dict) -> Dict:
        """分析健康数据"""
        pass
    
    @abstractmethod
    def generate_reminder(self, context: Dict) -> Dict:
        """生成提醒内容"""
        pass
    
    @abstractmethod
    def health_assessment(self, user_data: Dict) -> Dict:
        """健康评估"""
        pass
    
    @abstractmethod
    def name(self) -> str:
        """返回模型名称"""
        pass
```

### 6.2 支持的模型列表

| 模型名称 | 提供商 | 特点 | 适用场景 |
|---------|--------|------|---------|
| deepseek-v3 | DeepSeek | 低成本、长上下文 | 日常对话、病历解析 |
| gpt-4-turbo | OpenAI | 高质量、多模态 | 复杂推理、图像分析 |
| qwen-max | 阿里云 | 中文优化、长文本 | 中文场景、知识问答 |
| claude-3 | Anthropic | 安全可靠、长上下文 | 医疗建议、健康分析 |
| glm-4 | 智谱AI | 国产、性价比高 | 通用场景 |
| yi-large | 零一万物 | 长上下文、低成本 | 大量文本处理 |

### 6.3 AI功能模块

#### 6.3.1 病历解析模块
- OCR文字识别
- 结构化信息提取
- 医学术语标准化
- 异常指标识别
- 诊断建议生成

#### 6.3.2 健康分析模块
- 健康趋势分析
- 风险预测
- 个性化建议
- 用药相互作用检查
- 饮食营养分析

#### 6.3.3 对话模块
- 多轮对话管理
- 上下文理解
- 意图识别
- 情绪识别
- 个性化回复

#### 6.3.4 提醒生成模块
- 智能提醒生成
- 提醒内容优化
- 提醒时机预测
- 提醒效果评估

---

## 🏗️ 技术架构

### 7.1 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                      客户端层                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │ iOS App  │  │ 小程序   │  │  H5网页  │              │
│  └──────────┘  └──────────┘  └──────────┘              │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                      API网关层                           │
│  ┌──────────────────────────────────────────┐          │
│  │  Kong / Nginx / AWS API Gateway          │          │
│  │  - 负载均衡                               │          │
│  │  - 限流熔断                               │          │
│  │  - 认证授权                               │          │
│  │  - 日志监控                               │          │
│  └──────────────────────────────────────────┘          │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                    微服务层                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │ 用户服务 │  │ 档案服务 │  │ AI服务   │              │
│  └──────────┘  └──────────┘  └──────────┘              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │ 提醒服务 │  │ 医疗服务 │  │ 社交服务 │              │
│  └──────────┘  └──────────┘  └──────────┘              │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                    数据层                                │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │PostgreSQL│  │  Redis   │  │ MongoDB  │              │
│  └──────────┘  └──────────┘  └──────────┘              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │   OSS    │  │Elasticsearch│ │  Kafka  │              │
│  └──────────┘  └──────────┘  └──────────┘              │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                  第三方服务层                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │ OCR服务  │  │ 推送服务 │  │ 支付服务 │              │
│  └──────────┘  └──────────┘  └──────────┘              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │ 地图服务 │  │ 短信服务 │  │ 语音服务 │              │
│  └──────────┘  └──────────┘  └──────────┘              │
└─────────────────────────────────────────────────────────┘
```

### 7.2 技术栈选型

#### 7.2.1 前端技术栈
```
iOS App:
- Swift 5.9
- SwiftUI
- Combine
- Alamofire
- Kingfisher
- Lottie

Android App:
- Kotlin 1.9
- Jetpack Compose
- Kotlin Coroutines
- Retrofit
- Coil
- Hilt (Dagger)

跨平台框架（双平台）:
- Flutter (Dart)
- React Native (TypeScript)
- Taro 3.x / uni-app

小程序:
- Taro 3.x / uni-app
- TypeScript
- Redux / Vuex

H5网页:
- React 18 / Vue 3
- TypeScript
- Tailwind CSS
- Vite

后台管理:
- React 18
- Ant Design Pro
- TypeScript
- UmiJS
```

#### 7.2.2 后端技术栈
```
主语言:
- Python 3.11
- FastAPI
- SQLAlchemy
- Pydantic

辅助语言:
- Go (高性能服务)
- Node.js (实时通信)

数据库:
- PostgreSQL 15 (主数据库)
- Redis 7 (缓存、会话)
- MongoDB 6 (日志、大数据)
- Elasticsearch 8 (全文搜索)

消息队列:
- Kafka / RabbitMQ

任务调度:
- Celery + Redis
- APScheduler

对象存储:
- 阿里云OSS / 腾讯云COS

监控运维:
- Prometheus + Grafana
- ELK Stack
- Sentry
- Docker + Kubernetes
```

### 7.3 安全架构

#### 7.3.1 认证授权
```
认证方式:
- JWT Token
- OAuth 2.0
- Apple Sign In
- 微信登录

授权机制:
- RBAC (基于角色的访问控制)
- ABAC (基于属性的访问控制)
- 细粒度权限控制

安全措施:
- HTTPS强制加密
- 敏感数据加密存储
- SQL注入防护
- XSS防护
- CSRF防护
- 接口签名验证
- 请求频率限制
```

#### 7.3.2 数据安全
```
数据加密:
- 传输加密: TLS 1.3
- 存储加密: AES-256
- 敏感字段加密

数据脱敏:
- 手机号脱敏
- 身份证脱敏
- 病历数据脱敏

数据备份:
- 每日自动备份
- 异地容灾备份
- 增量备份

数据审计:
- 操作日志记录
- 数据变更追踪
- 异常访问告警
```

---

## 📊 性能要求

### 8.1 响应时间
```
页面加载: ≤ 2秒
API响应: ≤ 500ms (P95)
病历解析: ≤ 5秒
AI对话首字: ≤ 2秒
图片上传: ≤ 3秒
```

### 8.2 并发能力
```
初期: 100 QPS
中期: 1000 QPS
长期: 10000 QPS
```

### 8.3 可用性
```
系统可用性: ≥ 99.9%
数据一致性: 强一致性
容灾能力: 异地多活
```

---

## 🚢 部署架构

### 9.1 环境划分
```
开发环境
测试环境
预发布环境
生产环境
```

### 9.2 CI/CD流程
```
代码提交 → 自动测试 → 代码审查 → 
构建镜像 → 部署测试 → 自动化测试 → 
部署预发布 → 人工验收 → 部署生产 → 监控告警
```

---

## 📈 扩展性设计

### 10.1 水平扩展
- 无状态服务设计
- 数据库读写分离
- 缓存集群
- CDN加速

### 10.2 插件化架构
- AI模型插件化
- 第三方服务插件化
- 业务规则插件化
- 通知渠道插件化

### 10.3 事件驱动
- 事件总线
- 异步处理
- 最终一致性
- 事件溯源

---

## 🎯 开发计划

### 11.1 MVP阶段（1-2个月）
- 用户注册登录
- 病历上传解析
- 健康档案管理
- AI基础对话
- 用药提醒

### 11.2 第二阶段（3-4个月）
- 健康指标监测
- 饮食营养管理
- 运动健康管理
- 子女远程陪伴
- 智能提醒系统

### 11.3 第三阶段（5-6个月）
- 医疗服务对接
- 健康知识库
- 社交功能
- 游戏化激励
- 会员体系

### 11.4 第四阶段（7-8个月）
- 健康评估预测
- 紧急救助系统
- 数据分析大屏
- 性能优化
- 安全加固

---

## 📝 附录

### 12.1 术语表
- OCR: 光学字符识别
- LLM: 大语言模型
- QPS: 每秒查询数
- MVP: 最小可行产品
- RBAC: 基于角色的访问控制

### 12.2 参考文档
- Apple Human Interface Guidelines
- 微信小程序开发文档
- FastAPI官方文档
- PostgreSQL官方文档

### 12.3 变更记录
- v1.0: 初始版本
- v2.0: 扩展版本（本文档）

---

**文档版本**: v2.0  
**最后更新**: 2026-06-10  
**编写者**: AI助手
