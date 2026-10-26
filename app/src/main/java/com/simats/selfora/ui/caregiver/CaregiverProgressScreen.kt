package com.simats.selfora.ui.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverProgressScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToTab: (String) -> Unit
) {
    Scaffold(
        containerColor = SelforaBackground,
        topBar = {
            GlassTopBar(
                title = "Progress",
                subtitle = "Weekly Overview",
                accentColor = SelforaPrimary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Weekly Overview Date Range Switcher (Image 2 Screen 8)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Week", tint = SelforaPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Weekly Overview", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("5 – 11 May 2025", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                    }

                    IconButton(onClick = {}) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Week", tint = SelforaPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 68% Independence Circular Gauge (Image 2 Screen 8)
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(SelforaPrimary.copy(alpha = 0.08f))
                    .border(6.dp, SelforaPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "68%",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SelforaPrimary
                    )
                    Text(
                        text = "Independence",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SelforaTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Category Progress Bars (Image 2 Screen 8)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CaregiverActivityProgressItem(
                        activityName = "Tooth Brushing",
                        stepsCompletedText = "(12 / 16 steps)",
                        percentage = 68
                    )

                    CaregiverActivityProgressItem(
                        activityName = "Wearing T-Shirt",
                        stepsCompletedText = "(9 / 15 steps)",
                        percentage = 60
                    )

                    CaregiverActivityProgressItem(
                        activityName = "Eating with Spoon",
                        stepsCompletedText = "(14 / 20 steps)",
                        percentage = 70
                    )
                }
            }

            Spacer(modifier = Modifier.height(130.dp))
        }
    }
}

@Composable
fun CaregiverActivityProgressItem(
    activityName: String,
    stepsCompletedText: String,
    percentage: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(activityName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(stepsCompletedText, fontSize = 12.sp, color = SelforaTextSecondary)
            }
            Text("$percentage%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = percentage / 100f,
            color = SelforaPrimary,
            trackColor = SelforaPrimary.copy(alpha = 0.15f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}
