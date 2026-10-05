package com.simats.selfora.ui.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.model.CaregiverPracticeHistoryItem
import com.simats.selfora.data.repository.PracticeSessionStore
import com.simats.selfora.ui.theme.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverHistoryScreen(
    onBack: () -> Unit
) {
    val storedRecords by PracticeSessionStore.records.collectAsState()

    val baseHistoryItems = remember {
        listOf(
            CaregiverPracticeHistoryItem(101L, "👕 T-Shirt Dressing", "Today", 12, 18, 18, true),
            CaregiverPracticeHistoryItem(100L, "🥣 Eating with Spoon", "Yesterday", 8, 10, 10, true),
            CaregiverPracticeHistoryItem(99L, "👟 Shoes & Socks", "Sep 27", 10, 10, 7, false),
            CaregiverPracticeHistoryItem(98L, "👕 T-Shirt Dressing", "Sep 26", 14, 18, 15, false),
            CaregiverPracticeHistoryItem(97L, "🥣 Eating with Spoon", "Sep 25", 9, 10, 10, true)
        )
    }

    val historyItems = remember(storedRecords) {
        val todayStr = LocalDate.now().toString()
        val dynamicItems = storedRecords.map { rec ->
            CaregiverPracticeHistoryItem(
                sessionId = rec.sessionId,
                activityTitle = rec.activityTitle,
                dateDisplay = if (rec.practiceDate == todayStr) "Today" else rec.practiceDate,
                durationMinutes = rec.durationMinutes,
                totalSteps = rec.totalSteps,
                completedSteps = rec.independentCount,
                isFullyCompleted = rec.neededHelpCount == 0
            )
        }
        val dynamicIds = dynamicItems.map { it.sessionId }.toSet()
        val staticFiltered = baseHistoryItems.filterNot { it.sessionId in dynamicIds }
        dynamicItems + staticFiltered
    }

    var selectedItem by remember { mutableStateOf<CaregiverPracticeHistoryItem?>(null) }

    Scaffold(
        containerColor = SelforaBackground,
        topBar = {
            TopAppBar(
                title = { Text("Practice History", fontWeight = FontWeight.Bold, color = SelforaTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SelforaTextPrimary)
                    }
                },
                modifier = Modifier.statusBarsPadding(),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SelforaSurface)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SelforaBackground)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(historyItems) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedItem = item },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SelforaSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.dateDisplay, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaTextSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(item.activityTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaTextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${item.completedSteps} / ${item.totalSteps} Steps • ${item.durationMinutes} min", fontSize = 12.sp, color = SelforaTextSecondary)
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (item.isFullyCompleted) SelforaSuccess.copy(alpha = 0.15f) else SelforaWarning.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    if (item.isFullyCompleted) "Completed" else "Partially Done",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isFullyCompleted) SelforaSuccess else SelforaWarning
                                )
                            }
                        }
                    }
                }
            }

            // Session Detail Dialog
            selectedItem?.let { item ->
                AlertDialog(
                    onDismissRequest = { selectedItem = null },
                    title = { Text(item.activityTitle, fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Date: ${item.dateDisplay}")
                            Text("Duration: ${item.durationMinutes} minutes")
                            Text("Steps Practiced: ${item.totalSteps}")
                            Text("Steps Completed Independently: ${item.completedSteps}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Outcome: ${if (item.isFullyCompleted) "Child completed all practice steps with guidance." else "Child attempted practice steps with parent assistance."}",
                                fontSize = 13.sp,
                                color = SelforaTextSecondary
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { selectedItem = null }) {
                            Text("Close", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}
