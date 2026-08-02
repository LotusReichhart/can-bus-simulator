package com.lotusreichhart.canbussimulator.domain.usecase

import com.lotusreichhart.canbussimulator.domain.model.CanFrame
import com.lotusreichhart.canbussimulator.domain.repository.CanFrameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCanFramesUseCase @Inject constructor(
    private val repository: CanFrameRepository
) {
    operator fun invoke(): Flow<List<CanFrame>> {
        return repository.getCanFrames()
    }
}
