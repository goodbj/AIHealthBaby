package com.yikangbao.app.data.api

import com.yikangbao.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface YiKangBaoApi {

    // Auth APIs
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthToken>

    @POST("api/auth/verify_code")
    suspend fun sendSmsCode(@Body request: SendCodeRequest): Response<Unit>

    @GET("api/user/profile")
    suspend fun getProfile(): Response<User>

    // Health Record APIs
    @POST("api/records/upload")
    suspend fun uploadRecord(@Body request: UploadRecordRequest): Response<HealthRecord>

    @GET("api/records/list")
    suspend fun getRecords(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): Response<List<HealthRecord>>

    @GET("api/records/{id}")
    suspend fun getRecordById(@Path("id") id: String): Response<HealthRecord>

    @POST("api/records/{id}/confirm")
    suspend fun confirmRecord(
        @Path("id") id: String,
        @Body parsedData: Map<String, String>
    ): Response<Unit>

    @DELETE("api/records/{id}")
    suspend fun deleteRecord(@Path("id") id: String): Response<Unit>

    // Chat APIs
    @POST("api/chat/sessions")
    suspend fun createChatSession(): Response<ChatSession>

    @GET("api/chat/sessions")
    suspend fun getChatSessions(): Response<List<ChatSession>>

    @POST("api/chat/message")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<SendMessageResponse>

    @GET("api/chat/history/{sessionId}")
    suspend fun getChatHistory(@Path("sessionId") sessionId: String): Response<List<ChatMessage>>

    // Reminder APIs
    @POST("api/reminders")
    suspend fun createReminder(@Body request: CreateReminderRequest): Response<Reminder>

    @GET("api/reminders")
    suspend fun getReminders(): Response<List<Reminder>>

    @PUT("api/reminders/{id}")
    suspend fun updateReminder(
        @Path("id") id: String,
        @Body reminder: Reminder
    ): Response<Unit>

    @DELETE("api/reminders/{id}")
    suspend fun deleteReminder(@Path("id") id: String): Response<Unit>

    @POST("api/reminders/{id}/complete")
    suspend fun completeReminder(@Path("id") id: String): Response<Unit>

    // Health Indicator APIs
    @POST("api/indicators")
    suspend fun addIndicator(@Body indicator: HealthIndicator): Response<HealthIndicator>

    @GET("api/indicators")
    suspend fun getIndicators(
        @Query("type") type: String?,
        @Query("start_date") startDate: Long?,
        @Query("end_date") endDate: Long?
    ): Response<List<HealthIndicator>>
}
