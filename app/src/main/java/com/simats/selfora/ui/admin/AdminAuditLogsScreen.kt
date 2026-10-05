package com.simats.selfora.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.AuditLogItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAuditLogsScreen(
    onBack: () -> Unit
) {
    var logs by remember { mutableStateOf<List<AuditLogItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val defaultLogs = remember {
        listOf(
            AuditLogItem(101L, 4L, "ROLE_SUPER_ADMIN", "CREATE_THERAPIST", "Therapist ID #4", "Provisioned Dr. Vikram Seth (OT) with initial credentials", "2026-10-03 14:30:12"),
            AuditLogItem(102L, 4L, "ROLE_SUPER_ADMIN", "ACTIVATE_THERAPIST", "Therapist ID #2", "Re-activated clinician account status for Dr. Rajesh Kumar", "2026-10-03 13:15:45"),
            AuditLogItem(103L, 1L, "ROLE_THERAPIST", "LOGIN_SUCCESS", "User ID #1", "Successful authentication token issued", "2026-10-03 11:42:00"),
            AuditLogItem(104L, 4L, "ROLE_SUPER_ADMIN", "REASSIGN_CHILDREN", "Child ID #1", "Reassigned clinical caseload for Aarav Sharma", "2026-10-03 10:20:18"),
            AuditLogItem(105L, 2L, "ROLE_CAREGIVER", "SUBMIT_PRACTICE", "Practice Session #14", "Submitted home practice video for dressing T-shirt step 3", "2026-10-03 09:10:05")
        )
    }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val response = ApiClient.apiService.getAuditLogs()
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                logs = response.body()!!
            } else {
                logs = defaultLogs
            }
        } catch (e: Exception) {
            logs = defaultLogs
        } finally {
            isLoading = false
        }
    }

    val darkSlate = Color(0xFF0F172A)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Activity & Audit Logs", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(logs) { item ->
                Surface(
                    modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(darkSlate.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.History, contentDescription = null, tint = darkSlate, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(item.action, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = darkSlate)
                            }
                            Text(item.timestamp, fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Actor: ID #${item.actorId} (${item.actorRole})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB))
                        Text("Entity: ${item.affectedEntity}", fontSize = 11.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Summary: ${item.changeSummary}", fontSize = 12.sp, color = Color(0xFF334155))
                    }
                }
            }
        }
    }
}
