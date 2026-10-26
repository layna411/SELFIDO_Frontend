package com.simats.selfora.ui.admin

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.R
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.AdminChildItem
import com.simats.selfora.ui.components.glass.GlassTextField
import kotlinx.coroutines.launch

enum class ChildStatusFilter { ALL, ACTIVE, INACTIVE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildManagementScreen(
    onNavigateToChildDetails: (Long) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(ChildStatusFilter.ALL) }
    var children by remember { mutableStateOf<List<AdminChildItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val blueAccent = Color(0xFF2563EB)
    val textSecondary = Color(0xFF64748B)

    fun fetchChildren() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val response = ApiClient.apiService.getAllAdminChildren()
                if (response.isSuccessful && response.body() != null) {
                    children = response.body()!!
                } else {
                    errorMessage = "Failed to load children: HTTP ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage ?: "Unable to connect"}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchChildren()
    }

    val filteredList = children.filter { c ->
        val matchesSearch = searchQuery.isBlank() ||
                c.fullName.contains(searchQuery, ignoreCase = true) ||
                c.assignedTherapistName.contains(searchQuery, ignoreCase = true) ||
                c.linkedCaregiverName.contains(searchQuery, ignoreCase = true) ||
                c.activeAdlCategory.contains(searchQuery, ignoreCase = true) ||
                c.id.toString() == searchQuery.trim()

        val matchesFilter = when (selectedFilter) {
            ChildStatusFilter.ALL -> true
            ChildStatusFilter.ACTIVE -> c.isActive
            ChildStatusFilter.INACTIVE -> !c.isActive
        }

        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Centralized Child Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { fetchChildren() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = blueAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(16.dp)
        ) {
            GlassTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search Children",
                placeholder = "Search by name, therapist, caregiver, or ID...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedFilter == ChildStatusFilter.ALL,
                    onClick = { selectedFilter = ChildStatusFilter.ALL },
                    label = { Text("All (${children.size})", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedFilter == ChildStatusFilter.ACTIVE,
                    onClick = { selectedFilter = ChildStatusFilter.ACTIVE },
                    label = { Text("Active (${children.count { it.isActive }})", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedFilter == ChildStatusFilter.INACTIVE,
                    onClick = { selectedFilter = ChildStatusFilter.INACTIVE },
                    label = { Text("Inactive (${children.count { !it.isActive }})", fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = blueAccent)
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage!!, color = textSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { fetchChildren() },
                            colors = ButtonDefaults.buttonColors(containerColor = blueAccent)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry")
                        }
                    }
                }
            } else if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No child records found.", color = textSecondary, fontSize = 14.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredList, key = { it.id }) { child ->
                        AdminChildCard(
                            child = child,
                            onClick = { onNavigateToChildDetails(child.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminChildCard(
    child: AdminChildItem,
    onClick: () -> Unit
) {
    val isBoy = child.gender.equals("MALE", ignoreCase = true)
    val avatarDrawable = if (isBoy) R.drawable.boy_avatar else R.drawable.girl_avatar
    val accentColor = if (isBoy) Color(0xFF2563EB) else Color(0xFF7C3AED)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = avatarDrawable),
                contentDescription = child.fullName,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(child.fullName.ifBlank { "Child #${child.id}" }, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Text("ID #${child.id} • Age ${child.age} • ${child.gender}", fontSize = 11.sp, color = Color(0xFF64748B))

                Spacer(modifier = Modifier.height(4.dp))

                Text("Therapist: ${child.assignedTherapistName}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = accentColor)
                Text("Caregiver: ${child.linkedCaregiverName}", fontSize = 11.sp, color = Color(0xFF64748B))
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (child.isActive) accentColor.copy(alpha = 0.1f) else Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = if (child.isActive) "Active" else "Inactive",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (child.isActive) accentColor else Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
