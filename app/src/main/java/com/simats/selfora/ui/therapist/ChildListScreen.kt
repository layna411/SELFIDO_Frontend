package com.simats.selfora.ui.therapist

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.simats.selfora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildListScreen(
    onChildSelected: (Long) -> Unit,
    onAddChildClick: () -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var sortBy by remember { mutableStateOf("Name") } // Name, Recent Session
    var childrenList by remember { mutableStateOf<List<ChildSummaryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val filterOptions = listOf("All", "Active", "Assessment Pending", "Completed")
    val sortOptions = listOf("Name", "Recent Session")

    // Default sample list featuring 3D Avatar Demo profiles & pediatric cases
    val defaultChildren = listOf(
        ChildSummaryItem(101L, "Aarav Sharma", 7, "BOY", "aarav_boy", "Sunita Sharma", "Mother", "Today", "ACTIVE", 3, 80, 75, 70, 80, false, true, isDemoProfile = true),
        ChildSummaryItem(102L, "Ananya Reddy", 6, "GIRL", "ananya_girl", "Suresh Reddy", "Father", "Yesterday", "ACTIVE", 3, 85, 90, 78, 85, false, true, isDemoProfile = true),
        ChildSummaryItem(1L, "Arjun Kumar", 8, "BOY", "aarav_boy", "Priya Kumar", "Mother", "2026-10-02", "ACTIVE", 3, 72, 65, 80, 72, true, true),
        ChildSummaryItem(3L, "Kavya Patel", 7, "GIRL", "ananya_girl", "Meera Patel", "Mother", "2026-09-25", "PENDING_ASSESSMENT", 2, 45, 50, 60, 51, true, false),
        ChildSummaryItem(4L, "Rohan Sharma", 9, "BOY", "aarav_boy", "Sunita Sharma", "Mother", "2026-10-01", "ACTIVE", 3, 92, 88, 95, 91, false, true)
    )

    LaunchedEffect(searchQuery, selectedFilter, sortBy) {
        isLoading = true
        try {
            val filterParam = when (selectedFilter) {
                "Assessment Pending" -> "ASSESSMENT_PENDING"
                "Completed" -> "COMPLETED"
                "Active" -> "ACTIVE"
                else -> null
            }
            val response = ApiClient.apiService.getTherapistChildren(filterParam, searchQuery.ifBlank { null })
            val body = response.body()
            var listToUse = if (response.isSuccessful && body != null && body.isNotEmpty()) body else defaultChildren

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
        } catch (_: Exception) {
            var listToUse = defaultChildren.filter {
                val matchSearch = searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.id.toString().contains(searchQuery)
                val matchFilter = when (selectedFilter) {
                    "Assessment Pending" -> it.hasPendingAssessment || it.programmeStatus == "PENDING_ASSESSMENT"
                    "Active" -> it.programmeStatus == "ACTIVE" || it.hasActiveHomeProgram
                    "Completed" -> it.programmeStatus == "COMPLETED"
                    else -> true
                }
                matchSearch && matchFilter
            }
            listToUse = if (sortBy == "Recent Session") listToUse.sortedByDescending { it.lastSessionDate } else listToUse.sortedBy { it.name }
            childrenList = listToUse
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "Assigned Children",
                subtitle = "Assigned Pediatric Records & ADL Profiles",
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
                placeholder = "Search child by name or ID...",
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
            } else if (childrenList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = SelforaTextSecondary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No children found matching search criteria", color = SelforaTextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 150.dp)
                ) {
                    items(childrenList) { child ->
                        GlassChildCard(child = child, onViewChild = { onChildSelected(child.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun GlassChildCard(
    child: ChildSummaryItem,
    onViewChild: () -> Unit
) {
    val safeGender = (child.gender ?: "BOY").uppercase()
    val isGirl = safeGender.contains("GIRL") || safeGender.contains("FEMALE")
    val avatarEmoji = if (isGirl) "👧" else "👦"
    val safeName = child.name ?: "Unnamed Child"
    val safeCaregiver = child.caregiverName ?: "Unlinked"
    val safeRel = child.caregiverRelationship ?: "Caregiver"
    val safeStatus = child.programmeStatus ?: "ACTIVE"
    val safeLastSession = child.lastSessionDate ?: "No recent session"

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 8.dp,
        onClick = onViewChild
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChildAvatarImage(
                    avatarId = child.avatarUrl ?: "ic_avatar_default",
                    gender = safeGender,
                    size = 50.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = safeName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SelforaTextPrimary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        if (child.isDemoProfile || child.id >= 100L) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SelforaPrimary
                            ) {
                                Text(
                                    "DEMO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "ID: #${child.id} • ${child.age} yrs • $safeGender",
                        fontSize = 12.sp,
                        color = SelforaPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Caregiver: $safeCaregiver ($safeRel)",
                        fontSize = 11.sp,
                        color = SelforaTextSecondary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (safeStatus) {
                    "PENDING_ASSESSMENT" -> SelforaWarning.copy(alpha = 0.15f)
                    "COMPLETED" -> SelforaSuccess.copy(alpha = 0.15f)
                    else -> SelforaPrimary.copy(alpha = 0.15f)
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
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ADL Progress Bar Breakdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ChildMetricProgress("Dressing", child.dressingPercentage, SelforaPrimary, modifier = Modifier.weight(1f))
            ChildMetricProgress("Eating", child.eatingPercentage, SelforaSecondary, modifier = Modifier.weight(1f))
            ChildMetricProgress("Shoes", child.shoesPercentage, SelforaSuccess, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Assigned Goals: ${child.assignedGoalsCount} • Last Session: $safeLastSession", fontSize = 11.sp, color = SelforaTextSecondary)
            Text("Overall: ${child.overallProgressPercentage}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
        }

        Spacer(modifier = Modifier.height(10.dp))

        GlassOutlinedButton(
            text = "View Clinical Profile →",
            onClick = onViewChild,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ChildMetricProgress(
    title: String?,
    percentage: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val safeTitle = title ?: "Progress"
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(safeTitle, fontSize = 11.sp, color = SelforaTextSecondary)
            Text("$percentage%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = (percentage / 100f).coerceIn(0f, 1f),
            color = color,
            trackColor = color.copy(alpha = 0.15f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color.Transparent, shape = RoundedCornerShape(3.dp))
        )
    }
}
