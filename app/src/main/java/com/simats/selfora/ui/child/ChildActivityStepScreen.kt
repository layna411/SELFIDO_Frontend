package com.simats.selfora.ui.child



import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.background

import androidx.compose.foundation.clickable

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

import com.simats.selfora.data.model.ActivityResponse

import com.simats.selfora.data.model.TaskStepResponse

import com.simats.selfora.data.repository.PracticeSessionStore

import com.simats.selfora.ui.components.VisualStepGuidanceCard

import com.simats.selfora.ui.theme.*

import com.simats.selfora.ui.therapist.getSampleEatingSteps

import com.simats.selfora.ui.therapist.getSampleGirlFrockSteps

import com.simats.selfora.ui.therapist.getSampleShoesSteps

import com.simats.selfora.ui.therapist.getSampleTShirtSteps
import com.simats.selfora.ui.therapist.getSampleShirtSteps
import com.simats.selfora.ui.therapist.getSampleJacketSteps

import com.simats.selfora.utils.LanguageManager

import com.simats.selfora.utils.TtsManager

import kotlinx.coroutines.launch



@Composable

fun ChildActivityStepScreen(

    activityId: Long = 1L,

    onFinished: () -> Unit,

    onBack: () -> Unit

) {

    val currentLanguage by LanguageManager.currentLanguage.collectAsState()

    val isTamil = currentLanguage == "ta"



    var steps by remember { mutableStateOf<List<TaskStepResponse>>(emptyList()) }

    var activityDetail by remember { mutableStateOf<ActivityResponse?>(null) }

    var currentStepIndex by remember { mutableStateOf(0) }

    var isLoading by remember { mutableStateOf(true) }

    var isSubmitting by remember { mutableStateOf(false) }

    var submitError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()



    val context = LocalContext.current

    val ttsManager = remember { TtsManager(context) }

    val gender = remember { SessionManager.getActiveChildGender().ifBlank { "BOY" } }

    val activeChildId = remember { SessionManager.getActiveChildId() }



    LaunchedEffect(currentLanguage) {

        ttsManager.setLanguage(if (isTamil) "ta" else "en")

    }



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
            3L -> getSampleJacketSteps()
            2L -> if (gender.equals("GIRL", ignoreCase = true)) getSampleGirlFrockSteps() else getSampleShirtSteps()
            else -> getSampleTShirtSteps()
        }

        try {

            // Fetch activity metadata (including starting positions in en & ta)

            val actRes = ApiClient.apiService.getActivityById(activityId)

            if (actRes.isSuccessful && actRes.body() != null) {

                activityDetail = actRes.body()

            }



            // Fetch steps

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



    // Auto-save active progress on step change and auto-speak instruction in selected language

    LaunchedEffect(currentStepIndex, steps, currentLanguage) {

        if (steps.isNotEmpty() && currentStepIndex in steps.indices) {

            ttsManager.setLanguage(if (isTamil) "ta" else "en")

            val rawInstruction = if (isTamil && !steps[currentStepIndex].childInstructionTa.isNullOrBlank()) {

                steps[currentStepIndex].childInstructionTa!!

            } else {

                steps[currentStepIndex].childInstruction

            }

            ttsManager.speak(rawInstruction)



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

            val displayStepTitle = if (isTamil && !step.titleTa.isNullOrBlank()) step.titleTa!! else step.title

            val displayInstruction = if (isTamil && !step.childInstructionTa.isNullOrBlank()) step.childInstructionTa!! else step.childInstruction

            val displayActivityTitle = if (isTamil && !activityDetail?.titleTa.isNullOrBlank()) activityDetail?.titleTa!! else (activityDetail?.title ?: "")



            Column(

                modifier = Modifier.fillMaxSize(),

                verticalArrangement = Arrangement.SpaceBetween,

                horizontalAlignment = Alignment.CenterHorizontally

            ) {

                // Header Bar (Back button, ActivityStepProgress, Language Switcher & Audio)

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.SpaceBetween,

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    IconButton(onClick = onBack) {

                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SelforaBlueDark)

                    }



                    Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {

                        com.simats.selfora.ui.components.progress.ActivityStepProgress(

                            currentStep = currentStepIndex + 1,

                            totalSteps = steps.size

                        )

                    }



                    // Direct Compact Language Toggle Button

                    Surface(

                        shape = RoundedCornerShape(14.dp),

                        color = if (isTamil) Color(0xFFFEF3C7) else Color.White,

                        border = BorderStroke(1.dp, if (isTamil) Color(0xFFF59E0B) else Color(0xFFCBD5E1)),

                        modifier = Modifier.clickable {

                            LanguageManager.toggleLanguage()

                        }

                    ) {

                        Text(

                            text = if (isTamil) "🇮🇳 தமிழ்" else "🇬🇧 EN",

                            fontSize = 11.sp,

                            fontWeight = FontWeight.ExtraBold,

                            color = if (isTamil) Color(0xFFB45309) else SelforaPrimary,

                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)

                        )

                    }



                    Spacer(modifier = Modifier.width(6.dp))



                    IconButton(onClick = { ttsManager.speak(displayInstruction) }) {

                        Icon(Icons.Default.VolumeUp, contentDescription = "Replay Audio", tint = SelforaPrimary)

                    }

                }



                // Starting Position Card (Prominently displayed at initial step 1)

                val startPos = if (isTamil && !activityDetail?.startingPositionTa.isNullOrBlank()) {

                    activityDetail?.startingPositionTa

                } else {

                    activityDetail?.startingPosition

                }

                if (currentStepIndex == 0 && !startPos.isNullOrBlank()) {

                    Surface(

                        shape = RoundedCornerShape(14.dp),

                        color = Color.White.copy(alpha = 0.95f),

                        border = BorderStroke(1.dp, SelforaPrimary.copy(alpha = 0.3f)),

                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)

                    ) {

                        Row(

                            modifier = Modifier.padding(10.dp),

                            verticalAlignment = Alignment.CenterVertically

                        ) {

                            Text("📍", fontSize = 18.sp)

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {

                                Text(

                                    text = if (isTamil) "தொடக்க நிலை:" else "STARTING POSITION:",

                                    fontSize = 10.sp,

                                    fontWeight = FontWeight.ExtraBold,

                                    color = SelforaPrimary

                                )

                                Text(

                                    text = startPos,

                                    fontSize = 12.sp,

                                    fontWeight = FontWeight.SemiBold,

                                    color = SelforaTextPrimary,

                                    lineHeight = 14.sp

                                )

                            }

                        }

                    }

                }



                // Visual Step Guidance Card

                VisualStepGuidanceCard(

                    stepNumber = currentStepIndex + 1,

                    totalSteps = steps.size,

                    stepTitle = displayStepTitle,

                    childInstruction = displayInstruction,

                    gender = gender,

                    activityTitle = displayActivityTitle,

                    isTamil = isTamil,

                    mediaUrl = step.media.firstOrNull()?.mediaUrl,

                    onSpeakInstruction = { ttsManager.speak(displayInstruction) }

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

                            Text(if (isTamil) "முந்தையது" else "Previous", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                        }

                    }



                    IconButton(

                        onClick = { ttsManager.speak(displayInstruction) },

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

                                                submitError = if (isTamil) "முன்னேற்றம் பாதுகாக்கப்பட்டது! அனுப்ப தட்டவும்." else "Progress safely saved offline! Tap to retry sending."

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

                                            submitError = if (isTamil) "இணைப்பு தாமதமாகிறது. முன்னேற்றம் பாதுகாக்கப்பட்டது!" else "Connection paused. Progress safely saved! Tap to retry."

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

                            Text(if (isTamil) "சேமிக்கிறது... ✨" else "Saving... ✨", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)

                        } else {

                            val nextButtonText = if (currentStepIndex == steps.size - 1) {

                                if (isTamil) "நான் முடித்தேன்! 🎉" else "I DID IT! 🎉"

                            } else {

                                if (isTamil) "அடுத்த படி" else "NEXT STEP"

                            }

                            Text(

                                text = nextButtonText,

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

