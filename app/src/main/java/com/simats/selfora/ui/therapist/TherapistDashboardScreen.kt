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
import com.simats.selfora.data.model.PendingReviewItem
import com.simats.selfora.data.model.ScheduledSessionItem
import com.simats.selfora.data.model.TherapistDashboardSummaryResponse
import com.simats.selfora.ui.components.avatar.ChildAvatarImage
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*

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
    var showProfileDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val response = ApiClient.apiService.getTherapistDashboardSummary()
            if (response.isSuccessful && response.body() != null) {
                summary = response.body()!!
            }
        } catch (_: Exception) {}
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "${getTimeBasedGreeting()} 👋",
                subtitle = "${summary.therapistName} (${summary.designation})",
                accentColor = SelforaPrimary,
                actions = {
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
                text = "CLINICAL DASHBOARD METRICS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaPrimary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // 4 Live Statistics Glass Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardSummaryGlassCard(
                    title = "Assigned Children",
                    count = "${summary.activeChildrenCount}",
                    subtitle = "Active Clinical Cases",
                    emoji = "👦",
                    accentColor = SelforaPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToChildren
                )
                DashboardSummaryGlassCard(
                    title = "Today's Sessions",
                    count = "${summary.todaySessionsCount}",
                    subtitle = "Scheduled Sessions",
                    emoji = "📅",
                    accentColor = SelforaSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToChildren
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardSummaryGlassCard(
                    title = "Completed Sessions",
                    count = "${summary.completedSessionsCount}",
                    subtitle = "Sessions Recorded",
                    emoji = "✅",
                    accentColor = SelforaSuccess,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToProgress
                )
                DashboardSummaryGlassCard(
                    title = "Pending Reviews",
                    count = "${summary.pendingReviewsCount}",
                    subtitle = "Home Practice Logs",
                    emoji = "📋",
                    accentColor = SelforaWarning,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToHomePrograms
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "QUICK CLINICAL ACTIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaPrimary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Quick Action Shortcuts Grid / List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionLargeGlassButton(
                    title = "Analyze & Adapt Dashboard",
                    subtitle = "Clinical performance analysis, session comparisons & recommendations",
                    icon = Icons.Default.Analytics,
                    gradient = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)),
                    onClick = onNavigateToProgress
                )

                QuickActionLargeGlassButton(
                    title = "Prompt Fading Management",
                    subtitle = "Therapist-controlled prompt fading plans & auditable history",
                    icon = Icons.Default.TrendingDown,
                    gradient = listOf(Color(0xFF7C3AED), Color(0xFF6D28D9)),
                    onClick = onNavigateToProgress
                )

                QuickActionLargeGlassButton(
                    title = "View Assigned Children",
                    subtitle = "Access pediatric cases, clinical profiles & ADL goals",
                    icon = Icons.Default.ChildCare,
                    gradient = listOf(SelforaPrimary, Color(0xFF1D4ED8)),
                    onClick = onNavigateToChildren
                )

                QuickActionLargeGlassButton(
                    title = "Start New Assessment",
                    subtitle = "Evaluate ADL baseline (Dressing, Eating, Shoes) & prompt levels",
                    icon = Icons.Default.Assignment,
                    gradient = listOf(SelforaSuccess, Color(0xFF059669)),
                    onClick = onNavigateToAssessment
                )

                QuickActionLargeGlassButton(
                    title = "Review Home Practice",
                    subtitle = "Evaluate caregiver practice logs & offer clinical guidance",
                    icon = Icons.Default.HomeWork,
                    gradient = listOf(SelforaWarning, Color(0xFFD97706)),
                    onClick = onNavigateToHomePrograms
                )

                QuickActionLargeGlassButton(
                    title = "View Progress Reports",
                    subtitle = "Analyze longitudinal prompt fading & ADL independence metrics",
                    icon = Icons.Default.ShowChart,
                    gradient = listOf(SelforaSecondary, Color(0xFF6D28D9)),
                    onClick = onNavigateToProgress
                )

                QuickActionLargeGlassButton(
                    title = "Add Child & Caregiver",
                    subtitle = "Single workflow to create child record & link caregiver",
                    icon = Icons.Default.PersonAdd,
                    gradient = listOf(Color(0xFF0EA5E9), Color(0xFF0284C7)),
                    onClick = onNavigateToAddChildWorkflow
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Demo Pediatric Profiles Section (3D Avatars)
            Text(
                text = "DEMO PEDIATRIC PROFILES (3D AVATARS)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaPrimary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Aarav Demo Profile
                GlassCard(
                    modifier = Modifier.weight(1f),
                    elevation = 6.dp,
                    onClick = { onNavigateToChildProfile(101L) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            ChildAvatarImage(avatarId = "aarav_boy", gender = "BOY", size = 58.dp)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF2563EB)
                            ) {
                                Text(
                                    "DEMO",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Aarav", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SelforaTextPrimary)
                        Text("7 yrs • Boy Avatar", fontSize = 11.sp, color = SelforaPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("ADL Progress: 80%", fontSize = 11.sp, color = SelforaSuccess, fontWeight = FontWeight.Bold)
                        Text("Assigned: Dressing, Eating", fontSize = 10.sp, color = SelforaTextSecondary)
                    }
                }

                // Ananya Demo Profile
                GlassCard(
                    modifier = Modifier.weight(1f),
                    elevation = 6.dp,
                    onClick = { onNavigateToChildProfile(102L) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            ChildAvatarImage(avatarId = "ananya_girl", gender = "GIRL", size = 58.dp)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF9333EA)
                            ) {
                                Text(
                                    "DEMO",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ananya", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SelforaTextPrimary)
                        Text("6 yrs • Girl Avatar", fontSize = 11.sp, color = Color(0xFF9333EA), fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("ADL Progress: 85%", fontSize = 11.sp, color = SelforaSuccess, fontWeight = FontWeight.Bold)
                        Text("Assigned: Dressing, Shoes", fontSize = 10.sp, color = SelforaTextSecondary)
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
