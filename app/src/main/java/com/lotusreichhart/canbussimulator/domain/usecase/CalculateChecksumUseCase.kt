package com.lotusreichhart.canbussimulator.domain.usecase

import com.lotusreichhart.canbussimulator.domain.service.ChecksumCalculator
import javax.inject.Inject

class CalculateChecksumUseCase @Inject constructor(
    private val checksumCalculator: ChecksumCalculator
) {
    operator fun invoke(data: ByteArray): Byte {
        return checksumCalculator.calculate(data)
    }
}
