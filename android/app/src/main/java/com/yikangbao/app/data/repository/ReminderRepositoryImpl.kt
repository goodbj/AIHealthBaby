package com.yikangbao.app.data.repository

import com.yikangbao.app.data.api.YiKangBaoApi
import com.yikangbao.app.data.model.CreateReminderRequest
import com.yikangbao.app.data.model.Reminder
import com.yikangbao.app.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val api: YiKangBaoApi
) : ReminderRepository {

    private val _remindersFlow = MutableStateFlow<List<Reminder>>(emptyList())

    override suspend fun createReminder(reminder: Reminder): Result<Reminder> {
        return try {
            val request = CreateReminderRequest(
                reminderType = reminder.reminderType,
                title = reminder.title,
                content = reminder.content,
                scheduledTime = reminder.scheduledTime,
                repeatRule = reminder.repeatRule,
                notifyMethods = reminder.notifyMethods,
                priority = reminder.priority
            )
            unwrap { api.createReminder(request) }.onSuccess { created ->
                _remindersFlow.value = _remindersFlow.value + created
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReminders(): Result<List<Reminder>> {
        return try {
            val userId = AuthSession.userId ?: return Result.failure(Exception("请先登录"))
            unwrap { api.getReminders(userId) }.onSuccess { reminders ->
                _remindersFlow.value = reminders
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateReminder(reminder: Reminder): Result<Unit> {
        return try {
            unwrap { api.updateReminder(reminder.id, reminder) }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteReminder(id: String): Result<Unit> {
        return try {
            unwrap { api.deleteReminder(id) }.onSuccess {
                _remindersFlow.value = _remindersFlow.value.filter { it.id != id }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeReminder(id: String): Result<Unit> {
        return try {
            unwrap { api.completeReminder(id) }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeReminders(): Flow<List<Reminder>> = _remindersFlow
}
