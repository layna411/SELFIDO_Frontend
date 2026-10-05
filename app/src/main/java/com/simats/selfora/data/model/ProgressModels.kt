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

// Stage 5 Analyze & Adapt Models

data class ActivityPerformanceCardDto(
    val categoryName: String, // Dressing, Eating, Grooming, Shoes & Socks
    val currentIndependencePct: Double,
    val previousIndependencePct: Double,
    val trend: String, // IMPROVING, STABLE, REGRESSING
    val sessionsCount: Int
)

data class DifficultStepDto(
    val stepId: Long,
    val stepNumber: Int,
    val stepTitle: String,
    val activityTitle: String,
    val mainAssistanceLevel: String,
    val physicalAssistanceCount: Int,
    val verbalPromptCount: Int,
    val unableCount: Int,
    val reasonDescription: String
)

data class IndependentStepDto(
    val stepId: Long,
    val stepNumber: Int,
    val stepTitle: String,
    val activityTitle: String,
    val consecutiveIndependentSessions: Int,
    val generalizationReadiness: String
)

data class SessionComparisonDto(
    val currentSessionDate: String,
    val previousSessionDate: String,
    val currentIndependencePercentage: Double,
    val previousIndependencePercentage: Double,
    val improvedSteps: List<String> = emptyList(),
    val regressedSteps: List<String> = emptyList(),
    val unchangedSteps: List<String> = emptyList()
)

data class RecommendationDto(
    val triggeringEvidence: String,
    val sessionDates: String,
    val activityTitle: String,
    val stepTitle: String,
    val suggestedTherapistAction: String
)

data class CaregiverProgressSummaryDto(
    val totalSubmissions: Int,
    val completedActivitiesCount: Int,
    val reportedOutcomeSummary: String,
    val participationFrequency: String,
    val distinctFromClinicalScore: Boolean = true
)

data class AnalyzeAndAdaptDashboardResponse(
    val childId: Long,
    val childName: String,
    val selectedActivityId: Long,
    val selectedActivityTitle: String,
    val totalRecordedSessions: Int,
    val latestAssessmentDate: String,
    val previousAssessmentDate: String,
    val currentIndependencePercentage: Double,
    val previousIndependencePercentage: Double,
    val overallTrend: String,
    val activityCards: List<ActivityPerformanceCardDto> = emptyList(),
    val difficultSteps: List<DifficultStepDto> = emptyList(),
    val independentSteps: List<IndependentStepDto> = emptyList(),
    val sessionComparison: SessionComparisonDto? = null,
    val factualObservations: List<String> = emptyList(),
    val recommendations: List<RecommendationDto> = emptyList(),
    val caregiverProgressSummary: CaregiverProgressSummaryDto? = null
)
