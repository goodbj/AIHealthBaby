package com.yikangbao.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikangbao.app.data.model.User
import com.yikangbao.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    /** M1 演示登录状态：未登录显示一键登录入口。 */
    val isLoggedIn: Boolean = false,
    val loginMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        refreshLoginState()
        loadProfile()
    }

    fun refreshLoginState() {
        viewModelScope.launch {
            val loggedIn = authRepository.isLoggedIn().first()
            _uiState.update { it.copy(isLoggedIn = loggedIn) }
        }
    }

    /**
     * M1 演示一键登录：任意手机号 + 固定验证码（后端 YKB_DEMO_SMS_CODE）。
     * 未填手机号时自动生成演示号，老人免输入。
     */
    fun demoLogin(phone: String? = null) {
        viewModelScope.launch {
            val p: String = phone ?: "138${System.currentTimeMillis().toString().takeLast(8)}"
            authRepository.sendSmsCode(p)
            authRepository.loginWithPhone(p, "123456")
                .onSuccess { _ ->
                    _uiState.update { it.copy(isLoggedIn = true, loginMessage = "登录成功", error = null) }
                    loadProfile()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(loginMessage = null, error = e.message) }
                }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            authRepository.getCurrentUser()
                .onSuccess { user ->
                    _uiState.update { it.copy(isLoading = false, user = user) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(user = null, isLoggedIn = false) }
        }
    }
}
