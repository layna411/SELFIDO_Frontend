package com.simats.selfora.ui.child

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.TaskStepResponse
import com.simats.selfora.ui.therapist.getSampleTShirtSteps
import com.simats.selfora.ui.therapist.getSampleGirlFrockSteps
import com.simats.selfora.ui.therapist.getSampleEatingSteps
import com.simats.selfora.ui.therapist.getSampleShoesSteps
import com.simats.selfora.ui.components.VisualStepGuidanceCard
import com.simats.selfora.ui.theme.*
import com.simats.selfora.utils.TtsManager
import kotlinx.coroutines.launch

@Composable
fun ChildActivityStepScreen(
    activityId: Long = 1L,
    onFinished: () -> Unit,
    onBack: () -> Unit
) {
    var steps by remember { mutableStateOf<List<TaskStepResponse>>(emptyList()) }
    var currentStepIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
        }
    }

    LaunchedEffect(activityId) {
        isLoading = true
        val fallbackSteps = when (activityId) {
            13L -> getSampleEatingSteps()
            9L -> getSampleShoesSteps()
            2L -> getSampleGirlFrockSteps()
            else -> getSampleTShirtSteps()
        }
        try {
            val res = ApiClient.apiService.getTaskSteps(activityId)
            steps = if (res.isSuccessful && res.body() != null && res.body()!!.isNotEmpty()) res.body()!! else fallbackSteps
        } catch (e: Exception) {
            steps = fallbackSteps
        } finally {
            isLoading = false
        }
    }

    // Auto-speak child instruction on step change
    LaunchedEffect(currentStepIndex, steps) {
        if (steps.isNotEmpty() && currentStepIndex < steps.size) {
            val instruction = steps[currentStepIndex].childInstruction
            ttsManager.speak(instruction)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChildBlueCard)
            .padding(24.dp)
    ) {
        if (isLoading || steps.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            val step = steps[currentStepIndex]

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Step Counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SelforaBlueDark)
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White
                    ) {
                        Text(
                            "Step ${currentStepIndex + 1} of ${steps.size}",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SelforaBlueDark
                        )
                    }

                    Spacer(modifier = Modifier.width(48.dp))
                }

                // Animated GIF Visual Step Guidance Card
                VisualStepGuidanceCard(
                    stepNumber = currentStepIndex + 1,
                    totalSteps = steps.size,
                    stepTitle = step.title,
                    childInstruction = step.childInstruction,
                    gender = "BOY",
                    onSpeakInstruction = { ttsManager.speak(step.childInstruction) }
                )

                // Big Child Touch Action Buttons (Symmetrically Aligned)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStepIndex > 0) {
                        Button(
                            onClick = { currentStepIndex-- },
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = SelforaBlueDark)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Previous", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStepIndex < steps.size - 1) {
                                currentStepIndex++
                            } else {
                                onFinished()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ChildGreenPlay)
                    ) {
                        Text(
                            if (currentStepIndex == steps.size - 1) "I DID IT! 🎉" else "NEXT STEP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    }
}
