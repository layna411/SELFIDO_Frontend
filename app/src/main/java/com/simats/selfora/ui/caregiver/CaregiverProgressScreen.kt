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
                // Header streak card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SelforaSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("8 Practice Sessions Completed!", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SelforaTextPrimary)
                            Text("Great job helping Aarav practice at home this week!", fontSize = 12.sp, color = SelforaTextSecondary)

                            Spacer(modifier = Modifier.height(16.dp))

                            // Mon-Fri practice session checklist
                            Text("This Week's Activity Streak", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SelforaTextPrimary)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
                                val completed = listOf(true, true, false, true, true)

                                days.forEachIndexed { idx, day ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (completed[idx]) SelforaSuccess else SelforaBorder,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (completed[idx]) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                                } else {
                                                    Text("—", color = SelforaTextSecondary, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(day, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                // Progress Breakdown Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SelforaSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("This Week's Progress", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaTextPrimary)
                            Spacer(modifier = Modifier.height(16.dp))

                            CaregiverProgressItem("👕 Dressing", 0.80f, "80%")
                            Spacer(modifier = Modifier.height(12.dp))
                            CaregiverProgressItem("🥣 Eating", 0.60f, "60%")
                            Spacer(modifier = Modifier.height(12.dp))
                            CaregiverProgressItem("👟 Shoes", 0.50f, "50%")
                        }
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
