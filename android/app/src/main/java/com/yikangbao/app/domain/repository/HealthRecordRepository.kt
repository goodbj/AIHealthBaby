package com.yikangbao.app.domain.repository

import com.yikangbao.app.data.model.HealthRecord
import kotlinx.coroutines.flow.Flow

interface HealthRecordRepository {
    suspend fun uploadRecord(imagePaths: List<String>): Result<HealthRecord>
    suspend fun getRecords(page: Int, pageSize: Int): Result<List<HealthRecord>>
    suspend fun getRecordById(id: String): Result<HealthRecord>
    suspend fun confirmRecord(id: String, parsedData: String): Result<Unit>
    suspend fun deleteRecord(id: String): Result<Unit>
    fun observeRecords(): Flow<List<HealthRecord>>
}
