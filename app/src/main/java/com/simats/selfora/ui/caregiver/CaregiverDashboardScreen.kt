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
import com.simats.selfora.data.repository.NotificationRepository
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
    var linkedChildren by remember { mutableStateOf<List<LinkedChildInfo>>(emptyList()) }
    var selectedChild by remember { mutableStateOf<LinkedChildInfo?>(null) }
    var childDropdownExpanded by remember { mutableStateOf(false) }

    var homePrograms by remember { mutableStateOf<List<HomeProgramResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun loadDashboardData() {
        isLoading = true
        scope.launch {
            try {
                val response = ApiClient.apiService.getCaregiverDashboardSummary()
                if (response.isSuccessful && response.body() != null) {
                    val summary = response.body()!!
                    linkedChildren = summary.linkedChildren
                    if (linkedChildren.isNotEmpty()) {
                        selectedChild = linkedChildren.first()
                    }
                }
                val hpRes = ApiClient.apiService.getTodayHomePrograms()
                if (hpRes.isSuccessful && hpRes.body() != null) {
                    homePrograms = hpRes.body()!!
                }
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    val unreadNotificationCount by NotificationRepository.unreadCount.collectAsState()

    LaunchedEffect(Unit) {
        loadDashboardData()
        NotificationRepository.fetchNotifications()
    }

    Scaffold(
        containerColor = SelforaBackground,
        topBar = {
            val currentLanguage by com.simats.selfora.utils.LanguageManager.currentLanguage.collectAsState()
            val isTamil = currentLanguage == "ta"
            GlassTopBar(
                title = if (isTamil) "பராமரிப்பாளர் முகப்பு" else "Caregiver Home",
                subtitle = if (isTamil) "வீட்டுப் பயிற்சி & வழிகாட்டுதல்" else "Home Practice & Guidance",
                accentColor = SelforaPrimary,
                actions = {
                    if (unreadNotificationCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = Color(0xFFEF4444),
                                    contentColor = Color.White
                                ) {
                                    Text("$unreadNotificationCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        ) {
                            GlassIconButton(
                                icon = Icons.Default.Notifications,
                                onClick = onNavigateToNotifications,
                                tint = SelforaPrimary,
                                size = 36.dp,
                                iconSize = 18.dp
                            )
                        }
                    } else {
                        GlassIconButton(
                            icon = Icons.Default.Notifications,
                            onClick = onNavigateToNotifications,
                            tint = SelforaPrimary,
                            size = 36.dp,
                            iconSize = 18.dp
                        )
                    }
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Greeting Hero Banner (Image 2 Screen 2)
                item {
                    val caregiverName = SessionManager.getUsername().ifBlank { "Caregiver" }
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 22.dp,
                        backgroundColor = Color.White.copy(alpha = 0.95f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hello, $caregiverName!",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SelforaTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "You're doing a great job!",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SelforaTextSecondary
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = SelforaPrimary.copy(alpha = 0.12f),
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("👩‍👦", fontSize = 28.sp)
                                }
                            }
                        }
                    }
                }

                // Today's Home Program Section
                item {
                    val todayDateStr = remember {
                        java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    }
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Today's Home Program",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SelforaTextPrimary
                            )
                            Text(
                                todayDateStr,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SelforaTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (homePrograms.isEmpty()) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("✨", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("No Practice Scheduled for Today", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SelforaTextPrimary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Great job staying on track! Your therapist will assign new routines as goals progress.", fontSize = 12.sp, color = SelforaTextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            }
                        } else {
                            val primaryProg = homePrograms.first()
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 20.dp,
                                backgroundColor = Color.White.copy(alpha = 0.95f),
                                borderColor = SelforaPrimary.copy(alpha = 0.3f),
                                elevation = 4.dp
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = SelforaPrimary.copy(alpha = 0.12f),
                                        modifier = Modifier.size(50.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("👕", fontSize = 26.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            primaryProg.title ?: "Home Practice",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = SelforaTextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            primaryProg.description ?: "Assigned ADL practice",
                                            fontSize = 13.sp,
                                            color = SelforaTextSecondary
                                        )
                                        Text(
                                            "${primaryProg.frequencyPerWeek} times per week",
                                            fontSize = 12.sp,
                                            color = SelforaPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                GlassButton(
                                    text = "Start Practice Session",
                                    icon = Icons.Default.PlayArrow,
                                    onClick = { onStartPractice(primaryProg.id, "boy_tshirt_activity") },
                                    gradient = listOf(SelforaPrimary, Color(0xFF6D28D9)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                )
                            }
                        }
                    }
                }

                // Other Activities Section (Image 2 Screen 2)
                item {
                    Text(
                        "Other Activities",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SelforaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OtherActivityItem(
                            emoji = "👕",
                            title = "Wearing T-Shirt",
                            frequency = "2 times a day",
                            onClick = { onStartPractice(2L, "boy_tshirt_activity") }
                        )

                        OtherActivityItem(
                            emoji = "🥣",
                            title = "Eating with Spoon",
                            frequency = "During meals",
                            onClick = { onStartPractice(3L, "eating_spoon_activity") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OtherActivityItem(
    emoji: String,
    title: String,
    frequency: String,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        cornerRadius = 16.dp,
        backgroundColor = Color.White.copy(alpha = 0.9f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SelforaBgLight,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(emoji, fontSize = 22.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SelforaTextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(frequency, fontSize = 12.sp, color = SelforaTextSecondary)
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SelforaTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
