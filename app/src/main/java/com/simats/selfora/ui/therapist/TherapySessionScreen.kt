package com.simats.selfora.ui.therapist

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.*
import com.simats.selfora.ui.components.VisualStepGuidanceCard
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapySessionScreen(
    childId: Long,
    activityId: Long = 1L,
    onSessionCompleted: (sessionId: Long) -> Unit,
    onBack: () -> Unit
) {
    var activeSessionId by remember { mutableStateOf<Long?>(null) }
    var steps by remember { mutableStateOf<List<TaskStepResponse>>(emptyList()) }
    var currentStepIndex by remember { mutableStateOf(0) }

    // Map storing step recordings (stepIndex -> StepAssessmentState)
    val stepRecordingsMap = remember { mutableStateMapOf<Int, StepAssessmentState>() }

    var secondsElapsed by remember { mutableStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var showConfirmCompleteDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val promptHierarchy = listOf(
        Pair(0, "Level 0: Independent"),
        Pair(1, "Level 1: Visual Prompt"),
        Pair(2, "Level 2: Gesture"),
        Pair(3, "Level 3: Verbal Prompt"),
        Pair(4, "Level 4: Model / Video"),
        Pair(5, "Level 5: Partial Physical"),
        Pair(6, "Level 6: Full Physical")
    )

    // Live session timer
    LaunchedEffect(activeSessionId) {
        if (activeSessionId != null) {
            while (true) {
                delay(1000)
                secondsElapsed++
            }
        }
    }

    // Initialize live session and fetch activity steps
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val stepsRes = ApiClient.apiService.getTaskSteps(activityId)
            val loadedSteps = if (stepsRes.isSuccessful && !stepsRes.body().isNullOrEmpty()) stepsRes.body()!! else getSampleTShirtSteps()
            steps = loadedSteps

            // Initialize step states
            loadedSteps.forEachIndexed { idx, st ->
                stepRecordingsMap[idx] = StepAssessmentState(
                    stepId = st.id,
                    stepNumber = st.stepNumber,
                    title = st.title,
                    instruction = st.instructionText ?: st.childInstruction ?: ""
                )
            }

            val sessionRes = ApiClient.apiService.startSession(
                StartSessionRequest(childId = childId, activityId = activityId, sessionType = "CLINIC_THERAPY", environment = "CLINIC")
            )
            activeSessionId = if (sessionRes.isSuccessful && sessionRes.body() != null) sessionRes.body()!!.sessionId else 1L
        } catch (e: Exception) {
            val loadedSteps = getSampleTShirtSteps()
            steps = loadedSteps
            loadedSteps.forEachIndexed { idx, st ->
                stepRecordingsMap[idx] = StepAssessmentState(
                    stepId = st.id,
                    stepNumber = st.stepNumber,
                    title = st.title,
                    instruction = st.instructionText ?: st.childInstruction ?: ""
                )
            }
            activeSessionId = 1L
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "Live Therapy Session",
                subtitle = "Child ID: #$childId • Duration: ${secondsElapsed / 60}:${String.format("%02d", secondsElapsed % 60)}",
                onBackClick = onBack,
                accentColor = SelforaPrimary,
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SelforaWarning.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = SelforaWarning, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${secondsElapsed / 60}:${String.format("%02d", secondsElapsed % 60)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SelforaWarning
                            )
                        }
                    }
                }
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
            if (isLoading || steps.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SelforaPrimary)
                }
            } else {
                val currentStepObj = steps[currentStepIndex]
                val currentRecording = stepRecordingsMap[currentStepIndex] ?: StepAssessmentState(
                    stepId = currentStepObj.id,
                    stepNumber = currentStepObj.stepNumber,
                    title = currentStepObj.title,
                    instruction = currentStepObj.instructionText ?: ""
                )
                val totalSteps = steps.size

                // Session Progress Bar Header
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Boy T-Shirt Dressing Session", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaTextPrimary)
                        Surface(shape = RoundedCornerShape(12.dp), color = SelforaPrimary.copy(alpha = 0.15f)) {
                            Text("Step ${currentStepObj.stepNumber} of $totalSteps", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = (currentStepIndex + 1) / totalSteps.toFloat(),
                        color = SelforaPrimary,
                        trackColor = SelforaPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Visual Guidance Step Card
                VisualStepGuidanceCard(
                    stepNumber = currentStepObj.stepNumber,
                    totalSteps = totalSteps,
                    stepTitle = currentStepObj.title,
                    childInstruction = currentStepObj.instructionText ?: currentStepObj.childInstruction ?: "",
                    gender = "BOY",
                    onSpeakInstruction = {}
                )

                Spacer(modifier = Modifier.height(18.dp))

                // PROMPT HIERARCHY SELECTION MODULE (SECTION 4 & 5)
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White.copy(alpha = 0.95f),
                    borderColor = SelforaPrimary.copy(alpha = 0.4f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("OBSERVED ASSISTANCE / PROMPT LEVEL", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 7-Level Hierarchy Selection
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        promptHierarchy.forEach { (levelId, levelTitle) ->
                            val isSelected = currentRecording.outcome == "COMPLETED" && currentRecording.promptLevelId == levelId
                            val levelColor = getPromptHierarchyColor(levelTitle)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(if (isSelected) 4.dp else 1.dp, shape = RoundedCornerShape(14.dp))
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) levelColor else Color.White)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) SelforaPrimary else levelColor.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        currentRecording.promptLevelId = levelId
                                        currentRecording.outcome = "COMPLETED"
                                        stepRecordingsMap[currentStepIndex] = currentRecording
                                    }
                                    .padding(vertical = 12.dp, horizontal = 14.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            currentRecording.promptLevelId = levelId
                                            currentRecording.outcome = "COMPLETED"
                                            stepRecordingsMap[currentStepIndex] = currentRecording
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = if (isSelected) Color.White else SelforaPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = levelTitle,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else SelforaTextPrimary
                                    )
                                }
                            }
                        }

                        // Separate Options: Unable & Not Assessed
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val isUnableSelected = currentRecording.outcome == "UNABLE"
                            GlassOutlinedButton(
                                text = if (isUnableSelected) "✓ Unable" else "Unable",
                                accentColor = SelforaError,
                                onClick = {
                                    currentRecording.outcome = "UNABLE"
                                    currentRecording.promptLevelId = null
                                    stepRecordingsMap[currentStepIndex] = currentRecording
                                },
                                modifier = Modifier.weight(1f)
                            )

                            val isNotAssessedSelected = currentRecording.outcome == "NOT_ASSESSED"
                            GlassOutlinedButton(
                                text = if (isNotAssessedSelected) "✓ Not Assessed" else "Not Assessed",
                                accentColor = SelforaWarning,
                                onClick = {
                                    currentRecording.outcome = "NOT_ASSESSED"
                                    currentRecording.promptLevelId = null
                                    stepRecordingsMap[currentStepIndex] = currentRecording
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Therapist Observation Input
                    GlassTextField(
                        value = currentRecording.notes,
                        onValueChange = {
                            currentRecording.notes = it
                            stepRecordingsMap[currentStepIndex] = currentRecording
                        },
                        label = "Therapist Observation Notes",
                        placeholder = "Notes on prompt timing, motor coordination, or child behavior...",
                        singleLine = false,
                        leadingIcon = Icons.Default.EditNote
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Navigation Action Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (currentStepIndex > 0) {
                            GlassOutlinedButton(
                                text = "← Previous",
                                onClick = { currentStepIndex-- },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        GlassButton(
                            text = if (currentStepIndex == totalSteps - 1) "Finish Session ✓" else "Record & Next Step →",
                            onClick = {
                                scope.launch {
                                    isRecording = true
                                    try {
                                        activeSessionId?.let { sId ->
                                            ApiClient.apiService.recordPerformance(
                                                id = sId,
                                                request = RecordPerformanceRequest(
                                                    stepId = currentStepObj.id,
                                                    promptLevelId = currentRecording.promptLevelId ?: 0,
                                                    outcome = currentRecording.outcome,
                                                    durationSeconds = 15,
                                                    therapistNote = currentRecording.notes,
                                                    environment = "CLINIC"
                                                )
                                            )
                                        }
                                    } catch (_: Exception) {}

                                    if (currentStepIndex < totalSteps - 1) {
                                        currentStepIndex++
                                    } else {
                                        showConfirmCompleteDialog = true
                                    }
                                    isRecording = false
                                }
                            },
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }

    // Final Completion Confirmation Dialog
    if (showConfirmCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmCompleteDialog = false },
            title = { Text("Complete Therapy Session", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("All $steps.size steps recorded. Ready to save live therapy session records to backend database?", fontSize = 13.sp, color = SelforaTextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Session Duration: ${secondsElapsed / 60}m ${secondsElapsed % 60}s", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmCompleteDialog = false
                        scope.launch {
                            activeSessionId?.let { sId ->
                                try {
                                    ApiClient.apiService.completeSession(sId)
                                } catch (_: Exception) {}
                                onSessionCompleted(sId)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary)
                ) { Text("Save & Complete Session ✓") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmCompleteDialog = false }) { Text("Review Steps") }
            }
        )
    }
}
