package com.yikangbao.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yikangbao.app.ui.theme.HealthGreen
import com.yikangbao.app.ui.theme.HealthGreenGradient
import kotlinx.coroutines.launch

/**
 * 语音输入按钮组件
 * 支持按下开始录音、松开结束录音的交互方式
 */
@Composable
fun VoiceInputButtonV2(
    isListening: Boolean,
    isProcessing: Boolean,
    amplitude: Float = 0f,
    statusText: String? = null,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    // 脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // 波纹动画
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = if (isListening) 0.3f else 0f,
        targetValue = if (isListening) 0f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // 外圈波纹
            if (isListening) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(HealthGreen.copy(alpha = 0.2f))
                )
                
                // 第二层波纹
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(pulseScale * 0.9f)
                        .clip(CircleShape)
                        .background(HealthGreen.copy(alpha = 0.3f))
                )
            }

            // 主按钮
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .scale(if (isListening) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(HealthGreenGradient)
                    )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "语音输入",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 状态文字
        Text(
            text = when {
                isProcessing -> "正在处理..."
                isListening -> "松开发送"
                else -> "按住说话"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (isListening || isProcessing) HealthGreen 
                   else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // 识别中的文字预览
        if (!statusText.isNullOrEmpty() && isListening) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * 语音波形可视化组件
 */
@Composable
fun VoiceWaveform(
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    val barCount = 5
    val animatedAmplitudes = remember { mutableStateListOf<Float>().apply { repeat(barCount) { add(0.3f) } } }
    
    // 更新波形
    LaunchedEffect(amplitude) {
        if (amplitude > 0) {
            animatedAmplitudes[2] = amplitude // 中间条最高
            animatedAmplitudes[1] = amplitude * 0.8f
            animatedAmplitudes[3] = amplitude * 0.8f
            animatedAmplitudes[0] = amplitude * 0.5f
            animatedAmplitudes[4] = amplitude * 0.5f
        } else {
            repeat(barCount) { animatedAmplitudes[it] = 0.3f }
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        repeat(barCount) { index ->
            val height = (20 + animatedAmplitudes[index] * 40).dp
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(height)
                    .clip(RoundedCornerShape(2.dp))
                    .background(HealthGreen)
            )
        }
    }
}
