package com.yikangbao.app.domain.repository

import com.yikangbao.app.data.model.HealthRecord
import kotlinx.coroutines.flow.Flow

interface HealthRecordRepository {
    /** 上传病历照片（本地文件路径列表），后端 OCR+LLM 解析后返回结构化记录。 */
    suspend fun uploadRecord(imagePaths: List<String>): Result<HealthRecord>
    suspend fun getRecords(page: Int, pageSize: Int): Result<List<HealthRecord>>
    suspend fun getRecordById(id: String): Result<HealthRecord>
    /** 用户确认/修正诊断；diagnosis 传 null 表示仅确认。 */
    suspend fun confirmRecord(id: String, diagnosis: String?): Result<HealthRecord>
    suspend fun deleteRecord(id: String): Result<Unit>
    fun observeRecords(): Flow<List<HealthRecord>>
}
