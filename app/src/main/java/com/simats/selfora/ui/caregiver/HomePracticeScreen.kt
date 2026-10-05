package com.simats.selfora.ui.caregiver

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.local.OfflineSessionEntity
import com.simats.selfora.data.local.SelforaDatabase
import com.simats.selfora.data.model.*
import com.simats.selfora.data.repository.DressingRepository
import com.simats.selfora.ui.therapist.*
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*

import com.simats.selfora.data.repository.PracticeSessionStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePracticeScreen(
    programId: Long = 1L,
    activityCode: String = "boy_tshirt_activity",
    onPracticeCompleted: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Steps list based on activityCode
    var steps by remember { mutableStateOf<List<TaskStepResponse>>(emptyList()) }
    var currentStepIndex by remember { mutableStateOf(0) }
    var activeSessionId by remember { mutableStateOf<Long?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Session completion state
    var isSessionComplete by remember { mutableStateOf(false) }
    var independentCount by remember { mutableStateOf(0) }
    var neededHelpCount by remember { mutableStateOf(0) }
    var totalPracticeTimeSeconds by remember { mutableStateOf(720) } // Default ~12 mins

    // Observation state per step
    val stepOutcomes = remember { mutableStateMapOf<Int, CaregiverObservationOutcome>() }
    val stepNotes = remember { mutableStateMapOf<Int, String>() }
    var selectedObservation by remember { mutableStateOf<CaregiverObservationOutcome?>(null) }
    var caregiverNote by remember { mutableStateOf("") }
    var showObservationSheet by remember { mutableStateOf(false) }

    // Text To Speech
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsSpeaking by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    LaunchedEffect(activityCode) {
        isLoading = true
        try {
            steps = when (activityCode) {
                "eating_spoon_activity" -> getSampleEatingSteps()
                "shoes_socks_activity" -> getSampleShoesSteps()
                "girl_frock_activity" -> getSampleGirlFrockSteps()
                else -> getSampleTShirtSteps()
            }

            val sessionRes = ApiClient.apiService.startSession(
                StartSessionRequest(childId = 1L, activityId = 1L, sessionType = "HOME_PRACTICE", environment = "HOME")
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
        containerColor = SelforaBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            when (activityCode) {
                                "eating_spoon_activity" -> "Eating with Spoon"
                                "shoes_socks_activity" -> "Shoes & Socks"
                                "girl_frock_activity" -> "Girl Frock Dressing"
                                else -> "Boy T-Shirt Dressing"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = SelforaTextPrimary
                        )
                        Text("Home Practice Mode", fontSize = 11.sp, color = SelforaPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.statusBarsPadding(),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SelforaSurface)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SelforaBackground)
        ) {
            if (isLoading || steps.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = SelforaPrimary)
            } else if (isSessionComplete) {
                // Section 8: Session Completion View
                CaregiverSessionCompleteView(
                    activityTitle = when (activityCode) {
                        "eating_spoon_activity" -> "Eating with Spoon"
                        "shoes_socks_activity" -> "Shoes & Socks"
                        "girl_frock_activity" -> "Girl Frock Dressing"
                        else -> "Boy T-Shirt Dressing"
                    },
                    totalSteps = steps.size,
                    completedIndependent = independentCount,
                    neededHelp = neededHelpCount,
                    durationMinutes = totalPracticeTimeSeconds / 60,
                    onFinish = onPracticeCompleted,
                    onPracticeAgain = {
                        isSessionComplete = false
                        currentStepIndex = 0
                        independentCount = 0
                        neededHelpCount = 0
                    }
                )
            } else {
                val currentStep = steps[currentStepIndex]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Progress bar & step indicator
                        LinearProgressIndicator(
                            progress = { (currentStepIndex + 1).toFloat() / steps.size },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = SelforaPrimary,
                            trackColor = SelforaBorder
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Step ${currentStepIndex + 1} of ${steps.size}",
                                fontWeight = FontWeight.Bold,
                                color = SelforaPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                "${((currentStepIndex + 1).toFloat() / steps.size * 100).toInt()}% Done",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Visual Animation / Illustration Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = SelforaSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    color = SelforaPrimary.copy(alpha = 0.08f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        com.simats.selfora.ui.components.avatar.ChildAvatar(
                                            avatarType = if (activityCode.contains("girl"))
                                                com.simats.selfora.ui.components.avatar.ChildAvatarType.GIRL
                                            else
                                                com.simats.selfora.ui.components.avatar.ChildAvatarType.BOY,
                                            avatarSize = 150.dp,
                                            avatarPose = com.simats.selfora.ui.components.avatar.AvatarPose.WAVE,
                                            backgroundStyle = com.simats.selfora.ui.components.avatar.AvatarBackgroundStyle.NONE
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    currentStep.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = SelforaTextPrimary,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Spoken Guidance Box
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = SelforaSuccess.copy(alpha = 0.12f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "CHILD GUIDANCE:",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SelforaSuccess
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "\"${currentStep.childInstruction}\"",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SelforaSuccess,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Audio & Animation Control Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            tts?.speak(currentStep.childInstruction, TextToSpeech.QUEUE_FLUSH, null, null)
                                        },
                                        modifier = Modifier.weight(1f).height(44.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary)
                                    ) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Play Guidance", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            tts?.speak(currentStep.childInstruction, TextToSpeech.QUEUE_FLUSH, null, null)
                                        },
                                        modifier = Modifier.weight(1f).height(44.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SelforaPrimary)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Replay", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Section 6: Caregiver Performance Recording Card (STRICTLY NON-CLINICAL)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = SelforaSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "How did your child do?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SelforaTextPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                CaregiverObservationOutcome.values().forEach { outcome ->
                                    val isSelected = selectedObservation == outcome
                                    val bgColour = Color(android.graphics.Color.parseColor(outcome.colorHex)).copy(alpha = if (isSelected) 0.2f else 0.06f)
                                    val borderColour = if (isSelected) Color(android.graphics.Color.parseColor(outcome.colorHex)) else Color.Transparent

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(bgColour)
                                            .border(1.5.dp, borderColour, RoundedCornerShape(12.dp))
                                            .clickable { selectedObservation = outcome }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(outcome.emoji, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                outcome.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = SelforaTextPrimary
                                            )
                                            Text(
                                                outcome.description,
                                                fontSize = 11.sp,
                                                color = SelforaTextSecondary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = caregiverNote,
                                    onValueChange = { caregiverNote = it },
                                    label = { Text("Add a note (optional)") },
                                    placeholder = { Text("Write something...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 2,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SelforaBackground,
                                        unfocusedContainerColor = SelforaBackground,
                                        focusedBorderColor = SelforaPrimary,
                                        unfocusedBorderColor = SelforaBorder
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Bottom Navigation Buttons (Previous & Next/Complete)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (currentStepIndex > 0) {
                                    val currentObs = selectedObservation ?: CaregiverObservationOutcome.INDEPENDENT
                                    stepOutcomes[currentStepIndex] = currentObs
                                    stepNotes[currentStepIndex] = caregiverNote

                                    currentStepIndex--
                                    selectedObservation = stepOutcomes[currentStepIndex] ?: CaregiverObservationOutcome.INDEPENDENT
                                    caregiverNote = stepNotes[currentStepIndex] ?: ""
                                } else {
                                    onBack()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (currentStepIndex == 0) "Exit" else "Previous", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    val obs = selectedObservation ?: CaregiverObservationOutcome.INDEPENDENT
                                    stepOutcomes[currentStepIndex] = obs
                                    stepNotes[currentStepIndex] = caregiverNote

                                    // Save offline local record per step
                                    try {
                                        val sId = activeSessionId ?: 1L
                                        val db = SelforaDatabase.getDatabase(context)
                                        db.offlineSessionDao().insertOfflineRecord(
                                            OfflineSessionEntity(
                                                sessionId = sId,
                                                stepId = currentStep.id,
                                                promptLevelId = obs.promptLevelEquivalent,
                                                outcome = obs.name,
                                                attempts = 1,
                                                durationSeconds = 15,
                                                therapistNote = null,
                                                caregiverNote = caregiverNote,
                                                environment = "HOME",
                                                isSynced = false
                                            )
                                        )

                                        ApiClient.apiService.recordPerformance(
                                            id = sId,
                                            request = RecordPerformanceRequest(
                                                stepId = currentStep.id,
                                                promptLevelId = obs.promptLevelEquivalent,
                                                outcome = obs.name,
                                                durationSeconds = 15,
                                                caregiverNote = caregiverNote,
                                                environment = "HOME"
                                            )
                                        )
                                    } catch (_: Exception) {}

                                    if (currentStepIndex < steps.size - 1) {
                                        currentStepIndex++
                                        selectedObservation = stepOutcomes[currentStepIndex] ?: CaregiverObservationOutcome.INDEPENDENT
                                        caregiverNote = stepNotes[currentStepIndex] ?: ""
                                    } else {
                                        // Calculate total independent vs needed help across ALL steps
                                        val totalIndep = steps.indices.count { idx ->
                                            val o = stepOutcomes[idx] ?: CaregiverObservationOutcome.INDEPENDENT
                                            o == CaregiverObservationOutcome.INDEPENDENT || o == CaregiverObservationOutcome.LITTLE_HELP
                                        }
                                        val totalHelp = steps.size - totalIndep
                                        independentCount = totalIndep
                                        neededHelpCount = totalHelp

                                        val stepResultsList = steps.mapIndexed { idx, s ->
                                            val o = stepOutcomes[idx] ?: CaregiverObservationOutcome.INDEPENDENT
                                            val n = stepNotes[idx] ?: ""
                                            StepResultItemDto(
                                                stepId = s.id,
                                                stepNumber = s.stepNumber,
                                                stepTitle = s.title,
                                                outcome = o.name,
                                                observation = n
                                            )
                                        }

                                        val actTitle = when (activityCode) {
                                            "eating_spoon_activity" -> "Eating with Spoon"
                                            "shoes_socks_activity" -> "Shoes & Socks"
                                            "girl_frock_activity" -> "Girl Frock Dressing"
                                            else -> "Boy T-Shirt Dressing"
                                        }
                                        val actId = when (activityCode) {
                                            "eating_spoon_activity" -> 13L
                                            "shoes_socks_activity" -> 9L
                                            "girl_frock_activity" -> 2L
                                            else -> 1L
                                        }

                                        // Store in PracticeSessionStore
                                        PracticeSessionStore.addPracticeRecord(
                                            programId = programId,
                                            childId = 1L,
                                            activityId = actId,
                                            activityTitle = actTitle,
                                            durationMinutes = totalPracticeTimeSeconds / 60,
                                            caregiverNotes = caregiverNote.ifEmpty { "Child completed home practice session successfully!" },
                                            totalSteps = steps.size,
                                            independentCount = totalIndep,
                                            neededHelpCount = totalHelp,
                                            stepResults = stepResultsList
                                        )

                                        try {
                                            ApiClient.apiService.completeSession(activeSessionId ?: 1L)
                                            val practiceReq = SubmitPracticeRequest(
                                                homeProgramId = programId,
                                                childId = 1L,
                                                activityId = actId,
                                                practiceDate = java.time.LocalDate.now().toString(),
                                                durationMinutes = totalPracticeTimeSeconds / 60,
                                                caregiverNotes = caregiverNote.ifEmpty { "Child completed home practice session successfully!" },
                                                stepResults = stepResultsList
                                            )
                                            ApiClient.apiService.submitPractice(practiceReq)
                                        } catch (_: Exception) {}

                                        isSessionComplete = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SelforaSuccess)
                        ) {
                            Text(
                                if (currentStepIndex == steps.size - 1) "Finish Session" else "Next Step",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.navigationBarsPadding().height(24.dp))
                }
            }
        }
    }
}

@Composable
fun CaregiverSessionCompleteView(
    activityTitle: String,
    totalSteps: Int,
    completedIndependent: Int,
    neededHelp: Int,
    durationMinutes: Int,
    onFinish: () -> Unit,
    onPracticeAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SelforaSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎉", fontSize = 56.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Practice Complete!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = SelforaTextPrimary
                )
                Text(
                    activityTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SelforaPrimary
                )

                Spacer(modifier = Modifier.height(20.dp))

                Divider(color = SelforaBorder)

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$totalSteps", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = SelforaPrimary)
                        Text("Steps Practiced", fontSize = 11.sp, color = SelforaTextSecondary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$completedIndependent", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = SelforaSuccess)
                        Text("Completed", fontSize = 11.sp, color = SelforaTextSecondary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$neededHelp", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = SelforaWarning)
                        Text("Needed Help", fontSize = 11.sp, color = SelforaTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SelforaBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Practice time: $durationMinutes minutes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SelforaTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onPracticeAgain,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Practice Again", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onFinish,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SelforaSuccess)
                    ) {
                        Text("Finish", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
