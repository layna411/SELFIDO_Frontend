package com.simats.selfora.ui.caregiver

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.*
import com.simats.selfora.data.repository.PracticeSessionStore
import com.simats.selfora.ui.components.VisualStepGuidanceCard
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.components.progress.ActivityStepProgress
import com.simats.selfora.ui.components.progress.SelforaLinearProgress
import com.simats.selfora.ui.therapist.*
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*

enum class CaregiverPracticeStage {
    ACTIVITY_DETAILS,
    CAREGIVER_GUIDANCE,
    STEP_VIEW,
    RECORD_PERFORMANCE,
    TODAY_SUMMARY
}

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

    var stage by remember { mutableStateOf(CaregiverPracticeStage.ACTIVITY_DETAILS) }
    var program by remember { mutableStateOf<HomeProgramResponse?>(null) }
    var steps by remember { mutableStateOf<List<TaskStepResponse>>(emptyList()) }
    var currentStepIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submissionError by remember { mutableStateOf<String?>(null) }

    // Idempotent Client Session ID
    val clientSessionId = remember { UUID.randomUUID().toString() }

    // Recorded step outcomes: Step ID -> (Outcome, Observation)
    val stepResultsMap = remember { mutableStateMapOf<Long, StepResultItemDto>() }
    var selectedOutcome by remember { mutableStateOf("INDEPENDENT") }
    var stepNoteText by remember { mutableStateOf("") }
    var sessionDurationMinutes by remember { mutableStateOf(15) }
    var overallCaregiverNotes by remember { mutableStateOf("") }

    // TTS
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
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

    // Load Program & Task Steps from backend
    LaunchedEffect(programId) {
        isLoading = true
        submissionError = null
        try {
            val progRes = ApiClient.apiService.getHomeProgramById(programId)
            if (progRes.isSuccessful && progRes.body() != null) {
                program = progRes.body()!!
                val actId = program!!.activityId
                val stepRes = ApiClient.apiService.getTaskSteps(actId)
                if (stepRes.isSuccessful && !stepRes.body().isNullOrEmpty()) {
                    steps = stepRes.body()!!
                } else {
                    steps = SelfidoCatalogue.getStepsForActivity(actId)
                }
            } else {
                // Fallback to active program or catalogue
                val todayRes = ApiClient.apiService.getTodayHomePrograms()
                val found = todayRes.body()?.firstOrNull { it.id == programId }
                if (found != null) {
                    program = found
                    val actId = found.activityId
                    val stepRes = ApiClient.apiService.getTaskSteps(actId)
                    steps = if (stepRes.isSuccessful && !stepRes.body().isNullOrEmpty()) stepRes.body()!! else SelfidoCatalogue.getStepsForActivity(actId)
                } else {
                    steps = SelfidoCatalogue.getStepsForActivity(1L)
                }
            }
        } catch (e: Exception) {
            val actId = program?.activityId ?: 1L
            steps = SelfidoCatalogue.getStepsForActivity(actId)
        } finally {
            isLoading = false
        }
    }

    val activityTitle = program?.title ?: "Home Practice Session"

    // Five Clinical Caregiver Outcomes
    val caregiverOutcomes = listOf(
        Triple("INDEPENDENT", "Completed Independently", "Child did this step with no assistance"),
        Triple("LITTLE_HELP", "Completed with a Little Help", "Needed gentle prompt or small gesture"),
        Triple("LOT_HELP", "Completed with a Lot of Help", "Needed hands-on guidance or full physical help"),
        Triple("COULD_NOT_COMPLETE", "Could Not Complete", "Child tried but could not finish this step"),
        Triple("NOT_PRACTISED", "Not Practised", "Skipped this step today")
    )

    fun submitPracticeSession() {
        val prog = program
        val childId = prog?.childId ?: 1L
        val actId = prog?.activityId ?: 1L

        val resultsList = steps.map { s ->
            stepResultsMap[s.id] ?: StepResultItemDto(
                stepId = s.id,
                stepNumber = s.stepNumber,
                stepTitle = s.title,
                outcome = "INDEPENDENT",
                observation = "Completed"
            )
        }

        val request = SubmitPracticeRequest(
            clientSessionId = clientSessionId,
            homeProgramId = programId,
            childId = childId,
            activityId = actId,
            practiceDate = LocalDate.now().toString(),
            durationMinutes = sessionDurationMinutes,
            caregiverNotes = overallCaregiverNotes.ifBlank { "Home practice completed successfully." },
            stepResults = resultsList
        )

        isSubmitting = true
        submissionError = null

        scope.launch {
            try {
                val res = ApiClient.apiService.submitPractice(request)
                if (res.isSuccessful && res.body() != null) {
                    val saved = res.body()!!
                    // Update client store with verified response
                    val indep = resultsList.count { it.outcome == "INDEPENDENT" }
                    val help = resultsList.count { it.outcome == "LITTLE_HELP" || it.outcome == "LOT_HELP" }
                    PracticeSessionStore.addPracticeRecord(
                        programId = programId,
                        childId = childId,
                        activityId = actId,
                        activityTitle = prog?.title ?: "Home Practice",
                        durationMinutes = sessionDurationMinutes,
                        caregiverNotes = overallCaregiverNotes,
                        totalSteps = steps.size,
                        independentCount = indep,
                        neededHelpCount = help,
                        stepResults = resultsList
                    )
                    stage = CaregiverPracticeStage.TODAY_SUMMARY
                    Toast.makeText(context, "Practice submitted to therapist!", Toast.LENGTH_SHORT).show()
                } else {
                    submissionError = "Server rejected submission (${res.code()}). Please try again."
                }
            } catch (e: Exception) {
                submissionError = "Connection error: ${e.localizedMessage}"
            } finally {
                isSubmitting = false
            }
        }
    }

    Scaffold(
        containerColor = SelforaBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (stage) {
                            CaregiverPracticeStage.ACTIVITY_DETAILS -> "Activity Details"
                            CaregiverPracticeStage.CAREGIVER_GUIDANCE -> "Caregiver Guidance"
                            CaregiverPracticeStage.STEP_VIEW -> activityTitle
                            CaregiverPracticeStage.RECORD_PERFORMANCE -> "Record Step Outcome"
                            CaregiverPracticeStage.TODAY_SUMMARY -> "Practice Summary"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SelforaTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (stage) {
                            CaregiverPracticeStage.ACTIVITY_DETAILS -> onBack()
                            CaregiverPracticeStage.CAREGIVER_GUIDANCE -> stage = CaregiverPracticeStage.ACTIVITY_DETAILS
                            CaregiverPracticeStage.STEP_VIEW -> {
                                if (currentStepIndex > 0) currentStepIndex--
                                else stage = CaregiverPracticeStage.CAREGIVER_GUIDANCE
                            }
                            CaregiverPracticeStage.RECORD_PERFORMANCE -> stage = CaregiverPracticeStage.STEP_VIEW
                            CaregiverPracticeStage.TODAY_SUMMARY -> onPracticeCompleted()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SelforaPrimary)
                    }
                },
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
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SelforaPrimary)
                }
            } else {
                when (stage) {
                    // STAGE 1: ACTIVITY DETAILS
                    CaregiverPracticeStage.ACTIVITY_DETAILS -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("👕", fontSize = 56.sp)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(activityTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            program?.description ?: "Practice daily to build independence.",
                                            fontSize = 13.sp,
                                            color = SelforaTextSecondary,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Total Steps", fontSize = 11.sp, color = SelforaTextSecondary)
                                                Text("${steps.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Est. Duration", fontSize = 11.sp, color = SelforaTextSecondary)
                                                Text("${program?.targetDurationMinutes ?: 15} mins", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Frequency", fontSize = 11.sp, color = SelforaTextSecondary)
                                                Text("${program?.frequencyPerWeek ?: 3}x / week", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                                            }
                                        }
                                    }
                                }

                                if (!program?.caregiverInstructions.isNullOrBlank()) {
                                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                                        Text("Therapist Guidance:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("\"${program!!.caregiverInstructions}\"", fontSize = 13.sp, color = SelforaTextPrimary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            GlassButton(
                                text = "Start Guided Practice →",
                                onClick = { stage = CaregiverPracticeStage.CAREGIVER_GUIDANCE },
                                modifier = Modifier.fillMaxWidth().height(52.dp)
                            )
                        }
                    }

                    // STAGE 2: CAREGIVER GUIDANCE
                    CaregiverPracticeStage.CAREGIVER_GUIDANCE -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("Caregiver Instructions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("💡 Practice Tips:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SelforaPrimary)
                                        Text("• Allow your child to initiate each step before offering help.", fontSize = 13.sp, color = SelforaTextPrimary)
                                        Text("• Use short, clear verbal cues rather than taking over.", fontSize = 13.sp, color = SelforaTextPrimary)
                                        Text("• Offer enthusiastic praise when your child completes a step!", fontSize = 13.sp, color = SelforaTextPrimary)
                                    }
                                }

                                if (!program?.safetyConsiderations.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFFFFFBEB),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("⚠️ Safety Considerations:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFB45309))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(program!!.safetyConsiderations!!, fontSize = 12.sp, color = Color(0xFF92400E))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            GlassButton(
                                text = "Begin Step 1 →",
                                onClick = {
                                    currentStepIndex = 0
                                    stage = CaregiverPracticeStage.STEP_VIEW
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp)
                            )
                        }
                    }

                    // STAGE 3: STEP VIEW (Visual Guidance + TTS)
                    CaregiverPracticeStage.STEP_VIEW -> {
                        val currentStep = steps[currentStepIndex]
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                VisualStepGuidanceCard(
                                    stepNumber = currentStep.stepNumber,
                                    totalSteps = steps.size,
                                    stepTitle = currentStep.title,
                                    childInstruction = currentStep.instructionText ?: currentStep.childInstruction ?: currentStep.title,
                                    gender = "BOY",
                                    mediaUrl = currentStep.media.firstOrNull()?.mediaUrl,
                                    onSpeakInstruction = {
                                        val text = currentStep.instructionText ?: currentStep.childInstruction ?: currentStep.title
                                        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                                    }
                                )

                                ActivityStepProgress(
                                    currentStep = currentStepIndex + 1,
                                    totalSteps = steps.size,
                                    accentColor = SelforaPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GlassOutlinedButton(
                                    text = "Back",
                                    onClick = {
                                        if (currentStepIndex > 0) currentStepIndex--
                                        else stage = CaregiverPracticeStage.CAREGIVER_GUIDANCE
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                GlassButton(
                                    text = "Record Outcome →",
                                    onClick = {
                                        val existing = stepResultsMap[currentStep.id]
                                        selectedOutcome = existing?.outcome ?: "INDEPENDENT"
                                        stepNoteText = existing?.observation ?: ""
                                        stage = CaregiverPracticeStage.RECORD_PERFORMANCE
                                    },
                                    modifier = Modifier.weight(1.5f)
                                )
                            }
                        }
                    }

                    // STAGE 4: RECORD PERFORMANCE (5 Caregiver Outcomes)
                    CaregiverPracticeStage.RECORD_PERFORMANCE -> {
                        val currentStep = steps[currentStepIndex]
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .imePadding()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Column {
                                Text("Step ${currentStepIndex + 1} of ${steps.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(currentStep.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Select the outcome that best describes your child's effort:", fontSize = 13.sp, color = SelforaTextSecondary)
                            }

                            // 5 Caregiver Outcomes
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                caregiverOutcomes.forEach { (code, title, desc) ->
                                    val isSelected = selectedOutcome == code
                                    Surface(
                                        onClick = { selectedOutcome = code },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) Color(0xFFF0FDF4) else Color.White,
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) SelforaSuccess else Color(0xFFE2E8F0)
                                        ),
                                        shadowElevation = if (isSelected) 2.dp else 1.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedOutcome = code },
                                                colors = RadioButtonDefaults.colors(selectedColor = SelforaSuccess)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SelforaTextPrimary)
                                                Text(desc, fontSize = 11.sp, color = SelforaTextSecondary)
                                            }
                                        }
                                    }
                                }
                            }

                            // Optional Observation Notes
                            GlassTextField(
                                value = stepNoteText,
                                onValueChange = { stepNoteText = it },
                                label = "Observation Note (Optional)",
                                placeholder = "e.g. Needed encouragement, tried twice...",
                                singleLine = false,
                                leadingIcon = Icons.Default.EditNote
                            )

                            if (submissionError != null) {
                                Text(submissionError!!, fontSize = 12.sp, color = SelforaError, fontWeight = FontWeight.Medium)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            GlassButton(
                                text = if (currentStepIndex == steps.size - 1) {
                                    if (isSubmitting) "Submitting Practice..." else "Complete & Submit Session ✓"
                                } else "Save & Next Step →",
                                onClick = {
                                    stepResultsMap[currentStep.id] = StepResultItemDto(
                                        stepId = currentStep.id,
                                        stepNumber = currentStep.stepNumber,
                                        stepTitle = currentStep.title,
                                        outcome = selectedOutcome,
                                        observation = stepNoteText.trim()
                                    )

                                    if (currentStepIndex < steps.size - 1) {
                                        currentStepIndex++
                                        stage = CaregiverPracticeStage.STEP_VIEW
                                    } else {
                                        // Final Step: Submit session
                                        submitPracticeSession()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp)
                            )
                        }
                    }

                    // STAGE 5: TODAY'S SUMMARY (Live Persisted Outcome)
                    CaregiverPracticeStage.TODAY_SUMMARY -> {
                        val indepCount = stepResultsMap.values.count { it.outcome == "INDEPENDENT" }
                        val helpCount = stepResultsMap.values.count { it.outcome == "LITTLE_HELP" || it.outcome == "LOT_HELP" }
                        val couldNotCount = stepResultsMap.values.count { it.outcome == "COULD_NOT_COMPLETE" }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shadowElevation = 4.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("⭐", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Great Job Today!", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = SelforaTextPrimary)
                                    Text("Practice record submitted for therapist review.", fontSize = 13.sp, color = SelforaSuccess, fontWeight = FontWeight.SemiBold)

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Steps Practised", fontSize = 13.sp, color = SelforaTextSecondary)
                                        Text("${steps.size} / ${steps.size}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Independent Steps", fontSize = 13.sp, color = SelforaTextSecondary)
                                        Text("$indepCount", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaSuccess)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Assisted Steps", fontSize = 13.sp, color = SelforaTextSecondary)
                                        Text("$helpCount", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Review Status", fontSize = 13.sp, color = SelforaTextSecondary)
                                        Surface(shape = RoundedCornerShape(8.dp), color = SelforaWarning.copy(alpha = 0.15f)) {
                                            Text("Pending Review", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaWarning, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            GlassButton(
                                text = "Return to Dashboard",
                                onClick = onPracticeCompleted,
                                modifier = Modifier.fillMaxWidth().height(52.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
