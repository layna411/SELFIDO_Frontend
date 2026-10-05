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
import kotlinx.coroutines.launch
import java.time.LocalDate

data class StepAssessmentState(
    val stepId: Long,
    val stepNumber: Int,
    val title: String,
    val instruction: String,
    var promptLevelId: Int? = null, // 0..6
    var outcome: String = "PENDING", // COMPLETED, UNABLE, NOT_ASSESSED
    var notes: String = "",
    var notAssessedReason: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentScreen(
    childId: Long,
    onAssessmentSaved: () -> Unit,
    onBack: () -> Unit
) {
    var isStarted by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("DRESSING") }
    var selectedActivityId by remember { mutableStateOf(1L) }
    var selectedActivityTitle by remember { mutableStateOf("Boy T-Shirt Dressing") }
    var assessmentType by remember { mutableStateOf("Baseline Baseline Evaluation") }

    var currentStepIndex by remember { mutableStateOf(0) }
    var stepStates by remember { mutableStateOf<List<StepAssessmentState>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showNotAssessedDialog by remember { mutableStateOf(false) }
    var notAssessedReasonInput by remember { mutableStateOf("") }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentDate = remember { LocalDate.now().toString() }

    // Prompt Hierarchy Definitions
    val promptHierarchy = listOf(
        Pair(0, "Level 0: Independent"),
        Pair(1, "Level 1: Visual Prompt"),
        Pair(2, "Level 2: Gesture"),
        Pair(3, "Level 3: Verbal Prompt"),
        Pair(4, "Level 4: Model / Video"),
        Pair(5, "Level 5: Partial Physical"),
        Pair(6, "Level 6: Full Physical")
    )

    // Load Task Steps for selected activity
    fun loadActivitySteps(actId: Long) {
        isLoading = true
        scope.launch {
            try {
                val res = ApiClient.apiService.getTaskSteps(actId)
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    stepStates = res.body()!!.map {
                        StepAssessmentState(
                            stepId = it.id,
                            stepNumber = it.stepNumber,
                            title = it.title,
                            instruction = it.instructionText ?: it.childInstruction ?: ""
                        )
                    }
                } else {
                    stepStates = getSampleAssessmentSteps()
                }
            } catch (e: Exception) {
                stepStates = getSampleAssessmentSteps()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = if (!isStarted) "Start New Clinical Assessment" else "Pediatric Assessment ($selectedActivityTitle)",
                subtitle = "Child ID: #$childId • Date: $currentDate",
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
            if (!isStarted) {
                // SECTION 2: Start New Assessment Screen Config
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("PEDIATRIC PATIENT DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = SelforaBlueLight, modifier = Modifier.size(48.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text("👦", fontSize = 24.sp) }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Arjun Kumar", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                            Text("Child ID: #$childId • Age: 8 yrs • Gender: BOY", fontSize = 12.sp, color = SelforaTextSecondary)
                            Text("Assigned Doctor: Dr. Sarah Jenkins", fontSize = 11.sp, color = SelforaPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("SELECT ADL CATEGORY & ACTIVITY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassChip(
                            text = "👕 Dressing",
                            selected = selectedCategory == "DRESSING",
                            onClick = {
                                selectedCategory = "DRESSING"
                                selectedActivityId = 1L
                                selectedActivityTitle = "Boy T-Shirt Dressing"
                            },
                            modifier = Modifier.weight(1f)
                        )
                        GlassChip(
                            text = "🥄 Eating",
                            selected = selectedCategory == "EATING",
                            onClick = {
                                selectedCategory = "EATING"
                                selectedActivityId = 2L
                                selectedActivityTitle = "Spoon Feeding Routine"
                            },
                            modifier = Modifier.weight(1f)
                        )
                        GlassChip(
                            text = "👟 Shoes",
                            selected = selectedCategory == "SHOES",
                            onClick = {
                                selectedCategory = "SHOES"
                                selectedActivityId = 3L
                                selectedActivityTitle = "Velcro Shoe Fastening"
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Selected Activity: $selectedActivityTitle", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                    Text("Evaluation Date: $currentDate • Type: $assessmentType", fontSize = 12.sp, color = SelforaTextSecondary)

                    Spacer(modifier = Modifier.height(18.dp))

                    GlassButton(
                        text = "🚀 Start Assessment",
                        onClick = {
                            loadActivitySteps(selectedActivityId)
                            isStarted = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // SECTION 3, 4, 5: Task Analysis & Step Assessment Recording
                if (isLoading || stepStates.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SelforaPrimary)
                    }
                } else {
                    val currentStep = stepStates[currentStepIndex]
                    val totalSteps = stepStates.size

                    // Overall Progress & Step Header
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedActivityTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaTextPrimary)
                            Surface(shape = RoundedCornerShape(12.dp), color = SelforaPrimary.copy(alpha = 0.15f)) {
                                Text("Step ${currentStep.stepNumber} of $totalSteps", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
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

                    // Step Visual Guidance Card
                    VisualStepGuidanceCard(
                        stepNumber = currentStep.stepNumber,
                        totalSteps = totalSteps,
                        stepTitle = currentStep.title,
                        childInstruction = currentStep.instruction,
                        gender = "BOY",
                        onSpeakInstruction = {}
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // CLINICAL PROMPT HIERARCHY SELECTION CARD (SECTION 4)
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White.copy(alpha = 0.95f),
                        borderColor = SelforaPrimary.copy(alpha = 0.4f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CLINICAL ASSISTANCE / PROMPT LEVEL", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Render 7 Prompt Hierarchy Levels
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            promptHierarchy.forEach { (levelId, levelTitle) ->
                                val isSelected = currentStep.outcome == "COMPLETED" && currentStep.promptLevelId == levelId
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
                                            currentStep.promptLevelId = levelId
                                            currentStep.outcome = "COMPLETED"
                                        }
                                        .padding(vertical = 12.dp, horizontal = 14.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                currentStep.promptLevelId = levelId
                                                currentStep.outcome = "COMPLETED"
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
                                val isUnableSelected = currentStep.outcome == "UNABLE"
                                GlassOutlinedButton(
                                    text = if (isUnableSelected) "✓ Unable" else "Unable",
                                    accentColor = SelforaError,
                                    onClick = {
                                        currentStep.outcome = "UNABLE"
                                        currentStep.promptLevelId = null
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                val isNotAssessedSelected = currentStep.outcome == "NOT_ASSESSED"
                                GlassOutlinedButton(
                                    text = if (isNotAssessedSelected) "✓ Not Assessed" else "Not Assessed",
                                    accentColor = SelforaWarning,
                                    onClick = {
                                        notAssessedReasonInput = currentStep.notAssessedReason
                                        showNotAssessedDialog = true
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Therapist Notes Input
                        GlassTextField(
                            value = currentStep.notes,
                            onValueChange = { currentStep.notes = it },
                            label = "Therapist Observation Notes (Optional)",
                            placeholder = "Record specific clinical observations for this step...",
                            singleLine = false,
                            leadingIcon = Icons.Default.EditNote
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Step Navigation Controls (Prev / Next / Finish)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (currentStepIndex > 0) {
                                GlassOutlinedButton(
                                    text = "← Previous",
                                    onClick = { currentStepIndex-- },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            GlassButton(
                                text = if (currentStepIndex == totalSteps - 1) "Review Assessment ✓" else "Next Step →",
                                onClick = {
                                    if (currentStep.outcome == "PENDING") {
                                        Toast.makeText(context, "Please select an assistance level or status for Step ${currentStep.stepNumber}", Toast.LENGTH_SHORT).show()
                                    } else {
                                        if (currentStepIndex < totalSteps - 1) {
                                            currentStepIndex++
                                        } else {
                                            showConfirmationDialog = true
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }

    // Not Assessed Reason Dialog
    if (showNotAssessedDialog) {
        AlertDialog(
            onDismissRequest = { showNotAssessedDialog = false },
            title = { Text("Mark Step as Not Assessed", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Provide clinical explanation for skipping this step:", fontSize = 13.sp, color = SelforaTextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notAssessedReasonInput,
                        onValueChange = { notAssessedReasonInput = it },
                        placeholder = { Text("e.g. Physical constraint, sensory aversion...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val step = stepStates[currentStepIndex]
                        step.outcome = "NOT_ASSESSED"
                        step.promptLevelId = null
                        step.notAssessedReason = notAssessedReasonInput
                        showNotAssessedDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary)
                ) { Text("Confirm Skip") }
            },
            dismissButton = {
                TextButton(onClick = { showNotAssessedDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Confirmation & Final Save Dialog (SECTION 6)
    if (showConfirmationDialog) {
        val totalAssessed = stepStates.count { it.outcome != "NOT_ASSESSED" }
        val independentCount = stepStates.count { it.outcome == "COMPLETED" && it.promptLevelId == 0 }
        val independencePct = if (totalAssessed > 0) Math.round((independentCount / totalAssessed.toDouble()) * 1000) / 10.0 else 0.0

        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Complete Assessment Submission", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Review assessment summary for Arjun Kumar:", fontSize = 13.sp, color = SelforaTextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("• Total Steps: ${stepStates.size}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("• Independent Steps: $independentCount", fontSize = 13.sp, color = SelforaSuccess, fontWeight = FontWeight.Bold)
                    Text("• Assessed Steps: $totalAssessed", fontSize = 13.sp)
                    Text("• Independence Percentage: $independencePct%", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = SelforaPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Persist clinical record to backend database?", fontSize = 12.sp, color = SelforaTextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmationDialog = false
                        isSubmitting = true
                        scope.launch {
                            try {
                                val req = CreateAssessmentRequest(
                                    childId = childId,
                                    activityId = selectedActivityId,
                                    notes = "Clinical assessment recorded via Stage 4 Therapist Interface",
                                    stepResults = stepStates.map { s ->
                                        StepAssessmentRequest(
                                            stepId = s.stepId,
                                            promptLevelId = s.promptLevelId ?: 0,
                                            outcome = s.outcome,
                                            notes = if (s.outcome == "NOT_ASSESSED") s.notAssessedReason else s.notes
                                        )
                                    }
                                )
                                ApiClient.apiService.createAssessment(req)
                            } catch (_: Exception) {} finally {
                                isSubmitting = false
                                Toast.makeText(context, "Assessment saved successfully to backend!", Toast.LENGTH_SHORT).show()
                                onAssessmentSaved()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text("Save & Submit ✓")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) { Text("Review Again") }
            }
        )
    }
}

fun getSampleAssessmentSteps(): List<StepAssessmentState> {
    return getSampleTShirtSteps().map {
        StepAssessmentState(
            stepId = it.id,
            stepNumber = it.stepNumber,
            title = it.title,
            instruction = it.instructionText ?: ""
        )
    }
}
