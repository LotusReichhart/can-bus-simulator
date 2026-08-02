package com.lotusreichhart.canbussimulator.domain.service

interface ChecksumCalculator {
    fun calculate(data: ByteArray): Byte
}
