package com.lotusreichhart.canbussimulator

import com.lotusreichhart.canbussimulator.data.database.CanFrameEntity
import com.lotusreichhart.canbussimulator.data.mapper.toDomain
import com.lotusreichhart.canbussimulator.data.mapper.toEntity
import com.lotusreichhart.canbussimulator.domain.model.CanFrame
import com.lotusreichhart.canbussimulator.domain.service.ChecksumCalculator
import com.lotusreichhart.canbussimulator.domain.usecase.CalculateChecksumUseCase
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class CoreEngineUnitTest {

    @Test
    fun testMapperCanFrameToEntityAndBack() {
        val originalFrame = CanFrame(
            canId = 0x123,
            data = byteArrayOf(1, 2, 3, 4),
            timestamp = 1690000000L
        )

        val entity = originalFrame.toEntity()
        assertEquals(0x123, entity.canId)
        assertArrayEquals(byteArrayOf(1, 2, 3, 4), entity.data)
        assertEquals(1690000000L, entity.timestamp)

        val domainFrame = entity.toDomain()
        assertEquals(originalFrame, domainFrame)
    }

    @Test
    fun testCalculateChecksumUseCase() {
        val fakeCalculator = object : ChecksumCalculator {
            override fun calculate(data: ByteArray): Byte {
                var sum = 0
                for (b in data) {
                    sum = sum xor b.toInt()
                }
                return sum.toByte()
            }
        }

        val useCase = CalculateChecksumUseCase(fakeCalculator)
        val testData = byteArrayOf(0x01, 0x02, 0x03)
        val expectedChecksum = (0x01 xor 0x02 xor 0x03).toByte()

        val result = useCase(testData)
        assertEquals(expectedChecksum, result)
    }
}
