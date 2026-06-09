package com.yikangbao.app.voice

import com.yikangbao.app.voice.VoiceCommand.*

/**
 * 语音指令类型
 */
sealed class VoiceCommand {
    // 健康指标记录
    data class RecordBloodPressure(
        val systolic: Int,      // 收缩压
        val diastolic: Int      // 舒张压
    ) : VoiceCommand()
    
    data class RecordBloodSugar(
        val value: Float,
        val type: String = "fasting"  // fasting, postprandial, random
    ) : VoiceCommand()
    
    data class RecordHeartRate(val value: Int) : VoiceCommand()
    data class RecordWeight(val value: Float) : VoiceCommand()
    
    // 提醒相关
    data class SetMedicationReminder(
        val medicationName: String,
        val time: String
    ) : VoiceCommand()
    
    data class SetReminder(
        val content: String,
        val time: String
    ) : VoiceCommand()
    
    // 查询相关
    data class QueryHealthReport(val period: String = "week") : VoiceCommand()
    data class QueryRecords(val type: String? = null) : VoiceCommand()
    data class QueryTrend(val indicatorType: String) : VoiceCommand()
    
    // 医疗服务
    data class BookAppointment(
        val department: String,
        val hospital: String? = null
    ) : VoiceCommand()
    
    // 上传病历
    object UploadRecord : VoiceCommand()
    
    // AI对话
    data class ChatQuestion(val question: String) : VoiceCommand()
    
    // 通用帮助
    object Help : VoiceCommand()
    object Unknown : VoiceCommand()
}

/**
 * 语音指令解析结果
 */
data class ParsedCommand(
    val command: VoiceCommand,
    val confidence: Float = 1.0f,
    val originalText: String
)

/**
 * 语音指令解析器
 * 将用户的语音文本解析为结构化的指令
 */
class VoiceCommandParser {

    /**
     * 解析语音文本为指令
     */
    fun parse(text: String): ParsedCommand {
        val normalizedText = text.trim().lowercase()
        
        return when {
            // 记录血压: "帮我记录血压 80 120" 或 "血压80 120"
            normalizedText.contains("血压") -> parseBloodPressure(normalizedText, text)
            
            // 记录血糖: "记录血糖 5.6" 或 "血糖5.6"
            normalizedText.contains("血糖") -> parseBloodSugar(normalizedText, text)
            
            // 记录心率: "心率72" 或 "记录心率72"
            normalizedText.contains("心率") -> parseHeartRate(normalizedText, text)
            
            // 记录体重: "体重65公斤" 或 "记录体重65"
            normalizedText.contains("体重") -> parseWeight(normalizedText, text)
            
            // 用药提醒: "提醒我吃降压药" 或 "明天早上提醒我吃药"
            normalizedText.contains("提醒") && normalizedText.contains("药") -> 
                parseMedicationReminder(normalizedText, text)
            
            // 设置提醒: "提醒我明天测血糖"
            normalizedText.contains("提醒") -> parseReminder(normalizedText, text)
            
            // 查询健康报告: "健康报告" "这周健康报告"
            normalizedText.contains("健康报告") || normalizedText.contains("健康周报") -> 
                ParsedCommand(QueryHealthReport("week"), 1.0f, text)
            
            // 查询档案: "查看病历" "体检报告"
            normalizedText.contains("病历") || normalizedText.contains("档案") || normalizedText.contains("体检") ->
                parseRecordsQuery(normalizedText, text)
            
            // 查询趋势: "血压趋势" "血糖变化"
            normalizedText.contains("趋势") || normalizedText.contains("变化") ->
                parseTrendQuery(normalizedText, text)
            
            // 预约挂号: "挂个心内科的号" "预约挂号"
            normalizedText.contains("挂号") || normalizedText.contains("预约") ->
                parseAppointment(normalizedText, text)
            
            // 上传病历: "上传病历" "拍照上传"
            normalizedText.contains("上传") && (normalizedText.contains("病历") || normalizedText.contains("报告")) ->
                ParsedCommand(UploadRecord, 1.0f, text)
            
            // 帮助: "帮帮我" "怎么用"
            normalizedText.contains("帮帮我") || normalizedText.contains("怎么用") || normalizedText.contains("帮助") ->
                ParsedCommand(Help, 1.0f, text)
            
            // 其他情况作为AI对话问题
            else -> ParsedCommand(ChatQuestion(text), 0.7f, text)
        }
    }

    private fun parseBloodPressure(normalized: String, original: String): ParsedCommand {
        // 提取数字，格式可能是 "血压80 120" 或 "血压80/120" 或 "收缩压120舒张压80"
        val numbers = Regex("\\d+").findAll(normalized).map { it.value.toInt() }.toList()
        
        return if (numbers.size >= 2) {
            // 假设第一个是收缩压（较大的数），第二个是舒张压
            val (systolic, diastolic) = if (numbers[0] > numbers[1]) {
                numbers[0] to numbers[1]
            } else {
                numbers[1] to numbers[0]
            }
            ParsedCommand(RecordBloodPressure(systolic, diastolic), 1.0f, original)
        } else {
            ParsedCommand(ChatQuestion(original), 0.5f, original)
        }
    }

    private fun parseBloodSugar(normalized: String, original: String): ParsedCommand {
        val numbers = Regex("[\\d.]+").findAll(normalized).map { it.value.toFloat() }.toList()
        
        val type = when {
            normalized.contains("空腹") -> "fasting"
            normalized.contains("餐后") -> "postprandial"
            else -> "fasting"
        }
        
        return if (numbers.isNotEmpty()) {
            ParsedCommand(RecordBloodSugar(numbers[0], type), 1.0f, original)
        } else {
            ParsedCommand(ChatQuestion(original), 0.5f, original)
        }
    }

    private fun parseHeartRate(normalized: String, original: String): ParsedCommand {
        val numbers = Regex("\\d+").findAll(normalized).map { it.value.toInt() }.toList()
        
        return if (numbers.isNotEmpty()) {
            ParsedCommand(RecordHeartRate(numbers[0]), 1.0f, original)
        } else {
            ParsedCommand(ChatQuestion(original), 0.5f, original)
        }
    }

    private fun parseWeight(normalized: String, original: String): ParsedCommand {
        val numbers = Regex("[\\d.]+").findAll(normalized).map { it.value.toFloat() }.toList()
        
        return if (numbers.isNotEmpty()) {
            ParsedCommand(RecordWeight(numbers[0]), 1.0f, original)
        } else {
            ParsedCommand(ChatQuestion(original), 0.5f, original)
        }
    }

    private fun parseMedicationReminder(normalized: String, original: String): ParsedCommand {
        // 提取药品名称和时间
        val medicationName = when {
            normalized.contains("降压药") -> "降压药"
            normalized.contains("降糖药") -> "降糖药"
            normalized.contains("止痛药") -> "止痛药"
            else -> "药"
        }
        
        val time = when {
            normalized.contains("早上") || normalized.contains("早晨") -> "08:00"
            normalized.contains("中午") -> "12:00"
            normalized.contains("晚上") || normalized.contains("睡前") -> "20:00"
            normalized.contains("饭前") -> "饭前"
            normalized.contains("饭后") -> "饭后"
            else -> "08:00"
        }
        
        return ParsedCommand(SetMedicationReminder(medicationName, time), 0.9f, original)
    }

    private fun parseReminder(normalized: String, original: String): ParsedCommand {
        // 提取提醒内容和时间
        val content = when {
            normalized.contains("测血糖") -> "测血糖"
            normalized.contains("测血压") -> "测血压"
            normalized.contains("吃药") -> "吃药"
            normalized.contains("体检") -> "体检"
            else -> "提醒事项"
        }
        
        val time = when {
            normalized.contains("明天") -> "tomorrow"
            normalized.contains("后天") -> "day_after_tomorrow"
            normalized.contains("下周") -> "next_week"
            else -> "today"
        }
        
        return ParsedCommand(SetReminder(content, time), 0.85f, original)
    }

    private fun parseRecordsQuery(normalized: String, original: String): ParsedCommand {
        val type = when {
            normalized.contains("体检") -> "checkup"
            normalized.contains("门诊") -> "outpatient"
            normalized.contains("住院") -> "inpatient"
            else -> null
        }
        return ParsedCommand(QueryRecords(type), 1.0f, original)
    }

    private fun parseTrendQuery(normalized: String, original: String): ParsedCommand {
        val indicatorType = when {
            normalized.contains("血压") -> "blood_pressure"
            normalized.contains("血糖") -> "blood_sugar"
            normalized.contains("心率") -> "heart_rate"
            normalized.contains("体重") -> "weight"
            else -> "all"
        }
        return ParsedCommand(QueryTrend(indicatorType), 1.0f, original)
    }

    private fun parseAppointment(normalized: String, original: String): ParsedCommand {
        val department = when {
            normalized.contains("心内") -> "心内科"
            normalized.contains("心外") -> "心外科"
            normalized.contains("内分泌") -> "内分泌科"
            normalized.contains("神经") -> "神经内科"
            normalized.contains("消化") -> "消化内科"
            normalized.contains("呼吸") -> "呼吸内科"
            normalized.contains("骨科") -> "骨科"
            normalized.contains("眼科") -> "眼科"
            normalized.contains("耳鼻喉") -> "耳鼻喉科"
            normalized.contains("皮肤") -> "皮肤科"
            else -> "全科"
        }
        return ParsedCommand(BookAppointment(department), 1.0f, original)
    }
}
