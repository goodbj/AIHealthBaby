package com.yikangbao.app.data.repository

import com.yikangbao.app.data.api.YiKangBaoApi
import com.yikangbao.app.data.model.ChatMessage
import com.yikangbao.app.data.model.ChatSession
import com.yikangbao.app.data.model.SendMessageRequest
import com.yikangbao.app.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val api: YiKangBaoApi
) : ChatRepository {

    private val _messagesFlow = MutableSharedFlow<ChatMessage>()

    override suspend fun sendMessage(content: String, sessionId: String?): Result<ChatMessage> {
        return try {
            val response = api.sendMessage(SendMessageRequest(content, sessionId))
            if (response.isSuccessful && response.body() != null) {
                val result = response.body()!!
                _messagesFlow.emit(result.message)
                Result.success(result.message)
            } else {
                Result.failure(Exception("Failed to send message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSessions(): Result<List<ChatSession>> {
        return try {
            val response = api.getChatSessions()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get sessions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getChatHistory(sessionId: String): Result<List<ChatMessage>> {
        return try {
            val response = api.getChatHistory(sessionId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get chat history"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeMessages(): Flow<ChatMessage> = _messagesFlow
}
