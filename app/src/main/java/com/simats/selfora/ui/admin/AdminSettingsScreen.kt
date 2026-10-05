package com.simats.selfora.ui.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.components.glass.GlassTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    var enableAuditLogging by remember { mutableStateOf(true) }
    var forceFirstLoginPasswordChange by remember { mutableStateOf(true) }
    var preventAdminDeletion by remember { mutableStateOf(true) }

    val darkSlate = Color(0xFF0F172A)
    val goldAccent = Color(0xFFD97706)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Super Admin Settings & Security", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Change Super Admin Password", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = darkSlate)

                    Spacer(modifier = Modifier.height(14.dp))

                    GlassTextField(
                        value = currentPass,
                        onValueChange = { currentPass = it },
                        label = "Current Password",
                        leadingIcon = Icons.Default.Lock,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    GlassTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = "New Admin Password",
                        leadingIcon = Icons.Default.Lock,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    GlassTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        label = "Confirm New Password",
                        leadingIcon = Icons.Default.Lock,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (newPass.length < 6) {
                                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (newPass != confirmPass) {
                                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            Toast.makeText(context, "Super Admin password updated successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = darkSlate),
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update Admin Password", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Security Policies & Audit Controls", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = darkSlate)

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mandatory Audit Logging", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = darkSlate)
                            Text("Record all administrator data modifications", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Switch(checked = enableAuditLogging, onCheckedChange = { enableAuditLogging = it })
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Force First Login Password Change", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = darkSlate)
                            Text("Require newly provisioned clinicians to update temporary password", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Switch(checked = forceFirstLoginPasswordChange, onCheckedChange = { forceFirstLoginPasswordChange = it })
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Protect Super Admin Account Deletion", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = darkSlate)
                            Text("Prevent deletion or deactivation of the primary active administrator", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Switch(checked = preventAdminDeletion, onCheckedChange = { preventAdminDeletion = it })
                    }
                }
            }
        }
    }
}
