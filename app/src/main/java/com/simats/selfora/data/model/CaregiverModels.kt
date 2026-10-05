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
    INDEPENDENT("Completed Independently", "Child completed without any assistance", "🟢", "#10B981", 0),
    LITTLE_HELP("Completed with a Little Help", "Child completed with a slight visual or verbal cue", "🔵", "#2563EB", 2),
    LOT_HELP("Completed with a Lot of Help", "Child completed with physical guidance or demonstration", "🟠", "#F59E0B", 4),
    COULD_NOT_COMPLETE("Could Not Complete", "Child was unable or refused to complete the step today", "🔴", "#EF4444", 6),
    NOT_PRACTISED("Not Practised", "Step was skipped or not attempted during this session", "⚪", "#64748B", 0)
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
