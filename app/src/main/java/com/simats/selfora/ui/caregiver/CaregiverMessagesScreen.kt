package com.simats.selfora.ui.caregiver

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import com.simats.selfora.data.local.SessionManager
import com.simats.selfora.data.model.MessageResponse
import com.simats.selfora.data.model.SendMessageRequest
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*
import com.simats.selfora.utils.LanguageManager
import kotlinx.coroutines.launch

@Composable
fun CaregiverMessagesScreen(
    onNavigateToTab: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    // Intercept hardware and gesture back presses so the app never exits accidentally
    BackHandler {
        onBack()
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isTamil = currentLanguage == "ta"

    val activeChildId = remember {
        SessionManager.getActiveChildId().let { if (it > 0L) it else 1L }
    }
    val activeChildName = remember {
        SessionManager.getActiveChildName()
    }
    val currentUserName = remember {
        SessionManager.getUsername().ifBlank { "Meena (Caregiver)" }
    }
    val currentUserId = remember {
        SessionManager.getUserId().let { if (it > 0L) it else 1L }
    }

    var messages by remember {
        mutableStateOf(
            listOf(
                MessageResponse(
                    id = 1L,
                    senderId = 2L,
                    senderName = "Dr. Priya Shanmugam, MOT",
                    receiverId = currentUserId,
                    receiverName = currentUserName,
                    childId = activeChildId,
                    messageText = if (isTamil)
                        "வணக்கம்! $activeChildName தினசரி வீட்டுப் பயிற்சியில் மிகச் சிறப்பாக முன்னேறுகிறார். வழிகாட்டப்பட்டபடி உடல் உதவிகளை (Physical Prompts) படிப்படியாகக் குறைத்து சைகை உதவிகளுக்கு (Gestural Prompts) மாற்றவும்."
                    else
                        "Hello! $activeChildName is making steady progress with daily home practice routines. Please continue fading physical prompts gradually towards gestural cues as outlined in the clinical plan.",
                    isRead = true,
                    sentAt = "Today, 9:30 AM"
                ),
                MessageResponse(
                    id = 2L,
                    senderId = 2L,
                    senderName = "Dr. Priya Shanmugam, MOT",
                    receiverId = currentUserId,
                    receiverName = currentUserName,
                    childId = activeChildId,
                    messageText = if (isTamil)
                        "ஆடை அணிதல் மற்றும் பல் துலக்குதல் படிநிலை வழிகாட்டி PDF ஆவணம் கீழே இணைக்கப்பட்டுள்ளது. தேவைப்படும்போது சரிபார்க்கவும்."
                    else
                        "I have attached the complete step-by-step visual dressing and brushing guidance guide below for reference.",
                    isRead = true,
                    sentAt = "Today, 9:32 AM"
                )
            )
        )
    }

    var newMessageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(activeChildId) {
        try {
            val res = ApiClient.apiService.getConversation(activeChildId)
            if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                messages = res.body()!!
            }
        } catch (_: Exception) {}
    }

    val sendCaregiverMessage = {
        if (newMessageText.isNotBlank()) {
            val textToSend = newMessageText.trim()
            val newMsg = MessageResponse(
                id = (messages.size + 1).toLong(),
                senderId = currentUserId,
                senderName = currentUserName,
                receiverId = 2L,
                receiverName = "Dr. Priya Shanmugam, MOT",
                childId = activeChildId,
                messageText = textToSend,
                isRead = false,
                sentAt = if (isTamil) "இப்போது" else "Just now"
            )
            messages = messages + newMsg
            newMessageText = ""
            coroutineScope.launch {
                listState.animateScrollToItem((messages.size + 1).coerceAtLeast(0))
                try {
                    ApiClient.apiService.sendMessage(
                        SendMessageRequest(
                            receiverId = 2L,
                            childId = activeChildId,
                            messageText = textToSend
                        )
                    )
                } catch (_: Exception) {}
            }
        }
    }

    val quickReplies = remember(isTamil) {
        if (isTamil) {
            listOf(
                "குழந்தை இன்று படிகளை முடித்தது! 🌟",
                "பொத்தான்களுக்கு கூடுதல் உதவி தேவை 👕",
                "உதவிகள் வெற்றிகரமாக குறைக்கப்பட்டது 👍",
                "சமீபத்திய பயிற்சி வீடியோவை மதிப்பாய்வு செய்யவும் 📋"
            )
        } else {
            listOf(
                "Child completed today's steps! 🌟",
                "Needs more practice with buttons 👕",
                "Reduced physical prompts successfully 👍",
                "Please review recent session video 📋"
            )
        }
    }

    Scaffold(
        containerColor = SelforaBackground,
        topBar = {
            GlassTopBar(
                title = if (isTamil) "மருத்துவ செய்திகள்" else "Clinical Messages",
                subtitle = if (isTamil) "Dr. பிரியா OT • நேரடி வழிகாட்டுதல்" else "Dr. Priya OT • Direct Guidance",
                onBackClick = onBack,
                accentColor = SelforaPrimary,
                showLanguageToggle = true,
                actions = {
                    GlassIconButton(
                        icon = Icons.Default.Refresh,
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val res = ApiClient.apiService.getConversation(activeChildId)
                                    if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                                        messages = res.body()!!
                                    }
                                } catch (_: Exception) {}
                            }
                        },
                        tint = SelforaPrimary,
                        size = 36.dp,
                        iconSize = 18.dp
                    )
                }
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    // Quick Reply Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickReplies.forEach { reply ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.clickable {
                                    newMessageText = reply
                                }
                            ) {
                                Text(
                                    text = reply,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SelforaTextPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newMessageText,
                            onValueChange = { newMessageText = it },
                            placeholder = {
                                Text(
                                    text = if (isTamil) "மருத்துவரிடம் செய்தி அனுப்பவும்..." else "Message Dr. Priya OT...",
                                    fontSize = 13.5.sp,
                                    color = SelforaTextSecondary
                                )
                            },
                            modifier = Modifier.weight(1f),
                            maxLines = 3,
                            shape = RoundedCornerShape(24.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { sendCaregiverMessage() }),
                            colors = OutlinedTextFieldDefaults.colors(
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
                                .size(46.dp)
                                .background(
                                    if (newMessageText.isNotBlank()) SelforaPrimary else Color(0xFFCBD5E1),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Professional Clinical Profile Header Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SelforaPrimary.copy(alpha = 0.10f),
                                border = BorderStroke(1.5.dp, SelforaPrimary.copy(alpha = 0.25f)),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.HealthAndSafety,
                                        contentDescription = null,
                                        tint = SelforaPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Dr. Priya Shanmugam, MOT",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SelforaTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified Therapist",
                                        tint = SelforaPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isTamil) "முதுநிலை குழந்தைசார் தொழில்முறை சிகிச்சையாளர்" else "Senior Pediatric Occupational Therapist",
                                    fontSize = 11.5.sp,
                                    color = SelforaTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Patient Context & Security Tag
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ChildCare,
                                        contentDescription = null,
                                        tint = SelforaSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isTamil) "நோயாளி: $activeChildName (ஐடி #$activeChildId)" else "Patient: $activeChildName (ID #$activeChildId)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SelforaTextPrimary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isTamil) "ஆன்லைன்" else "Active",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Prescription / Step Guide Attachment Card
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Toast.makeText(
                                context,
                                if (isTamil) "ஆடை அணிதல் படி வழிகாட்டி PDF திறக்கப்படுகிறது..." else "Opening Clinical Step Guide PDF...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFEE2E2),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = "PDF Guide",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isTamil) "ஆடை அணிதல் படிநிலை வழிகாட்டி" else "Visual Dressing Step Guide",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isTamil) "1.4 MB • மருத்துவர் பரிந்துரைத்தது" else "1.4 MB • Prescribed Clinical Reference",
                                    fontSize = 11.sp,
                                    color = SelforaTextSecondary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SelforaPrimary.copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, SelforaPrimary.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = SelforaPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isTamil) "பார்க்க" else "View",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Message Items
            itemsIndexed(messages, key = { index, msg -> "${msg.id}_${msg.sentAt}_$index" }) { _, msg ->
                val isCaregiverMsg = msg.senderId == currentUserId || msg.senderName.contains("Caregiver", ignoreCase = true)

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
                        shadowElevation = 1.5.dp,
                        border = BorderStroke(
                            1.dp,
                            if (isCaregiverMsg) SelforaPrimary else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.widthIn(max = 310.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (!isCaregiverMsg) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Text(
                                        text = msg.senderName,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SelforaPrimary
                                    )
                                }
                            }

                            Text(
                                text = msg.messageText,
                                fontSize = 13.5.sp,
                                color = if (isCaregiverMsg) Color.White else SelforaTextPrimary,
                                lineHeight = 19.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.align(Alignment.End),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = msg.sentAt,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isCaregiverMsg) Color.White.copy(alpha = 0.80f) else SelforaTextSecondary
                                )
                                if (isCaregiverMsg) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Delivered",
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(12.dp)
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
