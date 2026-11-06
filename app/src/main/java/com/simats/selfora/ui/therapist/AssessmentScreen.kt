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
import com.simats.selfora.utils.LanguageManager
import com.simats.selfora.utils.TtsManager


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
    val titleTa: String? = null,
    val instructionTa: String? = null,
    val mediaUrl: String? = null,
    var promptLevelId: Int? = null, // 0..6
    var outcome: String = "PENDING", // COMPLETED, UNABLE, NOT_ASSESSED
    var notes: String = "",
    var notAssessedReason: String = ""
)





@OptIn(ExperimentalMaterial3Api::class)


@Composable


fun AssessmentScreen(


    childId: Long,


    onChildSelected: ((Long) -> Unit)? = null,


    onAssessmentSaved: () -> Unit,


    onBack: () -> Unit


) {


    var activeChildId by remember(childId) { mutableStateOf(if (childId > 0) childId else 0L) }


    var assignedChildren by remember { mutableStateOf<List<ChildSummaryItem>>(emptyList()) }


    var isChildrenLoading by remember { mutableStateOf(false) }





    var isStarted by remember { mutableStateOf(false) }


    var isShelfLayout by remember { mutableStateOf(false) } // Default to modern 2x2 grid view matching reference design





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





    // Child caseload search query & filtered list


    var childSearchQuery by remember { mutableStateOf("") }


    val filteredChildren = remember(assignedChildren, childSearchQuery) {


        if (childSearchQuery.isBlank()) {


            assignedChildren


        } else {


            val q = childSearchQuery.trim().lowercase()


            assignedChildren.filter {


                it.name.lowercase().contains(q) ||


                it.id.toString().contains(q) ||


                (it.caregiverName.isNotBlank() && it.caregiverName.lowercase().contains(q))


            }


        }


    }





    val scope = rememberCoroutineScope()


    val context = LocalContext.current
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isTamil = currentLanguage == "ta"
    val ttsManager = remember { TtsManager(context) }
    LaunchedEffect(currentLanguage) {
        ttsManager.setLanguage(if (isTamil) "ta" else "en")
    }
    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
        }
    }


    val currentDate = remember { LocalDate.now().toString() }





    // Automatically load therapist assigned children when caseload is empty or childId is zero


    LaunchedEffect(Unit) {


        if (assignedChildren.isEmpty()) {


            isChildrenLoading = true


            try {


                val res = ApiClient.apiService.getTherapistChildren()


                if (res.isSuccessful && !res.body().isNullOrEmpty()) {


                    val list = res.body()!!


                    assignedChildren = list


                    if (activeChildId <= 0L) {


                        activeChildId = list.first().id


                        onChildSelected?.invoke(list.first().id)


                    }


                }


            } catch (_: Exception) {}


            finally {


                isChildrenLoading = false


            }


        }


    }





    // Fetch Child Details whenever activeChildId changes


    LaunchedEffect(activeChildId) {


        if (activeChildId <= 0L) {


            childLoading = false


            return@LaunchedEffect


        }


        childLoading = true


        try {


            val res = ApiClient.apiService.getChildById(activeChildId)


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





    // ADL Category State


    var selectedCategory by remember(childGender) {


        mutableStateOf(if (childGender == null) "SHOES" else "DRESSING")


    } // "DRESSING", "SHOES", "EATING"





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
                            instruction = it.instructionText ?: it.childInstruction ?: "",
                            titleTa = it.titleTa,
                            instructionTa = it.instructionTextTa ?: it.childInstructionTa,
                            mediaUrl = it.media.firstOrNull()?.mediaUrl
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


            if (!isStarted) {


                val doctorName = childDto?.therapistName?.ifBlank { null }


                    ?: com.simats.selfora.data.local.SessionManager.getUsername().ifBlank { null }


                    ?: "Jenkins"


                StartSessionTopBar(


                    childId = activeChildId,


                    currentDate = currentDate,


                    therapistName = doctorName,


                    therapistDesignation = "Occupational Therapist (OT)",


                    title = "Clinical ADL Assessment",


                    onBackClick = null


                )


            } else {


                GlassTopBar(


                    title = "Assessment: $selectedActivityTitle",


                    subtitle = "Step ${currentStepIndex + 1} of ${stepStates.size} • Child ID: #$activeChildId",


                    onBackClick = null,
                    actions = {
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                            color = SelforaError.copy(alpha = 0.10f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SelforaError.copy(alpha = 0.30f)),
                            modifier = Modifier.clickable { showExitConfirmationDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Exit Assessment",
                                    tint = SelforaError,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Exit",
                                    fontSize = 11.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = SelforaError
                                )
                            }
                        }
                    },


                    accentColor = SelforaPrimary


                )


            }


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


                if (!started) {


                    // Empty caseload handling


                    if (activeChildId <= 0L && !isChildrenLoading && assignedChildren.isEmpty()) {


                        Surface(


                            modifier = Modifier.fillMaxWidth(),


                            shape = RoundedCornerShape(20.dp),


                            color = Color.White,


                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),


                            shadowElevation = 1.dp


                        ) {


                            Column(


                                horizontalAlignment = Alignment.CenterHorizontally,


                                modifier = Modifier.padding(24.dp)


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


                                Text("No Assigned Children Found", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaTextPrimary)


                                Spacer(modifier = Modifier.height(6.dp))


                                Text(


                                    "No children are currently assigned to your caseload. Please assign or register children from the Children list or Dashboard.",


                                    color = SelforaTextSecondary,


                                    fontSize = 13.sp,


                                    textAlign = TextAlign.Center


                                )


                                Spacer(modifier = Modifier.height(16.dp))


                                Button(


                                    onClick = onBack,


                                    colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary),


                                    shape = RoundedCornerShape(12.dp)


                                ) {


                                    Text("Go to Dashboard", fontWeight = FontWeight.Bold)


                                }


                            }


                        }


                    } else {


                        // =========================================================================


                        // PATIENT CASELOAD & REAL-TIME SEARCH (Clean, Pixel-Perfect Alignment)


                        // =========================================================================


                        if (assignedChildren.isNotEmpty()) {


                            Column(


                                modifier = Modifier.fillMaxWidth(),


                                verticalArrangement = Arrangement.spacedBy(10.dp)


                            ) {


                                // Section Header Row


                                Row(


                                    modifier = Modifier.fillMaxWidth(),


                                    horizontalArrangement = Arrangement.SpaceBetween,


                                    verticalAlignment = Alignment.CenterVertically


                                ) {


                                    Text(


                                        text = "SELECT PATIENT CASELOAD",


                                        fontSize = 11.5.sp,


                                        fontWeight = FontWeight.Bold,


                                        color = SelforaPrimary,


                                        letterSpacing = 0.5.sp


                                    )





                                    Surface(


                                        shape = RoundedCornerShape(8.dp),


                                        color = if (childSearchQuery.isNotBlank() && filteredChildren.isEmpty()) SelforaError.copy(alpha = 0.12f) else SelforaPrimary.copy(alpha = 0.08f)


                                    ) {


                                        Text(


                                            text = if (childSearchQuery.isNotBlank()) "${filteredChildren.size} of ${assignedChildren.size} Found" else "${assignedChildren.size} Children Assigned",


                                            fontSize = 10.5.sp,


                                            color = if (childSearchQuery.isNotBlank() && filteredChildren.isEmpty()) SelforaError else SelforaPrimary,


                                            fontWeight = FontWeight.Bold,


                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)


                                        )


                                    }


                                }





                                // Pixel-perfect Real-time Child Search Input


                                GlassSearchBar(


                                    query = childSearchQuery,


                                    onQueryChange = { childSearchQuery = it },


                                    placeholder = "Search patient by name, ID (e.g. Kevin, #13)...",


                                    onClear = { childSearchQuery = "" },


                                    modifier = Modifier.fillMaxWidth()


                                )





                                // Real-time Filtered Children Pills


                                if (filteredChildren.isEmpty()) {


                                    Surface(


                                        shape = RoundedCornerShape(14.dp),


                                        color = Color.White,


                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),


                                        shadowElevation = 1.dp,


                                        modifier = Modifier.fillMaxWidth()


                                    ) {


                                        Row(


                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),


                                            verticalAlignment = Alignment.CenterVertically,


                                            horizontalArrangement = Arrangement.SpaceBetween


                                        ) {


                                            Text(


                                                text = "No children match \"$childSearchQuery\"",


                                                fontSize = 12.5.sp,


                                                color = SelforaTextSecondary


                                            )


                                            Text(


                                                text = "Clear search",


                                                fontSize = 12.sp,


                                                fontWeight = FontWeight.Bold,


                                                color = SelforaPrimary,


                                                modifier = Modifier.clickable { childSearchQuery = "" }


                                            )


                                        }


                                    }


                                } else {


                                    Row(


                                        modifier = Modifier


                                            .fillMaxWidth()


                                            .horizontalScroll(rememberScrollState()),


                                        horizontalArrangement = Arrangement.spacedBy(8.dp)


                                    ) {


                                        filteredChildren.forEach { childItem ->


                                            val isSelected = childItem.id == activeChildId


                                            val childGenderShort = childItem.gender.trim().uppercase()


                                            val avatarEmoji = if (childGenderShort.startsWith("G") || childGenderShort.startsWith("F")) "👧" else "👦"


                                            Surface(


                                                shape = RoundedCornerShape(12.dp),


                                                color = if (isSelected) SelforaPrimary else Color.White,


                                                border = BorderStroke(1.dp, if (isSelected) SelforaPrimary else Color(0xFFE2E8F0)),


                                                shadowElevation = if (isSelected) 2.dp else 1.dp,


                                                modifier = Modifier


                                                    .height(42.dp)


                                                    .clickable {


                                                        activeChildId = childItem.id


                                                        manualGenderOverride = null


                                                        onChildSelected?.invoke(childItem.id)


                                                    }


                                            ) {


                                                Row(


                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),


                                                    verticalAlignment = Alignment.CenterVertically


                                                ) {


                                                    Text(avatarEmoji, fontSize = 15.sp)


                                                    Spacer(modifier = Modifier.width(6.dp))


                                                    Text(


                                                        text = childItem.name,


                                                        fontSize = 12.5.sp,


                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,


                                                        color = if (isSelected) Color.White else SelforaTextPrimary


                                                    )


                                                    Spacer(modifier = Modifier.width(5.dp))


                                                    Text(


                                                        text = "#${childItem.id}",


                                                        fontSize = 11.sp,


                                                        fontWeight = FontWeight.SemiBold,


                                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else SelforaTextSecondary


                                                    )


                                                    if (isSelected) {


                                                        Spacer(modifier = Modifier.width(6.dp))


                                                        Icon(


                                                            imageVector = Icons.Default.Check,


                                                            contentDescription = "Selected",


                                                            tint = Color.White,


                                                            modifier = Modifier.size(14.dp)


                                                        )


                                                    }


                                                }


                                            }


                                        }


                                    }


                                }


                            }


                            Spacer(modifier = Modifier.height(14.dp))


                        }





                        // =========================================================================


                        // SECTION 1: Patient Context Card & Interactive Dual-Gender Switch


                        // =========================================================================


                        PatientContextCard(


                            childDto = childDto,


                            childId = activeChildId,


                            childGender = childGender,


                            onSelectGender = { g ->


                                manualGenderOverride = g


                                if (selectedCategory == "DRESSING") {


                                    selectedActivityId = if (g == "GIRL") 17L else 1L


                                }


                            }


                        )





                        Spacer(modifier = Modifier.height(14.dp))





                        // =========================================================================


                        // SECTION 2: ADL Category Tabs (Dresses, Footwear, Food) + Grid/Shelf Toggle


                        // =========================================================================


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





                        // =========================================================================


                        // SECTION 3: Structured Activity Boxes by Category and Gender


                        // =========================================================================


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





                        // =========================================================================


                        // SECTION 4: Selected Activity Overview & Launch Assessment Button


                        // =========================================================================


                        SelectedActivityOverviewCard(


                            activity = selectedActivityDef,


                            childGender = childGender,


                            selectedCategory = selectedCategory,


                            cardTitle = "SELECTED ADL FOR CLINICAL ASSESSMENT",


                            buttonText = "Start Clinical Assessment",


                            onStartSessionClick = {


                                loadActivitySteps(selectedActivityId)


                                isStarted = true


                            }


                        )





                        Spacer(modifier = Modifier.height(16.dp))


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


                        Surface(


                            modifier = Modifier.fillMaxWidth(),


                            shape = RoundedCornerShape(18.dp),


                            color = Color.White,


                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),


                            shadowElevation = 1.dp


                        ) {


                            Column(modifier = Modifier.padding(16.dp)) {


                                Row(


                                    modifier = Modifier.fillMaxWidth(),


                                    horizontalArrangement = Arrangement.SpaceBetween,


                                    verticalAlignment = Alignment.CenterVertically


                                ) {


                                    Column(modifier = Modifier.weight(1f)) {


                                        Text(selectedActivityTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaTextPrimary)


                                        Text("Task Step Assessment (${childGender ?: "Common"})", fontSize = 11.5.sp, color = SelforaTextSecondary)


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
                                gender = childGender ?: "BOY",
                                activityTitle = selectedActivityTitle,
                                isTamil = isTamil,
                                stepTitleTa = step.titleTa,
                                childInstructionTa = step.instructionTa,
                                mediaUrl = step.mediaUrl,
                                onSpeakInstruction = {
                                    val text = if (isTamil && !step.instructionTa.isNullOrBlank()) step.instructionTa!! else step.instruction
                                    ttsManager.speak(text)
                                },
                                onSpeakCustomInstruction = { text, isTa ->
                                    ttsManager.speakInLanguage(text, if (isTa) "ta" else "en")
                                }
                            )


                        }





                        Spacer(modifier = Modifier.height(18.dp))





                        // CLINICAL PROMPT HIERARCHY SELECTION CARD (L0 - L6, Unable, Not Assessed)


                        Surface(


                            modifier = Modifier.fillMaxWidth(),


                            shape = RoundedCornerShape(20.dp),


                            color = Color.White,


                            border = BorderStroke(1.5.dp, SelforaPrimary.copy(alpha = 0.3f)),


                            shadowElevation = 2.dp


                        ) {


                            Column(modifier = Modifier.padding(16.dp)) {


                                Row(verticalAlignment = Alignment.CenterVertically) {


                                    Icon(Icons.Default.Psychology, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(22.dp))


                                    Spacer(modifier = Modifier.width(8.dp))


                                    Text("CLINICAL ASSISTANCE / PROMPT LEVEL", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)


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


                                                .shadow(if (isSelected) 3.dp else 0.5.dp, shape = RoundedCornerShape(14.dp))


                                                .clip(RoundedCornerShape(14.dp))


                                                .background(if (isSelected) levelColor else Color.White)


                                                .border(


                                                    width = if (isSelected) 2.dp else 1.dp,


                                                    color = if (isSelected) SelforaPrimary else Color(0xFFE2E8F0),


                                                    shape = RoundedCornerShape(14.dp)


                                                )


                                                .clickable {


                                                    stepStates = stepStates.toMutableList().also {


                                                        it[currentStepIndex] = currentStep.copy(promptLevelId = levelId, outcome = "COMPLETED")


                                                    }


                                                }


                                                .padding(vertical = 11.dp, horizontal = 12.dp),


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


                                                    fontSize = 13.5.sp,


                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,


                                                    color = if (isSelected) Color.White else SelforaTextPrimary


                                                )


                                            }


                                        }


                                    }





                                    // Separate Options: Unable & Not Assessed


                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {


                                        val isUnableSelected = currentStep.outcome == "UNABLE"


                                        Button(


                                            onClick = {


                                                stepStates = stepStates.toMutableList().also {


                                                    it[currentStepIndex] = currentStep.copy(outcome = "UNABLE", promptLevelId = 7)


                                                }


                                            },


                                            shape = RoundedCornerShape(12.dp),


                                            colors = ButtonDefaults.buttonColors(


                                                containerColor = if (isUnableSelected) SelforaError else Color(0xFFFEE2E2),


                                                contentColor = if (isUnableSelected) Color.White else SelforaError


                                            ),


                                            border = BorderStroke(1.dp, if (isUnableSelected) SelforaError else Color(0xFFFCA5A5)),


                                            modifier = Modifier.weight(1f).height(46.dp)


                                        ) {


                                            Text(if (isUnableSelected) "✓ Unable" else "Unable", fontWeight = FontWeight.Bold, fontSize = 13.sp)


                                        }





                                        val isNotAssessedSelected = currentStep.outcome == "NOT_ASSESSED"


                                        Button(


                                            onClick = {


                                                notAssessedReasonInput = currentStep.notAssessedReason


                                                showNotAssessedDialog = true


                                            },


                                            shape = RoundedCornerShape(12.dp),


                                            colors = ButtonDefaults.buttonColors(


                                                containerColor = if (isNotAssessedSelected) SelforaWarning else Color(0xFFFEF3C7),


                                                contentColor = if (isNotAssessedSelected) Color.White else Color(0xFF92400E)


                                            ),


                                            border = BorderStroke(1.dp, if (isNotAssessedSelected) SelforaWarning else Color(0xFFFDE68A)),


                                            modifier = Modifier.weight(1f).height(46.dp)


                                        ) {


                                            Text(if (isNotAssessedSelected) "✓ Not Assessed" else "Not Assessed", fontWeight = FontWeight.Bold, fontSize = 13.sp)


                                        }


                                    }


                                }





                                Spacer(modifier = Modifier.height(16.dp))





                                // Therapist Observation Notes Input


                                OutlinedTextField(


                                    value = currentStep.notes,


                                    onValueChange = { newNotes ->


                                        stepStates = stepStates.toMutableList().also {


                                            it[currentStepIndex] = currentStep.copy(notes = newNotes)


                                        }


                                    },


                                    label = { Text("Therapist Observation Notes (Optional)", fontSize = 12.sp) },


                                    placeholder = { Text("Record specific clinical observations for this step...", fontSize = 12.sp) },


                                    singleLine = false,


                                    maxLines = 3,


                                    leadingIcon = {


                                        Icon(Icons.Default.EditNote, contentDescription = null, tint = SelforaPrimary)


                                    },


                                    shape = RoundedCornerShape(14.dp),


                                    colors = OutlinedTextFieldDefaults.colors(


                                        focusedBorderColor = SelforaPrimary,


                                        unfocusedBorderColor = Color(0xFFCBD5E1),


                                        focusedContainerColor = Color.White,


                                        unfocusedContainerColor = Color(0xFFF8FAFC)


                                    ),


                                    modifier = Modifier.fillMaxWidth()


                                )





                                Spacer(modifier = Modifier.height(20.dp))





                                // Step Navigation Controls (Prev / Next / Finish)


                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {


                                    if (currentStepIndex > 0) {


                                        OutlinedButton(


                                            onClick = { currentStepIndex-- },


                                            shape = RoundedCornerShape(12.dp),


                                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),


                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SelforaTextPrimary),


                                            modifier = Modifier.weight(1f).height(48.dp)


                                        ) {


                                            Text("← Previous", fontWeight = FontWeight.SemiBold)


                                        }


                                    }





                                    Button(


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


                                        colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary),


                                        shape = RoundedCornerShape(12.dp),


                                        modifier = Modifier.weight(1.5f).height(48.dp)


                                    ) {


                                        Text(


                                            text = if (currentStepIndex == totalSteps - 1) "Review Assessment ✓" else "Next Step →",


                                            fontWeight = FontWeight.Bold,


                                            color = Color.White


                                        )


                                    }


                                }


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


                    OutlinedTextField(


                        value = notAssessedReasonInput,


                        onValueChange = { notAssessedReasonInput = it },


                        label = { Text("Reason") },


                        placeholder = { Text("e.g. Physical constraint, sensory aversion...") },


                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),


                        keyboardActions = KeyboardActions(onDone = { confirmNotAssessed() }),


                        shape = RoundedCornerShape(12.dp),


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


                    Text("Activity: $selectedActivityTitle (${childGender ?: "Common"})", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)


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


                                    childId = activeChildId,


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


 * Backward-compatible helper for ActivitySelectionCard.


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
                            instruction = it.instructionText ?: it.childInstruction ?: "",
                            titleTa = it.titleTa,
                            instructionTa = it.instructionTextTa ?: it.childInstructionTa,
                            mediaUrl = it.media.firstOrNull()?.mediaUrl
                        )


    }


}


