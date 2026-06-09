package com.yikangbao.app.data.model

import com.google.gson.annotations.SerializedName

data class ChatSession(
    val id: String,
    val userId: String,
    val title: String?,
    val modelUsed: String?,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

data class ChatMessage(
    val id: String,
    val sessionId: String,
    val userId: String,
    val role: String,
    val content: String,
    val voiceUrl: String?,
    val modelUsed: String?,
    val tokensUsed: Int?,
    val createdAt: String
)

data class SendMessageRequest(
    val content: String,
    val sessionId: String?
)

data class SendMessageResponse(
    val message: ChatMessage,
    val sessionId: String
)
