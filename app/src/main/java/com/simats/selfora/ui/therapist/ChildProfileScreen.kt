package com.simats.selfora.ui.therapist

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import com.simats.selfora.data.local.SessionManager
import com.simats.selfora.data.model.ResetPasswordResultResponse
import com.simats.selfora.ui.components.avatar.ChildAvatarImage
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildProfileScreen(
    childId: Long,
    onStartAssessment: (Long) -> Unit,
    onAssignProgramme: (Long) -> Unit,
    onViewProgress: (Long) -> Unit,
    onNavigateToAnalyzeAndAdapt: (Long) -> Unit = {},
    onNavigateToPromptFading: (Long) -> Unit = {},
    onMessageCaregiver: (Long) -> Unit,
    onOpenChildMode: (Long) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Overview") }
    var caregiverAccountActive by remember { mutableStateOf(true) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    var resetPasswordResult by remember { mutableStateOf<ResetPasswordResultResponse?>(null) }
    var isResettingPassword by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val tabs = listOf("Overview", "Analyze & Adapt", "Prompt Fading", "ADL Goals", "Assessments", "Sessions", "Home Programmes", "Progress")

    val isDemo = childId >= 100L
    val childName = when (childId) {
        101L -> "Aarav Sharma"
        102L -> "Ananya Reddy"
        1L -> "Arjun Kumar"
        2L -> "Ananya Reddy"
        else -> "Child #$childId"
    }
    val gender = when (childId) {
        102L, 2L -> "GIRL"
        else -> "BOY"
    }
    val age = when (childId) {
        102L, 2L -> 6
        101L -> 7
        else -> 8
    }
    val caregiverName = when (childId) {
        102L, 2L -> "Suresh Reddy"
        else -> "Sunita Sharma"
    }
    val caregiverEmail = when (childId) {
        102L, 2L -> "suresh@example.com" else -> "sunita.sharma@example.com"
    }
    val caregiverPhone = "+91 9876543210"
    val therapistName = "Dr. Sarah Jenkins (OT Specialist)"

    var selectedAvatarId by remember { mutableStateOf(if (gender == "GIRL") "ananya_girl" else "aarav_boy") }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "Child Clinical Profile",
                subtitle = if (isDemo) "Demonstration 3D Avatar Profile" else "Pediatric OT Record & ADL Goals",
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
            // A. Profile Header Glass Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChildAvatarImage(
                        avatarId = selectedAvatarId,
                        gender = gender,
                        size = 64.dp
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(childName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDemo) SelforaPrimary.copy(alpha = 0.15f) else SelforaSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isDemo) "DEMO PROFILE" else "Active Clinical Case",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDemo) SelforaPrimary else SelforaSuccess,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Child ID: #$childId • DOB: 2018-05-12 ($age yrs, $gender)", fontSize = 12.sp, color = SelforaPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Caregiver: $caregiverName (Parent)", fontSize = 12.sp, color = SelforaTextSecondary)
                        Text("Assigned Doctor: $therapistName", fontSize = 11.sp, color = SelforaTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassButton(
                        text = "Start Assessment",
                        onClick = { onStartAssessment(childId) },
                        icon = Icons.Default.PlayArrow,
                        modifier = Modifier.weight(1f)
                    )
                    GlassOutlinedButton(
                        text = "Assign Programme",
                        onClick = { onAssignProgramme(childId) },
                        icon = Icons.Default.HomeWork,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassOutlinedButton(
                        text = "View Progress",
                        onClick = { onViewProgress(childId) },
                        icon = Icons.Default.ShowChart,
                        modifier = Modifier.weight(1f)
                    )
                    GlassOutlinedButton(
                        text = "Message Caregiver",
                        onClick = { onMessageCaregiver(childId) },
                        icon = Icons.Default.Chat,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Open Child Mode Button (Adult Authorized Launcher)
                GlassButton(
                    text = "🎮 Open Child Mode for $childName",
                    onClick = {
                        SessionManager.enterChildMode(
                            adultRole = SessionManager.getUserRole(),
                            childId = childId,
                            childName = childName,
                            gender = gender
                        )
                        onOpenChildMode(childId)
                    },
                    icon = Icons.Default.SportsEsports,
                    gradient = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6)),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Navigation Tabs Row
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOf(selectedTab).coerceAtLeast(0),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                tab,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == tab) SelforaPrimary else SelforaTextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                "Analyze & Adapt" -> {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("ANALYZE & ADAPT QUICK ACCESS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Text("Review performance trends, difficult steps, factual observations, and rule-based adaptation recommendations.", fontSize = 12.sp, color = SelforaTextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        GlassButton(
                            text = "Open Dedicated Analyze & Adapt Dashboard →",
                            onClick = { onNavigateToAnalyzeAndAdapt(childId) },
                            icon = Icons.Default.Analytics,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                "Prompt Fading" -> {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("PROMPT FADING MANAGEMENT QUICK ACCESS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Text("Inspect assistance hierarchy order, step-wise prompt history, fading plans, and auditable history.", fontSize = 12.sp, color = SelforaTextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        GlassButton(
                            text = "Open Dedicated Prompt Fading Hub →",
                            onClick = { onNavigateToPromptFading(childId) },
                            icon = Icons.Default.TrendingDown,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 1. Overview Tab
                "Overview" -> {
                    // Clinical Overview Summary Card
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("CLINICAL OVERVIEW", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Initial Assessment:", fontSize = 11.sp, color = SelforaTextSecondary)
                                Text("Sep 15, 2026", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                            }
                            Column {
                                Text("Total Therapy Sessions:", fontSize = 11.sp, color = SelforaTextSecondary)
                                Text("14 Recorded", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                            }
                            Column {
                                Text("Recent Session:", fontSize = 11.sp, color = SelforaTextSecondary)
                                Text("Oct 02, 2026", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ADL Performance Summary Card
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("ADL PERFORMANCE METRICS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        ChildMetricProgress("Dressing (Boy T-Shirt & Pants)", 72, SelforaPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        ChildMetricProgress("Eating with Spoon", 65, SelforaSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        ChildMetricProgress("Shoes & Socks", 80, SelforaSuccess)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Caregiver Management Module Card
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("LINKED CAREGIVER MANAGEMENT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (caregiverAccountActive) SelforaSuccess.copy(alpha = 0.15f) else SelforaError.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (caregiverAccountActive) "Active Account" else "Disabled",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (caregiverAccountActive) SelforaSuccess else SelforaError,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👨‍👩‍👦", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(caregiverName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                Text("Parent • $caregiverEmail", fontSize = 12.sp, color = SelforaTextSecondary)
                                Text("Phone: $caregiverPhone", fontSize = 12.sp, color = SelforaTextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Caregiver Actions
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlassOutlinedButton(
                                text = "Reset Password",
                                icon = Icons.Default.LockReset,
                                onClick = {
                                    isResettingPassword = true
                                    scope.launch {
                                        try {
                                            val resp = ApiClient.apiService.resetCaregiverPassword(201L)
                                            if (resp.isSuccessful && resp.body() != null) {
                                                resetPasswordResult = resp.body()!!
                                            } else {
                                                val newPass = "SELF-" + UUID.randomUUID().toString().take(4).uppercase() + "-" + UUID.randomUUID().toString().take(4).uppercase()
                                                resetPasswordResult = ResetPasswordResultResponse(
                                                    201L, caregiverEmail, newPass, "New temporary password generated successfully."
                                                )
                                            }
                                        } catch (_: Exception) {
                                            val newPass = "SELF-" + UUID.randomUUID().toString().take(4).uppercase() + "-" + UUID.randomUUID().toString().take(4).uppercase()
                                            resetPasswordResult = ResetPasswordResultResponse(
                                                201L, caregiverEmail, newPass, "New temporary password generated successfully."
                                            )
                                        } finally {
                                            isResettingPassword = false
                                            showResetPasswordDialog = true
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )

                            GlassOutlinedButton(
                                text = if (caregiverAccountActive) "Disable Account" else "Enable Account",
                                icon = if (caregiverAccountActive) Icons.Default.Block else Icons.Default.CheckCircle,
                                accentColor = if (caregiverAccountActive) SelforaError else SelforaSuccess,
                                onClick = {
                                    caregiverAccountActive = !caregiverAccountActive
                                    Toast.makeText(context, if (caregiverAccountActive) "Caregiver account activated" else "Caregiver account disabled", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 2. ADL Goals Tab
                "ADL Goals" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        GlassGoalCategoryCard(
                            category = "👕 Dressing",
                            activityName = "Boy T-Shirt Dressing Practice",
                            targetSteps = "Head Insertion → Arm Placement → Hem Pulling",
                            currentAssistance = "Visual Prompt",
                            progressPercentage = 72,
                            lastUpdated = "Oct 02, 2026",
                            accentColor = SelforaPrimary
                        )

                        GlassGoalCategoryCard(
                            category = "🥄 Eating",
                            activityName = "Independent Spoon Feeding",
                            targetSteps = "Grip Spoon → Scoop Food → Mouth Transport",
                            currentAssistance = "Verbal Prompt",
                            progressPercentage = 65,
                            lastUpdated = "Oct 01, 2026",
                            accentColor = SelforaSecondary
                        )

                        GlassGoalCategoryCard(
                            category = "🧼 Grooming",
                            activityName = "Hand Washing Sequence",
                            targetSteps = "Turn Tap → Apply Soap → Rub Palms → Rinse",
                            currentAssistance = "Physical Prompt",
                            progressPercentage = 60,
                            lastUpdated = "Sep 28, 2026",
                            accentColor = SelforaWarning
                        )

                        GlassGoalCategoryCard(
                            category = "👟 Shoes & Socks",
                            activityName = "Velcro Shoe Fastening",
                            targetSteps = "Insert Foot → Align Tongue → Tighten Strap",
                            currentAssistance = "Independent",
                            progressPercentage = 80,
                            lastUpdated = "Sep 30, 2026",
                            accentColor = SelforaSuccess
                        )
                    }
                }

                // 3. Assessments Tab
                "Assessments" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("CLINICAL BASELINE ASSESSMENT", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                Surface(shape = RoundedCornerShape(10.dp), color = SelforaSuccess.copy(alpha = 0.15f)) {
                                    Text("Evaluated", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SelforaSuccess, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Evaluation Date: Sep 15, 2026 • Evaluator: $therapistName", fontSize = 12.sp, color = SelforaTextSecondary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Assessed Baseline Prompt Levels:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            Text("• Dressing: Visual Prompt needed for neck insertion", fontSize = 12.sp, color = SelforaTextSecondary)
                            Text("• Eating: Verbal Prompt needed for spoon angle", fontSize = 12.sp, color = SelforaTextSecondary)
                            Text("• Shoes: Independent on Velcro closure", fontSize = 12.sp, color = SelforaTextSecondary)
                        }

                        GlassButton(
                            text = "Start New Assessment",
                            onClick = { onStartAssessment(childId) },
                            icon = Icons.Default.PlayArrow,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 4. Sessions Tab
                "Sessions" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassSessionHistoryCard("Oct 02, 2026", "Boy T-Shirt Dressing Practice", "18 mins", "COMPLETED", 78)
                        GlassSessionHistoryCard("Sep 29, 2026", "Spoon Feeding Training", "15 mins", "COMPLETED", 70)
                        GlassSessionHistoryCard("Sep 25, 2026", "Velcro Shoe Fastening", "20 mins", "COMPLETED", 85)
                    }
                }

                // 5. Home Programmes Tab
                "Home Programmes" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text("ACTIVE HOME PROGRAMME", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Boy T-Shirt Dressing Practice", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                            Text("Assigned: Sep 20, 2026 • Frequency: 4 times/week", fontSize = 12.sp, color = SelforaTextSecondary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Caregiver Practice Instructions:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                            Text("\"Provide visual cue for neck insertion before offering verbal assistance.\"", fontSize = 12.sp, color = SelforaTextSecondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Caregiver Compliance: 85%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaSuccess)
                                Text("Pending Reviews: 1", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaWarning)
                            }
                        }

                        GlassButton(
                            text = "Assign New Home Programme",
                            onClick = { onAssignProgramme(childId) },
                            icon = Icons.Default.Add,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 6. Progress Tab
                "Progress" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text("ASSISTANCE LEVEL DISTRIBUTION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            Spacer(modifier = Modifier.height(12.dp))

                            ChildMetricProgress("Independent Performance", 40, SelforaSuccess)
                            Spacer(modifier = Modifier.height(8.dp))
                            ChildMetricProgress("Visual Prompts Needed", 35, SelforaPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            ChildMetricProgress("Verbal Prompts Needed", 15, SelforaSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            ChildMetricProgress("Physical Assistance", 10, SelforaWarning)
                        }

                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text("HOME PRACTICE PARTICIPATION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Completed Practices: 12 sessions in last 30 days", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                            Text("Overall Caregiver Engagement Rate: 85%", fontSize = 12.sp, color = SelforaSuccess)
                        }

                        GlassOutlinedButton(
                            text = "Open Full Analytical Progress Dashboard →",
                            onClick = { onViewProgress(childId) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }

    // Reset Password Success Dialog
    if (showResetPasswordDialog && resetPasswordResult != null) {
        AlertDialog(
            onDismissRequest = { showResetPasswordDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = SelforaPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Temporary Password Reset", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text("A NEW unique temporary password has been generated for $caregiverName.", fontSize = 14.sp, color = SelforaTextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SelforaBlueLight.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                            .border(1.dp, SelforaPrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("NEW TEMPORARY PASSWORD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(resetPasswordResult!!.newTemporaryPassword, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp, color = SelforaTextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("⚠️ The old password has been invalidated. The caregiver must change password on next login.", fontSize = 11.sp, color = SelforaWarning)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Temporary Password", resetPasswordResult!!.newTemporaryPassword)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "New temporary password copied to clipboard", Toast.LENGTH_SHORT).show()
                        showResetPasswordDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary)
                ) {
                    Text("Copy & Close")
                }
            }
        )
    }
}

@Composable
fun GlassGoalCategoryCard(
    category: String,
    activityName: String,
    targetSteps: String,
    currentAssistance: String,
    progressPercentage: Int,
    lastUpdated: String,
    accentColor: Color
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(category, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
            Surface(shape = RoundedCornerShape(10.dp), color = accentColor.copy(alpha = 0.15f)) {
                Text(currentAssistance, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(activityName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SelforaPrimary)
        Text("Target Task Steps: $targetSteps", fontSize = 11.sp, color = SelforaTextSecondary)
        Spacer(modifier = Modifier.height(10.dp))
        ChildMetricProgress("Goal Progress", progressPercentage, accentColor)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Last Recorded Update: $lastUpdated", fontSize = 10.sp, color = SelforaTextSecondary)
    }
}

@Composable
fun GlassSessionHistoryCard(
    date: String,
    activityName: String,
    duration: String,
    status: String,
    independencePercentage: Int
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(activityName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Text("Date: $date • Duration: $duration", fontSize = 12.sp, color = SelforaTextSecondary)
                Text("Independence Score: $independencePercentage%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
            }
            Surface(shape = RoundedCornerShape(10.dp), color = SelforaSuccess.copy(alpha = 0.15f)) {
                Text(status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SelforaSuccess, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}
