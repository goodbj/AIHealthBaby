package com.yikangbao.app.data.api

import com.yikangbao.app.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/** 统一响应封装（PRD 6.3：后端所有 API 返回 { code, msg, data, trace_id }）。 */
data class ApiEnvelope<T>(
    val code: Int,
    val msg: String,
    val data: T?,
    val traceId: String?
)

interface YiKangBaoApi {

    // Auth APIs（M1：演示一键登录，验证码固定，见后端 YKB_DEMO_SMS_CODE）
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiEnvelope<AuthToken>>

    @POST("api/auth/verify_code")
    suspend fun sendSmsCode(@Body request: SendCodeRequest): Response<ApiEnvelope<Unit>>

    @GET("api/user/profile")
    suspend fun getProfile(): Response<ApiEnvelope<User>>

    // Health Record APIs（M1 病历闭环：上传→OCR→LLM 结构化→确认→时间轴）
    @Multipart
    @POST("api/records/upload")
    suspend fun uploadRecord(
        @Part("user_id") userId: okhttp3.RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Response<ApiEnvelope<HealthRecord>>

    @GET("api/records/list")
    suspend fun getRecords(
        @Query("user_id") userId: String,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): Response<ApiEnvelope<List<HealthRecord>>>

    @GET("api/records/{id}")
    suspend fun getRecordById(@Path("id") id: String): Response<ApiEnvelope<HealthRecord>>

    @POST("api/records/{id}/confirm")
    suspend fun confirmRecord(
        @Path("id") id: String,
        @Body parsedData: ConfirmRecordRequest
    ): Response<ApiEnvelope<HealthRecord>>

    @DELETE("api/records/{id}")
    suspend fun deleteRecord(@Path("id") id: String): Response<ApiEnvelope<Unit>>

    // Chat APIs（M2 契约保留：/api/chat 已在 M0 落地，sessions 接口后补）
    @POST("api/chat")
    suspend fun chat(@Body request: ChatDirectRequest): Response<ApiEnvelope<ChatReplyData>>

    // Reminder APIs（M2 已落地）
    @POST("api/reminders")
    suspend fun createReminder(@Body request: CreateReminderRequest): Response<ApiEnvelope<Reminder>>

    @GET("api/reminders")
    suspend fun getReminders(@Query("user_id") userId: String): Response<ApiEnvelope<List<Reminder>>>

    @PUT("api/reminders/{id}")
    suspend fun updateReminder(
        @Path("id") id: String,
        @Body reminder: Reminder
    ): Response<ApiEnvelope<Unit>>

    @DELETE("api/reminders/{id}")
    suspend fun deleteReminder(@Path("id") id: String): Response<ApiEnvelope<Unit>>

    @POST("api/reminders/{id}/complete")
    suspend fun completeReminder(@Path("id") id: String): Response<ApiEnvelope<Unit>>

    // Health Indicator APIs（预留）
    @POST("api/indicators")
    suspend fun addIndicator(@Body indicator: HealthIndicator): Response<ApiEnvelope<HealthIndicator>>

    @GET("api/indicators")
    suspend fun getIndicators(
        @Query("type") type: String?,
        @Query("start_date") startDate: Long?,
        @Query("end_date") endDate: Long?
    ): Response<ApiEnvelope<List<HealthIndicator>>>
}

/** 确认修正请求体（后端 /api/records/{id}/confirm）。 */
data class ConfirmRecordRequest(
    val diagnosis: String?,
    val indicators: List<IndicatorItem>? = null,
    val medications: List<Map<String, String>>? = null,
    val record_date: String? = null
)

data class IndicatorItem(
    val name: String,
    val value: String,
    val level: String?
)

/** 直连对话请求/响应（后端 /api/chat，M0 契约）。 */
data class ChatDirectRequest(
    val prompt: String,
    val user_id: String? = null
)

data class ChatReplyData(
    val reply: String,
    val model: String,
    val disclaimer: Boolean
)
