package com.simats.selfora.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SendMessageRequest(
    val receiverId: Long,
    val childId: Long,
    val messageText: String
)

data class MessageResponse(
    val id: Long,
    val senderId: Long,
    val senderName: String,
    val receiverId: Long,
    val receiverName: String,
    val childId: Long,
    val messageText: String,
    val isRead: Boolean,
    val sentAt: String
)

data class ChatPartnerResponse(
    val partnerId: Long,
    val partnerName: String,
    val partnerRole: String,
    val childId: Long,
    val childName: String
)

/**
 * Parses ISO-8601 LocalDateTime strings (e.g. "2026-11-10T11:25:00.12345") into readable clock time ("11:25 AM")
 */
fun formatChatTime(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    // If already formatted like "11:25 AM", return directly
    if (raw.endsWith("AM", ignoreCase = true) || raw.endsWith("PM", ignoreCase = true) || raw.equals("Just now", ignoreCase = true)) {
        return raw
    }
    return try {
        val cleaned = raw.substringBefore(".").replace("T", " ")
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = parser.parse(cleaned)
        if (date != null) {
            val todayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val isToday = todayFmt.format(date) == todayFmt.format(Date())
            if (isToday) {
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
            } else {
                SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(date)
            }
        } else {
            raw.takeLast(8)
        }
    } catch (_: Exception) {
        try {
            // Fallback for yyyy-MM-ddTHH:mm
            val cleaned = raw.replace("T", " ").take(16)
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = parser.parse(cleaned)
            if (date != null) SimpleDateFormat("h:mm a", Locale.getDefault()).format(date) else raw
        } catch (_: Exception) {
            raw
        }
    }
}
