package com.simats.selfora.ui.therapist

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.api.NetworkErrorHandler
import com.simats.selfora.data.model.CaregiverPracticeSessionResponse
import com.simats.selfora.data.model.TherapistReviewRequest
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeProgramReviewScreen(
    childId: Long = 1L,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var pendingSessions by remember { mutableStateOf<List<CaregiverPracticeSessionResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedIndex by remember { mutableStateOf(0) }
    var therapistFeedback by remember { mutableStateOf("") }
    var isSubmittingReview by remember { mutableStateOf(false) }

    fun loadPendingReviews() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val resp = ApiClient.apiService.getPendingReviews()
                if (resp.isSuccessful && resp.body() != null) {
                    val all = resp.body()!!
                    // Filter for this child if childId is specified (> 0)
                    val filtered = if (childId > 0) {
                        all.filter { it.childId == childId }
                    } else {
                        all
                    }
                    pendingSessions = filtered
                    selectedIndex = 0
                    therapistFeedback = ""
                } else {
                    errorMessage = NetworkErrorHandler.getHttpErrorMessage(resp.code())
                }
            } catch (e: Exception) {
                errorMessage = NetworkErrorHandler.getErrorMessage(e)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(childId) {
        loadPendingReviews()
    }

    fun submitReview(sessionId: Long) {
        if (therapistFeedback.trim().isEmpty()) {
            Toast.makeText(context, "Please enter clinical feedback before approving.", Toast.LENGTH_SHORT).show()
            return
        }

        isSubmittingReview = true
        scope.launch {
            try {
                val req = TherapistReviewRequest(
                    therapistFeedback = therapistFeedback.trim(),
                    status = "REVIEWED",
                    flaggedForFollowup = false
                )
                val resp = ApiClient.apiService.reviewPracticeSession(sessionId, req)
                if (resp.isSuccessful) {
                    Toast.makeText(context, "Practice review submitted successfully!", Toast.LENGTH_SHORT).show()
                    loadPendingReviews()
                } else {
                    Toast.makeText(context, "Review failed: HTTP ${resp.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, NetworkErrorHandler.getErrorMessage(e), Toast.LENGTH_SHORT).show()
            } finally {
                isSubmittingReview = false
            }
        }
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "Home Program Review",
                subtitle = "Caregiver Home Practice Clinical Review",
                onBackClick = onBack,
                accentColor = SelforaPrimary
            )
        },
        containerColor = SelforaBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SelforaPrimary)
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Unable to Load Reviews",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                errorMessage!!,
                                fontSize = 13.sp,
                                color = SelforaTextSecondary,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            GlassButton(
                                text = "Retry",
                                onClick = { loadPendingReviews() },
                                modifier = Modifier.fillMaxWidth(0.6f)
                            )
                        }
                    }
                }
            } else if (pendingSessions.isEmpty()) {
                // Empty Database State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SelforaSuccess.copy(alpha = 0.12f),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SelforaSuccess,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "All Caught Up!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "There are no pending caregiver practice submissions waiting for clinical review for this child.",
                                fontSize = 13.sp,
                                color = SelforaTextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            GlassButton(
                                text = "Return to Dashboard",
                                onClick = onBack,
                                modifier = Modifier.fillMaxWidth(0.7f)
                            )
                        }
                    }
                }
            } else {
                // Display Live Pending Sessions
                val currentSession = pendingSessions.getOrNull(selectedIndex) ?: pendingSessions.first()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    // Multi-session selector if more than 1 pending
                    if (pendingSessions.size > 1) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.9f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Pending Sessions (${selectedIndex + 1} of ${pendingSessions.size})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaTextPrimary
                                )
                                Row {
                                    IconButton(
                                        onClick = { if (selectedIndex > 0) selectedIndex-- },
                                        enabled = selectedIndex > 0
                                    ) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous")
                                    }
                                    IconButton(
                                        onClick = { if (selectedIndex < pendingSessions.size - 1) selectedIndex++ },
                                        enabled = selectedIndex < pendingSessions.size - 1
                                    ) {
                                        Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Program & Child Overview Card
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    currentSession.childName ?: "Child #${currentSession.childId}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaTextPrimary
                                )
                                Text(
                                    "Practice Date: ${currentSession.practiceDate}",
                                    fontSize = 12.sp,
                                    color = SelforaTextSecondary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFD97706).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    currentSession.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = SelforaBorder.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(14.dp))

                        ReviewFieldRow(
                            label = "Target Activity",
                            value = currentSession.activityTitle ?: "ADL Activity #${currentSession.activityId}"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ReviewFieldRow(
                            label = "Home Programme",
                            value = currentSession.homeProgramTitle ?: "Active Home Regimen"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ReviewFieldRow(
                            label = "Submitted By Caregiver",
                            value = currentSession.caregiverName ?: "Caregiver #${currentSession.caregiverId}"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ReviewFieldRow(
                            label = "Practice Duration",
                            value = "${currentSession.durationMinutes} Minutes"
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Caregiver Observations Card
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White.copy(alpha = 0.95f),
                        borderColor = SelforaSuccess.copy(alpha = 0.3f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Feedback,
                                contentDescription = null,
                                tint = SelforaSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Caregiver Observations & Step Results",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Caregiver Notes
                        Text(
                            "Caregiver Notes:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SelforaTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SelforaBgLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (!currentSession.caregiverNotes.isNullOrBlank()) {
                                    "\"${currentSession.caregiverNotes}\""
                                } else {
                                    "No additional written observations provided."
                                },
                                fontSize = 13.sp,
                                color = SelforaTextPrimary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Step-by-step breakdown
                        if (!currentSession.stepResults.isNullOrEmpty()) {
                            Text(
                                "Step Performance (${currentSession.stepResults.size} steps):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SelforaTextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            currentSession.stepResults.forEach { step ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "Step ${step.stepNumber}: ${step.stepTitle}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = SelforaTextPrimary
                                            )
                                            if (!step.observation.isNullOrBlank()) {
                                                Text(
                                                    step.observation,
                                                    fontSize = 11.sp,
                                                    color = SelforaTextSecondary
                                                )
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when (step.outcome?.uppercase()) {
                                                "INDEPENDENT" -> SelforaSuccess.copy(alpha = 0.15f)
                                                "VERBAL" -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                                                "PHYSICAL" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                                else -> Color(0xFFD97706).copy(alpha = 0.15f)
                                            }
                                        ) {
                                            Text(
                                                text = step.outcome ?: "ATTEMPTED",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (step.outcome?.uppercase()) {
                                                    "INDEPENDENT" -> SelforaSuccess
                                                    "VERBAL" -> Color(0xFF1D4ED8)
                                                    "PHYSICAL" -> Color(0xFFDC2626)
                                                    else -> Color(0xFFB45309)
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Therapist Clinical Review Submission Card
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Therapist Clinical Feedback",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Provide feedback or instructions for the caregiver's next home practice:",
                            fontSize = 12.sp,
                            color = SelforaTextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = therapistFeedback,
                            onValueChange = { therapistFeedback = it },
                            placeholder = { Text("e.g. Great progress on step 3. Try to fade verbal prompts tomorrow.") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SelforaPrimary,
                                unfocusedBorderColor = SelforaBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        GlassButton(
                            text = if (isSubmittingReview) "Submitting..." else "Approve & Complete Review",
                            onClick = { submitReview(currentSession.id) },
                            icon = Icons.Default.Check,
                            gradient = listOf(SelforaPrimary, Color(0xFF6D28D9)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun ReviewFieldRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = SelforaTextSecondary, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
    }
}
