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

enum class TherapySessionStage {
    SELECT_ACTIVITY,
    STEP_VIEW,
    RECORD_PERFORMANCE
}

data class ClinicActivityOption(
    val id: Long,
    val category: String,
    val title: String,
    val emoji: String,
    val totalSteps: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapySessionScreen(
    childId: Long,
    activityId: Long = 5L,
    onSessionCompleted: (sessionId: Long) -> Unit,
    onBack: () -> Unit
) {
    var stage by remember { mutableStateOf(TherapySessionStage.SELECT_ACTIVITY) }
    var selectedActivityId by remember { mutableStateOf(5L) } // Default Tooth Brushing (Image 1 Screen 5)
    
    val activityOptions = listOf(
        ClinicActivityOption(5L, "Grooming", "Tooth Brushing", "🪥", 7),
        ClinicActivityOption(1L, "Dressing", "Wearing T-Shirt", "👕", 7),
        ClinicActivityOption(13L, "Eating", "Eating with Spoon", "🥣", 7)
    )

    var activeSessionId by remember { mutableStateOf<Long?>(null) }
    var steps by remember { mutableStateOf<List<TaskStepResponse>>(emptyList()) }
    var currentStepIndex by remember { mutableStateOf(2) } // Step 3 of 7 in prototype! (0-indexed = 2)

    val stepRecordingsMap = remember { mutableStateMapOf<Int, StepAssessmentState>() }
    var secondsElapsed by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Live session timer
    LaunchedEffect(stage) {
        if (stage != TherapySessionStage.SELECT_ACTIVITY) {
            while (true) {
                delay(1000)
                secondsElapsed++
            }
        }
    }

    fun loadStepsForActivity(actId: Long) {
        isLoading = true
        scope.launch {
            try {
                val stepsRes = ApiClient.apiService.getTaskSteps(actId)
                val loadedSteps = if (stepsRes.isSuccessful && !stepsRes.body().isNullOrEmpty()) {
                    stepsRes.body()!!
                } else if (actId == 5L) {
                    getSampleToothBrushingSteps()
                } else {
                    getSampleTShirtSteps()
                }
                steps = loadedSteps

                loadedSteps.forEachIndexed { idx, st ->
                    if (!stepRecordingsMap.containsKey(idx)) {
                        stepRecordingsMap[idx] = StepAssessmentState(
                            stepId = st.id,
                            stepNumber = st.stepNumber,
                            title = st.title,
                            instruction = st.instructionText ?: st.childInstruction ?: ""
                        )
                    }
                }

                // If starting with tooth brushing, default to step index 2 (Step 3 of 7) like prototype mockup
                currentStepIndex = if (loadedSteps.size >= 3) 2 else 0

                val sessionRes = ApiClient.apiService.startSession(
                    StartSessionRequest(childId = childId, activityId = actId, sessionType = "CLINIC_THERAPY", environment = "CLINIC")
                )
                activeSessionId = if (sessionRes.isSuccessful && sessionRes.body() != null) sessionRes.body()!!.sessionId else 1L
            } catch (e: Exception) {
                val loadedSteps = if (actId == 5L) getSampleToothBrushingSteps() else getSampleTShirtSteps()
                steps = loadedSteps
                loadedSteps.forEachIndexed { idx, st ->
                    stepRecordingsMap[idx] = StepAssessmentState(
                        stepId = st.id,
                        stepNumber = st.stepNumber,
                        title = st.title,
                        instruction = st.instructionText ?: st.childInstruction ?: ""
                    )
                }
                currentStepIndex = if (loadedSteps.size >= 3) 2 else 0
                activeSessionId = 1L
            } finally {
                isLoading = false
            }
        }
    }

    val selectedOption = activityOptions.firstOrNull { it.id == selectedActivityId } ?: activityOptions.first()

    Scaffold(
        topBar = {
            GlassTopBar(
                title = when (stage) {
                    TherapySessionStage.SELECT_ACTIVITY -> "Start New Session"
                    TherapySessionStage.STEP_VIEW -> selectedOption.title
                    TherapySessionStage.RECORD_PERFORMANCE -> "Record Performance"
                },
                subtitle = when (stage) {
                    TherapySessionStage.SELECT_ACTIVITY -> "Select Activity"
                    TherapySessionStage.STEP_VIEW -> "Step ${currentStepIndex + 1} of ${steps.size}"
                    TherapySessionStage.RECORD_PERFORMANCE -> "Step ${currentStepIndex + 1} of ${steps.size}: ${steps.getOrNull(currentStepIndex)?.title ?: ""}"
                },
                onBackClick = {
                    when (stage) {
                        TherapySessionStage.SELECT_ACTIVITY -> onBack()
                        TherapySessionStage.STEP_VIEW -> {
                            if (currentStepIndex > 0) currentStepIndex-- else stage = TherapySessionStage.SELECT_ACTIVITY
                        }
                        TherapySessionStage.RECORD_PERFORMANCE -> stage = TherapySessionStage.STEP_VIEW
                    }
                },
                accentColor = SelforaPrimary,
                actions = {
                    if (stage != TherapySessionStage.SELECT_ACTIVITY) {
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
            when (stage) {
                // ==========================================
                // 1. SELECT ACTIVITY STAGE (Image 1 Screen 5)
                // ==========================================
                TherapySessionStage.SELECT_ACTIVITY -> {
                    Text(
                        text = "Select Activity",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelforaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        activityOptions.forEach { opt ->
                            val isSelected = opt.id == selectedActivityId

                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedActivityId = opt.id },
                                backgroundColor = if (isSelected) SelforaPrimary.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.85f),
                                borderColor = if (isSelected) SelforaPrimary else SelforaBorder.copy(alpha = 0.5f),
                                cornerRadius = 18.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) SelforaPrimary.copy(alpha = 0.15f) else SelforaBgLight,
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(opt.emoji, fontSize = 24.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column {
                                            Text(
                                                text = "${opt.category} (${opt.title})",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SelforaTextPrimary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${opt.totalSteps} structured ADL task steps",
                                                fontSize = 12.sp,
                                                color = SelforaTextSecondary
                                            )
                                        }
                                    }

                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedActivityId = opt.id },
                                        colors = RadioButtonDefaults.colors(selectedColor = SelforaPrimary)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    GlassButton(
                        text = "Next",
                        onClick = {
                            loadStepsForActivity(selectedActivityId)
                            stage = TherapySessionStage.STEP_VIEW
                        },
                        icon = Icons.Default.ArrowForward,
                        gradient = listOf(SelforaPrimary, Color(0xFF6D28D9)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )
                }

                // ==========================================
                // 2. STEP VIEW STAGE (Image 1 Screen 6)
                // ==========================================
                TherapySessionStage.STEP_VIEW -> {
                    if (isLoading || steps.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = SelforaPrimary)
                        }
                    } else {
                        val currentStepObj = steps[currentStepIndex]

                        Text(
                            text = "Step ${currentStepIndex + 1} of ${steps.size}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SelforaTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentStepObj.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SelforaTextPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Illustration / Guidance Card (Matching Tooth Brushing mockup)
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 24.dp
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = SelforaPrimary.copy(alpha = 0.12f),
                                    modifier = Modifier.size(130.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            when (selectedOption.id) {
                                                5L -> "🪥"
                                                13L -> "🥣"
                                                else -> "👕"
                                            },
                                            fontSize = 64.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = currentStepObj.instructionText ?: currentStepObj.childInstruction ?: "Move the toothbrush all around your teeth.",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SelforaTextPrimary,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // 7-Step Dots Indicator
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    steps.indices.forEach { idx ->
                                        Box(
                                            modifier = Modifier
                                                .size(if (idx == currentStepIndex) 10.dp else 8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (idx == currentStepIndex) SelforaPrimary else SelforaPrimary.copy(alpha = 0.25f)
                                                )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Back & Next Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlassOutlinedButton(
                                text = "Back",
                                onClick = {
                                    if (currentStepIndex > 0) currentStepIndex-- else stage = TherapySessionStage.SELECT_ACTIVITY
                                },
                                modifier = Modifier.weight(1f)
                            )

                            GlassButton(
                                text = "Next",
                                onClick = { stage = TherapySessionStage.RECORD_PERFORMANCE },
                                icon = Icons.Default.ArrowForward,
                                gradient = listOf(SelforaPrimary, Color(0xFF6D28D9)),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ==========================================
                // 3. RECORD PERFORMANCE STAGE (Image 1 Screen 7)
                // ==========================================
                TherapySessionStage.RECORD_PERFORMANCE -> {
                    if (steps.isNotEmpty()) {
                        val currentStepObj = steps[currentStepIndex]
                        val currentRecording = stepRecordingsMap[currentStepIndex] ?: StepAssessmentState(
                            stepId = currentStepObj.id,
                            stepNumber = currentStepObj.stepNumber,
                            title = currentStepObj.title,
                            instruction = currentStepObj.instructionText ?: ""
                        )

                        Text(
                            text = "Step ${currentStepIndex + 1} of ${steps.size}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SelforaTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentStepObj.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Level of Assistance",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val assistanceOptions = listOf(
                            Triple("Independent", "No help needed", SelforaSuccess),
                            Triple("Verbal Prompt", "Spoken reminder", Color(0xFFF59E0B)),
                            Triple("Visual Prompt", "Shown picture / gesture", SelforaPrimary),
                            Triple("Physical Assistance", "Helped physically", Color(0xFF8B5CF6)),
                            Triple("Unable", "Could not do", SelforaError)
                        )

                        // Default to Verbal Prompt if nothing chosen yet (matches prototype mockup Screen 7)
                        val selectedOutcomeName = when {
                            currentRecording.outcome == "UNABLE" -> "Unable"
                            currentRecording.promptLevelId == 0 -> "Independent"
                            currentRecording.promptLevelId == 3 -> "Verbal Prompt"
                            currentRecording.promptLevelId == 1 -> "Visual Prompt"
                            currentRecording.promptLevelId in listOf(5, 6) -> "Physical Assistance"
                            else -> "Verbal Prompt"
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            assistanceOptions.forEach { (title, subtitle, color) ->
                                val isSelected = selectedOutcomeName == title

                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            when (title) {
                                                "Independent" -> {
                                                    currentRecording.promptLevelId = 0
                                                    currentRecording.outcome = "COMPLETED"
                                                }
                                                "Verbal Prompt" -> {
                                                    currentRecording.promptLevelId = 3
                                                    currentRecording.outcome = "COMPLETED"
                                                }
                                                "Visual Prompt" -> {
                                                    currentRecording.promptLevelId = 1
                                                    currentRecording.outcome = "COMPLETED"
                                                }
                                                "Physical Assistance" -> {
                                                    currentRecording.promptLevelId = 5
                                                    currentRecording.outcome = "COMPLETED"
                                                }
                                                "Unable" -> {
                                                    currentRecording.promptLevelId = null
                                                    currentRecording.outcome = "UNABLE"
                                                }
                                            }
                                            stepRecordingsMap[currentStepIndex] = currentRecording
                                        },
                                    backgroundColor = if (isSelected) color.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.9f),
                                    borderColor = if (isSelected) color else SelforaBorder.copy(alpha = 0.5f),
                                    cornerRadius = 16.dp
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = CircleShape,
                                                color = color.copy(alpha = 0.15f),
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        when (title) {
                                                            "Independent" -> Icons.Default.CheckCircle
                                                            "Verbal Prompt" -> Icons.Default.RecordVoiceOver
                                                            "Visual Prompt" -> Icons.Default.Visibility
                                                            "Physical Assistance" -> Icons.Default.PanTool
                                                            else -> Icons.Default.Cancel
                                                        },
                                                        contentDescription = null,
                                                        tint = color,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = title,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SelforaTextPrimary
                                                )
                                                Text(
                                                    text = subtitle,
                                                    fontSize = 12.sp,
                                                    color = SelforaTextSecondary
                                                )
                                            }
                                        }

                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                when (title) {
                                                    "Independent" -> {
                                                        currentRecording.promptLevelId = 0
                                                        currentRecording.outcome = "COMPLETED"
                                                    }
                                                    "Verbal Prompt" -> {
                                                        currentRecording.promptLevelId = 3
                                                        currentRecording.outcome = "COMPLETED"
                                                    }
                                                    "Visual Prompt" -> {
                                                        currentRecording.promptLevelId = 1
                                                        currentRecording.outcome = "COMPLETED"
                                                    }
                                                    "Physical Assistance" -> {
                                                        currentRecording.promptLevelId = 5
                                                        currentRecording.outcome = "COMPLETED"
                                                    }
                                                    "Unable" -> {
                                                        currentRecording.promptLevelId = null
                                                        currentRecording.outcome = "UNABLE"
                                                    }
                                                }
                                                stepRecordingsMap[currentStepIndex] = currentRecording
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = color)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        GlassButton(
                            text = if (currentStepIndex == steps.size - 1) "Save & Finish Session ✓" else "Save & Next",
                            onClick = {
                                scope.launch {
                                    try {
                                        activeSessionId?.let { sId ->
                                            ApiClient.apiService.recordPerformance(
                                                id = sId,
                                                request = RecordPerformanceRequest(
                                                    stepId = currentStepObj.id,
                                                    promptLevelId = currentRecording.promptLevelId ?: 3,
                                                    outcome = currentRecording.outcome,
                                                    durationSeconds = 15,
                                                    therapistNote = currentRecording.notes,
                                                    environment = "CLINIC"
                                                )
                                            )
                                        }
                                    } catch (_: Exception) {}

                                    if (currentStepIndex < steps.size - 1) {
                                        currentStepIndex++
                                        stage = TherapySessionStage.STEP_VIEW
                                    } else {
                                        // Complete session and advance to summary screen
                                        activeSessionId?.let { sId ->
                                            try {
                                                ApiClient.apiService.completeSession(sId)
                                            } catch (_: Exception) {}
                                            onSessionCompleted(sId)
                                        } ?: onSessionCompleted(1L)
                                    }
                                }
                            },
                            icon = Icons.Default.Check,
                            gradient = listOf(SelforaPrimary, Color(0xFF6D28D9)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}
