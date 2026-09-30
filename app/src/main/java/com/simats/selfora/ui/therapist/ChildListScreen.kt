package com.simats.selfora.ui.therapist

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
    var childrenList by remember { mutableStateOf<List<ChildSummaryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val filterOptions = listOf("All", "Active", "Assessment Pending", "Home Programme Active")

    // Default sample list for instant offline responsiveness
    val defaultChildren = listOf(
        ChildSummaryItem(1L, "Arjun Kumar", 8, "Priya Kumar", "Mother", 72, 65, 80, true, true),
        ChildSummaryItem(2L, "Ananya Reddy", 6, "Suresh Reddy", "Father", 85, 90, 78, false, true),
        ChildSummaryItem(3L, "Kavya Patel", 7, "Meera Patel", "Mother", 45, 50, 60, true, false),
        ChildSummaryItem(4L, "Rohan Sharma", 9, "Sunita Sharma", "Mother", 92, 88, 95, false, true)
    )

    LaunchedEffect(searchQuery, selectedFilter) {
        isLoading = true
        try {
            val filterParam = when (selectedFilter) {
                "Assessment Pending" -> "ASSESSMENT_PENDING"
                "Home Programme Active" -> "HOME_PROGRAM_ACTIVE"
                "Active" -> "ACTIVE"
                else -> null
            }
            val response = ApiClient.apiService.getTherapistChildren(filterParam, searchQuery.ifBlank { null })
            val body = response.body()
            if (response.isSuccessful && body != null && body.isNotEmpty()) {
                childrenList = body
            } else {
                childrenList = defaultChildren.filter {
                    val matchSearch = searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
                    val matchFilter = when (selectedFilter) {
                        "Assessment Pending" -> it.hasPendingAssessment
                        "Home Programme Active" -> it.hasActiveHomeProgram
                        else -> true
                    }
                    matchSearch && matchFilter
                }
            }
        } catch (_: Exception) {
            childrenList = defaultChildren.filter {
                val matchSearch = searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
                val matchFilter = when (selectedFilter) {
                    "Assessment Pending" -> it.hasPendingAssessment
                    "Home Programme Active" -> it.hasActiveHomeProgram
                    else -> true
                }
                matchSearch && matchFilter
            }
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Children", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SelforaTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SelforaTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onAddChildClick) {
                        Icon(Icons.Default.Add, contentDescription = "Add Child", tint = SelforaPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SelforaSurface)
            )
        },
        containerColor = SelforaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            GlassTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "",
                placeholder = "🔍 Search child by name...",
                leadingIcon = Icons.Default.Search,
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, contentDescription = "Clear") } }
                } else null
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { filter ->
                    GlassChip(
                        text = filter,
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        modifier = Modifier.weight(1f)
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
                    Text("No children found matching criteria", color = SelforaTextSecondary, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SelforaBlueLight,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("👦", fontSize = 24.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(child.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SelforaTextPrimary)
                    Text("Age: ${child.age} • Caregiver: ${child.caregiverName} (${child.caregiverRelationship})", fontSize = 12.sp, color = SelforaTextSecondary)
                }
            }
            if (child.hasPendingAssessment) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SelforaWarning.copy(alpha = 0.15f)
                ) {
                    Text("Pending Assmt", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SelforaWarning, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Progress indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ChildMetricProgress("Dressing", child.dressingPercentage, SelforaPrimary, modifier = Modifier.weight(1f))
            ChildMetricProgress("Eating", child.eatingPercentage, SelforaSecondary, modifier = Modifier.weight(1f))
            ChildMetricProgress("Shoes", child.shoesPercentage, SelforaSuccess, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(14.dp))

        GlassOutlinedButton(
            text = "View Child →",
            onClick = onViewChild,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ChildMetricProgress(
    title: String,
    percentage: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, fontSize = 11.sp, color = SelforaTextSecondary)
            Text("$percentage%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = percentage / 100f,
            color = color,
            trackColor = color.copy(alpha = 0.15f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color.Transparent, shape = RoundedCornerShape(3.dp))
        )
    }
}
