package com.simats.selfora.ui.therapist

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.*
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CaregiverPromptLevelInfo(
    val id: Int,
    val title: String,
    val shortName: String,
    val parentAction: String,
    val icon: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeProgramCreateScreen(
    childId: Long = 1L,
    onProgramCreated: () -> Unit,
    onNavigateToReview: (Long) -> Unit = { onProgramCreated() },
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var childDto by remember { mutableStateOf<ChildDto?>(null) }
    var isChildLoading by remember { mutableStateOf(true) }

    // Fetch child info to establish strict gender context
    LaunchedEffect(childId) {
        if (childId <= 0L) {
            isChildLoading = false
            return@LaunchedEffect
        }
        isChildLoading = true
        try {
            val res = ApiClient.apiService.getChildById(childId)
            if (res.isSuccessful && res.body() != null) {
                childDto = res.body()
            }
        } catch (_: Exception) {}
        finally {
            isChildLoading = false
        }
    }

    val childGender = remember(childDto) {
        val raw = childDto?.gender?.trim()?.uppercase() ?: "BOY"
        if (raw.startsWith("G") || raw.startsWith("F")) "GIRL" else "BOY"
    }

    var selectedCategory by remember { mutableStateOf("DRESSING") } // "DRESSING", "SHOES", "EATING"
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

    var activitySteps by remember { mutableStateOf<List<TaskStepResponse>>(emptyList()) }
    var selectedStepIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var isStepsLoading by remember { mutableStateOf(false) }

    // Load Task Steps for selected activity
    LaunchedEffect(selectedActivityId) {
        isStepsLoading = true
        try {
            val stepRes = ApiClient.apiService.getTaskSteps(selectedActivityId)
            if (stepRes.isSuccessful && !stepRes.body().isNullOrEmpty()) {
                activitySteps = stepRes.body()!!
                selectedStepIds = activitySteps.map { it.id }.toSet()
            } else {
                activitySteps = SelfidoCatalogue.getStepsForActivity(selectedActivityId)
                selectedStepIds = activitySteps.map { it.id }.toSet()
            }
        } catch (e: Exception) {
            activitySteps = SelfidoCatalogue.getStepsForActivity(selectedActivityId)
            selectedStepIds = activitySteps.map { it.id }.toSet()
        } finally {
            isStepsLoading = false
        }
    }

    // Meaningful Prompt Levels for Caregivers
    val promptLevels = remember {
        listOf(
            CaregiverPromptLevelInfo(
                id = 0,
                title = "Level 0: Independent (Self-Reliant)",
                shortName = "Independent",
                parentAction = "Observe child from a short distance without helping. Let child attempt and complete the step entirely on their own.",
                icon = "🌟"
            ),
            CaregiverPromptLevelInfo(
                id = 1,
                title = "Level 1: Visual Prompt (Visual Guide)",
                shortName = "Visual Prompt",
                parentAction = "Point to the step picture on the app screen or visual schedule. Do not give verbal hints or touch the child.",
                icon = "🖼️"
            ),
            CaregiverPromptLevelInfo(
                id = 2,
                title = "Level 2: Gesture Prompt (Pointing)",
                shortName = "Gesture Prompt",
                parentAction = "Point directly to the clothing item, shoe strap, or food without speaking. Encourage non-verbal recognition.",
                icon = "👉"
            ),
            CaregiverPromptLevelInfo(
                id = 3,
                title = "Level 3: Verbal Prompt (Spoken Cue)",
                shortName = "Verbal Prompt",
                parentAction = "Give a concise, direct 2–3 word spoken reminder (e.g. 'Push arm through', 'Scoop the rice'). Avoid lecturing.",
                icon = "🗣️"
            ),
            CaregiverPromptLevelInfo(
                id = 4,
                title = "Level 4: Model / Video (Demonstration)",
                shortName = "Demonstration",
                parentAction = "Demonstrate the movement once slowly in front of the child, then say: 'Now you try!'",
                icon = "👀"
            ),
            CaregiverPromptLevelInfo(
                id = 5,
                title = "Level 5: Partial Physical (Light Guidance)",
                shortName = "Partial Physical",
                parentAction = "Gently touch or guide child's elbow or wrist toward the target, then fade your hands away immediately.",
                icon = "🤝"
            ),
            CaregiverPromptLevelInfo(
                id = 6,
                title = "Level 6: Full Physical (Hand-over-Hand)",
                shortName = "Full Physical",
                parentAction = "Place your hands gently over child's hands and guide the entire action smoothly together.",
                icon = "🤲"
            )
        )
    }

    var targetPromptLevelId by remember { mutableStateOf(3) } // Level 3: Verbal Prompt default
    var promptLevelDropdownExpanded by remember { mutableStateOf(false) }

    // Schedule & Routine Context
    var preferredRoutine by remember { mutableStateOf("MORNING") }
    var frequencyPerWeek by remember { mutableStateOf(3) }
    var targetDurationMinutes by remember { mutableStateOf(15) }

    // Meaningful Guidance Text
    var caregiverInstructions by remember(selectedActivityDef, targetPromptLevelId) {
        val sp = selectedActivityDef?.startingPosition ?: "Place the item within comfortable reach in front of child."
        val levelName = promptLevels.firstOrNull { it.id == targetPromptLevelId }?.shortName ?: "Verbal Prompt"
        mutableStateOf(
            "1. Setup: $sp\n" +
            "2. The 5-Second Wait Rule: After giving the initial cue, wait 5–7 seconds before intervening to allow processing time.\n" +
            "3. Prompt Level: Provide $levelName only if child pauses or requests help.\n" +
            "4. Immediate Praise: Praise specific effort enthusiastically upon completion!"
        )
    }

    var thingsToAvoid by remember {
        mutableStateOf(
            "• Do not rush or complete the step for the child.\n" +
            "• Do not repeat instructions constantly without waiting 5 seconds.\n" +
            "• Avoid expressing frustration or scolding.\n" +
            "• Do not jump to full physical assistance immediately."
        )
    }

    var safetyConsiderations by remember(selectedCategory) {
        mutableStateOf(
            when (selectedCategory) {
                "DRESSING" -> "Ensure child is seated comfortably on a stable chair or bed with safe footing. Ensure clothing fits comfortably."
                "SHOES" -> "Ensure child is seated stably with heels flat on the floor or stool. Check for proper shoe fit and dry soles."
                "EATING" -> "Ensure child is sitting upright with elbows supported on the table. Offer manageable food portions."
                else -> "Ensure a quiet, low-distraction environment with adequate lighting."
            }
        )
    }

    var isSaving by remember { mutableStateOf(false) }
    var isAssignedSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = if (isAssignedSuccess) "Home Program Assigned" else "Prescribe Home Programme",
                subtitle = "Child ID: #$childId • Assigned by Occupational Therapist",
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
            if (!isAssignedSuccess) {
                // SECTION 1: Patient Identity Banner with Strict Gender Enforcement
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("PEDIATRIC PATIENT PROFILE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
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
                                Text("Age: $displayAge yrs • Therapist: $doctorName", fontSize = 12.sp, color = SelforaTextSecondary)
                            }
                        }

                        // Locked Gender Badge (Shows ONLY the child's own gender)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (childGender == "GIRL") SelforaSecondary.copy(alpha = 0.14f) else SelforaPrimary.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, if (childGender == "GIRL") SelforaSecondary.copy(alpha = 0.35f) else SelforaPrimary.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = if (childGender == "GIRL") "👧 Girl Baby" else "👦 Boy Baby",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (childGender == "GIRL") SelforaSecondary else SelforaPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 2: ADL Category Selector (Dressing, Shoes, Eating)
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("1. SELECT ADL CATEGORY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        GlassChip(
                            text = "👕 Dressing",
                            selected = selectedCategory == "DRESSING",
                            onClick = { selectedCategory = "DRESSING" },
                            modifier = Modifier.weight(1f)
                        )
                        GlassChip(
                            text = "👟 Shoes",
                            selected = selectedCategory == "SHOES",
                            onClick = { selectedCategory = "SHOES" },
                            modifier = Modifier.weight(1f)
                        )
                        GlassChip(
                            text = "🥄 Eating",
                            selected = selectedCategory == "EATING",
                            onClick = { selectedCategory = "EATING" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 3: Activity Cards (Strictly filtered by child's gender)
                val activitySections = remember(childGender, selectedCategory) {
                    SelfidoCatalogue.getActivitiesForGenderAndCategory(childGender, selectedCategory)
                }

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

                        // Grid of Activity Boxes
                        activities.chunked(2).forEachIndexed { rowIndex, rowActivities ->
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
                                if (rowActivities.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            if (rowIndex < activities.chunked(2).size - 1) {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // SECTION 4: Starting Position Preparation for Caregiver
                selectedActivityDef?.let { act ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = SelforaPrimary.copy(alpha = 0.5f)
                    ) {
                        Text("CAREGIVER ACTIVITY PREPARATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(act.icon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(act.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                Text("${act.totalSteps} Total Steps • Gender: $childGender", fontSize = 12.sp, color = SelforaPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        if (act.startingPosition.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        "🎯 Starting Position Setup for Parent:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SelforaPrimary
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        act.startingPosition,
                                        fontSize = 12.sp,
                                        color = SelforaTextPrimary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // SECTION 5: Target Assistance / Prompt Level with Meaningful Parent Explanations
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("2. TARGET ASSISTANCE LEVEL (FOR PARENT)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Specify how much help the caregiver should provide during practice:", fontSize = 12.sp, color = SelforaTextSecondary)

                    Spacer(modifier = Modifier.height(10.dp))

                    val selectedLevelInfo = promptLevels.firstOrNull { it.id == targetPromptLevelId } ?: promptLevels[3]

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { promptLevelDropdownExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.5.dp, SelforaPrimary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(selectedLevelInfo.icon, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    selectedLevelInfo.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaTextPrimary
                                )
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SelforaPrimary)
                        }

                        DropdownMenu(
                            expanded = promptLevelDropdownExpanded,
                            onDismissRequest = { promptLevelDropdownExpanded = false }
                        ) {
                            promptLevels.forEach { lvl ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(lvl.icon, fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(lvl.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(lvl.parentAction, fontSize = 11.sp, color = SelforaTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    },
                                    onClick = {
                                        targetPromptLevelId = lvl.id
                                        promptLevelDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Clinical Explanatory Box for Parent Action
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SelforaPrimary.copy(alpha = 0.08f),
                        border = BorderStroke(0.8.dp, SelforaPrimary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                            Text("💡", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Caregiver Action:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                                Text(
                                    selectedLevelInfo.parentAction,
                                    fontSize = 12.sp,
                                    color = SelforaTextPrimary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 6: Target Routine Context & Schedule
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("3. PRACTICE ROUTINE & FREQUENCY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Routine Context:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))

                    val routineOptions = listOf(
                        Pair("MORNING", "🌅 Morning (Before School)"),
                        Pair("EVENING", "🌆 Evening (After Bath)"),
                        Pair("MEALTIME", "🍽️ Mealtime Routine"),
                        Pair("PRACTICE", "🎯 Focused Practice")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        routineOptions.chunked(2).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                row.forEach { (code, label) ->
                                    val isSelected = preferredRoutine == code
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) SelforaPrimary.copy(alpha = 0.12f) else Color.White,
                                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) SelforaPrimary else Color(0xFFE2E8F0)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { preferredRoutine = code }
                                    ) {
                                        Text(
                                            label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) SelforaPrimary else SelforaTextPrimary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Frequency & Duration
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Times per week:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(2, 3, 5, 7).forEach { f ->
                                    val isSel = frequencyPerWeek == f
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) SelforaPrimary else Color.White,
                                        border = BorderStroke(1.dp, if (isSel) SelforaPrimary else Color(0xFFE2E8F0)),
                                        modifier = Modifier.clickable { frequencyPerWeek = f }
                                    ) {
                                        Text(
                                            if (f == 7) "Daily" else "${f}x",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else SelforaTextPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Target duration:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(10, 15, 20).forEach { dur ->
                                    val isSel = targetDurationMinutes == dur
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) SelforaPrimary else Color.White,
                                        border = BorderStroke(1.dp, if (isSel) SelforaPrimary else Color(0xFFE2E8F0)),
                                        modifier = Modifier.clickable { targetDurationMinutes = dur }
                                    ) {
                                        Text(
                                            "${dur}m",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else SelforaTextPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 7: Focus Steps Selection (Optional Step Sub-Scoping)
                if (activitySteps.isNotEmpty()) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("4. TARGET FOCUS STEPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            TextButton(
                                onClick = {
                                    selectedStepIds = if (selectedStepIds.size == activitySteps.size) emptySet() else activitySteps.map { it.id }.toSet()
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    if (selectedStepIds.size == activitySteps.size) "Deselect All" else "Select All (${activitySteps.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Select which steps the parent should focus on during this 2-week home program:",
                            fontSize = 12.sp,
                            color = SelforaTextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            activitySteps.forEach { step ->
                                val isChecked = selectedStepIds.contains(step.id)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isChecked) SelforaPrimary.copy(alpha = 0.06f) else Color.White,
                                    border = BorderStroke(0.8.dp, if (isChecked) SelforaPrimary.copy(alpha = 0.35f) else Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedStepIds = if (isChecked) selectedStepIds - step.id else selectedStepIds + step.id
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                selectedStepIds = if (checked) selectedStepIds + step.id else selectedStepIds - step.id
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = SelforaPrimary)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text("Step ${step.stepNumber}: ${step.title}", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextPrimary)
                                            if (!step.instructionText.isNullOrBlank()) {
                                                Text(step.instructionText, fontSize = 11.sp, color = SelforaTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // SECTION 8: Meaningful Clinical Guidance & Instructions for Caregiver
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("5. ACTIONABLE CAREGIVER GUIDANCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Instructions for Parent / Caregiver:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    GlassTextField(
                        value = caregiverInstructions,
                        onValueChange = { caregiverInstructions = it },
                        label = "Caregiver Step-by-Step Guidance",
                        placeholder = "Specific instructions for the parent...",
                        singleLine = false,
                        leadingIcon = Icons.Default.EditNote
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Things for Parent to Avoid:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    GlassTextField(
                        value = thingsToAvoid,
                        onValueChange = { thingsToAvoid = it },
                        label = "Common Pitfalls to Avoid",
                        placeholder = "What the parent should NOT do...",
                        singleLine = false,
                        leadingIcon = Icons.Default.Block
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Safety & Environment Notes:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    GlassTextField(
                        value = safetyConsiderations,
                        onValueChange = { safetyConsiderations = it },
                        label = "Safety Considerations",
                        placeholder = "Safety notes for home environment...",
                        singleLine = false,
                        leadingIcon = Icons.Default.Security
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                GlassButton(
                    text = if (isSaving) "Prescribing Programme..." else "Prescribe Home Programme ✓",
                    onClick = {
                        val act = selectedActivityDef
                        if (act == null) {
                            Toast.makeText(context, "Please select an activity", Toast.LENGTH_SHORT).show()
                            return@GlassButton
                        }
                        if (selectedStepIds.isEmpty()) {
                            Toast.makeText(context, "Please select at least one focus step", Toast.LENGTH_SHORT).show()
                            return@GlassButton
                        }

                        val targetLevelName = promptLevels.firstOrNull { it.id == targetPromptLevelId }?.shortName ?: "Verbal Prompt"
                        val routineLabel = when (preferredRoutine) {
                            "MORNING" -> "Morning Routine"
                            "EVENING" -> "Evening Routine"
                            "MEALTIME" -> "Mealtime Routine"
                            else -> "Practice Session"
                        }

                        val goalStmt = "Goal: Perform ${act.title} (${selectedStepIds.size} focus steps) with $targetLevelName during $routineLabel (${frequencyPerWeek}x per week)."

                        scope.launch {
                            isSaving = true
                            try {
                                val req = CreateHomeProgramRequest(
                                    childId = childId,
                                    activityId = act.id,
                                    title = "${act.title} Home Practice ($childGender)",
                                    description = act.description.ifBlank { "Therapist-prescribed ADL home practice." },
                                    priority = "HIGH",
                                    scheduleType = "DAILY",
                                    preferredTime = preferredRoutine,
                                    frequencyPerWeek = frequencyPerWeek,
                                    targetDurationMinutes = targetDurationMinutes,
                                    targetPromptLevelId = targetPromptLevelId,
                                    goalStatement = goalStmt,
                                    caregiverInstructions = caregiverInstructions,
                                    stepSpecificInstructions = "Follow visual cues and celebrate independent attempts.",
                                    safetyConsiderations = safetyConsiderations,
                                    thingsToAvoid = thingsToAvoid,
                                    encouragementSuggestions = "Enthusiastically praise effort with high-fives and words of encouragement!",
                                    startDate = LocalDate.now().toString(),
                                    endDate = LocalDate.now().plusWeeks(2).toString(),
                                    targetStepIds = selectedStepIds.toList()
                                )
                                val res = ApiClient.apiService.createHomeProgram(req)
                                if (res.isSuccessful) {
                                    isAssignedSuccess = true
                                    Toast.makeText(context, "Home Programme prescribed successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Server response: ${res.code()}", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    icon = Icons.Default.Check,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )
            } else {
                // Confirmation State
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(shape = CircleShape, color = SelforaSuccess.copy(alpha = 0.15f), modifier = Modifier.size(72.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SelforaSuccess, modifier = Modifier.size(42.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Home Programme Prescribed!", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = SelforaTextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "The caregiver will now see this clear, meaningful practice activity on their dashboard.",
                        fontSize = 13.sp,
                        color = SelforaTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("SUMMARY OF PRESCRIBED PRACTICE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Activity: ${selectedActivityDef?.title ?: "ADL Activity"} ($childGender)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("• Focus Steps: ${selectedStepIds.size} of ${activitySteps.size} selected", fontSize = 12.5.sp)
                        Text("• Target Assistance: ${promptLevels.firstOrNull { it.id == targetPromptLevelId }?.title}", fontSize = 12.5.sp, color = SelforaPrimary, fontWeight = FontWeight.SemiBold)
                        Text("• Practice Schedule: $frequencyPerWeek times / week ($targetDurationMinutes min)", fontSize = 12.5.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Caregiver instruction and starting position guidance have been synced to the parent portal.",
                                fontSize = 11.5.sp,
                                color = SelforaTextSecondary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    GlassButton(
                        text = "Return to Child Profile",
                        onClick = onProgramCreated,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}
