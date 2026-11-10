package com.yikangbao.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.yikangbao.app.data.model.HealthRecord
import com.yikangbao.app.ui.screens.records.RecordsViewModel
import com.yikangbao.app.ui.theme.HealthGreen
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * M1 病历上传入口（Issue #180）：拍照 / 相册 → 上传 → OCR+LLM 解析 → 确认修正弹层。
 * 用 ActivityResultContracts（无需权限库）；拍照走 FileProvider 缓存目录。
 */
@Composable
fun RecordUploadSection(
    viewModel: RecordsViewModel,
    onUploadSuccess: (HealthRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pickedPaths by remember { mutableStateOf<List<String>>(emptyList()) }
    var showConfirm by remember { mutableStateOf(false) }
    var uploadedRecord by remember { mutableStateOf<HealthRecord?>(null) }
    val uiState by viewModel.uiState.collectAsState()

    fun newCaptureUri(): Uri {
        val dir = File(context.cacheDir, "images").apply { mkdirs() }
        val name = "ykb_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        val file = File(dir, name)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok && pendingCameraUri != null) {
            pickedPaths = listOf(pendingCameraUri!!.toRealPath(context))
            viewModel.uploadRecord(pickedPaths)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            pickedPaths = uris.take(5).map { it.toRealPath(context) }
            viewModel.uploadRecord(pickedPaths)
        }
    }

    // 上传成功 → 弹确认修正层
    LaunchedEffect(uiState.lastUploaded) {
        uiState.lastUploaded?.let { record ->
            uploadedRecord = record
            showConfirm = true
            viewModel.consumeLastUploaded()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (uiState.isUploading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(12.dp))
                Text("正在识别解析，请稍候…", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        val uri = newCaptureUri()
                        pendingCameraUri = uri
                        cameraLauncher.launch(uri)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthGreen)
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("拍照识别")
                }
                OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("相册导入")
                }
            }
        }
        uiState.error?.let { err ->
            Spacer(Modifier.height(8.dp))
            Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }

    uploadedRecord?.let { record ->
        if (showConfirm) {
            RecordConfirmDialog(
                record = record,
                onDismiss = { showConfirm = false },
                onConfirm = { diagnosis ->
                    viewModel.confirmRecord(record.id, diagnosis)
                    showConfirm = false
                    onUploadSuccess(record)
                }
            )
        }
    }
}

/** 内容 URI 拷贝到缓存目录拿真实路径（后端需要读文件流，走 multipart 直传路径）。 */
private fun Uri.toRealPath(context: android.content.Context): String {
    return try {
        val dir = File(context.cacheDir, "images").apply { mkdirs() }
        val dest = File(dir, "import_${System.nanoTime()}.jpg")
        context.contentResolver.openInputStream(this)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
        dest.absolutePath
    } catch (e: Exception) {
        toString() // 兜底：部分内容提供方直接给路径
    }
}

/**
 * 解析结果确认修正弹层：回显 OCR 全文 + 结构化字段，用户可改诊断后确认归档。
 */
@Composable
fun RecordConfirmDialog(
    record: HealthRecord,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var diagnosis by remember(record.id) { mutableStateOf(record.diagnosis ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("解析完成，请确认") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "置信度 ${(record.confidence ?: 0f) * 100}%，确认后归档进时间轴。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Text("诊断", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("AI 没识别出诊断？手动补一个") },
                    singleLine = true
                )
                if (record.indicators?.isNotEmpty() == true) {
                    Spacer(Modifier.height(12.dp))
                    Text("指标", style = MaterialTheme.typography.labelMedium)
                    record.indicators.forEach { ind ->
                        Text(
                            "· ${ind.name}：${ind.value}${ind.level?.let { "（$it）" } ?: ""}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                record.ocrRawText?.takeIf { it.isNotBlank() }?.let { raw ->
                    Spacer(Modifier.height(12.dp))
                    Text("OCR 原文", style = MaterialTheme.typography.labelMedium)
                    Text(
                        raw.take(200),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(diagnosis.ifBlank { null }) }) { Text("确认归档") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("暂不归档") }
        }
    )
}
