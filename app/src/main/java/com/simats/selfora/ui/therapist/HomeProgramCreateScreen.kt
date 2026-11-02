package com.simats.selfora.ui.therapist

import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.ActivityResponse
import com.simats.selfora.data.model.CreateHomeProgramRequest
import com.simats.selfora.data.model.TaskStepResponse
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate

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

    var isAssignedSuccess by remember { mutableStateOf(false) }
    var activitiesList by remember { mutableStateOf<List<ActivityResponse>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("DRESSING") }
    var selectedActivity by remember { mutableStateOf<ActivityResponse?>(null) }
    var activitySteps by remember { mutableStateOf<List<TaskStepResponse>>(emptyList()) }
    var selectedStepIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var isActivitiesLoading by remember { mutableStateOf(true) }

    var targetPromptLevelId by remember { mutableStateOf(3) } // Verbal default
    var promptLevelDropdownExpanded by remember { mutableStateOf(false) }
    var activityDropdownExpanded by remember { mutableStateOf(false) }

    var frequencyPerWeek by remember { mutableStateOf(3) }
    var targetDurationMinutes by remember { mutableStateOf(15) }
    var preferredTime by remember { mutableStateOf("Morning / Evening") }
    var caregiverInstructions by remember { mutableStateOf("Provide clear verbal guidance and encourage independent effort.") }
    var safetyConsiderations by remember { mutableStateOf("Ensure safe footing and adequate supervision.") }

    var isSaving by remember { mutableStateOf(false) }

    val promptLevels = listOf(
        Pair(0, "Level 0: Independent"),
        Pair(1, "Level 1: Visual Prompt"),
        Pair(2, "Level 2: Gesture"),
        Pair(3, "Level 3: Verbal Prompt"),
        Pair(4, "Level 4: Model / Video"),
        Pair(5, "Level 5: Partial Physical"),
        Pair(6, "Level 6: Full Physical")
    )

    // Load Activities
    LaunchedEffect(Unit) {
        isActivitiesLoading = true
        try {
            val res = ApiClient.apiService.getAllActivities()
            if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                activitiesList = res.body()!!
                selectedActivity = activitiesList.firstOrNull { it.categoryName == "DRESSING" } ?: activitiesList.first()
            }
        } catch (_: Exception) {}
        finally {
            isActivitiesLoading = false
        }
    }

    // Load Task Steps when selected activity changes
    LaunchedEffect(selectedActivity) {
        val act = selectedActivity ?: return@LaunchedEffect
        try {
            val stepRes = ApiClient.apiService.getTaskSteps(act.id)
            if (stepRes.isSuccessful && stepRes.body() != null) {
                activitySteps = stepRes.body()!!
                selectedStepIds = activitySteps.map { it.id }.toSet()
            }
        } catch (_: Exception) {}
    }

    val filteredActivities = activitiesList.filter { act ->
        when (selectedCategory) {
            "DRESSING" -> act.categoryName == "DRESSING" && !act.title.contains("Shoe", ignoreCase = true) && !act.title.contains("Sock", ignoreCase = true)
            "SHOES" -> act.title.contains("Shoe", ignoreCase = true) || act.title.contains("Sock", ignoreCase = true)
            "EATING" -> act.categoryName == "EATING"
            else -> true
        }
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = if (isAssignedSuccess) "Home Program Assigned" else "Assign Home Program",
                subtitle = if (isAssignedSuccess) "Confirmation" else "Therapist-Prescribed Home Practice",
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
                // Category Filter
                Text("Select ADL Category", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    GlassChip(
                        text = "👕 Dressing",
                        selected = selectedCategory == "DRESSING",
                        onClick = {
                            selectedCategory = "DRESSING"
                            selectedActivity = activitiesList.firstOrNull { it.categoryName == "DRESSING" && !it.title.contains("Shoe") && !it.title.contains("Sock") }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    GlassChip(
                        text = "🥄 Eating",
                        selected = selectedCategory == "EATING",
                        onClick = {
                            selectedCategory = "EATING"
                            selectedActivity = activitiesList.firstOrNull { it.categoryName == "EATING" }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    GlassChip(
                        text = "👟 Shoes",
                        selected = selectedCategory == "SHOES",
                        onClick = {
                            selectedCategory = "SHOES"
                            selectedActivity = activitiesList.firstOrNull { it.title.contains("Shoe") || it.title.contains("Sock") }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Activity Selector
                Text("Select Target Activity", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activityDropdownExpanded = true }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎯", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(selectedActivity?.title ?: "Select Activity", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                Text(selectedActivity?.description ?: "ADL task for practice", fontSize = 12.sp, color = SelforaTextSecondary)
                            }
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SelforaPrimary)
                    }

                    DropdownMenu(
                        expanded = activityDropdownExpanded,
                        onDismissRequest = { activityDropdownExpanded = false }
                    ) {
                        filteredActivities.forEach { act ->
                            DropdownMenuItem(
                                text = { Text(act.title, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    selectedActivity = act
                                    activityDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Target Prompt Level
                Text("Target Assistance / Prompt Level", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { promptLevelDropdownExpanded = true }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val levelName = promptLevels.firstOrNull { it.first == targetPromptLevelId }?.second ?: "Level 3: Verbal Prompt"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SelforaPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                levelName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SelforaPrimary)
                    }

                    DropdownMenu(
                        expanded = promptLevelDropdownExpanded,
                        onDismissRequest = { promptLevelDropdownExpanded = false }
                    ) {
                        promptLevels.forEach { (id, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    targetPromptLevelId = id
                                    promptLevelDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Target Steps Checklist
                if (activitySteps.isNotEmpty()) {
                    Text("Focus Steps (${selectedStepIds.size} of ${activitySteps.size} selected)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            activitySteps.forEach { step ->
                                val isChecked = selectedStepIds.contains(step.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedStepIds = if (isChecked) selectedStepIds - step.id else selectedStepIds + step.id
                                        },
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
                                    Text("Step ${step.stepNumber}: ${step.title}", fontSize = 13.sp, color = SelforaTextPrimary)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Practice Schedule & Instructions
                Text("Caregiver Guidance Instructions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                GlassTextField(
                    value = caregiverInstructions,
                    onValueChange = { caregiverInstructions = it },
                    label = "Instructions for Parent / Caregiver",
                    placeholder = "Specify how the caregiver should prompt and reinforce...",
                    singleLine = false,
                    leadingIcon = Icons.Default.EditNote
                )

                Spacer(modifier = Modifier.height(14.dp))

                GlassTextField(
                    value = safetyConsiderations,
                    onValueChange = { safetyConsiderations = it },
                    label = "Safety & Environment Notes",
                    placeholder = "e.g. Ensure dry floor, adequate supervision...",
                    singleLine = false,
                    leadingIcon = Icons.Default.Security
                )

                Spacer(modifier = Modifier.height(28.dp))

                GlassButton(
                    text = if (isSaving) "Assigning Programme..." else "Assign Home Programme ✓",
                    onClick = {
                        val act = selectedActivity
                        if (act == null) {
                            Toast.makeText(context, "Please select an activity", Toast.LENGTH_SHORT).show()
                            return@GlassButton
                        }
                        scope.launch {
                            isSaving = true
                            try {
                                val req = CreateHomeProgramRequest(
                                    childId = childId,
                                    activityId = act.id,
                                    title = "${act.title} Home Practice",
                                    description = act.description ?: "Assigned ADL practice",
                                    priority = "NORMAL",
                                    scheduleType = "DAILY",
                                    preferredTime = preferredTime,
                                    frequencyPerWeek = frequencyPerWeek,
                                    targetDurationMinutes = targetDurationMinutes,
                                    targetPromptLevelId = targetPromptLevelId,
                                    goalStatement = "Target: Complete ${selectedStepIds.size} steps with ${promptLevels.firstOrNull { it.first == targetPromptLevelId }?.second ?: "assistance"}",
                                    caregiverInstructions = caregiverInstructions,
                                    stepSpecificInstructions = "Follow visual step cues and praise independent attempts.",
                                    safetyConsiderations = safetyConsiderations,
                                    thingsToAvoid = "Do not take over the activity immediately without giving child time to attempt.",
                                    encouragementSuggestions = "Cheer and celebrate upon completion!",
                                    startDate = LocalDate.now().toString(),
                                    endDate = LocalDate.now().plusWeeks(2).toString(),
                                    targetStepIds = selectedStepIds.toList()
                                )
                                val res = ApiClient.apiService.createHomeProgram(req)
                                if (res.isSuccessful) {
                                    isAssignedSuccess = true
                                    Toast.makeText(context, "Home Programme assigned successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Error assigning programme: ${res.code()}", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    icon = Icons.Default.Check,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                )
            } else {
                // Confirmation State
                Spacer(modifier = Modifier.height(30.dp))
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
                    Text("Home Programme Assigned!", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = SelforaTextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("The caregiver will now see this practice activity on their dashboard.", fontSize = 13.sp, color = SelforaTextSecondary)

                    Spacer(modifier = Modifier.height(30.dp))

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
