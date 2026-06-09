package com.yikangbao.app.voice

import com.yikangbao.app.voice.VoiceCommand.*

/**
 * 语音指令执行结果
 */
sealed class CommandResult {
    object Success : CommandResult()
    data class Message(val text: String, val shouldSpeak: Boolean = true) : CommandResult()
    data class Navigate(val destination: String) : CommandResult()
    data class Error(val message: String) : CommandResult()
    data class Confirm(val prompt: String, val command: VoiceCommand) : CommandResult()
}

/**
 * 语音指令执行器
 * 负责执行解析后的语音指令
 */
class VoiceCommandExecutor {

    /**
     * 执行语音指令
     * @param command 解析后的语音指令
     * @return 执行结果
     */
    suspend fun execute(command: VoiceCommand): CommandResult {
        return when (command) {
            is RecordBloodPressure -> executeRecordBloodPressure(command)
            is RecordBloodSugar -> executeRecordBloodSugar(command)
            is RecordHeartRate -> executeRecordHeartRate(command)
            is RecordWeight -> executeRecordWeight(command)
            is SetMedicationReminder -> executeSetMedicationReminder(command)
            is SetReminder -> executeSetReminder(command)
            is QueryHealthReport -> executeQueryHealthReport(command)
            is QueryRecords -> executeQueryRecords(command)
            is QueryTrend -> executeQueryTrend(command)
            is BookAppointment -> executeBookAppointment(command)
            is UploadRecord -> executeUploadRecord()
            is Help -> executeHelp()
            is ChatQuestion -> executeChatQuestion(command)
            is VoiceCommand.Unknown -> CommandResult.Message("抱歉，我没有理解您的意思，请再说一次")
        }
    }

    private suspend fun executeRecordBloodPressure(cmd: RecordBloodPressure): CommandResult {
        // TODO: 调用实际的 Repository 保存数据
        return CommandResult.Message(
            "已记录血压：收缩压 ${cmd.systolic} mmHg，舒张压 ${cmd.diastolic} mmHg。" +
            getBloodPressureAdvice(cmd.systolic, cmd.diastolic),
            shouldSpeak = true
        )
    }

    private fun getBloodPressureAdvice(systolic: Int, diastolic: Int): String {
        return when {
            systolic >= 180 || diastolic >= 110 -> "血压偏高，建议立即就医。"
            systolic >= 140 || diastolic >= 90 -> "血压略高，请注意休息，按时服药。"
            systolic < 90 || diastolic < 60 -> "血压偏低，请注意营养，避免突然站立。"
            else -> "血压正常，请继续保持健康的生活方式。"
        }
    }

    private suspend fun executeRecordBloodSugar(cmd: RecordBloodSugar): CommandResult {
        val typeText = when (cmd.type) {
            "fasting" -> "空腹血糖"
            "postprandial" -> "餐后血糖"
            else -> "血糖"
        }
        
        return CommandResult.Message(
            "已记录$typeText：${cmd.value} mmol/L。" +
            getBloodSugarAdvice(cmd.value, cmd.type),
            shouldSpeak = true
        )
    }

    private fun getBloodSugarAdvice(value: Float, type: String): String {
        val (high, low) = if (type == "fasting") 7.0f to 3.9f else 11.1f to 3.9f
        
        return when {
            value >= high -> "血糖偏高，建议控制饮食，适当运动，按时服药。"
            value < low -> "血糖偏低，请及时补充糖分，注意规律进餐。"
            else -> "血糖正常，请继续保持。"
        }
    }

    private suspend fun executeRecordHeartRate(cmd: RecordHeartRate): CommandResult {
        return CommandResult.Message(
            "已记录心率：${cmd.value} 次/分。" +
            if (cmd.value in 60..100) "心率正常。" 
            else if (cmd.value > 100) "心率偏快，请注意休息。"
            else "心率偏慢，如有不适请就医检查。",
            shouldSpeak = true
        )
    }

    private suspend fun executeRecordWeight(cmd: RecordWeight): CommandResult {
        return CommandResult.Message(
            "已记录体重：${cmd.value} 公斤。",
            shouldSpeak = true
        )
    }

    private suspend fun executeSetMedicationReminder(cmd: SetMedicationReminder): CommandResult {
        // TODO: 调用实际的 ReminderRepository 创建提醒
        return CommandResult.Message(
            "已设置提醒：${cmd.time} 服用${cmd.medicationName}。",
            shouldSpeak = true
        )
    }

    private suspend fun executeSetReminder(cmd: SetReminder): CommandResult {
        // TODO: 调用实际的 ReminderRepository 创建提醒
        val timeText = when (cmd.time) {
            "tomorrow" -> "明天"
            "day_after_tomorrow" -> "后天"
            "next_week" -> "下周"
            else -> "今天"
        }
        return CommandResult.Message(
            "已设置提醒：$timeText ${cmd.content}。",
            shouldSpeak = true
        )
    }

    private suspend fun executeQueryHealthReport(cmd: QueryHealthReport): CommandResult {
        // TODO: 调用实际的 Repository 获取数据
        return CommandResult.Navigate("health_report")
    }

    private suspend fun executeQueryRecords(cmd: QueryRecords): CommandResult {
        return CommandResult.Navigate("records")
    }

    private suspend fun executeQueryTrend(cmd: QueryTrend): CommandResult {
        return CommandResult.Navigate("trend/${cmd.indicatorType}")
    }

    private suspend fun executeBookAppointment(cmd: BookAppointment): CommandResult {
        return CommandResult.Message(
            "正在为您查找${cmd.department}的号源...",
            shouldSpeak = true
        )
        // TODO: 实际调用挂号API
    }

    private suspend fun executeUploadRecord(): CommandResult {
        return CommandResult.Navigate("upload_record")
    }

    private suspend fun executeHelp(): CommandResult {
        return CommandResult.Message(
            "您可以这样说：\n" +
            "• \"帮我记录血压，80，120\"\n" +
            "• \"记录血糖 5.6\"\n" +
            "• \"明早提醒我测血糖\"\n" +
            "• \"查看健康报告\"\n" +
            "• \"挂个心内科的号\"\n" +
            "或者直接问我任何健康问题。",
            shouldSpeak = false
        )
    }

    private suspend fun executeChatQuestion(cmd: ChatQuestion): CommandResult {
        // 返回导航到聊天界面，并带上问题
        return CommandResult.Navigate("chat?question=${java.net.URLEncoder.encode(cmd.question, "UTF-8")}")
    }
}
