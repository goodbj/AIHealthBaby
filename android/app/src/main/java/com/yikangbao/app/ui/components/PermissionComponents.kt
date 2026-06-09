package com.yikangbao.app.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 录音权限请求处理
 * @return 是否有录音权限
 */
@Composable
fun rememberRecordAudioPermissionState(
    onPermissionResult: (Boolean) -> Unit = {}
): RecordAudioPermissionState {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onPermissionResult(granted)
    }

    return remember {
        RecordAudioPermissionState(
            request = { launcher.launch(Manifest.permission.RECORD_AUDIO) }
        )
    }
}

data class RecordAudioPermissionState(
    val request: () -> Unit
)

/**
 * 权限请求对话框
 */
@Composable
fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("需要录音权限") },
        text = {
            Text("语音输入功能需要访问您的麦克风，用于识别您的语音指令。")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("授权")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 语音权限拒绝提示
 */
@Composable
fun PermissionDeniedContent(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            text = "录音权限被拒绝",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "语音输入功能需要录音权限才能正常工作。\n请在系统设置中授予权限。",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onOpenSettings) {
            Text("打开设置")
        }
    }
}
