package com.simats.selfora.ui.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.components.progress.*
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
            TopAppBar(
                title = { Text("My Child's Home Practice", fontWeight = FontWeight.Bold, color = SelforaTextPrimary) },
                modifier = Modifier.statusBarsPadding(),
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "History", tint = SelforaPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SelforaSurface)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .background(SelforaBackground)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 150.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Reward & Streak Card
                item {
                    ChildRewardProgress(
                        starsEarned = 4,
                        totalStars = 5,
                        currentStreak = 5
                    )
                }

                // Active Assigned Home Programme Completion
                item {
                    Text("ACTIVE HOME PROGRAMMES", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SelforaPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    ProgrammeCompletionCard(
                        title = "Upper Body Dressing - T-Shirt Practice",
                        assignedDate = "Oct 01, 2026",
                        completionPercentage = 80,
                        caregiverStatusText = "8/10 Sessions Completed (4x/week)",
                        isPendingReview = false,
                        onClick = onNavigateToHistory
                    )
                }

                // Longitudinal Home Practice Progress Trend
                item {
                    ProgressTrendChart(
                        dataPoints = listOf(
                            "Wk 1" to 50f,
                            "Wk 2" to 62f,
                            "Wk 3" to 70f,
                            "Wk 4" to 80f
                        ),
                        title = "Home Practice Consistency Trend",
                        subtitle = "Weekly completion rate of home tasks",
                        lineColor = Color(0xFF10B981)
                    )
                }

                // ADL Category Practice Progress Breakdown
                item {
                    Text("PRACTICE COMPLETION BY ADL CATEGORY", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SelforaPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ADLProgressCard("Dressing", 80, sessionCount = 8, trendDelta = "+10%")
                        ADLProgressCard("Eating", 60, sessionCount = 5, trendDelta = "+5%")
                        ADLProgressCard("Shoes & Socks", 50, sessionCount = 4, trendDelta = "+8%")
                    }
                }

                // Practice History shortcut
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SelforaSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        onClick = onNavigateToHistory
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.History, contentDescription = null, tint = SelforaPrimary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("View Detailed Practice History", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SelforaTextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CaregiverProgressItem(activityLabel: String, progress: Float, percentageText: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(activityLabel, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SelforaTextPrimary)
            Text(percentageText, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SelforaPrimary)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = SelforaPrimary,
            trackColor = SelforaBorder
        )
    }
}
