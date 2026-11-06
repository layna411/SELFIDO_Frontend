package com.simats.selfora.ui.therapist

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.local.SessionManager
import com.simats.selfora.data.model.ChildDto
import com.simats.selfora.data.model.ResetPasswordResultResponse
import com.simats.selfora.ui.components.avatar.ChildAvatarImage
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.components.progress.SelforaAdlCategoryGrid
import com.simats.selfora.ui.components.progress.SelforaLinearProgress
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildProfileScreen(
    childId: Long,
    onStartAssessment: (Long) -> Unit,
    onStartSession: (Long) -> Unit = onStartAssessment,
    onAssignProgramme: (Long) -> Unit,
    onViewProgress: (Long) -> Unit,
    onNavigateToAnalyzeAndAdapt: (Long) -> Unit = {},
    onNavigateToPromptFading: (Long) -> Unit = {},
    onMessageCaregiver: (Long) -> Unit,
    onOpenChildMode: (Long) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Overview") }
    var caregiverAccountActive by remember { mutableStateOf(true) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    var resetPasswordResult by remember { mutableStateOf<ResetPasswordResultResponse?>(null) }
    var isResettingPassword by remember { mutableStateOf(false) }
    var childDto by remember { mutableStateOf<ChildDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val tabs = listOf("Overview", "Analyze & Adapt", "Prompt Fading", "ADL Goals", "Assessments", "Sessions", "Home Programmes", "Progress")

    var assessmentsList by remember { mutableStateOf<List<com.simats.selfora.data.model.AssessmentResponse>>(emptyList()) }
    var sessionsList by remember { mutableStateOf<List<com.simats.selfora.data.model.SessionResponse>>(emptyList()) }
    var homeProgramsList by remember { mutableStateOf<List<com.simats.selfora.data.model.HomeProgramResponse>>(emptyList()) }
    var dashboardData by remember { mutableStateOf<com.simats.selfora.data.model.AnalyzeAndAdaptDashboardResponse?>(null) }
    var isTabDataLoading by remember { mutableStateOf(false) }

    fun loadTabData(tab: String) {
        scope.launch {
            isTabDataLoading = true
            try {
                when (tab) {
                    "Assessments" -> {
                        val res = ApiClient.apiService.getAssessmentsForChild(childId)
                        if (res.isSuccessful && res.body() != null) {
                            assessmentsList = res.body()!!
                        }
                    }
                    "Sessions" -> {
                        val res = ApiClient.apiService.getSessionsForChild(childId)
                        if (res.isSuccessful && res.body() != null) {
                            sessionsList = res.body()!!
                        }
                    }
                    "Home Programmes" -> {
                        val res = ApiClient.apiService.getAllHomeProgramsForChild(childId)
                        if (res.isSuccessful && res.body() != null) {
                            homeProgramsList = res.body()!!
                        }
                    }
                }
            } catch (_: Exception) {}
            finally {
                isTabDataLoading = false
            }
        }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab in listOf("Assessments", "Sessions", "Home Programmes")) {
            loadTabData(selectedTab)
        }
    }

    LaunchedEffect(childId) {
        isLoading = true
        errorMessage = null
        try {
            val response = ApiClient.apiService.getChildById(childId)
            if (response.isSuccessful && response.body() != null) {
                childDto = response.body()
                caregiverAccountActive = childDto?.caregiverActive ?: true
            } else {
                val code = response.code()
                errorMessage = if (code == 403) "Access Denied: You are not authorized to view this child record." else "Child profile unavailable (HTTP $code)"
            }
            // Fetch child profile related data in parallel
            launch {
                try {
                    val sRes = ApiClient.apiService.getSessionsForChild(childId)
                    if (sRes.isSuccessful && sRes.body() != null) {
                        sessionsList = sRes.body()!!
                    }
                } catch (_: Exception) {}
            }
            launch {
                try {
                    val aRes = ApiClient.apiService.getAssessmentsForChild(childId)
                    if (aRes.isSuccessful && aRes.body() != null) {
                        assessmentsList = aRes.body()!!
                    }
                } catch (_: Exception) {}
            }
            launch {
                try {
                    val hpRes = ApiClient.apiService.getAllHomeProgramsForChild(childId)
                    if (hpRes.isSuccessful && hpRes.body() != null) {
                        homeProgramsList = hpRes.body()!!
                    }
                } catch (_: Exception) {}
            }
            launch {
                try {
                    val dRes = ApiClient.apiService.getAnalyzeAndAdaptDashboard(childId)
                    if (dRes.isSuccessful && dRes.body() != null) {
                        dashboardData = dRes.body()
                    }
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    val childName = childDto?.let { "${it.firstName} ${it.lastName ?: ""}".trim() } ?: "Child Profile"
    val gender = (childDto?.gender ?: "BOY").uppercase()
    val age = childDto?.dateOfBirth?.let { dobStr ->
        try {
            val birthDate = java.time.LocalDate.parse(dobStr)
            java.time.Period.between(birthDate, java.time.LocalDate.now()).years
        } catch (_: Exception) { 7 }
    } ?: 7
    val caregiverName = childDto?.caregiverName ?: "Unassigned"
    val caregiverEmail = childDto?.caregiverEmail ?: "Not provided"
    val caregiverPhone = childDto?.caregiverPhone ?: "Not provided"
    val caregiverId = childDto?.caregiverId
    val therapistName = childDto?.therapistName ?: "Unassigned"

    var selectedAvatarId by remember { mutableStateOf(childDto?.avatarUrl ?: if (gender == "GIRL") "ananya_girl" else "aarav_boy") }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "$childName - Profile",
                subtitle = "Age $age • ${if (gender == "GIRL") "Female" else "Male"} • ASD",
                onBackClick = onBack,
                accentColor = SelforaPrimary,
                actions = {
                    IconButton(onClick = { Toast.makeText(context, "Edit Child Profile", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = SelforaPrimary)
                    }
                }
            )
        },
        containerColor = SelforaBackground
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SelforaPrimary)
            }
        } else if (errorMessage != null || childDto == null) {
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
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            "Access Restricted",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = SelforaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            errorMessage ?: "You do not have authorization to view this child profile.",
                            color = SelforaTextSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Back to Children List")
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
            // A. Profile Header Glass Card (Matching Prototype Screen 4)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChildAvatarImage(
                        avatarId = selectedAvatarId,
                        gender = gender,
                        size = 64.dp
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(childName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SelforaPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "CHILD-#${childDto?.id ?: childId}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Age $age • ${if (gender == "GIRL") "Female" else "Male"} • ASD", fontSize = 13.sp, color = SelforaTextSecondary, fontWeight = FontWeight.Medium)
                        Text("Doctor: $therapistName", fontSize = 11.sp, color = SelforaPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // B. Current Goals Card (Matching Prototype Screen 4)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Current Goals", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                    Text(
                        "View All",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SelforaPrimary,
                        modifier = Modifier.clickable { selectedTab = "ADL Goals" }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                val activeGoals = homeProgramsList.filter { it.isActive }.ifEmpty { homeProgramsList }.take(3)
                if (activeGoals.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        activeGoals.forEach { hp ->
                            val titleText = hp.goalStatement?.ifBlank { null } ?: hp.title ?: hp.activityTitle
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("• ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                                Text(titleText, fontSize = 13.sp, color = SelforaTextPrimary)
                            }
                        }
                    }
                } else {
                    Text("No active goals assigned yet.", fontSize = 13.sp, color = SelforaTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // C. Recent Sessions Card (Matching Prototype Screen 4)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Recent Sessions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                if (sessionsList.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        sessionsList.take(3).forEach { s ->
                            val dateStr = s.startTime?.take(10) ?: "Recent"
                            val promptStr = s.performanceRecords.firstOrNull()?.promptLevelName ?: "Clinic Session"
                            RecentSessionItem(
                                date = dateStr,
                                activityName = s.activityTitle,
                                promptLevel = promptStr
                            )
                        }
                    }
                } else {
                    Text("No therapy sessions recorded yet.", fontSize = 13.sp, color = SelforaTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // D. Primary "Start New Session" Action Button (Screen 4 Main CTA)
            GlassButton(
                text = "Start New Session",
                onClick = { onStartSession(childId) },
                icon = Icons.Default.PlayArrow,
                gradient = listOf(SelforaPrimary, Color(0xFF6D28D9)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Tabs Row
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOf(selectedTab).coerceAtLeast(0),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                tab,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == tab) SelforaPrimary else SelforaTextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                "Analyze & Adapt" -> {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("ANALYZE & ADAPT QUICK ACCESS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Text("Review performance trends, difficult steps, factual observations, and rule-based adaptation recommendations.", fontSize = 12.sp, color = SelforaTextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        GlassButton(
                            text = "Open Dedicated Analyze & Adapt Dashboard →",
                            onClick = { onNavigateToAnalyzeAndAdapt(childId) },
                            icon = Icons.Default.Analytics,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                "Prompt Fading" -> {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("PROMPT FADING MANAGEMENT QUICK ACCESS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Text("Inspect assistance hierarchy order, step-wise prompt history, fading plans, and auditable history.", fontSize = 12.sp, color = SelforaTextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        GlassButton(
                            text = "Open Dedicated Prompt Fading Hub →",
                            onClick = { onNavigateToPromptFading(childId) },
                            icon = Icons.Default.TrendingDown,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 1. Overview Tab
                "Overview" -> {
                    // Clinical Overview Summary Card
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("CLINICAL OVERVIEW", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        val initialAssessmentDate = assessmentsList.minByOrNull { it.assessmentDate }?.assessmentDate?.take(10) ?: "Not Assessed"
                        val recentSessionDate = sessionsList.maxByOrNull { it.startTime }?.startTime?.take(10) ?: "None"
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Initial Assessment:", fontSize = 11.sp, color = SelforaTextSecondary)
                                Text(initialAssessmentDate, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                            }
                            Column {
                                Text("Total Therapy Sessions:", fontSize = 11.sp, color = SelforaTextSecondary)
                                Text("${sessionsList.size} Recorded", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                            }
                            Column {
                                Text("Recent Session:", fontSize = 11.sp, color = SelforaTextSecondary)
                                Text(recentSessionDate, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ADL Performance Summary Card (All 4 Clinical Categories)
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("ADL PERFORMANCE METRICS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        val dressingCard = dashboardData?.activityCards?.find { it.categoryName.contains("Dressing", ignoreCase = true) }
                        val eatingCard = dashboardData?.activityCards?.find { it.categoryName.contains("Eating", ignoreCase = true) }
                        val groomingCard = dashboardData?.activityCards?.find { it.categoryName.contains("Grooming", ignoreCase = true) }
                        val shoesCard = dashboardData?.activityCards?.find { it.categoryName.contains("Shoes", ignoreCase = true) }

                        SelforaAdlCategoryGrid(
                            dressingPct = dressingCard?.currentIndependencePct?.toInt() ?: 0,
                            eatingPct = eatingCard?.currentIndependencePct?.toInt() ?: 0,
                            groomingPct = groomingCard?.currentIndependencePct?.toInt() ?: 0,
                            shoesPct = shoesCard?.currentIndependencePct?.toInt() ?: 0
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Caregiver Management Module Card
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("LINKED CAREGIVER MANAGEMENT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (caregiverAccountActive) SelforaSuccess.copy(alpha = 0.15f) else SelforaError.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (caregiverAccountActive) "Active Account" else "Disabled",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (caregiverAccountActive) SelforaSuccess else SelforaError,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = SelforaSecondary.copy(alpha = 0.12f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.FamilyRestroom,
                                        contentDescription = null,
                                        tint = SelforaSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(caregiverName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                Text("Parent • $caregiverEmail", fontSize = 12.sp, color = SelforaTextSecondary)
                                Text("Phone: $caregiverPhone", fontSize = 12.sp, color = SelforaTextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Caregiver Actions
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlassOutlinedButton(
                                text = "Reset Password",
                                icon = Icons.Default.LockReset,
                                onClick = {
                                    if (caregiverId != null) {
                                        isResettingPassword = true
                                        scope.launch {
                                            try {
                                                val resp = ApiClient.apiService.resetCaregiverPassword(caregiverId)
                                                if (resp.isSuccessful && resp.body() != null) {
                                                    resetPasswordResult = resp.body()!!
                                                    showResetPasswordDialog = true
                                                } else {
                                                    Toast.makeText(context, "Password reset failed (HTTP ${resp.code()})", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                            } finally {
                                                isResettingPassword = false
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "No linked caregiver account to reset.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )

                            GlassOutlinedButton(
                                text = if (caregiverAccountActive) "Disable Account" else "Enable Account",
                                icon = if (caregiverAccountActive) Icons.Default.Block else Icons.Default.CheckCircle,
                                accentColor = if (caregiverAccountActive) SelforaError else SelforaSuccess,
                                onClick = {
                                    if (caregiverId != null) {
                                        scope.launch {
                                            try {
                                                val newStatus = !caregiverAccountActive
                                                val resp = ApiClient.apiService.toggleCaregiverStatus(caregiverId, newStatus)
                                                if (resp.isSuccessful) {
                                                    caregiverAccountActive = newStatus
                                                    Toast.makeText(context, if (caregiverAccountActive) "Caregiver account activated" else "Caregiver account disabled", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Failed to update account status", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "No linked caregiver account to manage.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 2. ADL Goals Tab
                "ADL Goals" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        if (homeProgramsList.isEmpty()) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🎯", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("No Active ADL Goals", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SelforaTextPrimary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Assign a Home Programme or conduct an Assessment to establish ADL goals.", fontSize = 12.sp, color = SelforaTextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            }
                        } else {
                            homeProgramsList.forEach { hp ->
                                val targetStepsStr = if (hp.targetSteps.isNotEmpty()) {
                                    hp.targetSteps.joinToString(" → ") { it.title }
                                } else {
                                    hp.goalStatement?.ifBlank { null } ?: "ADL Task Steps"
                                }
                                val targetAssistance = hp.targetPromptLevelName.ifBlank { "Independent" }
                                val assignedDate = hp.startDate?.take(10) ?: hp.createdAt.take(10).ifBlank { "Recent" }
                                GlassGoalCategoryCard(
                                    category = hp.activityTitle,
                                    activityName = hp.title ?: hp.activityTitle,
                                    targetSteps = targetStepsStr,
                                    currentAssistance = targetAssistance,
                                    progressPercentage = if (hp.isActive) 100 else 50,
                                    lastUpdated = assignedDate,
                                    accentColor = SelforaPrimary
                                )
                            }
                        }
                    }
                }

                // 3. Assessments Tab
                "Assessments" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        if (isTabDataLoading) {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = SelforaPrimary, modifier = Modifier.size(28.dp))
                            }
                        } else if (assessmentsList.isEmpty()) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("📋", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("No Clinical Assessments Yet", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SelforaTextPrimary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Conduct a baseline assessment to evaluate this child's initial prompt levels.", fontSize = 12.sp, color = SelforaTextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            }
                        } else {
                            assessmentsList.forEach { a ->
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(a.activityTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                        Surface(shape = RoundedCornerShape(10.dp), color = SelforaSuccess.copy(alpha = 0.15f)) {
                                            Text("Completed", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SelforaSuccess, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Date: ${a.assessmentDate.take(10)} • Evaluator: ${a.therapistName}", fontSize = 12.sp, color = SelforaTextSecondary)
                                    if (!a.notes.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Clinical Notes: ${a.notes}", fontSize = 12.sp, color = SelforaTextPrimary)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val independentSteps = a.stepResults.count { it.outcome == "COMPLETED" && it.promptLevelId == 0 }
                                    Text("• Assessed Steps: ${a.stepResults.size} ($independentSteps Independent)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SelforaPrimary)
                                }
                            }
                        }

                        GlassButton(
                            text = "Start New Assessment",
                            onClick = { onStartAssessment(childId) },
                            icon = Icons.Default.PlayArrow,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 4. Sessions Tab
                "Sessions" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (isTabDataLoading) {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = SelforaPrimary, modifier = Modifier.size(28.dp))
                            }
                        } else if (sessionsList.isEmpty()) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🎯", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("No Therapy Sessions Recorded", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SelforaTextPrimary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Sessions conducted with this child will appear here.", fontSize = 12.sp, color = SelforaTextSecondary)
                                }
                            }
                        } else {
                            sessionsList.forEach { s ->
                                val dateStr = s.startTime?.take(10) ?: "Recent"
                                val durationStr = s.totalDurationSeconds?.let { "${it / 60} mins" } ?: "15 mins"
                                val totalRecords = s.performanceRecords.size
                                val indepCount = s.performanceRecords.count { it.promptLevelId == 0 && it.outcome == "COMPLETED" }
                                val indepPct = if (totalRecords > 0) (indepCount * 100) / totalRecords else 0
                                GlassSessionHistoryCard(
                                    date = dateStr,
                                    activityName = s.activityTitle,
                                    duration = durationStr,
                                    status = if (s.endTime != null) "COMPLETED" else "IN PROGRESS",
                                    independencePercentage = indepPct
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        GlassButton(
                            text = "Start New Session",
                            onClick = { onStartSession(childId) },
                            icon = Icons.Default.PlayArrow,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 5. Home Programmes Tab
                "Home Programmes" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        if (isTabDataLoading) {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = SelforaPrimary, modifier = Modifier.size(28.dp))
                            }
                        } else if (homeProgramsList.isEmpty()) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🏠", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("No Active Home Programmes", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SelforaTextPrimary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Assign a home programme to guide caregiver practice routines.", fontSize = 12.sp, color = SelforaTextSecondary)
                                }
                            }
                        } else {
                            homeProgramsList.forEach { hp ->
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(hp.title ?: "Home Practice", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (hp.isActive) SelforaSuccess.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                if (hp.isActive) "ACTIVE" else "INACTIVE",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (hp.isActive) SelforaSuccess else Color.Gray,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val startStr = hp.startDate?.take(10) ?: "Recent"
                                    Text("Assigned: $startStr • Frequency: ${hp.frequencyPerWeek} times/week", fontSize = 12.sp, color = SelforaTextSecondary)
                                    if (!hp.caregiverInstructions.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Caregiver Instructions:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                                        Text("\"${hp.caregiverInstructions}\"", fontSize = 12.sp, color = SelforaTextSecondary)
                                    }
                                }
                            }
                        }

                        GlassButton(
                            text = "Assign New Home Programme",
                            onClick = { onAssignProgramme(childId) },
                            icon = Icons.Default.Add,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 6. Progress Tab
                "Progress" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        val allPerformanceRecords = sessionsList.flatMap { it.performanceRecords }
                        val totalAssessedSteps = allPerformanceRecords.size
                        val indepSteps = allPerformanceRecords.count { it.promptLevelId == 0 }
                        val visualSteps = allPerformanceRecords.count { it.promptLevelId == 1 }
                        val verbalSteps = allPerformanceRecords.count { it.promptLevelId in listOf(2, 3, 4) }
                        val physicalSteps = allPerformanceRecords.count { it.promptLevelId in listOf(5, 6) }

                        val indepPct = if (totalAssessedSteps > 0) (indepSteps * 100) / totalAssessedSteps else 0
                        val visualPct = if (totalAssessedSteps > 0) (visualSteps * 100) / totalAssessedSteps else 0
                        val verbalPct = if (totalAssessedSteps > 0) (verbalSteps * 100) / totalAssessedSteps else 0
                        val physicalPct = if (totalAssessedSteps > 0) (physicalSteps * 100) / totalAssessedSteps else 0

                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text("ASSISTANCE LEVEL DISTRIBUTION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            Spacer(modifier = Modifier.height(12.dp))

                            if (totalAssessedSteps > 0) {
                                ChildMetricProgress("Independent Performance", indepPct, SelforaSuccess)
                                Spacer(modifier = Modifier.height(8.dp))
                                ChildMetricProgress("Visual Prompts Needed", visualPct, SelforaPrimary)
                                Spacer(modifier = Modifier.height(8.dp))
                                ChildMetricProgress("Verbal Prompts Needed", verbalPct, SelforaSecondary)
                                Spacer(modifier = Modifier.height(8.dp))
                                ChildMetricProgress("Physical Assistance", physicalPct, SelforaWarning)
                            } else {
                                Text("No clinical session performance records yet.", fontSize = 12.sp, color = SelforaTextSecondary)
                            }
                        }

                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text("HOME PRACTICE PARTICIPATION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            Spacer(modifier = Modifier.height(10.dp))
                            val cgSummary = dashboardData?.caregiverProgressSummary
                            val subCount = cgSummary?.totalSubmissions ?: 0
                            Text(cgSummary?.reportedOutcomeSummary ?: "No caregiver practice submissions recorded yet.", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                            if (subCount > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Caregiver Practice Submissions: $subCount logged", fontSize = 12.sp, color = SelforaSuccess)
                            }
                        }

                        GlassOutlinedButton(
                            text = "Open Full Analytical Progress Dashboard →",
                            onClick = { onViewProgress(childId) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

    // Reset Password Success Dialog
    if (showResetPasswordDialog && resetPasswordResult != null) {
        AlertDialog(
            onDismissRequest = { showResetPasswordDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = SelforaPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Temporary Password Reset", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text("A NEW unique temporary password has been generated for $caregiverName.", fontSize = 14.sp, color = SelforaTextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SelforaBlueLight.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                            .border(1.dp, SelforaPrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("NEW TEMPORARY PASSWORD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(resetPasswordResult!!.newTemporaryPassword, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp, color = SelforaTextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("⚠️ The old password has been invalidated. The caregiver must change password on next login.", fontSize = 11.sp, color = SelforaWarning)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Temporary Password", resetPasswordResult!!.newTemporaryPassword)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "New temporary password copied to clipboard", Toast.LENGTH_SHORT).show()
                        showResetPasswordDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary)
                ) {
                    Text("Copy & Close")
                }
            }
        )
    }
}

@Composable
fun GlassGoalCategoryCard(
    category: String,
    activityName: String,
    targetSteps: String,
    currentAssistance: String,
    progressPercentage: Int,
    lastUpdated: String,
    accentColor: Color
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(category, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
            Surface(shape = RoundedCornerShape(10.dp), color = accentColor.copy(alpha = 0.15f)) {
                Text(currentAssistance, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(activityName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SelforaPrimary)
        Text("Target Task Steps: $targetSteps", fontSize = 11.sp, color = SelforaTextSecondary)
        Spacer(modifier = Modifier.height(10.dp))
        ChildMetricProgress("Goal Progress", progressPercentage, accentColor)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Last Recorded Update: $lastUpdated", fontSize = 10.sp, color = SelforaTextSecondary)
    }
}

@Composable
fun GlassSessionHistoryCard(
    date: String,
    activityName: String,
    duration: String,
    status: String,
    independencePercentage: Int
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(activityName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Text("Date: $date • Duration: $duration", fontSize = 12.sp, color = SelforaTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Independence Score", fontSize = 11.5.sp, color = SelforaTextSecondary)
                    Text("$independencePercentage%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                SelforaLinearProgress(
                    progress = independencePercentage.toFloat() / 100f,
                    color = SelforaPrimary,
                    height = 6.dp
                )
            }
            Surface(shape = RoundedCornerShape(10.dp), color = SelforaSuccess.copy(alpha = 0.15f)) {
                Text(status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SelforaSuccess, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable
fun RecentSessionItem(
    date: String,
    activityName: String,
    promptLevel: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(1.dp, SelforaBorder.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(date, fontSize = 11.sp, color = SelforaTextSecondary, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(2.dp))
                Text(activityName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(promptLevel, fontSize = 12.sp, color = SelforaPrimary, fontWeight = FontWeight.SemiBold)
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SelforaSuccess.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "Completed",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SelforaSuccess,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

