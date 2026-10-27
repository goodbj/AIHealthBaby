package com.yikangbao.app.voice

import android.util.Log
import com.yikangbao.app.voice.VoiceCommand.*

private const val TAG = "VoiceCommandExecutor"

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
        Log.d(TAG, "===== 开始执行语音指令 =====")
        Log.d(TAG, "指令类型: ${command.javaClass.simpleName}")
        Log.d(TAG, "指令详情: $command")
        
        val result = when (command) {
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
            is ShareRecord -> executeShareRecord(command)
            is Help -> executeHelp()
            is ChatQuestion -> executeChatQuestion(command)
            is VoiceCommand.Unknown -> executeUnknown()
        }
        
        Log.d(TAG, "指令执行完成，结果: ${result.javaClass.simpleName}")
        if (result is CommandResult.Message) {
            Log.d(TAG, "返回消息: ${result.text}")
        } else if (result is CommandResult.Navigate) {
            Log.d(TAG, "导航目标: ${result.destination}")
        } else if (result is CommandResult.Error) {
            Log.e(TAG, "执行错误: ${result.message}")
        }
        Log.d(TAG, "===== 语音指令执行结束 =====")
        
        return result
    }

    private suspend fun executeRecordBloodPressure(cmd: RecordBloodPressure): CommandResult {
        Log.d(TAG, "[血压记录] 收缩压: ${cmd.systolic} mmHg, 舒张压: ${cmd.diastolic} mmHg")
        
        val advice = getBloodPressureAdvice(cmd.systolic, cmd.diastolic)
        Log.d(TAG, "[血压记录] 健康建议: $advice")
        
        return CommandResult.Message(
            "已记录血压：收缩压 ${cmd.systolic} mmHg，舒张压 ${cmd.diastolic} mmHg。$advice",
            shouldSpeak = true
        )
    }

    private fun getBloodPressureAdvice(systolic: Int, diastolic: Int): String {
        return when {
            systolic >= 180 || diastolic >= 110 -> {
                Log.w(TAG, "[健康警告] 血压严重偏高，建议立即就医")
                "血压偏高，建议立即就医。"
            }
            systolic >= 140 || diastolic >= 90 -> {
                Log.w(TAG, "[健康警告] 血压略高")
                "血压略高，请注意休息，按时服药。"
            }
            systolic < 90 || diastolic < 60 -> {
                Log.w(TAG, "[健康警告] 血压偏低")
                "血压偏低，请注意营养，避免突然站立。"
            }
            else -> {
                Log.i(TAG, "[健康状态] 血压正常")
                "血压正常，请继续保持健康的生活方式。"
            }
        }
    }

    private suspend fun executeRecordBloodSugar(cmd: RecordBloodSugar): CommandResult {
        val typeText = when (cmd.type) {
            "fasting" -> "空腹血糖"
            "postprandial" -> "餐后血糖"
            else -> "血糖"
        }
        Log.d(TAG, "[血糖记录] 类型: $typeText, 值: ${cmd.value} mmol/L")
        
        val advice = getBloodSugarAdvice(cmd.value, cmd.type)
        Log.d(TAG, "[血糖记录] 健康建议: $advice")
        
        return CommandResult.Message(
            "已记录$typeText：${cmd.value} mmol/L。$advice",
            shouldSpeak = true
        )
    }

    private fun getBloodSugarAdvice(value: Float, type: String): String {
        val (high, low) = if (type == "fasting") 7.0f to 3.9f else 11.1f to 3.9f
        
        return when {
            value >= high -> {
                Log.w(TAG, "[健康警告] 血糖偏高")
                "血糖偏高，建议控制饮食，适当运动，按时服药。"
            }
            value < low -> {
                Log.w(TAG, "[健康警告] 血糖偏低")
                "血糖偏低，请及时补充糖分，注意规律进餐。"
            }
            else -> {
                Log.i(TAG, "[健康状态] 血糖正常")
                "血糖正常，请继续保持。"
            }
        }
    }

    private suspend fun executeRecordHeartRate(cmd: RecordHeartRate): CommandResult {
        Log.d(TAG, "[心率记录] 值: ${cmd.value} 次/分")
        
        val advice = when {
            cmd.value in 60..100 -> {
                Log.i(TAG, "[健康状态] 心率正常")
                "心率正常。"
            }
            cmd.value > 100 -> {
                Log.w(TAG, "[健康警告] 心率偏快")
                "心率偏快，请注意休息。"
            }
            else -> {
                Log.w(TAG, "[健康警告] 心率偏慢")
                "心率偏慢，如有不适请就医检查。"
            }
        }
        
        return CommandResult.Message(
            "已记录心率：${cmd.value} 次/分。$advice",
            shouldSpeak = true
        )
    }

    private suspend fun executeRecordWeight(cmd: RecordWeight): CommandResult {
        Log.d(TAG, "[体重记录] 值: ${cmd.value} 公斤")
        return CommandResult.Message(
            "已记录体重：${cmd.value} 公斤。",
            shouldSpeak = true
        )
    }

    private suspend fun executeSetMedicationReminder(cmd: SetMedicationReminder): CommandResult {
        Log.d(TAG, "[用药提醒] 药物: ${cmd.medicationName}, 时间: ${cmd.time}")
        return CommandResult.Message(
            "已设置提醒：${cmd.time} 服用${cmd.medicationName}。",
            shouldSpeak = true
        )
    }

    private suspend fun executeSetReminder(cmd: SetReminder): CommandResult {
        val timeText = when (cmd.time) {
            "tomorrow" -> "明天"
            "day_after_tomorrow" -> "后天"
            "next_week" -> "下周"
            else -> "今天"
        }
        Log.d(TAG, "[通用提醒] 时间: $timeText, 内容: ${cmd.content}")
        return CommandResult.Message(
            "已设置提醒：$timeText ${cmd.content}。",
            shouldSpeak = true
        )
    }

    private suspend fun executeQueryHealthReport(cmd: QueryHealthReport): CommandResult {
        Log.d(TAG, "[查询报告] 周期: ${cmd.period}")
        return CommandResult.Navigate("health_report")
    }

    private suspend fun executeQueryRecords(cmd: QueryRecords): CommandResult {
        Log.d(TAG, "[查询记录] 类型: ${cmd.type ?: "全部"}")
        return CommandResult.Navigate("records")
    }

    private suspend fun executeQueryTrend(cmd: QueryTrend): CommandResult {
        Log.d(TAG, "[查询趋势] 指标类型: ${cmd.indicatorType}")
        return CommandResult.Navigate("trend/${cmd.indicatorType}")
    }

    private suspend fun executeBookAppointment(cmd: BookAppointment): CommandResult {
        Log.d(TAG, "[预约挂号] 科室: ${cmd.department}, 医院: ${cmd.hospital ?: "默认"}")
        return CommandResult.Message(
            "正在为您查找${cmd.department}的号源...",
            shouldSpeak = true
        )
    }

    private suspend fun executeUploadRecord(): CommandResult {
        Log.d(TAG, "[上传病历] 导航到上传页面")
        return CommandResult.Navigate("upload_record")
    }

    private suspend fun executeShareRecord(cmd: ShareRecord): CommandResult {
        Log.d(TAG, "[分享功能] 目标类型: ${cmd.targetType}, 目标ID: ${cmd.targetId}")
        Log.d(TAG, "[分享功能] 权限级别: ${cmd.permissionLevel}, 有效期: ${cmd.expireDays}天")
        
        val shareUrl = MockShareService.createShareLink(cmd)
        Log.d(TAG, "[分享功能] 生成分享链接: $shareUrl")
        
        return CommandResult.Message(
            "分享链接已生成：$shareUrl\n有效期${cmd.expireDays}天",
            shouldSpeak = false
        )
    }

    private suspend fun executeHelp(): CommandResult {
        Log.d(TAG, "[帮助中心] 用户请求帮助")
        return CommandResult.Message(
            "您可以这样说：\n" +
            "• \"帮我记录血压，80，120\"\n" +
            "• \"记录血糖 5.6\"\n" +
            "• \"明早提醒我测血糖\"\n" +
            "• \"查看健康报告\"\n" +
            "• \"挂个心内科的号\"\n" +
            "• \"分享我的健康报告\"\n" +
            "或者直接问我任何健康问题。",
            shouldSpeak = false
        )
    }

    private suspend fun executeChatQuestion(cmd: ChatQuestion): CommandResult {
        Log.d(TAG, "[AI对话] 用户问题: ${cmd.question}")
        return CommandResult.Navigate("chat?question=${java.net.URLEncoder.encode(cmd.question, "UTF-8")}")
    }

    private suspend fun executeUnknown(): CommandResult {
        Log.w(TAG, "[未知指令] 无法识别用户语音")
        return CommandResult.Message("抱歉，我没有理解您的意思，请再说一次")
    }
}

/**
 * Mock分享服务
 * 模拟分享链接生成和验证逻辑
 */
object MockShareService {
    private const val TAG = "MockShareService"
    private val shareRecords = mutableMapOf<String, ShareData>()
    
    data class ShareData(
        val userId: String,
        val targetType: String,
        val targetId: String,
        val permissionLevel: String,
        val expireAt: Long,
        var viewCount: Int = 0,
        var convertedUserId: String? = null
    )

    /**
     * 创建分享链接
     */
    fun createShareLink(cmd: VoiceCommand.ShareRecord): String {
        Log.d(TAG, "创建分享链接 - 用户ID: ${cmd.userId}, 目标: ${cmd.targetType}/${cmd.targetId}")
        
        val shareCode = generateShareCode()
        val expireAt = System.currentTimeMillis() + (cmd.expireDays * 24 * 60 * 60 * 1000L)
        
        shareRecords[shareCode] = ShareData(
            userId = cmd.userId,
            targetType = cmd.targetType,
            targetId = cmd.targetId,
            permissionLevel = cmd.permissionLevel,
            expireAt = expireAt
        )
        
        Log.d(TAG, "分享链接创建成功 - 分享码: $shareCode, 过期时间: ${java.util.Date(expireAt)}")
        return "https://yikangbao.com/share/$shareCode"
    }

    /**
     * 通过分享码获取分享内容
     */
    fun getShareContent(shareCode: String): ShareData? {
        Log.d(TAG, "查询分享内容 - 分享码: $shareCode")
        
        val shareData = shareRecords[shareCode] ?: run {
            Log.e(TAG, "分享码不存在: $shareCode")
            return null
        }
        
        if (System.currentTimeMillis() > shareData.expireAt) {
            Log.w(TAG, "分享链接已过期: $shareCode")
            shareRecords.remove(shareCode)
            return null
        }
        
        shareData.viewCount++
        Log.d(TAG, "分享内容查询成功 - 查看次数: ${shareData.viewCount}")
        return shareData
    }

    /**
     * 生成随机分享码
     */
    private fun generateShareCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..8).map { chars.random() }.joinToString("")
    }

    /**
     * 获取用户分享统计
     */
    fun getUserShareStats(userId: String): Map<String, Int> {
        val userShares = shareRecords.values.filter { it.userId == userId }
        return mapOf(
            "total_shares" to userShares.size,
            "total_views" to userShares.sumOf { it.viewCount },
            "active_shares" to userShares.count { System.currentTimeMillis() <= it.expireAt }
        )
    }
}
