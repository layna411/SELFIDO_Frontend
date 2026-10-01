package com.simats.selfora.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
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
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptationScreen(
    childId: Long = 1L,
    activityId: Long = 1L,
    sessionId: Long = 1L,
    onAdaptationSaved: () -> Unit,
    onBack: () -> Unit
) {
    var currentPromptLevel by remember { mutableStateOf(3) } // Verbal
    var targetPromptLevel by remember { mutableStateOf(1) } // Visual
    var focusStepText by remember { mutableStateOf("4, 7, 10") }
    var clinicalRationale by remember { mutableStateOf("Child performed steps 1-3 independently. Fading from Verbal to Visual prompts for neck and arm positioning.") }

    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clinical Adaptive Plan", fontWeight = FontWeight.Bold, color = SelforaTextPrimary) },
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
                        Text("PROMPT FADING PLAN", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaBlueDark)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Current vs Target Flow Card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Current Prompt", fontSize = 11.sp, color = SelforaTextMuted)
                                Surface(shape = RoundedCornerShape(12.dp), color = SelforaOrangeWarning.copy(alpha = 0.15f)) {
                                    Text("VERBAL", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontWeight = FontWeight.Bold, color = SelforaOrangeWarning)
                                }
                            }

                            Surface(shape = CircleShape, color = SelforaBluePrimary.copy(alpha = 0.12f)) {
                                Text("TO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaBluePrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Target Prompt", fontSize = 11.sp, color = SelforaTextMuted)
                                Surface(shape = RoundedCornerShape(12.dp), color = SelforaGreenSuccess.copy(alpha = 0.15f)) {
                                    Text("VISUAL", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontWeight = FontWeight.Bold, color = SelforaGreenSuccess)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Select Target Prompt Level for Fading:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))

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
                            value = focusStepText,
                            onValueChange = { focusStepText = it },
                            label = { Text("Next Session Focus Steps (comma separated step IDs)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = clinicalRationale,
                            onValueChange = { clinicalRationale = it },
                            label = { Text("Clinical Rationale & Notes") },
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
                            val stepIds = focusStepText.split(",").mapNotNull { it.trim().toLongOrNull() }
                            val req = CreateAdaptivePlanRequest(
                                childId = childId,
                                activityId = activityId,
                                sessionId = sessionId,
                                currentPromptLevelId = currentPromptLevel,
                                targetPromptLevelId = targetPromptLevel,
                                targetStepIds = if (stepIds.isNotEmpty()) stepIds else listOf(4L, 7L, 10L),
                                clinicalRationale = clinicalRationale,
                                isConfirmed = true
                            )
                            ApiClient.apiService.createAdaptivePlan(req)
                        } catch (e: Exception) {}
                        isSaving = false
                        onAdaptationSaved()
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
                    Text("CONFIRM & SAVE ADAPTATION PLAN", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}
