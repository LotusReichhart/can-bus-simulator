package com.lotusreichhart.canbussimulator.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CanFrameEntity::class], version = 1, exportSchema = false)
abstract class CanFrameDatabase : RoomDatabase() {
    abstract fun canFrameDao(): CanFrameDao

    companion object {
        @Volatile
        private var INSTANCE: CanFrameDatabase? = null

        fun getDatabase(context: Context): CanFrameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CanFrameDatabase::class.java,
                    "can_bus_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
