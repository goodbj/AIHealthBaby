package com.yikangbao.app.domain.repository

import com.yikangbao.app.data.model.ChatMessage
import com.yikangbao.app.data.model.ChatSession
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(content: String, sessionId: String? = null): Result<ChatMessage>
    suspend fun getSessions(): Result<List<ChatSession>>
    suspend fun getChatHistory(sessionId: String): Result<List<ChatMessage>>
    fun observeMessages(): Flow<ChatMessage>
}
