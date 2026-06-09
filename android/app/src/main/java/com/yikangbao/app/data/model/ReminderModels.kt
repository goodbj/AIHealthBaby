package com.yikangbao.app.data.model

import com.google.gson.annotations.SerializedName

data class Reminder(
    val id: String,
    val userId: String,
    val reminderType: String,
    val title: String,
    val content: String?,
    val scheduledTime: String,
    val repeatRule: RepeatRule?,
    val notifyMethods: List<String>,
    val priority: String,
    val status: String,
    val completedAt: String?,
    val createdAt: String
)

data class RepeatRule(
    val type: String,
    val interval: Int?,
    val daysOfWeek: List<Int>?
)

data class CreateReminderRequest(
    val reminderType: String,
    val title: String,
    val content: String?,
    val scheduledTime: String,
    val repeatRule: RepeatRule?,
    val notifyMethods: List<String>,
    val priority: String
)
