package com.yikangbao.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

// 苹果风格圆角 - iOS Human Interface Guidelines
// iOS常用圆角尺寸: 8pt, 12pt, 16pt, 20pt, 24pt
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),    // 小按钮、小卡片
    small = RoundedCornerShape(12.dp),        // 按钮、输入框
    medium = RoundedCornerShape(16.dp),       // 卡片、容器
    large = RoundedCornerShape(20.dp),        // 大卡片、模态框
    extraLarge = RoundedCornerShape(24.dp)     // 底部弹窗、全屏卡片
)

// 自定义圆角常量
val AppleButtonCornerRadius = 12.dp
val AppleCardCornerRadius = 16.dp
val AppleSheetCornerRadius = 24.dp