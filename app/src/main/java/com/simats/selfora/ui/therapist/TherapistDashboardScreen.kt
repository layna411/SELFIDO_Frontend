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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.TherapistDashboardSummaryResponse
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapistDashboardScreen(
    onNavigateToAddChildWorkflow: () -> Unit,
    onNavigateToChildren: () -> Unit,
    onNavigateToAssessment: () -> Unit,
    onNavigateToHomePrograms: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onLogout: () -> Unit
) {
    var summary by remember { mutableStateOf(TherapistDashboardSummaryResponse()) }
    var currentTab by remember { mutableStateOf(TherapistTab.DASHBOARD) }

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
            TopAppBar(
                title = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Good Morning, ${summary.therapistName} 👋",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary,
                            maxLines = 1,
                            softWrap = false,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "SELFIDO Clinical Dashboard",
                            fontSize = 12.sp,
                            color = SelforaTextSecondary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        BadgedBox(
                            badge = { Badge { Text("3") } }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = SelforaPrimary)
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Surface(
                            shape = CircleShape,
                            color = SelforaPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Profile", tint = SelforaPrimary, modifier = Modifier.padding(6.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SelforaSurface)
            )
        },
        bottomBar = {
            GlassNavigationBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    currentTab = tab
                    when (tab) {
                        TherapistTab.DASHBOARD -> {}
                        TherapistTab.CHILDREN -> onNavigateToChildren()
                        TherapistTab.ASSESSMENTS -> onNavigateToAssessment()
                        TherapistTab.PROGRESS -> onNavigateToProgress()
                        TherapistTab.MESSAGES -> onNavigateToMessages()
                    }
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
            Text(
                text = "CLINICAL OVERVIEW",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaPrimary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // 4 Summary Glass Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardSummaryGlassCard(
                    title = "Children",
                    count = "${summary.activeChildrenCount}",
                    subtitle = "Active Children",
                    emoji = "👦",
                    accentColor = SelforaPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToChildren
                )
                DashboardSummaryGlassCard(
                    title = "Caregivers",
                    count = "${summary.linkedCaregiversCount}",
                    subtitle = "Linked Caregivers",
                    emoji = "👨‍👩‍👦",
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
                    title = "Assessments",
                    count = "${summary.pendingAssessmentsCount}",
                    subtitle = "Pending Evaluation",
                    emoji = "📋",
                    accentColor = SelforaWarning,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAssessment
                )
                DashboardSummaryGlassCard(
                    title = "Home Programmes",
                    count = "${summary.activeHomeProgramsCount}",
                    subtitle = "Active Programs",
                    emoji = "🏠",
                    accentColor = SelforaSuccess,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToHomePrograms
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "QUICK ACTIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaPrimary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Large Quick Action Glass Buttons
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionLargeGlassButton(
                    title = "+ Add Child",
                    subtitle = "Single workflow to create child & link caregiver with generated temp password",
                    icon = Icons.Default.PersonAdd,
                    gradient = listOf(SelforaPrimary, Color(0xFF1D4ED8)),
                    onClick = onNavigateToAddChildWorkflow
                )

                QuickActionLargeGlassButton(
                    title = "+ Add Caregiver",
                    subtitle = "Register parent/guardian and generate secure credentials",
                    icon = Icons.Default.GroupAdd,
                    gradient = listOf(SelforaSecondary, Color(0xFF6D28D9)),
                    onClick = onNavigateToAddChildWorkflow
                )

                QuickActionLargeGlassButton(
                    title = "+ New Assessment",
                    subtitle = "Evaluate ADL baseline (Dressing, Eating, Shoes) and prompt levels",
                    icon = Icons.Default.Assignment,
                    gradient = listOf(SelforaSuccess, Color(0xFF059669)),
                    onClick = onNavigateToAssessment
                )

                QuickActionLargeGlassButton(
                    title = "+ Home Programme",
                    subtitle = "Create & assign structured ADL home practice to caregivers",
                    icon = Icons.Default.HomeWork,
                    gradient = listOf(SelforaWarning, Color(0xFFD97706)),
                    onClick = onNavigateToHomePrograms
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
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
        contentPadding = PaddingValues(12.dp),
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
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = gradient.first(), modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, fontSize = 12.sp, color = SelforaTextSecondary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SelforaTextSecondary)
        }
    }
}
