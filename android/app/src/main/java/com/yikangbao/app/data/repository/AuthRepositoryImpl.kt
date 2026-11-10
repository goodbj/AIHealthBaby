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
    private val userIdKey = stringPreferencesKey("user_id")

    override suspend fun loginWithPhone(phone: String, code: String): Result<AuthToken> {
        return try {
            val result = unwrap { api.login(LoginRequest(phone, code)) }
            result.map { token ->
                saveToken(token)
                token
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendSmsCode(phone: String): Result<Unit> {
        // 演示模式：后端固定验证码（YKB_DEMO_SMS_CODE），无需真实下发
        return Result.success(Unit)
    }

    override suspend fun getCurrentUser(): Result<User> {
        return try {
            unwrap { api.getProfile() }
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
            preferences.remove(userIdKey)
        }
        AuthSession.token = null
        AuthSession.userId = null
    }

    private suspend fun saveToken(token: AuthToken) {
        AuthSession.token = token.token
        AuthSession.userId = token.userId
        context.dataStore.edit { preferences ->
            preferences[accessTokenKey] = token.token
            preferences[userIdKey] = token.userId
        }
    }

    /** 冷启动恢复登录态（MainActivity 先调这个再进 UI）。 */
    suspend fun restoreSession() {
        val prefs = context.dataStore.data.first()
        AuthSession.token = prefs[accessTokenKey]
        AuthSession.userId = prefs[userIdKey]
    }
}
