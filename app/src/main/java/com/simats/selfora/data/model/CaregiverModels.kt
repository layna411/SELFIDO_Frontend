package com.simats.selfora.data.model

data class LinkedChildInfo(
    val childId: Long,
    val name: String,
    val gender: String, // "BOY" or "GIRL"
    val age: Int = 6,
    val therapistName: String = "Dr. Sarah Jenkins",
    val profilePicUrl: String? = null
)

enum class CaregiverObservationOutcome(
    val title: String,
    val description: String,
    val emoji: String,
    val colorHex: String,
    val promptLevelEquivalent: Int
) {
    INDEPENDENT("Did independently", "Child completed without assistance", "🟢", "#10B981", 0),
    LITTLE_HELP("Needed a little help", "Child needed slight visual or verbal cue", "🔵", "#2563EB", 2),
    LOT_OF_HELP("Needed a lot of help", "Child needed physical guidance or demonstration", "🟠", "#F59E0B", 4),
    COULD_NOT_COMPLETE("Could not complete", "Child was unable to complete the step today", "🔴", "#EF4444", 6)
}

data class CaregiverNotificationItem(
    val id: Long,
    val title: String,
    val message: String,
    val timestamp: String,
    val type: String, // "PROGRAMME", "REMINDER", "MESSAGE", "INSTRUCTION"
    val isRead: Boolean = false
)

data class CaregiverPracticeHistoryItem(
    val sessionId: Long,
    val activityTitle: String,
    val dateDisplay: String,
    val durationMinutes: Int,
    val totalSteps: Int,
    val completedSteps: Int,
    val isFullyCompleted: Boolean
)
