package com.yikangbao.app.domain.repository

import com.yikangbao.app.data.model.AuthToken
import com.yikangbao.app.data.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun loginWithPhone(phone: String, code: String): Result<AuthToken>
    suspend fun sendSmsCode(phone: String): Result<Unit>
    suspend fun getCurrentUser(): Result<User>
    fun isLoggedIn(): Flow<Boolean>
    suspend fun logout()
}
