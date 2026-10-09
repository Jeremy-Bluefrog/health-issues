package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AiAnalysisResult
import com.example.data.model.DepartmentRecommendation
import com.example.data.model.FollowUpQuestion
import com.example.data.model.InitialSymptomInput
import com.example.data.model.PossibleCause
import com.example.data.model.TriageLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class GeminiTriageService {

    companion object {
        private const val TAG = "GeminiTriageService"
        private const val GEMINI_MODEL = "gemini-2.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    /**
     * Analyze symptoms with Gemini, maintaining conversation context for multi-turn inquiry.
     */
    suspend fun analyzeSymptoms(
        input: InitialSymptomInput,
        conversationHistory: List<Pair<String, String>>, // list of (speaker, text)
        isRequestingFinalSummary: Boolean = false,
        round: Int = 1
    ): AiAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured or empty, using clinical smart rule engine.")
            return@withContext ClinicalKnowledgeEngine.analyze(input, conversationHistory, isRequestingFinalSummary, round)
        }

        try {
            val systemInstruction = buildSystemPrompt(isRequestingFinalSummary, round)
            val userPrompt = buildUserPrompt(input, conversationHistory, isRequestingFinalSummary)

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", userPrompt))
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemInstruction))
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.3)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val url = "$BASE_URL$GEMINI_MODEL:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Empty body"
                Log.e(TAG, "Gemini API returned error ${response.code}: $errorBody")
                return@withContext ClinicalKnowledgeEngine.analyze(input, conversationHistory, isRequestingFinalSummary, round)
            }

            val responseBodyString = response.body?.string() ?: ""
            val jsonRoot = JSONObject(responseBodyString)
            val candidates = jsonRoot.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            if (text.isBlank()) {
                return@withContext ClinicalKnowledgeEngine.analyze(input, conversationHistory, isRequestingFinalSummary, round)
            }

            parseAiResponse(text, isRequestingFinalSummary, round)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to call Gemini API, falling back to smart clinical engine", e)
            ClinicalKnowledgeEngine.analyze(input, conversationHistory, isRequestingFinalSummary, round)
        }
    }

    private fun buildSystemPrompt(isFinal: Boolean, round: Int): String {
        return """
            你是一位專業、嚴謹且富同理心的醫療分診諮詢 AI 助手（非執業醫師）。
            請使用繁體中文（台灣醫學常用語），為使用者輸入的症狀進行臨床邏輯分診與深度追問。

            【任務要求】
            1. 評估嚴重程度分級 (triageLevel)：只能是 EMERGENCY (急診紅燈)、PROMPT (儘速就醫橙燈)、ROUTINE_CLINIC (常規門診黃燈)、SELF_CARE (自我照護綠燈)。
            2. 摘要 (summary)：以溫和、專業口吻簡述患者目前狀況與初步研判。
            3. 潛在可能病因 (possibleCauses)：列出 2~4 項鑑別診斷探討，含名稱、可能性(如高/中/待排除)、說明、關鍵表現。
            4. ${if (isFinal || round >= 3) "使用者已進入總結或多輪回答，followUpQuestions 可給 0~1 題非必要之進階追蹤題，或留空。" else "【重點：AI繼續追問】：請針對尚未明朗的臨床鑑別關鍵，提出 1 到 3 個精確的追問問題 (followUpQuestions)，每一題必須提供 3~4 個具體常見選項 (options) 供使用者點擊快速回答，並說明 purpose (追問目的)。"}
            5. 建議就醫科別 (recommendedDepartments)：1~2 個科別及理由。
            6. 紅旗警告 (redFlagWarnings)：若出現哪些警訊必須立即撥打119或衝急診。
            7. 居家照護與舒緩建議 (homeCareAdvice)：安全、溫和的照護指引及「切勿做的事」(如未確診前忌盲目服用止痛藥掩蓋腹膜炎)。
            8. 輸出格式：必須為標準合法的 JSON 字串，不得包含 markdown 標籤以外的多餘非 JSON 內容。
            
            JSON結構規範：
            {
              "triageLevel": "EMERGENCY" | "PROMPT" | "ROUTINE_CLINIC" | "SELF_CARE",
              "summary": "...",
              "possibleCauses": [
                {
                  "name": "...",
                  "likelihood": "...",
                  "explanation": "...",
                  "keySigns": ["...", "..."]
                }
              ],
              "followUpQuestions": [
                {
                  "id": "q1",
                  "question": "...",
                  "purpose": "...",
                  "options": ["...", "...", "..."]
                }
              ],
              "recommendedDepartments": [
                {
                  "name": "...",
                  "reason": "...",
                  "urgency": "..."
                }
              ],
              "redFlagWarnings": ["...", "..."],
              "homeCareAdvice": ["...", "..."],
              "disclaimer": "本 AI 分析僅供就醫分診參考，無法取代醫師臨床診斷與治療。若症狀持續或劇烈惡化，請立即就醫。"
            }
        """.trimIndent()
    }

    private fun buildUserPrompt(
        input: InitialSymptomInput,
        history: List<Pair<String, String>>,
        isFinal: Boolean
    ): String {
        val sb = StringBuilder()
        sb.append("【病患主訴基本資料】\n")
        sb.append("- 症狀描述：${input.description}\n")
        if (input.duration.isNotBlank()) sb.append("- 發作持續時間：${input.duration}\n")
        if (input.painLevel > 0) sb.append("- 自評疼痛程度：${input.painLevel} / 10\n")
        sb.append("- 年齡群組：${input.ageGroup}\n")
        if (input.chronicConditions.isNotBlank()) sb.append("- 過去病史/用藥：${input.chronicConditions}\n")

        if (history.isNotEmpty()) {
            sb.append("\n【前後互動問答與追問記錄】\n")
            for ((speaker, msg) in history) {
                sb.append("[$speaker]: $msg\n")
            }
        }

        if (isFinal) {
            sb.append("\n【使用者指令】：請彙整上述全部對話與回答，提供最完整的綜合評估分析報告。")
        } else {
            sb.append("\n【請執行分診評估並提出精準的追問問題】")
        }

        return sb.toString()
    }

    private fun parseAiResponse(jsonString: String, isFinal: Boolean, round: Int): AiAnalysisResult {
        // Strip markdown code fences if present
        val cleanJson = jsonString
            .replace("```json", "")
            .replace("```", "")
            .trim()

        val json = JSONObject(cleanJson)

        val triageStr = json.optString("triageLevel", "ROUTINE_CLINIC")
        val triageLevel = when (triageStr.uppercase()) {
            "EMERGENCY" -> TriageLevel.EMERGENCY
            "PROMPT" -> TriageLevel.PROMPT
            "SELF_CARE" -> TriageLevel.SELF_CARE
            else -> TriageLevel.ROUTINE_CLINIC
        }

        val summary = json.optString("summary", "已收到您的症狀描述，以下為分診評估與建議。")

        val possibleCauses = mutableListOf<PossibleCause>()
        val causesArray = json.optJSONArray("possibleCauses")
        if (causesArray != null) {
            for (i in 0 until causesArray.length()) {
                val item = causesArray.optJSONObject(i) ?: continue
                val keySignsList = mutableListOf<String>()
                val signsArr = item.optJSONArray("keySigns")
                if (signsArr != null) {
                    for (j in 0 until signsArr.length()) {
                        keySignsList.add(signsArr.optString(j))
                    }
                }
                possibleCauses.add(
                    PossibleCause(
                        name = item.optString("name", "未定病因"),
                        likelihood = item.optString("likelihood", "鑑別中"),
                        explanation = item.optString("explanation", ""),
                        keySigns = keySignsList
                    )
                )
            }
        }

        val followUpQuestions = mutableListOf<FollowUpQuestion>()
        val questionsArray = json.optJSONArray("followUpQuestions")
        if (questionsArray != null) {
            for (i in 0 until questionsArray.length()) {
                val qObj = questionsArray.optJSONObject(i) ?: continue
                val optionsList = mutableListOf<String>()
                val optsArr = qObj.optJSONArray("options")
                if (optsArr != null) {
                    for (j in 0 until optsArr.length()) {
                        optionsList.add(optsArr.optString(j))
                    }
                }
                followUpQuestions.add(
                    FollowUpQuestion(
                        id = qObj.optString("id", UUID.randomUUID().toString()),
                        question = qObj.optString("question", ""),
                        purpose = qObj.optString("purpose", ""),
                        options = optionsList
                    )
                )
            }
        }

        val recommendedDepts = mutableListOf<DepartmentRecommendation>()
        val deptsArray = json.optJSONArray("recommendedDepartments")
        if (deptsArray != null) {
            for (i in 0 until deptsArray.length()) {
                val dObj = deptsArray.optJSONObject(i) ?: continue
                recommendedDepts.add(
                    DepartmentRecommendation(
                        name = dObj.optString("name", "一般家醫科"),
                        reason = dObj.optString("reason", "綜合評估與後續轉診"),
                        urgency = dObj.optString("urgency", "常規")
                    )
                )
            }
        }

        val redFlags = mutableListOf<String>()
        val redFlagsArr = json.optJSONArray("redFlagWarnings")
        if (redFlagsArr != null) {
            for (i in 0 until redFlagsArr.length()) {
                redFlags.add(redFlagsArr.optString(i))
            }
        }

        val homeCare = mutableListOf<String>()
        val homeCareArr = json.optJSONArray("homeCareAdvice")
        if (homeCareArr != null) {
            for (i in 0 until homeCareArr.length()) {
                homeCare.add(homeCareArr.optString(i))
            }
        }

        val disclaimer = json.optString(
            "disclaimer",
            "本 AI 系統僅提供健康教育與分診參考，無法取代執業醫師之實體診斷與處方。若症狀劇烈或突發加重，請務必立即就醫。"
        )

        return AiAnalysisResult(
            triageLevel = triageLevel,
            summary = summary,
            possibleCauses = possibleCauses,
            followUpQuestions = followUpQuestions,
            recommendedDepartments = recommendedDepts,
            redFlagWarnings = redFlags,
            homeCareAdvice = homeCare,
            disclaimer = disclaimer,
            isComprehensive = isFinal,
            inquiryRound = round
        )
    }
}
