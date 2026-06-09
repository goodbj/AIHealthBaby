package com.yikangbao.app.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikangbao.app.data.model.HealthIndicator
import com.yikangbao.app.domain.repository.AuthRepository
import com.yikangbao.app.domain.repository.HealthRecordRepository
import com.yikangbao.app.voice.CommandResult
import com.yikangbao.app.voice.VoiceInputManager
import com.yikangbao.app.voice.VoiceInputUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val todayIndicators: List<HealthIndicator> = emptyList(),
    val recentRecordsCount: Int = 0,
    val pendingRemindersCount: Int = 0,
    val error: String? = null,
    val voiceState: VoiceInputUiState = VoiceInputUiState(),
    val commandMessage: String? = null,
    val navigateTo: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val authRepository: AuthRepository,
    private val healthRecordRepository: HealthRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val voiceManager = VoiceInputManager(context, viewModelScope)

    init {
        loadHomeData()
        observeVoiceInput()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            authRepository.getCurrentUser()
                .onSuccess { user ->
                    _uiState.update { it.copy(userName = user.name ?: "用户") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }

            healthRecordRepository.getRecords(1, 5)
                .onSuccess { records ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            recentRecordsCount = records.size
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startVoiceInput() {
        if (!hasRecordPermission()) {
            _uiState.update { it.copy(error = "需要录音权限") }
            return
        }
        voiceManager.startVoiceInput()
    }

    fun stopVoiceInput() {
        voiceManager.stopVoiceInput()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearCommandMessage() {
        _uiState.update { it.copy(commandMessage = null) }
    }

    fun clearNavigation() {
        _uiState.update { it.copy(navigateTo = null) }
    }

    fun processTextInput(text: String) {
        voiceManager.processText(text)
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }

    private fun observeVoiceInput() {
        viewModelScope.launch {
            voiceManager.uiState.collect { voiceState ->
                _uiState.update { it.copy(voiceState = voiceState) }
            }
        }

        viewModelScope.launch {
            voiceManager.commandResult.collect { result ->
                when (result) {
                    is CommandResult.Message -> {
                        _uiState.update { it.copy(commandMessage = result.text) }
                    }
                    is CommandResult.Navigate -> {
                        _uiState.update { it.copy(navigateTo = result.destination) }
                    }
                    is CommandResult.Error -> {
                        _uiState.update { it.copy(error = result.message) }
                    }
                    else -> {}
                }
            }
        }
    }
}
