package com.yikangbao.app.domain.repository

import com.yikangbao.app.data.model.HealthIndicator
import com.yikangbao.app.data.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    suspend fun createReminder(reminder: Reminder): Result<Reminder>
    suspend fun getReminders(): Result<List<Reminder>>
    suspend fun updateReminder(reminder: Reminder): Result<Unit>
    suspend fun deleteReminder(id: String): Result<Unit>
    suspend fun completeReminder(id: String): Result<Unit>
    fun observeReminders(): Flow<List<Reminder>>
}

interface IndicatorRepository {
    suspend fun addIndicator(indicator: HealthIndicator): Result<HealthIndicator>
    suspend fun getIndicators(type: String?, startDate: Long?, endDate: Long?): Result<List<HealthIndicator>>
    fun observeIndicators(): Flow<List<HealthIndicator>>
}
