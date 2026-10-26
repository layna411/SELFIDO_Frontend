package com.simats.selfora.ui.therapist

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
    onNavigateToReview: (Long) -> Unit = { onProgramCreated() },
    onBack: () -> Unit
) {
    var isAssignedSuccess by remember { mutableStateOf(false) }

    var selectedActivity by remember { mutableStateOf("Tooth Brushing") }
    var specificStepFocus by remember { mutableStateOf("Brush all around teeth") }
    var promptLevelForHome by remember { mutableStateOf("Verbal Prompt") }
    var practiceTimesPerDay by remember { mutableStateOf("Practice 1 time daily (Night)") }
    var targetGoal by remember { mutableStateOf("Target: Complete 6 of 7 steps with verbal/visual prompt") }

    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
                // ==========================================
                // ASSIGN HOME PROGRAM (Image 1 Screen 10)
                // ==========================================
                Text("Select Activity for Home Practice", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪥", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(selectedActivity, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SelforaPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text("Specific Step/Focus for Home", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(specificStepFocus, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = SelforaTextPrimary)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SelforaPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text("Practice Plan", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("• ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            Text(practiceTimesPerDay, fontSize = 14.sp, color = SelforaTextPrimary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("• ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            Text(targetGoal, fontSize = 14.sp, color = SelforaTextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text("Prompt Level for Home", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    promptLevelForHome,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SelforaPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                GlassButton(
                    text = "Add Caregiver Guidance",
                    onClick = {
                        scope.launch {
                            isSaving = true
                            try {
                                val req = CreateHomeProgramRequest(
                                    childId = childId,
                                    activityId = 5L,
                                    title = "Tooth Brushing Home Practice",
                                    description = "Brush all around teeth",
                                    priority = "HIGH",
                                    scheduleType = "DAILY",
                                    preferredTime = "Night",
                                    frequencyPerWeek = 7,
                                    targetDurationMinutes = 10,
                                    targetPromptLevelId = 3, // Verbal
                                    goalStatement = targetGoal,
                                    caregiverInstructions = "Brush all around teeth gently. Use verbal prompt first.",
                                    stepSpecificInstructions = specificStepFocus,
                                    safetyConsiderations = "Supervise water temperature and toothpaste amount.",
                                    thingsToAvoid = "Do not brush aggressively or take over unless child needs help.",
                                    encouragementSuggestions = "Praise child enthusiastically after brushing teeth!",
                                    startDate = LocalDate.now().toString(),
                                    endDate = LocalDate.now().plusWeeks(2).toString(),
                                    targetStepIds = listOf(3L, 4L)
                                )
                                ApiClient.apiService.createHomeProgram(req)
                            } catch (_: Exception) {}
                            isSaving = false
                            isAssignedSuccess = true
                        }
                    },
                    icon = Icons.Default.Check,
                    gradient = listOf(SelforaPrimary, Color(0xFF6D28D9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )
            } else {
                // ==========================================
                // CONFIRMATION SCREEN (Image 1 Screen 12)
                // ==========================================
                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SelforaSuccess.copy(alpha = 0.15f),
                        modifier = Modifier.size(100.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = SelforaSuccess,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Home Program Assigned\nSuccessfully!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SelforaTextPrimary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Caregiver will see the activity in their\napp with instructions.",
                        fontSize = 14.sp,
                        color = SelforaTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SelforaPrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Assigned For\n12 May 2025",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaPrimary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    GlassOutlinedButton(
                        text = "View Home Program Summary",
                        onClick = { onNavigateToReview(childId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    GlassButton(
                        text = "Done",
                        onClick = onProgramCreated,
                        gradient = listOf(SelforaPrimary, Color(0xFF6D28D9)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}
