package com.simats.selfora.ui.caregiver

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.api.ChatWebSocketClient
import com.simats.selfora.data.api.ChatWsEvent
import com.simats.selfora.data.local.SessionManager
import com.simats.selfora.data.model.MessageResponse
import com.simats.selfora.data.model.SendMessageRequest
import com.simats.selfora.data.model.formatChatTime
import com.simats.selfora.ui.components.LanguageTogglePill
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*
import com.simats.selfora.utils.LanguageManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CaregiverMessagesScreen(
    onNavigateToTab: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    // Intercept back presses so the app returns cleanly to dashboard instead of exiting
    BackHandler {
        onBack()
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isTamil = currentLanguage == "ta"

    val currentUserName = remember {
        SessionManager.getUsername().ifBlank { "Caregiver" }
    }
    val currentUserId = remember {
        SessionManager.getUserId()
    }

    var activeChildId by remember {
        mutableStateOf(SessionManager.getActiveChildId().let { if (it > 0L) it else 0L })
    }
    var activeChildName by remember {
        mutableStateOf(SessionManager.getActiveChildName())
    }
    var therapistId by remember { mutableStateOf(0L) }
    var therapistName by remember { mutableStateOf("Occupational Therapist") }
    var therapistInitials by remember { mutableStateOf("OT") }

    var messages by remember { mutableStateOf<List<MessageResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var newMessageText by remember { mutableStateOf("") }

    val wsConnected by ChatWebSocketClient.isConnected.collectAsState()
    val listState = rememberLazyListState()

    // Ensure WebSocket connects and resolve active child + assigned Therapist + real DB history
    LaunchedEffect(Unit) {
        ChatWebSocketClient.ensureConnected()
        isLoading = true

        // 1. Resolve active child if not cached in SessionManager
        if (activeChildId <= 0L) {
            try {
                val dashRes = ApiClient.apiService.getCaregiverDashboardSummary()
                if (dashRes.isSuccessful && dashRes.body() != null) {
                    val firstChild = dashRes.body()!!.linkedChildren.firstOrNull()
                    if (firstChild != null) {
                        activeChildId = firstChild.childId
                        activeChildName = firstChild.name
                        therapistName = firstChild.therapistName.ifBlank { "Occupational Therapist" }
                        SessionManager.saveActiveChildContext(firstChild.childId, firstChild.name, firstChild.gender)
                    }
                }
            } catch (_: Exception) {}
        }

        val cId = if (activeChildId > 0L) activeChildId else 1L
        activeChildId = cId

        // 2. Resolve assigned Therapist ID & Full Name from backend
        try {
            val partnerRes = ApiClient.apiService.getChatPartner(cId)
            if (partnerRes.isSuccessful && partnerRes.body() != null) {
                val partner = partnerRes.body()!!
                therapistId = partner.partnerId
                therapistName = partner.partnerName
                if (partner.childName.isNotBlank()) activeChildName = partner.childName
                val parts = partner.partnerName.trim().split(" ").filter { it.isNotBlank() }
                therapistInitials = when {
                    parts.size >= 2 -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
                    parts.size == 1 -> parts[0].take(2).uppercase()
                    else -> "OT"
                }
            }
        } catch (_: Exception) {}

        // 3. Mark existing messages as read via WebSocket & REST
        ChatWebSocketClient.markRead(cId)
        try { ApiClient.apiService.markConversationAsRead(cId) } catch (_: Exception) {}

        // 4. Load REAL messages from Database (no static fake messages)
        try {
            val res = ApiClient.apiService.getConversation(cId)
            if (res.isSuccessful && res.body() != null) {
                messages = res.body()!!.sortedWith(compareBy({ it.sentAt }, { it.id }))
                if (messages.isNotEmpty()) {
                    listState.scrollToItem(messages.size - 1)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to load messages: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    // Listen to live real-time WebSocket frames
    LaunchedEffect(activeChildId) {
        if (activeChildId <= 0L) return@LaunchedEffect
        ChatWebSocketClient.events.collect { event ->
            when (event) {
                is ChatWsEvent.NewMessage -> {
                    val incoming = event.message
                    if (incoming.childId == activeChildId) {
                        val updated = messages.toMutableList()
                        val optIdx = updated.indexOfFirst {
                            it.id < 0L && it.senderId == incoming.senderId && it.messageText == incoming.messageText
                        }
                        if (optIdx >= 0) {
                            updated[optIdx] = incoming
                        } else if (updated.none { it.id == incoming.id }) {
                            updated.add(incoming)
                        }
                        messages = updated.sortedWith(compareBy({ it.sentAt }, { it.id }))
                        if (messages.isNotEmpty()) {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                        if (incoming.senderId != currentUserId) {
                            ChatWebSocketClient.markRead(activeChildId)
                        }
                    }
                }

                is ChatWsEvent.MessagesRead -> {
                    if (event.childId == activeChildId && event.readByUserId != currentUserId) {
                        messages = messages.map { m ->
                            if (m.senderId == currentUserId) m.copy(isRead = true) else m
                        }
                    }
                }

                is ChatWsEvent.ContactsUpdated -> {}
            }
        }
    }

    val sendCaregiverMessage = {
        if (newMessageText.isNotBlank() && activeChildId > 0L) {
            val textToSend = newMessageText.trim()
            val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
            val tempId = -System.currentTimeMillis()
            val targetTherapistId = therapistId // 0L is auto-resolved by backend to the child's assigned therapist!

            val optimisticMsg = MessageResponse(
                id = tempId,
                senderId = currentUserId,
                senderName = currentUserName,
                receiverId = targetTherapistId,
                receiverName = therapistName,
                childId = activeChildId,
                messageText = textToSend,
                isRead = false,
                sentAt = nowIso
            )
            messages = messages + optimisticMsg
            newMessageText = ""

            coroutineScope.launch {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
                val sentViaWs = ChatWebSocketClient.sendMessage(targetTherapistId, activeChildId, textToSend)
                if (!sentViaWs) {
                    try {
                        val res = ApiClient.apiService.sendMessage(
                            SendMessageRequest(
                                receiverId = targetTherapistId,
                                childId = activeChildId,
                                messageText = textToSend
                            )
                        )
                        if (res.isSuccessful && res.body() != null) {
                            val saved = res.body()!!
                            messages = messages.map { if (it.id == tempId) saved else it }
                        } else {
                            Toast.makeText(
                                context,
                                if (isTamil) "செய்தி அனுப்புவதில் பிழை (${res.code()})" else "Failed to send message (${res.code()})",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            if (isTamil) "நெட்வொர்க் பிழை" else "Network error sending message",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    val quickReplies = remember(isTamil) {
        if (isTamil) {
            listOf(
                "குழந்தை இன்று படிகளை முடித்தது! 🌟",
                "பொத்தான்களுக்கு கூடுதல் உதவி தேவை 👕",
                "உதவிகள் வெற்றிகரமாக குறைக்கப்பட்டது 👍",
                "சமீபத்திய பயிற்சியை மதிப்பாய்வு செய்யவும் 📋"
            )
        } else {
            listOf(
                "Child completed today's steps! 🌟",
                "Needs more cues for buttons 👕",
                "Prompt faded successfully 👍",
                "Please review our latest practice 📋"
            )
        }
    }

    Scaffold(
        containerColor = SelforaBackground, // Authentic SELFORA Theme Background
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, SelforaBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        onClick = onBack,
                        tint = SelforaPrimary,
                        size = 36.dp,
                        iconSize = 18.dp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = CircleShape,
                        color = SelforaPrimary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, SelforaPrimary.copy(alpha = 0.35f)),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = therapistInitials,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = therapistName,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (wsConnected) SelforaSuccess else SelforaWarning)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (wsConnected) {
                                    if (isTamil) "நேரடி இணைப்பு • சிகிச்சையாளர்" else "Live WebSocket • Occupational Therapist"
                                } else {
                                    if (isTamil) "மருத்துவ சிகிச்சையாளர்" else "Occupational Therapist"
                                },
                                fontSize = 11.sp,
                                color = SelforaTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    GlassIconButton(
                        icon = Icons.Default.Call,
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:+919876543210")
                            }
                            context.startActivity(intent)
                        },
                        tint = SelforaPrimary,
                        size = 36.dp,
                        iconSize = 18.dp
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    LanguageTogglePill()
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, SelforaBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickReplies.forEach { reply ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SelforaSecondary.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, SelforaSecondary.copy(alpha = 0.25f)),
                                modifier = Modifier.clickable { newMessageText = reply }
                            ) {
                                Text(
                                    text = reply,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SelforaSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newMessageText,
                            onValueChange = { newMessageText = it },
                            placeholder = {
                                Text(
                                    text = if (isTamil) "செய்தியை தட்டச்சு செய்க..." else "Message $therapistName...",
                                    fontSize = 13.sp,
                                    color = SelforaTextSecondary
                                )
                            },
                            modifier = Modifier.weight(1f),
                            maxLines = 4,
                            shape = RoundedCornerShape(22.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { sendCaregiverMessage() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                focusedTextColor = SelforaTextPrimary,
                                unfocusedTextColor = SelforaTextPrimary,
                                focusedBorderColor = SelforaPrimary,
                                unfocusedBorderColor = SelforaBorder
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { sendCaregiverMessage() },
                            enabled = newMessageText.isNotBlank(),
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (newMessageText.isNotBlank()) SelforaPrimary else Color(0xFFCBD5E1),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Child Context Header
            Surface(
                color = Color.White,
                border = BorderStroke(1.dp, SelforaBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (activeChildName.isNotBlank()) "Child: $activeChildName • ADL Routine" else "Active ADL Care Thread",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SelforaTextPrimary
                    )
                    Text(
                        text = if (wsConnected) "● Live WebSocket" else "Connecting...",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (wsConnected) SelforaSuccess else SelforaWarning
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SelforaPrimary)
                }
            } else if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = SelforaPrimary.copy(alpha = 0.1f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = SelforaPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isTamil) "இன்னும் செய்திகள் இல்லை" else "No messages yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isTamil) "சிகிச்சையாளரிடம் உங்கள் கேள்விகளை இங்கே கேட்கலாம்." else "Send a message to $therapistName about home practice updates.",
                            fontSize = 12.5.sp,
                            color = SelforaTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        messages,
                        key = { index, msg -> "${msg.id}_${msg.sentAt}_$index" }
                    ) { _, msg ->
                        val isMe = msg.senderId == currentUserId

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 4.dp,
                                    bottomEnd = if (isMe) 4.dp else 16.dp
                                ),
                                color = if (isMe) SelforaPrimary else Color.White,
                                border = BorderStroke(
                                    1.dp,
                                    if (isMe) SelforaPrimary else SelforaBorder
                                ),
                                shadowElevation = 1.dp,
                                modifier = Modifier.widthIn(min = 84.dp, max = 305.dp)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                                    if (!isMe) {
                                        Text(
                                            text = msg.senderName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SelforaPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }

                                    Text(
                                        text = msg.messageText,
                                        fontSize = 13.8.sp,
                                        color = if (isMe) Color.White else SelforaTextPrimary,
                                        lineHeight = 19.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.align(Alignment.End),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = formatChatTime(msg.sentAt),
                                            fontSize = 10.sp,
                                            color = if (isMe) Color.White.copy(alpha = 0.85f) else SelforaTextSecondary
                                        )
                                        if (isMe) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.DoneAll,
                                                contentDescription = if (msg.isRead) "Read" else "Delivered",
                                                tint = if (msg.isRead) Color(0xFF93C5FD) else Color.White.copy(alpha = 0.70f),
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
