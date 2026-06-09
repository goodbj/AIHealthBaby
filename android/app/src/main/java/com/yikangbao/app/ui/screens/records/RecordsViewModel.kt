package com.yikangbao.app.ui.screens.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikangbao.app.data.model.HealthRecord
import com.yikangbao.app.domain.repository.HealthRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecordsUiState(
    val isLoading: Boolean = false,
    val records: List<HealthRecord> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val healthRecordRepository: HealthRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()

    init {
        loadRecords()
        observeRecords()
    }

    private fun observeRecords() {
        viewModelScope.launch {
            healthRecordRepository.observeRecords().collect { records ->
                _uiState.update { it.copy(records = records) }
            }
        }
    }

    fun loadRecords(page: Int = 1, pageSize: Int = 20) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            healthRecordRepository.getRecords(page, pageSize)
                .onSuccess { records ->
                    _uiState.update {
                        it.copy(isLoading = false, records = records)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message)
                    }
                }
        }
    }

    fun uploadRecord(imagePaths: List<String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            healthRecordRepository.uploadRecord(imagePaths)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message)
                    }
                }
        }
    }

    fun deleteRecord(id: String) {
        viewModelScope.launch {
            healthRecordRepository.deleteRecord(id)
        }
    }
}
