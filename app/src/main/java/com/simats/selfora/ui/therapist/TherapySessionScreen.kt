package com.simats.selfora.ui.therapist

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.*
import com.simats.selfora.ui.components.VisualStepGuidanceCard
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.components.progress.ActivityStepProgress
import com.simats.selfora.ui.components.progress.SelforaLinearProgress
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class TherapySessionStage {
    SELECT_ACTIVITY,
    STEP_VIEW,
    RECORD_PERFORMANCE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapySessionScreen(
    childId: Long,
    activityId: Long = 1L,
    viewModel: TherapySessionViewModel = viewModel(),
    onSessionCompleted: (sessionId: Long) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentDate = remember { LocalDate.now().toString() }

    // Fetch child profile to auto-scope gender & display identity
    var childDto by remember { mutableStateOf<ChildDto?>(null) }
    var childLoading by remember { mutableStateOf(true) }

    LaunchedEffect(childId) {
        if (childId <= 0L) {
            childLoading = false
            return@LaunchedEffect
        }
        childLoading = true
        try {
            val res = ApiClient.apiService.getChildById(childId)
            if (res.isSuccessful && res.body() != null) {
                childDto = res.body()
            }
        } catch (_: Exception) {}
        finally {
            childLoading = false
        }
    }

    // Gender State: auto-detect from child profile with interactive therapist switch
    var manualGenderOverride by remember { mutableStateOf<String?>(null) }
    val detectedGender = remember(childDto) {
        val g = childDto?.gender?.trim()?.uppercase()
        if (g != null && (g.startsWith("G") || g.startsWith("F"))) {
            "GIRL"
        } else if (g != null && (g.startsWith("B") || g.startsWith("M"))) {
            "BOY"
        } else {
            null
        }
    }
    val childGender = manualGenderOverride ?: detectedGender

    // Category & View Mode State
    var selectedCategory by remember(childGender) {
        mutableStateOf(if (childGender == null) "SHOES" else "DRESSING")
    } // "DRESSING", "SHOES", "EATING"
    var isShelfLayout by remember { mutableStateOf(true) } // true: notebook sketch shelf, false: 2x2 grid
    var showExitConfirmationDialog by remember { mutableStateOf(false) }

    // Selected Activity ID with auto-default strictly matching child gender
    var selectedActivityId by remember(childGender, selectedCategory) {
        mutableStateOf(
            if (activityId > 0 && selectedCategory == "DRESSING" && ((childGender == "GIRL" && activityId in 17L..25L) || (childGender == "BOY" && activityId in 1L..16L))) {
                activityId
            } else {
                when (selectedCategory) {
                    "DRESSING" -> if (childGender == "GIRL") 17L else 1L
                    "SHOES" -> 8L
                    "EATING" -> 11L
                    else -> 1L
                }
            }
        )
    }

    val selectedActivityDef = remember(selectedActivityId) {
        SelfidoCatalogue.getActivityById(selectedActivityId)
    }
    val selectedActivityTitle = selectedActivityDef?.title ?: "Therapy Session"

    // Intercept back gesture / hardware back when inside an active session
    BackHandler(enabled = uiState.stage != TherapySessionStage.SELECT_ACTIVITY) {
        showExitConfirmationDialog = true
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    val steps = uiState.steps
    val currentStepIndex = uiState.currentStepIndex
    val currentStepObj = steps.getOrNull(currentStepIndex)
    val currentRecord = uiState.stepRecords[currentStepIndex]

    Scaffold(
        topBar = {
            if (uiState.stage == TherapySessionStage.SELECT_ACTIVITY) {
                val doctorName = childDto?.therapistName?.ifBlank { null }
                    ?: com.simats.selfora.data.local.SessionManager.getUsername().ifBlank { null }
                    ?: "Jenkins"
                StartSessionTopBar(
                    childId = childId,
                    currentDate = currentDate,
                    therapistName = doctorName,
                    therapistDesignation = "Occupational Therapist (OT)",
                    onBackClick = onBack
                )
            } else {
                GlassTopBar(
                    title = when (uiState.stage) {
                        TherapySessionStage.STEP_VIEW -> selectedActivityTitle
                        TherapySessionStage.RECORD_PERFORMANCE -> "Record Performance"
                        else -> "Therapy Session"
                    },
                    subtitle = when (uiState.stage) {
                        TherapySessionStage.STEP_VIEW -> "Step ${currentStepIndex + 1} of ${steps.size}"
                        TherapySessionStage.RECORD_PERFORMANCE -> "Step ${currentStepIndex + 1} of ${steps.size}: ${currentStepObj?.title ?: ""}"
                        else -> null
                    },
                    onBackClick = {
                        when (uiState.stage) {
                            TherapySessionStage.STEP_VIEW -> {
                                showExitConfirmationDialog = true
                            }
                            TherapySessionStage.RECORD_PERFORMANCE -> {
                                viewModel.setStage(TherapySessionStage.STEP_VIEW)
                            }
                            else -> onBack()
                        }
                    },
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
                                    "${uiState.secondsElapsed / 60}:${String.format("%02d", uiState.secondsElapsed % 60)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = SelforaWarning
                                )
                            }
                        }
                    }
                )
            }
        },
        containerColor = SelforaBackground
    ) { padding ->
        AnimatedContent(
            targetState = uiState.stage,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { it / 4 } + fadeIn(animationSpec = tween(300)))
                        .togetherWith(slideOutHorizontally(animationSpec = tween(250, easing = FastOutSlowInEasing)) { -it / 4 } + fadeOut(animationSpec = tween(200)))
                } else {
                    (slideInHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { -it / 4 } + fadeIn(animationSpec = tween(300)))
                        .togetherWith(slideOutHorizontally(animationSpec = tween(250, easing = FastOutSlowInEasing)) { it / 4 } + fadeOut(animationSpec = tween(200)))
                }
            },
            label = "SessionStageTransition"
        ) { stage ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (stage) {
                    // =========================================================================
                    // 1. SELECT ACTIVITY STAGE (Exact Match with Assessment Workflow)
                    // =========================================================================
                    TherapySessionStage.SELECT_ACTIVITY -> {
                        // SECTION 1: Patient Context Card & Interactive Dual-Gender Switch
                        PatientContextCard(
                            childDto = childDto,
                            childId = childId,
                            childGender = childGender,
                            onSelectGender = { g ->
                                manualGenderOverride = g
                                if (selectedCategory == "DRESSING") {
                                    selectedActivityId = if (g == "GIRL") 17L else 1L
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // SECTION 2: ADL Category Tabs (Dresses, Footwear, Food) + Grid/Shelf Toggle
                        AdlCategorySelector(
                            selectedCategory = selectedCategory,
                            onSelectCategory = { cat ->
                                selectedCategory = cat
                                selectedActivityId = when (cat) {
                                    "DRESSING" -> if (childGender == "GIRL") 17L else 1L
                                    "SHOES" -> 8L
                                    "EATING" -> 11L
                                    else -> 1L
                                }
                            },
                            isShelfLayout = isShelfLayout,
                            onToggleLayout = { isShelfLayout = !isShelfLayout }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // SECTION 3: Structured Activity Boxes by Category and Gender
                        AnimatedContent(
                            targetState = Pair(selectedCategory, childGender),
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(240)) + slideInHorizontally(animationSpec = tween(240)) { it / 6 })
                                    .togetherWith(fadeOut(animationSpec = tween(180)) + slideOutHorizontally(animationSpec = tween(180)) { -it / 6 })
                            },
                            label = "CategoryAndGenderContent"
                        ) { (cat, gender) ->
                            if (cat == "DRESSING" && gender == null) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFFFEF3C7),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("⚠️", fontSize = 18.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Gender Pathway Required",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF92400E)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Dressing activities are gender-specific. Please choose a pathway above or select common Footwear or Food activities.",
                                            fontSize = 12.sp,
                                            color = Color(0xFF78350F),
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    manualGenderOverride = "BOY"
                                                    selectedActivityId = 1L
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("👦 Boy Pathway", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Button(
                                                onClick = {
                                                    manualGenderOverride = "GIRL"
                                                    selectedActivityId = 17L
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = SelforaSecondary),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("👧 Girl Pathway", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            } else {
                                val activitySections = remember(cat, gender) {
                                    SelfidoCatalogue.getActivitiesForGenderAndCategory(gender ?: "BOY", cat)
                                }

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    activitySections.forEach { (sectionTitle, activities) ->
                                        val sectionIcon = when {
                                            sectionTitle.contains("Upper", ignoreCase = true) || sectionTitle.contains("Top", ignoreCase = true) -> "👕"
                                            sectionTitle.contains("Lower", ignoreCase = true) || sectionTitle.contains("Bottom", ignoreCase = true) -> "👖"
                                            sectionTitle.contains("Sock", ignoreCase = true) -> "🧦"
                                            sectionTitle.contains("Shoe", ignoreCase = true) -> "👟"
                                            sectionTitle.contains("Hand", ignoreCase = true) -> "🍛"
                                            else -> "🥣"
                                        }

                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            AdlSectionHeader(
                                                title = sectionTitle,
                                                icon = sectionIcon,
                                                activityCount = activities.size
                                            )

                                            if (isShelfLayout) {
                                                // SHELF VIEW: Horizontal notebook sketch row of uniform cards
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .horizontalScroll(rememberScrollState()),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    activities.forEachIndexed { index, act ->
                                                        val isSelected = act.id == selectedActivityId
                                                        ActivitySelectionCard(
                                                            number = index + 1,
                                                            activity = act,
                                                            isSelected = isSelected,
                                                            onClick = { selectedActivityId = act.id },
                                                            modifier = Modifier.width(148.dp)
                                                        )
                                                    }
                                                }
                                            } else {
                                                // GRID VIEW: Balanced 2-Column Grid
                                                activities.chunked(2).forEachIndexed { rowIndex, rowActivities ->
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        rowActivities.forEachIndexed { colIndex, act ->
                                                            val itemNumber = rowIndex * 2 + colIndex + 1
                                                            val isSelected = act.id == selectedActivityId
                                                            ActivitySelectionCard(
                                                                number = itemNumber,
                                                                activity = act,
                                                                isSelected = isSelected,
                                                                onClick = { selectedActivityId = act.id },
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                        }
                                                        if (rowActivities.size == 1) {
                                                            Spacer(modifier = Modifier.weight(1f))
                                                        }
                                                    }
                                                    if (rowIndex < activities.chunked(2).size - 1) {
                                                        Spacer(modifier = Modifier.height(10.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // SECTION 4: Selected Activity Overview & Start Session Button
                        SelectedActivityOverviewCard(
                            activity = selectedActivityDef,
                            childGender = childGender,
                            selectedCategory = selectedCategory,
                            onStartSessionClick = {
                                viewModel.initSession(childId, selectedActivityId)
                                viewModel.setStage(TherapySessionStage.STEP_VIEW)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // =========================================================================
                    // 2. STEP VIEW STAGE
                    // =========================================================================
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
                                        modifier = Modifier.size(110.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(selectedActivityDef?.icon ?: "🎯", fontSize = 56.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    Text(
                                        text = currentStepObj.instructionText ?: currentStepObj.childInstruction ?: "Perform the activity step.",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = SelforaTextPrimary,
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        lineHeight = 22.sp,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    ActivityStepProgress(
                                        currentStep = currentStepIndex + 1,
                                        totalSteps = steps.size,
                                        accentColor = SelforaPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

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
                                            showExitConfirmationDialog = true
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                GlassButton(
                                    text = "Record Performance →",
                                    onClick = { viewModel.setStage(TherapySessionStage.RECORD_PERFORMANCE) },
                                    icon = Icons.Default.ArrowForward,
                                    gradient = listOf(SelforaPrimary, Color(0xFF6D28D9)),
                                    modifier = Modifier.weight(1.5f)
                                )
                            }
                        }
                    }

                    // =========================================================================
                    // 3. RECORD PERFORMANCE STAGE (L0-L6, Unable, Not Assessed)
                    // =========================================================================
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
                                    border = BorderStroke(1.dp, activeLevel.composeColor.copy(alpha = 0.3f)),
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

                            if (uiState.errorMessage != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SelforaError.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, SelforaError.copy(alpha = 0.3f)),
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
                                                onAdvanceToNextStep = {},
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

    // Exit Therapy Session Confirmation Dialog
    if (showExitConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmationDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = SelforaWarning,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Exit Therapy Session?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = SelforaTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Do you want to exit this therapy session?",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SelforaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You are currently running a session for $selectedActivityTitle (Step ${currentStepIndex + 1} of ${steps.size}). Any unsaved step performance records and session timer will be discarded.",
                        fontSize = 12.sp,
                        color = SelforaTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmationDialog = false
                        viewModel.setStage(TherapySessionStage.SELECT_ACTIVITY)
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SelforaError),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Yes, Exit Session", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showExitConfirmationDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Continue Session", color = SelforaPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}
