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
import com.simats.selfora.ui.components.VisualStepGuidanceCard
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.components.progress.ActivityStepProgress
import com.simats.selfora.ui.therapist.*
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch
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
    activityCode: String = "tooth_brushing_activity",
    onPracticeCompleted: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var stage by remember { mutableStateOf(CaregiverPracticeStage.ACTIVITY_DETAILS) }
    var steps by remember { mutableStateOf<List<TaskStepResponse>>(emptyList()) }
    var currentStepIndex by remember { mutableStateOf(0) }
    var activeSessionId by remember { mutableStateOf<Long?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Observation outcome per step
    var selectedAssistance by remember { mutableStateOf("VERBAL") } // INDEPENDENT, VERBAL, VISUAL, PHYSICAL, UNABLE
    var stepNoteText by remember { mutableStateOf("") }
    var isAudioPlaying by remember { mutableStateOf(false) }

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

    LaunchedEffect(activityCode) {
        isLoading = true
        steps = when (activityCode) {
            "eating_spoon_activity" -> getSampleEatingSteps()
            "shoes_socks_activity" -> getSampleShoesSteps()
            "girl_frock_activity" -> getSampleGirlFrockSteps()
            "boy_tshirt_activity" -> getSampleTShirtSteps()
            else -> getSampleToothBrushingSteps()
        }
        activeSessionId = 1L
        isLoading = false
    }

    val activityTitle = when (activityCode) {
        "eating_spoon_activity" -> "Eating with Spoon"
        "shoes_socks_activity" -> "Shoes & Socks"
        "girl_frock_activity" -> "Girl Frock Dressing"
        "boy_tshirt_activity" -> "Wearing T-Shirt"
        else -> "Tooth Brushing"
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
                            CaregiverPracticeStage.RECORD_PERFORMANCE -> "Record Performance"
                            CaregiverPracticeStage.TODAY_SUMMARY -> "Today's Summary"
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
                                else stage = CaregiverPracticeStage.ACTIVITY_DETAILS
                            }
                            CaregiverPracticeStage.RECORD_PERFORMANCE -> stage = CaregiverPracticeStage.STEP_VIEW
                            CaregiverPracticeStage.TODAY_SUMMARY -> onPracticeCompleted()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SelforaTextPrimary)
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SelforaSecondary)
                }
            } else {
                when (stage) {
                    // STAGE 1: ACTIVITY DETAILS (Image 2 Screen 3)
                    CaregiverPracticeStage.ACTIVITY_DETAILS -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(activityTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                    Surface(shape = RoundedCornerShape(10.dp), color = SelforaPrimary.copy(alpha = 0.12f)) {
                                        Text("Assigned by Therapist", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text("Focus Step", fontSize = 12.sp, color = SelforaTextSecondary, fontWeight = FontWeight.Medium)
                                Text("Brush all around teeth", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextPrimary)

                                Spacer(modifier = Modifier.height(16.dp))

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = SelforaSecondary.copy(alpha = 0.06f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SelforaSecondary.copy(alpha = 0.15f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Practice Plan", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SelforaSecondary)

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("🕒 When", fontSize = 12.sp, color = SelforaTextSecondary)
                                            Text("At night", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("🔄 How many times", fontSize = 12.sp, color = SelforaTextSecondary)
                                            Text("1 time", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("💬 Prompt to use", fontSize = 12.sp, color = SelforaTextSecondary)
                                            Text("Verbal Prompt", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("🎯 Goal", fontSize = 12.sp, color = SelforaTextSecondary)
                                            Text("Complete 5 of ${steps.size} steps", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaSuccess)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text("Therapist Instruction", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                Spacer(modifier = Modifier.height(6.dp))

                                Surface(
                                    onClick = {
                                        isAudioPlaying = !isAudioPlaying
                                        if (isAudioPlaying) {
                                            tts?.speak("Please guide your child to brush all teeth surfaces with gentle reminders.", TextToSpeech.QUEUE_FLUSH, null, null)
                                        } else {
                                            tts?.stop()
                                        }
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White.copy(alpha = 0.9f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                if (isAudioPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                                contentDescription = null,
                                                tint = SelforaPrimary,
                                                modifier = Modifier.size(26.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                if (isAudioPlaying) "Playing audio instruction..." else "Tap to play instruction",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = SelforaTextPrimary
                                            )
                                        }
                                        Text("00:35", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaTextSecondary)
                                    }
                                }
                            }

                            GlassOutlinedButton(
                                text = "Caregiver Guidance (DO / AVOID / PROMPT)",
                                icon = Icons.Default.MenuBook,
                                onClick = { stage = CaregiverPracticeStage.CAREGIVER_GUIDANCE },
                                modifier = Modifier.fillMaxWidth()
                            )

                            GlassButton(
                                text = "Start Practice →",
                                onClick = { stage = CaregiverPracticeStage.STEP_VIEW },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // STAGE 2: CAREGIVER GUIDANCE (Image 2 Screen 5)
                    CaregiverPracticeStage.CAREGIVER_GUIDANCE -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // DO Card
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFF0FDF4),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("DO ✓", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF16A34A))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("• Allow your child to try each step first.", fontSize = 13.sp, color = Color(0xFF166534))
                                    Text("• Use verbal prompt only when needed.", fontSize = 13.sp, color = Color(0xFF166534))
                                    Text("• Praise every successful attempt.", fontSize = 13.sp, color = Color(0xFF166534))
                                }
                            }

                            // AVOID Card
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFFEF2F2),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("AVOID ✕", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFDC2626))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("• Do not give physical help unless child is unable.", fontSize = 13.sp, color = Color(0xFF991B1B))
                                    Text("• Avoid doing the activity for the child.", fontSize = 13.sp, color = Color(0xFF991B1B))
                                }
                            }

                            // PROMPT Card
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFEFF6FF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("PROMPT →", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2563EB))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("• Use VERBAL prompt first.", fontSize = 13.sp, color = Color(0xFF1E40AF))
                                    Text("• If needed, use VISUAL prompt.", fontSize = 13.sp, color = Color(0xFF1E40AF))
                                    Text("• Reduce prompt as child improves.", fontSize = 13.sp, color = Color(0xFF1E40AF))
                                }
                            }

                            // Motivational Banner
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFFAF5FF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD8B4FE)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Encourage independence", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF7E22CE))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Small steps today, big progress tomorrow!", fontSize = 12.sp, color = Color(0xFF6B21A8))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            GlassButton(
                                text = "Start Practice Now →",
                                onClick = { stage = CaregiverPracticeStage.STEP_VIEW },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // STAGE 3: STEP-BY-STEP GUIDE (Image 2 Screen 4)
                    CaregiverPracticeStage.STEP_VIEW -> {
                        val currentStep = steps[currentStepIndex]
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Step ${currentStepIndex + 1} of ${steps.size}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentStep.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaTextPrimary,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Image Card
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    shadowElevation = 3.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = when (activityCode) {
                                                    "eating_spoon_activity" -> "🥣"
                                                    "shoes_socks_activity" -> "👟"
                                                    "boy_tshirt_activity" -> "👕"
                                                    else -> "🪥"
                                                },
                                                fontSize = 64.sp
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = currentStep.instructionText ?: currentStep.title,
                                                fontSize = 13.sp,
                                                color = SelforaTextSecondary,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Modern Activity Step Progress
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
                                        else stage = CaregiverPracticeStage.ACTIVITY_DETAILS
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                GlassButton(
                                    text = "Record Step →",
                                    onClick = { stage = CaregiverPracticeStage.RECORD_PERFORMANCE },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // STAGE 4: RECORD PERFORMANCE (Image 2 Screen 6)
                    CaregiverPracticeStage.RECORD_PERFORMANCE -> {
                        val currentStep = steps[currentStepIndex]
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .imePadding()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column {
                                Text("Step ${currentStepIndex + 1} of ${steps.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(currentStep.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("How did your child perform this step?", fontSize = 13.sp, color = SelforaTextSecondary)
                            }

                            // Assistance Options
                            val assistanceLevels = listOf(
                                Triple("INDEPENDENT", "Independent", "No help needed"),
                                Triple("VERBAL", "Verbal Prompt", "Spoken reminder"),
                                Triple("VISUAL", "Visual Prompt", "Shown picture / gesture"),
                                Triple("PHYSICAL", "Physical Assistance", "Helped physically"),
                                Triple("UNABLE", "Unable", "Could not do")
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                assistanceLevels.forEach { (code, title, desc) ->
                                    val isSelected = selectedAssistance == code
                                    Surface(
                                        onClick = { selectedAssistance = code },
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isSelected) Color(0xFFF0FDF4) else Color.White,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) SelforaSuccess else Color(0xFFE2E8F0)
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
                                                onClick = { selectedAssistance = code },
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

                            // Notes (optional)
                            GlassTextField(
                                value = stepNoteText,
                                onValueChange = { stepNoteText = it },
                                label = "Notes (optional)",
                                placeholder = "Write any observations or notes...",
                                singleLine = false,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                modifier = Modifier.fillMaxWidth()
                            )

                            GlassButton(
                                text = "Save & Continue →",
                                onClick = {
                                    stepNoteText = ""
                                    if (currentStepIndex < steps.size - 1) {
                                        currentStepIndex++
                                        stage = CaregiverPracticeStage.STEP_VIEW
                                    } else {
                                        stage = CaregiverPracticeStage.TODAY_SUMMARY
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // STAGE 5: TODAY'S SUMMARY (Image 2 Screen 7)
                    CaregiverPracticeStage.TODAY_SUMMARY -> {
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
                                    Text("Great effort today!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                    Text("Keep it up!", fontSize = 14.sp, color = SelforaSuccess, fontWeight = FontWeight.SemiBold)

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Activities Completed", fontSize = 13.sp, color = SelforaTextSecondary)
                                        Text("2 / 3", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Steps Completed", fontSize = 13.sp, color = SelforaTextSecondary)
                                        Text("${steps.size} / ${steps.size}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Prompt Used Mostly", fontSize = 13.sp, color = SelforaTextSecondary)
                                        Text("Verbal Prompt", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Child Response", fontSize = 13.sp, color = SelforaTextSecondary)
                                        Text("Good 😊", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaSuccess)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            GlassButton(
                                text = "View Details / Done",
                                onClick = onPracticeCompleted,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
