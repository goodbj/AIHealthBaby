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
    val isUploading: Boolean = false,
    val records: List<HealthRecord> = emptyList(),
    val error: String? = null,
    /** 刚上传成功待确认的记录（M1 确认修正弹层）。 */
    val lastUploaded: HealthRecord? = null
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
            _uiState.update { it.copy(isLoading = true, error = null) }

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
            _uiState.update { it.copy(isUploading = true, error = null) }

            healthRecordRepository.uploadRecord(imagePaths)
                .onSuccess { record ->
                    _uiState.update { it.copy(isUploading = false, lastUploaded = record) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isUploading = false, error = e.message)
                    }
                }
        }
    }

    /** 用户确认修正（M1 闭环最后一步）。 */
    fun confirmRecord(id: String, diagnosis: String?) {
        viewModelScope.launch {
            healthRecordRepository.confirmRecord(id, diagnosis)
                .onSuccess { updated ->
                    _uiState.update { state ->
                        state.copy(
                            records = state.records.map { if (it.id == updated.id) updated else it }
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun consumeLastUploaded() {
        _uiState.update { it.copy(lastUploaded = null) }
    }

    fun deleteRecord(id: String) {
        viewModelScope.launch {
            healthRecordRepository.deleteRecord(id)
        }
    }
}
