package com.simats.selfora.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
fun HomeProgramReviewScreen(
    childId: Long = 1L,
    onBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var pendingReviews by remember { mutableStateOf<List<CaregiverPracticeSessionResponse>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf("Pending Review") }
    var selectedSession by remember { mutableStateOf<CaregiverPracticeSessionResponse?>(null) }
    var feedbackText by remember { mutableStateOf("Great progress on step 7! Keep encouraging independent head insertion.") }
    var isFlagged by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    fun loadReviews() {
        isLoading = true
        scope.launch {
            try {
                val res = ApiClient.apiService.getPendingReviews()
                if (res.isSuccessful && res.body() != null) {
                    pendingReviews = res.body()!!
                } else {
                    pendingReviews = createMockReviews(childId)
                }
            } catch (e: Exception) {
                pendingReviews = createMockReviews(childId)
            } finally {
                if (pendingReviews.isNotEmpty()) {
                    selectedSession = pendingReviews.first()
                }
                isLoading = false
            }
        }
    }

    LaunchedEffect(childId) {
        loadReviews()
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "Home Practice Review Hub",
                subtitle = "Evaluate Caregiver Practice Logs & Feedback",
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
            // Filter Selector
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Pending Review", "All Submissions", "Reviewed").forEach { f ->
                    GlassChip(
                        text = f,
                        selected = selectedFilter == f,
                        onClick = { selectedFilter = f },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SelforaPrimary)
                }
            } else if (pendingReviews.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SelforaSuccess, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("All Practice Submissions Reviewed!", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                            Text("No pending caregiver submissions requiring feedback.", fontSize = 12.sp, color = SelforaTextSecondary)
                        }
                    }
                }
            } else {
                // List of Pending Submissions
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("SELECT CAREGIVER SUBMISSION TO REVIEW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    pendingReviews.forEach { sess ->
                        val isSelected = selectedSession?.id == sess.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) SelforaBlueLight else Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) SelforaPrimary else SelforaBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSession = sess }
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(sess.childName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                    Text("Caregiver: ${sess.caregiverName} • ${sess.activityTitle}", fontSize = 11.sp, color = SelforaTextSecondary)
                                    Text("Date: ${sess.practiceDate} • Duration: ${sess.durationMinutes}m", fontSize = 10.sp, color = SelforaPrimary)
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (sess.status == "REVIEWED") SelforaSuccess.copy(alpha = 0.15f) else SelforaWarning.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        sess.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sess.status == "REVIEWED") SelforaSuccess else SelforaWarning,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detail Review Card for Selected Session
                selectedSession?.let { sess ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("SUBMISSION DETAILS & STEP OUTCOMES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Child: ${sess.childName} • Caregiver: ${sess.caregiverName}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                        Text("Programme: ${sess.homeProgramTitle}", fontSize = 12.sp, color = SelforaPrimary)
                        Text("Submitted At: ${sess.submittedAt}", fontSize = 10.sp, color = SelforaTextMuted)

                        sess.caregiverNotes?.let { notes ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(shape = RoundedCornerShape(10.dp), color = SelforaSecondary.copy(alpha = 0.08f), modifier = Modifier.fillMaxWidth()) {
                                Text("Caregiver Notes: \"$notes\"", fontSize = 12.sp, color = SelforaTextPrimary, modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Caregiver-Reported Step Outcomes:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))

                        sess.stepResults.forEach { step ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Step ${step.stepNumber}: ${step.stepTitle}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                    if (!step.observation.isNullOrEmpty()) {
                                        Text("Observation: ${step.observation}", fontSize = 10.sp, color = SelforaTextSecondary)
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (step.outcome) {
                                        "INDEPENDENT" -> SelforaSuccess.copy(alpha = 0.15f)
                                        "LITTLE_HELP" -> SelforaPrimary.copy(alpha = 0.15f)
                                        "LOT_HELP" -> SelforaWarning.copy(alpha = 0.15f)
                                        else -> SelforaError.copy(alpha = 0.15f)
                                    }
                                ) {
                                    Text(
                                        when (step.outcome) {
                                            "INDEPENDENT" -> "Indep"
                                            "LITTLE_HELP" -> "Little Help"
                                            "LOT_HELP" -> "Lot Help"
                                            "COULD_NOT_COMPLETE" -> "Could Not"
                                            else -> "Not Practised"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (step.outcome) {
                                            "INDEPENDENT" -> SelforaSuccess
                                            "LITTLE_HELP" -> SelforaPrimary
                                            "LOT_HELP" -> SelforaWarning
                                            else -> SelforaError
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = SelforaBorder.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Therapist Feedback Editor
                        Text("ADD THERAPIST FEEDBACK FOR CAREGIVER", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaSecondary)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = feedbackText,
                            onValueChange = { feedbackText = it },
                            label = { Text("Therapist Feedback & Encouragement") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isFlagged, onCheckedChange = { isFlagged = it })
                            Text("Flag session for clinical follow-up during next clinic visit", fontSize = 11.sp, color = SelforaTextSecondary)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        GlassButton(
                            text = "MARK AS REVIEWED & SEND FEEDBACK",
                            onClick = {
                                isSaving = true
                                scope.launch {
                                    try {
                                        val req = TherapistReviewRequest(feedbackText, "REVIEWED", isFlagged)
                                        ApiClient.apiService.reviewPracticeSession(sess.id, req)
                                    } catch (_: Exception) {}
                                    isSaving = false
                                    loadReviews()
                                }
                            },
                            icon = Icons.Default.Check,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

private fun createMockReviews(childId: Long): List<CaregiverPracticeSessionResponse> {
    return listOf(
        CaregiverPracticeSessionResponse(
            id = 101L,
            homeProgramId = 1L,
            homeProgramTitle = "Boy T-Shirt Dressing Practice",
            childId = childId,
            childName = if (childId == 2L) "Ananya Reddy" else "Arjun Kumar",
            caregiverId = 201L,
            caregiverName = "Priya Kumar",
            activityId = 1L,
            activityTitle = "Boy T-Shirt Dressing",
            practiceDate = java.time.LocalDate.now().toString(),
            durationMinutes = 15,
            caregiverNotes = "Arjun attempted step 7 independently on Monday! Needed a little help with the right sleeve on Thursday.",
            status = "SUBMITTED",
            submittedAt = java.time.LocalDateTime.now().toString(),
            reviewingTherapistName = null,
            reviewedAt = null,
            therapistFeedback = null,
            flaggedForFollowup = false,
            stepResults = listOf(
                com.simats.selfora.data.model.StepResultItemDto(1L, 1, "Orient clothing front/back", "INDEPENDENT", "Completed with ease."),
                com.simats.selfora.data.model.StepResultItemDto(4L, 4, "Right sleeve insertion", "LITTLE_HELP", "Hesitated slightly before inserting arm."),
                com.simats.selfora.data.model.StepResultItemDto(7L, 7, "Head insertion", "INDEPENDENT", "Gazed at visual card and popped head through!"),
                com.simats.selfora.data.model.StepResultItemDto(10L, 10, "Hem pull down", "LOT_HELP", "Caregiver guided hands to hem.")
            )
        )
    )
}

