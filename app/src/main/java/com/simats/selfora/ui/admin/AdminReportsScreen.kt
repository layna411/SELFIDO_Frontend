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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val amberAccent = Color(0xFFD97706)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Analytics Export", fontWeight = FontWeight.Bold) },
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
            Text("Available Administrative Reports", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
            Text("Export authorized clinical and operational summaries in PDF/Excel format.", fontSize = 12.sp, color = textSecondary)

            Spacer(modifier = Modifier.height(16.dp))

            ReportExportCard(
                title = "Therapist Performance & Activity Report",
                description = "Sessions conducted, assessment completions, and caregiver review response rates",
                format = "PDF / Excel",
                onExportPdf = { Toast.makeText(context, "Therapist Performance PDF Report Generated & Downloaded", Toast.LENGTH_SHORT).show() },
                onExportExcel = { Toast.makeText(context, "Therapist Performance Excel Report Generated & Downloaded", Toast.LENGTH_SHORT).show() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ReportExportCard(
                title = "Child Registration & Longitudinal Progress",
                description = "Baseline assessment comparison, ADL independence trajectories, and prompt fading progress",
                format = "PDF / Excel",
                onExportPdf = { Toast.makeText(context, "Child Progress PDF Report Generated & Downloaded", Toast.LENGTH_SHORT).show() },
                onExportExcel = { Toast.makeText(context, "Child Progress Excel Report Generated & Downloaded", Toast.LENGTH_SHORT).show() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ReportExportCard(
                title = "Home Programmes & Caregiver Engagement",
                description = "Home practice adherence rates, video uploads, and therapist response times",
                format = "PDF / Excel",
                onExportPdf = { Toast.makeText(context, "Caregiver Engagement PDF Report Generated", Toast.LENGTH_SHORT).show() },
                onExportExcel = { Toast.makeText(context, "Caregiver Engagement Excel Report Generated", Toast.LENGTH_SHORT).show() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ReportExportCard(
                title = "System Activity & Audit Logging Report",
                description = "Administrative login history, account provisioning, and permission modifications",
                format = "PDF / Excel",
                onExportPdf = { Toast.makeText(context, "System Audit Log PDF Report Generated", Toast.LENGTH_SHORT).show() },
                onExportExcel = { Toast.makeText(context, "System Audit Log Excel Report Generated", Toast.LENGTH_SHORT).show() }
            )
        }
    }
}

@Composable
private fun ReportExportCard(
    title: String,
    description: String,
    format: String,
    onExportPdf: () -> Unit,
    onExportExcel: () -> Unit
) {
    val amberAccent = Color(0xFFD97706)

    Surface(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = amberAccent, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(description, fontSize = 12.sp, color = Color(0xFF64748B))

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onExportExcel,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Excel", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onExportPdf,
                    colors = ButtonDefaults.buttonColors(containerColor = amberAccent),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export PDF", fontSize = 12.sp)
                }
            }
        }
    }
}
