package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
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
import com.example.data.model.TriageLevel
import com.example.ui.theme.TriageClinicAmber
import com.example.ui.theme.TriageClinicAmberBg
import com.example.ui.theme.TriageEmergencyRed
import com.example.ui.theme.TriageEmergencyRedBg
import com.example.ui.theme.TriagePromptOrange
import com.example.ui.theme.TriagePromptOrangeBg
import com.example.ui.theme.TriageSelfCareGreen
import com.example.ui.theme.TriageSelfCareGreenBg

@Composable
fun TriageBadge(
    level: TriageLevel,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (level) {
        TriageLevel.EMERGENCY -> Triple(TriageEmergencyRedBg, TriageEmergencyRed, Icons.Default.Warning)
        TriageLevel.PROMPT -> Triple(TriagePromptOrangeBg, TriagePromptOrange, Icons.Default.AccessTime)
        TriageLevel.ROUTINE_CLINIC -> Triple(TriageClinicAmberBg, TriageClinicAmber, Icons.Default.LocalHospital)
        TriageLevel.SELF_CARE -> Triple(TriageSelfCareGreenBg, TriageSelfCareGreen, Icons.Default.CheckCircle)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("triage_badge_${level.name.lowercase()}")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = level.labelZh,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = level.labelZh,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
