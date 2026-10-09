package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TriageEmergencyRed
import com.example.ui.theme.TriageEmergencyRedBg

@Composable
fun RedFlagGuideScreen(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("red_flag_guide_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Emergency Callout
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TriageEmergencyRedBg),
                border = BorderStroke(1.5.dp, TriageEmergencyRed)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(TriageEmergencyRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "緊急急救電話：119",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TriageEmergencyRed
                        )
                        Text(
                            text = "若突發意識模糊、心肌梗塞、大出血或嚴重呼吸困難，請勿等待，立刻撥打 119 或由專人送至最近急診室！",
                            fontSize = 12.sp,
                            color = Color(0xFF7F1D1D),
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Section: Critical Red Flags
        item {
            Text(
                text = "常見危重紅旗徵候（不可輕忽）",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 1. 心血管危急
        item {
            RedFlagItem(
                icon = Icons.Default.Favorite,
                title = "心肌梗塞與心血管危象",
                signs = listOf(
                    "胸骨後壓榨感、窒息感超過 15 分鐘不緩解",
                    "痛感延伸至下巴、左肩頸或左手臂內側",
                    "伴隨大量冒冷汗、臉色死白、嚴重噁心與呼吸短促"
                )
            )
        }

        // 2. 腦中風辨識 FAST
        item {
            RedFlagItem(
                icon = Icons.Default.Psychology,
                title = "急性腦中風 (FAST 辨識法)",
                signs = listOf(
                    "F (Face 臉部)：請患者微笑，嘴角單邊下垂或法令紋消失",
                    "A (Arm 手臂)：雙手平舉向前，單側手臂無力垂落",
                    "S (Speech 說話)：口齒不清、無法理解他人對話或答非所問",
                    "T (Time 時間)：記下發作確切時間，把握黃金 3~4.5 小時溶栓治療"
                )
            )
        }

        // 3. 急腹症
        item {
            RedFlagItem(
                icon = Icons.Default.Warning,
                title = "急腹症（闌尾穿孔/腹膜炎/內出血）",
                signs = listOf(
                    "肚子摸起來如木板般僵硬（板狀腹）",
                    "輕壓腹部並迅速放開時引發劇烈震痛（反彈痛）",
                    "嘔吐咖啡渣狀胃液、大量吐血或排出瀝青樣黑便",
                    "突發撕裂樣腹部劇痛伴隨血壓驟降昏厥"
                )
            )
        }

        // Section: How to talk to doctor
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "就醫溝通訣竅：如何向醫師精確描述？",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AdviceRow(num = "1", title = "發生時間與起伏", desc = "何時開始？是突然爆發還是慢慢加重？持續多久？")
                    AdviceRow(num = "2", title = "感覺與精確部位", desc = "悶痛、刺痛、絞痛還是灼熱感？手指能否指出最痛一點？")
                    AdviceRow(num = "3", title = "誘發與緩解因子", desc = "吃飽後、空腹、走路、深呼吸或特定姿勢會變嚴重或減輕嗎？")
                    AdviceRow(num = "4", title = "伴隨症狀與病史", desc = "有無發燒、腹瀉、手麻、嘔吐？有無高血壓/糖尿病或藥物過敏？")
                }
            }
        }
    }
}

@Composable
private fun RedFlagItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    signs: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            signs.forEach { sign ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "•", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 6.dp))
                    Text(text = sign, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun AdviceRow(num: String, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = num,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
        }
    }
}
