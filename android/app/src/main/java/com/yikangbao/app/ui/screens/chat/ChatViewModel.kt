package com.yikangbao.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikangbao.app.data.model.ChatMessage
import com.yikangbao.app.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val isLoading: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val currentSessionId: String? = null,
    val isTyping: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        observeMessages()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            chatRepository.observeMessages().collect { message ->
                _uiState.update { state ->
                    state.copy(messages = state.messages + message)
                }
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isTyping = true) }

            // Add user message
            val userMessage = ChatMessage(
                id = System.currentTimeMillis().toString(),
                sessionId = _uiState.value.currentSessionId ?: "",
                userId = "",
                role = "user",
                content = content,
                voiceUrl = null,
                modelUsed = null,
                tokensUsed = null,
                createdAt = System.currentTimeMillis().toString()
            )
            _uiState.update { state ->
                state.copy(messages = state.messages + userMessage)
            }

            // Send to API
            chatRepository.sendMessage(content, _uiState.value.currentSessionId)
                .onSuccess { response ->
                    _uiState.update { state ->
                        state.copy(
                            currentSessionId = response.sessionId,
                            isTyping = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isTyping = false, error = e.message)
                    }
                }
        }
    }

    fun loadHistory(sessionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            chatRepository.getChatHistory(sessionId)
                .onSuccess { messages ->
                    _uiState.update {
                        it.copy(isLoading = false, messages = messages)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message)
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
