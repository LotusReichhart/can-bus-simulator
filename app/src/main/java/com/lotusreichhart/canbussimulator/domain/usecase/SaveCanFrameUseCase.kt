package com.lotusreichhart.canbussimulator.domain.usecase

import com.lotusreichhart.canbussimulator.domain.model.CanFrame
import com.lotusreichhart.canbussimulator.domain.repository.CanFrameRepository
import javax.inject.Inject

class SaveCanFrameUseCase @Inject constructor(
    private val repository: CanFrameRepository
) {
    suspend operator fun invoke(frame: CanFrame) {
        repository.saveCanFrame(frame)
    }
}
