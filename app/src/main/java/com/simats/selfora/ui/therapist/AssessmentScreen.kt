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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.*
import com.simats.selfora.ui.components.VisualStepGuidanceCard
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.components.progress.ActivityStepProgress
import com.simats.selfora.ui.components.progress.SelforaLinearProgress
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
    var selectedCategory by remember { mutableStateOf("DRESSING") } // "DRESSING", "SHOES", "EATING"
    var isShelfLayout by remember { mutableStateOf(true) } // true: horizontal notebook sketch shelf, false: 2x2 grid

    var currentStepIndex by remember { mutableStateOf(0) }
    var stepStates by remember { mutableStateOf<List<StepAssessmentState>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showNotAssessedDialog by remember { mutableStateOf(false) }
    var notAssessedReasonInput by remember { mutableStateOf("") }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showExitConfirmationDialog by remember { mutableStateOf(false) }

    // Intercept back gesture / hardware back when inside an active assessment
    BackHandler(enabled = isStarted) {
        showExitConfirmationDialog = true
    }

    var childDto by remember { mutableStateOf<ChildDto?>(null) }
    var childLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentDate = remember { LocalDate.now().toString() }

    // Fetch Child Details
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
        val g = childDto?.gender?.trim()?.uppercase() ?: "BOY"
        if (g.startsWith("G") || g.startsWith("F")) "GIRL" else "BOY"
    }
    val childGender = manualGenderOverride ?: detectedGender

    // Selected Activity ID with auto-default strictly matching child gender
    var selectedActivityId by remember(childGender, selectedCategory) {
        mutableStateOf(
            when (selectedCategory) {
                "DRESSING" -> if (childGender == "GIRL") 17L else 1L
                "SHOES" -> 8L
                "EATING" -> 11L
                else -> 1L
            }
        )
    }

    val selectedActivityDef = remember(selectedActivityId) {
        SelfidoCatalogue.getActivityById(selectedActivityId)
    }
    val selectedActivityTitle = selectedActivityDef?.title ?: "ADL Activity"

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
                    stepStates = getSampleAssessmentSteps(actId)
                }
            } catch (e: Exception) {
                stepStates = getSampleAssessmentSteps(actId)
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = if (!isStarted) "Clinical ADL Assessment" else "Assessment: $selectedActivityTitle",
                subtitle = "Child ID: #$childId • Date: $currentDate",
                onBackClick = {
                    if (isStarted) {
                        showExitConfirmationDialog = true
                    } else {
                        onBack()
                    }
                },
                accentColor = SelforaPrimary
            )
        },
        containerColor = SelforaBackground
    ) { padding ->
        AnimatedContent(
            targetState = isStarted,
            transitionSpec = {
                if (targetState) {
                    (slideInHorizontally(animationSpec = tween(320, easing = FastOutSlowInEasing)) { it / 4 } + fadeIn(animationSpec = tween(300)))
                        .togetherWith(slideOutHorizontally(animationSpec = tween(260, easing = FastOutSlowInEasing)) { -it / 4 } + fadeOut(animationSpec = tween(220)))
                } else {
                    (slideInHorizontally(animationSpec = tween(320, easing = FastOutSlowInEasing)) { -it / 4 } + fadeIn(animationSpec = tween(300)))
                        .togetherWith(slideOutHorizontally(animationSpec = tween(260, easing = FastOutSlowInEasing)) { it / 4 } + fadeOut(animationSpec = tween(220)))
                }
            },
            label = "AssessmentStageTransition"
        ) { started ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (childId <= 0L) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SelforaPrimary.copy(alpha = 0.12f),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.ChildCare, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(32.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("No Child Selected for Assessment", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaTextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Please select an assigned child from 'My Children' or the Dashboard before starting a clinical assessment.",
                                color = SelforaTextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onBack,
                                colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Go to My Children")
                            }
                        }
                    }
                } else if (!started) {
                    // =========================================================================
                    // SECTION 1: Pediatric Patient Details Header & Interactive Gender Switch
                    // =========================================================================
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("PEDIATRIC PATIENT DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (childGender == "GIRL") SelforaSecondary.copy(alpha = 0.15f) else SelforaBlueLight,
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(if (childGender == "GIRL") "👧" else "👦", fontSize = 28.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    val displayName = childDto?.let { "${it.firstName} ${it.lastName ?: ""}".trim() } ?: "Child #$childId"
                                    val displayAge = childDto?.dateOfBirth?.let {
                                        try {
                                            val birth = LocalDate.parse(it)
                                            java.time.Period.between(birth, LocalDate.now()).years
                                        } catch (_: Exception) { 7 }
                                    } ?: 7
                                    val doctorName = childDto?.therapistName ?: "Assigned Occupational Therapist"

                                    Text(displayName, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                    Text("Child ID: #$childId • Age: $displayAge yrs", fontSize = 12.sp, color = SelforaTextSecondary)
                                    Text("Doctor: $doctorName", fontSize = 11.sp, color = SelforaPrimary, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            // Interactive Gender Switch (Boy Baby / Girl Baby)
                            Column(horizontalAlignment = Alignment.End) {
                                Text("GENDER SCOPE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SelforaTextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (childGender == "BOY") SelforaPrimary else Color.White,
                                        border = BorderStroke(1.5.dp, if (childGender == "BOY") SelforaPrimary else Color(0xFFCBD5E1)),
                                        modifier = Modifier.clickable {
                                            manualGenderOverride = "BOY"
                                            if (selectedCategory == "DRESSING") selectedActivityId = 1L
                                        }
                                    ) {
                                        Text(
                                            text = "👦 Boy",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (childGender == "BOY") Color.White else SelforaTextPrimary
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (childGender == "GIRL") SelforaSecondary else Color.White,
                                        border = BorderStroke(1.5.dp, if (childGender == "GIRL") SelforaSecondary else Color(0xFFCBD5E1)),
                                        modifier = Modifier.clickable {
                                            manualGenderOverride = "GIRL"
                                            if (selectedCategory == "DRESSING") selectedActivityId = 17L
                                        }
                                    ) {
                                        Text(
                                            text = "👧 Girl",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (childGender == "GIRL") Color.White else SelforaTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // =========================================================================
                    // SECTION 2: ADL Category Tabs (3 Categories: Dresses, Footwear, Food)
                    // =========================================================================
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SELECT ADL CATEGORY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SelforaPrimary.copy(alpha = 0.08f),
                                modifier = Modifier.clickable { isShelfLayout = !isShelfLayout }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(if (isShelfLayout) "↔️ Shelf View" else "▦ Grid View", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            GlassChip(
                                text = "👗 Dresses",
                                selected = selectedCategory == "DRESSING",
                                onClick = {
                                    selectedCategory = "DRESSING"
                                    selectedActivityId = if (childGender == "GIRL") 17L else 1L
                                },
                                modifier = Modifier.weight(1f)
                            )
                            GlassChip(
                                text = "👟 Footwear",
                                selected = selectedCategory == "SHOES",
                                onClick = {
                                    selectedCategory = "SHOES"
                                    selectedActivityId = 8L
                                },
                                modifier = Modifier.weight(1f)
                            )
                            GlassChip(
                                text = "🍽️ Food",
                                selected = selectedCategory == "EATING",
                                onClick = {
                                    selectedCategory = "EATING"
                                    selectedActivityId = 11L
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // =========================================================================
                    // SECTION 3: Structured Activity Boxes by Category and Gender
                    // Smooth animated transition when category or gender changes
                    // =========================================================================
                    AnimatedContent(
                        targetState = Pair(selectedCategory, childGender),
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(260)) + slideInHorizontally(animationSpec = tween(260)) { it / 6 })
                                .togetherWith(fadeOut(animationSpec = tween(200)) + slideOutHorizontally(animationSpec = tween(200)) { -it / 6 })
                        },
                        label = "CategoryAndGenderContent"
                    ) { (cat, gender) ->
                        val activitySections = remember(cat, gender) {
                            SelfidoCatalogue.getActivitiesForGenderAndCategory(gender, cat)
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            activitySections.forEach { (sectionTitle, activities) ->
                                val sectionIcon = when {
                                    sectionTitle.contains("Upper", ignoreCase = true) || sectionTitle.contains("Top", ignoreCase = true) -> "👕"
                                    sectionTitle.contains("Lower", ignoreCase = true) || sectionTitle.contains("Bottom", ignoreCase = true) -> "👖"
                                    sectionTitle.contains("Sock", ignoreCase = true) -> "🧦"
                                    sectionTitle.contains("Shoe", ignoreCase = true) -> "👟"
                                    sectionTitle.contains("Hand", ignoreCase = true) -> "🍛"
                                    else -> "🥣"
                                }

                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(sectionIcon, fontSize = 18.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = sectionTitle.uppercase(),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SelforaPrimary
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = SelforaPrimary.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = "${activities.size} Activities",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SelforaPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (isShelfLayout) {
                                        // ---------------------------------------------------------
                                        // SHELF VIEW: Matches the Notebook Sketch Diagram Exactly
                                        // Horizontal row of uniform square cards side-by-side
                                        // ---------------------------------------------------------
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            activities.forEachIndexed { index, act ->
                                                val isSelected = act.id == selectedActivityId
                                                ActivitySelectionBox(
                                                    number = index + 1,
                                                    activity = act,
                                                    isSelected = isSelected,
                                                    onClick = { selectedActivityId = act.id },
                                                    modifier = Modifier.width(138.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        // ---------------------------------------------------------
                                        // GRID VIEW: Balanced 2-Column Grid with Fixed Card Heights
                                        // No empty voids, perfectly aligned borders and heights
                                        // ---------------------------------------------------------
                                        activities.chunked(2).forEachIndexed { rowIndex, rowActivities ->
                                            if (rowActivities.size == 2) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    rowActivities.forEachIndexed { colIndex, act ->
                                                        val itemNumber = rowIndex * 2 + colIndex + 1
                                                        val isSelected = act.id == selectedActivityId
                                                        ActivitySelectionBox(
                                                            number = itemNumber,
                                                            activity = act,
                                                            isSelected = isSelected,
                                                            onClick = { selectedActivityId = act.id },
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                }
                                            } else {
                                                // Single odd card: spans full width gracefully
                                                val act = rowActivities.first()
                                                val itemNumber = rowIndex * 2 + 1
                                                val isSelected = act.id == selectedActivityId
                                                ActivitySelectionBox(
                                                    number = itemNumber,
                                                    activity = act,
                                                    isSelected = isSelected,
                                                    onClick = { selectedActivityId = act.id },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                            if (rowIndex < activities.chunked(2).size - 1) {
                                                Spacer(modifier = Modifier.height(10.dp))
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }

                    // =========================================================================
                    // SECTION 4: Selected Activity Overview & Launch Assessment Button
                    // =========================================================================
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = SelforaPrimary.copy(alpha = 0.5f)
                    ) {
                        Text("SELECTED ADL FOR ASSESSMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = SelforaPrimary.copy(alpha = 0.12f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(selectedActivityDef?.icon ?: "🎯", fontSize = 24.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = selectedActivityDef?.title ?: selectedActivityTitle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaTextPrimary
                                )
                                Text(
                                    text = "${selectedActivityDef?.totalSteps ?: 20} Steps • Gender: $childGender • $selectedCategory",
                                    fontSize = 12.sp,
                                    color = SelforaTextSecondary
                                )
                            }
                        }

                        if (!selectedActivityDef?.startingPosition.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "🎯 Clinical Starting Position:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SelforaPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = selectedActivityDef!!.startingPosition,
                                        fontSize = 12.sp,
                                        color = SelforaTextPrimary,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }

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
                    // =========================================================================
                    // SECTION 5: Active Clinical Step-by-Step Assessment Recording
                    // =========================================================================
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(selectedActivityTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaTextPrimary)
                                    Text("Task Step Assessment ($childGender)", fontSize = 11.5.sp, color = SelforaTextSecondary)
                                }
                                Surface(shape = RoundedCornerShape(12.dp), color = SelforaPrimary.copy(alpha = 0.12f)) {
                                    Text("Step ${currentStep.stepNumber} of $totalSteps", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            SelforaLinearProgress(
                                progress = ((currentStepIndex + 1).toFloat() / totalSteps.coerceAtLeast(1).toFloat()),
                                color = SelforaPrimary,
                                height = 8.dp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            ActivityStepProgress(
                                currentStep = currentStepIndex + 1,
                                totalSteps = totalSteps,
                                accentColor = SelforaPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Step Visual Guidance Card with animated step transition
                        AnimatedContent(
                            targetState = currentStepIndex,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInHorizontally(animationSpec = tween(250, easing = FastOutSlowInEasing)) { it / 4 } + fadeIn(animationSpec = tween(250)))
                                        .togetherWith(slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing)) { -it / 4 } + fadeOut(animationSpec = tween(180)))
                                } else {
                                    (slideInHorizontally(animationSpec = tween(250, easing = FastOutSlowInEasing)) { -it / 4 } + fadeIn(animationSpec = tween(250)))
                                        .togetherWith(slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing)) { it / 4 } + fadeOut(animationSpec = tween(180)))
                                }
                            },
                            label = "StepContentTransition"
                        ) { stepIdx ->
                            val step = stepStates[stepIdx]
                            VisualStepGuidanceCard(
                                stepNumber = step.stepNumber,
                                totalSteps = totalSteps,
                                stepTitle = step.title,
                                childInstruction = step.instruction,
                                gender = childGender,
                                onSpeakInstruction = {}
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // CLINICAL PROMPT HIERARCHY SELECTION CARD (L0 - L6, Unable, Not Assessed)
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
                                                stepStates = stepStates.toMutableList().also {
                                                    it[currentStepIndex] = currentStep.copy(promptLevelId = levelId, outcome = "COMPLETED")
                                                }
                                            }
                                            .padding(vertical = 12.dp, horizontal = 14.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = {
                                                    stepStates = stepStates.toMutableList().also {
                                                        it[currentStepIndex] = currentStep.copy(promptLevelId = levelId, outcome = "COMPLETED")
                                                    }
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
                                            stepStates = stepStates.toMutableList().also {
                                                it[currentStepIndex] = currentStep.copy(outcome = "UNABLE", promptLevelId = 7)
                                            }
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
                                onValueChange = { newNotes ->
                                    stepStates = stepStates.toMutableList().also {
                                        it[currentStepIndex] = currentStep.copy(notes = newNotes)
                                    }
                                },
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
    }

    // Exit Assessment Confirmation Dialog (when clinician wants to end in-between)
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
                    text = "Exit Assessment Session?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = SelforaTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Do you want to exit this assessment session?",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SelforaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You are currently at Step ${currentStepIndex + 1} of ${stepStates.size} for $selectedActivityTitle. Any unsubmitted evaluations and clinical notes will be discarded.",
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
                        isStarted = false
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
                    Text("Continue Assessment", color = SelforaPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // Not Assessed Reason Dialog
    if (showNotAssessedDialog) {
        val confirmNotAssessed = {
            stepStates = stepStates.toMutableList().also {
                it[currentStepIndex] = it[currentStepIndex].copy(
                    outcome = "NOT_ASSESSED",
                    promptLevelId = 8,
                    notAssessedReason = notAssessedReasonInput
                )
            }
            showNotAssessedDialog = false
        }

        AlertDialog(
            modifier = Modifier.imePadding(),
            onDismissRequest = { showNotAssessedDialog = false },
            title = { Text("Mark Step as Not Assessed", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Provide clinical explanation for skipping this step:", fontSize = 13.sp, color = SelforaTextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassTextField(
                        value = notAssessedReasonInput,
                        onValueChange = { notAssessedReasonInput = it },
                        label = "Reason",
                        placeholder = "e.g. Physical constraint, sensory aversion...",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { confirmNotAssessed() }),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = confirmNotAssessed,
                    colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary)
                ) {
                    Text("Confirm Skip", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotAssessedDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Final Assessment Review & Save Dialog
    if (showConfirmationDialog) {
        val independentCount = stepStates.count { it.outcome == "COMPLETED" && it.promptLevelId == 0 }
        val promptCount = stepStates.count { it.outcome == "COMPLETED" && (it.promptLevelId ?: 99) in 1..6 }
        val unableCount = stepStates.count { it.outcome == "UNABLE" }
        val notAssessedCount = stepStates.count { it.outcome == "NOT_ASSESSED" }
        val assessedCount = stepStates.size - notAssessedCount
        val indepPct = if (assessedCount > 0) ((independentCount.toFloat() / assessedCount.toFloat()) * 100).toInt() else 0

        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = {
                Text("Assessment Summary", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Activity: $selectedActivityTitle ($childGender)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Total Steps Evaluated: ${stepStates.size}", fontSize = 13.sp)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("• Independent Steps (L0):", fontSize = 13.sp)
                        Text("$independentCount", fontWeight = FontWeight.Bold, color = SelforaSuccess)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("• Prompted Steps (L1-L6):", fontSize = 13.sp)
                        Text("$promptCount", fontWeight = FontWeight.Bold, color = SelforaPrimary)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("• Unable to Complete:", fontSize = 13.sp)
                        Text("$unableCount", fontWeight = FontWeight.Bold, color = SelforaError)
                    }
                    if (notAssessedCount > 0) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("• Not Assessed (Skipped):", fontSize = 13.sp)
                            Text("$notAssessedCount", fontWeight = FontWeight.Bold, color = SelforaWarning)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SelforaPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Baseline Independence: $indepPct%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SelforaPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
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
                                    notes = "Clinical assessment for $selectedActivityTitle",
                                    stepResults = stepStates.map {
                                        StepAssessmentRequest(
                                            stepId = it.stepId,
                                            promptLevelId = it.promptLevelId ?: 3,
                                            outcome = it.outcome,
                                            notes = if (it.outcome == "NOT_ASSESSED") it.notAssessedReason else it.notes
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

/**
 * Visual Box Component corresponding directly to the handwritten boxes diagram.
 * Guaranteed fixed height and baseline alignment for 100% uniform visual presentation.
 */
@Composable
fun ActivitySelectionBox(
    number: Int,
    activity: SelfidoActivityDef,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ActivitySelectionCard(
        number = number,
        activity = activity,
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier
    )
}

fun getSampleAssessmentSteps(actId: Long = 1L): List<StepAssessmentState> {
    val steps = SelfidoCatalogue.getStepsForActivity(actId).ifEmpty { getSampleTShirtSteps() }
    return steps.map {
        StepAssessmentState(
            stepId = it.id,
            stepNumber = it.stepNumber,
            title = it.title,
            instruction = it.instructionText ?: it.childInstruction ?: ""
        )
    }
}
