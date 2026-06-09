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
            val response = api.createReminder(request)
            if (response.isSuccessful && response.body() != null) {
                val created = response.body()!!
                _remindersFlow.value = _remindersFlow.value + created
                Result.success(created)
            } else {
                Result.failure(Exception("Failed to create reminder"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReminders(): Result<List<Reminder>> {
        return try {
            val response = api.getReminders()
            if (response.isSuccessful && response.body() != null) {
                val reminders = response.body()!!
                _remindersFlow.value = reminders
                Result.success(reminders)
            } else {
                Result.failure(Exception("Failed to get reminders"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateReminder(reminder: Reminder): Result<Unit> {
        return try {
            val response = api.updateReminder(reminder.id, reminder)
            if (response.isSuccessful) {
                _remindersFlow.value = _remindersFlow.value.map {
                    if (it.id == reminder.id) reminder else it
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update reminder"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteReminder(id: String): Result<Unit> {
        return try {
            val response = api.deleteReminder(id)
            if (response.isSuccessful) {
                _remindersFlow.value = _remindersFlow.value.filter { it.id != id }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete reminder"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeReminder(id: String): Result<Unit> {
        return try {
            val response = api.completeReminder(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to complete reminder"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeReminders(): Flow<List<Reminder>> = _remindersFlow
}
