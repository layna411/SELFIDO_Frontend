package com.simats.selfora.data.model

data class CreateChildWithCaregiverRequest(
    val childFirstName: String,
    val childLastName: String = "",
    val dateOfBirth: String, // YYYY-MM-DD
    val gender: String = "BOY",
    val profilePhotoUrl: String = "",
    val registrationId: String = "",
    val diagnosisNotes: String = "",
    val caregiverName: String,
    val relationship: String = "Mother",
    val phone: String,
    val email: String,
    val address: String = ""
)

data class CredentialsSuccessResponse(
    val childId: Long,
    val childName: String,
    val caregiverId: Long,
    val caregiverName: String,
    val relationship: String,
    val caregiverEmail: String,
    val temporaryPassword: String,
    val mustChangePassword: Boolean = true
)

data class ScheduledSessionItem(
    val sessionId: Long,
    val childId: Long,
    val childName: String,
    val scheduledTime: String,
    val adlActivity: String,
    val status: String = "SCHEDULED" // SCHEDULED, IN_PROGRESS, COMPLETED
)

data class PendingReviewItem(
    val practiceId: Long,
    val childId: Long,
    val childName: String,
    val caregiverName: String,
    val activityName: String,
    val submittedDate: String,
    val notes: String = ""
)

data class TherapistDashboardSummaryResponse(
    val therapistName: String = "Dr. Sarah Jenkins",
    val designation: String = "Senior Occupational Specialist",
    val avatarUrl: String = "ic_therapist_default",
    val activeChildrenCount: Int = 24,
    val linkedCaregiversCount: Int = 20,
    val pendingAssessmentsCount: Int = 12,
    val activeHomeProgramsCount: Int = 18,
    val todaySessionsCount: Int = 4,
    val completedSessionsCount: Int = 2,
    val pendingReviewsCount: Int = 5,
    val todaySchedule: List<ScheduledSessionItem> = emptyList(),
    val pendingReviews: List<PendingReviewItem> = emptyList()
)

data class ChildSummaryItem(
    val id: Long,
    val name: String,
    val age: Int,
    val gender: String = "BOY",
    val avatarUrl: String = "ic_avatar_default",
    val caregiverName: String,
    val caregiverRelationship: String,
    val lastSessionDate: String = "No recent session",
    val programmeStatus: String = "ACTIVE",
    val assignedGoalsCount: Int = 3,
    val dressingPercentage: Int = 72,
    val eatingPercentage: Int = 65,
    val shoesPercentage: Int = 80,
    val overallProgressPercentage: Int = 75,
    val hasPendingAssessment: Boolean = false,
    val hasActiveHomeProgram: Boolean = true,
    val isDemoProfile: Boolean = false
)

data class ResetPasswordResultResponse(
    val caregiverId: Long,
    val caregiverEmail: String,
    val newTemporaryPassword: String,
    val message: String
)

data class PromptHistoryItem(
    val stepTitle: String,
    val stepNumber: Int,
    val date: String,
    val promptLevelName: String,
    val promptLevelCode: String,
    val observation: String
)
