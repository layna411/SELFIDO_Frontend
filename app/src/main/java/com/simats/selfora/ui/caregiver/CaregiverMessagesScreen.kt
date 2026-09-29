package com.simats.selfora.ui.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                MessageResponse(1L, 2L, "Dr. Sarah Jenkins", 1L, "Sunita Sharma (Parent)", 1L, "Hello Sunita, please continue practicing the T-shirt activity with Aarav this week.", true, "2026-09-28 09:30"),
                MessageResponse(2L, 1L, "Sunita Sharma (Parent)", 2L, "Dr. Sarah Jenkins", 1L, "Sure Doctor! Aarav did really well with putting his arms through the sleeves yesterday.", true, "2026-09-28 10:15"),
                MessageResponse(3L, 2L, "Dr. Sarah Jenkins", 1L, "Sunita Sharma (Parent)", 1L, "That is fantastic progress! Make sure to encourage him to try line alignment next.", true, "2026-09-28 11:00")
            )
        )
    }
    var newMessageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val isKeyboardVisible = imeBottomPadding > 0.dp

    Scaffold(
        containerColor = SelforaBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                        "Therapist Chat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = SelforaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Dr. Sarah Jenkins (Occupational Therapist)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (!isKeyboardVisible) 96.dp else 0.dp)
                    .imePadding()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                color = if (isCaregiverMsg) SelforaPrimary else Color.White,
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .widthIn(max = 290.dp)
                                    .then(
                                        if (!isCaregiverMsg) Modifier.border(1.dp, SelforaBorder, RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                                        else Modifier
                                    )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        msg.senderName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCaregiverMsg) Color.White.copy(alpha = 0.9f) else SelforaPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        msg.messageText,
                                        fontSize = 14.sp,
                                        color = if (isCaregiverMsg) Color.White else SelforaTextPrimary,
                                        lineHeight = 19.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        msg.sentAt,
                                        fontSize = 10.sp,
                                        color = if (isCaregiverMsg) Color.White.copy(alpha = 0.75f) else SelforaTextSecondary,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }

                // Input bar sitting directly on top of keyboard (or above floating bottom bar if keyboard closed)
                Surface(
                    color = Color.Transparent,
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newMessageText,
                            onValueChange = { newMessageText = it },
                            placeholder = { Text("Write a message to therapist...", fontSize = 13.sp, color = SelforaTextSecondary) },
                            modifier = Modifier.weight(1f),
                            maxLines = 3,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SelforaTextPrimary,
                                unfocusedTextColor = SelforaTextPrimary,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = SelforaPrimary,
                                unfocusedBorderColor = SelforaBorder
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
                            modifier = Modifier
                                .size(46.dp)
                                .background(SelforaPrimary, CircleShape)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // Floating Navigation Overlay (hidden when keyboard is open)
            if (!isKeyboardVisible) {
                CaregiverBottomNavigation(
                    currentRoute = "caregiver_messages",
                    onTabSelected = onNavigateToTab,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
