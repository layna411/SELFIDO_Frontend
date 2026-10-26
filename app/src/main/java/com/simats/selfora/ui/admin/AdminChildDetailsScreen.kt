package com.simats.selfora.ui.admin

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.R
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.AdminChildDetailProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChildDetailsScreen(
    childId: Long,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val blueAccent = Color(0xFF2563EB)
    val textSecondary = Color(0xFF64748B)

    var profile by remember { mutableStateOf<AdminChildDetailProfile?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadProfile() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val resp = ApiClient.apiService.getAdminChildDetails(childId)
                if (resp.isSuccessful && resp.body() != null) {
                    profile = resp.body()
                } else {
                    errorMessage = "Failed to load child profile: HTTP ${resp.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(childId) {
        loadProfile()
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

                // Personnel & Activity Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Linked Personnel & Active Programmes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(12.dp))

                        AdminDetailRow("Assigned Therapist", summary.assignedTherapistName)
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
            }
        }
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
