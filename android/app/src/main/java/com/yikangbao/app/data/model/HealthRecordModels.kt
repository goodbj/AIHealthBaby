package com.yikangbao.app.data.model

import com.google.gson.annotations.SerializedName

data class HealthRecord(
    val id: String,
    val userId: String,
    val recordType: String,
    val imageUrls: List<String>,
    val ocrRawText: String?,
    val parsedData: ParsedRecordData?,
    val diagnosis: String?,
    val indicators: List<HealthIndicator>?,
    val hospitalName: String?,
    val department: String?,
    val doctorName: String?,
    val recordDate: String,
    val confidence: Float?,
    val status: String,
    val createdAt: String
)

data class ParsedRecordData(
    val chiefComplaint: String?,
    val presentIllness: String?,
    val diagnosis: List<String>?,
    val prescriptions: List<Prescription>?,
    val advice: String?
)

data class Prescription(
    val medicationName: String,
    val dosage: String,
    val frequency: String,
    val duration: String
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

data class UploadRecordRequest(
    val images: List<String>
)
