package com.lotusreichhart.canbussimulator.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CanFrameDao {
    @Query("SELECT * FROM can_frames ORDER BY timestamp DESC")
    fun getCanFrames(): Flow<List<CanFrameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(frame: CanFrameEntity): Long

    @Query("DELETE FROM can_frames")
    suspend fun deleteAll(): Int
}
