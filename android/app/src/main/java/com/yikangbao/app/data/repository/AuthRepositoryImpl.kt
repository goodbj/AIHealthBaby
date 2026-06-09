package com.yikangbao.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yikangbao.app.data.api.YiKangBaoApi
import com.yikangbao.app.data.model.AuthToken
import com.yikangbao.app.data.model.LoginRequest
import com.yikangbao.app.data.model.SendCodeRequest
import com.yikangbao.app.data.model.User
import com.yikangbao.app.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: YiKangBaoApi
) : AuthRepository {

    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")

    override suspend fun loginWithPhone(phone: String, code: String): Result<AuthToken> {
        return try {
            val response = api.login(LoginRequest(phone, code))
            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!
                saveTokens(token)
                Result.success(token)
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendSmsCode(phone: String): Result<Unit> {
        return try {
            val response = api.sendSmsCode(SendCodeRequest(phone))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to send code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = api.getProfile()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[accessTokenKey] != null
        }
    }

    override suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences.remove(accessTokenKey)
            preferences.remove(refreshTokenKey)
        }
    }

    private suspend fun saveTokens(token: AuthToken) {
        context.dataStore.edit { preferences ->
            preferences[accessTokenKey] = token.accessToken
            preferences[refreshTokenKey] = token.refreshToken
        }
    }

    suspend fun getAccessToken(): String? {
        return context.dataStore.data.first()[accessTokenKey]
    }
}
