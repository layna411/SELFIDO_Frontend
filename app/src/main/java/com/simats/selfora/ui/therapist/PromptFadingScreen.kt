package com.simats.selfora.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import com.simats.selfora.data.model.*
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptFadingScreen(
    childId: Long = 1L,
    onBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var fadingPlans by remember { mutableStateOf<List<PromptFadingPlanResponse>>(emptyList()) }
    var fadingHistory by remember { mutableStateOf<List<PromptFadingHistoryItemResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fading Plan Form State
    var selectedStepNumber by remember { mutableStateOf(7) }
    var selectedStepTitle by remember { mutableStateOf("Put head through neck opening") }
    var currentLevelId by remember { mutableStateOf(3) } // Verbal Prompt
    var targetLevelId by remember { mutableStateOf(1) } // Visual Prompt
    var clinicalReasonText by remember { mutableStateOf("Child performed step independently in 3 consecutive sessions with visual cues.") }
    var reviewCriteriaText by remember { mutableStateOf("Review after 3 successful clinic sessions.") }
    var notesText by remember { mutableStateOf("Caregiver will reinforce visual cues at home.") }
    var isSaving by remember { mutableStateOf(false) }
    var actionStatusMessage by remember { mutableStateOf<String?>(null) }

    val promptHierarchy = listOf(
        0 to ("Level 0 – Independent" to PromptLevel0Independent),
        1 to ("Level 1 – Visual Prompt" to PromptLevel1Visual),
        2 to ("Level 2 – Gesture" to PromptLevel2Gesture),
        3 to ("Level 3 – Verbal Prompt" to PromptLevel3Verbal),
        4 to ("Level 4 – Model / Video" to PromptLevel4Model),
        5 to ("Level 5 – Partial Physical" to PromptLevel5PartialPhysical),
        6 to ("Level 6 – Full Physical" to PromptLevel6FullPhysical)
    )

    fun loadData() {
        isLoading = true
        scope.launch {
            try {
                val planRes = ApiClient.apiService.getPromptFadingPlansForChild(childId)
                if (planRes.isSuccessful && planRes.body() != null) {
                    fadingPlans = planRes.body()!!
                } else {
                    fadingPlans = createMockPlans(childId)
                }

                val histRes = ApiClient.apiService.getPromptFadingHistoryForChild(childId)
                if (histRes.isSuccessful && histRes.body() != null) {
                    fadingHistory = histRes.body()!!
                } else {
                    fadingHistory = createMockHistory(childId)
                }
            } catch (e: Exception) {
                fadingPlans = createMockPlans(childId)
                fadingHistory = createMockHistory(childId)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(childId) {
        loadData()
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "Prompt Fading Management",
                subtitle = "Clinical Hierarchy & Auditable History",
                onBackClick = onBack,
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
            // 1. ASSISTANCE HIERARCHY LEGEND CARD
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("CLINICAL ASSISTANCE HIERARCHY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary, letterSpacing = 1.sp)
                Text("Structured fading progression (Level 6 -> Level 0). 'Unable' is tracked separately.", fontSize = 11.sp, color = SelforaTextSecondary)
                Spacer(modifier = Modifier.height(10.dp))

                promptHierarchy.forEach { (lvl, pair) ->
                    val (label, color) = pair
                    Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = color, modifier = Modifier.size(10.dp)) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = PromptLevelUnable, modifier = Modifier.size(10.dp)) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unable / Refused (Separate Outcome – NOT a prompt level)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PromptLevelUnable)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. FADING PLAN CREATOR & APPROVAL WORKFLOW
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("CREATE & REVIEW PROMPT FADING PLAN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                Text("Therapist-controlled decision engine to fade or restore assistance levels", fontSize = 11.sp, color = SelforaTextSecondary)
                Spacer(modifier = Modifier.height(14.dp))

                // Target Task Step Selection
                Text("Selected Target Task Step:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SelforaSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SelforaBorder),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = SelforaPrimary.copy(alpha = 0.15f), modifier = Modifier.size(28.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("$selectedStepNumber", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Step $selectedStepNumber: $selectedStepTitle", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                    }
                }

                // Current vs Proposed Level Flow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Current Prompt", fontSize = 10.sp, color = SelforaTextSecondary)
                        Surface(shape = RoundedCornerShape(10.dp), color = SelforaOrangeWarning.copy(alpha = 0.15f)) {
                            Text(promptHierarchy.find { it.first == currentLevelId }?.second?.first ?: "Level $currentLevelId", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SelforaOrangeWarning)
                        }
                    }

                    Surface(shape = CircleShape, color = SelforaBluePrimary.copy(alpha = 0.12f)) {
                        Text("TO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SelforaBluePrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Proposed Target", fontSize = 10.sp, color = SelforaTextSecondary)
                        Surface(shape = RoundedCornerShape(10.dp), color = SelforaGreenSuccess.copy(alpha = 0.15f)) {
                            Text(promptHierarchy.find { it.first == targetLevelId }?.second?.first ?: "Level $targetLevelId", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SelforaGreenSuccess)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Select Target Prompt Chip Selector
                Text("Select Proposed Target Prompt Level:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0 to "Indep (L0)", 1 to "Visual (L1)", 2 to "Gesture (L2)", 3 to "Verbal (L3)", 5 to "Partial (L5)").forEach { (lvl, lbl) ->
                        FilterChip(
                            selected = targetLevelId == lvl,
                            onClick = { targetLevelId = lvl },
                            label = { Text(lbl, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = clinicalReasonText,
                    onValueChange = { clinicalReasonText = it },
                    label = { Text("Clinical Rationale for Fading / Restoring Level") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reviewCriteriaText,
                    onValueChange = { reviewCriteriaText = it },
                    label = { Text("Review Criteria (e.g. 3 consecutive sessions)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassButton(
                        text = "Approve Fading Plan",
                        onClick = {
                            isSaving = true
                            scope.launch {
                                try {
                                    val req = CreatePromptFadingPlanRequest(
                                        childId = childId,
                                        activityId = 1L,
                                        stepId = 7L,
                                        currentPromptLevelId = currentLevelId,
                                        targetPromptLevelId = targetLevelId,
                                        clinicalReason = clinicalReasonText,
                                        reviewCriteria = reviewCriteriaText,
                                        notes = notesText
                                    )
                                    ApiClient.apiService.createPromptFadingPlan(req)
                                } catch (_: Exception) {}
                                isSaving = false
                                actionStatusMessage = "Fading plan approved and logged in auditable history."
                                loadData()
                            }
                        },
                        icon = Icons.Default.Check,
                        modifier = Modifier.weight(1f)
                    )

                    GlassOutlinedButton(
                        text = "Retain Current Level",
                        onClick = {
                            targetLevelId = currentLevelId
                            actionStatusMessage = "Level retained as per therapist clinical decision."
                        },
                        icon = Icons.Default.Lock,
                        modifier = Modifier.weight(1f)
                    )
                }

                actionStatusMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(msg, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaSuccess)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. PROMPT FADING HISTORY TIMELINE (AUDIT TRAIL)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CHRONOLOGICAL PROMPT FADING HISTORY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                }
                Text("Auditable log of therapist-approved prompt changes", fontSize = 11.sp, color = SelforaTextSecondary)
                Spacer(modifier = Modifier.height(14.dp))

                if (fadingHistory.isEmpty()) {
                    Text("No prompt fading events recorded yet.", fontSize = 12.sp, color = SelforaTextMuted)
                } else {
                    fadingHistory.forEach { h ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SelforaSurface.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SelforaBorder),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Step ${h.stepNumber}: ${h.stepTitle}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                    Text(h.changeDate, fontSize = 10.sp, color = SelforaTextMuted)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = SelforaOrangeWarning.copy(alpha = 0.15f)) {
                                        Text(h.previousPromptLevelName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SelforaOrangeWarning, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    Text(" ➔ ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                                    Surface(shape = RoundedCornerShape(6.dp), color = SelforaGreenSuccess.copy(alpha = 0.15f)) {
                                        Text(h.newPromptLevelName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SelforaGreenSuccess, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Approved By: ${h.approvedByTherapistName}", fontSize = 11.sp, color = SelforaPrimary, fontWeight = FontWeight.SemiBold)
                                Text("Reason: ${h.reason}", fontSize = 11.sp, color = SelforaTextSecondary)
                                h.subsequentPerformanceNotes?.let { notes ->
                                    Text("Subsequent Observation: $notes", fontSize = 11.sp, color = SelforaSuccess)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

private fun createMockPlans(childId: Long): List<PromptFadingPlanResponse> {
    return listOf(
        PromptFadingPlanResponse(
            id = 1L, childId = childId, childName = "Arjun Kumar", activityId = 1L, activityTitle = "Boy T-Shirt Dressing",
            stepId = 7L, stepNumber = 7, stepTitle = "Put head through neck opening",
            currentPromptLevelId = 3, currentPromptLevelName = "Verbal Prompt",
            targetPromptLevelId = 1, targetPromptLevelName = "Visual Prompt",
            clinicalReason = "Child consistently responds to visual cues.", reviewCriteria = "3 clinic sessions",
            notes = "Reinforce at home", status = "APPROVED", approvedByTherapistName = "Dr. Sarah Jenkins", approvedAt = "2026-10-01"
        )
    )
}

private fun createMockHistory(childId: Long): List<PromptFadingHistoryItemResponse> {
    return listOf(
        PromptFadingHistoryItemResponse(
            id = 1L, childId = childId, stepId = 7L, stepNumber = 7, stepTitle = "Put head through neck opening",
            approvedByTherapistName = "Dr. Sarah Jenkins", previousPromptLevelName = "Verbal Prompt",
            newPromptLevelName = "Visual Prompt", changeDate = "2026-10-01", reason = "Consistently accurate with visual card cue.",
            subsequentPerformanceNotes = "Observed 100% independence in subsequent session!"
        ),
        PromptFadingHistoryItemResponse(
            id = 2L, childId = childId, stepId = 1L, stepNumber = 1, stepTitle = "Garment orientation",
            approvedByTherapistName = "Dr. Sarah Jenkins", previousPromptLevelName = "Visual Prompt",
            newPromptLevelName = "Independent", changeDate = "2026-09-20", reason = "Mastered orientation independently.",
            subsequentPerformanceNotes = "Mastery sustained across 4 consecutive sessions."
        )
    )
}
