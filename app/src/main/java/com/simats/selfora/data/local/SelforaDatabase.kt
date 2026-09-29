package com.simats.selfora.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [OfflineSessionEntity::class], version = 1, exportSchema = false)
abstract class SelforaDatabase : RoomDatabase() {
    abstract fun offlineSessionDao(): OfflineSessionDao

    companion object {
        @Volatile
        private var INSTANCE: SelforaDatabase? = null

        fun getDatabase(context: Context): SelforaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SelforaDatabase::class.java,
                    "selfora_offline.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
