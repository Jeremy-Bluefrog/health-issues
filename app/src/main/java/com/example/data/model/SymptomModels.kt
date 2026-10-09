package com.example.data.model

import com.squareup.moshi.JsonClass

enum class TriageLevel(
    val labelZh: String,
    val descriptionZh: String
) {
    EMERGENCY(
        labelZh = "緊急就醫 (急診)",
        descriptionZh = "出現高風險紅旗徵兆，建議立即就近前往急診醫學科或撥打119"
    ),
    PROMPT(
        labelZh = "儘速就醫 (24小時內)",
        descriptionZh = "症狀具有潛在進展性，建議今日或24小時內前往門診專科評估"
    ),
    ROUTINE_CLINIC(
        labelZh = "建議常規門診",
        descriptionZh = "目前狀況尚穩定，建議數日內至適當科別門診安排檢查"
    ),
    SELF_CARE(
        labelZh = "自我照護與密切觀察",
        descriptionZh = "初步研判多屬良性或暫時性不適，可先行居家舒緩，若惡化再就醫"
    )
}

@JsonClass(generateAdapter = true)
data class PossibleCause(
    val name: String,
    val likelihood: String,
    val explanation: String,
    val keySigns: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class FollowUpQuestion(
    val id: String,
    val question: String,
    val purpose: String,
    val options: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DepartmentRecommendation(
    val name: String,
    val reason: String,
    val urgency: String = "常規"
)

@JsonClass(generateAdapter = true)
data class AiAnalysisResult(
    val triageLevel: TriageLevel = TriageLevel.ROUTINE_CLINIC,
    val summary: String,
    val possibleCauses: List<PossibleCause> = emptyList(),
    val followUpQuestions: List<FollowUpQuestion> = emptyList(),
    val recommendedDepartments: List<DepartmentRecommendation> = emptyList(),
    val redFlagWarnings: List<String> = emptyList(),
    val homeCareAdvice: List<String> = emptyList(),
    val disclaimer: String = "本 AI 系統僅提供健康教育與分診參考，無法取代執業醫師之實體診斷與處方。若症狀劇烈或突發加重，請務必立即就醫。",
    val isComprehensive: Boolean = false,
    val inquiryRound: Int = 1
)

enum class MessageSender {
    USER,
    AI,
    SYSTEM
}

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val timestamp: Long = System.currentTimeMillis(),
    val text: String,
    val analysisResult: AiAnalysisResult? = null,
    val answeredQuestionId: String? = null,
    val selectedOption: String? = null
)

data class InitialSymptomInput(
    val description: String,
    val duration: String = "",
    val painLevel: Int = 0, // 0 to 10
    val ageGroup: String = "成人",
    val chronicConditions: String = ""
)
