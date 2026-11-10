package com.yikangbao.app.data.model

import com.google.gson.annotations.SerializedName

/** 健康记录（字段名对齐后端 /api/records 视图，camelCase 直映射）。 */
data class HealthRecord(
    val id: String,
    val userId: String,
    val recordType: String,
    val imageUrls: List<String>,
    val ocrRawText: String?,
    val diagnosis: String?,
    val indicators: List<RecordIndicator>?,
    val medications: List<MedicationItem>?,
    val hospitalName: String?,
    val department: String?,
    val doctorName: String?,
    val recordDate: String,
    val confidence: Float?,
    val status: String,
    val createdAt: String,
    val message: String? = null
)

data class RecordIndicator(
    val name: String,
    val value: String,
    val level: String?
)

data class MedicationItem(
    val name: String,
    val dosage: String?,
    val frequency: String?
)

data class HealthIndicator(
    val id: String,
    val userId: String,
    val indicatorType: String,
    val value: Float,
    val unit: String,
    val level: String?,
    val measuredAt: String,
    val source: String?,
    val notes: String?
)
