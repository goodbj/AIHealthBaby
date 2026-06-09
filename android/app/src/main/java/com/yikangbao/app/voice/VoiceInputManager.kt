package com.yikangbao.app.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 语音输入管理器
 * 整合语音识别、指令解析和执行的完整流程
 */
class VoiceInputManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val recognitionManager = VoiceRecognitionManager(context)
    private val commandParser = VoiceCommandParser()
    private val commandExecutor = VoiceCommandExecutor()

    private val _uiState = MutableStateFlow(VoiceInputUiState())
    val uiState: StateFlow<VoiceInputUiState> = _uiState.asStateFlow()

    private val _commandResult = MutableStateFlow<CommandResult?>(null)
    val commandResult: StateFlow<CommandResult?> = _commandResult.asStateFlow()

    init {
        recognitionManager.initialize()
        observeRecognitionState()
    }

    /**
     * 检查是否有录音权限
     */
    fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 开始语音输入
     */
    fun startVoiceInput() {
        if (!hasRecordPermission()) {
            _uiState.value = _uiState.value.copy(
                error = "需要录音权限",
                needsPermission = true
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isListening = true,
            error = null,
            recognizedText = ""
        )
        recognitionManager.startListening()
    }

    /**
     * 停止语音输入
     */
    fun stopVoiceInput() {
        _uiState.value = _uiState.value.copy(isListening = false)
        recognitionManager.stopListening()
    }

    /**
     * 取消当前操作
     */
    fun cancel() {
        recognitionManager.cancel()
        _uiState.value = VoiceInputUiState()
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * 清除命令结果
     */
    fun clearCommandResult() {
        _commandResult.value = null
    }

    /**
     * 手动处理文本（用于文字输入场景）
     */
    fun processText(text: String) {
        scope.launch(Dispatchers.Main) {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            
            val parsed = commandParser.parse(text)
            val result = commandExecutor.execute(parsed.command)
            
            _uiState.value = _uiState.value.copy(isProcessing = false)
            _commandResult.value = result
        }
    }

    /**
     * 释放资源
     */
    fun destroy() {
        recognitionManager.destroy()
    }

    private fun observeRecognitionState() {
        scope.launch(Dispatchers.Main) {
            recognitionManager.state.collect { state ->
                when (state) {
                    is VoiceRecognitionState.Idle -> {
                        _uiState.value = _uiState.value.copy(
                            isListening = false,
                            isProcessing = false
                        )
                    }
                    is VoiceRecognitionState.Listening -> {
                        _uiState.value = _uiState.value.copy(
                            isListening = true,
                            statusText = "正在聆听..."
                        )
                    }
                    is VoiceRecognitionState.Processing -> {
                        _uiState.value = _uiState.value.copy(
                            statusText = state.partialText,
                            recognizedText = state.partialText
                        )
                    }
                    is VoiceRecognitionState.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isListening = false,
                            isProcessing = true,
                            recognizedText = state.text,
                            statusText = "正在处理..."
                        )
                        // 解析并执行指令
                        processRecognizedText(state.text)
                    }
                    is VoiceRecognitionState.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isListening = false,
                            isProcessing = false,
                            error = state.message
                        )
                    }
                    is VoiceRecognitionState.NoMatch -> {
                        _uiState.value = _uiState.value.copy(
                            isListening = false,
                            isProcessing = false,
                            error = "未识别到语音，请重试"
                        )
                    }
                }
            }
        }

        // 监听振幅变化
        scope.launch(Dispatchers.Main) {
            recognitionManager.amplitude.collect { amp ->
                _uiState.value = _uiState.value.copy(amplitude = amp)
            }
        }
    }

    private fun processRecognizedText(text: String) {
        scope.launch(Dispatchers.Main) {
            val parsed = commandParser.parse(text)
            val result = commandExecutor.execute(parsed.command)
            
            _uiState.value = _uiState.value.copy(isProcessing = false)
            _commandResult.value = result
        }
    }
}

/**
 * 语音输入UI状态
 */
data class VoiceInputUiState(
    val isListening: Boolean = false,
    val isProcessing: Boolean = false,
    val recognizedText: String = "",
    val statusText: String = "",
    val amplitude: Float = 0f,
    val error: String? = null,
    val needsPermission: Boolean = false
)
