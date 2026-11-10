package com.yikangbao.app.data.repository

import com.yikangbao.app.data.api.ChatDirectRequest
import com.yikangbao.app.data.api.YiKangBaoApi
import com.yikangbao.app.data.model.ChatMessage
import com.yikangbao.app.data.model.ChatSession
import com.yikangbao.app.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val api: YiKangBaoApi
) : ChatRepository {

    private val _messagesFlow = MutableSharedFlow<ChatMessage>()

    override suspend fun sendMessage(content: String, sessionId: String?): Result<ChatMessage> {
        return try {
            // M0 契约 /api/chat：注入档案由后端按 user_id 完成（M2）
            val result = unwrap {
                api.chat(ChatDirectRequest(prompt = content, user_id = AuthSession.userId))
            }
            result.map { data ->
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId ?: "",
                    userId = AuthSession.userId ?: "",
                    role = "assistant",
                    content = data.reply,
                    voiceUrl = null,
                    modelUsed = data.model,
                    tokensUsed = null,
                    createdAt = ""
                )
            }.onSuccess { message ->
                _messagesFlow.emit(message)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSessions(): Result<List<ChatSession>> {
        // sessions 接口 M2 后补：先返回空列表不阻塞 UI
        return Result.success(emptyList())
    }

    override suspend fun getChatHistory(sessionId: String): Result<List<ChatMessage>> {
        // 历史查询后补：先返回空列表
        return Result.success(emptyList())
    }

    override fun observeMessages(): Flow<ChatMessage> = _messagesFlow
}
