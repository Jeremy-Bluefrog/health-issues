package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.ui.components.AnalysisDetailCard
import com.example.ui.components.FollowUpCard
import com.example.ui.components.TriageBadge
import com.example.ui.viewmodel.SymptomUiState
import com.example.ui.viewmodel.SymptomViewModel

@Composable
fun ConsultationScreen(
    state: SymptomUiState,
    viewModel: SymptomViewModel,
    modifier: Modifier = Modifier
) {
    if (!state.isSessionActive) {
        // 尚未進入問診對話：顯示病徵輸入與引導頁面
        InitialInputForm(
            state = state,
            viewModel = viewModel,
            modifier = modifier
        )
    } else {
        // 互動對話與持續追問流程
        ActiveConsultationThread(
            state = state,
            viewModel = viewModel,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InitialInputForm(
    state: SymptomUiState,
    viewModel: SymptomViewModel,
    modifier: Modifier = Modifier
) {
    var showAdvancedOptions by remember { mutableStateOf(false) }
    var painSliderValue by remember { mutableFloatStateOf(state.initialInput.painLevel.toFloat()) }

    val quickTags = listOf(
        "右下腹疼痛", "頭痛且畏光噁心", "胸口悶重壓感", "高燒畏寒",
        "喉嚨劇痛咳痰", "天旋地轉頭暈", "胃酸倒流灼痛", "嘔吐腹瀉",
        "呼吸不順微喘", "皮膚突發紅疹", "全身關節痠痛"
    )

    val durationOptions = listOf("幾小時內", "今天開始", "1~2天", "3~5天", "超過一週", "數週以上")
    val ageGroups = listOf("幼童", "青少年", "成年人", "65歲以上長者")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
    ) {
        // Minimalist Clean Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "智能症狀分診與深度追問",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "輸入身體不適，AI 將引導追問並提供精準分科建議",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Primary Symptom Input Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "請描述您目前不舒服的症狀 *",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.initialInput.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        placeholder = {
                            Text(
                                "例如：昨天晚上開始右下腹部隱隱作痛，今天稍微發燒，走路跳躍時肚子震痛明顯，且食慾很差...",
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("symptom_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    if (state.errorMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Symptom Chips
                    Text(
                        text = "常見症狀快速標籤（點擊加入描述）：",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickTags.forEach { tag ->
                            FilterChip(
                                selected = false,
                                onClick = { viewModel.appendQuickTag(tag) },
                                label = { Text(tag, fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AddCircleOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Expandable Advanced Details (Duration, Pain Scale, Age, Conditions)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "補充個人特徵（有助於提升鑑別精準度）",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = { showAdvancedOptions = !showAdvancedOptions }) {
                            Icon(
                                imageVector = if (showAdvancedOptions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "展開或收起補充選項"
                            )
                        }
                    }

                    AnimatedVisibility(visible = showAdvancedOptions) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            // 1. 發作持續時間
                            Text(
                                text = "症狀已持續時間：",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                durationOptions.forEach { dur ->
                                    val isSelected = state.initialInput.duration == dur
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.updateDuration(if (isSelected) "" else dur)
                                        },
                                        label = { Text(dur, fontSize = 12.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 2. 自評疼痛程度 (Pain Scale 0-10)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "主觀疼痛程度：",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${painSliderValue.toInt()} 分 (${getPainDescription(painSliderValue.toInt())})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = painSliderValue,
                                onValueChange = {
                                    painSliderValue = it
                                    viewModel.updatePainLevel(it.toInt())
                                },
                                valueRange = 0f..10f,
                                steps = 9,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("pain_level_slider")
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 3. 年齡層
                            Text(
                                text = "年齡族群：",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                ageGroups.forEach { age ->
                                    val isSelected = state.initialInput.ageGroup == age
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.updateAgeGroup(age) },
                                        label = { Text(age, fontSize = 12.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 4. 慢性病史或用藥
                            Text(
                                text = "既往慢性病或固定用藥（選填）：",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = state.initialInput.chronicConditions,
                                onValueChange = { viewModel.updateChronicConditions(it) },
                                placeholder = { Text("例如：高血壓、糖尿病、氣喘、正在服用抗凝血藥...", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Action CTA Button
        item {
            Button(
                onClick = { viewModel.startConsultation() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_analysis_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MedicalInformation,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "開始 AI 分診分析與互動追問",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "🛡️ 本工具專為急門診分流指引設計，不作為最終診斷。遇劇痛呼吸困難請即刻掛急診。",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline,
                lineHeight = 15.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun ActiveConsultationThread(
    state: SymptomUiState,
    viewModel: SymptomViewModel,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom whenever messages or loading state changes
    LaunchedEffect(state.messages.size, state.isAnalyzing) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Active Session Top Control Bar
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "第 ${state.inquiryRound} 輪問診中",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 產出完整評估按鈕
                    OutlinedButton(
                        onClick = { viewModel.requestFinalComprehensiveReport() },
                        enabled = !state.isAnalyzing,
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("request_final_report_button"),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "產出最終報告", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // 重新問診按鈕
                    IconButton(
                        onClick = { viewModel.resetConsultation() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("reset_consultation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "重新開始問診",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Conversation Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(state.messages) { index, msg ->
                val isLatestAiMessage = msg.sender == MessageSender.AI && index == state.messages.lastIndex

                if (msg.sender == MessageSender.USER) {
                    UserMessageBubble(message = msg)
                } else {
                    AiMessageItem(
                        message = msg,
                        isLatest = isLatestAiMessage,
                        isAnalyzing = state.isAnalyzing,
                        onAnswerQuestion = { qId, qText, answerText ->
                            viewModel.answerFollowUp(qId, qText, answerText)
                        }
                    )
                }
            }

            if (state.isAnalyzing) {
                item {
                    LoadingAnalysisBubble()
                }
            }
        }
    }
}

@Composable
private fun UserMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 4.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
                .background(MaterialTheme.colorScheme.primary)
                .padding(14.dp)
                .testTag("user_message_bubble")
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun AiMessageItem(
    message: ChatMessage,
    isLatest: Boolean,
    isAnalyzing: Boolean,
    onAnswerQuestion: (String, String, String) -> Unit
) {
    val analysis = message.analysisResult

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_message_item")
    ) {
        if (analysis != null) {
            // Full Detailed Clinical Assessment
            AnalysisDetailCard(analysis = analysis)

            // AI Follow-up Questions (追問區塊)
            if (analysis.followUpQuestions.isNotEmpty() && !analysis.isComprehensive) {
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Help,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "為進一步確定病因，AI 正在向您追問以下重要細節：",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                analysis.followUpQuestions.forEachIndexed { qIdx, question ->
                    FollowUpCard(
                        question = question,
                        questionIndex = qIdx + 1,
                        totalQuestions = analysis.followUpQuestions.size,
                        isAnswered = !isLatest, // Only the latest AI turn is interactive
                        selectedAnswer = message.selectedOption,
                        onAnswerSelected = { answer ->
                            if (!isAnalyzing) {
                                onAnswerQuestion(question.id, question.question, answer)
                            }
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        } else {
            // Simple text fallback
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(14.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun LoadingAnalysisBubble() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "AI 正在以臨床思維深入分析病理徵兆...",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getPainDescription(level: Int): String {
    return when (level) {
        0 -> "完全無痛"
        1, 2 -> "輕微隱痛 (不影響日常)"
        3, 4 -> "輕中度 (需分散注意力)"
        5, 6 -> "中度疼痛 (影響工作作息)"
        7, 8 -> "劇烈疼痛 (無法專注入眠)"
        9, 10 -> "極度劇痛 (難以忍受)"
        else -> "普通"
    }
}
