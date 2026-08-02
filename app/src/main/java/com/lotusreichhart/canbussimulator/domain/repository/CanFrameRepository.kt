package com.lotusreichhart.canbussimulator.domain.repository

import com.lotusreichhart.canbussimulator.domain.model.CanFrame
import kotlinx.coroutines.flow.Flow

interface CanFrameRepository {
    fun getCanFrames(): Flow<List<CanFrame>>
    suspend fun saveCanFrame(frame: CanFrame)
    suspend fun clearAllFrames()
}
