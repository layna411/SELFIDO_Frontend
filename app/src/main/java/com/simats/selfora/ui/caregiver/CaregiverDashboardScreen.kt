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
import com.simats.selfora.data.repository.PracticeRecordEntry
import com.simats.selfora.data.repository.PracticeSessionStore
import com.simats.selfora.data.local.SessionManager
import com.simats.selfora.data.model.CaregiverPracticeSessionResponse
import com.simats.selfora.data.model.HomeProgramResponse
import com.simats.selfora.data.model.LinkedChildInfo
import com.simats.selfora.ui.components.avatar.ChildAvatarImage
import com.simats.selfora.ui.components.dialogs.PracticeSessionIntroDialog
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.therapist.SelfidoCatalogue
import com.simats.selfora.ui.theme.*
import com.simats.selfora.utils.LanguageManager
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

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
    var recentPracticeHistory by remember { mutableStateOf<List<CaregiverPracticeSessionResponse>>(emptyList()) }
    var totalCompletedPractices by remember { mutableStateOf(0) }
    val localRecords by PracticeSessionStore.records.collectAsState()
    var isLoading by remember { mutableStateOf(false) }

    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isTamil = currentLanguage == "ta"

    var showPracticeIntro by remember { mutableStateOf(false) }
    var pendingPracticeProgId by remember { mutableStateOf<Long?>(null) }
    var pendingActivityId by remember { mutableStateOf<String?>(null) }
    var pendingProgramTitle by remember { mutableStateOf("ADL Practice Session") }

    val scope = rememberCoroutineScope()

    fun loadDashboardData() {
        isLoading = true
        scope.launch {
            try {
                val response = ApiClient.apiService.getCaregiverDashboardSummary()
                if (response.isSuccessful && response.body() != null) {
                    val summary = response.body()!!
                    linkedChildren = summary.linkedChildren
                    recentPracticeHistory = summary.recentPracticeHistory
                    totalCompletedPractices = summary.totalCompletedPractices
                    if (summary.todayHomePrograms.isNotEmpty()) {
                        homePrograms = summary.todayHomePrograms
                    }
                    if (linkedChildren.isNotEmpty()) {
                        val currentActiveId = SessionManager.getActiveChildId()
                        val matchedChild = linkedChildren.find { it.childId == currentActiveId } ?: linkedChildren.first()
                        selectedChild = matchedChild
                        SessionManager.saveActiveChildContext(matchedChild.childId, matchedChild.name, matchedChild.gender)
                    }
                }
                val hpRes = ApiClient.apiService.getTodayHomePrograms()
                if (hpRes.isSuccessful && !hpRes.body().isNullOrEmpty()) {
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

                // 1. Modern Practice Completion & Weekly Progress Tracker
                item {
                    val childPrograms = if (selectedChild != null) {
                        homePrograms.filter { it.childId == selectedChild!!.childId }.ifEmpty { homePrograms }
                    } else {
                        homePrograms
                    }
                    val progChildGender = selectedChild?.gender ?: SessionManager.getActiveChildGender()
                    val isGirlChild = progChildGender.uppercase().startsWith("G") || progChildGender.uppercase().startsWith("F")

                    WeeklyPracticeProgressCard(
                        recentHistory = recentPracticeHistory,
                        localRecords = localRecords,
                        homePrograms = childPrograms,
                        isGirl = isGirlChild
                    )
                }

                // 2. Assigned Home Programs Section (Displays ALL therapist-assigned practices)
                item {
                    val childPrograms = if (selectedChild != null) {
                        homePrograms.filter { it.childId == selectedChild!!.childId }.ifEmpty { homePrograms }
                    } else {
                        homePrograms
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Assigned Home Programs",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = SelforaTextPrimary
                                )
                                Text(
                                    "Therapist-prescribed clinical ADL routines",
                                    fontSize = 12.sp,
                                    color = SelforaTextSecondary
                                )
                            }
                            if (childPrograms.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SelforaPrimary.copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SelforaPrimary.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        "${childPrograms.size} Active",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SelforaPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (childPrograms.isEmpty()) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("✨", fontSize = 36.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "No Assigned Routines Yet",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = SelforaTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Your occupational therapist will assign tailored ADL routines here. As soon as routines are prescribed, they will appear with full instructions.",
                                        fontSize = 12.sp,
                                        color = SelforaTextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                childPrograms.forEach { prog ->
                                    val progGender = prog.childGender ?: selectedChild?.gender ?: SessionManager.getActiveChildGender()
                                    val isGirl = progGender.uppercase().startsWith("G") || progGender.uppercase().startsWith("F")
                                    val actDef = SelfidoCatalogue.getActivityById(prog.activityId)
                                    val actEmoji = actDef?.icon ?: if (isGirl) "👗" else "👕"

                                    // Calculate how many times this specific task was completed
                                    val taskCompletions = (recentPracticeHistory.filter { it.homeProgramId == prog.id }.size +
                                            localRecords.filter { it.programId == prog.id }.size)

                                    AssignedPracticeCard(
                                        program = prog,
                                        actEmoji = actEmoji,
                                        isGirl = isGirl,
                                        completionsCount = taskCompletions,
                                        onStart = {
                                            pendingPracticeProgId = prog.id
                                            pendingActivityId = prog.activityId.toString()
                                            pendingProgramTitle = prog.title ?: "${actDef?.title ?: "ADL"} Practice"
                                            showPracticeIntro = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPracticeIntro && pendingPracticeProgId != null) {
        PracticeSessionIntroDialog(
            activityTitle = pendingProgramTitle,
            isTamil = isTamil,
            onStartPractice = {
                val progId = pendingPracticeProgId!!
                val actId = pendingActivityId ?: if (SessionManager.getActiveChildGender().uppercase().let { it.startsWith("G") || it.startsWith("F") }) "17" else "1"
                showPracticeIntro = false
                onStartPractice(progId, actId)
            },
            onDismiss = {
                showPracticeIntro = false
            }
        )
    }
}

/**
 * Modern 7-day pill strip and completion tracker showing weekly progress,
 * practiced days, and pending practice routines for today.
 */
@Composable
fun WeeklyPracticeProgressCard(
    recentHistory: List<CaregiverPracticeSessionResponse>,
    localRecords: List<PracticeRecordEntry>,
    homePrograms: List<HomeProgramResponse>,
    isGirl: Boolean
) {
    val today = remember { LocalDate.now() }
    val monday = remember(today) { today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }

    // Aggregate unique dates practiced this week
    val practicedDaysThisWeek = remember(recentHistory, localRecords, monday) {
        val days = mutableSetOf<LocalDate>()
        recentHistory.forEach { s ->
            try {
                val dt = LocalDate.parse(s.practiceDate.take(10))
                if (!dt.isBefore(monday) && !dt.isAfter(monday.plusDays(6))) {
                    days.add(dt)
                }
            } catch (_: Exception) {}
        }
        localRecords.forEach { r ->
            try {
                val dt = LocalDate.parse(r.practiceDate.take(10))
                if (!dt.isBefore(monday) && !dt.isAfter(monday.plusDays(6))) {
                    days.add(dt)
                }
            } catch (_: Exception) {}
        }
        days
    }

    val totalCompletedThisWeek = practicedDaysThisWeek.size
    val weeklyTargetDays = if (homePrograms.isNotEmpty()) {
        homePrograms.maxOfOrNull { it.frequencyPerWeek }?.coerceIn(1, 7) ?: 5
    } else {
        5
    }

    val progressPercent = (totalCompletedThisWeek.toFloat() / weeklyTargetDays.toFloat()).coerceIn(0f, 1f)
    val isPracticedToday = practicedDaysThisWeek.contains(today)
    val accentColor = if (isGirl) SelforaSecondary else SelforaPrimary

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        backgroundColor = Color.White.copy(alpha = 0.95f),
        borderColor = accentColor.copy(alpha = 0.25f),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Metric & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = accentColor.copy(alpha = 0.12f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Weekly Consistency", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SelforaTextPrimary)
                        Text("$totalCompletedThisWeek of $weeklyTargetDays practice days met", fontSize = 11.5.sp, color = SelforaTextSecondary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isPracticedToday) SelforaSuccess.copy(alpha = 0.14f) else Color(0xFFF59E0B).copy(alpha = 0.14f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isPracticedToday) SelforaSuccess.copy(alpha = 0.35f) else Color(0xFFF59E0B).copy(alpha = 0.35f))
                ) {
                    Text(
                        text = if (isPracticedToday) "Done Today ✅" else "Pending Today ⏳",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPracticedToday) SelforaSuccess else Color(0xFFD97706),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Modern Linear Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LinearProgressIndicator(
                    progress = { progressPercent },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.12f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${(progressPercent * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7-Day Day Capsules (Mon to Sun)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                for (i in 0..6) {
                    val dayDate = monday.plusDays(i.toLong())
                    val isDone = practicedDaysThisWeek.contains(dayDate)
                    val isCurrentDay = dayDate == today

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when {
                                isDone -> accentColor
                                isCurrentDay -> accentColor.copy(alpha = 0.14f)
                                else -> SelforaBgLight
                            },
                            border = if (isCurrentDay && !isDone) androidx.compose.foundation.BorderStroke(1.5.dp, accentColor) else null,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isDone) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${dayDate.dayOfMonth}",
                                        fontSize = 12.sp,
                                        fontWeight = if (isCurrentDay) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isCurrentDay) accentColor else SelforaTextSecondary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dayLabels[i],
                            fontSize = 10.sp,
                            fontWeight = if (isCurrentDay) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrentDay) accentColor else SelforaTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Today's Status Message
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isPracticedToday) SelforaSuccess.copy(alpha = 0.08f) else Color(0xFFF59E0B).copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isPracticedToday) "🌟" else "💡",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPracticedToday)
                            "Great job! Today's home practice session has been completed."
                        else
                            "Practice session pending for today. Regular consistency builds daily life independence!",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = SelforaTextPrimary,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

/**
 * Modern Card for an assigned ADL practice showing:
 * - Activity Icon & Title
 * - Frequency per week, preferred routine time, target duration
 * - Prescribed prompt level & target focus steps
 * - Therapist clinical advice
 * - Number of times practiced
 * - Start Practice Session button
 */
@Composable
fun AssignedPracticeCard(
    program: HomeProgramResponse,
    actEmoji: String,
    isGirl: Boolean,
    completionsCount: Int,
    onStart: () -> Unit
) {
    val themeColor = if (isGirl) SelforaSecondary else SelforaPrimary

    // Parse focus steps from targetSteps or stepSpecificInstructions
    val focusStepsCount = remember(program) {
        if (program.targetSteps.isNotEmpty()) {
            program.targetSteps.size
        } else {
            val instr = program.stepSpecificInstructions ?: ""
            if (instr.startsWith("FOCUS_STEPS:")) {
                instr.removePrefix("FOCUS_STEPS:").split(",").filter { it.isNotBlank() }.size
            } else {
                0
            }
        }
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        backgroundColor = Color.White.copy(alpha = 0.96f),
        borderColor = themeColor.copy(alpha = 0.28f),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Activity Icon, Title, and Completion Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = themeColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(actEmoji, fontSize = 26.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = program.title ?: "Home ADL Routine",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SelforaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = program.description ?: "Therapist-prescribed daily living practice.",
                        fontSize = 12.sp,
                        color = SelforaTextSecondary,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Prescription Parameter Badges (Frequency, Routine, Duration, Target Level)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Frequency badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = themeColor.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(
                        0.8.dp,
                        themeColor.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "FREQUENCY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "${program.frequencyPerWeek}x / wk",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                    }
                }

                // Routine Time badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0EA5E9).copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(
                        0.8.dp,
                        Color(0xFF0EA5E9).copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "ROUTINE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0EA5E9)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            program.preferredTime?.take(8) ?: "Daily",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                    }
                }

                // Duration badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF8B5CF6).copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(
                        0.8.dp,
                        Color(0xFF8B5CF6).copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "DURATION",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B5CF6)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "${program.targetDurationMinutes} mins",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                    }
                }

                // Completed Counter
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SelforaSuccess.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(
                        0.8.dp,
                        SelforaSuccess.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "COMPLETED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaSuccess
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "$completionsCount times",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                    }
                }
            }

            // Target Prompt Level & Focus Steps
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (program.targetPromptLevelName.isNotBlank()) {
                    Text(
                        text = "🎯 Target: ${program.targetPromptLevelName}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColor
                    )
                }
                if (focusStepsCount > 0) {
                    Text(
                        text = "📌 $focusStepsCount focus step${if (focusStepsCount > 1) "s" else ""}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SelforaTextSecondary
                    )
                }
            }

            // Therapist Advice Note (if present)
            if (!program.caregiverInstructions.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SelforaBgLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("👩‍⚕️", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = program.caregiverInstructions,
                            fontSize = 11.5.sp,
                            color = SelforaTextPrimary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Start Practice Session Button
            GlassButton(
                text = "Start Practice Session",
                icon = Icons.Default.PlayArrow,
                onClick = onStart,
                gradient = if (isGirl) listOf(SelforaSecondary, Color(0xFFC026D3)) else listOf(
                    SelforaPrimary,
                    Color(0xFF6D28D9)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            )
        }
    }
}