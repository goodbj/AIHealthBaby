package com.yikangbao.app.ui.screens.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikangbao.app.data.model.HealthRecord
import com.yikangbao.app.domain.repository.HealthRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecordDetailUiState(
    val isLoading: Boolean = false,
    val record: HealthRecord? = null,
    val error: String? = null
)

@HiltViewModel
class RecordDetailViewModel @Inject constructor(
    private val healthRecordRepository: HealthRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordDetailUiState())
    val uiState: StateFlow<RecordDetailUiState> = _uiState.asStateFlow()

    fun loadRecord(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            healthRecordRepository.getRecordById(id)
                .onSuccess { record ->
                    _uiState.update { it.copy(isLoading = false, record = record) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
