package com.simats.selfora.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.CreateHomeProgramRequest
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
    var activityId by remember { mutableStateOf(1L) } // T-Shirt
    var frequency by remember { mutableStateOf("3") }
    var duration by remember { mutableStateOf("15") }
    var targetPromptLevel by remember { mutableStateOf(1) } // Visual
    var goalStatement by remember { mutableStateOf("Increase independent dressing performance to 80% without physical guidance.") }
    var instructions by remember { mutableStateOf("Guide Aarav through Steps 4, 7, and 10 at home using picture cues. Provide verbal prompt only if child hesitates > 10s.") }

    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assign Home Programme", fontWeight = FontWeight.Bold, color = SelforaTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SelforaTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SelforaSurface,
                    titleContentColor = SelforaTextPrimary,
                    navigationIconContentColor = SelforaTextPrimary,
                    actionIconContentColor = SelforaTextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SelforaBgLight)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("HOME PROGRAMME SPECIFICATION", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaBlueDark)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = frequency,
                                onValueChange = { frequency = it },
                                label = { Text("Frequency (Days/Wk)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = duration,
                                onValueChange = { duration = it },
                                label = { Text("Duration (Mins)") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Target Prompt Level for Home Practice:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(0 to "Indep", 1 to "Visual", 2 to "Gesture", 3 to "Verbal").forEach { (lvl, label) ->
                                FilterChip(
                                    selected = targetPromptLevel == lvl,
                                    onClick = { targetPromptLevel = lvl },
                                    label = { Text(label, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = goalStatement,
                            onValueChange = { goalStatement = it },
                            label = { Text("Clinical Goal Statement") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = instructions,
                            onValueChange = { instructions = it },
                            label = { Text("Caregiver Step-by-Step Instructions") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        isSaving = true
                        try {
                            val req = CreateHomeProgramRequest(
                                childId = childId,
                                activityId = activityId,
                                frequencyPerWeek = frequency.toIntOrNull() ?: 3,
                                targetDurationMinutes = duration.toIntOrNull() ?: 15,
                                targetPromptLevelId = targetPromptLevel,
                                goalStatement = goalStatement,
                                caregiverInstructions = instructions,
                                startDate = LocalDate.now().toString(),
                                endDate = LocalDate.now().plusWeeks(2).toString(),
                                targetStepIds = listOf(4L, 7L, 10L)
                            )
                            ApiClient.apiService.createHomeProgram(req)
                        } catch (e: Exception) {}
                        isSaving = false
                        onProgramCreated()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SelforaBluePrimary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ASSIGN TO CAREGIVER", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
