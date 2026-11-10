package com.simats.selfora.ui.therapist

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.repository.NotificationRepository
import com.simats.selfora.data.model.ChildSummaryItem
import com.simats.selfora.data.model.HomeProgramResponse
import com.simats.selfora.data.model.PendingReviewItem
import com.simats.selfora.data.model.ScheduledSessionItem
import com.simats.selfora.data.model.SessionResponse
import com.simats.selfora.data.model.TherapistDashboardSummaryResponse
import com.simats.selfora.data.model.formatChatTime
import com.simats.selfora.ui.components.avatar.ChildAvatarImage
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.components.progress.SelforaLinearProgress
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapistDashboardScreen(
    onNavigateToAddChildWorkflow: () -> Unit,
    onNavigateToChildren: () -> Unit,
    onNavigateToChildProfile: (Long) -> Unit = {},
    onNavigateToSessionSummary: (Long) -> Unit = {},
    onNavigateToAssessment: () -> Unit,
    onNavigateToCreateHomeProgram: (Long) -> Unit = {},
    onNavigateToHomePrograms: () -> Unit,
    onNavigateToPendingReviews: (Long) -> Unit = { onNavigateToHomePrograms() },
    onNavigateToProgress: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onLogout: () -> Unit
) {
    var summary by remember { mutableStateOf(TherapistDashboardSummaryResponse()) }
    var assignedChildren by remember { mutableStateOf<List<ChildSummaryItem>>(emptyList()) }
    var todaySessionsDetails by remember { mutableStateOf<List<SessionResponse>>(emptyList()) }
    var assignedHomePrograms by remember { mutableStateOf<List<HomeProgramResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingTodaySessions by remember { mutableStateOf(false) }
    var isLoadingHomePrograms by remember { mutableStateOf(false) }
    var todaySessionsError by remember { mutableStateOf<String?>(null) }
    var homeProgramsError by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showTodaySessionsSheet by remember { mutableStateOf(false) }
    var showHomeProgramsSheet by remember { mutableStateOf(false) }
    var showChildPickerForProgram by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    suspend fun fetchTodaySessionsWithFallback(currentChildren: List<ChildSummaryItem>) {
        isLoadingTodaySessions = true
        todaySessionsError = null
        try {
            val res = ApiClient.apiService.getTherapistTodaySessions()
            if (res.isSuccessful && res.body() != null) {
                todaySessionsDetails = res.body()!!
                return
            }
            val childrenList = currentChildren.ifEmpty {
                val cr = ApiClient.apiService.getTherapistChildren()
                if (cr.isSuccessful && cr.body() != null) {
                    assignedChildren = cr.body()!!
                    cr.body()!!
                } else emptyList()
            }
            if (childrenList.isNotEmpty()) {
                val todayPrefix = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val aggregated = mutableListOf<SessionResponse>()
                for (child in childrenList) {
                    try {
                        val sRes = ApiClient.apiService.getSessionsForChild(child.id)
                        if (sRes.isSuccessful && sRes.body() != null) {
                            aggregated.addAll(
                                sRes.body()!!.filter { (it.startTime ?: "").startsWith(todayPrefix) }
                            )
                        }
                    } catch (_: Exception) {}
                }
                todaySessionsDetails = aggregated.distinctBy { it.sessionId }.sortedByDescending { it.startTime }
            } else {
                todaySessionsError = "Unable to load today's sessions (HTTP ${res.code()})"
            }
        } catch (e: Exception) {
            todaySessionsError = e.localizedMessage ?: "Failed to load today's sessions"
        } finally {
            isLoadingTodaySessions = false
        }
    }

    suspend fun fetchHomeProgramsWithFallback(currentChildren: List<ChildSummaryItem>) {
        isLoadingHomePrograms = true
        homeProgramsError = null
        try {
            val res = ApiClient.apiService.getTherapistHomePrograms()
            if (res.isSuccessful && res.body() != null) {
                assignedHomePrograms = res.body()!!
                return
            }
            val childrenList = currentChildren.ifEmpty {
                val cr = ApiClient.apiService.getTherapistChildren()
                if (cr.isSuccessful && cr.body() != null) {
                    assignedChildren = cr.body()!!
                    cr.body()!!
                } else emptyList()
            }
            if (childrenList.isNotEmpty()) {
                val aggregated = mutableListOf<HomeProgramResponse>()
                for (child in childrenList) {
                    try {
                        val hpRes = ApiClient.apiService.getAllHomeProgramsForChild(child.id)
                        if (hpRes.isSuccessful && hpRes.body() != null) {
                            aggregated.addAll(
                                hpRes.body()!!.filter { p ->
                                    val st = (p.status ?: "").uppercase(Locale.getDefault())
                                    p.isActive && st !in listOf("COMPLETED", "CANCELLED", "PAUSED")
                                }
                            )
                        }
                    } catch (_: Exception) {}
                }
                assignedHomePrograms = aggregated.distinctBy { it.id }.sortedByDescending { it.id }
            } else {
                homeProgramsError = "Unable to load home programs (HTTP ${res.code()})"
            }
        } catch (e: Exception) {
            homeProgramsError = e.localizedMessage ?: "Failed to load home programs"
        } finally {
            isLoadingHomePrograms = false
        }
    }

    fun refreshTodaySessionsDetail() {
        scope.launch { fetchTodaySessionsWithFallback(assignedChildren) }
    }

    fun refreshHomeProgramsDetail() {
        scope.launch { fetchHomeProgramsWithFallback(assignedChildren) }
    }

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
                var latestChildren = assignedChildren
                val childrenResponse = ApiClient.apiService.getTherapistChildren()
                if (childrenResponse.isSuccessful && childrenResponse.body() != null) {
                    latestChildren = childrenResponse.body()!!
                    assignedChildren = latestChildren
                }
                fetchTodaySessionsWithFallback(latestChildren)
                fetchHomeProgramsWithFallback(latestChildren)
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}. Tap Refresh to try again."
            } finally {
                isLoading = false
            }
        }
    }

    val unreadNotificationCount by NotificationRepository.unreadCount.collectAsState()

    var hasLaunchedInitialLoad by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        hasLaunchedInitialLoad = true
        loadDashboardData()
        NotificationRepository.fetchNotifications()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && hasLaunchedInitialLoad) {
                loadDashboardData()
                scope.launch { NotificationRepository.fetchNotifications() }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "${getTimeBasedGreeting()} 👋",
                subtitle = "${summary.therapistName.ifBlank { "Therapist" }} • ${summary.designation.ifBlank { "OT" }}",
                accentColor = SelforaPrimary,
                actions = {
                    GlassIconButton(
                        icon = Icons.Default.Refresh,
                        onClick = {
                            loadDashboardData()
                            scope.launch { NotificationRepository.fetchNotifications() }
                        },
                        tint = SelforaPrimary,
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
                    GlassIconButton(
                        icon = Icons.Default.Person,
                        onClick = { showProfileDialog = true },
                        tint = SelforaPrimary,
                        size = 36.dp,
                        iconSize = 18.dp
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
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = SelforaPrimary,
                                modifier = Modifier.size(28.dp)
                            )
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
                    subtitle = if (summary.todaySessionsCount > 0) "Conducted Today" else "Therapy Sessions",
                    emoji = "📅",
                    accentColor = SelforaSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        showTodaySessionsSheet = true
                        refreshTodaySessionsDetail()
                    }
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
                    onClick = {
                        showHomeProgramsSheet = true
                        refreshHomeProgramsDetail()
                    }
                )
                DashboardSummaryGlassCard(
                    title = "Pending Reviews",
                    count = "${summary.pendingReviewsCount}",
                    subtitle = "Caregiver Practice Logs",
                    emoji = "📋",
                    accentColor = SelforaWarning,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToPendingReviews(0L) }
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
                    onClick = {
                        showTodaySessionsSheet = true
                        refreshTodaySessionsDetail()
                    }
                )

                QuickActionChevronCard(
                    title = "Assign Home Program",
                    icon = Icons.Default.Assignment,
                    iconTint = SelforaSuccess,
                    onClick = {
                        when {
                            assignedChildren.isEmpty() -> onNavigateToAddChildWorkflow()
                            assignedChildren.size == 1 -> onNavigateToCreateHomeProgram(assignedChildren.first().id)
                            else -> showChildPickerForProgram = true
                        }
                    }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S SCHEDULE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SelforaPrimary,
                    letterSpacing = 1.sp
                )
                if (summary.todaySchedule.isNotEmpty() || todaySessionsDetails.isNotEmpty()) {
                    Text(
                        text = "View All (${summary.todaySessionsCount}) →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelforaPrimary,
                        modifier = Modifier.clickable {
                            showTodaySessionsSheet = true
                            refreshTodaySessionsDetail()
                        }
                    )
                }
            }
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
                            Text("No Sessions Conducted Today", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                            Text("Start a clinical session from My Children to see today's sessions here.", fontSize = 12.sp, color = SelforaTextSecondary)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    summary.todaySchedule.take(5).forEach { session ->
                        GlassScheduleCard(
                            session = session,
                            onSelectChild = {
                                if (session.status == "COMPLETED") {
                                    onNavigateToSessionSummary(session.sessionId)
                                } else {
                                    onNavigateToChildProfile(session.childId)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pending Home Practice Reviews Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PENDING CAREGIVER REVIEWS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SelforaPrimary,
                    letterSpacing = 1.sp
                )
                if (summary.pendingReviews.isNotEmpty() || summary.pendingReviewsCount > 0) {
                    Text(
                        text = "Review All (${summary.pendingReviewsCount}) →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelforaWarning,
                        modifier = Modifier.clickable { onNavigateToPendingReviews(0L) }
                    )
                }
            }
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
                    summary.pendingReviews.take(5).forEach { review ->
                        GlassPendingReviewCard(
                            review = review,
                            onReview = { onNavigateToPendingReviews(review.childId) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }

    if (showTodaySessionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTodaySessionsSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sessions Today (${todaySessionsDetails.size.coerceAtLeast(summary.todaySchedule.size)})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                        Text(
                            text = "Live clinical sessions recorded today",
                            fontSize = 12.sp,
                            color = SelforaTextSecondary
                        )
                    }
                    IconButton(onClick = { refreshTodaySessionsDetail() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = SelforaPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoadingTodaySessions && todaySessionsDetails.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SelforaPrimary)
                    }
                } else if (todaySessionsError != null && todaySessionsDetails.isEmpty() && summary.todaySchedule.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = SelforaBackground,
                        border = BorderStroke(1.dp, SelforaBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(34.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(todaySessionsError!!, fontSize = 13.sp, color = SelforaTextSecondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { refreshTodaySessionsDetail() },
                                colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Retry", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (todaySessionsDetails.isEmpty() && summary.todaySchedule.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = SelforaBackground,
                        border = BorderStroke(1.dp, SelforaBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.EventAvailable, contentDescription = null, tint = SelforaSecondary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Sessions Today", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SelforaTextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "You haven't started any therapy sessions today.",
                                fontSize = 12.sp,
                                color = SelforaTextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    showTodaySessionsSheet = false
                                    onNavigateToChildren()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Select Child to Start Session", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (todaySessionsDetails.isNotEmpty()) {
                            items(todaySessionsDetails, key = { it.sessionId }) { session ->
                                val isCompleted = session.endTime != null
                                Surface(
                                    onClick = {
                                        showTodaySessionsSheet = false
                                        if (isCompleted) onNavigateToSessionSummary(session.sessionId)
                                        else onNavigateToChildProfile(session.childId)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = SelforaBackground,
                                    border = BorderStroke(1.dp, SelforaBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = session.childName,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SelforaTextPrimary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${session.activityTitle} • ${session.environment}",
                                                fontSize = 12.sp,
                                                color = SelforaTextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val mins = (session.totalDurationSeconds / 60).coerceAtLeast(1)
                                            val timeLabel = formatChatTime(session.startTime).ifBlank { "Today" }
                                            val indLabel = session.independencePercentage?.let { " • ${it.toInt()}% Indep." } ?: ""
                                            Text(
                                                text = "🕒 $timeLabel • $mins min$indLabel",
                                                fontSize = 11.sp,
                                                color = SelforaPrimary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = (if (isCompleted) SelforaSuccess else SelforaPrimary).copy(alpha = 0.14f)
                                        ) {
                                            Text(
                                                text = if (isCompleted) "COMPLETED" else "IN PROGRESS",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCompleted) SelforaSuccess else SelforaPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            items(summary.todaySchedule, key = { it.sessionId }) { item ->
                                GlassScheduleCard(
                                    session = item,
                                    onSelectChild = {
                                        showTodaySessionsSheet = false
                                        if (item.status == "COMPLETED") onNavigateToSessionSummary(item.sessionId)
                                        else onNavigateToChildProfile(item.childId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showHomeProgramsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHomeProgramsSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Home Programs (${assignedHomePrograms.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                        Text(
                            text = "Active home programmes assigned to your children",
                            fontSize = 12.sp,
                            color = SelforaTextSecondary
                        )
                    }
                    IconButton(onClick = { refreshHomeProgramsDetail() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = SelforaPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoadingHomePrograms && assignedHomePrograms.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SelforaPrimary)
                    }
                } else if (homeProgramsError != null && assignedHomePrograms.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = SelforaBackground,
                        border = BorderStroke(1.dp, SelforaBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(34.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(homeProgramsError!!, fontSize = 13.sp, color = SelforaTextSecondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { refreshHomeProgramsDetail() },
                                colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Retry", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (assignedHomePrograms.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = SelforaBackground,
                        border = BorderStroke(1.dp, SelforaBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.HomeWork, contentDescription = null, tint = SelforaSuccess, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Active Home Programs", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SelforaTextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Assign a structured home ADL programme for your caregiver to practice at home.",
                                fontSize = 12.sp,
                                color = SelforaTextSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(assignedHomePrograms, key = { it.id }) { program ->
                            Surface(
                                onClick = {
                                    showHomeProgramsSheet = false
                                    onNavigateToChildProfile(program.childId)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = SelforaBackground,
                                border = BorderStroke(1.dp, SelforaBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val safeTitle = (program.title ?: "").ifBlank { program.activityTitle ?: "Home Programme" }
                                        val safeStatus = (program.status ?: "").ifBlank { "ACTIVE" }
                                        Text(
                                            text = safeTitle,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SelforaTextPrimary,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = SelforaSuccess.copy(alpha = 0.14f)
                                        ) {
                                            Text(
                                                text = safeStatus,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SelforaSuccess,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val safeChild = (program.childName ?: "").ifBlank { "Child #${program.childId}" }
                                    val safeAct = (program.activityTitle ?: "").ifBlank { "ADL Activity" }
                                    val safePrompt = (program.targetPromptLevelName ?: "").ifBlank { "Visual" }
                                    val safeStart = program.startDate ?: ""
                                    val safeEnd = program.endDate ?: ""
                                    Text(
                                        text = "Child: $safeChild • $safeAct",
                                        fontSize = 12.sp,
                                        color = SelforaTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${program.frequencyPerWeek}x/week • ${program.targetDurationMinutes}m • Target: $safePrompt",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SelforaPrimary
                                    )
                                    if (safeStart.isNotBlank() && safeEnd.isNotBlank()) {
                                        Text(
                                            text = "Period: $safeStart → $safeEnd",
                                            fontSize = 11.sp,
                                            color = SelforaTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        showHomeProgramsSheet = false
                        when {
                            assignedChildren.isEmpty() -> onNavigateToAddChildWorkflow()
                            assignedChildren.size == 1 -> onNavigateToCreateHomeProgram(assignedChildren.first().id)
                            else -> showChildPickerForProgram = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Assign New Home Program", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showChildPickerForProgram) {
        AlertDialog(
            onDismissRequest = { showChildPickerForProgram = false },
            containerColor = SelforaSurface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    "Select Child for Home Program",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = SelforaTextPrimary
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(assignedChildren, key = { it.id }) { child ->
                        Surface(
                            onClick = {
                                showChildPickerForProgram = false
                                onNavigateToCreateHomeProgram(child.id)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = SelforaBackground,
                            border = BorderStroke(1.dp, SelforaBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(child.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SelforaTextPrimary)
                                    Text("${child.age} yrs • Caregiver: ${child.caregiverName}", fontSize = 11.sp, color = SelforaTextSecondary)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SelforaPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChildPickerForProgram = false }) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
            }
        )
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
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = SelforaPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
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

