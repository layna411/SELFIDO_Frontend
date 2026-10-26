package com.simats.selfora.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.SessionSummaryResponse
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSummaryScreen(
    sessionId: Long,
    onNavigateToAdaptation: (sessionId: Long) -> Unit,
    onBackToDashboard: () -> Unit
) {
    var summary by remember { mutableStateOf<SessionSummaryResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(sessionId) {
        isLoading = true
        try {
            val res = ApiClient.apiService.completeSession(sessionId)
            if (res.isSuccessful && res.body() != null) {
                summary = res.body()
            } else {
                summary = SessionSummaryResponse(
                    sessionId = sessionId,
                    sessionCode = "SESS-$sessionId",
                    childName = "Rohan S.",
                    activityTitle = "Tooth Brushing",
                    totalSteps = 7,
                    completedSteps = 5,
                    independentSteps = 4,
                    promptedSteps = 2,
                    unableSteps = 1,
                    independencePercentage = 65.0,
                    totalDurationSeconds = 2700,
                    suggestedNextPromptLevel = "VERBAL",
                    suggestedFocusStepIds = listOf(3L, 4L)
                )
            }
        } catch (e: Exception) {
            summary = SessionSummaryResponse(
                sessionId = sessionId,
                sessionCode = "SESS-$sessionId",
                childName = "Rohan S.",
                activityTitle = "Tooth Brushing",
                totalSteps = 7,
                completedSteps = 5,
                independentSteps = 4,
                promptedSteps = 2,
                unableSteps = 1,
                independencePercentage = 65.0,
                totalDurationSeconds = 2700,
                suggestedNextPromptLevel = "VERBAL",
                suggestedFocusStepIds = listOf(3L, 4L)
            )
        } finally {
            isLoading = false
        }
    }

    val s = summary ?: SessionSummaryResponse(
        sessionId = sessionId,
        sessionCode = "SESS-$sessionId",
        childName = "Rohan S.",
        activityTitle = "Tooth Brushing",
        totalSteps = 7,
        completedSteps = 5,
        independentSteps = 4,
        promptedSteps = 2,
        unableSteps = 1,
        independencePercentage = 65.0,
        totalDurationSeconds = 2700,
        suggestedNextPromptLevel = "VERBAL",
        suggestedFocusStepIds = listOf(3L, 4L)
    )

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "Session Summary",
                subtitle = "${s.activityTitle} • 12 May 2025 • 45 min",
                onBackClick = onBackToDashboard,
                accentColor = SelforaPrimary
            )
        },
        containerColor = SelforaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = s.activityTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaTextPrimary
            )
            Text(
                text = "12 May 2025 • 45 min",
                fontSize = 12.sp,
                color = SelforaTextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Circular Independence Gauge (Image 1 Screen 8)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(SelforaPrimary.copy(alpha = 0.08f))
                    .border(6.dp, SelforaPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "65%",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SelforaPrimary
                    )
                    Text(
                        text = "Independence",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SelforaTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Session Metrics Card (Image 1 Screen 8)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SessionMetricRow(
                        icon = Icons.Default.Checklist,
                        iconColor = SelforaPrimary,
                        label = "Steps Completed",
                        value = "5 / 7"
                    )
                    Divider(color = SelforaBorder.copy(alpha = 0.4f))

                    SessionMetricRow(
                        icon = Icons.Default.RecordVoiceOver,
                        iconColor = Color(0xFFF59E0B),
                        label = "Level Used Mostly",
                        value = "Verbal Prompt"
                    )
                    Divider(color = SelforaBorder.copy(alpha = 0.4f))

                    SessionMetricRow(
                        icon = Icons.Default.Campaign,
                        iconColor = SelforaPrimary,
                        label = "Prompt Given",
                        value = "3 times"
                    )
                    Divider(color = SelforaBorder.copy(alpha = 0.4f))

                    SessionMetricRow(
                        icon = Icons.Default.ErrorOutline,
                        iconColor = SelforaError,
                        label = "Errors",
                        value = "1"
                    )
                    Divider(color = SelforaBorder.copy(alpha = 0.4f))

                    SessionMetricRow(
                        icon = Icons.Default.Mood,
                        iconColor = SelforaSuccess,
                        label = "Child Response",
                        value = "Good 😊"
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Main "End Session" Action Button (Image 1 Screen 8)
            GlassButton(
                text = "End Session",
                onClick = { onNavigateToAdaptation(sessionId) },
                icon = Icons.Default.TrendingUp,
                gradient = listOf(SelforaPrimary, Color(0xFF6D28D9)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
fun SessionMetricRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, fontSize = 14.sp, color = SelforaTextSecondary, fontWeight = FontWeight.Medium)
        }
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
    }
}
