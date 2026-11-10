package com.yikangbao.app.ui.screens.records

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yikangbao.app.ui.components.LoadingIndicator
import com.yikangbao.app.ui.theme.HealthGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailScreen(
    recordId: String,
    onNavigateBack: () -> Unit,
    viewModel: RecordDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(recordId) {
        viewModel.loadRecord(recordId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("档案详情") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        if (uiState.isLoading) {
            LoadingIndicator()
        } else if (uiState.error != null && uiState.record == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    uiState.error ?: "加载失败",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            uiState.record?.let { record ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // 记录头卡：类型 + 日期 + 状态
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HealthGreen.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = HealthGreen
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = record.recordType,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = record.recordDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                AssistChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            when (record.status) {
                                                "confirmed" -> "已确认归档"
                                                "corrected" -> "已人工修正"
                                                else -> "待确认"
                                            }
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (record.status == "pending")
                                            MaterialTheme.colorScheme.errorContainer
                                        else HealthGreen.copy(alpha = 0.15f)
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 诊断
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("诊断结果", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = record.diagnosis?.ifBlank { null } ?: "（AI 未识别出诊断，可拍照重新上传）",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            record.confidence?.let {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "AI 置信度 ${(it * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 指标
                    if (record.indicators?.isNotEmpty() == true) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("指标", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                record.indicators.forEach { ind ->
                                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(
                                            ind.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.width(110.dp)
                                        )
                                        Text(
                                            "${ind.value}${ind.level?.let { "  （$it）" } ?: ""}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 用药
                    if (record.medications?.isNotEmpty() == true) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("用药", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                record.medications.forEach { med ->
                                    Text(
                                        "· ${med.name}${med.dosage?.takeIf { it.isNotBlank() }?.let { "  $it" } ?: ""}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // OCR 原文
                    record.ocrRawText?.takeIf { it.isNotBlank() }?.let { raw ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("OCR 原文", style = MaterialTheme.typography.titleSmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(raw, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    // 确认归档按钮（pending 才显示）
                    if (record.status == "pending") {
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.confirmRecord() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = HealthGreen)
                        ) {
                            Text("确认归档")
                        }
                    }
                }
            }
        }
    }
}
