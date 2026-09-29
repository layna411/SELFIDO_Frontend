package com.simats.selfora.data.model

data class MediaResponse(
    val id: Long,
    val mediaType: String,
    val mediaUrl: String,
    val thumbnailUrl: String?,
    val caption: String?
)

data class TaskStepResponse(
    val id: Long,
    val activityId: Long,
    val stepNumber: Int,
    val title: String,
    val instructionText: String,
    val childInstruction: String,
    val audioPromptUrl: String?,
    val media: List<MediaResponse> = emptyList()
)

data class ActivityResponse(
    val id: Long,
    val categoryId: Long,
    val categoryName: String,
    val title: String,
    val targetGender: String,
    val description: String?,
    val iconUrl: String?,
    val totalSteps: Int,
    val steps: List<TaskStepResponse> = emptyList()
)
