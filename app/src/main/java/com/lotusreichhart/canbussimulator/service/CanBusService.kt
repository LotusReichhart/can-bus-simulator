package com.lotusreichhart.canbussimulator.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.lotusreichhart.canbussimulator.domain.model.CanFrame
import com.lotusreichhart.canbussimulator.domain.usecase.CalculateChecksumUseCase
import com.lotusreichhart.canbussimulator.domain.usecase.SaveCanFrameUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class CanBusService : Service() {

    @Inject
    lateinit var saveCanFrameUseCase: SaveCanFrameUseCase

    @Inject
    lateinit var calculateChecksumUseCase: CalculateChecksumUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val binder = LocalBinder()

    private val _canFrameFlow = MutableSharedFlow<CanFrame>(replay = 0, extraBufferCapacity = 64)
    val canFrameFlow: SharedFlow<CanFrame> = _canFrameFlow.asSharedFlow()

    inner class LocalBinder : Binder() {
        fun getService(): CanBusService = this@CanBusService
    }

    override fun onCreate() {
        super.onCreate()
        startSimulation()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun startSimulation() {
        serviceScope.launch {
            val ids = listOf(0x101, 0x102, 0x103)
            while (isActive) {
                val canId = ids.random()
                val rawData = when (canId) {
                    0x101 -> {
                        val rpm = Random.nextInt(800, 6001)
                        byteArrayOf(
                            ((rpm shr 8) and 0xFF).toByte(),
                            (rpm and 0xFF).toByte()
                        )
                    }
                    0x102 -> {
                        val speed = Random.nextInt(0, 181)
                        byteArrayOf(speed.toByte())
                    }
                    0x103 -> {
                        val temp = Random.nextInt(70, 111)
                        byteArrayOf(temp.toByte())
                    }
                    else -> byteArrayOf(0)
                }

                val checksum = calculateChecksumUseCase(rawData)
                val dataWithChecksum = rawData + checksum

                val frame = CanFrame(
                    canId = canId,
                    data = dataWithChecksum,
                    timestamp = System.currentTimeMillis()
                )

                saveCanFrameUseCase(frame)
                _canFrameFlow.emit(frame)

                delay(500)
            }
        }
    }
}
