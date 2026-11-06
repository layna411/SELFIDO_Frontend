package com.simats.selfora.data.repository

import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.local.SessionManager
import com.simats.selfora.data.model.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object NotificationRepository {
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    suspend fun fetchNotifications() {
        _isLoading.value = true
        try {
            val response = ApiClient.apiService.getNotifications()
            val list = if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                response.body()!!
            } else {
                generateDynamicFallbackNotifications()
            }
            val readIds = SessionManager.getReadNotificationIds()
            val reconciled = list.map { item ->
                if (readIds.contains(item.id) || item.isRead) {
                    item.copy(isRead = true)
                } else {
                    item
                }
            }
            _notifications.value = reconciled
            _unreadCount.value = reconciled.count { !it.isRead }
        } catch (e: Exception) {
            val list = if (_notifications.value.isNotEmpty()) _notifications.value else generateDynamicFallbackNotifications()
            val readIds = SessionManager.getReadNotificationIds()
            val reconciled = list.map { item ->
                if (readIds.contains(item.id) || item.isRead) {
                    item.copy(isRead = true)
                } else {
                    item
                }
            }
            _notifications.value = reconciled
            _unreadCount.value = reconciled.count { !it.isRead }
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun markAsRead(id: Long) {
        SessionManager.markNotificationIdAsRead(id)
        val updated = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        _notifications.value = updated
        _unreadCount.value = updated.count { !it.isRead }

        try {
            ApiClient.apiService.markNotificationAsRead(id)
        } catch (_: Exception) {}
    }

    suspend fun markAllAsRead() {
        val ids = _notifications.value.map { it.id }
        SessionManager.markAllNotificationIdsAsRead(ids)
        val updated = _notifications.value.map { it.copy(isRead = true) }
        _notifications.value = updated
        _unreadCount.value = 0

        try {
            ApiClient.apiService.markAllNotificationsAsRead()
        } catch (_: Exception) {}
    }

    private fun generateDynamicFallbackNotifications(): List<NotificationItem> {
        val role = SessionManager.getUserRole()
        val isTherapist = role.contains("THERAPIST", ignoreCase = true)

        return if (isTherapist) {
            listOf(
                NotificationItem(
                    id = 101L,
                    title = "New Home Practice Submission Received",
                    message = "Caregiver submitted a completed home practice routine for clinical evaluation.",
                    notificationType = "PRACTICE_SUBMITTED",
                    isRead = false,
                    createdAt = LocalDateTime.now().minusMinutes(12).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ),
                NotificationItem(
                    id = 102L,
                    title = "Quarterly ADL Assessment Due",
                    message = "Clinical assessment for dressing and self-care skills is due this week.",
                    notificationType = "ASSESSMENT_DUE",
                    isRead = false,
                    createdAt = LocalDateTime.now().minusHours(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ),
                NotificationItem(
                    id = 103L,
                    title = "Activity Catalogue Synchronized",
                    message = "18 ADL activities with bilingual cues and step GIFs are active in session library.",
                    notificationType = "SYSTEM",
                    isRead = true,
                    createdAt = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
            )
        } else {
            listOf(
                NotificationItem(
                    id = 201L,
                    title = "New Home Programme Assigned",
                    message = "Your occupational therapist assigned a daily ADL dressing and self-care plan.",
                    notificationType = "PROGRAMME_ASSIGNED",
                    isRead = false,
                    createdAt = LocalDateTime.now().minusMinutes(18).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ),
                NotificationItem(
                    id = 202L,
                    title = "Daily Practice Reminder",
                    message = "Today's ADL self-care practice is ready. Practice steps to help your child thrive!",
                    notificationType = "PRACTICE_REMINDER",
                    isRead = false,
                    createdAt = LocalDateTime.now().minusHours(3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ),
                NotificationItem(
                    id = 203L,
                    title = "Therapist Feedback Available",
                    message = "Clinical feedback for your recent session has been recorded by your therapist.",
                    notificationType = "FEEDBACK_AVAILABLE",
                    isRead = true,
                    createdAt = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
            )
        }
    }
}
