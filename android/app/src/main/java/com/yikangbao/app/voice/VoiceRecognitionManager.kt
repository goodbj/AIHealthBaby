package com.yikangbao.app.voice

import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * 语音识别状态
 */
sealed class VoiceRecognitionState {
    object Idle : VoiceRecognitionState()
    object Listening : VoiceRecognitionState()
    data class Processing(val partialText: String) : VoiceRecognitionState()
    data class Success(val text: String) : VoiceRecognitionState()
    data class Error(val message: String) : VoiceRecognitionState()
    object NoMatch : VoiceRecognitionState()
}

/**
 * 语音识别管理器
 * 封装 Android SpeechRecognizer API，提供语音转文字功能
 */
class VoiceRecognitionManager(private val context: Context) {

    private val TAG = "VoiceRecognition"

    private var speechRecognizer: SpeechRecognizer? = null
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false

    private val _state = MutableStateFlow<VoiceRecognitionState>(VoiceRecognitionState.Idle)
    val state: StateFlow<VoiceRecognitionState> = _state.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    /**
     * 初始化语音识别器
     */
    fun initialize() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(recognitionListener)
            }
        } else {
            Log.w(TAG, "Speech recognition not available on this device")
        }
    }

    /**
     * 开始监听语音
     */
    fun startListening() {
        if (speechRecognizer == null) {
            initialize()
        }

        _state.value = VoiceRecognitionState.Listening
        _recognizedText.value = ""

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE.toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
        }

        speechRecognizer?.startListening(intent)
        startAmplitudeTracking()
    }

    /**
     * 停止监听
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        stopAmplitudeTracking()
        isRecording = false
    }

    /**
     * 取消当前识别
     */
    fun cancel() {
        speechRecognizer?.cancel()
        stopAmplitudeTracking()
        _state.value = VoiceRecognitionState.Idle
    }

    /**
     * 释放资源
     */
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        stopAmplitudeTracking()
        _state.value = VoiceRecognitionState.Idle
    }

    /**
     * 开始跟踪音量振幅（用于波形动画）
     */
    private fun startAmplitudeTracking() {
        isRecording = true
        // 模拟振幅变化，实际项目中可以使用 MediaRecorder 获取真实振幅
        Thread {
            while (isRecording) {
                try {
                    // 模拟波形：随机生成 0.3-1.0 之间的振幅
                    val amp = 0.3f + (Math.random() * 0.7f).toFloat()
                    _amplitude.value = amp
                    Thread.sleep(50)
                } catch (e: InterruptedException) {
                    break
                }
            }
            _amplitude.value = 0f
        }.start()
    }

    private fun stopAmplitudeTracking() {
        isRecording = false
        _amplitude.value = 0f
    }

    /**
     * 语音识别监听器
     */
    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "Ready for speech")
            _state.value = VoiceRecognitionState.Listening
        }

        override fun onBeginningOfSpeech() {
            Log.d(TAG, "Beginning of speech")
        }

        override fun onRmsChanged(rmsdB: Float) {
            // 音量变化，可用于实时波形显示
            _amplitude.value = (rmsdB / 10f).coerceIn(0f, 1f)
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            Log.d(TAG, "End of speech")
            stopAmplitudeTracking()
        }

        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "音频录制错误"
                SpeechRecognizer.ERROR_CLIENT -> "客户端错误"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "权限不足"
                SpeechRecognizer.ERROR_NETWORK -> "网络错误"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络超时"
                SpeechRecognizer.ERROR_NO_MATCH -> "未识别到语音"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别器忙"
                SpeechRecognizer.ERROR_SERVER -> "服务器错误"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "语音超时"
                else -> "未知错误: $error"
            }
            Log.e(TAG, "Recognition error: $errorMessage")
            
            if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                _state.value = VoiceRecognitionState.NoMatch
            } else {
                _state.value = VoiceRecognitionState.Error(errorMessage)
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val text = matches[0]
                Log.d(TAG, "Recognition result: $text")
                _recognizedText.value = text
                _state.value = VoiceRecognitionState.Success(text)
            } else {
                _state.value = VoiceRecognitionState.NoMatch
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val text = matches[0]
                Log.d(TAG, "Partial result: $text")
                _state.value = VoiceRecognitionState.Processing(text)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            Log.d(TAG, "Event: $eventType")
        }
    }
}
