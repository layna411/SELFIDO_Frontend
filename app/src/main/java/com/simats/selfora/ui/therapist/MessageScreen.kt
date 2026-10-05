package com.simats.selfora.ui.therapist

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.MessageResponse
import com.simats.selfora.data.model.SendMessageRequest
import com.simats.selfora.ui.components.glass.GlassCard
import com.simats.selfora.ui.components.glass.GlassTextField
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

data class CaregiverContact(
    val caregiverId: Long,
    val caregiverName: String,
    val relationship: String,
    val childId: Long,
    val childName: String,
    val avatarEmoji: String = "👩",
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    childId: Long = 1L,
    onBack: () -> Unit
) {
    var selectedCaregiver by remember { mutableStateOf<CaregiverContact?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<MessageResponse>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val caregiverContacts = remember {
        listOf(
            CaregiverContact(201L, "Priya Kumar", "Mother", 1L, "Arjun Kumar", "👩", "Thank you Doctor! He did great with the visual prompt picture today.", "10:15 AM", 1),
            CaregiverContact(202L, "Suresh Reddy", "Father", 2L, "Ananya Reddy", "👨", "Ananya tried putting her arms in the sleeves today!", "Yesterday", 0),
            CaregiverContact(203L, "Meera Patel", "Mother", 3L, "Kavya Patel", "👩", "Should we practice spoon eating before or after lunch?", "Sep 28", 2),
            CaregiverContact(204L, "Sunita Sharma", "Mother", 4L, "Rohan Sharma", "👩", "Rohan finished all 10 steps for shoe dressing!", "Sep 25", 0)
        )
    }

    fun loadConversation(cId: Long) {
        scope.launch {
            isLoading = true
            try {
                val res = ApiClient.apiService.getConversation(cId)
                if (res.isSuccessful && res.body() != null && res.body()!!.isNotEmpty()) {
                    messages = res.body()!!
                } else {
                    messages = getSampleMessagesForCaregiver(selectedCaregiver?.caregiverName ?: "Caregiver")
                }
            } catch (e: Exception) {
                messages = getSampleMessagesForCaregiver(selectedCaregiver?.caregiverName ?: "Caregiver")
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (selectedCaregiver == null) {
                        Text("Caregiver Messages", fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedCaregiver!!.avatarEmoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(selectedCaregiver!!.caregiverName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaTextPrimary)
                                Text("${selectedCaregiver!!.relationship} of ${selectedCaregiver!!.childName}", fontSize = 12.sp, color = SelforaTextSecondary)
                            }
                        }
                    }
                },
                navigationIcon = {
                    if (selectedCaregiver != null) {
                        TextButton(onClick = { selectedCaregiver = null }) {
                            Text("Chats", color = SelforaPrimary, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SelforaPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SelforaSurface)
            )
        },
        containerColor = SelforaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (selectedCaregiver == null) {
                // VIEW 1: CAREGIVERS LIST
                Column(modifier = Modifier.padding(16.dp)) {
                    GlassTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = "",
                        placeholder = "Search caregiver by name...",
                        leadingIcon = Icons.Default.Search
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("LINKED CAREGIVERS (${caregiverContacts.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    val filteredList = caregiverContacts.filter {
                        searchQuery.isBlank() || it.caregiverName.contains(searchQuery, ignoreCase = true) || it.childName.contains(searchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 150.dp)
                    ) {
                        items(filteredList) { contact ->
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = 4.dp,
                                onClick = {
                                    selectedCaregiver = contact
                                    loadConversation(contact.childId)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = SelforaPurpleAccent.copy(alpha = 0.15f),
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(contact.avatarEmoji, fontSize = 24.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(contact.caregiverName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SelforaTextPrimary)
                                            Text(contact.lastMessageTime, fontSize = 11.sp, color = SelforaTextSecondary)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("${contact.relationship} of ${contact.childName}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SelforaPrimary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = contact.lastMessage,
                                            fontSize = 12.sp,
                                            color = SelforaTextSecondary,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                    if (contact.unreadCount > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = CircleShape,
                                            color = SelforaPrimary,
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("${contact.unreadCount}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // VIEW 2: 1-ON-1 CHAT THREAD WITH SELECTED CAREGIVER
                Column(modifier = Modifier.fillMaxSize()) {
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
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (isMe) SelforaPrimary else Color.White,
                                    shadowElevation = 4.dp,
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = msg.senderName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMe) Color.White.copy(alpha = 0.8f) else SelforaPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = msg.messageText,
                                            fontSize = 14.sp,
                                            color = if (isMe) Color.White else SelforaTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Surface(
                        color = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding()
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
                                placeholder = { Text("Type message to ${selectedCaregiver!!.caregiverName}...") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (inputText.isNotBlank()) {
                                        val text = inputText
                                        inputText = ""
                                        scope.launch {
                                            try {
                                                ApiClient.apiService.sendMessage(
                                                    SendMessageRequest(
                                                        receiverId = selectedCaregiver!!.caregiverId,
                                                        childId = selectedCaregiver!!.childId,
                                                        messageText = text
                                                    )
                                                )
                                            } catch (e: Exception) {}
                                            loadConversation(selectedCaregiver!!.childId)
                                        }
                                    }
                                }
                            ) {
                                Surface(shape = CircleShape, color = SelforaPrimary, modifier = Modifier.size(42.dp)) {
                                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.padding(10.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getSampleMessagesForCaregiver(caregiverName: String): List<MessageResponse> {
    return listOf(
        MessageResponse(1L, 1L, "Dr. Sarah Jenkins", 201L, caregiverName, 1L, "Hello $caregiverName! Please continue with the assigned dressing practice this week.", false, "10:00 AM"),
        MessageResponse(2L, 201L, caregiverName, 1L, "Dr. Sarah Jenkins", 1L, "Thank you Doctor! We completed today's practice successfully.", false, "10:15 AM")
    )
}
