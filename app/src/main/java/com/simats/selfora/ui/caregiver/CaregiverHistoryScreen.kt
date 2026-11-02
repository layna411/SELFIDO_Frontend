package com.simats.selfora.ui.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.CaregiverPracticeSessionResponse
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverHistoryScreen(
    childId: Long = 1L,
    onBack: () -> Unit = {}
) {
    var historyList by remember { mutableStateOf<List<CaregiverPracticeSessionResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun loadHistory() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val res = ApiClient.apiService.getPracticeHistory(childId)
                if (res.isSuccessful && res.body() != null) {
                    historyList = res.body()!!
                } else {
                    errorMessage = "Unable to load practice history (${res.code()})"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(childId) {
        loadHistory()
    }

    Scaffold(
        containerColor = SelforaBackground,
        topBar = {
            GlassTopBar(
                title = "Practice History",
                subtitle = "Past Records & Therapist Feedback",
                onBackClick = onBack,
                accentColor = SelforaPrimary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SelforaPrimary)
                    }
                } else if (errorMessage != null) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⚠️", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(errorMessage!!, fontSize = 13.sp, color = SelforaError, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(12.dp))
                            GlassButton(
                                text = "Retry",
                                onClick = { loadHistory() },
                                modifier = Modifier.fillMaxWidth(0.5f)
                            )
                        }
                    }
                } else if (historyList.isEmpty()) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📝", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No Practice Sessions Recorded", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaTextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("When you record and submit home practice routines, they will appear here along with therapist reviews.", fontSize = 12.sp, color = SelforaTextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                } else {
                    historyList.forEach { item ->
                        CaregiverHistoryRecordCard(
                            session = item
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            GlassButton(
                text = "Refresh Practice History",
                onClick = { loadHistory() },
                icon = Icons.Default.Refresh,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
fun CaregiverHistoryRecordCard(
    session: CaregiverPracticeSessionResponse
) {
    val isReviewed = "REVIEWED".equals(session.status, ignoreCase = true)
    val statusColor = if (isReviewed) SelforaSuccess else SelforaWarning
    val statusText = if (isReviewed) "Reviewed" else "Pending Review"

    val indepCount = session.stepResults?.count { it.outcome == "INDEPENDENT" } ?: 0
    val totalSteps = session.stepResults?.size ?: 0

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        backgroundColor = Color.White.copy(alpha = 0.95f)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(session.practiceDate, fontSize = 11.sp, color = SelforaTextSecondary, fontWeight = FontWeight.SemiBold)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(session.activityTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Duration: ${session.durationMinutes} mins", fontSize = 12.sp, color = SelforaTextSecondary)
                Text(
                    if (totalSteps > 0) "$indepCount / $totalSteps Independent" else "Practice Complete",
                    fontSize = 12.sp,
                    color = SelforaPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!session.caregiverNotes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Caregiver Note: \"${session.caregiverNotes}\"", fontSize = 12.sp, color = SelforaTextSecondary)
            }

            if (isReviewed && !session.therapistFeedback.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SelforaPrimary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Therapist Feedback (${session.reviewingTherapistName ?: "Dr. Sarah Jenkins"}):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("\"${session.therapistFeedback}\"", fontSize = 12.sp, color = SelforaTextPrimary)
                    }
                }
            }
        }
    }
}
