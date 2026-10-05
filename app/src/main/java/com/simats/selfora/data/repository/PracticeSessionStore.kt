package com.simats.selfora.data.repository

import com.simats.selfora.data.model.CaregiverPracticeSessionResponse
import com.simats.selfora.data.model.StepResultItemDto
import com.simats.selfora.data.model.SubmitPracticeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class PracticeRecordEntry(
    val sessionId: Long,
    val programId: Long,
    val childId: Long,
    val activityId: Long,
    val activityTitle: String,
    val practiceDate: String,
    val durationMinutes: Int,
    val caregiverNotes: String,
    val totalSteps: Int,
    val independentCount: Int,
    val neededHelpCount: Int,
    val stepResults: List<StepResultItemDto>,
    val status: String = "SUBMITTED" // SUBMITTED, REVIEWED
)

object PracticeSessionStore {
    private val _records = MutableStateFlow<List<PracticeRecordEntry>>(
        listOf(
            PracticeRecordEntry(
                sessionId = 101L,
                programId = 1L,
                childId = 1L,
                activityId = 1L,
                activityTitle = "Boy T-Shirt Dressing",
                practiceDate = LocalDate.now().minusDays(1).toString(),
                durationMinutes = 12,
                caregiverNotes = "Child did head insertion independently!",
                totalSteps = 8,
                independentCount = 6,
                neededHelpCount = 2,
                stepResults = emptyList()
            )
        )
    )
    val records: StateFlow<List<PracticeRecordEntry>> = _records.asStateFlow()

    fun addPracticeRecord(
        programId: Long,
        childId: Long,
        activityId: Long,
        activityTitle: String,
        durationMinutes: Int,
        caregiverNotes: String,
        totalSteps: Int,
        independentCount: Int,
        neededHelpCount: Int,
        stepResults: List<StepResultItemDto>
    ): PracticeRecordEntry {
        val nextId = (_records.value.maxOfOrNull { it.sessionId } ?: 100L) + 1L
        val entry = PracticeRecordEntry(
            sessionId = nextId,
            programId = programId,
            childId = childId,
            activityId = activityId,
            activityTitle = activityTitle,
            practiceDate = LocalDate.now().toString(),
            durationMinutes = durationMinutes.coerceAtLeast(1),
            caregiverNotes = caregiverNotes.ifBlank { "Home practice completed successfully." },
            totalSteps = totalSteps,
            independentCount = independentCount,
            neededHelpCount = neededHelpCount,
            stepResults = stepResults
        )
        _records.value = listOf(entry) + _records.value
        return entry
    }

    fun getRecordsForActivity(activityId: Long): List<PracticeRecordEntry> {
        return _records.value.filter { it.activityId == activityId }
    }

    fun getTotalCompletedCount(): Int {
        return _records.value.size
    }

    fun getCompletedCountForActivity(activityId: Long): Int {
        return _records.value.count { it.activityId == activityId }
    }

    fun isActivityPracticedToday(activityId: Long): Boolean {
        val today = LocalDate.now().toString()
        return _records.value.any { it.activityId == activityId && it.practiceDate == today }
    }
}
