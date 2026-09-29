package com.simats.selfora.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
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

    var selectedPromptLevel by remember { mutableStateOf(3) } // default Verbal
    var isUnable by remember { mutableStateOf(false) }
    var therapistNote by remember { mutableStateOf("") }

    var secondsElapsed by remember { mutableStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    // Timer effect
    LaunchedEffect(activeSessionId) {
        if (activeSessionId != null) {
            while (true) {
                delay(1000)
                secondsElapsed++
            }
        }
    }

    // Start session and fetch steps
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val stepsRes = ApiClient.apiService.getTaskSteps(activityId)
            steps = if (stepsRes.isSuccessful && stepsRes.body() != null) stepsRes.body()!! else getSampleTShirtSteps()

            val sessionRes = ApiClient.apiService.startSession(
                StartSessionRequest(childId = childId, activityId = activityId, sessionType = "CLINIC_THERAPY", environment = "CLINIC")
            )
            activeSessionId = if (sessionRes.isSuccessful && sessionRes.body() != null) sessionRes.body()!!.sessionId else 1L
        } catch (e: Exception) {
            steps = getSampleTShirtSteps()
            activeSessionId = 1L
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Therapy Session (Live)", fontWeight = FontWeight.Bold, color = SelforaTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SelforaTextPrimary)
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = SelforaWarning, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${secondsElapsed / 60}:${String.format("%02d", secondsElapsed % 60)}",
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SelforaSurface,
                    titleContentColor = SelforaTextPrimary,
                    navigationIconContentColor = SelforaTextPrimary,
                    actionIconContentColor = SelforaTextPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SelforaBgLight)
                .padding(16.dp)
        ) {
            if (isLoading || steps.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val currentStep = steps[currentStepIndex]

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { (currentStepIndex + 1).toFloat() / steps.size },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = SelforaBluePrimary,
                            trackColor = SelforaBlueLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Step ${currentStepIndex + 1} of ${steps.size}", fontWeight = FontWeight.Bold, color = SelforaBlueDark)
                            Text("Activity: T-Shirt", fontSize = 12.sp, color = SelforaTextMuted)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Current Step Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    currentStep.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = SelforaTextDark
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SelforaBgLight,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("CLINICAL INSTRUCTION:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SelforaTextMuted)
                                        Text(currentStep.instructionText, fontSize = 13.sp, color = SelforaTextDark)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("CHILD SPOKEN PROMPT:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SelforaTeal)
                                        Text("\"${currentStep.childInstruction}\"", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SelforaTeal)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text("Select Assistance / Prompt Level Provided:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                PromptHierarchyBar(
                                    selectedLevel = selectedPromptLevel,
                                    isUnable = isUnable,
                                    onLevelSelected = { lvl ->
                                        selectedPromptLevel = lvl
                                        isUnable = false
                                    },
                                    onUnableSelected = {
                                        isUnable = true
                                    }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = therapistNote,
                                    onValueChange = { therapistNote = it },
                                    label = { Text("Therapist Observation Notes") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 2
                                )
                            }
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (currentStepIndex > 0) {
                            OutlinedButton(
                                onClick = { currentStepIndex-- },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Text("Previous")
                            }
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    isRecording = true
                                    try {
                                        activeSessionId?.let { sId ->
                                            ApiClient.apiService.recordPerformance(
                                                id = sId,
                                                request = RecordPerformanceRequest(
                                                    stepId = currentStep.id,
                                                    promptLevelId = if (isUnable) 6 else selectedPromptLevel,
                                                    outcome = if (isUnable) "UNABLE" else "COMPLETED",
                                                    durationSeconds = 15,
                                                    therapistNote = therapistNote,
                                                    environment = "CLINIC"
                                                )
                                            )
                                        }
                                    } catch (e: Exception) {}

                                    therapistNote = ""
                                    if (currentStepIndex < steps.size - 1) {
                                        currentStepIndex++
                                    } else {
                                        // End of session
                                        activeSessionId?.let { sId ->
                                            try {
                                                ApiClient.apiService.completeSession(sId)
                                            } catch (e: Exception) {}
                                            onSessionCompleted(sId)
                                        }
                                    }
                                    isRecording = false
                                }
                            },
                            modifier = Modifier
                                .weight(2f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SelforaBluePrimary)
                        ) {
                            if (isRecording) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text(
                                    if (currentStepIndex == steps.size - 1) "FINISH & SAVE SESSION" else "RECORD & NEXT STEP",
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(if (currentStepIndex == steps.size - 1) Icons.Default.Check else Icons.Default.PlayArrow, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }
}
