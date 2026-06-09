package com.yikangbao.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikangbao.app.data.model.HealthIndicator
import com.yikangbao.app.domain.repository.AuthRepository
import com.yikangbao.app.domain.repository.HealthRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val todayIndicators: List<HealthIndicator> = emptyList(),
    val recentRecordsCount: Int = 0,
    val pendingRemindersCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val healthRecordRepository: HealthRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            authRepository.getCurrentUser()
                .onSuccess { user ->
                    _uiState.update { it.copy(userName = user.name ?: "用户") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }

            healthRecordRepository.getRecords(1, 5)
                .onSuccess { records ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            recentRecordsCount = records.size
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
