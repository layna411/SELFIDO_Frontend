package com.simats.selfora.ui.therapist

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Premium Healthcare Messaging Palette (UI styling only)
private val MsgPrimary = Color(0xFF7456C8)
private val MsgBackground = Color(0xFFF8F7FC)
private val MsgCardBg = Color(0xFFFFFFFF)
private val MsgSecondary = Color(0xFFEEE8FA)
private val MsgTextPrimary = Color(0xFF252238)
private val MsgTextSecondary = Color(0xFF89849A)
private val MsgSuccess = Color(0xFF43A779)
private val MsgBorder = Color(0xFFE7E2F4)

data class CaregiverContact(
    val caregiverId: Long,
    val caregiverName: String,
    val relationship: String,
    val childId: Long,
    val childName: String,
    val initials: String = "CG",
    val lastMessage: String?,
    val lastMessageAtIso: String?,
    val lastMessageTime: String,
    val unreadCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    childId: Long = 0L,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isTamil = currentLanguage == "ta"

    val currentUserId = remember { SessionManager.getUserId() }
    val currentUsername = remember { SessionManager.getUsername().ifBlank { "Therapist" } }

    var selectedCaregiver by remember { mutableStateOf<CaregiverContact?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<MessageResponse>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isLoadingContacts by remember { mutableStateOf(true) }
    var isLoadingChat by remember { mutableStateOf(false) }
    var chatLoadError by remember { mutableStateOf<String?>(null) }
    var chatReloadTrigger by remember { mutableStateOf(0) }

    var caregiverContacts by remember { mutableStateOf<List<CaregiverContact>>(emptyList()) }
    val wsConnected by ChatWebSocketClient.isConnected.collectAsState()
    val listState = rememberLazyListState()

    // Helper to keep contacts sorted: most recent message at top, no-message contacts below alphabetically
    fun sortContactsByRecency(list: List<CaregiverContact>): List<CaregiverContact> {
        return list.sortedWith { a, b ->
            val ta = a.lastMessageAtIso
            val tb = b.lastMessageAtIso
            when {
                ta == null && tb == null -> a.caregiverName.compareTo(b.caregiverName, ignoreCase = true)
                ta == null -> 1
                tb == null -> -1
                else -> tb.compareTo(ta)
            }
        }
    }

    suspend fun refreshContactsFromBackend(autoOpenChildId: Long = 0L) {
        try {
            val res = ApiClient.apiService.getTherapistCaregivers()
            if (res.isSuccessful && res.body() != null) {
                val mapped = res.body()!!.map { item ->
                    val nameParts = item.fullName.trim().split(" ").filter { it.isNotBlank() }
                    val inits = when {
                        nameParts.size >= 2 -> "${nameParts[0].take(1)}${nameParts[1].take(1)}".uppercase()
                        nameParts.size == 1 -> nameParts[0].take(2).uppercase()
                        else -> "CG"
                    }
                    CaregiverContact(
                        caregiverId = item.id,
                        caregiverName = item.fullName,
                        relationship = item.relationship.ifBlank { "Parent" },
                        childId = item.childId,
                        childName = item.childName,
                        initials = inits,
                        lastMessage = item.lastMessage,
                        lastMessageAtIso = item.lastMessageAt,
                        lastMessageTime = formatChatTime(item.lastMessageAt),
                        unreadCount = if (selectedCaregiver?.childId == item.childId) 0 else item.unreadMessagesCount
                    )
                }
                caregiverContacts = sortContactsByRecency(mapped)
                if (autoOpenChildId > 0L && selectedCaregiver == null) {
                    selectedCaregiver = caregiverContacts.find { it.childId == autoOpenChildId }
                }
            }
        } catch (_: Exception) {
        } finally {
            isLoadingContacts = false
        }
    }

    // Back button protection
    BackHandler {
        if (selectedCaregiver != null) {
            selectedCaregiver = null
        } else {
            onBack()
        }
    }

    // Ensure WebSocket connects and load real Caregiver Directory from backend
    LaunchedEffect(Unit) {
        ChatWebSocketClient.ensureConnected()
        isLoadingContacts = true
        refreshContactsFromBackend(autoOpenChildId = childId)
    }

    // Listen to real-time WebSocket events (New Messages, Read Receipts, Directory Updates)
    LaunchedEffect(Unit) {
        ChatWebSocketClient.events.collect { event ->
            when (event) {
                is ChatWsEvent.NewMessage -> {
                    val incoming = event.message
                    val activeCg = selectedCaregiver

                    // Update active chat stream if this message belongs to the open child thread
                    if (activeCg != null && incoming.childId == activeCg.childId) {
                        val updated = messages.toMutableList()
                        // Remove optimistic local placeholder matching same sender + text + negative id
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

                        // If message came from partner while we have this chat open, mark read immediately
                        if (incoming.senderId != currentUserId) {
                            ChatWebSocketClient.markRead(incoming.childId)
                        }
                    }

                    // Update contacts list snippet, timestamp, unread count, and re-sort most recent at top
                    val isCurrentlyViewing = activeCg?.childId == incoming.childId
                    val isFromPartner = incoming.senderId != currentUserId
                    val updatedContacts = caregiverContacts.map { c ->
                        if (c.childId == incoming.childId) {
                            c.copy(
                                lastMessage = incoming.messageText,
                                lastMessageAtIso = incoming.sentAt,
                                lastMessageTime = formatChatTime(incoming.sentAt),
                                unreadCount = if (!isCurrentlyViewing && isFromPartner) c.unreadCount + 1 else if (isCurrentlyViewing) 0 else c.unreadCount
                            )
                        } else c
                    }
                    caregiverContacts = sortContactsByRecency(updatedContacts)
                }

                is ChatWsEvent.MessagesRead -> {
                    if (selectedCaregiver?.childId == event.childId && event.readByUserId != currentUserId) {
                        messages = messages.map { m ->
                            if (m.senderId == currentUserId) m.copy(isRead = true) else m
                        }
                    }
                }

                is ChatWsEvent.ContactsUpdated -> {
                    // Refresh directory counts silently in background
                    refreshContactsFromBackend()
                }
            }
        }
    }

    // Load real DB conversation & clear unread count when a caregiver contact is opened
    LaunchedEffect(selectedCaregiver?.childId, chatReloadTrigger) {
        val cg = selectedCaregiver ?: return@LaunchedEffect
        ChatWebSocketClient.ensureConnected()

        // 1. Immediately clear unread badge locally
        caregiverContacts = caregiverContacts.map {
            if (it.caregiverId == cg.caregiverId && it.childId == cg.childId) it.copy(unreadCount = 0) else it
        }

        // 2. Load conversation history from database (backend atomically marks unread messages as read — avoids lock race!)
        isLoadingChat = true
        chatLoadError = null
        messages = emptyList()
        try {
            val res = ApiClient.apiService.getConversation(cg.childId)
            if (res.isSuccessful && res.body() != null) {
                messages = res.body()!!.sortedWith(compareBy({ it.sentAt.orEmpty() }, { it.id }))
                if (messages.isNotEmpty()) {
                    listState.scrollToItem(messages.size - 1)
                }
            } else {
                chatLoadError = if (isTamil) "செய்திகளை ஏற்ற முடியவில்லை (HTTP ${res.code()})" else "Unable to load messages (HTTP ${res.code()})"
            }
        } catch (e: Exception) {
            chatLoadError = if (isTamil) "நெட்வொர்க் பிழை" else "Unable to load messages: ${e.localizedMessage ?: "Network error"}"
        } finally {
            isLoadingChat = false
        }
    }

    // Silent background sync every 5 seconds while a chat is open (zero loading spinner)
    LaunchedEffect(selectedCaregiver?.childId) {
        val cg = selectedCaregiver ?: return@LaunchedEffect
        while (true) {
            delay(5000L)
            try {
                val res = ApiClient.apiService.getConversation(cg.childId)
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

    val sendMessageAction = {
        if (inputText.isNotBlank() && selectedCaregiver != null) {
            val textToSend = inputText.trim()
            val cg = selectedCaregiver!!
            val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
            val tempId = -System.currentTimeMillis()

            val optimisticMsg = MessageResponse(
                id = tempId,
                senderId = currentUserId,
                senderName = currentUsername,
                receiverId = cg.caregiverId,
                receiverName = cg.caregiverName,
                childId = cg.childId,
                messageText = textToSend,
                isRead = false,
                sentAt = nowIso
            )
            messages = messages + optimisticMsg
            inputText = ""

            // Update directory card immediately and bring to top
            caregiverContacts = sortContactsByRecency(
                caregiverContacts.map {
                    if (it.childId == cg.childId) {
                        it.copy(
                            lastMessage = textToSend,
                            lastMessageAtIso = nowIso,
                            lastMessageTime = formatChatTime(nowIso),
                            unreadCount = 0
                        )
                    } else it
                }
            )

            scope.launch {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
                // Always persist reliably via REST POST /api/messages (backend also broadcasts instantly over WebSocket!)
                try {
                    val res = ApiClient.apiService.sendMessage(
                        SendMessageRequest(
                            receiverId = cg.caregiverId,
                            childId = cg.childId,
                            messageText = textToSend
                        )
                    )
                    if (res.isSuccessful && res.body() != null) {
                        val saved = res.body()!!
                        val replaced = messages.map { if (it.id == tempId) saved else it }
                        messages = replaced.distinctBy { it.id }.sortedWith(compareBy({ it.sentAt.orEmpty() }, { it.id }))
                    } else {
                        messages = messages.filterNot { it.id == tempId }
                        Toast.makeText(
                            context,
                            if (isTamil) "செய்தி அனுப்புவதில் பிழை (${res.code()})" else "Failed to send message (HTTP ${res.code()})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    messages = messages.filterNot { it.id == tempId }
                    Toast.makeText(
                        context,
                        if (isTamil) "நெட்வொர்க் பிழை" else "Network error sending message",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    val clinicalQuickCues = remember(isTamil) {
        if (isTamil) {
            listOf(
                "பின்நோக்கிய சங்கிலி முறையை முயற்சிக்கவும் 🔗",
                "அடுத்த உதவிக்குறிப்புக்கு முன் 3-5 விநாடிகள் காத்திருக்கவும் ⏱️",
                "இந்த வாரம் வாய்மொழி உதவிகளுக்கு மாற்றவும் 📋",
                "சுயாதீன முயற்சி மிக நன்று! 🌟"
            )
        } else {
            listOf(
                "Try Backward Chaining 🔗",
                "Wait 3-5s before next cue ⏱️",
                "Fade to verbal cues this week 📋",
                "Great independent progress! 🌟"
            )
        }
    }

    Scaffold(
        containerColor = MsgBackground,
        topBar = {
            if (selectedCaregiver == null) {
                // View 1: Premium Caregiver Directory Header with Back Button to Dashboard
                Surface(
                    color = MsgCardBg,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, MsgBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MsgSecondary,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onBack)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Dashboard",
                                    tint = MsgPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = if (isTamil) "மருத்துவ உரையாடல்கள்" else "Clinical Messages",
                                fontSize = 18.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MsgTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = if (isTamil) "பெற்றோர்கள் & குடும்பத்தினருடன் உரையாடல்" else "Caregiver Conversations",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MsgTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LanguageTogglePill()

                            Surface(
                                shape = CircleShape,
                                color = MsgSecondary,
                                border = BorderStroke(1.dp, MsgPrimary.copy(alpha = 0.18f)),
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        scope.launch {
                                            isLoadingContacts = true
                                            ChatWebSocketClient.ensureConnected()
                                            refreshContactsFromBackend()
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = MsgPrimary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // View 2: Premium Individual Chat Header
                val cg = selectedCaregiver!!
                Surface(
                    color = MsgCardBg,
                    shadowElevation = 3.dp,
                    border = BorderStroke(1.dp, MsgBorder),
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
                            color = MsgSecondary,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { selectedCaregiver = null }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MsgPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Avatar with live presence dot
                        Box(modifier = Modifier.size(44.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = MsgSecondary,
                                border = BorderStroke(1.5.dp, MsgPrimary.copy(alpha = 0.28f)),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = cg.initials,
                                        fontSize = 15.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MsgPrimary
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(MsgCardBg)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(MsgSuccess)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cg.caregiverName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MsgTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "${cg.relationship} of ${cg.childName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MsgTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        LanguageTogglePill()
                    }
                }
            }
        },
        bottomBar = {
            if (selectedCaregiver != null) {
                val cg = selectedCaregiver!!
                // Clean, Modern Bottom Message Composer (No double bottom inset, symmetrical 44dp pill + send button)
                Surface(
                    color = MsgCardBg,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(color = MsgBorder, thickness = 1.dp)

                        // Sleek Quick Clinical Cues Strip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            clinicalQuickCues.forEach { cue ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MsgSecondary,
                                    border = BorderStroke(1.dp, MsgPrimary.copy(alpha = 0.16f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { inputText = cue }
                                ) {
                                    Text(
                                        text = cue,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MsgPrimary,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        // Symmetrical Input Pill + Circular Send Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 14.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = MsgBackground,
                                border = BorderStroke(
                                    1.dp,
                                    if (inputText.isNotBlank()) MsgPrimary.copy(alpha = 0.55f) else MsgBorder
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
                                    if (inputText.isEmpty()) {
                                        Text(
                                            text = if (isTamil) "செய்தியை தட்டச்சு செய்க..." else "Message ${cg.caregiverName}...",
                                            fontSize = 14.sp,
                                            color = MsgTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    BasicTextField(
                                        value = inputText,
                                        onValueChange = { inputText = it },
                                        textStyle = TextStyle(
                                            color = MsgTextPrimary,
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp
                                        ),
                                        cursorBrush = SolidColor(MsgPrimary),
                                        maxLines = 4,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                        keyboardActions = KeyboardActions(onSend = { sendMessageAction() }),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Surface(
                                shape = CircleShape,
                                color = if (inputText.isNotBlank()) MsgPrimary else MsgSecondary,
                                shadowElevation = if (inputText.isNotBlank()) 3.dp else 0.dp,
                                modifier = Modifier.size(44.dp)
                            ) {
                                IconButton(
                                    onClick = { sendMessageAction() },
                                    enabled = inputText.isNotBlank(),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = if (inputText.isNotBlank()) Color.White else MsgTextSecondary.copy(alpha = 0.7f),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MsgBackground)
        ) {
            if (selectedCaregiver == null) {
                // ==================== VIEW 1: PREMIUM CAREGIVER CONTACTS DIRECTORY ====================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    // Rounded Modern Search Bar
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MsgCardBg,
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, MsgBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = if (isTamil) "பெற்றோர் அல்லது குழந்தை பெயரை தேடவும்..." else "Search caregiver or child...",
                                    fontSize = 13.5.sp,
                                    color = MsgTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MsgPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = MsgTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MsgCardBg,
                                unfocusedContainerColor = MsgCardBg,
                                focusedTextColor = MsgTextPrimary,
                                unfocusedTextColor = MsgTextPrimary,
                                focusedBorderColor = MsgPrimary,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = MsgPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTamil) "அனைத்து உரையாடல்கள்" else "RECENT CONVERSATIONS",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MsgTextSecondary,
                            letterSpacing = 0.8.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MsgSecondary,
                            modifier = Modifier.width(IntrinsicSize.Max)
                        ) {
                            Text(
                                text = "${caregiverContacts.size} " + (if (isTamil) "தொடர்புகள்" else "Contacts"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MsgPrimary,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val filteredList = caregiverContacts.filter {
                        searchQuery.isBlank() ||
                            it.caregiverName.contains(searchQuery, ignoreCase = true) ||
                            it.childName.contains(searchQuery, ignoreCase = true)
                    }

                    if (isLoadingContacts && caregiverContacts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MsgPrimary, strokeWidth = 3.dp)
                        }
                    } else if (filteredList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp), contentAlignment = Alignment.Center) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = MsgCardBg,
                                border = BorderStroke(1.dp, MsgBorder),
                                shadowElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MsgSecondary,
                                        modifier = Modifier.size(64.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.ChatBubbleOutline,
                                                contentDescription = null,
                                                tint = MsgPrimary,
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = if (isTamil) "தொடர்புகள் எதுவும் இல்லை" else "No caregiver conversations found",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MsgTextPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isTamil) "வேறு பெயரைத் தேடிப் பார்க்கவும்" else "Try adjusting your search or check back when caregivers are linked.",
                                        fontSize = 12.5.sp,
                                        color = MsgTextSecondary,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 120.dp)
                        ) {
                            itemsIndexed(
                                filteredList,
                                key = { index, contact -> "${contact.caregiverId}_${contact.childId}_$index" }
                            ) { _, contact ->
                                val hasUnread = contact.unreadCount > 0
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MsgCardBg,
                                    border = BorderStroke(
                                        width = if (hasUnread) 1.5.dp else 1.dp,
                                        color = if (hasUnread) MsgPrimary.copy(alpha = 0.45f) else MsgBorder
                                    ),
                                    shadowElevation = if (hasUnread) 3.dp else 1.5.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { selectedCaregiver = contact }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Refined Circular Avatar
                                        Surface(
                                            shape = CircleShape,
                                            color = if (hasUnread) MsgPrimary else MsgSecondary,
                                            border = BorderStroke(
                                                1.dp,
                                                if (hasUnread) MsgPrimary else MsgPrimary.copy(alpha = 0.20f)
                                            ),
                                            modifier = Modifier.size(50.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = contact.initials,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (hasUnread) Color.White else MsgPrimary
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            // Name + Timestamp
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = contact.caregiverName,
                                                    fontSize = 15.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MsgTextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(end = 8.dp)
                                                )
                                                if (contact.lastMessageTime.isNotBlank()) {
                                                    Text(
                                                        text = contact.lastMessageTime,
                                                        fontSize = 11.sp,
                                                        color = if (hasUnread) MsgPrimary else MsgTextSecondary,
                                                        fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Medium,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(3.dp))

                                            // Child & Relationship Tag
                                            Text(
                                                text = "${contact.relationship} of ${contact.childName}",
                                                fontSize = 12.sp,
                                                color = MsgPrimary,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Spacer(modifier = Modifier.height(5.dp))

                                            // Preview + Unread Pill
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val preview = contact.lastMessage
                                                Text(
                                                    text = preview ?: if (isTamil) "உரையாடலைத் தொடங்க தட்டவும்" else "No messages yet — tap to start chat",
                                                    fontSize = 12.8.sp,
                                                    fontStyle = if (preview == null) FontStyle.Italic else FontStyle.Normal,
                                                    color = if (hasUnread) MsgTextPrimary else MsgTextSecondary,
                                                    fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )

                                                if (hasUnread) {
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(11.dp),
                                                        color = MsgPrimary,
                                                        modifier = Modifier.defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
                                                    ) {
                                                        Box(
                                                            contentAlignment = Alignment.Center,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = "${contact.unreadCount}",
                                                                fontSize = 10.5.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White,
                                                                maxLines = 1,
                                                                softWrap = false
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
            } else {
                // ==================== VIEW 2: PREMIUM REAL-TIME CHAT THREAD ====================
                val cg = selectedCaregiver!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MsgBackground)
                ) {
                    // Subtle Patient Care Context Strip
                    Surface(
                        color = MsgSecondary.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, MsgBorder),
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
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = MsgPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Patient: ${cg.childName} • Active ADL Care",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MsgTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MsgSuccess.copy(alpha = 0.14f),
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
                                            .background(MsgSuccess)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Active",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MsgSuccess,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    // Messages Stream (Incoming Left, Outgoing Right)
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        if (isLoadingChat && messages.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MsgPrimary, strokeWidth = 3.dp)
                            }
                        } else if (chatLoadError != null && messages.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = MsgCardBg,
                                    border = BorderStroke(1.dp, MsgBorder),
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
                                            color = MsgTextPrimary,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Button(
                                            onClick = { chatReloadTrigger++ },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MsgPrimary)
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
                                    color = MsgCardBg,
                                    border = BorderStroke(1.dp, MsgBorder),
                                    shadowElevation = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MsgSecondary,
                                            modifier = Modifier.size(60.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Chat,
                                                    contentDescription = null,
                                                    tint = MsgPrimary,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = if (isTamil) "இன்னும் செய்திகள் இல்லை" else "No messages yet",
                                            fontSize = 15.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MsgTextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (isTamil) "${cg.caregiverName} உடன் உரையாடலைத் தொடங்குங்கள்." else "Send a clinical update or cue to ${cg.caregiverName}.",
                                            fontSize = 12.5.sp,
                                            color = MsgTextSecondary,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 18.sp
                                        )
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
                                            color = if (isMe) MsgPrimary else MsgCardBg,
                                            border = if (isMe) null else BorderStroke(1.dp, MsgBorder),
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
                                                        color = MsgPrimary
                                                    )
                                                    Spacer(modifier = Modifier.height(3.dp))
                                                }

                                                Text(
                                                    text = msg.messageText,
                                                    fontSize = 14.sp,
                                                    color = if (isMe) Color.White else MsgTextPrimary,
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
                                                        color = if (isMe) Color.White.copy(alpha = 0.80f) else MsgTextSecondary
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
        }
    }
}
