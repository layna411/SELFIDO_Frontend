package com.simats.selfora.ui.caregiver

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.model.NotificationItem
import com.simats.selfora.data.repository.NotificationRepository
import com.simats.selfora.ui.components.glass.GlassIconButton
import com.simats.selfora.ui.components.glass.GlassTopBar
import com.simats.selfora.ui.theme.*
import com.simats.selfora.utils.LanguageManager
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CaregiverNotificationsScreen(
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isTamil = currentLanguage == "ta"

    val notifications by NotificationRepository.notifications.collectAsState()
    val unreadCount by NotificationRepository.unreadCount.collectAsState()
    val isLoading by NotificationRepository.isLoading.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL" or "UNREAD"

    LaunchedEffect(Unit) {
        NotificationRepository.fetchNotifications()
    }

    val filteredList = remember(notifications, selectedFilter) {
        if (selectedFilter == "UNREAD") {
            notifications.filter { !it.isRead }
        } else {
            notifications
        }
    }

    Scaffold(
        containerColor = SelforaBackground,
        topBar = {
            GlassTopBar(
                title = if (isTamil) "அறிவிப்புகள்" else "Notifications",
                subtitle = if (unreadCount > 0) {
                    if (isTamil) "$unreadCount புதிய அறிவிப்புகள்" else "$unreadCount unread"
                } else {
                    if (isTamil) "அனைத்தும் படிக்கப்பட்டது" else "All caught up"
                },
                onBackClick = onBack,
                accentColor = SelforaPrimary,
                actions = {
                    if (unreadCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SelforaPrimary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, SelforaPrimary.copy(alpha = 0.3f)),
                            modifier = Modifier.clickable {
                                coroutineScope.launch {
                                    NotificationRepository.markAllAsRead()
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Mark All Read",
                                    tint = SelforaPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isTamil) "படித்ததாகக் குறி" else "Mark All Read",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaPrimary
                                )
                            }
                        }
                    }
                    GlassIconButton(
                        icon = Icons.Default.Refresh,
                        onClick = {
                            coroutineScope.launch {
                                NotificationRepository.fetchNotifications()
                            }
                        },
                        tint = SelforaPrimary,
                        size = 36.dp,
                        iconSize = 18.dp
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SelforaBackground)
        ) {
            // Filter Pills Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // All Filter Tab
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (selectedFilter == "ALL") SelforaPrimary else Color.White,
                    border = BorderStroke(1.dp, if (selectedFilter == "ALL") SelforaPrimary else Color(0xFFE2E8F0)),
                    modifier = Modifier.clickable { selectedFilter = "ALL" }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTamil) "அனைத்தும்" else "All",
                            color = if (selectedFilter == "ALL") Color.White else SelforaTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (selectedFilter == "ALL") Color.White.copy(alpha = 0.25f) else Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = "${notifications.size}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (selectedFilter == "ALL") Color.White else SelforaTextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // Unread Filter Tab
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (selectedFilter == "UNREAD") SelforaPrimary else Color.White,
                    border = BorderStroke(1.dp, if (selectedFilter == "UNREAD") SelforaPrimary else Color(0xFFE2E8F0)),
                    modifier = Modifier.clickable { selectedFilter = "UNREAD" }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTamil) "படிக்காதவை" else "Unread",
                            color = if (selectedFilter == "UNREAD") Color.White else SelforaTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = if (selectedFilter == "UNREAD") Color.White else Color(0xFFEF4444)
                            ) {
                                Text(
                                    text = "$unreadCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (selectedFilter == "UNREAD") SelforaPrimary else Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (isLoading && notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SelforaPrimary)
                }
            } else if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = SelforaPrimary.copy(alpha = 0.08f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = null,
                                    tint = SelforaPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (isTamil) "அறிவிப்புகள் எதுவும் இல்லை" else "No Notifications Yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isTamil) "புதிய அறிவிப்புகள் வரும்போது இங்கு தோன்றும்." else "New clinical updates and practice reminders will appear here in real-time.",
                            fontSize = 12.sp,
                            color = SelforaTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
                ) {
                    itemsIndexed(filteredList, key = { index, item -> "${item.id}_$index" }) { _, item ->
                        val (icon, tintBg, tintIcon) = getNotificationIconInfo(item.notificationType)
                        val formattedTime = formatTimestamp(item.createdAt, isTamil)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!item.isRead) {
                                        coroutineScope.launch {
                                            NotificationRepository.markAsRead(item.id)
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (!item.isRead) Color.White else Color(0xFFFBFDFF)
                            ),
                            border = BorderStroke(
                                width = if (!item.isRead) 1.5.dp else 1.dp,
                                color = if (!item.isRead) SelforaPrimary.copy(alpha = 0.45f) else Color(0xFFE2E8F0)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (!item.isRead) 2.dp else 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = tintBg,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = tintIcon,
                                            modifier = Modifier.size(22.dp)
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
                                            text = item.title,
                                            fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = SelforaTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = formattedTime,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (!item.isRead) SelforaPrimary else SelforaTextSecondary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = item.message,
                                        fontSize = 12.sp,
                                        color = if (!item.isRead) SelforaTextPrimary.copy(alpha = 0.85f) else SelforaTextSecondary,
                                        lineHeight = 17.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Status action badge
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (!item.isRead) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = SelforaPrimary.copy(alpha = 0.12f),
                                                border = BorderStroke(1.dp, SelforaPrimary.copy(alpha = 0.35f)),
                                                modifier = Modifier.clickable {
                                                    coroutineScope.launch {
                                                        NotificationRepository.markAsRead(item.id)
                                                    }
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(SelforaPrimary)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = if (isTamil) "புதியது • படிக்க" else "New • Mark Read",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SelforaPrimary
                                                    )
                                                }
                                            }
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFF1F5F9),
                                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color(0xFF10B981),
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = if (isTamil) "படிக்கப்பட்டது" else "Read",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = SelforaTextSecondary
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

private fun getNotificationIconInfo(type: String): Triple<ImageVector, Color, Color> {
    return when (type.uppercase()) {
        "PRACTICE_SUBMITTED" -> Triple(Icons.Default.AssignmentTurnedIn, Color(0xFFE6FFFA), Color(0xFF0D9488))
        "PROGRAMME_ASSIGNED", "PROGRAMME" -> Triple(Icons.Default.Assignment, Color(0xFFEFF6FF), Color(0xFF2563EB))
        "PRACTICE_REMINDER", "REMINDER" -> Triple(Icons.Default.Alarm, Color(0xFFFEF3C7), Color(0xFFD97706))
        "FEEDBACK_AVAILABLE", "INSTRUCTION" -> Triple(Icons.Default.Chat, Color(0xFFECFDF5), Color(0xFF059669))
        "ASSESSMENT_DUE" -> Triple(Icons.Default.Assessment, Color(0xFFF3E8FF), Color(0xFF9333EA))
        else -> Triple(Icons.Default.Notifications, Color(0xFFF1F5F9), Color(0xFF475569))
    }
}

private fun formatTimestamp(isoString: String?, isTamil: Boolean): String {
    if (isoString.isNullOrBlank()) return if (isTamil) "இப்போது" else "Just now"
    return try {
        val parsed = LocalDateTime.parse(isoString.substring(0, minOf(19, isoString.length)))
        val now = LocalDateTime.now()
        val duration = Duration.between(parsed, now)
        val minutes = duration.toMinutes()
        val hours = duration.toHours()
        val days = duration.toDays()

        when {
            minutes < 1 -> if (isTamil) "இப்போது" else "Just now"
            minutes < 60 -> if (isTamil) "$minutes நிமிடங்களுக்கு முன்" else "${minutes}m ago"
            hours < 24 -> if (isTamil) "$hours மணிநேரத்திற்கு முன்" else "${hours}h ago"
            days == 1L -> if (isTamil) "நேற்று" else "Yesterday"
            days < 7 -> if (isTamil) "$days நாட்களுக்கு முன்" else "${days}d ago"
            else -> parsed.format(DateTimeFormatter.ofPattern("dd MMM"))
        }
    } catch (_: Exception) {
        if (isTamil) "இப்போது" else "Just now"
    }
}
