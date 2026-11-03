package com.simats.selfora.ui.therapist

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.ChildSummaryItem
import com.simats.selfora.data.model.PendingReviewItem
import com.simats.selfora.data.model.ScheduledSessionItem
import com.simats.selfora.data.model.TherapistDashboardSummaryResponse
import com.simats.selfora.ui.components.avatar.ChildAvatarImage
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.components.progress.SelforaLinearProgress
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun TherapistDashboardScreen(
    onNavigateToAddChildWorkflow: () -> Unit,
    onNavigateToChildren: () -> Unit,
    onNavigateToChildProfile: (Long) -> Unit = {},
    onNavigateToAssessment: () -> Unit,
    onNavigateToHomePrograms: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onLogout: () -> Unit
) {
    var summary by remember { mutableStateOf(TherapistDashboardSummaryResponse()) }
    var assignedChildren by remember { mutableStateOf<List<ChildSummaryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showProfileDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadDashboardData() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val response = ApiClient.apiService.getTherapistDashboardSummary()
                if (response.isSuccessful && response.body() != null) {
                    summary = response.body()!!
                } else {
                    val code = response.code()
                    if (code == 401 || code == 403) {
                        onLogout()
                        return@launch
                    }
                    errorMessage = "Failed to load clinical dashboard (HTTP $code)"
                }
                val childrenResponse = ApiClient.apiService.getTherapistChildren()
                if (childrenResponse.isSuccessful && childrenResponse.body() != null) {
                    assignedChildren = childrenResponse.body()!!
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}. Tap Refresh to try again."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadDashboardData()
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "${getTimeBasedGreeting()} 👋",
                subtitle = "${summary.therapistName.ifBlank { "Therapist" }} (${summary.designation})",
                accentColor = SelforaPrimary,
                actions = {
                    GlassIconButton(
                        icon = Icons.Default.Refresh,
                        onClick = { loadDashboardData() },
                        tint = SelforaPrimary,
                        size = 40.dp,
                        iconSize = 20.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BadgedBox(
                        badge = { Badge { Text("3") } }
                    ) {
                        GlassIconButton(
                            icon = Icons.Default.Notifications,
                            onClick = onNavigateToNotifications,
                            tint = SelforaPrimary,
                            size = 40.dp,
                            iconSize = 20.dp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    GlassIconButton(
                        icon = Icons.Default.Person,
                        onClick = { showProfileDialog = true },
                        tint = SelforaPrimary,
                        size = 40.dp,
                        iconSize = 20.dp
                    )
                }
            )
        },
        containerColor = SelforaBackground
    ) { padding ->
        if (isLoading && summary.therapistName.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SelforaPrimary)
            }
        } else if (errorMessage != null && summary.therapistName.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(errorMessage!!, color = SelforaTextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { loadDashboardData() }, colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary)) {
                            Text("Retry")
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
            // Header Profile Glass Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showProfileDialog = true }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = SelforaPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👩‍⚕️", fontSize = 28.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(summary.therapistName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                        Text(summary.designation, fontSize = 12.sp, color = SelforaPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Department of Pediatric Occupational Therapy", fontSize = 11.sp, color = SelforaTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Today Overview",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaPrimary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // 4 Overview Cards in 2x2 grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardSummaryGlassCard(
                    title = "Children",
                    count = "${summary.activeChildrenCount}",
                    subtitle = "Active Clinical Cases",
                    emoji = "👦",
                    accentColor = SelforaPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToChildren
                )
                DashboardSummaryGlassCard(
                    title = "Sessions Today",
                    count = "${summary.todaySessionsCount}",
                    subtitle = "Scheduled Sessions",
                    emoji = "📅",
                    accentColor = SelforaSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAssessment
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardSummaryGlassCard(
                    title = "Home Programs",
                    count = "${summary.activeHomeProgramsCount}",
                    subtitle = "Assigned Programs",
                    emoji = "🏠",
                    accentColor = SelforaSuccess,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToHomePrograms
                )
                DashboardSummaryGlassCard(
                    title = "Pending Reviews",
                    count = "${summary.pendingReviewsCount}",
                    subtitle = "Caregiver Practice Logs",
                    emoji = "📋",
                    accentColor = SelforaWarning,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToHomePrograms
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Quick Actions",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaPrimary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Quick Actions List with Chevron Right matching Prototype Flow
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionChevronCard(
                    title = "My Children",
                    icon = Icons.Default.People,
                    iconTint = SelforaPrimary,
                    onClick = onNavigateToChildren
                )

                QuickActionChevronCard(
                    title = "Today's Sessions",
                    icon = Icons.Default.CalendarToday,
                    iconTint = SelforaSecondary,
                    onClick = onNavigateToAssessment
                )

                QuickActionChevronCard(
                    title = "Assign Home Program",
                    icon = Icons.Default.Assignment,
                    iconTint = SelforaSuccess,
                    onClick = onNavigateToHomePrograms
                )

                QuickActionChevronCard(
                    title = "Reports",
                    icon = Icons.Default.BarChart,
                    iconTint = Color(0xFF0EA5E9),
                    onClick = onNavigateToProgress
                )

                QuickActionChevronCard(
                    title = "Analyze & Adapt Dashboard",
                    icon = Icons.Default.Analytics,
                    iconTint = Color(0xFF7C3AED),
                    onClick = onNavigateToProgress
                )

                QuickActionChevronCard(
                    title = "Add Child & Caregiver",
                    icon = Icons.Default.PersonAdd,
                    iconTint = Color(0xFF2563EB),
                    onClick = onNavigateToAddChildWorkflow
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Assigned Children Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MY ASSIGNED CHILDREN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SelforaPrimary,
                    letterSpacing = 1.sp
                )
                if (assignedChildren.isNotEmpty()) {
                    Text(
                        text = "View All (${assignedChildren.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelforaPrimary,
                        modifier = Modifier.clickable { onNavigateToChildren() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            if (assignedChildren.isEmpty()) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChildCare,
                            contentDescription = null,
                            tint = SelforaPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Children Assigned Yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SelforaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You currently have no pediatric cases assigned to your account. Use the button below to enroll a new child or contact your administrator.",
                            fontSize = 12.sp,
                            color = SelforaTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToAddChildWorkflow,
                            colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Child & Caregiver", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                val previewChildren = assignedChildren.take(2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    previewChildren.forEach { child ->
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            elevation = 6.dp,
                            onClick = { onNavigateToChildProfile(child.id) }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(contentAlignment = Alignment.TopEnd) {
                                    ChildAvatarImage(
                                        avatarId = child.avatarUrl,
                                        gender = child.gender,
                                        size = 58.dp
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (child.programmeStatus == "ACTIVE") SelforaSuccess else Color(0xFF2563EB)
                                    ) {
                                        Text(
                                            text = if (child.programmeStatus == "ACTIVE") "ACTIVE" else "NEW",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = child.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SelforaTextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${child.age} yrs • ${if (child.gender.equals("GIRL", ignoreCase = true)) "Girl" else "Boy"}",
                                    fontSize = 11.sp,
                                    color = SelforaPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Progress", fontSize = 10.5.sp, color = SelforaTextSecondary)
                                    Text(
                                        "${child.overallProgressPercentage}%",
                                        fontSize = 11.sp,
                                        color = SelforaSuccess,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                SelforaLinearProgress(
                                    progress = (child.overallProgressPercentage.coerceIn(0, 100)) / 100f,
                                    color = SelforaSuccess,
                                    height = 5.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Caregiver: ${child.caregiverName}",
                                    fontSize = 10.sp,
                                    color = SelforaTextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    if (previewChildren.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Today's Schedule Section
            Text(
                text = "TODAY'S SCHEDULE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaPrimary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (summary.todaySchedule.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.EventAvailable, contentDescription = null, tint = SelforaTextSecondary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("No Scheduled Sessions Today", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                            Text("All scheduled therapy sessions for today are completed or unassigned.", fontSize = 12.sp, color = SelforaTextSecondary)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    summary.todaySchedule.forEach { session ->
                        GlassScheduleCard(session = session, onSelectChild = { onNavigateToChildren() })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pending Home Practice Reviews Section
            Text(
                text = "PENDING CAREGIVER REVIEWS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaPrimary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (summary.pendingReviews.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SelforaSuccess, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("No Pending Home Practice Reviews", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                            Text("All caregiver home practice submissions have been reviewed.", fontSize = 12.sp, color = SelforaTextSecondary)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    summary.pendingReviews.forEach { review ->
                        GlassPendingReviewCard(review = review, onReview = onNavigateToHomePrograms)
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }

    if (showProfileDialog) {
            AlertDialog(
                onDismissRequest = { showProfileDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SelforaPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("👩‍⚕️", fontSize = 24.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = summary.therapistName.ifBlank { "Dr. Sarah Jenkins" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = SelforaTextPrimary
                            )
                            Text(
                                text = summary.designation.ifBlank { "Pediatric Occupational Therapist" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SelforaPrimary
                            )
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HorizontalDivider(color = SelforaBorder)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalHospital, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Department", fontSize = 11.sp, color = SelforaTextSecondary, fontWeight = FontWeight.SemiBold)
                                Text("Pediatric Occupational Therapy", fontSize = 13.sp, color = SelforaTextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Clinical License ID", fontSize = 11.sp, color = SelforaTextSecondary, fontWeight = FontWeight.SemiBold)
                                Text("OT-IND-2024-88492", fontSize = 13.sp, color = SelforaTextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Clinical Email", fontSize = 11.sp, color = SelforaTextSecondary, fontWeight = FontWeight.SemiBold)
                                Text("dr.sarah.jenkins@selfora.org", fontSize = 13.sp, color = SelforaTextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WorkHistory, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Specialization & Experience", fontSize = 11.sp, color = SelforaTextSecondary, fontWeight = FontWeight.SemiBold)
                                Text("ADL Training & Prompt Fading • 12+ Yrs", fontSize = 13.sp, color = SelforaTextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        HorizontalDivider(color = SelforaBorder)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showProfileDialog = false
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Logout Account", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showProfileDialog = false }) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = SelforaSurface,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun GlassScheduleCard(
    session: ScheduledSessionItem,
    onSelectChild: () -> Unit
) {
    val safeChildName = session.childName ?: "Child"
    val safeTime = session.scheduledTime ?: "Today"
    val safeActivity = session.adlActivity ?: "ADL Activity"
    val safeStatus = session.status ?: "SCHEDULED"

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        onClick = onSelectChild
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(safeChildName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text("🕒 $safeTime • $safeActivity", fontSize = 12.sp, color = SelforaTextSecondary)
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = when (safeStatus) {
                    "COMPLETED" -> SelforaSuccess.copy(alpha = 0.15f)
                    "IN_PROGRESS" -> SelforaPrimary.copy(alpha = 0.15f)
                    else -> SelforaWarning.copy(alpha = 0.15f)
                }
            ) {
                Text(
                    text = safeStatus,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (safeStatus) {
                        "COMPLETED" -> SelforaSuccess
                        "IN_PROGRESS" -> SelforaPrimary
                        else -> SelforaWarning
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun GlassPendingReviewCard(
    review: PendingReviewItem,
    onReview: () -> Unit
) {
    val safeChildName = review.childName ?: "Child"
    val safeCaregiver = review.caregiverName ?: "Caregiver"
    val safeActivity = review.activityName ?: "Home Practice"
    val safeDate = review.submittedDate ?: "Today"

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        onClick = onReview
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(safeChildName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Text("Caregiver: $safeCaregiver • Activity: $safeActivity", fontSize = 12.sp, color = SelforaTextSecondary)
                Text("Submitted: $safeDate", fontSize = 11.sp, color = SelforaPrimary)
            }
            GlassOutlinedButton(
                text = "Review",
                onClick = onReview
            )
        }
    }
}

@Composable
fun DashboardSummaryGlassCard(
    title: String,
    count: String,
    subtitle: String,
    emoji: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier,
        elevation = 6.dp,
        contentPadding = PaddingValues(14.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SelforaTextSecondary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = count,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SelforaTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(emoji, fontSize = 18.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = accentColor,
            maxLines = 1,
            softWrap = false,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun QuickActionLargeGlassButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 6.dp,
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = gradient.first().copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = gradient.first(), modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, fontSize = 12.sp, color = SelforaTextSecondary)
            }
        }
    }
}

@Composable
fun QuickActionChevronCard(
    title: String,
    icon: ImageVector,
    iconTint: Color = SelforaPrimary,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.92f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = iconTint.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = SelforaTextPrimary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SelforaTextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

