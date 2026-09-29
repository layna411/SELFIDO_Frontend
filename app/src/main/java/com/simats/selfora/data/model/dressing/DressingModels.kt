package com.simats.selfora.data.model.dressing

import java.util.Date

/**
 * Data structures for ADL Dressing & Activity Training System
 */

enum class GenderCategory {
    BOY,
    GIRL,
    UNISEX
}

enum class AnimationType {
    GIF,
    WEBP,
    MP4,
    LOTTIE,
    VECTOR_ANIMATED
}

enum class PromptLevel(val level: Int, val displayName: String, val description: String) {
    LEVEL_0(0, "Independent", "Child performs step with no assistance"),
    LEVEL_1(1, "Visual", "Child follows visual illustration or animation"),
    LEVEL_2(2, "Gesture", "Therapist/Caregiver points or gestures"),
    LEVEL_3(3, "Verbal", "Therapist/Caregiver gives verbal prompt"),
    LEVEL_4(4, "Model / Video", "Therapist/Caregiver demonstrates action"),
    LEVEL_5(5, "Partial Physical", "Therapist/Caregiver provides light physical guide"),
    LEVEL_6(6, "Full Physical", "Therapist/Caregiver guides hand-over-hand"),
    UNABLE(-1, "Unable", "Child could not complete step during session")
}

data class DressingStep(
    val stepId: String,
    val activityId: String,
    val stepNumber: Int,
    val title: String,
    val childGuidance: String,
    val visualDescription: String,
    val visualHighlight: String,
    val assetPath: String,
    val animationType: AnimationType = AnimationType.GIF,
    val durationMs: Long = 4000,
    val loop: Boolean = true,
    val audioPath: String? = null,
    val enabled: Boolean = true
)

data class DressingActivity(
    val activityId: String,
    val title: String,
    val category: String = "Dressing",
    val gender: GenderCategory,
    val garmentType: String,
    val totalSteps: Int,
    val iconName: String = "ic_shirt",
    val description: String,
    val steps: List<DressingStep>
)

data class StepPerformanceRecord(
    val stepId: String,
    val stepNumber: Int,
    val promptLevel: PromptLevel = PromptLevel.LEVEL_1,
    val completed: Boolean = true,
    val durationSeconds: Int = 0,
    val replayCount: Int = 0,
    val ttsPlayedCount: Int = 0,
    val therapistNotes: String = ""
)

data class DressingSessionProgress(
    val sessionId: String,
    val childId: String,
    val activityId: String,
    val currentStepIndex: Int = 0,
    val totalSteps: Int,
    val startTime: Date = Date(),
    var endTime: Date? = null,
    val stepRecords: MutableMap<Int, StepPerformanceRecord> = mutableMapOf(),
    val isCompleted: Boolean = false
) {
    val completionPercentage: Float
        get() = if (totalSteps > 0) (stepRecords.count { it.value.completed }.toFloat() / totalSteps) * 100f else 0f
}
