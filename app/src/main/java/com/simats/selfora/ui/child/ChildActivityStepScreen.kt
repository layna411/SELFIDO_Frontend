package com.simats.selfora.ui.child

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.local.SessionManager
import com.simats.selfora.data.model.TaskStepResponse
import com.simats.selfora.data.repository.PracticeSessionStore
import com.simats.selfora.ui.components.VisualStepGuidanceCard
import com.simats.selfora.ui.theme.*
import com.simats.selfora.ui.therapist.getSampleEatingSteps
import com.simats.selfora.ui.therapist.getSampleGirlFrockSteps
import com.simats.selfora.ui.therapist.getSampleShoesSteps
import com.simats.selfora.ui.therapist.getSampleTShirtSteps
import com.simats.selfora.utils.TtsManager
import kotlinx.coroutines.launch

@Composable
fun ChildActivityStepScreen(
    activityId: Long = 1L,
    onFinished: () -> Unit,
    onBack: () -> Unit
) {
    var steps by remember { mutableStateOf<List<TaskStepResponse>>(emptyList()) }
    var currentStepIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }
    val gender = remember { SessionManager.getActiveChildGender().ifBlank { "BOY" } }
    val activeChildId = remember { SessionManager.getActiveChildId() }

    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
        }
    }

    LaunchedEffect(activityId) {
        isLoading = true
        PracticeSessionStore.init(context)
        val fallbackSteps = when (activityId) {
            13L -> getSampleEatingSteps()
            9L -> getSampleShoesSteps()
            2L -> getSampleGirlFrockSteps()
            else -> getSampleTShirtSteps()
        }
        try {
            val res = ApiClient.apiService.getTaskSteps(activityId)
            steps = if (res.isSuccessful && res.body() != null && res.body()!!.isNotEmpty()) res.body()!! else fallbackSteps
            
            // Durable Session Recovery: check if saved progress exists for this child & activity
            val savedSession = PracticeSessionStore.getActivePracticeSession(activeChildId, activityId)
            if (savedSession != null && savedSession.currentStepIndex in steps.indices) {
                currentStepIndex = savedSession.currentStepIndex
            }
        } catch (e: Exception) {
            steps = fallbackSteps
            val savedSession = PracticeSessionStore.getActivePracticeSession(activeChildId, activityId)
            if (savedSession != null && savedSession.currentStepIndex in steps.indices) {
                currentStepIndex = savedSession.currentStepIndex
            }
        } finally {
            isLoading = false
        }
    }

    // Auto-save active progress on step change and auto-speak instruction
    LaunchedEffect(currentStepIndex, steps) {
        if (steps.isNotEmpty() && currentStepIndex in steps.indices) {
            val instruction = steps[currentStepIndex].childInstruction
            ttsManager.speak(instruction)

            if (activeChildId > 0) {
                PracticeSessionStore.saveActivePracticeSession(
                    com.simats.selfora.data.repository.ActivePracticeSession(
                        childId = activeChildId,
                        activityId = activityId,
                        currentStepIndex = currentStepIndex,
                        completedSteps = currentStepIndex,
                        totalSteps = steps.size,
                        sessionStatus = "IN_PROGRESS"
                    )
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChildBlueCard)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        if (isLoading || steps.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            val step = steps[currentStepIndex]

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar (Back button, ActivityStepProgress & Audio)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SelforaBlueDark)
                    }

                    Box(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        com.simats.selfora.ui.components.progress.ActivityStepProgress(
                            currentStep = currentStepIndex + 1,
                            totalSteps = steps.size
                        )
                    }

                    IconButton(onClick = { ttsManager.speak(step.childInstruction) }) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Replay Audio", tint = SelforaPrimary)
                    }
                }

                // Visual Step Guidance Card
                VisualStepGuidanceCard(
                    stepNumber = currentStepIndex + 1,
                    totalSteps = steps.size,
                    stepTitle = step.title,
                    childInstruction = step.childInstruction,
                    gender = gender,
                    onSpeakInstruction = { ttsManager.speak(step.childInstruction) }
                )

                // Navigation Touch Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStepIndex > 0) {
                        Button(
                            onClick = { currentStepIndex-- },
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = SelforaBlueDark)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Previous", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    IconButton(
                        onClick = { ttsManager.speak(step.childInstruction) },
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Replay", tint = SelforaPrimary)
                    }

                    Button(
                        onClick = {
                            if (isSubmitting) return@Button
                            val isCompleted = currentStepIndex == steps.size - 1

                            if (isCompleted) {
                                isSubmitting = true
                                submitError = null
                                scope.launch {
                                    if (activeChildId > 0) {
                                        try {
                                            val response = ApiClient.apiService.recordChildStepProgress(
                                                com.simats.selfora.data.model.RecordStepProgressRequest(
                                                    childId = activeChildId,
                                                    activityId = activityId,
                                                    stepNumber = steps.size,
                                                    isActivityCompleted = true
                                                )
                                            )
                                            if (response.isSuccessful && response.body() != null) {
                                                PracticeSessionStore.setLastStepProgress(response.body())
                                                PracticeSessionStore.clearActivePracticeSession(activeChildId, activityId)
                                                isSubmitting = false
                                                onFinished()
                                                return@launch
                                            } else {
                                                PracticeSessionStore.saveActivePracticeSession(
                                                    com.simats.selfora.data.repository.ActivePracticeSession(
                                                        childId = activeChildId,
                                                        activityId = activityId,
                                                        currentStepIndex = steps.size - 1,
                                                        completedSteps = steps.size,
                                                        totalSteps = steps.size,
                                                        sessionStatus = "PENDING_SYNC"
                                                    )
                                                )
                                                submitError = "Progress safely saved offline! Tap to retry sending."
                                            }
                                        } catch (e: Exception) {
                                            PracticeSessionStore.saveActivePracticeSession(
                                                com.simats.selfora.data.repository.ActivePracticeSession(
                                                    childId = activeChildId,
                                                    activityId = activityId,
                                                    currentStepIndex = steps.size - 1,
                                                    completedSteps = steps.size,
                                                    totalSteps = steps.size,
                                                    sessionStatus = "PENDING_SYNC"
                                                )
                                            )
                                            submitError = "Connection paused. Progress safely saved! Tap to retry."
                                        }
                                    }
                                    isSubmitting = false
                                }
                            } else {
                                submitError = null
                                if (activeChildId > 0) {
                                    scope.launch {
                                        try {
                                            ApiClient.apiService.recordChildStepProgress(
                                                com.simats.selfora.data.model.RecordStepProgressRequest(
                                                    childId = activeChildId,
                                                    activityId = activityId,
                                                    stepNumber = currentStepIndex + 1,
                                                    isActivityCompleted = false
                                                )
                                            )
                                        } catch (_: Exception) {}
                                    }
                                }
                                currentStepIndex++
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ChildGreenPlay),
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving... ✨", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        } else {
                            Text(
                                if (currentStepIndex == steps.size - 1) "I DID IT! 🎉" else "NEXT STEP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }
                }

                if (submitError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = submitError!!,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelforaError,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}
