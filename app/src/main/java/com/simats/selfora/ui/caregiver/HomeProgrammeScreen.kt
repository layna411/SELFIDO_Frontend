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
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Home Programme", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SelforaTextPrimary)
                        Text("Assigned by: Dr. Sarah Jenkins (Therapist)", fontSize = 12.sp, color = SelforaTextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SelforaSurface)
            )
        },
        bottomBar = {
            CaregiverBottomNavigation(
                currentRoute = "home_programme",
                onTabSelected = onNavigateToTab
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SelforaBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "This Week",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SelforaTextPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SelforaPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "Therapist Prescribed",
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
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(iconEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        program.activityTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = SelforaTextPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SelforaSecondary.copy(alpha = 0.12f)
                ) {
                    Text(
                        "Practice: ${program.frequencyPerWeek} times this week",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelforaSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total Steps: $totalSteps",
                    fontSize = 13.sp,
                    color = SelforaTextSecondary
                )
                Text(
                    "Status: ${if (completedCount > 0) "In Progress ($completedCount/$totalSteps)" else "Not Started"}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (completedCount > 0) SelforaPrimary else SelforaWarning
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                    Text(
                        program.goalStatement ?: "Improve independent ADL performance.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SelforaTextPrimary
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
