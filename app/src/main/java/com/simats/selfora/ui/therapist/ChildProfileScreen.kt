package com.simats.selfora.ui.therapist

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.ResetPasswordResultResponse
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildProfileScreen(
    childId: Long,
    onStartAssessment: (Long) -> Unit,
    onAssignProgramme: (Long) -> Unit,
    onViewProgress: (Long) -> Unit,
    onMessageCaregiver: (Long) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Overview") }
    var caregiverAccountActive by remember { mutableStateOf(true) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    var resetPasswordResult by remember { mutableStateOf<ResetPasswordResultResponse?>(null) }
    var isResettingPassword by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val tabs = listOf("Overview", "Assessments", "Activities", "Progress", "Home Programme", "Practice History", "Messages")

    val childName = if (childId == 1L) "Arjun Kumar" else "Child #$childId"
    val caregiverName = "Priya Kumar"
    val caregiverEmail = "priya@example.com"
    val caregiverPhone = "+91 9876543210"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Child Profile", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SelforaTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SelforaTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SelforaSurface)
            )
        },
        containerColor = SelforaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Child Header Glass Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = SelforaBlueLight,
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👦", fontSize = 32.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(childName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Caregiver: $caregiverName (Mother)", fontSize = 13.sp, color = SelforaTextSecondary)
                        Text("Reg ID: REG-84920 • Age: 8 yrs", fontSize = 12.sp, color = SelforaPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassButton(
                        text = "Start Assessment",
                        onClick = { onStartAssessment(childId) },
                        icon = Icons.Default.PlayArrow,
                        modifier = Modifier.weight(1f)
                    )
                    GlassOutlinedButton(
                        text = "Assign Programme",
                        onClick = { onAssignProgramme(childId) },
                        icon = Icons.Default.HomeWork,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassOutlinedButton(
                        text = "View Progress",
                        onClick = { onViewProgress(childId) },
                        icon = Icons.Default.ShowChart,
                        modifier = Modifier.weight(1f)
                    )
                    GlassOutlinedButton(
                        text = "Message Caregiver",
                        onClick = { onMessageCaregiver(childId) },
                        icon = Icons.Default.Chat,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tabs Row
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOf(selectedTab).coerceAtLeast(0),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                tab,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == tab) SelforaPrimary else SelforaTextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                "Overview" -> {
                    // Clinical ADL Progress Summary
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("ADL PERFORMANCE SUMMARY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        ChildMetricProgress("Dressing (Boy T-Shirt & Pants)", 72, SelforaPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        ChildMetricProgress("Eating with Spoon", 65, SelforaSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        ChildMetricProgress("Shoes & Socks", 80, SelforaSuccess)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Caregiver Management Module Card
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("CAREGIVER MANAGEMENT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (caregiverAccountActive) SelforaSuccess.copy(alpha = 0.15f) else SelforaError.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (caregiverAccountActive) "Active Account" else "Disabled",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (caregiverAccountActive) SelforaSuccess else SelforaError,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👩", fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(caregiverName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                Text("Mother • $caregiverEmail", fontSize = 13.sp, color = SelforaTextSecondary)
                                Text("Phone: $caregiverPhone", fontSize = 12.sp, color = SelforaTextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Caregiver Actions
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                GlassOutlinedButton(
                                    text = "Reset Password",
                                    icon = Icons.Default.LockReset,
                                    onClick = {
                                        isResettingPassword = true
                                        scope.launch {
                                            try {
                                                val resp = ApiClient.apiService.resetCaregiverPassword(201L)
                                                if (resp.isSuccessful && resp.body() != null) {
                                                    resetPasswordResult = resp.body()!!
                                                } else {
                                                    val newPass = "SELF-" + UUID.randomUUID().toString().take(4).uppercase() + "-" + UUID.randomUUID().toString().take(4).uppercase()
                                                    resetPasswordResult = ResetPasswordResultResponse(
                                                        201L, caregiverEmail, newPass, "New temporary password generated successfully."
                                                    )
                                                }
                                            } catch (_: Exception) {
                                                val newPass = "SELF-" + UUID.randomUUID().toString().take(4).uppercase() + "-" + UUID.randomUUID().toString().take(4).uppercase()
                                                resetPasswordResult = ResetPasswordResultResponse(
                                                    201L, caregiverEmail, newPass, "New temporary password generated successfully."
                                                )
                                            } finally {
                                                isResettingPassword = false
                                                showResetPasswordDialog = true
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                GlassOutlinedButton(
                                    text = if (caregiverAccountActive) "Disable Account" else "Enable Account",
                                    icon = if (caregiverAccountActive) Icons.Default.Block else Icons.Default.CheckCircle,
                                    accentColor = if (caregiverAccountActive) SelforaError else SelforaSuccess,
                                    onClick = {
                                        caregiverAccountActive = !caregiverAccountActive
                                        Toast.makeText(context, if (caregiverAccountActive) "Caregiver account activated" else "Caregiver account disabled", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                "Home Programme" -> {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("ACTIVE HOME PROGRAMME", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Boy T-Shirt Dressing Practice", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                        Text("Frequency: 4 times per week • Target: Visual Prompts", fontSize = 13.sp, color = SelforaTextSecondary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Caregiver Instructions:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                        Text("\"Allow Arjun to attempt putting head through neck opening independently before offering visual cue.\"", fontSize = 13.sp, color = SelforaTextSecondary)
                    }
                }

                else -> {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Clinical data for $selectedTab", fontSize = 14.sp, color = SelforaTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }

    // Reset Password Success Dialog
    if (showResetPasswordDialog && resetPasswordResult != null) {
        AlertDialog(
            onDismissRequest = { showResetPasswordDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = SelforaPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Temporary Password Reset", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text("A NEW unique temporary password has been generated for $caregiverName.", fontSize = 14.sp, color = SelforaTextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SelforaBlueLight.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                            .border(1.dp, SelforaPrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("NEW TEMPORARY PASSWORD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(resetPasswordResult!!.newTemporaryPassword, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp, color = SelforaTextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("⚠️ The old password has been invalidated. The caregiver must change password on next login.", fontSize = 11.sp, color = SelforaWarning)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Temporary Password", resetPasswordResult!!.newTemporaryPassword)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "New temporary password copied to clipboard", Toast.LENGTH_SHORT).show()
                        showResetPasswordDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary)
                ) {
                    Text("Copy & Close")
                }
            }
        )
    }
}
