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
    val titleTa: String? = null,
    val instructionText: String,
    val instructionTextTa: String? = null,
    val childInstruction: String,
    val childInstructionTa: String? = null,
    val audioPromptUrl: String?,
    val audioPromptUrlTa: String? = null,
    val media: List<MediaResponse> = emptyList()
)

data class ActivityResponse(
    val id: Long,
    val categoryId: Long,
    val categoryName: String,
    val title: String,
    val titleTa: String? = null,
    val targetGender: String,
    val startingPosition: String? = null,
    val startingPositionTa: String? = null,
    val description: String?,
    val descriptionTa: String? = null,
    val iconUrl: String?,
    val totalSteps: Int,
    val steps: List<TaskStepResponse> = emptyList()
)
