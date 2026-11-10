package com.yikangbao.app.data.repository

import com.yikangbao.app.data.api.ApiEnvelope
import com.yikangbao.app.data.api.YiKangBaoApi
import com.yikangbao.app.data.model.HealthRecord
import com.yikangbao.app.domain.repository.HealthRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** 统一响应封装解封：code != 0 转 Result.failure，业务错误消息直出。 */
suspend fun <T> unwrap(call: suspend () -> Response<ApiEnvelope<T>>): Result<T> =
    try {
        val resp = call()
        val body = resp.body()
        when {
            resp.isSuccessful && body != null && body.code == 0 && body.data != null ->
                Result.success(body.data)
            resp.isSuccessful && body != null && body.code == 0 ->
                Result.success(Unit as T) // data 为空的成功响应
            body != null ->
                Result.failure(Exception(body.msg))
            else ->
                Result.failure(Exception("HTTP ${resp.code()}: ${resp.message()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

@Singleton
class HealthRecordRepositoryImpl @Inject constructor(
    private val api: YiKangBaoApi
) : HealthRecordRepository {

    private val _recordsFlow = MutableStateFlow<List<HealthRecord>>(emptyList())

    override suspend fun uploadRecord(imagePaths: List<String>): Result<HealthRecord> {
        return try {
            // M1 上传需 user_id：复用 AuthRepository 存的登录态（演示模式兜底匿名 ID）
            val userId = AuthSession.userId ?: return Result.failure(Exception("请先登录"))
            val parts = imagePaths.map { path ->
                val file = File(path)
                val mime = when (file.extension.lowercase()) {
                    "png" -> "image/png"
                    "webp" -> "image/webp"
                    "bmp" -> "image/bmp"
                    else -> "image/jpeg"
                }
                MultipartBody.Part.createFormData(
                    "images", file.name, file.asRequestBody(mime.toMediaType())
                )
            }
            if (parts.isEmpty()) return Result.failure(Exception("未选择图片"))
            val uid = userId.toRequestBody("text/plain".toMediaType())
            val result = unwrap { api.uploadRecord(uid, parts) }
            result.onSuccess { record ->
                _recordsFlow.value = listOf(record) + _recordsFlow.value
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecords(page: Int, pageSize: Int): Result<List<HealthRecord>> {
        return try {
            val userId = AuthSession.userId ?: return Result.failure(Exception("请先登录"))
            unwrap { api.getRecords(userId, page, pageSize) }
                .onSuccess { records -> _recordsFlow.value = records }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecordById(id: String): Result<HealthRecord> {
        return try {
            unwrap { api.getRecordById(id) }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun confirmRecord(id: String, diagnosis: String?): Result<HealthRecord> {
        return try {
            unwrap { api.confirmRecord(id, com.yikangbao.app.data.api.ConfirmRecordRequest(diagnosis = diagnosis)) }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRecord(id: String): Result<Unit> {
        return try {
            unwrap { api.deleteRecord(id) }.onSuccess {
                _recordsFlow.value = _recordsFlow.value.filter { it.id != id }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeRecords(): Flow<List<HealthRecord>> = _recordsFlow
}

/** 登录态会话（M1 演示：token/userId 进程内共享；M4 换 DataStore 持久化）。 */
object AuthSession {
    var token: String? = null
    var userId: String? = null
}
