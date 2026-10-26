package com.simats.selfora.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.CreateAdaptivePlanRequest
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptationScreen(
    childId: Long = 1L,
    activityId: Long = 5L,
    sessionId: Long = 1L,
    onAdaptationSaved: () -> Unit,
    onBack: () -> Unit
) {
    var analysisText by remember {
        mutableStateOf("Rohan needs frequent verbal prompt for Step 3 (Rinse teeth). Good attempt and responds well to visual cue.")
    }
    var adaptPlanText by remember {
        mutableStateOf("Continue verbal prompt for Step 3. Introduce visual prompt for Step 4 next session.")
    }

    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "Analyze & Adapt",
                subtitle = "Clinical Fading & Strategy Planning",
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
            // Card 1: Analysis (Image 1 Screen 9)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Analysis", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = analysisText,
                    onValueChange = { analysisText = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SelforaPrimary,
                        unfocusedBorderColor = SelforaBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 2: Adapt Plan (Image 1 Screen 9)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Adapt Plan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = adaptPlanText,
                    onValueChange = { adaptPlanText = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SelforaPrimary,
                        unfocusedBorderColor = SelforaBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 3: Next Step Focus (Image 1 Screen 9)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Next Step Focus", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.9f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SelforaBorder.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Step 3: Brush teeth", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextPrimary)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Verbal Prompt",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.9f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SelforaBorder.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Step 4: Rinse mouth", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextPrimary)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SelforaPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Visual Prompt",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Save Adaptation Button (Image 1 Screen 9)
            GlassButton(
                text = "Save Adaptation",
                onClick = {
                    scope.launch {
                        isSaving = true
                        try {
                            val req = CreateAdaptivePlanRequest(
                                childId = childId,
                                activityId = activityId,
                                sessionId = sessionId,
                                currentPromptLevelId = 3, // Verbal
                                targetPromptLevelId = 1, // Visual
                                targetStepIds = listOf(3L, 4L),
                                clinicalRationale = "$analysisText | Plan: $adaptPlanText",
                                isConfirmed = true
                            )
                            ApiClient.apiService.createAdaptivePlan(req)
                        } catch (_: Exception) {}
                        isSaving = false
                        onAdaptationSaved()
                    }
                },
                icon = Icons.Default.Check,
                gradient = listOf(SelforaPrimary, Color(0xFF6D28D9)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}
