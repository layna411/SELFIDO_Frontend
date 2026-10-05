package com.simats.selfora.data.model

data class StepResultItemDto(
    val stepId: Long,
    val stepNumber: Int,
    val stepTitle: String,
    val outcome: String, // "INDEPENDENT", "LITTLE_HELP", "LOT_HELP", "COULD_NOT_COMPLETE", "NOT_PRACTISED"
    val observation: String? = null
)

data class SubmitPracticeRequest(
    val homeProgramId: Long,
    val childId: Long = 1L,
    val activityId: Long = 1L,
    val practiceDate: String = java.time.LocalDate.now().toString(),
    val durationMinutes: Int = 15,
    val caregiverNotes: String? = null,
    val stepResults: List<StepResultItemDto> = emptyList()
)

data class CaregiverPracticeSessionResponse(
    val id: Long,
    val homeProgramId: Long,
    val homeProgramTitle: String,
    val childId: Long,
    val childName: String,
    val caregiverId: Long,
    val caregiverName: String,
    val activityId: Long,
    val activityTitle: String,
    val practiceDate: String,
    val durationMinutes: Int,
    val caregiverNotes: String? = null,
    val status: String = "SUBMITTED", // SUBMITTED, REVIEWED
    val submittedAt: String = "",
    val reviewingTherapistName: String? = null,
    val reviewedAt: String? = null,
    val therapistFeedback: String? = null,
    val flaggedForFollowup: Boolean = false,
    val stepResults: List<StepResultItemDto> = emptyList()
)

data class TherapistReviewRequest(
    val therapistFeedback: String,
    val status: String = "REVIEWED",
    val flaggedForFollowup: Boolean = false
)
