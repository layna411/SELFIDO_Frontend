package com.simats.selfora.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.EnvironmentProgressItem
import com.simats.selfora.data.model.ProgressSummaryResponse
import com.simats.selfora.data.model.StepProgressItem
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapistProgressScreen(
    childId: Long = 1L,
    activityId: Long = 1L,
    onBack: () -> Unit
) {
    var progress by remember { mutableStateOf<ProgressSummaryResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(childId) {
        isLoading = true
        try {
            val res = ApiClient.apiService.getChildProgress(childId, activityId)
            if (res.isSuccessful && res.body() != null) {
                progress = res.body()
            } else {
                progress = getSampleProgressSummary()
            }
        } catch (e: Exception) {
            progress = getSampleProgressSummary()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress & Generalization", fontWeight = FontWeight.Bold, color = SelforaTextPrimary) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SelforaBgLight)
                .padding(16.dp)
        ) {
            if (isLoading || progress == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val p = progress!!

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("OVERALL INDEPENDENCE METRIC", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaTextMuted)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("${p.overallIndependencePercentage}%", fontWeight = FontWeight.Bold, fontSize = 42.sp, color = SelforaBlueDark)
                                Text("Activity: ${p.activityTitle} • ${p.totalSessionsCompleted} Sessions", fontSize = 13.sp, color = SelforaTextDark)
                            }
                        }
                    }

                    item {
                        Text("Generalization Across Environments", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaTextDark)
                    }

                    items(p.generalizationProgress) { env ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(env.environment, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SelforaTextDark)
                                    Text("Dominant Support: ${env.dominantPromptLevel}", fontSize = 12.sp, color = SelforaTextMuted)
                                }
                                Text("${env.independencePercentage}% Indep", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaGreenSuccess)
                            }
                        }
                    }

                    item {
                        Text("Step-Level Task Analysis Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaTextDark)
                    }

                    items(p.stepProgress) { step ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Step ${step.stepNumber}: ${step.stepTitle}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Prompt: ${step.currentPromptLevel}", fontSize = 11.sp, color = SelforaTextMuted)
                                }
                                Text("${step.independencePercentage}%", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SelforaBluePrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getSampleProgressSummary(): ProgressSummaryResponse {
    return ProgressSummaryResponse(
        childId = 1L,
        childName = "Aarav Sharma",
        activityId = 1L,
        activityTitle = "T-Shirt Dressing",
        overallIndependencePercentage = 65.5,
        totalSessionsCompleted = 8,
        promptDistribution = mapOf("INDEPENDENT" to 11, "VISUAL" to 4, "VERBAL" to 3),
        stepProgress = listOf(
            StepProgressItem(1L, 1, "Look at T-Shirt", "Independent", 100.0, 8),
            StepProgressItem(2L, 2, "Pick up T-Shirt", "Independent", 100.0, 8),
            StepProgressItem(7L, 7, "Put head through neck opening", "Visual Prompt", 50.0, 8),
            StepProgressItem(10L, 10, "Put right arm through sleeve", "Verbal Prompt", 37.5, 8)
        ),
        generalizationProgress = listOf(
            EnvironmentProgressItem("CLINIC", 75.0, "Independent"),
            EnvironmentProgressItem("HOME", 55.0, "Visual Prompt"),
            EnvironmentProgressItem("SCHOOL", 40.0, "Verbal Prompt")
        ),
        historyPoints = emptyList()
    )
}
