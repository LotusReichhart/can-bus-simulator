package com.lotusreichhart.canbussimulator.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.lotusreichhart.canbussimulator.ICanBusService
import com.lotusreichhart.canbussimulator.domain.model.CanFrame
import com.lotusreichhart.canbussimulator.domain.usecase.CalculateChecksumUseCase
import com.lotusreichhart.canbussimulator.domain.usecase.SaveCanFrameUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

    // Phạm vi coroutine cho các tác vụ bất đồng bộ của Service
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Job quản lý tiến trình giả lập phát dữ liệu định kỳ
    private var simulationJob: Job? = null

    private val _canFrameFlow = MutableSharedFlow<CanFrame>(replay = 0, extraBufferCapacity = 64)
    val canFrameFlow: SharedFlow<CanFrame> = _canFrameFlow.asSharedFlow()

    // Đối tượng Binder triển khai giao diện AIDL được định nghĩa trong ICanBusService
    private val binder = object : ICanBusService.Stub() {
        override fun startSimulation() {
            this@CanBusService.startSimulation()
        }

        override fun stopSimulation() {
            this@CanBusService.stopSimulation()
        }

        override fun injectDummyFrame(canId: Int) {
            this@CanBusService.injectDummyFrame(canId)
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Tự động bắt đầu mô phỏng khi Service được khởi tạo lần đầu
        startSimulation()
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d("CanBusService", "Service bound successfully via AIDL binder")
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        // Huỷ bỏ coroutine scope để tránh rò rỉ bộ nhớ khi Service bị huỷ
        serviceScope.cancel()
        Log.d("CanBusService", "Service destroyed and scope cancelled")
    }

    // Bắt đầu mô phỏng phát dữ liệu định kỳ
    private fun startSimulation() {
        // Tránh chạy song song nhiều Job mô phỏng cùng một lúc
        if (simulationJob?.isActive == true) {
            Log.w("CanBusService", "Simulation is already running")
            return
        }

        simulationJob = serviceScope.launch {
            Log.d("CanBusService", "Simulation started successfully")
            val ids = listOf(0x101, 0x102, 0x103)
            while (isActive) {
                val canId = ids.random()
                val rawData = generateRawData(canId)

                val checksum = calculateChecksumUseCase(rawData)
                val dataWithChecksum = rawData + checksum

                val frame = CanFrame(
                    canId = canId,
                    data = dataWithChecksum,
                    timestamp = System.currentTimeMillis()
                )

                // Lưu frame vào cơ sở dữ liệu và phát ra flow
                saveCanFrameUseCase(frame)
                _canFrameFlow.emit(frame)

                Log.d("CanBusService", "Generated and saved frame: ID = 0x${canId.toString(16).uppercase()}, data length = ${dataWithChecksum.size}")

                delay(500)
            }
        }
    }

    // Dừng tiến trình mô phỏng
    private fun stopSimulation() {
        if (simulationJob == null || simulationJob?.isActive == false) {
            Log.w("CanBusService", "Simulation is not running")
            return
        }
        // Huỷ Job mô phỏng và đặt lại về null
        simulationJob?.cancel()
        simulationJob = null
        Log.d("CanBusService", "Simulation stopped successfully")
    }

    // Chèn một CAN frame giả lập cụ thể dựa trên ID truyền vào
    private fun injectDummyFrame(canId: Int) {
        serviceScope.launch {
            val rawData = generateRawData(canId)
            val checksum = calculateChecksumUseCase(rawData)
            val dataWithChecksum = rawData + checksum

            val frame = CanFrame(
                canId = canId,
                data = dataWithChecksum,
                timestamp = System.currentTimeMillis()
            )

            // Lưu CAN Frame giả lập vào database và phát ra flow
            saveCanFrameUseCase(frame)
            _canFrameFlow.emit(frame)

            Log.d("CanBusService", "Injected and saved dummy frame: ID = 0x${canId.toString(16).uppercase()}")
        }
    }

    // Sinh dữ liệu thô tương ứng với từng CAN ID
    private fun generateRawData(canId: Int): ByteArray {
        return when (canId) {
            0x101 -> {
                // Sinh RPM ngẫu nhiên từ 800 đến 6000
                val rpm = Random.nextInt(800, 6001)
                byteArrayOf(
                    ((rpm shr 8) and 0xFF).toByte(),
                    (rpm and 0xFF).toByte()
                )
            }
            0x102 -> {
                // Sinh vận tốc ngẫu nhiên từ 0 đến 180
                val speed = Random.nextInt(0, 181)
                byteArrayOf(speed.toByte())
            }
            0x103 -> {
                // Sinh nhiệt độ động cơ ngẫu nhiên từ 70 đến 110
                val temp = Random.nextInt(70, 111)
                byteArrayOf(temp.toByte())
            }
            else -> {
                // Dữ liệu mặc định cho các CAN ID khác
                byteArrayOf(Random.nextInt(0, 256).toByte())
            }
        }
    }
}
