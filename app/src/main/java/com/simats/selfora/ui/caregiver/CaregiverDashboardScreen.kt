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
import com.simats.selfora.data.local.SessionManager
import com.simats.selfora.data.model.HomeProgramResponse
import com.simats.selfora.data.model.LinkedChildInfo
import com.simats.selfora.ui.components.avatar.ChildAvatarImage
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverDashboardScreen(
    onStartPractice: (programId: Long, activityId: String) -> Unit,
    onNavigateToTab: (String) -> Unit,
    onNavigateToMessages: () -> Unit = {},
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onOpenChildMode: () -> Unit = {},
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
        containerColor = SelforaBackground,
        topBar = {
            GlassTopBar(
                title = "${getTimeBasedGreeting()} 👋",
                subtitle = "Caregiver / Parent Portal",
                accentColor = SelforaSecondary,
                actions = {
                    GlassIconButton(
                        icon = Icons.Default.Notifications,
                        onClick = onNavigateToNotifications,
                        tint = SelforaSecondary,
                        size = 40.dp,
                        iconSize = 20.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    GlassIconButton(
                        icon = Icons.Default.Person,
                        onClick = onNavigateToProfile,
                        tint = SelforaSecondary,
                        size = 40.dp,
                        iconSize = 20.dp
                    )
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToMessages,
                containerColor = SelforaPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier.padding(bottom = 90.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Message Doctor",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                },
                text = {
                    Text(
                        text = "Message Doctor",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SelforaBackground)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 150.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Child Selector Bar & Open Child Mode Launcher
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { childDropdownExpanded = true },
                            cornerRadius = 20.dp,
                            backgroundColor = Color.White.copy(alpha = 0.90f),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ChildAvatarImage(
                                        avatarId = if (selectedChild.gender == "BOY") "aarav_boy" else "ananya_girl",
                                        gender = selectedChild.gender,
                                        size = 44.dp
                                    )

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
                                        "Switch",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SelforaSecondary
                                    )
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown",
                                        tint = SelforaSecondary
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

                        GlassButton(
                            text = "🎮 Open Child Mode for ${selectedChild.name}",
                            onClick = {
                                SessionManager.enterChildMode(
                                    adultRole = "ROLE_CAREGIVER",
                                    childId = selectedChild.childId,
                                    childName = selectedChild.name,
                                    gender = selectedChild.gender
                                )
                                onOpenChildMode()
                            },
                            icon = Icons.Default.SportsEsports,
                            gradient = listOf(SelforaSecondary, Color(0xFF9333EA)),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

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
                            CircularProgressIndicator(color = SelforaSecondary)
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
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp,
                        backgroundColor = Color.White.copy(alpha = 0.90f),
                        elevation = 6.dp
                    ) {
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
                            shape = RoundedCornerShape(14.dp),
                            color = SelforaSecondary.copy(alpha = 0.08f),
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

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        backgroundColor = Color.White.copy(alpha = 0.90f),
        elevation = 6.dp,
        onClick = onStartPractice
    ) {
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
                color = SelforaSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        GlassProgressIndicator(
            progress = if (totalSteps > 0) completedSteps.toFloat() / totalSteps else 0f,
            height = 8.dp,
            accentColor = SelforaSecondary,
            progressGradient = listOf(SelforaSecondary, Color(0xFF9333EA))
        )

        Spacer(modifier = Modifier.height(16.dp))

        GlassButton(
            text = "Start Practice",
            icon = Icons.Default.PlayArrow,
            onClick = onStartPractice,
            gradient = listOf(SelforaSecondary, Color(0xFF6D28D9)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )
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
