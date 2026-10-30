package com.simats.selfora.ui.admin

import android.widget.Toast
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.CaregiverItem
import com.simats.selfora.data.model.ResetPasswordRequest
import com.simats.selfora.data.model.UpdateCaregiverRequest
import com.simats.selfora.ui.components.glass.GlassTextField
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverDetailsScreen(
    caregiverId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val purpleAccent = Color(0xFF7C3AED)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    var caregiver by remember { mutableStateOf<CaregiverItem?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showEditDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }

    fun loadProfile() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val resp = ApiClient.apiService.getCaregiverById(caregiverId)
                if (resp.isSuccessful && resp.body() != null) {
                    caregiver = resp.body()
                } else {
                    errorMessage = "Failed to load caregiver profile: HTTP ${resp.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(caregiverId) {
        loadProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Caregiver Profile Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (caregiver != null) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = purpleAccent)
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
                CircularProgressIndicator(color = purpleAccent)
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(errorMessage!!, color = textSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { loadProfile() }, colors = ButtonDefaults.buttonColors(containerColor = purpleAccent)) {
                        Text("Retry")
                    }
                }
            }
        } else if (caregiver != null) {
            val c = caregiver!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Profile Card
                Surface(
                    modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(72.dp).clip(CircleShape).background(purpleAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = purpleAccent, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(c.fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Text("Caregiver Account ID: #${c.id}", fontSize = 12.sp, color = textSecondary)

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (c.isActive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                            ) {
                                Text(
                                    text = if (c.isActive) "ACTIVE ACCOUNT" else "INACTIVE / DISABLED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (c.isActive) Color(0xFF166534) else Color(0xFF991B1B),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            if (c.mustChangePassword) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFEF3C7)
                                ) {
                                    Text(
                                        text = "MUST CHANGE PASS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Contact & Account Details Card
                Surface(
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Contact & Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Divider(color = Color(0xFFF1F5F9))

                        DetailRow(icon = Icons.Default.Person, label = "Username", value = c.username.ifBlank { "N/A" })
                        DetailRow(icon = Icons.Default.Email, label = "Email Address", value = c.email.ifBlank { "N/A" })
                        DetailRow(icon = Icons.Default.Phone, label = "Phone Number", value = c.phone.ifBlank { "N/A" })
                        DetailRow(icon = Icons.Default.ChildCare, label = "Linked Children Count", value = "${c.linkedChildrenCount} Linked Child(ren)")
                    }
                }

                // Linked Children Section
                Surface(
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Linked Children Records", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = purpleAccent.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    "${c.linkedChildren.size} Registered",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = purpleAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Divider(color = Color(0xFFF1F5F9))

                        if (c.linkedChildren.isEmpty()) {
                            Text(
                                "No children linked to this caregiver account yet.",
                                fontSize = 13.sp,
                                color = textSecondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            c.linkedChildren.forEach { child ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF8FAFC),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(40.dp).clip(CircleShape).background(purpleAccent.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.ChildCare, contentDescription = null, tint = purpleAccent)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(child.fullName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                            Text("Assigned Therapist: ${child.assignedTherapistName}", fontSize = 11.sp, color = textSecondary)
                                            Text("Active Program: ${child.activeAdlCategory}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = purpleAccent)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Admin Action Buttons
                Surface(
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Administrative Controls", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Divider(color = Color(0xFFF1F5F9))

                        Button(
                            onClick = { showStatusDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (c.isActive) Color(0xFFEF4444) else Color(0xFF10B981)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                if (c.isActive) Icons.Default.Block else Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (c.isActive) "Deactivate Caregiver Account" else "Activate Caregiver Account")
                        }

                        OutlinedButton(
                            onClick = { showResetPasswordDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = purpleAccent)
                        ) {
                            Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Password")
                        }
                    }
                }
            }
        }
    }

    // Edit Profile Modal Dialog
    if (showEditDialog && caregiver != null) {
        val c = caregiver!!
        var fullName by remember { mutableStateOf(c.fullName) }
        var email by remember { mutableStateOf(c.email) }
        var phone by remember { mutableStateOf(c.phone) }
        var isSaving by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSaving) showEditDialog = false },
            title = { Text("Edit Caregiver Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().imePadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Full Name",
                        leadingIcon = Icons.Default.Person,
                        modifier = Modifier.fillMaxWidth()
                    )
                    GlassTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fullName.isBlank() || email.isBlank()) {
                            Toast.makeText(context, "Full Name and Email are required.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSaving = true
                        scope.launch {
                            try {
                                val req = UpdateCaregiverRequest(fullName = fullName.trim(), email = email.trim(), phone = phone.trim())
                                val resp = ApiClient.apiService.updateCaregiver(caregiverId, req)
                                if (resp.isSuccessful && resp.body() != null) {
                                    caregiver = resp.body()
                                    Toast.makeText(context, "Caregiver profile updated successfully!", Toast.LENGTH_SHORT).show()
                                    showEditDialog = false
                                } else {
                                    Toast.makeText(context, "Failed to update profile: HTTP ${resp.code()}", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error updating profile: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = purpleAccent),
                    enabled = !isSaving
                ) {
                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }, enabled = !isSaving) {
                    Text("Cancel")
                }
            }
        )
    }

    // Status Toggle Dialog
    if (showStatusDialog && caregiver != null) {
        val c = caregiver!!
        val newStatus = !c.isActive
        var isUpdating by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isUpdating) showStatusDialog = false },
            title = { Text(if (newStatus) "Activate Caregiver?" else "Deactivate Caregiver?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (newStatus) "Activating this caregiver will allow them to log into the SELFORA application."
                    else "Deactivating this caregiver will prevent them from logging in. Note: Child records and data will NOT be deleted."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isUpdating = true
                        scope.launch {
                            try {
                                val resp = ApiClient.apiService.updateCaregiverStatus(caregiverId, newStatus)
                                if (resp.isSuccessful && resp.body() != null) {
                                    caregiver = resp.body()
                                    Toast.makeText(context, "Caregiver status updated!", Toast.LENGTH_SHORT).show()
                                    showStatusDialog = false
                                } else {
                                    Toast.makeText(context, "Status update failed: HTTP ${resp.code()}", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Network error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            } finally {
                                isUpdating = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (newStatus) Color(0xFF10B981) else Color(0xFFEF4444)
                    ),
                    enabled = !isUpdating
                ) {
                    if (isUpdating) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text(if (newStatus) "Activate" else "Deactivate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStatusDialog = false }, enabled = !isUpdating) {
                    Text("Cancel")
                }
            }
        )
    }

    // Password Reset Dialog (Request Body)
    if (showResetPasswordDialog && caregiver != null) {
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var isResetting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isResetting) showResetPasswordDialog = false },
            title = { Text("Reset Caregiver Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().imePadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("This resets the password via a validated request body and sets mustChangePassword = true.", fontSize = 12.sp, color = textSecondary)

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPassword.isBlank() || newPassword.length < 6) {
                            Toast.makeText(context, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (newPassword != confirmPassword) {
                            Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isResetting = true
                        scope.launch {
                            try {
                                val req = ResetPasswordRequest(newPassword = newPassword)
                                val resp = ApiClient.apiService.resetCaregiverPasswordBody(caregiverId, req)
                                if (resp.isSuccessful) {
                                    Toast.makeText(context, "Password reset successfully!", Toast.LENGTH_LONG).show()
                                    showResetPasswordDialog = false
                                    loadProfile()
                                } else {
                                    Toast.makeText(context, "Failed to reset password: HTTP ${resp.code()}", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error resetting password: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            } finally {
                                isResetting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = purpleAccent),
                    enabled = !isResetting
                ) {
                    if (isResetting) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Reset Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPasswordDialog = false }, enabled = !isResetting) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, fontSize = 13.sp, color = Color(0xFF64748B), modifier = Modifier.weight(0.42f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A), modifier = Modifier.weight(0.58f))
    }
}
