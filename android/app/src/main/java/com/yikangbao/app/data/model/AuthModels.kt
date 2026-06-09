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

data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

data class LoginRequest(
    val phone: String,
    val code: String
)

data class SendCodeRequest(
    val phone: String
)
