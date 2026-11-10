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
import androidx.compose.ui.text.font.FontStyle
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
                        messages = updated.sortedWith(compareBy({ it.sentAt }, { it.id }))
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
    LaunchedEffect(selectedCaregiver?.childId) {
        val cg = selectedCaregiver ?: return@LaunchedEffect
        ChatWebSocketClient.ensureConnected()

        // 1. Immediately clear unread badge locally
        caregiverContacts = caregiverContacts.map {
            if (it.caregiverId == cg.caregiverId && it.childId == cg.childId) it.copy(unreadCount = 0) else it
        }

        // 2. Mark messages read in backend via WebSocket & REST
        ChatWebSocketClient.markRead(cg.childId)
        scope.launch {
            try { ApiClient.apiService.markConversationAsRead(cg.childId) } catch (_: Exception) {}
        }

        // 3. Load actual conversation history from database
        isLoadingChat = true
        messages = emptyList()
        try {
            val res = ApiClient.apiService.getConversation(cg.childId)
            if (res.isSuccessful && res.body() != null) {
                messages = res.body()!!.sortedWith(compareBy({ it.sentAt }, { it.id }))
                if (messages.isNotEmpty()) {
                    listState.scrollToItem(messages.size - 1)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to load messages: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoadingChat = false
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
                val sentViaWs = ChatWebSocketClient.sendMessage(cg.caregiverId, cg.childId, textToSend)
                if (!sentViaWs) {
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
        containerColor = SelforaBackground, // Exact SELFORA App Theme Background
        topBar = {
            if (selectedCaregiver == null) {
                // View 1: Caregiver Contacts Directory Header (SELFORA Theme)
                GlassTopBar(
                    title = if (isTamil) "மருத்துவ உரையாடல்கள்" else "Clinical Messages",
                    subtitle = if (wsConnected) {
                        if (isTamil) "நேரடி WebSocket இணைப்பு • பெற்றோர்கள்" else "Live WebSocket Connected • Caregiver Chats"
                    } else {
                        if (isTamil) "பெற்றோர்கள் & குடும்பத்தினருடன் தொடர்பு" else "Caregiver Consultation Directory"
                    },
                    accentColor = SelforaPrimary,
                    showLanguageToggle = true,
                    actions = {
                        GlassIconButton(
                            icon = Icons.Default.Refresh,
                            onClick = {
                                scope.launch {
                                    isLoadingContacts = true
                                    ChatWebSocketClient.ensureConnected()
                                    refreshContactsFromBackend()
                                }
                            },
                            tint = SelforaPrimary
                        )
                    }
                )
            } else {
                // View 2: SELFORA Theme Chat Header (Crisp white top bar with primary accents)
                val cg = selectedCaregiver!!
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
                            onClick = { selectedCaregiver = null },
                            tint = SelforaPrimary,
                            size = 36.dp,
                            iconSize = 18.dp
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = CircleShape,
                            color = SelforaSecondary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, SelforaSecondary.copy(alpha = 0.35f)),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = cg.initials,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cg.caregiverName,
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
                                    text = "${cg.relationship} of ${cg.childName}",
                                    fontSize = 11.5.sp,
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
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedCaregiver == null) {
                // ==================== VIEW 1: ENTIRE CONTACTS (CAREGIVER) DIRECTORY ====================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Search Bar (SELFORA Theme)
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = if (isTamil) "பெற்றோர் அல்லது குழந்தை பெயரை தேடவும்..." else "Search caregiver or child...",
                                fontSize = 13.sp,
                                color = SelforaTextSecondary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = SelforaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = SelforaTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = SelforaTextPrimary,
                            unfocusedTextColor = SelforaTextPrimary,
                            focusedBorderColor = SelforaPrimary,
                            unfocusedBorderColor = SelforaBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTamil) "அனைத்து உரையாடல்கள்" else "Caregiver Conversations",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SelforaPrimary.copy(alpha = 0.10f)
                        ) {
                            Text(
                                text = "${caregiverContacts.size} " + (if (isTamil) "தொடர்புகள்" else "Contacts"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val filteredList = caregiverContacts.filter {
                        searchQuery.isBlank() ||
                            it.caregiverName.contains(searchQuery, ignoreCase = true) ||
                            it.childName.contains(searchQuery, ignoreCase = true)
                    }

                    if (isLoadingContacts && caregiverContacts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = SelforaPrimary)
                        }
                    } else if (filteredList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = SelforaTextSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (isTamil) "தொடர்புகள் எதுவும் இல்லை" else "No caregiver conversations found",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SelforaTextSecondary
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 120.dp)
                        ) {
                            itemsIndexed(
                                filteredList,
                                key = { index, contact -> "${contact.caregiverId}_${contact.childId}_$index" }
                            ) { _, contact ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White,
                                    border = BorderStroke(
                                        width = if (contact.unreadCount > 0) 1.5.dp else 1.dp,
                                        color = if (contact.unreadCount > 0) SelforaPrimary.copy(alpha = 0.45f) else SelforaBorder
                                    ),
                                    shadowElevation = 1.5.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedCaregiver = contact }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = SelforaSecondary.copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, SelforaSecondary.copy(alpha = 0.30f)),
                                            modifier = Modifier.size(46.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = contact.initials,
                                                    fontSize = 15.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SelforaSecondary
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = contact.caregiverName,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SelforaTextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (contact.lastMessageTime.isNotBlank()) {
                                                    Text(
                                                        text = contact.lastMessageTime,
                                                        fontSize = 11.sp,
                                                        color = if (contact.unreadCount > 0) SelforaPrimary else SelforaTextSecondary,
                                                        fontWeight = if (contact.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Text(
                                                text = "${contact.relationship} of ${contact.childName}",
                                                fontSize = 11.5.sp,
                                                color = SelforaPrimary,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val preview = contact.lastMessage
                                                Text(
                                                    text = preview ?: if (isTamil) "உரையாடலைத் தொடங்க தட்டவும்" else "No messages yet — tap to start chat",
                                                    fontSize = 12.sp,
                                                    fontStyle = if (preview == null) FontStyle.Italic else FontStyle.Normal,
                                                    color = if (contact.unreadCount > 0) SelforaTextPrimary else SelforaTextSecondary,
                                                    fontWeight = if (contact.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )

                                                // Unread badge only visible when unreadCount > 0, disappears on read!
                                                if (contact.unreadCount > 0) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = SelforaPrimary,
                                                        modifier = Modifier.size(20.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = "${contact.unreadCount}",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White
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
                // ==================== VIEW 2: SELFORA-THEMED REAL-TIME CHAT (WHATSAPP ALIGNMENT ONLY) ====================
                val cg = selectedCaregiver!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SelforaBackground)
                ) {
                    // Subtle Patient Context Pill
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = SelforaPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Patient: ${cg.childName} • Active ADL Care",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SelforaTextPrimary
                                )
                            }
                            Text(
                                text = if (wsConnected) "● Live WebSocket" else "Connecting...",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (wsConnected) SelforaSuccess else SelforaWarning
                            )
                        }
                    }

                    // Messages Stream (Clean Alignment: Outgoing Right, Incoming Left)
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        if (isLoadingChat) {
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
                                        text = if (isTamil) "${cg.caregiverName} உடன் உரையாடலைத் தொடங்குங்கள்." else "Send a clinical update or cue to ${cg.caregiverName}.",
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
                                                        color = SelforaSecondary
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

                    // Bottom Input Bar (SELFORA Theme, Clean Alignment)
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
                                clinicalQuickCues.forEach { cue ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = SelforaPrimary.copy(alpha = 0.08f),
                                        border = BorderStroke(1.dp, SelforaPrimary.copy(alpha = 0.25f)),
                                        modifier = Modifier.clickable { inputText = cue }
                                    ) {
                                        Text(
                                            text = cue,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = SelforaPrimary,
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
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    placeholder = {
                                        Text(
                                            text = if (isTamil) "செய்தியை தட்டச்சு செய்க..." else "Message ${cg.caregiverName}...",
                                            fontSize = 13.sp,
                                            color = SelforaTextSecondary
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    maxLines = 4,
                                    shape = RoundedCornerShape(22.dp),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = { sendMessageAction() }),
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
                                    onClick = { sendMessageAction() },
                                    enabled = inputText.isNotBlank(),
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            if (inputText.isNotBlank()) SelforaPrimary else Color(0xFFCBD5E1),
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
            }
        }
    }
}
