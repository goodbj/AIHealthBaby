package com.yikangbao.app.ui.screens.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikangbao.app.data.model.Reminder
import com.yikangbao.app.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderUiState(
    val isLoading: Boolean = false,
    val reminders: List<Reminder> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderUiState())
    val uiState: StateFlow<ReminderUiState> = _uiState.asStateFlow()

    init {
        loadReminders()
        observeReminders()
    }

    private fun observeReminders() {
        viewModelScope.launch {
            reminderRepository.observeReminders().collect { reminders ->
                _uiState.update { it.copy(reminders = reminders) }
            }
        }
    }

    fun loadReminders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            reminderRepository.getReminders()
                .onSuccess { reminders ->
                    _uiState.update {
                        it.copy(isLoading = false, reminders = reminders)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message)
                    }
                }
        }
    }

    fun completeReminder(id: String) {
        viewModelScope.launch {
            reminderRepository.completeReminder(id)
        }
    }

    fun deleteReminder(id: String) {
        viewModelScope.launch {
            reminderRepository.deleteReminder(id)
        }
    }
}
