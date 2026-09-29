package com.simats.selfora.ui.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.model.CaregiverNotificationItem
import com.simats.selfora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverNotificationsScreen(
    onBack: () -> Unit
) {
    val notifications = remember {
        listOf(
            CaregiverNotificationItem(
                id = 1L,
                title = "New Activity Assigned",
                message = "Dr. Sarah Jenkins assigned Eating with Spoon to Aarav's Home Programme.",
                timestamp = "10 mins ago",
                type = "PROGRAMME"
            ),
            CaregiverNotificationItem(
                id = 2L,
                title = "Practice Reminder",
                message = "Your T-Shirt Dressing practice for Aarav is due today.",
                timestamp = "2 hours ago",
                type = "REMINDER"
            ),
            CaregiverNotificationItem(
                id = 3L,
                title = "Therapist Instruction Updated",
                message = "Focus on allowing child to attempt steps independently.",
                timestamp = "Yesterday",
                type = "INSTRUCTION"
            ),
            CaregiverNotificationItem(
                id = 4L,
                title = "Home Session Saved",
                message = "T-Shirt Dressing home session successfully saved (18 steps recorded).",
                timestamp = "Yesterday",
                type = "REMINDER"
            )
        )
    }

    Scaffold(
        containerColor = SelforaBackground,
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold, color = SelforaTextPrimary) },
                modifier = Modifier.statusBarsPadding(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SelforaTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SelforaSurface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SelforaBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notifications) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SelforaSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SelforaPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(20.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SelforaTextPrimary)
                                Text(item.timestamp, fontSize = 10.sp, color = SelforaTextSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(item.message, fontSize = 12.sp, color = SelforaTextSecondary, lineHeight = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
