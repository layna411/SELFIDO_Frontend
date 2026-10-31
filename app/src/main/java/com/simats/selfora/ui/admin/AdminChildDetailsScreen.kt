package com.simats.selfora.ui.admin

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.R
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.api.NetworkErrorHandler
import com.simats.selfora.data.model.AdminChildDetailProfile
import com.simats.selfora.data.model.ChildReassignRequest
import com.simats.selfora.data.model.TherapistItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChildDetailsScreen(
    childId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val blueAccent = Color(0xFF2563EB)
    val textSecondary = Color(0xFF64748B)

    var profile by remember { mutableStateOf<AdminChildDetailProfile?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Reassignment dialog states
    var showReassignDialog by remember { mutableStateOf(false) }
    var availableTherapists by remember { mutableStateOf<List<TherapistItem>>(emptyList()) }
    var isLoadingTherapists by remember { mutableStateOf(false) }
    var selectedTherapistId by remember { mutableStateOf<Long?>(null) }
    var reassignmentReason by remember { mutableStateOf("") }
    var isSubmittingReassign by remember { mutableStateOf(false) }
    var reassignError by remember { mutableStateOf<String?>(null) }

    fun loadProfile() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val resp = ApiClient.apiService.getAdminChildDetails(childId)
                if (resp.isSuccessful && resp.body() != null) {
                    profile = resp.body()
                } else {
                    errorMessage = NetworkErrorHandler.getHttpErrorMessage(resp.code())
                }
            } catch (e: Exception) {
                errorMessage = NetworkErrorHandler.getErrorMessage(e)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(childId) {
        loadProfile()
    }

    fun openReassignDialog() {
        showReassignDialog = true
        reassignError = null
        reassignmentReason = ""
        selectedTherapistId = null
        isLoadingTherapists = true

        scope.launch {
            try {
                val resp = ApiClient.apiService.getAllTherapists()
                if (resp.isSuccessful && resp.body() != null) {
                    val currentTherapistId = profile?.summary?.assignedTherapistId
                    // Filter for active therapists different from the current one
                    availableTherapists = resp.body()!!.filter {
                        it.isActive && (currentTherapistId == null || it.id != currentTherapistId)
                    }
                    if (availableTherapists.isNotEmpty()) {
                        selectedTherapistId = availableTherapists.first().id
                    }
                } else {
                    reassignError = "Failed to load therapists: HTTP ${resp.code()}"
                }
            } catch (e: Exception) {
                reassignError = NetworkErrorHandler.getErrorMessage(e)
            } finally {
                isLoadingTherapists = false
            }
        }
    }

    fun submitReassignment() {
        if (selectedTherapistId == null) {
            reassignError = "Please select a target therapist."
            return
        }
        if (reassignmentReason.trim().isEmpty()) {
            reassignError = "Please provide a valid clinical or administrative reason."
            return
        }

        isSubmittingReassign = true
        reassignError = null

        scope.launch {
            try {
                val req = ChildReassignRequest(
                    childId = childId,
                    newTherapistId = selectedTherapistId!!,
                    reason = reassignmentReason.trim()
                )
                val resp = ApiClient.apiService.reassignChildTherapist(childId, req)
                if (resp.isSuccessful && resp.body() != null) {
                    Toast.makeText(context, "Therapist reassigned successfully!", Toast.LENGTH_SHORT).show()
                    profile = resp.body()
                    showReassignDialog = false
                } else {
                    reassignError = NetworkErrorHandler.getHttpErrorMessage(resp.code())
                }
            } catch (e: Exception) {
                reassignError = NetworkErrorHandler.getErrorMessage(e)
            } finally {
                isSubmittingReassign = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Child Clinical File", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { loadProfile() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = blueAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = blueAccent)
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, color = textSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { loadProfile() }, colors = ButtonDefaults.buttonColors(containerColor = blueAccent)) {
                        Text("Retry")
                    }
                }
            }
        } else if (profile != null) {
            val summary = profile!!.summary
            val isBoy = summary.gender.equals("MALE", ignoreCase = true)
            val avatarDrawable = if (isBoy) R.drawable.boy_avatar else R.drawable.girl_avatar
            val accentColor = if (isBoy) Color(0xFF2563EB) else Color(0xFF7C3AED)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                // Profile Header Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = avatarDrawable),
                            contentDescription = summary.fullName,
                            modifier = Modifier.size(80.dp).clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(summary.fullName.ifBlank { "Child #${summary.id}" }, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text("Child ID #${summary.id} • Age ${summary.age} • ${summary.gender}", fontSize = 12.sp, color = textSecondary)

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (summary.isActive) accentColor.copy(alpha = 0.1f) else Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = if (summary.isActive) "Active Profile" else "Inactive Profile",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                color = if (summary.isActive) accentColor else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Personnel & Activity Card with Reassign Button
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Linked Personnel & Active Programmes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Assigned Therapist with Reassign Action
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Assigned Therapist", fontSize = 12.sp, color = textSecondary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(summary.assignedTherapistName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                }
                                OutlinedButton(
                                    onClick = { openReassignDialog() },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = blueAccent)
                                ) {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reassign", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }

                        AdminDetailRow("Linked Caregiver", summary.linkedCaregiverName)
                        AdminDetailRow("Active ADL Programme", profile!!.activeHomeProgramTitle.ifBlank { "Dressing - Upper Body T-Shirt" })
                        AdminDetailRow("Diagnosis Notes", profile!!.diagnosisNotes.ifBlank { "No diagnosis notes recorded." })
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ADL Categories Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Supported ADL Categories", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            profile!!.supportedAdlCategories.forEach { cat ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Clinical & Home Metrics Overview (Separated Metrics)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Clinical & Home Record Metrics", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(12.dp))

                        AdminDetailRow("Therapy Sessions Count", "${profile!!.totalSessions} Sessions")
                        AdminDetailRow("Completed Assessments", "${profile!!.completedAssessments} Assessments")
                        AdminDetailRow("Home Practice Sessions", "${profile!!.homePracticeSessionsCount} Completed Practices")
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Reassign Therapist Dialog
    if (showReassignDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSubmittingReassign) showReassignDialog = false },
            title = {
                Text(
                    text = "Reassign Child's Therapist",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Transfer clinical caseload responsibility for ${profile?.summary?.fullName ?: "this child"} to an active therapist:",
                        fontSize = 13.sp,
                        color = textSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoadingTherapists) {
                        Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = blueAccent, modifier = Modifier.size(28.dp))
                        }
                    } else if (availableTherapists.isEmpty()) {
                        Text(
                            text = "No other active therapists found to transfer to.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(
                            text = "Select New Therapist:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Scrollable radio list of therapists
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 160.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            availableTherapists.forEach { therapist ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedTherapistId = therapist.id }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (selectedTherapistId == therapist.id),
                                        onClick = { selectedTherapistId = therapist.id }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = therapist.fullName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = therapist.designation,
                                            fontSize = 11.sp,
                                            color = textSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Reason for Reassignment (Required):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = reassignmentReason,
                            onValueChange = { reassignmentReason = it },
                            placeholder = { Text("e.g. Clinical caseload rebalancing or therapist leave") },
                            modifier = Modifier.fillMaxWidth().height(90.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = blueAccent,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )
                    }

                    if (reassignError != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = reassignError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { submitReassignment() },
                    enabled = !isSubmittingReassign && !isLoadingTherapists && availableTherapists.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = blueAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isSubmittingReassign) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reassigning...")
                    } else {
                        Text("Confirm Reassignment")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showReassignDialog = false },
                    enabled = !isSubmittingReassign,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AdminDetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, color = Color(0xFF64748B))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
        }
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = Color(0xFFF1F5F9))
    }
}
