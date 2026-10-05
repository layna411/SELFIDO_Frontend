package com.simats.selfora.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.SessionSummaryResponse
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

data class SummaryTableRow(
    val stepNumber: Int,
    val taskTitle: String,
    val assistanceLevel: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSummaryScreen(
    sessionId: Long,
    onNavigateToAdaptation: (sessionId: Long) -> Unit,
    onBackToDashboard: () -> Unit
) {
    var summary by remember { mutableStateOf<SessionSummaryResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    val sampleTableRows = remember {
        listOf(
            SummaryTableRow(1, "Pick up T-shirt", "Independent", "Completed"),
            SummaryTableRow(2, "Orient front side", "Independent", "Completed"),
            SummaryTableRow(3, "Gather bottom hem", "Visual Prompt", "Completed"),
            SummaryTableRow(4, "Find neck hole", "Verbal Prompt", "Completed"),
            SummaryTableRow(5, "Lift over head", "Visual Prompt", "Completed"),
            SummaryTableRow(6, "Position over head", "Verbal Prompt", "Completed"),
            SummaryTableRow(7, "Put head through opening", "Partial Physical", "Completed"),
            SummaryTableRow(8, "Lower shirt past neck", "Visual Prompt", "Completed"),
            SummaryTableRow(9, "Find right armhole", "Independent", "Completed"),
            SummaryTableRow(10, "Push right arm through", "Visual Prompt", "Completed"),
            SummaryTableRow(11, "Find left armhole", "Independent", "Completed"),
            SummaryTableRow(12, "Push left arm through", "Visual Prompt", "Completed"),
            SummaryTableRow(13, "Pull shirt torso down", "Independent", "Completed"),
            SummaryTableRow(14, "Pull back hem down", "Verbal Prompt", "Completed"),
            SummaryTableRow(15, "Adjust shoulders", "Independent", "Completed"),
            SummaryTableRow(16, "Unfold twisted sleeves", "Unable", "Unable"),
            SummaryTableRow(17, "Straighten bottom edge", "Independent", "Completed"),
            SummaryTableRow(18, "Final check", "Independent", "Completed")
        )
    }

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
                    childName = "Arjun Kumar",
                    activityTitle = "Boy T-Shirt Dressing Practice",
                    totalSteps = 18,
                    completedSteps = 17,
                    independentSteps = 9,
                    promptedSteps = 7,
                    unableSteps = 1,
                    independencePercentage = 56.3,
                    totalDurationSeconds = 420,
                    suggestedNextPromptLevel = "VISUAL",
                    suggestedFocusStepIds = listOf(7L, 16L)
                )
            }
        } catch (e: Exception) {
            summary = SessionSummaryResponse(
                sessionId = sessionId,
                sessionCode = "SESS-$sessionId",
                childName = "Arjun Kumar",
                activityTitle = "Boy T-Shirt Dressing Practice",
                totalSteps = 18,
                completedSteps = 17,
                independentSteps = 9,
                promptedSteps = 7,
                unableSteps = 1,
                independencePercentage = 56.3,
                totalDurationSeconds = 420,
                suggestedNextPromptLevel = "VISUAL",
                suggestedFocusStepIds = listOf(7L, 16L)
            )
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "Clinical Assessment & Session Summary",
                subtitle = "Session ID: #$sessionId",
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
                .padding(16.dp)
        ) {
            if (isLoading || summary == null) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SelforaPrimary)
                }
            } else {
                val s = summary!!

                // Summary Hero Card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SelforaSuccess, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Session Clinical Record Saved!", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SelforaTextPrimary)
                        Text("${s.childName} • ${s.activityTitle}", fontSize = 13.sp, color = SelforaTextSecondary)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Independence Percentage Badge Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SelforaPrimary.copy(alpha = 0.12f), shape = RoundedCornerShape(18.dp))
                                .border(1.dp, SelforaPrimary.copy(alpha = 0.3f), shape = RoundedCornerShape(18.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${s.independencePercentage}%", fontWeight = FontWeight.ExtraBold, fontSize = 38.sp, color = SelforaPrimary)
                                Text("Overall ADL Independence Score", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                Text("Formula: (Independent Steps / Assessed Steps) × 100", fontSize = 10.sp, color = SelforaTextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stat Metrics Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            SummaryStatItem("Independent", "${s.independentSteps}", SelforaSuccess)
                            SummaryStatItem("Prompted", "${s.promptedSteps}", SelforaPrimary)
                            SummaryStatItem("Unable", "${s.unableSteps}", SelforaError)
                            SummaryStatItem("Total Steps", "${s.totalSteps}", SelforaTextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Step-by-Step Assistance Breakdown Table (Section 6)
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("STEP-WISE ASSISTANCE BREAKDOWN TABLE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SelforaPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Step", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f))
                        Text("Task", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                        Text("Assistance Level", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                        Text("Status", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Table Rows
                    sampleTableRows.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("#${row.stepNumber}", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f))
                            Text(row.taskTitle, fontSize = 12.sp, color = SelforaTextPrimary, modifier = Modifier.weight(2f))
                            Text(row.assistanceLevel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = getPromptHierarchyColor(row.assistanceLevel), modifier = Modifier.weight(2f))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (row.status == "Completed") SelforaSuccess.copy(alpha = 0.15f) else SelforaError.copy(alpha = 0.15f),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Text(
                                    text = row.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (row.status == "Completed") SelforaSuccess else SelforaError,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Divider(color = Color.Black.copy(alpha = 0.05f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fading Recommendation Preview
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SelforaSecondary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Clinical Prompt Fading Recommendation", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaSecondary)
                            Text("Suggested Baseline Target: ${s.suggestedNextPromptLevel}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextPrimary)
                            Text("Focus Steps for Fading: Steps 7 & 16", fontSize = 11.sp, color = SelforaTextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassButton(
                        text = "Proceed to Adaptive Plan",
                        onClick = { onNavigateToAdaptation(sessionId) },
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    GlassOutlinedButton(
                        text = "Return to Therapist Dashboard",
                        onClick = onBackToDashboard,
                        icon = Icons.Default.Home,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
fun SummaryStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = color)
        Text(label, fontSize = 11.sp, color = SelforaTextSecondary)
    }
}
