package com.simats.selfora.ui.caregiver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.local.SessionManager
import com.simats.selfora.data.model.CaregiverPracticeSessionResponse
import com.simats.selfora.data.model.HomeProgramResponse
import com.simats.selfora.data.model.LinkedChildInfo
import com.simats.selfora.data.repository.NotificationRepository
import com.simats.selfora.data.repository.PracticeRecordEntry
import com.simats.selfora.data.repository.PracticeSessionStore
import com.simats.selfora.ui.components.dialogs.PracticeSessionIntroDialog
import com.simats.selfora.ui.components.glass.GlassIconButton
import com.simats.selfora.ui.components.glass.GlassTopBar
import com.simats.selfora.ui.therapist.SelfidoCatalogue
import com.simats.selfora.utils.LanguageManager
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.Locale

// Caregiver Dashboard Premium Healthcare Palette
private val CaregiverPrimary = Color(0xFF7456C8)
private val CaregiverPrimaryDark = Color(0xFF5B3FB0)
private val CaregiverSecondary = Color(0xFFEEE8FA)
private val CaregiverBg = Color(0xFFF8F7FC)
private val CaregiverCardBg = Color(0xFFFFFFFF)
private val CaregiverCardBorder = Color(0xFFE9E4F5)
private val CaregiverTextPrimary = Color(0xFF252238)
private val CaregiverTextSecondary = Color(0xFF89849A)
private val CaregiverSuccess = Color(0xFF43A779)
private val CaregiverSuccessBg = Color(0xFFECFDF5)
private val CaregiverAmber = Color(0xFFD97706)
private val CaregiverAmberBg = Color(0xFFFFFBEB)
private val CaregiverAmberBorder = Color(0xFFF59E0B)
private val CaregiverInfoBlue = Color(0xFF0284C7)

private enum class ProgramFilter { ALL, INCOMPLETE, COMPLETED }

private data class ProgramCompletionStats(
    val isCompletedToday: Boolean,
    val completedDaysThisWeek: Int,
    val totalCompletions: Int
)

private fun parsePracticeDateOrNull(raw: String?): LocalDate? {
    if (raw.isNullOrBlank() || raw.length < 10) return null
    return try {
        LocalDate.parse(raw.take(10))
    } catch (_: Exception) {
        null
    }
}

private fun formatRoutineLabel(raw: String?): String {
    if (raw.isNullOrBlank()) return "Daily"
    val clean = raw.trim().replace('_', ' ').lowercase(Locale.getDefault())
    return clean.split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
        .ifBlank { "Daily" }
}

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
    val localRecords by PracticeSessionStore.records.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(ProgramFilter.ALL) }

    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isTamil = currentLanguage == "ta"

    var showPracticeIntro by remember { mutableStateOf(false) }
    var pendingPracticeProgId by remember { mutableStateOf<Long?>(null) }
    var pendingActivityId by remember { mutableStateOf<String?>(null) }
    var pendingProgramTitle by remember { mutableStateOf("ADL Practice Session") }

    val scope = rememberCoroutineScope()

    fun loadPracticeHistoryForChild(childId: Long) {
        scope.launch {
            try {
                val histRes = ApiClient.apiService.getPracticeHistory(childId)
                if (histRes.isSuccessful && histRes.body() != null) {
                    recentPracticeHistory = histRes.body()!!
                }
            } catch (_: Exception) {
            }
        }
    }

    fun loadDashboardData() {
        isLoading = true
        scope.launch {
            try {
                val response = ApiClient.apiService.getCaregiverDashboardSummary()
                if (response.isSuccessful && response.body() != null) {
                    val summary = response.body()!!
                    linkedChildren = summary.linkedChildren
                    recentPracticeHistory = summary.recentPracticeHistory
                    if (summary.todayHomePrograms.isNotEmpty()) {
                        homePrograms = summary.todayHomePrograms
                    }
                    if (linkedChildren.isNotEmpty()) {
                        val currentActiveId = SessionManager.getActiveChildId()
                        val matchedChild = linkedChildren.find { it.childId == currentActiveId } ?: linkedChildren.first()
                        selectedChild = matchedChild
                        SessionManager.saveActiveChildContext(matchedChild.childId, matchedChild.name, matchedChild.gender)
                        // Ensure full practice history is loaded for the selected child
                        try {
                            val histRes = ApiClient.apiService.getPracticeHistory(matchedChild.childId)
                            if (histRes.isSuccessful && histRes.body() != null) {
                                recentPracticeHistory = histRes.body()!!
                            }
                        } catch (_: Exception) {
                        }
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

    // Active child filtered data
    val activeChildId = selectedChild?.childId
    val childPrograms = remember(homePrograms, activeChildId) {
        if (activeChildId != null) {
            homePrograms.filter { it.childId == activeChildId }.ifEmpty { homePrograms }
        } else {
            homePrograms
        }
    }
    val childHistory = remember(recentPracticeHistory, activeChildId) {
        if (activeChildId != null) {
            recentPracticeHistory.filter { it.childId == activeChildId }
        } else {
            recentPracticeHistory
        }
    }
    val childLocalRecords = remember(localRecords, activeChildId) {
        if (activeChildId != null) {
            localRecords.filter { it.childId == activeChildId }
        } else {
            localRecords
        }
    }

    val today = remember { LocalDate.now() }
    val monday = remember(today) { today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    val sunday = remember(monday) { monday.plusDays(6) }

    // Deduplicated per-program completion statistics
    val programStatsMap = remember(childPrograms, childHistory, childLocalRecords, today, monday, sunday) {
        childPrograms.associate { prog ->
            val backendForProg = childHistory.filter { it.homeProgramId == prog.id }
            val backendDates = backendForProg.mapNotNull { parsePracticeDateOrNull(it.practiceDate) }.toSet()

            val localForProg = childLocalRecords.filter { it.programId == prog.id }
            val localOnlyCount = localForProg.count { r ->
                val dt = parsePracticeDateOrNull(r.practiceDate)
                dt != null && dt !in backendDates
            }
            val localDates = localForProg.mapNotNull { parsePracticeDateOrNull(it.practiceDate) }.toSet()

            val allDates = backendDates + localDates
            val isCompletedToday = today in allDates
            val completedDaysThisWeek = allDates.count { !it.isBefore(monday) && !it.isAfter(sunday) }
            val totalCompletions = backendForProg.size + localOnlyCount

            prog.id to ProgramCompletionStats(
                isCompletedToday = isCompletedToday,
                completedDaysThisWeek = completedDaysThisWeek,
                totalCompletions = totalCompletions
            )
        }
    }

    val totalAssignedCount = childPrograms.size
    val completedTodayCount = remember(childPrograms, programStatsMap) {
        childPrograms.count { programStatsMap[it.id]?.isCompletedToday == true }
    }
    val pendingTodayCount = (totalAssignedCount - completedTodayCount).coerceAtLeast(0)

    val filteredPrograms = remember(childPrograms, programStatsMap, selectedFilter) {
        when (selectedFilter) {
            ProgramFilter.ALL -> childPrograms
            ProgramFilter.INCOMPLETE -> childPrograms.filter { programStatsMap[it.id]?.isCompletedToday != true }
            ProgramFilter.COMPLETED -> childPrograms.filter { programStatsMap[it.id]?.isCompletedToday == true }
        }
    }

    val progChildGender = selectedChild?.gender ?: SessionManager.getActiveChildGender()
    val isGirlChild = progChildGender.uppercase().startsWith("G") || progChildGender.uppercase().startsWith("F")

    Scaffold(
        containerColor = CaregiverBg,
        topBar = {
            GlassTopBar(
                title = if (isTamil) "பராமரிப்பாளர் முகப்பு" else "Caregiver Home",
                subtitle = if (isTamil) "வீட்டுப் பயிற்சி & வழிகாட்டுதல்" else "Home Practice & Guidance",
                accentColor = CaregiverPrimary,
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassIconButton(
                            icon = Icons.Default.Refresh,
                            onClick = { loadDashboardData() },
                            tint = CaregiverPrimary,
                            size = 36.dp,
                            iconSize = 18.dp
                        )
                        if (unreadNotificationCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = Color(0xFFEF4444),
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            "$unreadNotificationCount",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            ) {
                                GlassIconButton(
                                    icon = Icons.Default.Notifications,
                                    onClick = onNavigateToNotifications,
                                    tint = CaregiverPrimary,
                                    size = 36.dp,
                                    iconSize = 18.dp
                                )
                            }
                        } else {
                            GlassIconButton(
                                icon = Icons.Default.Notifications,
                                onClick = onNavigateToNotifications,
                                tint = CaregiverPrimary,
                                size = 36.dp,
                                iconSize = 18.dp
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(CaregiverBg)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isLoading && homePrograms.isNotEmpty()) {
                    item {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = CaregiverPrimary,
                            trackColor = CaregiverSecondary
                        )
                    }
                }

                // 1. Greeting Hero Banner
                item {
                    val caregiverName = SessionManager.getUsername().ifBlank { "Caregiver" }
                    val subtitleText = when {
                        totalAssignedCount == 0 -> "Ready for therapist-guided home routines."
                        pendingTodayCount == 0 -> "Amazing work! All today's tasks are complete."
                        completedTodayCount > 0 -> "Great start! $pendingTodayCount more ${if (pendingTodayCount == 1) "task" else "tasks"} left today."
                        else -> "Let's build daily independence step by step."
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = CaregiverCardBg,
                        border = BorderStroke(1.dp, CaregiverCardBorder),
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Hello, $caregiverName!",
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CaregiverTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = subtitleText,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = CaregiverTextSecondary,
                                        lineHeight = 17.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Surface(
                                    shape = CircleShape,
                                    color = CaregiverSecondary,
                                    border = BorderStroke(1.dp, CaregiverPrimary.copy(alpha = 0.22f)),
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (isGirlChild) "👩‍👧" else "👩‍👦",
                                            fontSize = 26.sp
                                        )
                                    }
                                }
                            }

                            if (selectedChild != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = CaregiverCardBorder.copy(alpha = 0.8f))
                                Spacer(modifier = Modifier.height(10.dp))

                                Box {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(CaregiverBg)
                                            .clickable(enabled = linkedChildren.size > 1) {
                                                childDropdownExpanded = true
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Face,
                                            contentDescription = null,
                                            tint = CaregiverPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = selectedChild!!.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CaregiverTextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Therapist: ${selectedChild!!.therapistName}",
                                                fontSize = 11.sp,
                                                color = CaregiverTextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        if (linkedChildren.size > 1) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Switch Child",
                                                tint = CaregiverPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = childDropdownExpanded,
                                        onDismissRequest = { childDropdownExpanded = false }
                                    ) {
                                        linkedChildren.forEach { child ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = child.name,
                                                        fontWeight = if (child.childId == selectedChild?.childId) FontWeight.Bold else FontWeight.Normal,
                                                        color = CaregiverTextPrimary
                                                    )
                                                },
                                                onClick = {
                                                    selectedChild = child
                                                    SessionManager.saveActiveChildContext(child.childId, child.name, child.gender)
                                                    childDropdownExpanded = false
                                                    loadPracticeHistoryForChild(child.childId)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Accurate Today's Progress & Weekly Consistency Card
                item {
                    WeeklyPracticeProgressCard(
                        recentHistory = childHistory,
                        localRecords = childLocalRecords,
                        homePrograms = childPrograms
                    )
                }

                // 3. Assigned Home Programs Section
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Assigned Home Programs",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = CaregiverTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Therapist-prescribed daily ADL routines",
                                    fontSize = 12.sp,
                                    color = CaregiverTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (totalAssignedCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                val allDone = pendingTodayCount == 0
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (allDone) CaregiverSuccessBg else CaregiverSecondary,
                                    border = BorderStroke(
                                        1.dp,
                                        if (allDone) CaregiverSuccess.copy(alpha = 0.35f) else CaregiverPrimary.copy(alpha = 0.28f)
                                    ),
                                    modifier = Modifier.width(IntrinsicSize.Max)
                                ) {
                                    Text(
                                        text = "$completedTodayCount/$totalAssignedCount Done",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (allDone) CaregiverSuccess else CaregiverPrimary,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        // Filter Tabs for All / Incomplete / Completed
                        if (totalAssignedCount > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ProgramFilterChip(
                                    label = "All ($totalAssignedCount)",
                                    selected = selectedFilter == ProgramFilter.ALL,
                                    activeColor = CaregiverPrimary,
                                    onClick = { selectedFilter = ProgramFilter.ALL },
                                    modifier = Modifier.weight(1f)
                                )
                                ProgramFilterChip(
                                    label = "Incomplete ($pendingTodayCount)",
                                    selected = selectedFilter == ProgramFilter.INCOMPLETE,
                                    activeColor = CaregiverAmber,
                                    onClick = { selectedFilter = ProgramFilter.INCOMPLETE },
                                    modifier = Modifier.weight(1f)
                                )
                                ProgramFilterChip(
                                    label = "Completed ($completedTodayCount)",
                                    selected = selectedFilter == ProgramFilter.COMPLETED,
                                    activeColor = CaregiverSuccess,
                                    onClick = { selectedFilter = ProgramFilter.COMPLETED },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isLoading && childPrograms.isEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = CaregiverCardBg,
                                border = BorderStroke(1.dp, CaregiverCardBorder)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        color = CaregiverPrimary,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Loading assigned home programs...",
                                        fontSize = 13.sp,
                                        color = CaregiverTextSecondary
                                    )
                                }
                            }
                        } else if (childPrograms.isEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = CaregiverCardBg,
                                border = BorderStroke(1.dp, CaregiverCardBorder)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = CaregiverSecondary,
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("✨", fontSize = 26.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No Assigned Routines Yet",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.5.sp,
                                        color = CaregiverTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Your occupational therapist will assign tailored ADL routines here. As soon as routines are prescribed, they will appear with full instructions.",
                                        fontSize = 12.5.sp,
                                        color = CaregiverTextSecondary,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        } else if (filteredPrograms.isEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = CaregiverCardBg,
                                border = BorderStroke(1.dp, CaregiverCardBorder)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = if (selectedFilter == ProgramFilter.INCOMPLETE) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = if (selectedFilter == ProgramFilter.INCOMPLETE) CaregiverSuccess else CaregiverAmber,
                                        modifier = Modifier.size(30.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (selectedFilter == ProgramFilter.INCOMPLETE)
                                            "All assigned tasks are completed today!"
                                        else
                                            "No tasks completed yet today",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = CaregiverTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (selectedFilter == ProgramFilter.INCOMPLETE)
                                            "Switch to Completed or All to review or practice again."
                                        else
                                            "Switch to Incomplete or All to start your first routine today.",
                                        fontSize = 12.sp,
                                        color = CaregiverTextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                filteredPrograms.forEach { prog ->
                                    val progGender = prog.childGender ?: selectedChild?.gender ?: SessionManager.getActiveChildGender()
                                    val isGirl = progGender.uppercase().startsWith("G") || progGender.uppercase().startsWith("F")
                                    val actDef = SelfidoCatalogue.getActivityById(prog.activityId)
                                    val actEmoji = actDef?.icon ?: if (isGirl) "👗" else "👕"

                                    val stats = programStatsMap[prog.id] ?: ProgramCompletionStats(
                                        isCompletedToday = false,
                                        completedDaysThisWeek = 0,
                                        totalCompletions = 0
                                    )

                                    AssignedPracticeCard(
                                        program = prog,
                                        actEmoji = actEmoji,
                                        isCompletedToday = stats.isCompletedToday,
                                        completedDaysThisWeek = stats.completedDaysThisWeek,
                                        completionsCount = stats.totalCompletions,
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

@Composable
private fun ProgramFilterChip(
    label: String,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) activeColor else CaregiverCardBg,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) activeColor else CaregiverCardBorder
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 6.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (selected) Color.White else CaregiverTextSecondary,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Accurate Today's Assigned Task Tracker & 7-Day Weekly Consistency Card.
 * Tracks distinct assigned home programs completed per day so partial completion
 * (e.g., 1 of 4 assigned tasks) is never mistakenly reported as "Done Today".
 */
@Composable
fun WeeklyPracticeProgressCard(
    recentHistory: List<CaregiverPracticeSessionResponse>,
    localRecords: List<PracticeRecordEntry>,
    homePrograms: List<HomeProgramResponse>
) {
    val today = remember { LocalDate.now() }
    val monday = remember(today) { today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    val sunday = remember(monday) { monday.plusDays(6) }

    val assignedProgramIds = remember(homePrograms) { homePrograms.map { it.id }.toSet() }
    val totalAssignedCount = homePrograms.size

    // Map each date in current week -> Set of distinct completed program IDs
    val completedProgramIdsByDate = remember(recentHistory, localRecords, assignedProgramIds, monday, sunday) {
        val map = mutableMapOf<LocalDate, MutableSet<Long>>()
        recentHistory.forEach { s ->
            val dt = parsePracticeDateOrNull(s.practiceDate)
            if (dt != null && !dt.isBefore(monday) && !dt.isAfter(sunday)) {
                if (assignedProgramIds.isEmpty() || s.homeProgramId in assignedProgramIds) {
                    map.getOrPut(dt) { mutableSetOf() }.add(s.homeProgramId)
                }
            }
        }
        localRecords.forEach { r ->
            val dt = parsePracticeDateOrNull(r.practiceDate)
            if (dt != null && !dt.isBefore(monday) && !dt.isAfter(sunday)) {
                if (assignedProgramIds.isEmpty() || r.programId in assignedProgramIds) {
                    map.getOrPut(dt) { mutableSetOf() }.add(r.programId)
                }
            }
        }
        map
    }

    val completedTodayCount = (completedProgramIdsByDate[today] ?: emptySet()).size
    val pendingTodayCount = (totalAssignedCount - completedTodayCount).coerceAtLeast(0)
    val isAllDoneToday = totalAssignedCount > 0 && completedTodayCount >= totalAssignedCount
    val isPartiallyDoneToday = totalAssignedCount > 0 && completedTodayCount in 1 until totalAssignedCount

    val weeklyTargetDays = if (homePrograms.isNotEmpty()) {
        homePrograms.maxOfOrNull { it.frequencyPerWeek }?.coerceIn(1, 7) ?: 5
    } else {
        5
    }

    // Count days this week where ALL assigned tasks were completed vs any task completed
    val fullDaysThisWeek = remember(completedProgramIdsByDate, totalAssignedCount) {
        if (totalAssignedCount == 0) 0
        else completedProgramIdsByDate.values.count { it.size >= totalAssignedCount }
    }
    val activeDaysThisWeek = completedProgramIdsByDate.keys.size

    // Today's task completion ratio (if tasks are assigned), otherwise weekly active days ratio
    val todayProgress = if (totalAssignedCount > 0) {
        (completedTodayCount.toFloat() / totalAssignedCount.toFloat()).coerceIn(0f, 1f)
    } else {
        (activeDaysThisWeek.toFloat() / weeklyTargetDays.toFloat()).coerceIn(0f, 1f)
    }

    val statusPillBg = when {
        totalAssignedCount == 0 -> Color(0xFFF3F4F6)
        isAllDoneToday -> CaregiverSuccessBg
        else -> CaregiverAmberBg
    }
    val statusPillBorder = when {
        totalAssignedCount == 0 -> Color(0xFFD1D5DB)
        isAllDoneToday -> CaregiverSuccess.copy(alpha = 0.4f)
        else -> CaregiverAmberBorder.copy(alpha = 0.45f)
    }
    val statusPillTextColor = when {
        totalAssignedCount == 0 -> CaregiverTextSecondary
        isAllDoneToday -> CaregiverSuccess
        else -> CaregiverAmber
    }
    val statusPillText = when {
        totalAssignedCount == 0 -> "No Tasks"
        isAllDoneToday -> "All Done Today"
        isPartiallyDoneToday -> "$completedTodayCount/$totalAssignedCount Done"
        else -> "Pending Today"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CaregiverCardBg,
        border = BorderStroke(1.dp, CaregiverCardBorder),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CaregiverSecondary,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = CaregiverPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Weekly Consistency",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        color = CaregiverTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (totalAssignedCount > 0) {
                            "$fullDaysThisWeek full ${if (fullDaysThisWeek == 1) "day" else "days"} • $activeDaysThisWeek/$weeklyTargetDays active days"
                        } else {
                            "$activeDaysThisWeek of $weeklyTargetDays practice days met"
                        },
                        fontSize = 11.5.sp,
                        color = CaregiverTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusPillBg,
                    border = BorderStroke(1.dp, statusPillBorder),
                    modifier = Modifier.width(IntrinsicSize.Max)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                isAllDoneToday -> Icons.Default.CheckCircle
                                totalAssignedCount == 0 -> Icons.Default.Info
                                else -> Icons.Default.Schedule
                            },
                            contentDescription = null,
                            tint = statusPillTextColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = statusPillText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusPillTextColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // Today's Assigned Tasks Progress Box
            if (totalAssignedCount > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CaregiverBg,
                    border = BorderStroke(1.dp, CaregiverCardBorder.copy(alpha = 0.8f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today's Tasks",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CaregiverTextPrimary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                            Text(
                                text = "$completedTodayCount of $totalAssignedCount completed (${(todayProgress * 100).toInt()}%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAllDoneToday) CaregiverSuccess else CaregiverPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { todayProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (isAllDoneToday) CaregiverSuccess else CaregiverPrimary,
                            trackColor = CaregiverSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7-Day Consistency Capsules (Mon to Sun)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                for (i in 0..6) {
                    val dayDate = monday.plusDays(i.toLong())
                    val dayDoneCount = (completedProgramIdsByDate[dayDate] ?: emptySet()).size
                    val isFullDay = if (totalAssignedCount > 0) {
                        dayDoneCount >= totalAssignedCount
                    } else {
                        dayDoneCount > 0
                    }
                    val isPartialDay = totalAssignedCount > 0 && dayDoneCount in 1 until totalAssignedCount
                    val isCurrentDay = dayDate == today

                    val capsuleBg = when {
                        isFullDay -> CaregiverSuccess
                        isPartialDay -> CaregiverAmberBg
                        isCurrentDay -> CaregiverSecondary
                        else -> CaregiverBg
                    }
                    val capsuleBorder = when {
                        isFullDay -> null
                        isPartialDay -> BorderStroke(1.5.dp, CaregiverAmberBorder)
                        isCurrentDay -> BorderStroke(1.5.dp, CaregiverPrimary)
                        else -> BorderStroke(1.dp, CaregiverCardBorder)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(11.dp),
                            color = capsuleBg,
                            border = capsuleBorder,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                when {
                                    isFullDay -> {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "All Tasks Completed",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    isPartialDay -> {
                                        Text(
                                            text = "$dayDoneCount/$totalAssignedCount",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = CaregiverAmber,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = "${dayDate.dayOfMonth}",
                                            fontSize = 12.sp,
                                            fontWeight = if (isCurrentDay) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (isCurrentDay) CaregiverPrimary else CaregiverTextSecondary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = dayLabels[i],
                            fontSize = 10.5.sp,
                            fontWeight = if (isCurrentDay) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrentDay) CaregiverPrimary else CaregiverTextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }

            // Compact Legend
            if (totalAssignedCount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDot(color = CaregiverSuccess, label = "All Done")
                    Spacer(modifier = Modifier.width(14.dp))
                    LegendDot(color = CaregiverAmberBorder, label = "Partial")
                    Spacer(modifier = Modifier.width(14.dp))
                    LegendDot(color = CaregiverCardBorder, label = "Pending")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Contextual Status Message
            val bannerBg = when {
                isAllDoneToday -> CaregiverSuccessBg
                totalAssignedCount == 0 -> CaregiverSecondary.copy(alpha = 0.6f)
                else -> CaregiverAmberBg
            }
            val bannerAccent = when {
                isAllDoneToday -> CaregiverSuccess
                totalAssignedCount == 0 -> CaregiverPrimary
                else -> CaregiverAmber
            }
            val bannerText = when {
                totalAssignedCount == 0 ->
                    "No home routines assigned yet. Your therapist will add daily practices here."
                isAllDoneToday ->
                    "Great job! All $totalAssignedCount assigned home routines are completed for today."
                isPartiallyDoneToday ->
                    "$completedTodayCount of $totalAssignedCount assigned tasks completed today. $pendingTodayCount more ${if (pendingTodayCount == 1) "task" else "tasks"} still pending!"
                else ->
                    "$totalAssignedCount assigned ${if (totalAssignedCount == 1) "routine is" else "routines are"} pending for today. Tap any task below to begin!"
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = bannerBg,
                border = BorderStroke(1.dp, bannerAccent.copy(alpha = 0.22f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            isAllDoneToday -> Icons.Default.CheckCircle
                            totalAssignedCount == 0 -> Icons.Default.Info
                            else -> Icons.Default.NotificationsActive
                        },
                        contentDescription = null,
                        tint = bannerAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = bannerText,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = CaregiverTextPrimary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 10.5.sp,
            color = CaregiverTextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Individual Assigned Home Program Card with explicit per-program completion status
 * (Completed vs Incomplete today), weekly progress bar, aligned metadata tiles,
 * therapist clinical advice, and status-aware CTA button.
 */
@Composable
fun AssignedPracticeCard(
    program: HomeProgramResponse,
    actEmoji: String,
    isCompletedToday: Boolean,
    completedDaysThisWeek: Int,
    completionsCount: Int,
    onStart: () -> Unit
) {
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

    val weeklyTarget = program.frequencyPerWeek.coerceAtLeast(1)
    val weeklyRatio = (completedDaysThisWeek.toFloat() / weeklyTarget.toFloat()).coerceIn(0f, 1f)

    val cardBorderColor = if (isCompletedToday) {
        CaregiverSuccess.copy(alpha = 0.45f)
    } else {
        CaregiverCardBorder
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CaregiverCardBg,
        border = BorderStroke(if (isCompletedToday) 1.2.dp else 1.dp, cardBorderColor),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Activity Icon, Title/Description, and Separate Completion Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.size(48.dp)) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCompletedToday) CaregiverSuccessBg else CaregiverSecondary,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(actEmoji, fontSize = 24.sp)
                        }
                    }
                    if (isCompletedToday) {
                        Surface(
                            shape = CircleShape,
                            color = CaregiverSuccess,
                            border = BorderStroke(1.5.dp, Color.White),
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.BottomEnd)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = program.title ?: program.activityTitle.ifBlank { "Home ADL Routine" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            color = CaregiverTextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Separate Per-Task Completion Status Pill
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (isCompletedToday) CaregiverSuccessBg else CaregiverAmberBg,
                            border = BorderStroke(
                                1.dp,
                                if (isCompletedToday) CaregiverSuccess.copy(alpha = 0.4f) else CaregiverAmberBorder.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier.width(IntrinsicSize.Max)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCompletedToday) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = if (isCompletedToday) CaregiverSuccess else CaregiverAmber,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isCompletedToday) "Completed" else "Incomplete",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCompletedToday) CaregiverSuccess else CaregiverAmber,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = program.description ?: "Therapist-prescribed daily living practice.",
                        fontSize = 12.sp,
                        color = CaregiverTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Per-Program Weekly Progress Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = CaregiverBg,
                border = BorderStroke(1.dp, CaregiverCardBorder.copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "This week: $completedDaysThisWeek / $weeklyTarget days",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CaregiverTextPrimary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$completionsCount total",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompletedToday) CaregiverSuccess else CaregiverTextSecondary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { weeklyRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isCompletedToday || completedDaysThisWeek >= weeklyTarget) CaregiverSuccess else CaregiverPrimary,
                        trackColor = CaregiverSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Clean Equal-Height 3-Column Parameter Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProgramMetricTile(
                    label = "FREQUENCY",
                    value = "${program.frequencyPerWeek}x / wk",
                    accentColor = CaregiverPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                ProgramMetricTile(
                    label = "ROUTINE",
                    value = formatRoutineLabel(program.preferredTime),
                    accentColor = CaregiverInfoBlue,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                ProgramMetricTile(
                    label = "DURATION",
                    value = "${program.targetDurationMinutes} mins",
                    accentColor = Color(0xFF7C3AED),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }

            // Target Prompt Level & Focus Steps
            if (program.targetPromptLevelName.isNotBlank() || focusStepsCount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (program.targetPromptLevelName.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrackChanges,
                                contentDescription = null,
                                tint = CaregiverPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Target: ${program.targetPromptLevelName}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CaregiverPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (focusStepsCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CaregiverSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.width(IntrinsicSize.Max)
                        ) {
                            Text(
                                text = "$focusStepsCount focus ${if (focusStepsCount == 1) "step" else "steps"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CaregiverTextPrimary,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Therapist Clinical Advice Note
            if (!program.caregiverInstructions.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CaregiverBg,
                    border = BorderStroke(1.dp, CaregiverCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = CaregiverPrimary,
                            modifier = Modifier
                                .padding(top = 1.dp)
                                .size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = program.caregiverInstructions,
                            fontSize = 12.sp,
                            color = CaregiverTextPrimary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action CTA Button (Clear visual distinction between Incomplete & Completed Today)
            if (isCompletedToday) {
                OutlinedButton(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, CaregiverSuccess.copy(alpha = 0.65f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CaregiverSuccessBg,
                        contentColor = CaregiverSuccess
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Completed Today • Practice Again",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(CaregiverPrimary, CaregiverPrimaryDark)
                            )
                        )
                        .clickable(onClick = onStart),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Start Practice Session",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgramMetricTile(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(11.dp),
        color = accentColor.copy(alpha = 0.07f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.18f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CaregiverTextPrimary,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
