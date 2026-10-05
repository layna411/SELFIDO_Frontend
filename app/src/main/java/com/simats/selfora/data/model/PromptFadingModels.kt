package com.simats.selfora.data.model

data class CreatePromptFadingPlanRequest(
    val childId: Long,
    val activityId: Long,
    val stepId: Long,
    val currentPromptLevelId: Int,
    val targetPromptLevelId: Int,
    val clinicalReason: String,
    val reviewCriteria: String,
    val notes: String
)

data class PromptFadingPlanResponse(
    val id: Long,
    val childId: Long,
    val childName: String,
    val activityId: Long,
    val activityTitle: String,
    val stepId: Long,
    val stepNumber: Int,
    val stepTitle: String,
    val currentPromptLevelId: Int,
    val currentPromptLevelName: String,
    val targetPromptLevelId: Int,
    val targetPromptLevelName: String,
    val clinicalReason: String,
    val reviewCriteria: String,
    val notes: String,
    val status: String,
    val approvedByTherapistName: String? = null,
    val approvedAt: String? = null,
    val createdAt: String? = null
)

data class PromptFadingHistoryItemResponse(
    val id: Long,
    val childId: Long,
    val stepId: Long,
    val stepNumber: Int,
    val stepTitle: String,
    val approvedByTherapistName: String,
    val previousPromptLevelName: String,
    val newPromptLevelName: String,
    val changeDate: String,
    val reason: String,
    val subsequentPerformanceNotes: String? = null
)
