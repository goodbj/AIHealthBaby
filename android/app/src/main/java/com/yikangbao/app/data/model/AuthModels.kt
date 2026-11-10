package com.yikangbao.app.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: String,
    val phone: String,
    val name: String?,
    val avatarUrl: String?,
    val gender: String?,
    val birthDate: String?,
    val healthProfile: HealthProfile?
)

data class HealthProfile(
    val bloodType: String?,
    val allergies: List<String>?,
    val chronicDiseases: List<String>?,
    val medications: List<String>?
)

/** 登录响应数据（后端 /api/auth/login：token+userId，M1 演示契约）。 */
data class AuthToken(
    val token: String,
    val userId: String,
    val phone: String,
    val name: String?
)

data class LoginRequest(
    val phone: String,
    val code: String
)

data class SendCodeRequest(
    val phone: String
)
