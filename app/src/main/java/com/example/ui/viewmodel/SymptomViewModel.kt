package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ConsultationEntity
import com.example.data.model.AiAnalysisResult
import com.example.data.model.ChatMessage
import com.example.data.model.InitialSymptomInput
import com.example.data.model.MessageSender
import com.example.data.model.TriageLevel
import com.example.data.remote.GeminiTriageService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

data class SymptomUiState(
    val initialInput: InitialSymptomInput = InitialSymptomInput(""),
    val isAnalyzing: Boolean = false,
    val isSessionActive: Boolean = false,
    val sessionId: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val latestAnalysis: AiAnalysisResult? = null,
    val inquiryRound: Int = 0,
    val errorMessage: String? = null,
    val activeTab: Int = 0, // 0: 諮詢問答, 1: 問診紀錄, 2: 急症警訊指南
    val viewingHistoryItem: ConsultationEntity? = null
)

class SymptomViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val dao = db.consultationDao()
    private val geminiService = GeminiTriageService()

    private val _uiState = MutableStateFlow(SymptomUiState())
    val uiState: StateFlow<SymptomUiState> = _uiState.asStateFlow()

    val consultationHistory: StateFlow<List<ConsultationEntity>> = dao.getAllConsultations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setTab(tabIndex: Int) {
        _uiState.update { it.copy(activeTab = tabIndex, viewingHistoryItem = null) }
    }

    fun updateDescription(desc: String) {
        _uiState.update {
            it.copy(initialInput = it.initialInput.copy(description = desc), errorMessage = null)
        }
    }

    fun appendQuickTag(tag: String) {
        val current = _uiState.value.initialInput.description
        val updated = if (current.isBlank()) tag else "$current、$tag"
        updateDescription(updated)
    }

    fun updateDuration(duration: String) {
        _uiState.update { it.copy(initialInput = it.initialInput.copy(duration = duration)) }
    }

    fun updatePainLevel(pain: Int) {
        _uiState.update { it.copy(initialInput = it.initialInput.copy(painLevel = pain)) }
    }

    fun updateAgeGroup(age: String) {
        _uiState.update { it.copy(initialInput = it.initialInput.copy(ageGroup = age)) }
    }

    fun updateChronicConditions(cond: String) {
        _uiState.update { it.copy(initialInput = it.initialInput.copy(chronicConditions = cond)) }
    }

    fun startConsultation() {
        val input = _uiState.value.initialInput
        if (input.description.trim().isBlank()) {
            _uiState.update { it.copy(errorMessage = "請先輸入您感到不適的症狀描述") }
            return
        }

        val newSessionId = UUID.randomUUID().toString()
        val userInitialMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            text = buildString {
                append(input.description)
                if (input.duration.isNotBlank()) append("（已持續：${input.duration}）")
                if (input.painLevel > 0) append("（疼痛自評：${input.painLevel}/10）")
                if (input.chronicConditions.isNotBlank()) append("（病史：${input.chronicConditions}）")
            }
        )

        _uiState.update {
            it.copy(
                isAnalyzing = true,
                isSessionActive = true,
                sessionId = newSessionId,
                messages = listOf(userInitialMsg),
                inquiryRound = 1,
                errorMessage = null,
                latestAnalysis = null
            )
        }

        viewModelScope.launch {
            try {
                val analysis = geminiService.analyzeSymptoms(
                    input = input,
                    conversationHistory = emptyList(),
                    isRequestingFinalSummary = false,
                    round = 1
                )

                val aiMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.AI,
                    text = analysis.summary,
                    analysisResult = analysis
                )

                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        messages = it.messages + aiMsg,
                        latestAnalysis = analysis
                    )
                }

                saveOrUpdateHistorySession(newSessionId, input.description, analysis)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = "分析過程發生狀況，請重試：${e.message}"
                    )
                }
            }
        }
    }

    fun answerFollowUp(questionId: String, questionText: String, answerText: String) {
        if (answerText.trim().isBlank()) return

        val currentRound = _uiState.value.inquiryRound + 1
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            text = "【回答追問】$questionText ➔ $answerText",
            answeredQuestionId = questionId,
            selectedOption = answerText
        )

        val updatedMessages = _uiState.value.messages + userMsg

        _uiState.update {
            it.copy(
                messages = updatedMessages,
                isAnalyzing = true,
                inquiryRound = currentRound
            )
        }

        viewModelScope.launch {
            try {
                val historyTuples = buildHistoryTuples(updatedMessages)
                val analysis = geminiService.analyzeSymptoms(
                    input = _uiState.value.initialInput,
                    conversationHistory = historyTuples,
                    isRequestingFinalSummary = false,
                    round = currentRound
                )

                val aiMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.AI,
                    text = analysis.summary,
                    analysisResult = analysis
                )

                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        messages = it.messages + aiMsg,
                        latestAnalysis = analysis
                    )
                }

                saveOrUpdateHistorySession(_uiState.value.sessionId, _uiState.value.initialInput.description, analysis)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = "追問分析時發生錯誤：${e.message}"
                    )
                }
            }
        }
    }

    fun requestFinalComprehensiveReport() {
        val currentRound = _uiState.value.inquiryRound + 1
        val userReqMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            text = "請為我產出【最終完整綜合評估報告】"
        )
        val updatedMessages = _uiState.value.messages + userReqMsg

        _uiState.update {
            it.copy(
                messages = updatedMessages,
                isAnalyzing = true,
                inquiryRound = currentRound
            )
        }

        viewModelScope.launch {
            try {
                val historyTuples = buildHistoryTuples(updatedMessages)
                val finalAnalysis = geminiService.analyzeSymptoms(
                    input = _uiState.value.initialInput,
                    conversationHistory = historyTuples,
                    isRequestingFinalSummary = true,
                    round = currentRound
                )

                val aiMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.AI,
                    text = "已為您彙整所有症狀細節，以下為【最終綜合分診評估報告】",
                    analysisResult = finalAnalysis
                )

                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        messages = it.messages + aiMsg,
                        latestAnalysis = finalAnalysis
                    )
                }

                saveOrUpdateHistorySession(_uiState.value.sessionId, _uiState.value.initialInput.description, finalAnalysis)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = "產出報告時發生問題：${e.message}"
                    )
                }
            }
        }
    }

    fun resetConsultation() {
        _uiState.update {
            it.copy(
                isSessionActive = false,
                messages = emptyList(),
                latestAnalysis = null,
                inquiryRound = 0,
                errorMessage = null,
                initialInput = InitialSymptomInput("")
            )
        }
    }

    fun viewHistoryDetail(item: ConsultationEntity) {
        _uiState.update { it.copy(viewingHistoryItem = item) }
    }

    fun closeHistoryDetail() {
        _uiState.update { it.copy(viewingHistoryItem = null) }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
            if (_uiState.value.viewingHistoryItem?.id == id) {
                _uiState.update { it.copy(viewingHistoryItem = null) }
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            dao.clearAll()
            _uiState.update { it.copy(viewingHistoryItem = null) }
        }
    }

    private fun buildHistoryTuples(msgs: List<ChatMessage>): List<Pair<String, String>> {
        return msgs.map {
            val speaker = if (it.sender == MessageSender.USER) "病患" else "AI諮詢"
            speaker to it.text
        }
    }

    private fun saveOrUpdateHistorySession(
        sessionId: String,
        initialDesc: String,
        analysis: AiAnalysisResult
    ) {
        viewModelScope.launch {
            val causesSummary = analysis.possibleCauses.joinToString("、") { it.name }
            val deptSummary = analysis.recommendedDepartments.joinToString("、") { it.name }

            val jsonSnapshot = JSONObject().apply {
                put("triageLevel", analysis.triageLevel.name)
                put("summary", analysis.summary)
                put("causes", causesSummary)
                put("dept", deptSummary)
                put("disclaimer", analysis.disclaimer)
                put("isComprehensive", analysis.isComprehensive)
            }.toString()

            val entity = ConsultationEntity(
                sessionId = sessionId,
                initialSymptom = initialDesc,
                triageLevelName = analysis.triageLevel.name,
                summary = analysis.summary,
                possibleCausesSummary = causesSummary,
                recommendedDept = deptSummary,
                fullJson = jsonSnapshot
            )
            dao.insert(entity)
        }
    }
}
