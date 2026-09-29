package com.simats.selfora.ui.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.HomeProgramResponse
import com.simats.selfora.data.model.LinkedChildInfo
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverDashboardScreen(
    onStartPractice: (programId: Long, activityId: String) -> Unit,
    onNavigateToTab: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    var linkedChildren by remember {
        mutableStateOf(
            listOf(
                LinkedChildInfo(1L, "Aarav Sharma", "BOY", 6, "Dr. Sarah Jenkins"),
                LinkedChildInfo(2L, "Ananya Sharma", "GIRL", 5, "Dr. Sarah Jenkins")
            )
        )
    }
    var selectedChild by remember { mutableStateOf(linkedChildren.first()) }
    var childDropdownExpanded by remember { mutableStateOf(false) }

    var homePrograms by remember { mutableStateOf<List<HomeProgramResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedChild) {
        isLoading = true
        try {
            val res = ApiClient.apiService.getTodayHomePrograms()
            if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                homePrograms = res.body()!!
            } else {
                homePrograms = getSampleHomeProgramsForChild(selectedChild.gender)
            }
        } catch (e: Exception) {
            homePrograms = getSampleHomeProgramsForChild(selectedChild.gender)
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = SelforaSurface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Good Morning 👋",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = SelforaTextSecondary
                            )
                            Text(
                                "Caregiver / Parent",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaTextPrimary
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Notifications Button
                            IconButton(
                                onClick = onNavigateToNotifications,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(SelforaBackground)
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = SelforaPrimary
                                )
                            }

                            // Profile Button
                            IconButton(
                                onClick = onNavigateToProfile,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(SelforaPrimary.copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = SelforaPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Child Selector Section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SelforaBackground)
                            .clickable { childDropdownExpanded = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (selectedChild.gender == "BOY") SelforaPrimary else Color(0xFFEC4899),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        if (selectedChild.gender == "BOY") "👦" else "👧",
                                        fontSize = 18.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    "Selected Child",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaTextSecondary
                                )
                                Text(
                                    "${selectedChild.name} (${selectedChild.age} yrs)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaTextPrimary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Select Child",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SelforaPrimary
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = SelforaPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = childDropdownExpanded,
                            onDismissRequest = { childDropdownExpanded = false }
                        ) {
                            linkedChildren.forEach { child ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${child.name} (${if (child.gender == "BOY") "Boy" else "Girl"})",
                                            fontWeight = if (child.childId == selectedChild.childId) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        selectedChild = child
                                        childDropdownExpanded = false
                                    },
                                    leadingIcon = {
                                        Text(if (child.gender == "BOY") "👦" else "👧", fontSize = 16.sp)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            CaregiverBottomNavigation(
                currentRoute = "caregiver_dashboard",
                onTabSelected = onNavigateToTab
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SelforaBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Today's Practice",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SelforaTextPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SelforaSuccess.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "${homePrograms.size} Activities Assigned",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaSuccess
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SelforaPrimary)
                    }
                }
            } else {
                items(homePrograms) { program ->
                    CaregiverActivityCard(
                        program = program,
                        onStartPractice = {
                            val actCode = when (program.activityId) {
                                13L -> "eating_spoon_activity"
                                9L -> "shoes_socks_activity"
                                else -> if (selectedChild.gender == "GIRL") "girl_frock_activity" else "boy_tshirt_activity"
                            }
                            onStartPractice(program.id, actCode)
                        }
                    )
                }
            }

            // Therapist Instructions Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SelforaSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Chat,
                                contentDescription = null,
                                tint = SelforaSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Therapist Instructions",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SelforaTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Assigned by: ${selectedChild.therapistName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SelforaTextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SelforaBackground,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Practice dressing before school. Focus on allowing ${selectedChild.name} to attempt the steps independently. Offer gentle verbal guidance if stuck.",
                                modifier = Modifier.padding(12.dp),
                                fontSize = 13.sp,
                                color = SelforaTextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Bottom Spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CaregiverActivityCard(
    program: HomeProgramResponse,
    onStartPractice: () -> Unit
) {
    val (iconEmoji, totalSteps, completedSteps) = when (program.activityId) {
        13L -> Triple("🥣", 10, 0)
        9L -> Triple("👟", 10, 0)
        else -> Triple("👕", 18, 7)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SelforaSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(iconEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        program.activityTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = SelforaTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$totalSteps Steps",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SelforaTextSecondary
                )
                Text(
                    "Progress: $completedSteps / $totalSteps",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SelforaPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { if (totalSteps > 0) completedSteps.toFloat() / totalSteps else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = SelforaPrimary,
                trackColor = SelforaBorder
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStartPractice,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Practice", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            }
        }
    }
}

fun getSampleHomeProgramsForChild(gender: String): List<HomeProgramResponse> {
    val dressingTitle = if (gender == "GIRL") "Girl Frock Dressing" else "Boy T-Shirt Dressing"
    val dressingId = if (gender == "GIRL") 2L else 1L

    return listOf(
        HomeProgramResponse(
            id = 1L,
            childId = 1L,
            childName = "Aarav Sharma",
            therapistId = 1L,
            therapistName = "Dr. Sarah Jenkins",
            activityId = dressingId,
            activityTitle = dressingTitle,
            frequencyPerWeek = 4,
            targetDurationMinutes = 15,
            targetPromptLevelId = 1,
            targetPromptLevelName = "Visual Prompt",
            goalStatement = "Improve independent dressing.",
            caregiverInstructions = "Practice T-shirt dressing before school.",
            startDate = "2026-09-28",
            endDate = "2026-10-12",
            isActive = true,
            createdAt = "2026-09-28"
        ),
        HomeProgramResponse(
            id = 2L,
            childId = 1L,
            childName = "Aarav Sharma",
            therapistId = 1L,
            therapistName = "Dr. Sarah Jenkins",
            activityId = 13L,
            activityTitle = "Eating with Spoon",
            frequencyPerWeek = 3,
            targetDurationMinutes = 10,
            targetPromptLevelId = 1,
            targetPromptLevelName = "Visual Prompt",
            goalStatement = "Practice spoon holding.",
            caregiverInstructions = "Encourage proper grip during lunch.",
            startDate = "2026-09-28",
            endDate = "2026-10-12",
            isActive = true,
            createdAt = "2026-09-28"
        ),
        HomeProgramResponse(
            id = 3L,
            childId = 1L,
            childName = "Aarav Sharma",
            therapistId = 1L,
            therapistName = "Dr. Sarah Jenkins",
            activityId = 9L,
            activityTitle = "Shoes & Socks",
            frequencyPerWeek = 3,
            targetDurationMinutes = 10,
            targetPromptLevelId = 1,
            targetPromptLevelName = "Visual Prompt",
            goalStatement = "Practice putting on socks and shoes.",
            caregiverInstructions = "Assist with shoe positioning.",
            startDate = "2026-09-28",
            endDate = "2026-10-12",
            isActive = true,
            createdAt = "2026-09-28"
        )
    )
}
