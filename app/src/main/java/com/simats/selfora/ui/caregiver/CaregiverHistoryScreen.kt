package com.simats.selfora.ui.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverHistoryScreen(
    onBack: () -> Unit = {}
) {
    Scaffold(
        containerColor = SelforaBackground,
        topBar = {
            GlassTopBar(
                title = "History",
                subtitle = "Past Records & Sessions",
                onBackClick = onBack,
                accentColor = SelforaPrimary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Item 1 (Image 2 Screen 9)
                CaregiverHistoryRecordCard(
                    date = "12 May 2025",
                    activityName = "Tooth Brushing",
                    promptLevel = "Verbal Prompt",
                    stepsScore = "5 / 7 steps",
                    status = "Completed"
                )

                // Item 2 (Image 2 Screen 9)
                CaregiverHistoryRecordCard(
                    date = "11 May 2025",
                    activityName = "Wearing T-Shirt",
                    promptLevel = "Visual Prompt",
                    stepsScore = "4 / 6 steps",
                    status = "Completed"
                )

                // Item 3 (Image 2 Screen 9)
                CaregiverHistoryRecordCard(
                    date = "11 May 2025",
                    activityName = "Eating with Spoon",
                    promptLevel = "Independent",
                    stepsScore = "6 / 7 steps",
                    status = "Completed"
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            GlassButton(
                text = "View All History",
                onClick = {},
                icon = Icons.Default.History,
                gradient = listOf(SelforaPrimary, Color(0xFF6D28D9)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
fun CaregiverHistoryRecordCard(
    date: String,
    activityName: String,
    promptLevel: String,
    stepsScore: String,
    status: String
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        backgroundColor = Color.White.copy(alpha = 0.92f)
    ) {
        Column {
            Text(date, fontSize = 11.sp, color = SelforaTextSecondary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(activityName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SelforaSuccess.copy(alpha = 0.15f)
                ) {
                    Text(
                        status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelforaSuccess,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(promptLevel, fontSize = 12.sp, color = SelforaPrimary, fontWeight = FontWeight.SemiBold)
                Text(stepsScore, fontSize = 12.sp, color = SelforaTextSecondary, fontWeight = FontWeight.Medium)
            }
        }
    }
}
