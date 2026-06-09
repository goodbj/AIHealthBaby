package com.yikangbao.app.data.repository

import com.yikangbao.app.data.api.YiKangBaoApi
import com.yikangbao.app.data.model.HealthRecord
import com.yikangbao.app.data.model.UploadRecordRequest
import com.yikangbao.app.domain.repository.HealthRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRecordRepositoryImpl @Inject constructor(
    private val api: YiKangBaoApi
) : HealthRecordRepository {

    private val _recordsFlow = MutableStateFlow<List<HealthRecord>>(emptyList())

    override suspend fun uploadRecord(imagePaths: List<String>): Result<HealthRecord> {
        return try {
            val response = api.uploadRecord(UploadRecordRequest(imagePaths))
            if (response.isSuccessful && response.body() != null) {
                val record = response.body()!!
                _recordsFlow.value = _recordsFlow.value + record
                Result.success(record)
            } else {
                Result.failure(Exception("Failed to upload record"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecords(page: Int, pageSize: Int): Result<List<HealthRecord>> {
        return try {
            val response = api.getRecords(page, pageSize)
            if (response.isSuccessful && response.body() != null) {
                val records = response.body()!!
                _recordsFlow.value = records
                Result.success(records)
            } else {
                Result.failure(Exception("Failed to get records"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecordById(id: String): Result<HealthRecord> {
        return try {
            val response = api.getRecordById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get record"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun confirmRecord(id: String, parsedData: String): Result<Unit> {
        return try {
            val response = api.confirmRecord(id, mapOf("parsed_data" to parsedData))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to confirm record"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRecord(id: String): Result<Unit> {
        return try {
            val response = api.deleteRecord(id)
            if (response.isSuccessful) {
                _recordsFlow.value = _recordsFlow.value.filter { it.id != id }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete record"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeRecords(): Flow<List<HealthRecord>> = _recordsFlow
}
