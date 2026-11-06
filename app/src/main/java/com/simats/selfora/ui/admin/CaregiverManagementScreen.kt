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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.theme.*
import com.simats.selfora.ui.components.design.AppTopBar
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.CaregiverItem
import com.simats.selfora.ui.components.glass.GlassTextField
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverManagementScreen(
    onNavigateToCaregiverDetails: (Long) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val purpleAccent = SelforaSecondary
    val textPrimary = SelforaTextPrimary
    val textSecondary = SelforaTextSecondary

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Active", "Inactive"

    var caregivers by remember { mutableStateOf<List<CaregiverItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Status Toggle Dialog State
    var caregiverToToggle by remember { mutableStateOf<CaregiverItem?>(null) }
    var isTogglingStatus by remember { mutableStateOf(false) }

    fun loadCaregivers() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val resp = ApiClient.apiService.getAllCaregivers()
                if (resp.isSuccessful && resp.body() != null) {
                    caregivers = resp.body()!!
                } else {
                    errorMessage = "Failed to load caregivers: HTTP ${resp.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadCaregivers()
    }

    val filteredList = caregivers.filter { item ->
        val matchesFilter = when (selectedFilter) {
            "Active" -> item.isActive
            "Inactive" -> !item.isActive
            else -> true
        }
        val query = searchQuery.trim().lowercase()
        val matchesSearch = query.isEmpty() ||
                item.fullName.lowercase().contains(query) ||
                item.email.lowercase().contains(query) ||
                item.id.toString().contains(query) ||
                item.linkedChildrenNames.any { it.lowercase().contains(query) }

        matchesFilter && matchesSearch
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Caregiver & Parent Accounts",
                subtitle = "Linked Parent Logins & Access Status",
                accentColor = purpleAccent,
                actions = {
                    IconButton(onClick = { loadCaregivers() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = purpleAccent)
                    }
                }
            )
        },
        containerColor = SelforaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Informational Notice Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(14.dp),
                color = purpleAccent.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, purpleAccent.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = purpleAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Caregiver accounts are registered by therapists during child intake. Admin can view accounts, manage access status, and reset passwords.",
                        fontSize = 11.5.sp,
                        color = Color(0xFF581C87),
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Search Input
            GlassTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search Caregivers",
                placeholder = "Search by name, ID, email, or child name...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Active", "Inactive").forEach { filterOption ->
                    FilterChip(
                        selected = selectedFilter == filterOption,
                        onClick = { selectedFilter = filterOption },
                        label = { Text(filterOption, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = purpleAccent,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = textSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Content States
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = purpleAccent)
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(errorMessage!!, color = textSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { loadCaregivers() }, colors = ButtonDefaults.buttonColors(containerColor = purpleAccent)) {
                            Text("Retry")
                        }
                    }
                }
            } else if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No caregivers found.", fontWeight = FontWeight.Bold, color = textPrimary, fontSize = 16.sp)
                        Text("Try adjusting your search query or status filter.", color = textSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredList, key = { it.id }) { caregiver ->
                        CaregiverCard(
                            caregiver = caregiver,
                            purpleAccent = purpleAccent,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = { onNavigateToCaregiverDetails(caregiver.id) },
                            onToggleStatus = { caregiverToToggle = caregiver }
                        )
                    }
                }
            }
        }
    }

    // Toggle Caregiver Status Confirmation Dialog
    if (caregiverToToggle != null) {
        val target = caregiverToToggle!!
        val newStatus = !target.isActive

        AlertDialog(
            onDismissRequest = { if (!isTogglingStatus) caregiverToToggle = null },
            title = { Text(if (newStatus) "Activate Caregiver Account?" else "Deactivate Caregiver Account?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to ${if (newStatus) "activate" else "deactivate"} account for '${target.fullName}'? Child records and history will NOT be deleted.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        isTogglingStatus = true
                        scope.launch {
                            try {
                                val resp = ApiClient.apiService.updateCaregiverStatus(target.id, newStatus)
                                if (resp.isSuccessful && resp.body() != null) {
                                    caregivers = caregivers.map { if (it.id == target.id) resp.body()!! else it }
                                    Toast.makeText(context, "Caregiver status updated successfully!", Toast.LENGTH_SHORT).show()
                                    caregiverToToggle = null
                                } else {
                                    Toast.makeText(context, "Status update failed: HTTP ${resp.code()}", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Network error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            } finally {
                                isTogglingStatus = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (newStatus) Color(0xFF10B981) else Color(0xFFEF4444)
                    ),
                    enabled = !isTogglingStatus
                ) {
                    if (isTogglingStatus) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text(if (newStatus) "Activate" else "Deactivate")
                }
            },
            dismissButton = {
                TextButton(onClick = { caregiverToToggle = null }, enabled = !isTogglingStatus) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CaregiverCard(
    caregiver: CaregiverItem,
    purpleAccent: Color,
    textPrimary: Color,
    textSecondary: Color,
    onClick: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp), clip = false)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Avatar + Info + Badge/Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (caregiver.isActive) purpleAccent.copy(alpha = 0.12f) else Color(0xFFCBD5E1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FamilyRestroom,
                        contentDescription = null,
                        tint = if (caregiver.isActive) purpleAccent else Color(0xFF64748B),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = caregiver.fullName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ID: #${caregiver.id} • ${caregiver.username}",
                        fontSize = 11.5.sp,
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = caregiver.email.ifBlank { "No email address" },
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Status Badge & Clean Switch
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (caregiver.isActive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                    ) {
                        Text(
                            text = if (caregiver.isActive) "ACTIVE" else "INACTIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (caregiver.isActive) Color(0xFF166534) else Color(0xFF991B1B),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    Switch(
                        checked = caregiver.isActive,
                        onCheckedChange = { onToggleStatus() },
                        modifier = Modifier.scale(0.8f),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Info Row: Phone Number & Linked Children (Balanced 50/50 weights)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(0.5f)) {
                    Text(
                        text = "PHONE NUMBER",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = caregiver.phone.ifBlank { "Not Provided" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(0.5f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "LINKED CHILDREN",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    val childrenLabel = if (caregiver.linkedChildrenNames.isNotEmpty()) {
                        caregiver.linkedChildrenNames.joinToString(", ")
                    } else {
                        "${caregiver.linkedChildrenCount} Linked"
                    }
                    Text(
                        text = childrenLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = purpleAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
