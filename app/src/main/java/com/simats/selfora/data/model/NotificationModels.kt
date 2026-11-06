package com.simats.selfora.data.model

import com.google.gson.annotations.SerializedName

data class NotificationItem(
    @SerializedName("id") val id: Long,
    @SerializedName("userId") val userId: Long = 0L,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("notificationType") val notificationType: String = "GENERAL",
    @SerializedName("referenceId") val referenceId: Long? = null,
    @SerializedName("isRead", alternate = ["read", "is_read"]) var isRead: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null
)
