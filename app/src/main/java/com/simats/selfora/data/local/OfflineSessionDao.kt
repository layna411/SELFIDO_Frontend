package com.simats.selfora.data.local

import androidx.room.*

@Dao
interface OfflineSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflineRecord(record: OfflineSessionEntity): Long

    @Query("SELECT * FROM offline_performance_records WHERE isSynced = 0 ORDER BY createdAt ASC")
    suspend fun getUnsyncedRecords(): List<OfflineSessionEntity>

    @Query("UPDATE offline_performance_records SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("DELETE FROM offline_performance_records WHERE isSynced = 1")
    suspend fun clearSyncedRecords()
}
