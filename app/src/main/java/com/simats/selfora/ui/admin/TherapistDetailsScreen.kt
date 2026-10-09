package com.simats.selfora.ui.admin

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.TherapistItem
import com.simats.selfora.data.model.UpdateTherapistRequest
import com.simats.selfora.ui.components.glass.GlassTextField
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapistDetailsScreen(
    therapistId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val blueAccent = Color(0xFF2563EB)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    var therapist by remember { mutableStateOf<TherapistItem?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showEditDialog by remember { mutableStateOf(false) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }

    fun loadProfile() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val resp = ApiClient.apiService.getTherapistById(therapistId)
                if (resp.isSuccessful && resp.body() != null) {
                    therapist = resp.body()
                } else {
                    errorMessage = "Failed to load profile: HTTP ${resp.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(therapistId) {
        loadProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Therapist Clinical Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (therapist != null) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = blueAccent)
                        }
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
        } else if (therapist != null) {
            val t = therapist!!
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
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(if (t.isActive) blueAccent.copy(alpha = 0.12f) else Color(0xFFCBD5E1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = if (t.isActive) blueAccent else Color(0xFF64748B),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(t.fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Text("${t.designation.ifBlank { "Occupational Specialist" }} • ID #${t.id}", fontSize = 12.sp, color = textSecondary)
                        Text("Username: ${t.username.ifBlank { "therapist_${t.id}" }}", fontSize = 11.sp, color = Color(0xFF94A3B8))

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { showResetPasswordDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = blueAccent),
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reset Password", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { showEditDialog = true },
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Profile", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Information Detail Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Professional & Clinical Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)

                        Spacer(modifier = Modifier.height(16.dp))

                        DetailRow("Therapist ID", "#${t.id}")
                        DetailRow("Full Name", t.fullName)
                        DetailRow("Email", t.email)
                        DetailRow("Phone", t.phone)
                        DetailRow("Designation", t.designation.ifBlank { "Senior Occupational Therapist" })
                        DetailRow("Specialization", t.specialization.ifBlank { "Pediatric ADL & Fine Motor Skills" })
                        DetailRow("Qualifications", t.qualification.ifBlank { "BOT, MOT (Pediatrics)" })
                        DetailRow("Clinical Experience", t.experience.ifBlank { "5 Years" })
                        DetailRow("Account Status", if (t.isActive) "Active Account" else "Deactivated Account")
                        DetailRow("Password Reset Required", if (t.mustChangePassword) "Yes (First Login)" else "No")
                        DetailRow("Assigned Active Children", "${t.assignedChildrenCount} Children")
                    }
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditDialog && therapist != null) {
        var editName by remember { mutableStateOf(therapist!!.fullName) }
        var editEmail by remember { mutableStateOf(therapist!!.email) }
        var editPhone by remember { mutableStateOf(therapist!!.phone) }
        var editDesignation by remember { mutableStateOf(therapist!!.designation) }
        var editSpecialization by remember { mutableStateOf(therapist!!.specialization) }
        var editQualification by remember { mutableStateOf(therapist!!.qualification) }
        var editExperience by remember { mutableStateOf(therapist!!.experience) }
        var isSaving by remember { mutableStateOf(false) }

        AlertDialog(
            modifier = Modifier.imePadding(),
            onDismissRequest = { if (!isSaving) showEditDialog = false },
            title = { Text("Edit Therapist Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .imePadding()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassTextField(value = editName, onValueChange = { editName = it }, label = "Full Name")
                    GlassTextField(value = editEmail, onValueChange = { editEmail = it }, label = "Email")
                    GlassTextField(value = editPhone, onValueChange = { editPhone = it }, label = "Phone")
                    GlassTextField(value = editDesignation, onValueChange = { editDesignation = it }, label = "Designation")
                    GlassTextField(value = editSpecialization, onValueChange = { editSpecialization = it }, label = "Specialization")
                    GlassTextField(value = editQualification, onValueChange = { editQualification = it }, label = "Qualification")
                    GlassTextField(value = editExperience, onValueChange = { editExperience = it }, label = "Experience")
                }
            },
            confirmButton = {
                Button(
                    enabled = !isSaving,
                    onClick = {
                        if (editName.isBlank() || editEmail.isBlank()) {
                            Toast.makeText(context, "Name and Email are required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSaving = true
                        scope.launch {
                            try {
                                val resp = ApiClient.apiService.updateTherapist(
                                    therapistId,
                                    UpdateTherapistRequest(
                                        fullName = editName.trim(),
                                        email = editEmail.trim(),
                                        phone = editPhone.trim(),
                                        designation = editDesignation.trim(),
                                        specialization = editSpecialization.trim(),
                                        qualification = editQualification.trim(),
                                        experience = editExperience.trim()
                                    )
                                )
                                if (resp.isSuccessful && resp.body() != null) {
                                    therapist = resp.body()
                                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                    showEditDialog = false
                                } else {
                                    Toast.makeText(context, "Update failed: ${resp.message()}", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = blueAccent)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Password Dialog
    if (showResetPasswordDialog && therapist != null) {
        var newPassText by remember { mutableStateOf("password123") }
        var isResetting by remember { mutableStateOf(false) }

        AlertDialog(
            modifier = Modifier.imePadding(),
            onDismissRequest = { if (!isResetting) showResetPasswordDialog = false },
            title = { Text("Reset Therapist Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.imePadding()) {
                    Text("Provision a new temporary password for '${therapist!!.fullName}'. The user will be required to change password upon next login.")
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassTextField(
                        value = newPassText,
                        onValueChange = { newPassText = it },
                        label = "New Password",
                        placeholder = "password123"
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !isResetting,
                    onClick = {
                        isResetting = true
                        scope.launch {
                            try {
                                val resp = ApiClient.apiService.resetTherapistPassword(therapistId, newPassText.trim())
                                if (resp.isSuccessful) {
                                    Toast.makeText(context, "Password reset successfully!", Toast.LENGTH_SHORT).show()
                                    showResetPasswordDialog = false
                                    loadProfile()
                                } else {
                                    Toast.makeText(context, "Reset failed: ${resp.message()}", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            } finally {
                                isResetting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = blueAccent)
                ) {
                    Text("Confirm Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
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
