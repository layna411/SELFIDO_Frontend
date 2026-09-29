package com.simats.selfora.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.MessageResponse
import com.simats.selfora.data.model.SendMessageRequest
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    childId: Long = 1L,
    receiverId: Long = 2L, // Caregiver ID
    onBack: () -> Unit
) {
    var messages by remember { mutableStateOf<List<MessageResponse>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    fun loadMessages() {
        scope.launch {
            try {
                val res = ApiClient.apiService.getConversation(childId)
                if (res.isSuccessful && res.body() != null && res.body()!!.isNotEmpty()) {
                    messages = res.body()!!
                } else {
                    messages = getSampleMessages()
                }
            } catch (e: Exception) {
                messages = getSampleMessages()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadMessages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Caregiver Communication", fontWeight = FontWeight.Bold, color = SelforaTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SelforaTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SelforaSurface,
                    titleContentColor = SelforaTextPrimary,
                    navigationIconContentColor = SelforaTextPrimary,
                    actionIconContentColor = SelforaTextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SelforaBgLight)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.senderId == 1L
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) SelforaBluePrimary else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    msg.senderName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMe) Color.White.copy(alpha = 0.8f) else SelforaTextMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    msg.messageText,
                                    fontSize = 14.sp,
                                    color = if (isMe) Color.White else SelforaTextDark
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                color = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Type message to caregiver...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                scope.launch {
                                    val text = inputText
                                    inputText = ""
                                    try {
                                        ApiClient.apiService.sendMessage(
                                            SendMessageRequest(receiverId = receiverId, childId = childId, messageText = text)
                                        )
                                    } catch (e: Exception) {}
                                    loadMessages()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = SelforaBluePrimary)
                    }
                }
            }
        }
    }
}

fun getSampleMessages(): List<MessageResponse> {
    return listOf(
        MessageResponse(1L, 1L, "Dr. Sarah Jenkins", 2L, "Priya Sharma", 1L, "Hi Priya! I assigned the new T-Shirt home programme for Aarav focused on Step 7.", false, "10:00 AM"),
        MessageResponse(2L, 2L, "Priya Sharma", 1L, "Dr. Sarah Jenkins", 1L, "Thank you Doctor! He did great with the visual prompt picture today.", false, "10:15 AM")
    )
}
