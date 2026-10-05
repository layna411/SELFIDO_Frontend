package com.simats.selfora.data.model

data class ChildDashboardResponse(
    val childId: Long = 0,
    val childName: String = "",
    val preferredName: String = "",
    val avatarUrl: String? = null,
    val gender: String = "BOY",
    val assignedActivities: List<ChildActivitySummaryDto> = emptyList(),
    val continueActivity: ChildActivitySummaryDto? = null,
    val allActivities: List<ChildActivitySummaryDto> = emptyList(),
    val earnedRewards: List<ChildRewardDto> = emptyList(),
    val totalStars: Int = 0,
    val completedCount: Int = 0,
    val preferences: ChildModePreferenceDto? = null
)

data class ChildActivitySummaryDto(
    val activityId: Long = 0,
    val title: String = "",
    val description: String = "",
    val iconUrl: String? = null,
    val categoryName: String = "Dressing",
    val totalSteps: Int = 0,
    val completedSteps: Int = 0,
    val isAssigned: Boolean = false,
    val isCompleted: Boolean = false,
    val starsEarned: Int = 0
)

data class RecordStepProgressRequest(
    val childId: Long,
    val activityId: Long,
    val stepId: Long? = null,
    val stepNumber: Int? = null,
    val isActivityCompleted: Boolean = false
)

data class ChildRewardDto(
    val id: Long = 0,
    val badgeType: String = "",
    val badgeTitle: String = "",
    val badgeIcon: String? = null,
    val earnedAt: String? = null
)

data class ChildModePreferenceDto(
    val preferredName: String? = null,
    val audioEnabled: Boolean = true,
    val animationEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val language: String = "EN",
    val rewardSounds: Boolean = true
)
