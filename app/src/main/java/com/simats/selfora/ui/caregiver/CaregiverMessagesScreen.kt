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
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Premium Healthcare Messaging Palette (UI styling only)
private val CgMsgPrimary = Color(0xFF7456C8)
private val CgMsgBackground = Color(0xFFF8F7FC)
private val CgMsgCardBg = Color(0xFFFFFFFF)
private val CgMsgSecondary = Color(0xFFEEE8FA)
private val CgMsgTextPrimary = Color(0xFF252238)
private val CgMsgTextSecondary = Color(0xFF89849A)
private val CgMsgSuccess = Color(0xFF43A779)
private val CgMsgBorder = Color(0xFFE7E2F4)

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
    var chatLoadError by remember { mutableStateOf<String?>(null) }
    var newMessageText by remember { mutableStateOf("") }

    val wsConnected by ChatWebSocketClient.isConnected.collectAsState()
    val listState = rememberLazyListState()

    suspend fun loadMessagesForChild(targetChildId: Long, showSpinner: Boolean = true) {
        if (targetChildId <= 0L) {
            isLoading = false
            return
        }
        if (activeChildId != targetChildId) {
            activeChildId = targetChildId
        }
        if (showSpinner && messages.isEmpty()) {
            isLoading = true
        }
        chatLoadError = null
        try {
            withTimeoutOrNull(15000L) {
                // 1. Resolve assigned Therapist ID & Full Name from backend
                try {
                    val partnerRes = ApiClient.apiService.getChatPartner(targetChildId)
                    if (partnerRes.isSuccessful && partnerRes.body() != null) {
                        val partner = partnerRes.body()!!
                        therapistId = partner.partnerId
                        therapistName = partner.partnerName
                        if (partner.childId > 0L) activeChildId = partner.childId
                        if (partner.childName.isNotBlank()) activeChildName = partner.childName
                        val parts = partner.partnerName.trim().split(" ").filter { it.isNotBlank() }
                        therapistInitials = when {
                            parts.size >= 2 -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
                            parts.size == 1 -> parts[0].take(2).uppercase()
                            else -> "OT"
                        }
                    }
                } catch (_: Exception) {}

                // 2. Load REAL messages from Database (backend atomically marks unread as read)
                try {
                    val res = ApiClient.apiService.getConversation(targetChildId)
                    if (res.isSuccessful && res.body() != null) {
                        val fetched = res.body()!!.sortedWith(compareBy({ it.sentAt.orEmpty() }, { it.id }))
                        // Preserve any in-flight optimistic message (negative id) until confirmed
                        val pendingLocal = messages.filter { it.id < 0L }
                        messages = (fetched + pendingLocal.filter { p -> fetched.none { it.messageText == p.messageText && it.senderId == p.senderId } })
                            .sortedWith(compareBy({ it.sentAt.orEmpty() }, { it.id }))
                        chatLoadError = null
                    } else if (messages.isEmpty()) {
                        chatLoadError = if (isTamil) "செய்திகளை ஏற்ற முடியவில்லை (HTTP ${res.code()})" else "Unable to load messages (HTTP ${res.code()})"
                    }
                } catch (e: Exception) {
                    if (messages.isEmpty()) {
                        chatLoadError = if (isTamil) "நெட்வொர்க் பிழை" else "Unable to load messages: ${e.localizedMessage ?: "Network error"}"
                    }
                }
            }
        } catch (e: Exception) {
            if (messages.isEmpty()) {
                chatLoadError = "Unable to load messages: ${e.localizedMessage ?: "Network error"}"
            }
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            try {
                listState.animateScrollToItem(messages.size - 1)
            } catch (_: Exception) {}
        }
    }

    // Ensure WebSocket connects and resolve active child + assigned Therapist + real DB history
    LaunchedEffect(Unit) {
        if (messages.isEmpty()) isLoading = true
        try {
            ChatWebSocketClient.ensureConnected()

            var targetId = SessionManager.getActiveChildId()
            // Always check caregiver dashboard summary to get verified active child
            try {
                val dashRes = ApiClient.apiService.getCaregiverDashboardSummary()
                if (dashRes.isSuccessful && dashRes.body() != null) {
                    val linked = dashRes.body()!!.linkedChildren
                    if (linked.isNotEmpty()) {
                        val matched = linked.find { it.childId == targetId } ?: linked.first()
                        targetId = matched.childId
                        activeChildId = matched.childId
                        activeChildName = matched.name
                        if (matched.therapistName.isNotBlank()) {
                            therapistName = matched.therapistName
                        }
                        SessionManager.saveActiveChildContext(matched.childId, matched.name, matched.gender)
                    }
                }
            } catch (_: Exception) {}

            if (targetId > 0L) {
                activeChildId = targetId
                loadMessagesForChild(targetId, showSpinner = messages.isEmpty())
            } else {
                isLoading = false
            }
        } catch (_: Exception) {
            isLoading = false
        } finally {
            isLoading = false
        }
    }

    // Silent background sync every 5 seconds (zero loading spinner) for guaranteed delivery even if WebSocket drops
    LaunchedEffect(activeChildId) {
        if (activeChildId <= 0L) return@LaunchedEffect
        while (true) {
            delay(5000L)
            try {
                val res = ApiClient.apiService.getConversation(activeChildId)
                if (res.isSuccessful && res.body() != null) {
                    val fetched = res.body()!!.sortedWith(compareBy({ it.sentAt.orEmpty() }, { it.id }))
                    val pendingLocal = messages.filter { it.id < 0L }
                    val merged = (fetched + pendingLocal.filter { p -> fetched.none { it.messageText == p.messageText && it.senderId == p.senderId } })
                        .sortedWith(compareBy({ it.sentAt.orEmpty() }, { it.id }))
                    if (merged != messages) {
                        messages = merged
                    }
                }
            } catch (_: Exception) {}
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
                        messages = updated.sortedWith(compareBy({ it.sentAt.orEmpty() }, { it.id }))
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
        val textToSend = newMessageText.trim()
        if (textToSend.isNotBlank()) {
            val resolvedChildId = when {
                activeChildId > 0L -> activeChildId
                SessionManager.getActiveChildId() > 0L -> SessionManager.getActiveChildId()
                else -> 0L // Backend auto-resolves caregiver's linked child!
            }
            if (resolvedChildId > 0L && activeChildId <= 0L) {
                activeChildId = resolvedChildId
            }

            val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
            val tempId = -System.currentTimeMillis()
            val targetTherapistId = therapistId // 0L is auto-resolved by backend to the child's assigned therapist!

            val optimisticMsg = MessageResponse(
                id = tempId,
                senderId = currentUserId,
                senderName = currentUserName,
                receiverId = targetTherapistId,
                receiverName = therapistName,
                childId = resolvedChildId,
                messageText = textToSend,
                isRead = false,
                sentAt = nowIso
            )
            messages = messages + optimisticMsg
            newMessageText = ""

            coroutineScope.launch {
                if (messages.isNotEmpty()) {
                    try { listState.animateScrollToItem(messages.size - 1) } catch (_: Exception) {}
                }
                // Always persist reliably via REST POST /api/messages (backend also broadcasts over WebSocket!)
                try {
                    val res = ApiClient.apiService.sendMessage(
                        SendMessageRequest(
                            receiverId = targetTherapistId,
                            childId = resolvedChildId,
                            messageText = textToSend
                        )
                    )
                    if (res.isSuccessful && res.body() != null) {
                        val saved = res.body()!!
                        if (saved.childId > 0L) activeChildId = saved.childId
                        if (saved.receiverId > 0L) therapistId = saved.receiverId
                        val replaced = messages.map { if (it.id == tempId) saved else it }
                        messages = replaced.distinctBy { it.id }.sortedWith(compareBy({ it.sentAt.orEmpty() }, { it.id }))
                    } else {
                        messages = messages.filterNot { it.id == tempId }
                        val errBody = try { res.errorBody()?.string()?.take(80) } catch (_: Exception) { null }
                        Toast.makeText(
                            context,
                            if (isTamil) "செய்தி அனுப்புவதில் பிழை (${res.code()})" else "Failed to send message (HTTP ${res.code()})${if (!errBody.isNullOrBlank()) ": $errBody" else ""}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    messages = messages.filterNot { it.id == tempId }
                    Toast.makeText(
                        context,
                        if (isTamil) "நெட்வொர்க் பிழை" else "Network error: ${e.localizedMessage ?: "Failed to send"}",
                        Toast.LENGTH_LONG
                    ).show()
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
        containerColor = CgMsgBackground,
        topBar = {
            Surface(
                color = CgMsgCardBg,
                shadowElevation = 3.dp,
                border = BorderStroke(1.dp, CgMsgBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CgMsgSecondary,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onBack)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = CgMsgPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Therapist Avatar with live status indicator dot
                    Box(modifier = Modifier.size(44.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = CgMsgSecondary,
                            border = BorderStroke(1.5.dp, CgMsgPrimary.copy(alpha = 0.28f)),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = therapistInitials,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CgMsgPrimary
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(CgMsgCardBg)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(CgMsgSuccess)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = therapistName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CgMsgTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = if (isTamil) "சிகிச்சையாளர்" else "Occupational Therapist",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = CgMsgTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = CircleShape,
                        color = CgMsgSecondary,
                        border = BorderStroke(1.dp, CgMsgPrimary.copy(alpha = 0.18f)),
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable {
                                coroutineScope.launch {
                                    loadMessagesForChild(activeChildId)
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = CgMsgPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    LanguageTogglePill()
                }
            }
        },
        bottomBar = {
            Surface(
                color = CgMsgCardBg,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = CgMsgBorder, thickness = 1.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickReplies.forEach { reply ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = CgMsgSecondary,
                                border = BorderStroke(1.dp, CgMsgPrimary.copy(alpha = 0.16f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { newMessageText = reply }
                            ) {
                                Text(
                                    text = reply,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CgMsgPrimary,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 14.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = CgMsgBackground,
                            border = BorderStroke(
                                1.dp,
                                if (newMessageText.isNotBlank()) CgMsgPrimary.copy(alpha = 0.55f) else CgMsgBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 44.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 11.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (newMessageText.isEmpty()) {
                                    Text(
                                        text = if (isTamil) "செய்தியை தட்டச்சு செய்க..." else "Message $therapistName...",
                                        fontSize = 14.sp,
                                        color = CgMsgTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                BasicTextField(
                                    value = newMessageText,
                                    onValueChange = { newMessageText = it },
                                    textStyle = TextStyle(
                                        color = CgMsgTextPrimary,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    ),
                                    cursorBrush = SolidColor(CgMsgPrimary),
                                    maxLines = 4,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = { sendCaregiverMessage() }),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Surface(
                            shape = CircleShape,
                            color = if (newMessageText.isNotBlank()) CgMsgPrimary else CgMsgSecondary,
                            shadowElevation = if (newMessageText.isNotBlank()) 3.dp else 0.dp,
                            modifier = Modifier.size(44.dp)
                        ) {
                            IconButton(
                                onClick = { sendCaregiverMessage() },
                                enabled = newMessageText.isNotBlank(),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (newMessageText.isNotBlank()) Color.White else CgMsgTextSecondary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(19.dp)
                                )
                            }
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
                .background(CgMsgBackground)
        ) {
            // Child Care Context Strip
            Surface(
                color = CgMsgSecondary.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, CgMsgBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChildCare,
                            contentDescription = null,
                            tint = CgMsgPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (activeChildName.isNotBlank()) "Child: $activeChildName • ADL Care" else "Active Care Thread",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CgMsgTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CgMsgSuccess.copy(alpha = 0.14f),
                        modifier = Modifier.width(IntrinsicSize.Max)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(CgMsgSuccess)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Active",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = CgMsgSuccess,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            if (isLoading && messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CgMsgPrimary, strokeWidth = 3.dp)
                }
            } else if (chatLoadError != null && messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = CgMsgCardBg,
                        border = BorderStroke(1.dp, CgMsgBorder),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SelforaError.copy(alpha = 0.12f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = SelforaError,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = chatLoadError ?: "Unable to load messages",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CgMsgTextPrimary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        loadMessagesForChild(activeChildId)
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CgMsgPrimary)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retry", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = CgMsgCardBg,
                        border = BorderStroke(1.dp, CgMsgBorder),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = CgMsgSecondary,
                                modifier = Modifier.size(60.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = null,
                                        tint = CgMsgPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            if (activeChildId <= 0L) {
                                Text(
                                    text = if (isTamil) "இணைக்கப்பட்ட குழந்தை கிடைக்கவில்லை" else "No Linked Child Found",
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CgMsgTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isTamil) "சிகிச்சையாளர் உங்கள் குழந்தையின் சுயவிவரத்தை இணைத்த பிறகு உரையாடல் தொடங்கும்." else "Your therapist will link your child profile to start messaging.",
                                    fontSize = 12.5.sp,
                                    color = CgMsgTextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            isLoading = true
                                            try {
                                                val dashRes = ApiClient.apiService.getCaregiverDashboardSummary()
                                                if (dashRes.isSuccessful && dashRes.body() != null) {
                                                    val linked = dashRes.body()!!.linkedChildren
                                                    if (linked.isNotEmpty()) {
                                                        val firstChild = linked.first()
                                                        activeChildId = firstChild.childId
                                                        activeChildName = firstChild.name
                                                        therapistName = firstChild.therapistName.ifBlank { "Occupational Therapist" }
                                                        SessionManager.saveActiveChildContext(firstChild.childId, firstChild.name, firstChild.gender)
                                                        loadMessagesForChild(firstChild.childId)
                                                    }
                                                }
                                            } catch (_: Exception) {}
                                            finally { isLoading = false }
                                        }
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CgMsgPrimary)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Check Link", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text(
                                    text = if (isTamil) "இன்னும் செய்திகள் இல்லை" else "No messages yet",
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CgMsgTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isTamil) "சிகிச்சையாளரிடம் உங்கள் கேள்விகளை இங்கே கேட்கலாம்." else "Send a message to $therapistName about home practice updates.",
                                    fontSize = 12.5.sp,
                                    color = CgMsgTextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                    topStart = 20.dp,
                                    topEnd = 20.dp,
                                    bottomStart = if (isMe) 20.dp else 6.dp,
                                    bottomEnd = if (isMe) 6.dp else 20.dp
                                ),
                                color = if (isMe) CgMsgPrimary else CgMsgCardBg,
                                border = if (isMe) null else BorderStroke(1.dp, CgMsgBorder),
                                shadowElevation = if (isMe) 2.dp else 1.5.dp,
                                modifier = Modifier
                                    .widthIn(min = 84.dp, max = 296.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    if (!isMe) {
                                        Text(
                                            text = msg.senderName,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CgMsgPrimary
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                    }

                                    Text(
                                        text = msg.messageText,
                                        fontSize = 14.sp,
                                        color = if (isMe) Color.White else CgMsgTextPrimary,
                                        lineHeight = 20.sp
                                    )

                                    Spacer(modifier = Modifier.height(5.dp))

                                    Row(
                                        modifier = Modifier.align(Alignment.End),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = formatChatTime(msg.sentAt),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isMe) Color.White.copy(alpha = 0.80f) else CgMsgTextSecondary
                                        )
                                        if (isMe) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.DoneAll,
                                                contentDescription = if (msg.isRead) "Read" else "Delivered",
                                                tint = if (msg.isRead) Color(0xFFA7F3D0) else Color.White.copy(alpha = 0.65f),
                                                modifier = Modifier.size(14.dp)
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
