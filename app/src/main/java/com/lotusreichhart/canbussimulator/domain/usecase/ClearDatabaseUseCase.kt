package com.lotusreichhart.canbussimulator.domain.usecase

import com.lotusreichhart.canbussimulator.domain.repository.CanFrameRepository
import javax.inject.Inject

class ClearDatabaseUseCase @Inject constructor(
    private val repository: CanFrameRepository
) {
    suspend operator fun invoke() {
        repository.clearAllFrames()
    }
}
