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
import com.simats.selfora.data.model.ClinicalPromptLevel
import com.simats.selfora.data.model.PerformanceRecordResponse
import com.simats.selfora.data.model.SessionResponse
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSummaryScreen(
    sessionId: Long,
    onNavigateToAdaptation: (sessionId: Long, childId: Long, activityId: Long) -> Unit,
    onBackToDashboard: () -> Unit
) {
    var session by remember { mutableStateOf<SessionResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reloadTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(sessionId, reloadTrigger) {
        if (sessionId <= 0) {
            errorMessage = "Invalid clinical session ID ($sessionId)"
            isLoading = false
            return@LaunchedEffect
        }
        isLoading = true
        errorMessage = null
        try {
            val res = ApiClient.apiService.getSessionById(sessionId)
            if (res.isSuccessful && res.body() != null) {
                session = res.body()
            } else {
                val code = res.code()
                errorMessage = when (code) {
                    404 -> "Session #$sessionId was not found."
                    403 -> "Unauthorized to view this clinical session."
                    else -> "Failed to load session summary (HTTP $code)"
                }
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.localizedMessage ?: "Unable to connect to server"}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "Session Summary",
                subtitle = session?.let { "${it.childName} • ${it.sessionCode}" } ?: "Session #$sessionId",
                onBackClick = onBackToDashboard,
                accentColor = SelforaPrimary
            )
        },
        containerColor = SelforaBackground
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = SelforaPrimary)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Loading dynamic clinical session...",
                        fontSize = 13.5.sp,
                        color = SelforaTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else if (errorMessage != null || session == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SelforaError.copy(alpha = 0.12f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = SelforaError, modifier = Modifier.size(32.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Session Summary Unavailable",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = errorMessage ?: "No performance data could be found for session #$sessionId.",
                            color = SelforaTextSecondary,
                            fontSize = 13.5.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { reloadTrigger++ },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retry")
                            }
                            Button(
                                onClick = onBackToDashboard,
                                colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Dashboard")
                            }
                        }
                    }
                }
            }
        } else {
            val s = session!!
            val records = s.performanceRecords
            val totalSteps = s.totalSteps ?: records.size.coerceAtLeast(1)
            val completedSteps = s.completedSteps ?: records.count { it.outcome.equals("COMPLETED", ignoreCase = true) }
            val incompleteSteps = (totalSteps - completedSteps).coerceAtLeast(0)
            val indepSteps = s.independentSteps ?: records.count { it.promptLevelId == 0 && it.outcome.equals("COMPLETED", ignoreCase = true) }

            val independencePct = s.independencePercentage ?: run {
                val assessed = records.count { !it.outcome.equals("NOT_ASSESSED", ignoreCase = true) && it.promptLevelId != 8 }
                if (assessed > 0) (indepSteps.toDouble() / assessed) * 100.0 else 0.0
            }

            val dateFormatted = formatSessionDate(s.startTime)
            val startTimeFormatted = formatSessionTime(s.startTime)
            val endTimeFormatted = if (s.endTime != null) formatSessionTime(s.endTime) else "In Progress"
            val durationFormatted = formatDurationSeconds(s.totalDurationSeconds)
            val outcome = s.sessionOutcome ?: if (completedSteps >= totalSteps) "COMPLETED" else if (completedSteps > 0) "PARTIAL" else "IN_PROGRESS"
            val mostFrequent = s.mostFrequentPromptLevel ?: run {
                val counts = records.groupBy { it.promptLevelName }.mapValues { it.value.size }
                counts.maxByOrNull { it.value }?.key ?: "None"
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header profile card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = s.childName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = s.activityTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SelforaPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Session #${s.sessionId} • $dateFormatted",
                                fontSize = 12.sp,
                                color = SelforaTextSecondary
                            )
                        }

                        // Session Outcome Badge
                        val outcomeColor = when (outcome.uppercase()) {
                            "COMPLETED" -> SelforaSuccess
                            "PARTIAL" -> Color(0xFFF59E0B)
                            else -> SelforaPrimary
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = outcomeColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, outcomeColor.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = outcome.uppercase(),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = outcomeColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Circular Independence Gauge with dynamic color
                val gaugeColor = when {
                    independencePct >= 75.0 -> SelforaSuccess
                    independencePct >= 40.0 -> SelforaPrimary
                    else -> Color(0xFFF59E0B)
                }

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(gaugeColor.copy(alpha = 0.08f))
                        .border(6.dp, gaugeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${Math.round(independencePct)}%",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = gaugeColor
                        )
                        Text(
                            text = "Independence",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SelforaTextSecondary
                        )
                        Text(
                            text = "$indepSteps / $totalSteps Indep.",
                            fontSize = 10.5.sp,
                            color = gaugeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Clinical Session Metrics Card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Session Metrics & Timing",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelforaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SessionMetricRow(
                            icon = Icons.Default.Checklist,
                            iconColor = SelforaPrimary,
                            label = "Task Steps Completed",
                            value = "$completedSteps / $totalSteps"
                        )
                        Divider(color = SelforaBorder.copy(alpha = 0.4f))

                        SessionMetricRow(
                            icon = Icons.Default.PendingActions,
                            iconColor = if (incompleteSteps > 0) Color(0xFFF59E0B) else SelforaSuccess,
                            label = "Incomplete / Skipped Steps",
                            value = "$incompleteSteps"
                        )
                        Divider(color = SelforaBorder.copy(alpha = 0.4f))

                        SessionMetricRow(
                            icon = Icons.Default.Schedule,
                            iconColor = SelforaPrimary,
                            label = "Start & End Time",
                            value = "$startTimeFormatted → $endTimeFormatted"
                        )
                        Divider(color = SelforaBorder.copy(alpha = 0.4f))

                        SessionMetricRow(
                            icon = Icons.Default.Timer,
                            iconColor = Color(0xFF6366F1),
                            label = "Total Duration",
                            value = durationFormatted
                        )
                        Divider(color = SelforaBorder.copy(alpha = 0.4f))

                        SessionMetricRow(
                            icon = Icons.Default.RecordVoiceOver,
                            iconColor = Color(0xFFF59E0B),
                            label = "Most Frequent Prompt",
                            value = mostFrequent
                        )
                        Divider(color = SelforaBorder.copy(alpha = 0.4f))

                        SessionMetricRow(
                            icon = Icons.Default.Person,
                            iconColor = Color(0xFF10B981),
                            label = "Conducted By",
                            value = s.conductedByUserName.ifBlank { "Therapist" }
                        )
                    }
                }

                // Clinical Prompt Level Distribution Card
                if (records.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Recorded Clinical Prompt Levels",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Clinical assistance hierarchy used during this session",
                            fontSize = 11.5.sp,
                            color = SelforaTextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val promptCounts = records.groupBy { it.promptLevelCode }.mapValues { it.value.size }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            promptCounts.forEach { (code, count) ->
                                val clinicalLevel = ClinicalPromptLevel.fromCode(code)
                                val badgeColor = clinicalLevel?.composeColor ?: SelforaPrimary
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = badgeColor.copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = code,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeColor
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "×$count",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SelforaTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Step-by-Step Clinical Record Details
                if (records.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Step Performance Breakdown",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            records.sortedBy { it.stepNumber }.forEach { record ->
                                StepPerformanceCard(record = record)
                            }
                        }
                    }
                }

                // Therapist Notes Section
                if (!s.summaryNotes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notes, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Therapist Session Notes",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = s.summaryNotes,
                            fontSize = 13.sp,
                            color = SelforaTextPrimary,
                            lineHeight = 19.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Action Buttons: Proceed to Adaptation & Back to Dashboard
                GlassButton(
                    text = "Proceed to Analyze & Adapt",
                    onClick = { onNavigateToAdaptation(s.sessionId, s.childId, s.activityId) },
                    icon = Icons.Default.TrendingUp,
                    gradient = listOf(SelforaPrimary, Color(0xFF6D28D9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onBackToDashboard,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Return to Therapist Dashboard", color = SelforaTextPrimary, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun StepPerformanceCard(record: PerformanceRecordResponse) {
    val clinicalLevel = ClinicalPromptLevel.fromId(record.promptLevelId)
        ?: ClinicalPromptLevel.fromCode(record.promptLevelCode)
    val levelColor = clinicalLevel?.composeColor ?: SelforaPrimary

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(1.dp, SelforaBorder.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SelforaPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${record.stepNumber}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = record.stepTitle.ifBlank { "Step ${record.stepNumber}" },
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SelforaTextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = levelColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, levelColor.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = record.promptLevelName.ifBlank { record.promptLevelCode },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = levelColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Outcome: ${record.outcome}",
                    fontSize = 11.sp,
                    color = if (record.outcome.equals("COMPLETED", ignoreCase = true)) SelforaSuccess else Color(0xFFF59E0B),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${record.attempts} attempt(s) • ${record.durationSeconds}s",
                    fontSize = 11.sp,
                    color = SelforaTextSecondary
                )
            }

            if (!record.therapistNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SelforaBackground.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Note: ${record.therapistNote}",
                        fontSize = 11.5.sp,
                        color = SelforaTextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
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
            Text(label, fontSize = 13.5.sp, color = SelforaTextSecondary, fontWeight = FontWeight.Medium)
        }
        Text(value, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
    }
}

private fun formatSessionDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return "N/A"
    return try {
        if (dateStr.contains("T")) {
            val datePart = dateStr.split("T")[0]
            val parts = datePart.split("-")
            if (parts.size == 3) {
                val year = parts[0]
                val month = when (parts[1].toIntOrNull()) {
                    1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"; 5 -> "May"; 6 -> "Jun";
                    7 -> "Jul"; 8 -> "Aug"; 9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec";
                    else -> parts[1]
                }
                val day = parts[2]
                "$day $month $year"
            } else datePart
        } else {
            dateStr
        }
    } catch (_: Exception) {
        dateStr
    }
}

private fun formatSessionTime(dateTimeStr: String?): String {
    if (dateTimeStr.isNullOrBlank()) return "—"
    return try {
        if (dateTimeStr.contains("T")) {
            val timePart = dateTimeStr.split("T")[1]
            val timePieces = timePart.split(":")
            if (timePieces.size >= 2) {
                var hour = timePieces[0].toIntOrNull() ?: return timePart
                val min = timePieces[1]
                val amPm = if (hour >= 12) "PM" else "AM"
                if (hour > 12) hour -= 12
                if (hour == 0) hour = 12
                "$hour:$min $amPm"
            } else timePart
        } else dateTimeStr
    } catch (_: Exception) {
        dateTimeStr
    }
}

private fun formatDurationSeconds(durSec: Int): String {
    if (durSec <= 0) return "0s"
    val mins = durSec / 60
    val secs = durSec % 60
    return if (mins > 0) {
        "${mins} min ${if (secs > 0) "${secs}s" else ""}".trim()
    } else {
        "${secs}s"
    }
}
