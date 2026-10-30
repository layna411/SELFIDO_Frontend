package com.simats.selfora.ui.admin

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.AdminDashboardStatsResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminDashboardScreen(
    onNavigateToTherapists: () -> Unit,
    onNavigateToCreateTherapist: () -> Unit,
    onNavigateToChildren: () -> Unit,
    onNavigateToCaregivers: () -> Unit,
    onNavigateToClinicalOverview: () -> Unit = {},
    onNavigateToHomeProgrammes: () -> Unit = {},
    onNavigateToReports: () -> Unit,
    onNavigateToAuditLogs: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    var stats by remember { mutableStateOf(AdminDashboardStatsResponse()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val darkSlate = Color(0xFF0F172A)
    val goldAccent = Color(0xFFD97706)
    val blueAccent = Color(0xFF2563EB)
    val purpleAccent = Color(0xFF7C3AED)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    fun loadDashboardData() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val statsRes = ApiClient.apiService.getAdminDashboardStats()
                if (statsRes.isSuccessful && statsRes.body() != null) {
                    stats = statsRes.body()!!
                } else {
                    val code = statsRes.code()
                    errorMessage = if (code == 403) {
                        "Access denied. Super Admin privileges required."
                    } else {
                        "Failed to load dashboard statistics (Error $code)."
                    }
                }
            } catch (e: Exception) {
                errorMessage = if (e is java.io.IOException) {
                    "Unable to connect to server. Please check network connection."
                } else {
                    "Error loading dashboard data. Please try again."
                }
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadDashboardData()
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
                                        text = "System Operational • Administrator Console",
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

            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp)
            ) {
                // Error Banner with Retry Action
                if (errorMessage != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFEE2E2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF991B1B),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Button(
                                onClick = { loadDashboardData() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Retry", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Quick Action: Register New Therapist (Admin primary creation duty)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, blueAccent.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(blueAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = blueAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Therapist Registration",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Admin creates & onboards clinical therapists",
                                fontSize = 11.5.sp,
                                color = textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onNavigateToCreateTherapist,
                            colors = ButtonDefaults.buttonColors(containerColor = blueAccent),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Therapist", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section Title: Executive Statistics
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "System Statistics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "Real-time user & platform activity counts",
                        fontSize = 11.5.sp,
                        color = textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Clean 2x2 Stats Grid with pixel-perfect alignment
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
                            title = "Caregiver Accounts",
                            value = "${stats.totalCaregivers}",
                            subtitle = "Linked Parent Logins",
                            icon = Icons.Default.FamilyRestroom,
                            accentColor = purpleAccent,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToCaregivers
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdminStatCard(
                            title = "Registered Children",
                            value = "${stats.totalChildren}",
                            subtitle = "Clinical Profiles",
                            icon = Icons.Default.ChildCare,
                            accentColor = Color(0xFF10B981),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToChildren
                        )
                        AdminStatCard(
                            title = "Therapy Sessions",
                            value = "${stats.activeTherapySessions}",
                            subtitle = "Recorded Sessions",
                            icon = Icons.Default.Psychology,
                            accentColor = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToReports
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // System Administration Modules Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Administration Modules",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "Manage system accounts, access controls, and security logs",
                        fontSize = 11.5.sp,
                        color = textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminModuleRow(
                        title = "Therapist & Clinician Management",
                        description = "Register, edit profile, activate/deactivate therapists & reset credentials",
                        icon = Icons.Default.MedicalServices,
                        accentColor = blueAccent,
                        onClick = onNavigateToTherapists
                    )

                    AdminModuleRow(
                        title = "Caregiver & Parent Accounts",
                        description = "Oversee parent logins, access status, linked children & password resets",
                        icon = Icons.Default.FamilyRestroom,
                        accentColor = purpleAccent,
                        onClick = onNavigateToCaregivers
                    )

                    AdminModuleRow(
                        title = "Centralized Child Records",
                        description = "View child clinical profiles, linked caregivers & assigned clinicians",
                        icon = Icons.Default.ChildCare,
                        accentColor = Color(0xFF10B981),
                        onClick = onNavigateToChildren
                    )

                    AdminModuleRow(
                        title = "Security & System Audit Logs",
                        description = "Audit trail of logins, administrative actions, and data updates",
                        icon = Icons.Default.History,
                        accentColor = darkSlate,
                        onClick = onNavigateToAuditLogs
                    )

                    AdminModuleRow(
                        title = "Platform Reports & Overview",
                        description = "Generate clinician performance and child registration reports",
                        icon = Icons.Default.BarChart,
                        accentColor = Color(0xFFF59E0B),
                        onClick = onNavigateToReports
                    )

                    AdminModuleRow(
                        title = "System Settings & Access Security",
                        description = "Administrative configuration, credentials & session preferences",
                        icon = Icons.Default.Settings,
                        accentColor = Color(0xFF64748B),
                        onClick = onNavigateToSettings
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
            .fillMaxWidth()
            .height(134.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 2.5.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Top Row: Icon on left, Chevron arrow on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Big Metric Value - strictly anchored at identical Y level across left & right cards
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A),
                lineHeight = 26.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Title - strictly anchored at identical Y level across left & right cards
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Subtitle - strictly anchored at identical Y level across left & right cards
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
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 2.dp,
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
                    fontSize = 14.5.sp,
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
