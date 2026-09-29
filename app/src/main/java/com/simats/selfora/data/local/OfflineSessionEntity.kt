package com.simats.selfora.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_performance_records")
data class OfflineSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val stepId: Long,
    val promptLevelId: Int,
    val outcome: String,
    val attempts: Int,
    val durationSeconds: Int,
    val therapistNote: String?,
    val caregiverNote: String?,
    val environment: String?,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
