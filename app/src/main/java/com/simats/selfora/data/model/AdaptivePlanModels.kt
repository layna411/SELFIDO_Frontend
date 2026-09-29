package com.simats.selfora.data.model

data class CreateAdaptivePlanRequest(
    val childId: Long,
    val activityId: Long,
    val sessionId: Long,
    val currentPromptLevelId: Int,
    val targetPromptLevelId: Int,
    val targetStepIds: List<Long>,
    val clinicalRationale: String?,
    val isConfirmed: Boolean = true
)

data class AdaptivePlanResponse(
    val id: Long,
    val childId: Long,
    val childName: String,
    val activityId: Long,
    val activityTitle: String,
    val sessionId: Long,
    val currentPromptLevelId: Int,
    val currentPromptLevelName: String,
    val targetPromptLevelId: Int,
    val targetPromptLevelName: String,
    val targetStepIds: List<Long>,
    val clinicalRationale: String?,
    val isConfirmed: Boolean,
    val createdAt: String
)
