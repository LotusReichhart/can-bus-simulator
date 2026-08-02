package com.lotusreichhart.canbussimulator.data.jni

import com.lotusreichhart.canbussimulator.domain.service.ChecksumCalculator
import javax.inject.Inject

class NativeCalculator @Inject constructor() : ChecksumCalculator {
    companion object {
        init {
            System.loadLibrary("canbus_native")
        }
    }

    override fun calculate(data: ByteArray): Byte {
        return calculateChecksum(data)
    }

    private external fun calculateChecksum(data: ByteArray): Byte
}
