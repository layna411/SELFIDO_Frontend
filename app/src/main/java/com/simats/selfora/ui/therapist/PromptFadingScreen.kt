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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

    data class AdlStepItem(
        val id: Long,
        val activityId: Long,
        val activityName: String,
        val stepNumber: Int,
        val title: String,
        val shortTitle: String
    )

    val supportedSteps = remember {
        listOf(
            AdlStepItem(1L, 1L, "Dressing", 1, "Stand or sit in front of the T-shirt", "Positioning"),
            AdlStepItem(4L, 1L, "Dressing", 4, "Grasp shirt hem with both hands", "Grasp Hem"),
            AdlStepItem(7L, 1L, "Dressing", 7, "Put head through neck opening", "Head Through"),
            AdlStepItem(11L, 1L, "Dressing", 11, "Push right arm through sleeve", "Right Arm"),
            AdlStepItem(14L, 1L, "Dressing", 14, "Push left arm through sleeve", "Left Arm"),
            AdlStepItem(18L, 1L, "Dressing", 18, "Pull shirt hem down to waist", "Pull to Waist"),
            AdlStepItem(131L, 13L, "Eating", 1, "Look at the spoon and bowl", "Focus on Spoon"),
            AdlStepItem(133L, 13L, "Eating", 3, "Hold spoon handle with preferred hand", "Hold Handle"),
            AdlStepItem(136L, 13L, "Eating", 6, "Scoop food into spoon bowl", "Scoop Food"),
            AdlStepItem(138L, 13L, "Eating", 8, "Bring spoon to mouth and eat", "Spoon to Mouth"),
            AdlStepItem(91L, 9L, "Shoes & Socks", 1, "Look at shoes and socks", "Focus on Shoes"),
            AdlStepItem(92L, 9L, "Shoes & Socks", 2, "Grasp sock collar with both hands", "Grasp Sock"),
            AdlStepItem(95L, 9L, "Shoes & Socks", 5, "Pull sock over toes and heel", "Pull Sock"),
            AdlStepItem(98L, 9L, "Shoes & Socks", 8, "Insert foot into shoe", "Insert Foot")
        )
    }

    // Fading Plan Form State
    var selectedActivityId by remember { mutableStateOf(1L) }
    var selectedStepId by remember { mutableStateOf(7L) }
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
                    fadingPlans = emptyList()
                }

                val histRes = ApiClient.apiService.getPromptFadingHistoryForChild(childId)
                if (histRes.isSuccessful && histRes.body() != null) {
                    fadingHistory = histRes.body()!!
                } else {
                    fadingHistory = emptyList()
                }
            } catch (e: Exception) {
                fadingPlans = emptyList()
                fadingHistory = emptyList()
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
                subtitle = "Child ID: #$childId • Clinical Fading Hub",
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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 1. ASSISTANCE HIERARCHY LEGEND CARD
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("CLINICAL ASSISTANCE HIERARCHY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Structured fading progression (Level 6 -> Level 0). 'Unable' is tracked separately.", fontSize = 11.sp, color = SelforaTextSecondary)
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    promptHierarchy.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { (_, pair) ->
                                val (label, color) = pair
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = color.copy(alpha = 0.08f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(shape = CircleShape, color = color, modifier = Modifier.size(8.dp)) {}
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SelforaTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // Unable / Refused Special Outcome
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = PromptLevelUnable.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PromptLevelUnable.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = CircleShape, color = PromptLevelUnable, modifier = Modifier.size(8.dp)) {}
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Unable / Refused (Separate Outcome – NOT a prompt level)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PromptLevelUnable
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. FADING PLAN CREATOR & APPROVAL WORKFLOW
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("CREATE & REVIEW PROMPT FADING PLAN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                Text("Therapist-controlled decision engine to fade or restore assistance levels", fontSize = 11.sp, color = SelforaTextSecondary)
                Spacer(modifier = Modifier.height(14.dp))

                // ADL Category Selection (Excluding Grooming)
                Text("Select ADL Activity:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("👕 Dressing" to 1L, "🥄 Eating" to 13L, "👟 Shoes & Socks" to 9L).forEach { (label, aId) ->
                        val isSelected = selectedActivityId == aId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) SelforaPrimary else Color.White)
                                .border(1.dp, if (isSelected) SelforaPrimary else SelforaBorder, RoundedCornerShape(14.dp))
                                .clickable {
                                    selectedActivityId = aId
                                    val firstStep = supportedSteps.firstOrNull { it.activityId == aId }
                                    if (firstStep != null) {
                                        selectedStepId = firstStep.id
                                        selectedStepNumber = firstStep.stepNumber
                                        selectedStepTitle = firstStep.title
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else SelforaTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Step Quick Picker
                Text("Select Target Task Step:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    supportedSteps.filter { it.activityId == selectedActivityId }.forEach { s ->
                        val isSelected = selectedStepId == s.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SelforaPrimary else Color.White)
                                .border(1.dp, if (isSelected) SelforaPrimary else SelforaBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedStepId = s.id
                                    selectedStepNumber = s.stepNumber
                                    selectedStepTitle = s.title
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Step ${s.stepNumber}: ${s.shortTitle}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else SelforaTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

               // Select Target Prompt Chip Selector
                Text("Select Proposed Target Prompt Level:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0 to "Indep (L0)", 1 to "Visual (L1)", 2 to "Gesture (L2)", 3 to "Verbal (L3)", 5 to "Partial (L5)").forEach { (lvl, lbl) ->
                        val isSelected = targetLevelId == lvl
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SelforaPrimary else Color.White)
                                .border(1.dp, if (isSelected) SelforaPrimary else SelforaBorder, RoundedCornerShape(12.dp))
                                .clickable { targetLevelId = lvl }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = lbl,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else SelforaTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                GlassTextField(
                    value = clinicalReasonText,
                    onValueChange = { clinicalReasonText = it },
                    label = "Clinical Rationale for Fading / Restoring Level",
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                GlassTextField(
                    value = reviewCriteriaText,
                    onValueChange = { reviewCriteriaText = it },
                    label = "Review Criteria (e.g. 3 consecutive sessions)",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassButton(
                        text = "Approve Fading Plan",
                        onClick = {
                            isSaving = true
                            scope.launch {
                                try {
                                    val req = CreatePromptFadingPlanRequest(
                                        childId = childId,
                                        activityId = selectedActivityId,
                                        stepId = selectedStepId,
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

            // 3. ACTIVE PROMPT FADING PLANS
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingDown, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ACTIVE PROMPT FADING PLANS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                }
                Text("Current step-wise fading targets for this child", fontSize = 11.sp, color = SelforaTextSecondary)
                Spacer(modifier = Modifier.height(14.dp))

                if (fadingPlans.isEmpty()) {
                    Text("No active prompt fading plans for this child.", fontSize = 12.sp, color = SelforaTextMuted)
                } else {
                    fadingPlans.forEach { p ->
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
                                    Text(
                                        "Step ${p.stepNumber}: ${p.stepTitle}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SelforaTextPrimary,
                                        modifier = Modifier.weight(1f).padding(end = 6.dp)
                                    )
                                    Surface(shape = RoundedCornerShape(6.dp), color = SelforaPrimary.copy(alpha = 0.15f)) {
                                        Text(p.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = SelforaOrangeWarning.copy(alpha = 0.15f)) {
                                        Text(p.currentPromptLevelName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SelforaOrangeWarning, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    Text(" ➔ ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                                    Surface(shape = RoundedCornerShape(6.dp), color = SelforaGreenSuccess.copy(alpha = 0.15f)) {
                                        Text(p.targetPromptLevelName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SelforaGreenSuccess, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                if (!p.clinicalReason.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Rationale: ${p.clinicalReason}", fontSize = 11.sp, color = SelforaTextSecondary)
                                }
                                if (!p.reviewCriteria.isNullOrBlank()) {
                                    Text("Criteria: ${p.reviewCriteria}", fontSize = 11.sp, color = SelforaTextSecondary)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. PROMPT FADING HISTORY TIMELINE (AUDIT TRAIL)
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
                                    Text(
                                        "Step ${h.stepNumber}: ${h.stepTitle}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SelforaTextPrimary,
                                        modifier = Modifier.weight(1f).padding(end = 6.dp)
                                    )
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
