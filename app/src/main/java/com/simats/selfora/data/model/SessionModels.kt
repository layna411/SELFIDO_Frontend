package com.simats.selfora.data.model

data class StartSessionRequest(
    val childId: Long,
    val activityId: Long,
    val sessionType: String = "CLINIC_THERAPY",
    val environment: String = "CLINIC"
)

data class RecordPerformanceRequest(
    val stepId: Long,
    val promptLevelId: Int,
    val outcome: String = "COMPLETED",
    val attempts: Int = 1,
    val durationSeconds: Int = 0,
    val therapistNote: String? = null,
    val caregiverNote: String? = null,
    val environment: String? = "CLINIC"
)

data class PerformanceRecordResponse(
    val id: Long,
    val stepId: Long,
    val stepNumber: Int,
    val stepTitle: String,
    val promptLevelId: Int,
    val promptLevelCode: String,
    val promptLevelName: String,
    val outcome: String,
    val attempts: Int,
    val durationSeconds: Int,
    val therapistNote: String?,
    val caregiverNote: String?,
    val recordedAt: String
)

data class SessionResponse(
    val sessionId: Long,
    val sessionCode: String,
    val childId: Long,
    val childName: String,
    val conductedByUserId: Long,
    val conductedByUserName: String,
    val activityId: Long,
    val activityTitle: String,
    val sessionType: String,
    val environment: String,
    val startTime: String,
    val endTime: String?,
    val totalDurationSeconds: Int,
    val summaryNotes: String?,
    val performanceRecords: List<PerformanceRecordResponse> = emptyList()
)

data class SessionSummaryResponse(
    val sessionId: Long,
    val sessionCode: String,
    val childName: String,
    val activityTitle: String,
    val totalSteps: Int,
    val completedSteps: Int,
    val independentSteps: Int,
    val promptedSteps: Int,
    val unableSteps: Int,
    val independencePercentage: Double,
    val totalDurationSeconds: Int,
    val promptDistribution: Map<String, Int> = emptyMap(),
    val suggestedNextPromptLevel: String,
    val suggestedFocusStepIds: List<Long> = emptyList()
)
