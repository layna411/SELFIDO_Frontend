package com.simats.selfora.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.CreateHomeProgramRequest
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeProgramCreateScreen(
    childId: Long = 1L,
    onProgramCreated: () -> Unit,
    onBack: () -> Unit
) {
    var programTitle by remember { mutableStateOf("Boy T-Shirt Dressing Practice Programme") }
    var selectedCategory by remember { mutableStateOf("Dressing") }
    var activityId by remember { mutableStateOf(1L) }
    var scheduleType by remember { mutableStateOf("DAILY") }
    var frequency by remember { mutableStateOf("4") }
    var duration by remember { mutableStateOf("15") }
    var preferredTime by remember { mutableStateOf("Morning after bath") }
    var priority by remember { mutableStateOf("NORMAL") }
    var targetPromptLevel by remember { mutableStateOf(1) } // Visual
    var goalStatement by remember { mutableStateOf("Increase independent dressing performance to 80% with visual prompt cards at home.") }
    var caregiverInstructions by remember { mutableStateOf("Guide child through Steps 4, 7, and 10 at home using picture cues. Offer visual prompt first before offering verbal guidance.") }
    var safetyConsiderations by remember { mutableStateOf("Ensure child is seated safely on bed or chair during pant and shoe positioning.") }
    var thingsToAvoid by remember { mutableStateOf("Do not rush or physically force limbs through shirt sleeves if child exhibits resistance.") }
    var encouragementSuggestions by remember { mutableStateOf("Offer high-fives or reward stars after each completed step!") }

    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val adlCategories = listOf(
        "Dressing" to 1L,
        "Eating" to 13L,
        "Grooming" to 5L,
        "Shoes & Socks" to 9L
    )

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "Assign Home Programme",
                subtitle = "Therapist-Prescribed ADL Practice",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("HOME PROGRAMME SPECIFICATION", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SelforaPrimary)
                    Text("Prescribe structured ADL practice for home environment", fontSize = 11.sp, color = SelforaTextSecondary)
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = programTitle,
                        onValueChange = { programTitle = it },
                        label = { Text("Programme Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ADL Category Selection
                    Text("Select ADL Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        adlCategories.forEach { (catName, catActId) ->
                            val isSelected = selectedCategory == catName
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCategory = catName
                                    activityId = catActId
                                },
                                label = { Text(catName, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Practice Schedule Config
                    Text("Schedule & Practice Configuration:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("DAILY" to "Daily", "ALTERNATE_DAYS" to "Alt Days", "WEEKLY" to "Weekly").forEach { (type, label) ->
                            FilterChip(
                                selected = scheduleType == type,
                                onClick = { scheduleType = type },
                                label = { Text(label, fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = frequency,
                            onValueChange = { frequency = it },
                            label = { Text("Sessions/Wk") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Duration (Mins)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = preferredTime,
                        onValueChange = { preferredTime = it },
                        label = { Text("Preferred Practice Time") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = goalStatement,
                        onValueChange = { goalStatement = it },
                        label = { Text("Clinical Goal Statement") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dedicated Caregiver Instruction Editor
                    Text("CAREGIVER INSTRUCTION EDITOR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaSecondary)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = caregiverInstructions,
                        onValueChange = { caregiverInstructions = it },
                        label = { Text("Step-by-Step Caregiver Instructions") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = safetyConsiderations,
                        onValueChange = { safetyConsiderations = it },
                        label = { Text("Safety Considerations") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = thingsToAvoid,
                        onValueChange = { thingsToAvoid = it },
                        label = { Text("Things Caregiver Should Avoid") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = encouragementSuggestions,
                        onValueChange = { encouragementSuggestions = it },
                        label = { Text("Positive Encouragement Suggestions") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlassButton(
                text = "ASSIGN HOME PROGRAMME TO CAREGIVER",
                onClick = {
                    scope.launch {
                        isSaving = true
                        try {
                            val req = CreateHomeProgramRequest(
                                childId = childId,
                                activityId = activityId,
                                title = programTitle,
                                description = "Prescribed home practice for $selectedCategory",
                                priority = priority,
                                scheduleType = scheduleType,
                                preferredTime = preferredTime,
                                frequencyPerWeek = frequency.toIntOrNull() ?: 4,
                                targetDurationMinutes = duration.toIntOrNull() ?: 15,
                                targetPromptLevelId = targetPromptLevel,
                                goalStatement = goalStatement,
                                caregiverInstructions = caregiverInstructions,
                                stepSpecificInstructions = caregiverInstructions,
                                safetyConsiderations = safetyConsiderations,
                                thingsToAvoid = thingsToAvoid,
                                encouragementSuggestions = encouragementSuggestions,
                                startDate = LocalDate.now().toString(),
                                endDate = LocalDate.now().plusWeeks(4).toString(),
                                targetStepIds = listOf(4L, 7L, 10L)
                            )
                            ApiClient.apiService.createHomeProgram(req)
                        } catch (_: Exception) {}
                        isSaving = false
                        onProgramCreated()
                    }
                },
                icon = Icons.Default.Check,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}
