package com.simats.selfora.data.model

data class StepProgressItem(
    val stepId: Long,
    val stepNumber: Int,
    val stepTitle: String,
    val currentPromptLevel: String,
    val independencePercentage: Double,
    val totalAttempts: Int
)

data class EnvironmentProgressItem(
    val environment: String,
    val independencePercentage: Double,
    val dominantPromptLevel: String
)

data class SessionPoint(
    val sessionId: Long,
    val sessionDate: String,
    val independencePercentage: Double,
    val sessionType: String
)

data class ProgressSummaryResponse(
    val childId: Long,
    val childName: String,
    val activityId: Long,
    val activityTitle: String,
    val overallIndependencePercentage: Double,
    val totalSessionsCompleted: Int,
    val promptDistribution: Map<String, Int> = emptyMap(),
    val stepProgress: List<StepProgressItem> = emptyList(),
    val generalizationProgress: List<EnvironmentProgressItem> = emptyList(),
    val historyPoints: List<SessionPoint> = emptyList()
)
