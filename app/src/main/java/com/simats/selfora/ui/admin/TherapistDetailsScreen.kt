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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapistDetailsScreen(
    therapistId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val blueAccent = Color(0xFF2563EB)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    var showReassignDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Therapist Clinical Profile", fontWeight = FontWeight.Bold) },
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
                            .background(blueAccent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MedicalServices, contentDescription = null, tint = blueAccent, modifier = Modifier.size(36.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Dr. Sarah Jenkins (OT)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    Text("Senior Occupational Therapist • ID #1002", fontSize = 12.sp, color = textSecondary)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { Toast.makeText(context, "Password reset link sent to sarah.jenkins@selfora.org", Toast.LENGTH_SHORT).show() },
                            colors = ButtonDefaults.buttonColors(containerColor = blueAccent),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Password", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showReassignDialog = true },
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reassign Children", fontSize = 12.sp)
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

                    DetailRow("Email", "sarah.jenkins@selfora.org")
                    DetailRow("Phone", "+91 9876543210")
                    DetailRow("Specialization", "Pediatric ADL & Fine Motor Skills")
                    DetailRow("Qualifications", "BOT, MOT (Pediatrics)")
                    DetailRow("Clinical Experience", "8 Years")
                    DetailRow("Account Status", "Active Clinician")
                    DetailRow("Registration Date", "January 14, 2026")
                    DetailRow("Assigned Active Children", "12 Children")
                    DetailRow("Completed Sessions", "156 Therapy Sessions")
                }
            }
        }
    }

    if (showReassignDialog) {
        AlertDialog(
            onDismissRequest = { showReassignDialog = false },
            title = { Text("Reassign Children", fontWeight = FontWeight.Bold) },
            text = {
                Text("Select target clinician to transfer clinical caseload with audit log tracking.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReassignDialog = false
                        Toast.makeText(context, "Children reassigned with audit log record", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = blueAccent)
                ) {
                    Text("Reassign Caseload")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReassignDialog = false }) {
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
