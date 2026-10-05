package com.simats.selfora.ui.admin

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.AdminChartDataResponse
import com.simats.selfora.data.model.AdminDashboardStatsResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminDashboardScreen(
    onNavigateToTherapists: () -> Unit,
    onNavigateToCreateTherapist: () -> Unit,
    onNavigateToChildren: () -> Unit,
    onNavigateToCaregivers: () -> Unit,
    onNavigateToClinicalOverview: () -> Unit,
    onNavigateToHomeProgrammes: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToAuditLogs: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    var stats by remember { mutableStateOf(AdminDashboardStatsResponse()) }
    var chartData by remember { mutableStateOf(AdminChartDataResponse()) }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val darkSlate = Color(0xFF0F172A)
    val goldAccent = Color(0xFFD97706)
    val blueAccent = Color(0xFF2563EB)
    val purpleAccent = Color(0xFF7C3AED)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val statsRes = ApiClient.apiService.getAdminDashboardStats()
            if (statsRes.isSuccessful && statsRes.body() != null) {
                stats = statsRes.body()!!
            }
            val chartRes = ApiClient.apiService.getAdminChartData()
            if (chartRes.isSuccessful && chartRes.body() != null) {
                chartData = chartRes.body()!!
            }
        } catch (e: Exception) {
            // Keep default stats DTO
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Top Executive Header Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = darkSlate,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 14.dp)
                ) {
                    // Header Brand & Action Icons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(goldAccent.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = goldAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "SELFIDO ADMIN",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Settings Icon
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Logout Button
                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Logout",
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Welcome Greeting Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(goldAccent.copy(alpha = 0.25f))
                                    .border(1.5.dp, goldAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "SA",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = goldAccent
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Welcome back, Administrator",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color(0xFF22C55E), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "System Operational • Live Analytics Active",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Main Content Area with Tight Vertical Spacing (No Gaps)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp)
            ) {
                // Section Title
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Executive Statistics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "Platform performance overview",
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats Grid (Equal-Height Cards 148dp - Zero Gap, Zero Text Clipping)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdminStatCard(
                            title = "Total Therapists",
                            value = "${stats.totalTherapists}",
                            subtitle = "${stats.activeTherapists} Active Clinicians",
                            icon = Icons.Default.MedicalServices,
                            accentColor = blueAccent,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToTherapists
                        )
                        AdminStatCard(
                            title = "Registered Children",
                            value = "${stats.totalChildren}",
                            subtitle = "ADL Active Profiles",
                            icon = Icons.Default.ChildCare,
                            accentColor = Color(0xFF10B981),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToChildren
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdminStatCard(
                            title = "Caregiver Accounts",
                            value = "${stats.totalCaregivers}",
                            subtitle = "Linked Parent Logins",
                            icon = Icons.Default.FamilyRestroom,
                            accentColor = purpleAccent,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToCaregivers
                        )
                        AdminStatCard(
                            title = "Therapy Sessions",
                            value = "${stats.activeTherapySessions}",
                            subtitle = "Recorded Sessions",
                            icon = Icons.Default.Psychology,
                            accentColor = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToClinicalOverview
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdminStatCard(
                            title = "Completed Assessments",
                            value = "${stats.completedAssessments}",
                            subtitle = "Clinical Baselines",
                            icon = Icons.Default.AssignmentTurnedIn,
                            accentColor = Color(0xFF06B6D4),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToClinicalOverview
                        )
                        AdminStatCard(
                            title = "Home Programmes",
                            value = "${stats.activeHomeProgrammes}",
                            subtitle = "${stats.pendingCaregiverReviews} Pending Reviews",
                            icon = Icons.Default.HomeWork,
                            accentColor = Color(0xFFEC4899),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToHomeProgrammes
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Interactive Chart Section
                Text(
                    text = "Analytics & ADL Distribution",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ADL Skill Distribution",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Text(
                                    text = "Active child skills training across categories",
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = blueAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        chartData.adlCategoryDistribution.forEach { (category, count) ->
                            val percentage = (count.toFloat() / 100f).coerceIn(0.08f, 1.0f)
                            val catColor = when (category) {
                                "Dressing" -> blueAccent
                                "Eating" -> Color(0xFF10B981)
                                "Grooming" -> purpleAccent
                                else -> Color(0xFFF59E0B)
                            }

                            Column(modifier = Modifier.padding(vertical = 5.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(catColor)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(category, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                    }
                                    Text(
                                        text = "$count children (${(percentage * 100).toInt()}%)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(5.dp))

                                LinearProgressIndicator(
                                    progress = { percentage },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(CircleShape),
                                    color = catColor,
                                    trackColor = catColor.copy(alpha = 0.12f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // System Administration Modules Section
                Text(
                    text = "System Administration Modules",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminModuleRow(
                        title = "Therapist & Doctor Management",
                        description = "Add, edit, deactivate, or reassign therapists & clinicians",
                        icon = Icons.Default.MedicalServices,
                        accentColor = blueAccent,
                        onClick = onNavigateToTherapists
                    )

                    AdminModuleRow(
                        title = "Centralized Child Management",
                        description = "View profiles, linked caregivers, and therapy activity",
                        icon = Icons.Default.ChildCare,
                        accentColor = Color(0xFF10B981),
                        onClick = onNavigateToChildren
                    )

                    AdminModuleRow(
                        title = "Caregiver Accounts & Linking",
                        description = "Manage parent logins, home practice, and access control",
                        icon = Icons.Default.FamilyRestroom,
                        accentColor = purpleAccent,
                        onClick = onNavigateToCaregivers
                    )

                    AdminModuleRow(
                        title = "Clinical Data & Prompt Level Overview",
                        description = "Audit prompt levels L0-L6, fading plans & independence rates",
                        icon = Icons.Default.Assessment,
                        accentColor = Color(0xFF06B6D4),
                        onClick = onNavigateToClinicalOverview
                    )

                    AdminModuleRow(
                        title = "Home Programmes Overview",
                        description = "Review all active home training schedules & completions",
                        icon = Icons.Default.HomeWork,
                        accentColor = Color(0xFFEC4899),
                        onClick = onNavigateToHomeProgrammes
                    )

                    AdminModuleRow(
                        title = "Reports & PDF/Excel Exports",
                        description = "Generate clinician performance & clinical progress reports",
                        icon = Icons.Default.BarChart,
                        accentColor = Color(0xFFF59E0B),
                        onClick = onNavigateToReports
                    )

                    AdminModuleRow(
                        title = "System Activity & Audit Logs",
                        description = "Review security logs, login attempts, and data edits",
                        icon = Icons.Default.History,
                        accentColor = darkSlate,
                        onClick = onNavigateToAuditLogs
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(148.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp), clip = false)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A),
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AdminModuleRow(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp), clip = false)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
