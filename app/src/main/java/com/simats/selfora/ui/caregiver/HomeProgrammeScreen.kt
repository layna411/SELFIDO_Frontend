package com.simats.selfora.ui.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.HomeProgramResponse
import com.simats.selfora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeProgrammeScreen(
    onStartPractice: (programId: Long, activityId: String) -> Unit,
    onNavigateToTab: (String) -> Unit
) {
    var homePrograms by remember { mutableStateOf<List<HomeProgramResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val res = ApiClient.apiService.getTodayHomePrograms()
            if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                homePrograms = res.body()!!
            } else {
                homePrograms = getSampleHomeProgramsForChild("BOY")
            }
        } catch (e: Exception) {
            homePrograms = getSampleHomeProgramsForChild("BOY")
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = SelforaBackground,
        topBar = {
            Surface(
                color = SelforaSurface,
                shadowElevation = 2.dp,
                modifier = Modifier.statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        "Home Programme",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = SelforaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Assigned by: Dr. Sarah Jenkins (Therapist)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SelforaPrimary
                    )
                }
            }
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "This Week's Programme",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = SelforaTextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SelforaPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                "${homePrograms.size} Activities",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaPrimary
                            )
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = SelforaPrimary)
                        }
                    }
                } else {
                    items(homePrograms) { program ->
                        HomeProgrammeCard(
                            program = program,
                            onStartPractice = {
                                val actCode = when (program.activityId) {
                                    13L -> "eating_spoon_activity"
                                    9L -> "shoes_socks_activity"
                                    else -> "boy_tshirt_activity"
                                }
                                onStartPractice(program.id, actCode)
                            }
                        )
                    }
                }
            }

            // Floating Navigation Overlay
            CaregiverBottomNavigation(
                currentRoute = "home_programme",
                onTabSelected = onNavigateToTab,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun HomeProgrammeCard(
    program: HomeProgramResponse,
    onStartPractice: () -> Unit
) {
    val (iconEmoji, totalSteps, completedCount) = when (program.activityId) {
        13L -> Triple("🥣", 10, 0)
        9L -> Triple("👟", 10, 0)
        else -> Triple("👕", 18, 7)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SelforaSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top Row: Title on Left, Status Badge on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SelforaPrimary.copy(alpha = 0.1f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(iconEmoji, fontSize = 22.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            program.activityTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SelforaTextPrimary
                        )
                        Text(
                            "$totalSteps Clinical Steps",
                            fontSize = 12.sp,
                            color = SelforaTextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (completedCount > 0) SelforaSuccess.copy(alpha = 0.15f) else SelforaWarning.copy(alpha = 0.15f)
                ) {
                    Text(
                        if (completedCount > 0) "In Progress" else "Assigned",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (completedCount > 0) SelforaSuccess else SelforaWarning
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-row Badges: Practice frequency & Progress stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SelforaSecondary.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📅", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Target: ${program.frequencyPerWeek}x / week",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SelforaBackground,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📊", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Done: $completedCount / $totalSteps",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Therapist Goal Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SelforaBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "THERAPIST GOAL:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelforaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        program.goalStatement ?: "Improve independent ADL performance.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SelforaTextPrimary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStartPractice,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Practice", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            }
        }
    }
}
