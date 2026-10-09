package com.simats.selfora.ui.admin

import android.widget.Toast
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.TherapistItem
import com.simats.selfora.ui.components.glass.GlassTextField
import kotlinx.coroutines.launch

enum class StatusFilter { ALL, ACTIVE, INACTIVE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapistManagementScreen(
    onNavigateToCreateTherapist: () -> Unit,
    onNavigateToTherapistDetails: (Long) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(StatusFilter.ALL) }
    var therapists by remember { mutableStateOf<List<TherapistItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Status Confirmation Dialog State
    var statusDialogTherapist by remember { mutableStateOf<TherapistItem?>(null) }
    var statusDialogNewState by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val blueAccent = Color(0xFF2563EB)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    fun fetchTherapists() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val response = ApiClient.apiService.getAllTherapists()
                if (response.isSuccessful && response.body() != null) {
                    therapists = response.body()!!
                } else {
                    errorMessage = "Failed to load therapists: HTTP ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage ?: "Unable to connect"}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchTherapists()
    }

    val filteredList = therapists.filter { t ->
        val matchesSearch = searchQuery.isBlank() ||
                t.fullName.contains(searchQuery, ignoreCase = true) ||
                t.email.contains(searchQuery, ignoreCase = true) ||
                t.username.contains(searchQuery, ignoreCase = true) ||
                t.specialization.contains(searchQuery, ignoreCase = true) ||
                t.id.toString() == searchQuery.trim()

        val matchesFilter = when (selectedFilter) {
            StatusFilter.ALL -> true
            StatusFilter.ACTIVE -> t.isActive
            StatusFilter.INACTIVE -> !t.isActive
        }

        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Therapist & Doctor Management", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { fetchTherapists() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = blueAccent)
                    }
                    IconButton(onClick = onNavigateToCreateTherapist) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Therapist", tint = blueAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreateTherapist,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Therapist", fontWeight = FontWeight.Bold) },
                containerColor = blueAccent,
                contentColor = Color.White
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
            // Search Field
            GlassTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search Therapists",
                placeholder = "Search by name, email, username, specialization or ID...",
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
                    selected = selectedFilter == StatusFilter.ALL,
                    onClick = { selectedFilter = StatusFilter.ALL },
                    label = { Text("All (${therapists.size})", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedFilter == StatusFilter.ACTIVE,
                    onClick = { selectedFilter = StatusFilter.ACTIVE },
                    label = { Text("Active (${therapists.count { it.isActive }})", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedFilter == StatusFilter.INACTIVE,
                    onClick = { selectedFilter = StatusFilter.INACTIVE },
                    label = { Text("Inactive (${therapists.count { !it.isActive }})", fontSize = 12.sp) }
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
                            onClick = { fetchTherapists() },
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
                    Text("No therapist records found.", color = textSecondary, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList, key = { it.id }) { therapist ->
                        TherapistCard(
                            therapist = therapist,
                            onClick = { onNavigateToTherapistDetails(therapist.id) },
                            onToggleStatus = { newStatus ->
                                statusDialogTherapist = therapist
                                statusDialogNewState = newStatus
                            }
                        )
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Account Status Toggle
    if (statusDialogTherapist != null) {
        val target = statusDialogTherapist!!
        val actionText = if (statusDialogNewState) "activate" else "deactivate"
        AlertDialog(
            onDismissRequest = { statusDialogTherapist = null },
            title = { Text("Confirm Account ${if (statusDialogNewState) "Activation" else "Deactivation"}", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to $actionText the therapist account for '${target.fullName}'? ${if (!statusDialogNewState) "Deactivated therapists will not be able to log in to the system." else ""}")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val therapistId = target.id
                        val newState = statusDialogNewState
                        statusDialogTherapist = null
                        scope.launch {
                            try {
                                val resp = ApiClient.apiService.updateTherapistStatus(therapistId, newState)
                                if (resp.isSuccessful) {
                                    therapists = therapists.map {
                                        if (it.id == therapistId) it.copy(isActive = newState) else it
                                    }
                                    Toast.makeText(context, "Therapist status updated to ${if (newState) "Active" else "Inactive"}", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed: ${resp.message()}", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (statusDialogNewState) blueAccent else MaterialTheme.colorScheme.error)
                ) {
                    Text(if (statusDialogNewState) "Activate" else "Deactivate")
                }
            },
            dismissButton = {
                TextButton(onClick = { statusDialogTherapist = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TherapistCard(
    therapist: TherapistItem,
    onClick: () -> Unit,
    onToggleStatus: (Boolean) -> Unit
) {
    val blueAccent = Color(0xFF2563EB)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), clip = false)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (therapist.isActive) blueAccent.copy(alpha = 0.12f) else Color(0xFFCBD5E1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = if (therapist.isActive) blueAccent else Color(0xFF64748B),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(therapist.fullName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text(therapist.designation.ifBlank { "Occupational Specialist" }, fontSize = 12.sp, color = Color(0xFF64748B))
                    Text("ID: #${therapist.id} • ${therapist.email}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }

                // Active / Inactive Badge & Switch
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = therapist.isActive,
                        onCheckedChange = onToggleStatus,
                        modifier = Modifier.scale(0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Specialization", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text(therapist.specialization.ifBlank { "Pediatrics" }, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Assigned Children", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text("${therapist.assignedChildrenCount} Children", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = blueAccent)
                }
            }
        }
    }
}
