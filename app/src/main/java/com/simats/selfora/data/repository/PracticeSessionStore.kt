package com.simats.selfora.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.simats.selfora.data.model.CaregiverPracticeSessionResponse
import com.simats.selfora.data.model.StepProgressResponse
import com.simats.selfora.data.model.StepResultItemDto
import com.simats.selfora.data.model.SubmitPracticeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ActivePracticeSession(
    val childId: Long,
    val activityId: Long,
    val currentStepIndex: Int,
    val completedSteps: Int,
    val totalSteps: Int,
    val sessionStatus: String = "IN_PROGRESS", // IN_PROGRESS, COMPLETED, PENDING_SYNC
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

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

    private var _lastStepProgress: StepProgressResponse? = null

    fun setLastStepProgress(res: StepProgressResponse?) {
        _lastStepProgress = res
    }

    fun getLastStepProgress(): StepProgressResponse? = _lastStepProgress

    // =========================================================================
    // Durable Active Practice Session Persistence & Recovery (Survives Process Death)
    // =========================================================================
    private var prefs: SharedPreferences? = null
    private val gson = Gson()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences("selfora_practice_sessions", Context.MODE_PRIVATE)
        }
    }

    fun saveActivePracticeSession(session: ActivePracticeSession) {
        val sp = prefs ?: return
        val key = "active_session_${session.childId}_${session.activityId}"
        val json = gson.toJson(session)
        sp.edit()
            .putString(key, json)
            .putLong("last_active_activity_${session.childId}", session.activityId)
            .apply()
    }

    fun getActivePracticeSession(childId: Long, activityId: Long): ActivePracticeSession? {
        val sp = prefs ?: return null
        val key = "active_session_${childId}_$activityId"
        val json = sp.getString(key, null) ?: return null
        return try {
            gson.fromJson(json, ActivePracticeSession::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun clearActivePracticeSession(childId: Long, activityId: Long) {
        val sp = prefs ?: return
        val key = "active_session_${childId}_$activityId"
        sp.edit().remove(key).apply()
    }

    fun getPendingSyncSessions(childId: Long): List<ActivePracticeSession> {
        val sp = prefs ?: return emptyList()
        val all = sp.all
        val list = mutableListOf<ActivePracticeSession>()
        for ((key, value) in all) {
            if (key.startsWith("active_session_${childId}_") && value is String) {
                try {
                    val s = gson.fromJson(value, ActivePracticeSession::class.java)
                    if (s.sessionStatus == "PENDING_SYNC") {
                        list.add(s)
                    }
                } catch (_: Exception) {}
            }
        }
        return list
    }
}
