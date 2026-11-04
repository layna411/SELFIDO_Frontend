package com.simats.selfora.ui.therapist

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.ChildSummaryItem
import com.simats.selfora.ui.components.avatar.ChildAvatarImage
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.components.progress.*
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildListScreen(
    onChildSelected: (Long) -> Unit,
    onStartSession: (Long) -> Unit = onChildSelected,
    onAddChildClick: () -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var sortBy by remember { mutableStateOf("Name") } // Name, Recent Session
    var childrenList by remember { mutableStateOf<List<ChildSummaryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    val filterOptions = listOf("All", "Active", "Assessment Pending", "Completed")
    val sortOptions = listOf("Name", "Recent Session")

    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadChildren() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val filterParam = when (selectedFilter) {
                    "Assessment Pending" -> "ASSESSMENT_PENDING"
                    "Completed" -> "COMPLETED"
                    "Active" -> "ACTIVE"
                    else -> null
                }
                val response = ApiClient.apiService.getTherapistChildren(filterParam, searchQuery.ifBlank { null })
                if (response.isSuccessful && response.body() != null) {
                    var listToUse = response.body()!!

                    listToUse = listToUse.filter {
                        val matchSearch = searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.id.toString().contains(searchQuery)
                        val matchFilter = when (selectedFilter) {
                            "Assessment Pending" -> it.hasPendingAssessment || it.programmeStatus == "PENDING_ASSESSMENT"
                            "Active" -> it.programmeStatus == "ACTIVE" || it.hasActiveHomeProgram
                            "Completed" -> it.programmeStatus == "COMPLETED"
                            else -> true
                        }
                        matchSearch && matchFilter
                    }

                    listToUse = if (sortBy == "Recent Session") {
                        listToUse.sortedByDescending { it.lastSessionDate }
                    } else {
                        listToUse.sortedBy { it.name }
                    }

                    childrenList = listToUse
                } else {
                    val code = response.code()
                    childrenList = emptyList()
                    errorMessage = "Failed to load assigned children (HTTP $code)"
                }
            } catch (e: Exception) {
                childrenList = emptyList()
                errorMessage = "Network error: ${e.localizedMessage}. Tap Refresh to try again."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(searchQuery, selectedFilter, sortBy) {
        loadChildren()
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "Select Child",
                subtitle = "Active Pediatric Cases & ADL Goals",
                onBackClick = onBack,
                accentColor = SelforaPrimary,
                actions = {
                    GlassIconButton(
                        icon = Icons.Default.Add,
                        onClick = onAddChildClick,
                        tint = SelforaPrimary,
                        size = 40.dp,
                        iconSize = 22.dp
                    )
                }
            )
        },
        containerColor = SelforaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar (Name or ID)
            GlassSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search child",
                accentColor = SelforaPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips & Sort Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filter:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaTextSecondary)
                filterOptions.forEach { filter ->
                    GlassChip(
                        text = filter,
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text("Sort:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaTextSecondary)
                sortOptions.forEach { sort ->
                    GlassChip(
                        text = sort,
                        selected = sortBy == sort,
                        onClick = { sortBy = sort },
                        accentColor = SelforaSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SelforaPrimary)
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    GlassCard(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(errorMessage!!, color = SelforaTextSecondary, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { loadChildren() }, colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary)) {
                                Text("Retry")
                            }
                        }
                    }
                }
            } else if (childrenList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SelforaPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ChildCare, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(36.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        val isFiltered = searchQuery.isNotBlank() || selectedFilter != "All"
                        Text(
                            text = if (isFiltered) "No Matching Children Found" else "No Children Assigned",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = SelforaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isFiltered)
                                "No children match your search criteria. Try clearing filters or search query."
                            else
                                "You do not have any pediatric cases assigned to your account yet. When assigned by the administrator or enrolled via workflow, they will appear here.",
                            color = SelforaTextSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onAddChildClick,
                            colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Child & Caregiver", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 150.dp)
                ) {
                    items(childrenList) { child ->
                        GlassChildCard(
                            child = child,
                            onViewChild = { onChildSelected(child.id) },
                            onStartSession = { onStartSession(child.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GlassChildCard(
    child: ChildSummaryItem,
    onViewChild: () -> Unit,
    onStartSession: () -> Unit = onViewChild
) {
    val safeGender = (child.gender ?: "BOY").uppercase()
    val safeName = child.name ?: "Unnamed Child"
    val safeCaregiver = child.caregiverName ?: "Unlinked"
    val safeRel = child.caregiverRelationship ?: "Caregiver"
    val safeStatus = child.programmeStatus ?: "ACTIVE"
    val safeLastSession = child.lastSessionDate ?: "No recent session"
    val overallPct = child.overallProgressPercentage.coerceIn(0, 100)
    val overallColor = when {
        overallPct >= 75 -> SelforaSuccess
        overallPct >= 50 -> SelforaPrimary
        else -> SelforaWarning
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 6.dp,
        contentPadding = PaddingValues(16.dp),
        onClick = onViewChild
    ) {
        // Top Row: Avatar, Child Identity & Overall Progress Circular Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, SelforaPrimary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    ChildAvatarImage(
                        avatarId = child.avatarUrl ?: "ic_avatar_default",
                        gender = safeGender,
                        size = 50.dp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = safeName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SelforaTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (child.isDemoProfile || child.id >= 100L) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SelforaPrimary
                            ) {
                                Text(
                                    "DEMO",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "ID: #${child.id} • ${child.age} yrs • $safeGender",
                        fontSize = 11.5.sp,
                        color = SelforaPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    Text(
                        text = "Caregiver: $safeCaregiver ($safeRel)",
                        fontSize = 10.5.sp,
                        color = SelforaTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Overall Progress Circular Gauge
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressRing(
                    progress = overallPct / 100f,
                    ringColor = overallColor,
                    size = 50.dp,
                    strokeWidth = 5.dp,
                    showPercentageText = true,
                    subText = "Overall"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status Chip & Assigned Goals Sub-Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (safeStatus) {
                    "PENDING_ASSESSMENT" -> SelforaWarning.copy(alpha = 0.12f)
                    "COMPLETED" -> SelforaSuccess.copy(alpha = 0.12f)
                    else -> SelforaPrimary.copy(alpha = 0.12f)
                }
            ) {
                Text(
                    text = when (safeStatus) {
                        "PENDING_ASSESSMENT" -> "Assmt Pending"
                        "COMPLETED" -> "Completed"
                        else -> "Active Goal"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (safeStatus) {
                        "PENDING_ASSESSMENT" -> SelforaWarning
                        "COMPLETED" -> SelforaSuccess
                        else -> SelforaPrimary
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Text(
                text = "Last: $safeLastSession",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                color = SelforaTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Standardized 4-ADL Category Progress Grid
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.65f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "ADL CATEGORY PROGRESS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SelforaTextSecondary,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                SelforaAdlCategoryGrid(
                    dressingPct = child.dressingPercentage,
                    eatingPct = child.eatingPercentage,
                    groomingPct = child.groomingPercentage,
                    shoesPct = child.shoesPercentage
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
                text = "Assigned Goals: ${child.assignedGoalsCount}",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = SelforaTextSecondary
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SelforaPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.clickable { onStartSession() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start Session", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                    }
                }
                Text(
                    text = "Profile →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SelforaTextSecondary,
                    modifier = Modifier.clickable { onViewChild() }
                )
            }
        }
    }
}

@Composable
fun ChildMetricProgress(
    title: String?,
    percentage: Int,
    color: Color = SelforaPrimary,
    modifier: Modifier = Modifier
) {
    val safeTitle = title ?: "Progress"
    val safePct = percentage.coerceIn(0, 100)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = safeTitle,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = SelforaTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$safePct%",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        SelforaLinearProgress(
            percentage = safePct,
            color = color,
            height = 6.dp
        )
    }
}
