package com.simats.selfora.data.model

data class CreateHomeProgramRequest(
    val childId: Long,
    val activityId: Long,
    val frequencyPerWeek: Int = 3,
    val targetDurationMinutes: Int = 15,
    val targetPromptLevelId: Int,
    val goalStatement: String?,
    val caregiverInstructions: String?,
    val startDate: String,
    val endDate: String,
    val targetStepIds: List<Long>
)

data class HomeProgramResponse(
    val id: Long,
    val childId: Long,
    val childName: String,
    val therapistId: Long,
    val therapistName: String,
    val activityId: Long,
    val activityTitle: String,
    val frequencyPerWeek: Int,
    val targetDurationMinutes: Int,
    val targetPromptLevelId: Int,
    val targetPromptLevelName: String,
    val goalStatement: String?,
    val caregiverInstructions: String?,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean,
    val targetSteps: List<TaskStepResponse> = emptyList(),
    val createdAt: String
)
