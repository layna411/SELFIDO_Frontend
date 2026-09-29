package com.simats.selfora.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.local.SelforaDatabase
import com.simats.selfora.data.model.RecordPerformanceRequest

class SessionSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = SelforaDatabase.getDatabase(applicationContext)
        val dao = database.offlineSessionDao()
        val unsyncedRecords = dao.getUnsyncedRecords()

        if (unsyncedRecords.isEmpty()) {
            return Result.success()
        }

        var allSuccessful = true
        for (record in unsyncedRecords) {
            try {
                val response = ApiClient.apiService.recordPerformance(
                    id = record.sessionId,
                    request = RecordPerformanceRequest(
                        stepId = record.stepId,
                        promptLevelId = record.promptLevelId,
                        outcome = record.outcome,
                        attempts = record.attempts,
                        durationSeconds = record.durationSeconds,
                        therapistNote = record.therapistNote,
                        caregiverNote = record.caregiverNote,
                        environment = record.environment
                    )
                )

                if (response.isSuccessful) {
                    dao.markAsSynced(record.id)
                } else {
                    allSuccessful = false
                }
            } catch (e: Exception) {
                allSuccessful = false
            }
        }

        dao.clearSyncedRecords()
        return if (allSuccessful) Result.success() else Result.retry()
    }
}
