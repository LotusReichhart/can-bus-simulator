package com.lotusreichhart.canbussimulator.data.repository

import com.lotusreichhart.canbussimulator.data.database.CanFrameDao
import com.lotusreichhart.canbussimulator.data.mapper.toDomain
import com.lotusreichhart.canbussimulator.data.mapper.toEntity
import com.lotusreichhart.canbussimulator.domain.model.CanFrame
import com.lotusreichhart.canbussimulator.domain.repository.CanFrameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CanFrameRepositoryImpl @Inject constructor(
    private val canFrameDao: CanFrameDao
) : CanFrameRepository {
    override fun getCanFrames(): Flow<List<CanFrame>> {
        return canFrameDao.getCanFrames().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveCanFrame(frame: CanFrame) {
        canFrameDao.insert(frame.toEntity())
    }

    override suspend fun clearAllFrames() {
        canFrameDao.deleteAll()
    }
}
