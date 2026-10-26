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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.CaregiverItem
import com.simats.selfora.data.model.CreateCaregiverRequest
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

    val purpleAccent = Color(0xFF7C3AED)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Active", "Inactive"

    var caregivers by remember { mutableStateOf<List<CaregiverItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showCreateDialog by remember { mutableStateOf(false) }

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
            TopAppBar(
                title = { Text("Caregiver & Parent Accounts", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { loadCaregivers() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = purpleAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("New Caregiver", fontWeight = FontWeight.Bold) },
                containerColor = purpleAccent,
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
            // Search Input
            GlassTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search Caregivers",
                placeholder = "Search by name, ID, email, or child name...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

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
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredList, key = { it.id }) { caregiver ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(3.dp, RoundedCornerShape(20.dp), clip = false)
                                .clickable { onNavigateToCaregiverDetails(caregiver.id) },
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
                                            .background(if (caregiver.isActive) purpleAccent.copy(alpha = 0.12f) else Color(0xFFCBD5E1)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.FamilyRestroom,
                                            contentDescription = null,
                                            tint = if (caregiver.isActive) purpleAccent else Color(0xFF64748B),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = caregiver.fullName,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textPrimary,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (caregiver.isActive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                            ) {
                                                Text(
                                                    text = if (caregiver.isActive) "ACTIVE" else "INACTIVE",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (caregiver.isActive) Color(0xFF166534) else Color(0xFF991B1B),
                                                    softWrap = false,
                                                    maxLines = 1,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Caregiver ID: #${caregiver.id}",
                                            fontSize = 11.sp,
                                            color = textSecondary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Switch(
                                        checked = caregiver.isActive,
                                        onCheckedChange = { caregiverToToggle = caregiver },
                                        modifier = Modifier.    scale(0.85f),
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF10B981)
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Email Address", fontSize = 10.sp, color = textSecondary)
                                        Text(
                                            text = caregiver.email.ifBlank { "N/A" },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = textPrimary,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Linked Children", fontSize = 10.sp, color = textSecondary)
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
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
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
                Text("Are you sure you want to ${if (newStatus) "activate" else "deactivate"} account for '${target.fullName}'? Child records will NOT be deleted.")
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

    // Create Caregiver Dialog Form
    if (showCreateDialog) {
        var fullName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showCreateDialog = false },
            title = { Text("Create Caregiver Account", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().imePadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Full Name *",
                        leadingIcon = Icons.Default.Person,
                        modifier = Modifier.fillMaxWidth()
                    )
                    GlassTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address *",
                        leadingIcon = Icons.Default.Email,
                        modifier = Modifier.fillMaxWidth()
                    )
                    GlassTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone Number",
                        leadingIcon = Icons.Default.Phone,
                        modifier = Modifier.fillMaxWidth()
                    )
                    GlassTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Username *",
                        leadingIcon = Icons.Default.AccountCircle,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Initial Password *") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
                        if (fullName.isBlank() || email.isBlank() || username.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Please fill in all required fields (*).", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!email.trim().matches(emailPattern)) {
                            Toast.makeText(context, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSubmitting = true
                        scope.launch {
                            try {
                                val req = CreateCaregiverRequest(
                                    fullName = fullName.trim(),
                                    email = email.trim(),
                                    phone = phone.trim(),
                                    username = username.trim(),
                                    password = password
                                )
                                val resp = ApiClient.apiService.createCaregiver(req)
                                if (resp.isSuccessful && resp.body() != null) {
                                    Toast.makeText(context, "Caregiver account created successfully!", Toast.LENGTH_SHORT).show()
                                    showCreateDialog = false
                                    loadCaregivers()
                                } else {
                                    val err = resp.errorBody()?.string() ?: "HTTP ${resp.code()}"
                                    Toast.makeText(context, "Creation failed: $err", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error creating account: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            } finally {
                                isSubmitting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = purpleAccent),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Create Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }, enabled = !isSubmitting) {
                    Text("Cancel")
                }
            }
        )
    }
}
