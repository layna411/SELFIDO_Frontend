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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.selfora.data.model.*
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*

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
    viewModel: TherapySessionViewModel = viewModel(),
    onSessionCompleted: (sessionId: Long) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val activityOptions = remember {
        listOf(
            ClinicActivityOption(5L, "Grooming", "Tooth Brushing", "🪥", 7),
            ClinicActivityOption(1L, "Dressing", "Wearing T-Shirt", "👕", 18),
            ClinicActivityOption(13L, "Eating", "Eating with Spoon", "🥣", 10)
        )
    }

    var selectedActivityId by remember { mutableStateOf(activityId.takeIf { it > 0 } ?: 5L) }

    LaunchedEffect(childId, selectedActivityId) {
        if (uiState.steps.isEmpty() || uiState.childId != childId || uiState.activityId != selectedActivityId) {
            viewModel.initSession(childId, selectedActivityId)
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    val selectedOption = activityOptions.firstOrNull { it.id == selectedActivityId } ?: activityOptions.first()
    val steps = uiState.steps
    val currentStepIndex = uiState.currentStepIndex
    val currentStepObj = steps.getOrNull(currentStepIndex)
    val currentRecord = uiState.stepRecords[currentStepIndex]

    Scaffold(
        topBar = {
            GlassTopBar(
                title = when (uiState.stage) {
                    TherapySessionStage.SELECT_ACTIVITY -> "Start New Session"
                    TherapySessionStage.STEP_VIEW -> selectedOption.title
                    TherapySessionStage.RECORD_PERFORMANCE -> "Record Performance"
                },
                subtitle = when (uiState.stage) {
                    TherapySessionStage.SELECT_ACTIVITY -> "Select Activity"
                    TherapySessionStage.STEP_VIEW -> "Step ${currentStepIndex + 1} of ${steps.size}"
                    TherapySessionStage.RECORD_PERFORMANCE -> "Step ${currentStepIndex + 1} of ${steps.size}: ${currentStepObj?.title ?: ""}"
                },
                onBackClick = {
                    when (uiState.stage) {
                        TherapySessionStage.SELECT_ACTIVITY -> onBack()
                        TherapySessionStage.STEP_VIEW -> {
                            if (currentStepIndex > 0) {
                                viewModel.goToStep(currentStepIndex - 1)
                            } else {
                                viewModel.setStage(TherapySessionStage.SELECT_ACTIVITY)
                            }
                        }
                        TherapySessionStage.RECORD_PERFORMANCE -> {
                            viewModel.setStage(TherapySessionStage.STEP_VIEW)
                        }
                    }
                },
                accentColor = SelforaPrimary,
                actions = {
                    if (uiState.stage != TherapySessionStage.SELECT_ACTIVITY) {
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
                                    "${uiState.secondsElapsed / 60}:${String.format("%02d", uiState.secondsElapsed % 60)}",
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
            when (uiState.stage) {
                // ==========================================
                // 1. SELECT ACTIVITY STAGE
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
                                    .clickable {
                                        selectedActivityId = opt.id
                                        viewModel.initSession(childId, opt.id)
                                    },
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
                                        onClick = {
                                            selectedActivityId = opt.id
                                            viewModel.initSession(childId, opt.id)
                                        },
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
                            viewModel.setStage(TherapySessionStage.STEP_VIEW)
                        },
                        icon = Icons.Default.ArrowForward,
                        gradient = listOf(SelforaPrimary, Color(0xFF6D28D9)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )
                }

                // ==========================================
                // 2. STEP VIEW STAGE
                // ==========================================
                TherapySessionStage.STEP_VIEW -> {
                    if (uiState.isLoading || steps.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = SelforaPrimary)
                        }
                    } else if (currentStepObj != null) {
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

                        // Large Illustration / Guidance Card
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
                                    text = currentStepObj.instructionText ?: currentStepObj.childInstruction ?: "Perform the activity step.",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SelforaTextPrimary,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Steps Dots Indicator
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
                                    if (currentStepIndex > 0) {
                                        viewModel.goToStep(currentStepIndex - 1)
                                    } else {
                                        viewModel.setStage(TherapySessionStage.SELECT_ACTIVITY)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )

                            GlassButton(
                                text = "Next",
                                onClick = { viewModel.setStage(TherapySessionStage.RECORD_PERFORMANCE) },
                                icon = Icons.Default.ArrowForward,
                                gradient = listOf(SelforaPrimary, Color(0xFF6D28D9)),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ==========================================
                // 3. RECORD PERFORMANCE STAGE (L0-L6, Unable, Not Assessed)
                // ==========================================
                TherapySessionStage.RECORD_PERFORMANCE -> {
                    if (steps.isNotEmpty() && currentStepObj != null) {
                        val activeLevel = currentRecord?.selectedLevel
                        val isSaved = currentRecord?.isSaved == true

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

                        Spacer(modifier = Modifier.height(14.dp))

                        // Previously Saved Status Banner
                        if (isSaved && activeLevel != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = activeLevel.composeColor.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, activeLevel.composeColor.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = activeLevel.composeColor, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Saved in database: ${activeLevel.label}",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = activeLevel.composeColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Clinical Hierarchy Section Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CLINICAL PROMPT HIERARCHY (L0 – L6)",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SelforaTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        // All L0-L6 Options
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ClinicalPromptLevel.hierarchyLevels.forEach { prompt ->
                                val isSelected = activeLevel == prompt
                                val pColor = prompt.composeColor

                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectPromptLevel(currentStepIndex, prompt)
                                        },
                                    backgroundColor = if (isSelected) pColor.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.9f),
                                    borderColor = if (isSelected) pColor else SelforaBorder.copy(alpha = 0.4f),
                                    cornerRadius = 14.dp
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            // Level Badge Pill
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSelected) pColor else pColor.copy(alpha = 0.15f),
                                                modifier = Modifier.size(width = 38.dp, height = 32.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = prompt.shortLabel,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (isSelected) Color.White else pColor
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column {
                                                Text(
                                                    text = prompt.label,
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                    color = SelforaTextPrimary
                                                )
                                                Text(
                                                    text = prompt.description,
                                                    fontSize = 11.5.sp,
                                                    color = SelforaTextSecondary,
                                                    lineHeight = 15.sp
                                                )
                                            }
                                        }

                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.selectPromptLevel(currentStepIndex, prompt) },
                                            colors = RadioButtonDefaults.colors(selectedColor = pColor)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Separate Clinical Options: UNABLE and NOT_ASSESSED
                        Text(
                            text = "SEPARATE CLINICAL STATUSES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SelforaTextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // UNABLE CARD
                            val isUnable = activeLevel == ClinicalPromptLevel.UNABLE
                            val unableColor = ClinicalPromptLevel.UNABLE.composeColor
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isUnable) unableColor.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.9f))
                                    .border(
                                        width = if (isUnable) 2.dp else 1.dp,
                                        color = if (isUnable) unableColor else SelforaBorder.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        viewModel.selectPromptLevel(currentStepIndex, ClinicalPromptLevel.UNABLE)
                                    }
                                    .padding(vertical = 12.dp, horizontal = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (isUnable) unableColor else SelforaTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Unable",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUnable) unableColor else SelforaTextPrimary
                                        )
                                        Text(
                                            text = "0% Indep.",
                                            fontSize = 11.sp,
                                            color = SelforaTextSecondary
                                        )
                                    }
                                }
                            }

                            // NOT_ASSESSED CARD
                            val isNotAssessed = activeLevel == ClinicalPromptLevel.NOT_ASSESSED
                            val notAssessedColor = ClinicalPromptLevel.NOT_ASSESSED.composeColor
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isNotAssessed) notAssessedColor.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.9f))
                                    .border(
                                        width = if (isNotAssessed) 2.dp else 1.dp,
                                        color = if (isNotAssessed) notAssessedColor else SelforaBorder.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        viewModel.selectPromptLevel(currentStepIndex, ClinicalPromptLevel.NOT_ASSESSED)
                                    }
                                    .padding(vertical = 12.dp, horizontal = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.SkipNext,
                                        contentDescription = null,
                                        tint = if (isNotAssessed) notAssessedColor else SelforaTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Not Assessed",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isNotAssessed) notAssessedColor else SelforaTextPrimary
                                        )
                                        Text(
                                            text = "Excluded",
                                            fontSize = 11.sp,
                                            color = SelforaTextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Therapist Notes Input
                        GlassTextField(
                            value = currentRecord?.notes ?: "",
                            onValueChange = { viewModel.updateTherapistNotes(currentStepIndex, it) },
                            label = "Therapist Clinical Notes (Optional)",
                            placeholder = "Observations, cuing strategies, resistance...",
                            singleLine = false,
                            leadingIcon = Icons.Default.EditNote
                        )

                        // Error Banner if validation fails
                        if (uiState.errorMessage != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SelforaError.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SelforaError.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = SelforaError, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = uiState.errorMessage ?: "",
                                        color = SelforaError,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Navigation and Save Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GlassOutlinedButton(
                                text = "Back",
                                onClick = { viewModel.setStage(TherapySessionStage.STEP_VIEW) },
                                modifier = Modifier.weight(0.8f)
                            )

                            GlassButton(
                                text = if (uiState.isSaving) {
                                    "Saving..."
                                } else if (currentStepIndex == steps.size - 1) {
                                    "Save & Complete Session ✓"
                                } else {
                                    "Save & Next Step →"
                                },
                                onClick = {
                                    if (!uiState.isSaving) {
                                        viewModel.saveCurrentStepPerformance(
                                            onAdvanceToNextStep = {
                                                // Next step view is set by viewmodel
                                            },
                                            onSessionComplete = {
                                                viewModel.completeSession(onSessionCompleted)
                                            }
                                        )
                                    }
                                },
                                icon = if (uiState.isSaving) null else Icons.Default.Check,
                                gradient = listOf(SelforaPrimary, Color(0xFF6D28D9)),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(52.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
