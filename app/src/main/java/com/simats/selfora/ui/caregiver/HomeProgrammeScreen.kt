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
import com.simats.selfora.data.repository.PracticeSessionStore
import com.simats.selfora.ui.components.progress.SelforaLinearProgress
import com.simats.selfora.ui.therapist.SelfidoCatalogue
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeProgrammeScreen(
    onStartPractice: (programId: Long, activityId: String) -> Unit,
    onNavigateToTab: (String) -> Unit
) {
    var homePrograms by remember { mutableStateOf<List<HomeProgramResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val practiceRecords by PracticeSessionStore.records.collectAsState()
    val scope = rememberCoroutineScope()

    fun loadPrograms() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val res = ApiClient.apiService.getTodayHomePrograms()
                if (res.isSuccessful && res.body() != null) {
                    homePrograms = res.body()!!
                } else {
                    errorMessage = "Failed to load home programmes (${res.code()})"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage ?: "Unable to connect"}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadPrograms()
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
                        "Assigned Activities & Practice Guidance",
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 150.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Assigned Programmes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = SelforaTextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SelforaPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                "${homePrograms.size} Active",
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
                } else if (errorMessage != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SelforaSurface)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("⚠️", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    errorMessage!!,
                                    fontSize = 14.sp,
                                    color = SelforaTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { loadPrograms() },
                                    colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary)
                                ) {
                                    Text("Retry", color = Color.White)
                                }
                            }
                        }
                    }
                } else if (homePrograms.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SelforaSurface)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📋", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No Home Programmes Assigned",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = SelforaTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Your therapist has not assigned any home practice programmes yet.",
                                    fontSize = 12.sp,
                                    color = SelforaTextSecondary
                                )
                            }
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
}

@Composable
fun HomeProgrammeCard(
    program: HomeProgramResponse,
    onStartPractice: () -> Unit
) {
    val practiceRecords by PracticeSessionStore.records.collectAsState()
    val recordForThisAct = practiceRecords.firstOrNull { it.activityId == program.activityId }

    val actDef = SelfidoCatalogue.getActivityById(program.activityId)
    val iconEmoji = actDef?.icon ?: "🎯"
    val totalSteps = actDef?.totalSteps ?: 15
    val completedCount = recordForThisAct?.independentCount ?: if (program.activityId == 1L) 7 else 0
    val isPracticedToday = PracticeSessionStore.isActivityPracticedToday(program.activityId)

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
                    color = if (isPracticedToday) SelforaSuccess.copy(alpha = 0.15f) else if (completedCount > 0) SelforaPrimary.copy(alpha = 0.15f) else SelforaWarning.copy(alpha = 0.15f)
                ) {
                    Text(
                        if (isPracticedToday) "Practiced Today! ✓" else if (completedCount > 0) "In Progress" else "Assigned",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPracticedToday) SelforaSuccess else if (completedCount > 0) SelforaPrimary else SelforaWarning
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

            if (totalSteps > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                SelforaLinearProgress(
                    progress = (completedCount.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f),
                    color = if (isPracticedToday) SelforaSuccess else SelforaPrimary,
                    height = 5.dp,
                    modifier = Modifier.fillMaxWidth()
                )
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
                    if (!program.targetPromptLevelName.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SelforaPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                "Assistance: ${program.targetPromptLevelName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Caregiver Instructions Callout
            if (!program.caregiverInstructions.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0FDF4),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text("💡", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "PARENT PRACTICE GUIDANCE:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            program.caregiverInstructions!!,
                            fontSize = 12.sp,
                            color = Color(0xFF14532D),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Things to Avoid Callout
            if (!program.thingsToAvoid.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF2F2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text("⚠️", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "THINGS TO AVOID:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            program.thingsToAvoid!!,
                            fontSize = 11.5.sp,
                            color = Color(0xFF7F1D1D),
                            lineHeight = 15.sp
                        )
                    }
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
