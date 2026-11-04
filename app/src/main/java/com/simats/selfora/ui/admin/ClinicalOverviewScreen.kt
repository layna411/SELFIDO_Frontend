package com.simats.selfora.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.theme.*
import com.simats.selfora.ui.components.design.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicalOverviewScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val blueAccent = SelforaPrimary
    val textPrimary = SelforaTextPrimary
    val textSecondary = SelforaTextSecondary

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Clinical Overview",
                subtitle = "Prompt Fading & Adaptation Auditing",
                onBackClick = onBack
            )
        },
        containerColor = SelforaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                shadowElevation = 1.5.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Clinical Hierarchy Prompt Levels (L0–L6)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    Text("Standardized occupational therapy prompt taxonomy audit", fontSize = 12.sp, color = textSecondary)

                    Spacer(modifier = Modifier.height(16.dp))

                    PromptLevelAuditRow("L0 - Independent", "Child completes task step without physical or verbal cues", Color(0xFF10B981))
                    PromptLevelAuditRow("L1 - Visual Prompt", "Step photo, card or visual sequence cue", Color(0xFF06B6D4))
                    PromptLevelAuditRow("L2 - Gesture Prompt", "Therapist pointing toward sleeve or spoon", Color(0xFF3B82F6))
                    PromptLevelAuditRow("L3 - Verbal Prompt", "Clear spoken instructional prompt", Color(0xFF8B5CF6))
                    PromptLevelAuditRow("L4 - Model / Video", "Therapist demonstration or video modeling", Color(0xFFEC4899))
                    PromptLevelAuditRow("L5 - Partial Physical", "Minimal physical touch at elbow/wrist", Color(0xFFF59E0B))
                    PromptLevelAuditRow("L6 - Full Physical", "Hand-over-hand complete physical assistance", Color(0xFFEF4444))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                shadowElevation = 1.5.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Clinical Safeguards & Governance", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("• Historical clinical records are immutable and logged with actor signatures.", fontSize = 12.sp, color = textSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Caregiver home practice submissions do NOT alter clinical prompt fading plans without therapist review.", fontSize = 12.sp, color = textSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Prompt fading target criteria require 3 consecutive successful trials.", fontSize = 12.sp, color = textSecondary)
                }
            }
        }
    }
}

@Composable
private fun PromptLevelAuditRow(title: String, description: String, color: Color) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        }
        Text(description, fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.padding(start = 18.dp))
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = Color(0xFFF1F5F9))
    }
}
