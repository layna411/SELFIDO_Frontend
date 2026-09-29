package com.simats.selfora.data.model

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
