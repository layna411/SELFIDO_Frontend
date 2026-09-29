package com.simats.selfora.data.model

data class StepAssessmentRequest(
    val stepId: Long,
    val promptLevelId: Int,
    val outcome: String = "SUCCESS",
    val notes: String? = null
)

data class CreateAssessmentRequest(
    val childId: Long,
    val activityId: Long,
    val notes: String?,
    val stepResults: List<StepAssessmentRequest>
)

data class StepResultResponse(
    val id: Long,
    val stepId: Long,
    val stepNumber: Int,
    val stepTitle: String,
    val promptLevelId: Int,
    val promptLevelName: String,
    val outcome: String,
    val notes: String?
)

data class AssessmentResponse(
    val assessmentId: Long,
    val childId: Long,
    val childName: String,
    val therapistId: Long,
    val therapistName: String,
    val activityId: Long,
    val activityTitle: String,
    val assessmentDate: String,
    val notes: String?,
    val stepResults: List<StepResultResponse>
)
