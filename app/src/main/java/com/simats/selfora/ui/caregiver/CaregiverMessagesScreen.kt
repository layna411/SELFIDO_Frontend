package com.simats.selfora.ui.caregiver

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.model.MessageResponse
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverMessagesScreen(
    onNavigateToTab: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var messages by remember {
        mutableStateOf(
            listOf(
                MessageResponse(
                    id = 1L,
                    senderId = 2L,
                    senderName = "Dr. Priya OT",
                    receiverId = 1L,
                    receiverName = "Meena (Caregiver)",
                    childId = 1L,
                    messageText = "Hi Meena,\n\nRohan is doing well!\nPlease continue the same home practice. Try reducing prompt gradually as guided.\n\nYou're doing a wonderful job! ❤️",
                    isRead = true,
                    sentAt = "Today, 9:30 AM"
                )
            )
        )
    }
    var newMessageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    Scaffold(
        containerColor = SelforaBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            GlassTopBar(
                title = "Messages",
                subtitle = "Messages from Therapist",
                onBackClick = onBack,
                accentColor = SelforaPrimary
            )
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
                    .imePadding()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Therapist Header Info Card (Image 2 Screen 10)
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = SelforaPrimary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("👩‍⚕️", fontSize = 24.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Dr. Priya OT", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                    Text("Today, 9:30 AM", fontSize = 12.sp, color = SelforaTextSecondary)
                                }
                            }
                        }
                    }

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
                                    .widthIn(max = 320.dp)
                                    .border(
                                        1.dp,
                                        if (isCaregiverMsg) SelforaPrimary else SelforaBorder.copy(alpha = 0.5f),
                                        RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        msg.messageText,
                                        fontSize = 14.sp,
                                        color = if (isCaregiverMsg) Color.White else SelforaTextPrimary,
                                        lineHeight = 20.sp
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

                    // Attachment Card (Image 2 Screen 10)
                    item {
                        Column {
                            Text("Attachment", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaTextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SelforaBorder.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        Toast.makeText(context, "Opening Tooth Brushing Step Guide.pdf", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = SelforaError.copy(alpha = 0.12f),
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = SelforaError, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Tooth Brushing", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                            Text("Step Guide", fontSize = 12.sp, color = SelforaTextSecondary)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SelforaError.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            "PDF",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SelforaError,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Reply Input Bar (Image 2 Screen 10)
                Surface(
                    color = SelforaSurface,
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newMessageText,
                            onValueChange = { newMessageText = it },
                            placeholder = { Text("Reply to Therapist...", fontSize = 13.sp, color = SelforaTextSecondary) },
                            modifier = Modifier.weight(1f),
                            maxLines = 3,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SelforaTextPrimary,
                                unfocusedTextColor = SelforaTextPrimary,
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
                                        senderName = "Meena (Caregiver)",
                                        receiverId = 2L,
                                        receiverName = "Dr. Priya OT",
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
        }
    }
}
