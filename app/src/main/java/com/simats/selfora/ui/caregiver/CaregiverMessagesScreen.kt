package com.simats.selfora.ui.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.model.MessageResponse
import com.simats.selfora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverMessagesScreen(
    onNavigateToTab: (String) -> Unit
) {
    var messages by remember {
        mutableStateOf(
            listOf(
                MessageResponse(1L, 2L, "Dr. Sarah Jenkins", 1L, "Sunita Sharma (Parent)", 1L, "Hello Sunita, please continue practicing the T-shirt activity this week.", true, "2026-09-28 09:30"),
                MessageResponse(2L, 1L, "Sunita Sharma (Parent)", 2L, "Dr. Sarah Jenkins", 1L, "Sure Doctor! Aarav did really well with putting his arms through the sleeves yesterday.", true, "2026-09-28 10:15"),
                MessageResponse(3L, 2L, "Dr. Sarah Jenkins", 1L, "Sunita Sharma (Parent)", 1L, "That is fantastic progress! Make sure to encourage him to try line alignment next.", true, "2026-09-28 11:00")
            )
        )
    }
    var newMessageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Therapist Chat", fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                        Text("Dr. Sarah Jenkins (Occupational Therapist)", fontSize = 11.sp, color = SelforaPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.statusBarsPadding(),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SelforaSurface)
            )
        },
        bottomBar = {
            CaregiverBottomNavigation(
                currentRoute = "caregiver_messages",
                onTabSelected = onNavigateToTab
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SelforaBackground)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    val isCaregiverMsg = msg.senderId == 1L

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isCaregiverMsg) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isCaregiverMsg) 16.dp else 4.dp,
                                bottomEnd = if (isCaregiverMsg) 4.dp else 16.dp
                            ),
                            color = if (isCaregiverMsg) SelforaPrimary else SelforaSurface,
                            shadowElevation = 1.dp,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    msg.senderName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCaregiverMsg) Color.White.copy(alpha = 0.8f) else SelforaPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    msg.messageText,
                                    fontSize = 13.sp,
                                    color = if (isCaregiverMsg) Color.White else SelforaTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    msg.sentAt,
                                    fontSize = 9.sp,
                                    color = if (isCaregiverMsg) Color.White.copy(alpha = 0.7f) else SelforaTextSecondary,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }

            // Chat input bar
            Surface(
                color = SelforaSurface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newMessageText,
                        onValueChange = { newMessageText = it },
                        placeholder = { Text("Write a message to therapist...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SelforaBackground,
                            unfocusedContainerColor = SelforaBackground
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (newMessageText.isNotBlank()) {
                                messages = messages + MessageResponse(
                                    id = (messages.size + 1).toLong(),
                                    senderId = 1L,
                                    senderName = "Sunita Sharma (Parent)",
                                    receiverId = 2L,
                                    receiverName = "Dr. Sarah Jenkins",
                                    childId = 1L,
                                    messageText = newMessageText.trim(),
                                    isRead = false,
                                    sentAt = "Just now"
                                )
                                newMessageText = ""
                            }
                        },
                        modifier = Modifier.background(SelforaPrimary, CircleShape)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}
