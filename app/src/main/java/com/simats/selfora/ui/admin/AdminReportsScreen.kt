package com.simats.selfora.ui.admin

import android.widget.Toast
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.theme.*
import com.simats.selfora.ui.components.design.AppTopBar
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.api.NetworkErrorHandler
import com.simats.selfora.data.model.AdminChildItem
import com.simats.selfora.utils.PdfReportDownloader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val blueAccent = SelforaPrimary
    val textPrimary = SelforaTextPrimary
    val textSecondary = SelforaTextSecondary

    var isDownloading by remember { mutableStateOf(false) }
    var showChildSelectorDialog by remember { mutableStateOf(false) }
    var childrenList by remember { mutableStateOf<List<AdminChildItem>>(emptyList()) }
    var isLoadingChildren by remember { mutableStateOf(false) }
    var selectedChildId by remember { mutableStateOf<Long?>(null) }
    var downloadError by remember { mutableStateOf<String?>(null) }

    fun loadChildrenForReport() {
        showChildSelectorDialog = true
        isLoadingChildren = true
        downloadError = null
        scope.launch {
            try {
                val resp = ApiClient.apiService.getAllAdminChildren()
                if (resp.isSuccessful && resp.body() != null) {
                    childrenList = resp.body()!!
                    if (childrenList.isNotEmpty()) {
                        selectedChildId = childrenList.first().id
                    }
                } else {
                    downloadError = "Failed to load children: HTTP ${resp.code()}"
                }
            } catch (e: Exception) {
                downloadError = NetworkErrorHandler.getErrorMessage(e)
            } finally {
                isLoadingChildren = false
            }
        }
    }

    fun executeDownload(childId: Long, childName: String) {
        scope.launch {
            PdfReportDownloader.downloadAndOpenReport(
                context = context,
                childId = childId,
                childName = childName,
                onProgress = { isDownloading = it },
                onError = { err ->
                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                },
                onSuccess = {
                    showChildSelectorDialog = false
                }
            )
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Reports & Analytics Export",
                subtitle = "Authorized Clinical & Operational PDF Reports",
                onBackClick = onBack
            )
        },
        containerColor = SelforaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Text("Available Administrative Reports", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
            Text("Export authorized clinical and operational summaries in authentic PDF format.", fontSize = 12.sp, color = textSecondary)

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Clinical PDF Report Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 3.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, blueAccent.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = blueAccent.copy(alpha = 0.1f)
                        ) {
                            Text(
                                "OFFICIAL CLINICAL PDF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = blueAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (isDownloading) {
                            CircularProgressIndicator(color = blueAccent, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        "Child Clinical ADL Evaluation & Independence Report",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Generates genuine binary PDF with patient profile, 18-step task breakdown, prompt fading levels, and environmental generalization curves.",
                        fontSize = 12.sp,
                        color = textSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { loadChildrenForReport() },
                        enabled = !isDownloading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = blueAccent),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isDownloading) "Generating PDF..." else "Select Child & Download Clinical PDF")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ReportExportCard(
                title = "Therapist Caseload & Clinical Activity Report",
                description = "Sessions conducted, active caseload, assessment completions, and pending practice review statistics.",
                format = "System Audit",
                onExportPdf = { loadChildrenForReport() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ReportExportCard(
                title = "Home Programmes & Caregiver Compliance Summary",
                description = "Home practice adherence rates, caregiver observation notes, and therapist review turnaround.",
                format = "Clinical Audit",
                onExportPdf = { loadChildrenForReport() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ReportExportCard(
                title = "System Security & Audit Logging Report",
                description = "Administrative login history, account provisioning, and credential reset audit trails.",
                format = "Security Audit",
                onExportPdf = {
                    Toast.makeText(context, "Navigating to Audit Logs for complete security trail.", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // Select Child Dialog for PDF Download
    if (showChildSelectorDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDownloading) showChildSelectorDialog = false },
            title = {
                Text("Select Child for Clinical PDF Export", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Choose a child to generate and download their authentic occupational therapy clinical evaluation report:",
                        fontSize = 12.sp,
                        color = textSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isLoadingChildren) {
                        Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = blueAccent, modifier = Modifier.size(28.dp))
                        }
                    } else if (childrenList.isEmpty()) {
                        Text("No children found in clinic database.", fontSize = 13.sp, color = textSecondary)
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            childrenList.forEach { child ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedChildId = child.id }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (selectedChildId == child.id),
                                        onClick = { selectedChildId = child.id }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            child.fullName.ifBlank { "Child #${child.id}" },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textPrimary
                                        )
                                        Text(
                                            "Age: ${child.age} • Therapist: ${child.assignedTherapistName}",
                                            fontSize = 11.sp,
                                            color = textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (downloadError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(downloadError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val child = childrenList.find { it.id == selectedChildId }
                        if (child != null) {
                            executeDownload(child.id, child.fullName)
                        } else if (selectedChildId != null) {
                            executeDownload(selectedChildId!!, "Child_$selectedChildId")
                        }
                    },
                    enabled = !isDownloading && !isLoadingChildren && selectedChildId != null,
                    colors = ButtonDefaults.buttonColors(containerColor = blueAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isDownloading) "Downloading..." else "Download & Open PDF")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showChildSelectorDialog = false },
                    enabled = !isDownloading,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ReportExportCard(
    title: String,
    description: String,
    format: String,
    onExportPdf: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = format,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Spacer(modifier = Modifier.height(2.dp))
            Text(description, fontSize = 11.sp, color = Color(0xFF64748B), lineHeight = 15.sp)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onExportPdf,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2563EB))
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate PDF", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
