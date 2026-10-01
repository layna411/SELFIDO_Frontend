package com.simats.selfora.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.SessionSummaryResponse
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSummaryScreen(
    sessionId: Long,
    onNavigateToAdaptation: (sessionId: Long) -> Unit,
    onBackToDashboard: () -> Unit
) {
    var summary by remember { mutableStateOf<SessionSummaryResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(sessionId) {
        isLoading = true
        try {
            val res = ApiClient.apiService.completeSession(sessionId)
            if (res.isSuccessful && res.body() != null) {
                summary = res.body()
            } else {
                summary = SessionSummaryResponse(
                    sessionId = sessionId,
                    sessionCode = "SESS-1001",
                    childName = "Aarav Sharma",
                    activityTitle = "T-Shirt Dressing",
                    totalSteps = 18,
                    completedSteps = 17,
                    independentSteps = 11,
                    promptedSteps = 6,
                    unableSteps = 1,
                    independencePercentage = 61.1,
                    totalDurationSeconds = 480,
                    suggestedNextPromptLevel = "VISUAL",
                    suggestedFocusStepIds = listOf(4L, 7L, 10L)
                )
            }
        } catch (e: Exception) {
            summary = SessionSummaryResponse(
                sessionId = sessionId,
                sessionCode = "SESS-1001",
                childName = "Aarav Sharma",
                activityTitle = "T-Shirt Dressing",
                totalSteps = 18,
                completedSteps = 17,
                independentSteps = 11,
                promptedSteps = 6,
                unableSteps = 1,
                independencePercentage = 61.1,
                totalDurationSeconds = 480,
                suggestedNextPromptLevel = "VISUAL",
                suggestedFocusStepIds = listOf(4L, 7L, 10L)
            )
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Summary", fontWeight = FontWeight.Bold, color = SelforaTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackToDashboard) {
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
            if (isLoading || summary == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val s = summary!!

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
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
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SelforaGreenSuccess, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Session Completed!", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SelforaTextDark)
                                Text("${s.childName} • ${s.activityTitle}", fontSize = 13.sp, color = SelforaTextMuted)

                                Spacer(modifier = Modifier.height(16.dp))

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = SelforaBlueLight.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("${s.independencePercentage}%", fontWeight = FontWeight.Bold, fontSize = 36.sp, color = SelforaBlueDark)
                                        Text("Overall Independence Score", fontSize = 12.sp, color = SelforaTextDark)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    SummaryStat("Independent", "${s.independentSteps}", SelforaGreenSuccess)
                                    SummaryStat("Prompted", "${s.promptedSteps}", SelforaBluePrimary)
                                    SummaryStat("Unable", "${s.unableSteps}", SelforaRedAccent)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Fading Recommendation Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SelforaTeal.copy(alpha = 0.1f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SelforaTeal, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Clinical Prompt Fading Suggestion", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SelforaTeal)
                                    Text("Suggested Target Prompt: ${s.suggestedNextPromptLevel}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextDark)
                                    Text("Focus Steps: ${s.suggestedFocusStepIds.joinToString()}", fontSize = 11.sp, color = SelforaTextMuted)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { onNavigateToAdaptation(sessionId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SelforaBluePrimary)
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PROCEED TO ADAPTIVE PLAN", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(110.dp))
                }
            }
        }
    }
}

@Composable
fun SummaryStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
        Text(label, fontSize = 11.sp, color = SelforaTextMuted)
    }
}
