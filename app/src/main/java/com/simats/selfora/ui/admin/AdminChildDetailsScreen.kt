package com.simats.selfora.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChildDetailsScreen(
    childId: Long,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    val isBoy = childId % 2L != 0L
    val avatarDrawable = if (isBoy) R.drawable.boy_avatar else R.drawable.girl_avatar
    val childName = if (isBoy) "Aarav Sharma" else "Ananya Roy"
    val accentColor = if (isBoy) Color(0xFF2563EB) else Color(0xFF7C3AED)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Child Clinical File", fontWeight = FontWeight.Bold) },
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
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = avatarDrawable),
                        contentDescription = childName,
                        modifier = Modifier.size(80.dp).clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(childName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("Child ID #$childId • Age 6 • Male", fontSize = 12.sp, color = Color(0xFF64748B))

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = accentColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Current Independence: 85%",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
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
                    Text("Linked Personnel & Active Programmes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.height(12.dp))

                    AdminDetailRow("Assigned Therapist", "Dr. Sarah Jenkins (OT)")
                    AdminDetailRow("Linked Caregiver", "Priya Sharma (Mother)")
                    AdminDetailRow("Active ADL Programme", "Dressing - Upper Body T-Shirt")
                    AdminDetailRow("Current Prompt Level", "INDEPENDENT (L0)")
                    AdminDetailRow("Prompt Fading Plan", "Active (Fading to L0)")
                    AdminDetailRow("Total Practice Sessions", "24 Completed Sessions")
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
