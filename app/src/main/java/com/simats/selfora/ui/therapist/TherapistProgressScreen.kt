package com.simats.selfora.ui.therapist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.*
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.components.progress.*
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapistProgressScreen(
    childId: Long = 1L,
    onChildSelected: ((Long) -> Unit)? = null,
    onNavigateToAnalyzeAndAdapt: (Long) -> Unit = {},
    onNavigateToPromptFading: (Long) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var availableChildren by remember { mutableStateOf<List<ChildSummaryItem>>(emptyList()) }
    var isLoadingChildren by remember { mutableStateOf(true) }
    var selectedChild by remember { mutableStateOf<ChildSummaryItem?>(null) }
    var selectedTab by remember { mutableStateOf("Longitudinal Trends") }

    // Real-time Child Search Query State
    var childSearchQuery by remember { mutableStateOf("") }
    val filteredChildren = remember(availableChildren, childSearchQuery) {
        if (childSearchQuery.isBlank()) {
            availableChildren
        } else {
            val q = childSearchQuery.trim().lowercase()
            availableChildren.filter {
                it.name.lowercase().contains(q) ||
                it.id.toString().contains(q) ||
                (it.caregiverName.isNotBlank() && it.caregiverName.lowercase().contains(q))
            }
        }
    }

    // Dynamic Child-Specific Data State
    var childProgress by remember { mutableStateOf<ProgressSummaryResponse?>(null) }
    var childSessions by remember { mutableStateOf<List<SessionResponse>>(emptyList()) }
    var childPracticeHistory by remember { mutableStateOf<List<CaregiverPracticeSessionResponse>>(emptyList()) }
    var childAssessments by remember { mutableStateOf<List<AssessmentResponse>>(emptyList()) }
    var isLoadingData by remember { mutableStateOf(false) }

    // Fetch caseload of assigned children
    LaunchedEffect(Unit) {
        isLoadingChildren = true
        try {
            val response = ApiClient.apiService.getTherapistChildren()
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!
                availableChildren = list
                val initial = if (childId > 0) list.find { it.id == childId } ?: list.firstOrNull() else list.firstOrNull()
                selectedChild = initial
                if (initial != null) {
                    onChildSelected?.invoke(initial.id)
                }
            }
        } catch (_: Exception) {}
        finally {
            isLoadingChildren = false
        }
    }

    // When childId prop changes externally, update selectedChild
    LaunchedEffect(childId) {
        if (childId > 0 && availableChildren.isNotEmpty()) {
            val match = availableChildren.find { it.id == childId }
            if (match != null && match.id != selectedChild?.id) {
                selectedChild = match
            }
        }
    }

    // Dynamically load child-specific progress, session history, and practice records whenever selectedChild changes
    LaunchedEffect(selectedChild?.id) {
        val cid = selectedChild?.id ?: return@LaunchedEffect
        isLoadingData = true
        try {
            val progRes = ApiClient.apiService.getChildProgress(cid)
            childProgress = if (progRes.isSuccessful && progRes.body() != null) progRes.body() else null
        } catch (_: Exception) {
            childProgress = null
        }

        try {
            val sessRes = ApiClient.apiService.getSessionsForChild(cid)
            childSessions = if (sessRes.isSuccessful && sessRes.body() != null) sessRes.body()!! else emptyList()
        } catch (_: Exception) {
            childSessions = emptyList()
        }

        try {
            val pracRes = ApiClient.apiService.getPracticeHistory(cid)
            childPracticeHistory = if (pracRes.isSuccessful && pracRes.body() != null) pracRes.body()!! else emptyList()
        } catch (_: Exception) {
            childPracticeHistory = emptyList()
        }

        try {
            val assessRes = ApiClient.apiService.getAssessmentsForChild(cid)
            childAssessments = if (assessRes.isSuccessful && assessRes.body() != null) assessRes.body()!! else emptyList()
        } catch (_: Exception) {
            childAssessments = emptyList()
        } finally {
            isLoadingData = false
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 2.dp, spotColor = Color(0x18172033)),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Longitudinal Progress",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = if (selectedChild != null) "Patient: ${selectedChild!!.name} • ID: #${selectedChild!!.id}" else "Select a patient",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SelforaPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.width(IntrinsicSize.Max)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = SelforaPrimary.copy(alpha = 0.12f),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("📈", fontSize = 12.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Clinical Analytics",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SelforaTextPrimary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Text(
                                        text = "Longitudinal",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SelforaPrimary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = SelforaBackground
    ) { padding ->
        if (isLoadingChildren) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SelforaPrimary)
            }
        } else if (availableChildren.isEmpty() || selectedChild == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SelforaPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ChildCare, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(32.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("No Children Assigned", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaTextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "You do not have any children assigned to your clinical account. When children are enrolled or assigned by your administrator, their longitudinal progress and prompt fading trends will appear here.",
                            color = SelforaTextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            val currentChild = selectedChild!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // =========================================================================
                // 1. PATIENT CASELOAD & REAL-TIME SEARCH SECTION (Matching Assessment Screen)
                // =========================================================================
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SELECT PATIENT CASELOAD",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaPrimary,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (childSearchQuery.isNotBlank() && filteredChildren.isEmpty()) SelforaError.copy(alpha = 0.12f) else SelforaPrimary.copy(alpha = 0.08f),
                            modifier = Modifier.width(IntrinsicSize.Max)
                        ) {
                            Text(
                                text = if (childSearchQuery.isNotBlank()) "${filteredChildren.size} of ${availableChildren.size} Found" else "${availableChildren.size} Assigned",
                                fontSize = 10.5.sp,
                                color = if (childSearchQuery.isNotBlank() && filteredChildren.isEmpty()) SelforaError else SelforaPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
                            )
                        }
                    }

                    // Pixel-perfect Search Bar
                    GlassSearchBar(
                        query = childSearchQuery,
                        onQueryChange = { childSearchQuery = it },
                        placeholder = "Search patient by name or ID",
                        onClear = { childSearchQuery = "" },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Filtered Children Pill Carousel
                    if (filteredChildren.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "No children match \"$childSearchQuery\"",
                                    fontSize = 12.5.sp,
                                    color = SelforaTextSecondary
                                )
                                Text(
                                    text = "Clear search",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaPrimary,
                                    modifier = Modifier.clickable { childSearchQuery = "" }
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredChildren.forEach { child ->
                                val isSelected = currentChild.id == child.id
                                val avatarEmoji = if (child.gender.trim().uppercase().startsWith("G") || child.gender.trim().uppercase().startsWith("F")) "👧" else "👦"
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) SelforaPrimary else Color.White,
                                    border = BorderStroke(1.dp, if (isSelected) SelforaPrimary else Color(0xFFE2E8F0)),
                                    shadowElevation = if (isSelected) 2.dp else 1.dp,
                                    modifier = Modifier
                                        .height(42.dp)
                                        .clickable {
                                             selectedChild = child
                                             onChildSelected?.invoke(child.id)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(avatarEmoji, fontSize = 15.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = child.name,
                                            fontSize = 12.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else SelforaTextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "#${child.id}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else SelforaTextSecondary
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Patient Profile Header Summary Card
                val isGirlPatient = currentChild.gender.trim().uppercase().let { it.startsWith("G") || it.startsWith("F") }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isGirlPatient) SelforaSecondary.copy(alpha = 0.14f) else SelforaPrimary.copy(alpha = 0.12f),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(if (isGirlPatient) "👧" else "👦", fontSize = 24.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentChild.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SelforaTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Age: ${currentChild.age} yrs • ${currentChild.caregiverName} (${currentChild.caregiverRelationship})",
                                        fontSize = 12.sp,
                                        color = SelforaTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (currentChild.overallProgressPercentage >= 75) SelforaSuccess.copy(alpha = 0.12f) else SelforaPrimary.copy(alpha = 0.12f),
                                modifier = Modifier.width(IntrinsicSize.Max)
                            ) {
                                Text(
                                    text = "${currentChild.overallProgressPercentage}% Overall",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentChild.overallProgressPercentage >= 75) SelforaSuccess else SelforaPrimary,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Shortcuts to Stage 5 Modules
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onNavigateToAnalyzeAndAdapt(currentChild.id) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Analyze & Adapt",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { onNavigateToPromptFading(currentChild.id) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SelforaPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SelforaPrimary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Prompt Fading",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sub-tabs: Longitudinal Trends vs Session Timeline vs Caregiver Practice
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val tabList = listOf(
                            Triple("Longitudinal Trends", "Trends", Icons.AutoMirrored.Filled.TrendingUp),
                            Triple("Session Timeline", "Timeline", Icons.Default.History),
                            Triple("Caregiver Practice", "Caregiver", Icons.Default.Home)
                        )
                        tabList.forEach { (tabKey, shortLabel, icon) ->
                            val isSelected = selectedTab == tabKey
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) SelforaPrimary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clickable { selectedTab = tabKey }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else SelforaTextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = shortLabel,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else SelforaTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // =========================================================================
                // DYNAMIC CONTENT BASED ON SELECTED CHILD & TAB
                // =========================================================================
                when (selectedTab) {
                    // TAB 1: DYNAMIC LONGITUDINAL TRENDS
                    "Longitudinal Trends" -> {
                        if (isLoadingData) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = SelforaPrimary, modifier = Modifier.size(30.dp))
                            }
                        } else {
                            // 1. Compute real data points from persisted backend history & sessions (no fabricated data)
                            val dynamicDataPoints = remember(currentChild.id, childProgress, childSessions) {
                                if (childProgress?.historyPoints != null && childProgress!!.historyPoints.isNotEmpty()) {
                                    childProgress!!.historyPoints.map { pt ->
                                        formatDateLabel(pt.sessionDate) to pt.independencePercentage.toFloat()
                                    }
                                } else if (childSessions.isNotEmpty()) {
                                    childSessions.sortedBy { it.startTime }.map { s ->
                                        formatDateLabel(s.startTime) to (s.independencePercentage?.toFloat() ?: 0f)
                                    }
                                } else {
                                    emptyList()
                                }
                            }

                            if (dynamicDataPoints.isNotEmpty()) {
                                ProgressTrendChart(
                                    dataPoints = dynamicDataPoints,
                                    title = "Longitudinal Independence Trend",
                                    subtitle = "Clinical L0-L6 progression for ${currentChild.name} • ${currentChild.gender} (${currentChild.age} yrs)"
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    shadowElevation = 1.dp
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("📊", fontSize = 28.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "No Completed Sessions Yet",
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SelforaTextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Complete a clinical therapy session for ${currentChild.name} to plot longitudinal independence trends.",
                                            fontSize = 12.sp,
                                            color = SelforaTextSecondary,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 2. ADL Category Progress Breakdown (100% Real per child)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ADL CATEGORY PROGRESS BREAKDOWN",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaPrimary,
                                    letterSpacing = 0.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                )
                                Text(
                                    text = "${currentChild.assignedGoalsCount} Goals Active",
                                    fontSize = 11.sp,
                                    color = SelforaTextSecondary,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val dressingSessions = remember(currentChild.id, childSessions) {
                                childSessions.filter { s ->
                                    val t = s.activityTitle.lowercase()
                                    !t.contains("shoe") && !t.contains("sock") && (
                                        t.contains("dress") || t.contains("shirt") || t.contains("pant") ||
                                        t.contains("frock") || t.contains("jacket") || t.contains("trouser") ||
                                        t.contains("short") || t.contains("vest") || t.contains("skirt") ||
                                        t.contains("top") || t.contains("brief") || t.contains("panties") ||
                                        t.contains("camisole")
                                    )
                                }
                            }
                            val eatingSessions = remember(currentChild.id, childSessions) {
                                childSessions.filter { s ->
                                    val t = s.activityTitle.lowercase()
                                    t.contains("eat") || t.contains("spoon") || t.contains("food") ||
                                        t.contains("rice") || t.contains("breakfast") || t.contains("drink") || t.contains("cup")
                                }
                            }
                            val shoesSessions = remember(currentChild.id, childSessions) {
                                childSessions.filter { s ->
                                    val t = s.activityTitle.lowercase()
                                    t.contains("shoe") || t.contains("sock") || t.contains("sandal") || t.contains("slipper") || t.contains("footwear")
                                }
                            }

                            fun computeRealDelta(list: List<SessionResponse>): String? {
                                if (list.size < 2) return null
                                val sorted = list.sortedByDescending { it.startTime }
                                val latest = Math.round(sorted[0].independencePercentage ?: 0.0).toInt()
                                val prev = Math.round(sorted[1].independencePercentage ?: 0.0).toInt()
                                val diff = latest - prev
                                return when {
                                    diff > 0 -> "+$diff%"
                                    diff < 0 -> "$diff%"
                                    else -> "0%"
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                ADLProgressCard("Dressing", currentChild.dressingPercentage, sessionCount = dressingSessions.size, trendDelta = computeRealDelta(dressingSessions))
                                ADLProgressCard("Eating", currentChild.eatingPercentage, sessionCount = eatingSessions.size, trendDelta = computeRealDelta(eatingSessions))
                                ADLProgressCard("Shoes & Footwear", currentChild.shoesPercentage, sessionCount = shoesSessions.size, trendDelta = computeRealDelta(shoesSessions))
                                ADLProgressCard("Overall ADL Mastery", currentChild.overallProgressPercentage, sessionCount = childSessions.size, trendDelta = computeRealDelta(childSessions))
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 3. Clinical Assistance Hierarchy Evaluation (Dynamic prompt code)
                            val (dynamicPromptCode, dynamicPromptTitle) = remember(currentChild.overallProgressPercentage, childSessions.size) {
                                if (childSessions.isEmpty() && currentChild.overallProgressPercentage == 0) {
                                    "L0" to "No Clinical Evaluation Recorded Yet"
                                } else {
                                    when {
                                        currentChild.overallProgressPercentage >= 85 -> "L0" to "Level 0 – Independent (Mastery Achieved)"
                                        currentChild.overallProgressPercentage >= 70 -> "L1" to "Level 1 – Visual Prompt (Current Primary Level)"
                                        currentChild.overallProgressPercentage >= 55 -> "L2" to "Level 2 – Gesture Prompt (Current Primary Level)"
                                        currentChild.overallProgressPercentage >= 40 -> "L3" to "Level 3 – Verbal Prompt (Current Primary Level)"
                                        currentChild.overallProgressPercentage >= 25 -> "L4" to "Level 4 – Model / Video (Current Primary Level)"
                                        else -> "L5" to "Level 5 – Partial Physical Assistance"
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shadowElevation = 1.dp
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("CLINICAL ASSISTANCE HIERARCHY EVALUATION", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary, letterSpacing = 0.5.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Primary assistance level evaluated across ${currentChild.name}'s active ADL goals", fontSize = 11.sp, color = SelforaTextSecondary)
                                    Spacer(modifier = Modifier.height(14.dp))

                                    SessionProgressIndicator(promptLevelCode = dynamicPromptCode, promptLevelName = dynamicPromptTitle)

                                    Spacer(modifier = Modifier.height(14.dp))
                                    ProgressLegend(type = "PROMPT_LEVELS")
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 4. Dynamic Child Milestone
                            val milestoneSkill = when {
                                currentChild.shoesPercentage > currentChild.dressingPercentage && currentChild.shoesPercentage >= currentChild.eatingPercentage ->
                                    "Independent Shoes & Footwear"
                                currentChild.eatingPercentage > currentChild.dressingPercentage ->
                                    "Independent Spoon Feeding"
                                else ->
                                    "Independent Upper Body Dressing"
                            }
                            MilestoneCard(
                                title = milestoneSkill,
                                description = if (childSessions.isEmpty() && currentChild.overallProgressPercentage == 0) {
                                    "Complete initial clinical sessions for ${currentChild.name} to track milestone mastery."
                                } else {
                                    "${currentChild.name} is at ${currentChild.overallProgressPercentage}% overall ADL independence (primary level: $dynamicPromptCode)."
                                },
                                achievedDate = formatDateLabel(currentChild.lastSessionDate),
                                isAchieved = currentChild.overallProgressPercentage >= 60
                            )
                        }
                    }

                    // TAB 2: DYNAMIC SESSION TIMELINE
                    "Session Timeline" -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shadowElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("LONGITUDINAL CLINICAL SESSION TIMELINE", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary, letterSpacing = 0.5.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Chronological therapy sessions, clinical assessments and milestone logs for ${currentChild.name}", fontSize = 11.sp, color = SelforaTextSecondary)
                                Spacer(modifier = Modifier.height(14.dp))

                                val dynamicTimeline = remember(currentChild.id, childSessions, childAssessments) {
                                    val events = mutableListOf<TimelineEvent>()

                                    // Add real completed therapy sessions
                                    childSessions.forEach { sess ->
                                        val score = Math.round(sess.independencePercentage ?: 0.0).toInt()
                                        val color = if (score >= 75) SelforaSuccess else if (score >= 40) SelforaPrimary else SelforaWarning
                                        events.add(
                                            TimelineEvent(
                                                date = formatDateLabel(sess.startTime),
                                                title = "Therapy Session #${sess.sessionId}",
                                                activity = sess.activityTitle,
                                                score = "$score% Independence",
                                                details = sess.summaryNotes ?: "Conducted by ${sess.conductedByUserName} in ${sess.environment}",
                                                color = color
                                            )
                                        )
                                    }

                                    // Add real assessments
                                    childAssessments.forEach { assess ->
                                        val assessed = assess.stepResults.filter { !it.outcome.equals("NOT_ASSESSED", ignoreCase = true) && it.promptLevelId != 8 }
                                        val indep = assessed.count { it.promptLevelId == 0 && !it.outcome.equals("UNABLE", ignoreCase = true) }
                                        val pct = if (assessed.isNotEmpty()) Math.round((indep * 100.0) / assessed.size).toInt() else 0
                                        events.add(
                                            TimelineEvent(
                                                date = formatDateLabel(assess.assessmentDate),
                                                title = "Clinical Assessment #${assess.assessmentId}",
                                                activity = assess.activityTitle,
                                                score = "$pct% Baseline",
                                                details = assess.notes ?: "Clinical evaluation conducted by ${assess.therapistName}",
                                                color = SelforaSecondary
                                            )
                                        )
                                    }

                                    events
                                }

                                if (dynamicTimeline.isEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 18.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("📋", fontSize = 28.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "No Clinical History Recorded Yet",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SelforaTextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Completed therapy sessions and baseline assessments for ${currentChild.name} will appear here chronologically.",
                                            fontSize = 12.sp,
                                            color = SelforaTextSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    dynamicTimeline.forEach { item ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = item.color.copy(alpha = 0.08f),
                                            border = BorderStroke(1.dp, item.color.copy(alpha = 0.3f)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 10.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = item.title,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SelforaTextPrimary,
                                                        modifier = Modifier.weight(1f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color.White.copy(alpha = 0.85f),
                                                        border = BorderStroke(0.5.dp, Color(0xFFCBD5E1)),
                                                        modifier = Modifier.width(IntrinsicSize.Max)
                                                    ) {
                                                        Text(
                                                            text = item.date,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = SelforaTextSecondary,
                                                            maxLines = 1,
                                                            softWrap = false,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = "Activity: ${item.activity} • Score: ${item.score}",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = item.color,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = item.details,
                                                    fontSize = 11.sp,
                                                    color = SelforaTextSecondary,
                                                    lineHeight = 15.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TAB 3: DYNAMIC CAREGIVER HOME PRACTICE TREND
                    "Caregiver Practice" -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shadowElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "CAREGIVER HOME PRACTICE TREND",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SelforaPrimary,
                                        letterSpacing = 0.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SelforaSecondary.copy(alpha = 0.12f),
                                        modifier = Modifier.width(IntrinsicSize.Max)
                                    ) {
                                        Text(
                                            "Caregiver Logged",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SelforaSecondary,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Log of home practices submitted by ${currentChild.caregiverName} (${currentChild.caregiverRelationship})", fontSize = 11.sp, color = SelforaTextSecondary)
                                Spacer(modifier = Modifier.height(14.dp))

                                // Data Separation Safeguard Note
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SelforaPurpleAccent.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, SelforaSecondary.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("🔒 CLINICAL DATA SEPARATION SAFEGUARD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaSecondary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Caregiver-reported home practice outcomes are displayed separately and are NEVER merged into the therapist's official clinical assessment score.", fontSize = 10.5.sp, color = SelforaTextPrimary, lineHeight = 14.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // 100% Real Caregiver Practice Stats (No fake numbers)
                                val today = LocalDate.now()
                                val weekStart = today.minusDays((today.dayOfWeek.value - 1).toLong())
                                val practicedDatesThisWeek = remember(currentChild.id, childPracticeHistory) {
                                    childPracticeHistory.mapNotNull { rec ->
                                        try {
                                            val rawDate = rec.practiceDate.substringBefore("T")
                                            LocalDate.parse(rawDate)
                                        } catch (_: Exception) {
                                            null
                                        }
                                    }.filter { !it.isBefore(weekStart) && !it.isAfter(weekStart.plusDays(6)) }.toSet()
                                }

                                val totalPractices = childPracticeHistory.size
                                val thisWeekCount = practicedDatesThisWeek.size
                                val weeklyCompliance = "${thisWeekCount}/7 days"
                                val activitiesCount = "${currentChild.assignedGoalsCount} ADLs"

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Min),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text("Total Logged", fontSize = 10.sp, color = SelforaTextSecondary, maxLines = 1, softWrap = false)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("$totalPractices", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary, maxLines = 1, softWrap = false)
                                            Text("Sessions", fontSize = 9.sp, color = SelforaTextMuted, maxLines = 1, softWrap = false)
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text("This Week", fontSize = 10.sp, color = SelforaTextSecondary, maxLines = 1, softWrap = false)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(weeklyCompliance, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (thisWeekCount > 0) SelforaSuccess else SelforaTextSecondary, maxLines = 1, softWrap = false)
                                            Text("Practiced", fontSize = 9.sp, color = SelforaTextMuted, maxLines = 1, softWrap = false)
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text("Active ADLs", fontSize = 10.sp, color = SelforaTextSecondary, maxLines = 1, softWrap = false)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(activitiesCount, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary, maxLines = 1, softWrap = false)
                                            Text("Assigned", fontSize = 9.sp, color = SelforaTextMuted, maxLines = 1, softWrap = false)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Text("Caregiver Weekly Participation Grid (Current Week):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                Spacer(modifier = Modifier.height(8.dp))

                                val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    dayLabels.forEachIndexed { idx, label ->
                                        val dayDate = weekStart.plusDays(idx.toLong())
                                        DayCheckCard(
                                            day = label,
                                            isCompleted = practicedDatesThisWeek.contains(dayDate),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Caregiver Observations:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                Spacer(modifier = Modifier.height(2.dp))

                                val outcomeNote = remember(currentChild.id, childPracticeHistory) {
                                    childPracticeHistory.firstOrNull { !it.caregiverNotes.isNullOrBlank() }?.caregiverNotes
                                        ?: if (childPracticeHistory.isNotEmpty()) {
                                            "Practice submitted by ${currentChild.caregiverName} with no additional text notes."
                                        } else {
                                            "No caregiver home practice observations submitted yet for ${currentChild.name}."
                                        }
                                }
                                Text(outcomeNote, fontSize = 11.5.sp, color = SelforaTextSecondary, fontWeight = FontWeight.Medium, lineHeight = 16.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(110.dp))
            }
        }
    }
}

@Composable
fun DayCheckCard(day: String, isCompleted: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (isCompleted) SelforaSuccess.copy(alpha = 0.12f) else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, if (isCompleted) SelforaSuccess.copy(alpha = 0.7f) else Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 7.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(day, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SelforaTextSecondary, maxLines = 1, softWrap = false)
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                if (isCompleted) "✓" else "—",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isCompleted) SelforaSuccess else SelforaTextMuted,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

private fun formatDateLabel(raw: String?): String {
    if (raw.isNullOrBlank()) return "Recent"
    return try {
        if (raw.contains("T")) {
            val datePart = raw.substringBefore("T")
            val parsed = LocalDate.parse(datePart)
            parsed.format(DateTimeFormatter.ofPattern("MMM dd"))
        } else {
            val parsed = LocalDate.parse(raw)
            parsed.format(DateTimeFormatter.ofPattern("MMM dd"))
        }
    } catch (_: Exception) {
        if (raw.length >= 10) raw.take(10) else raw
    }
}

private data class TimelineEvent(
    val date: String,
    val title: String,
    val activity: String,
    val score: String,
    val details: String,
    val color: Color
)
