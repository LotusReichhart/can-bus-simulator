package com.lotusreichhart.canbussimulator

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.lotusreichhart.canbussimulator.presentation.injector.InjectorScreen
import com.lotusreichhart.canbussimulator.presentation.injector.InjectorViewModel
import com.lotusreichhart.canbussimulator.presentation.monitor.MonitorScreen
import com.lotusreichhart.canbussimulator.presentation.monitor.MonitorViewModel
import com.lotusreichhart.canbussimulator.presentation.statistic.StatisticScreen
import com.lotusreichhart.canbussimulator.presentation.statistic.StatisticViewModel
import com.lotusreichhart.canbussimulator.service.CanBusService
import com.lotusreichhart.canbussimulator.ui.theme.CanBusSimulatorTheme
import dagger.hilt.android.AndroidEntryPoint

enum class Tab { MONITOR, INJECTOR, STATISTIC }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val monitorViewModel: MonitorViewModel by viewModels()
    private val injectorViewModel: InjectorViewModel by viewModels()
    private val statisticViewModel: StatisticViewModel by viewModels()

    // Tham chiếu đến interface AIDL của dịch vụ mô phỏng CAN Bus
    private var canBusService: ICanBusService? = null

    // Quản lý kết nối vòng đời với Bound Service
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // Chuyển đổi binder nhận được sang giao diện AIDL ICanBusService
            canBusService = ICanBusService.Stub.asInterface(service)
            Log.d("MainActivity", "Successfully connected to CanBusService via AIDL")
            try {
                // Tự động kích hoạt mô phỏng sau khi kết nối thành công
                canBusService?.startSimulation()
            } catch (e: RemoteException) {
                Log.e("MainActivity", "Failed to start simulation through AIDL interface", e)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // Đặt lại tham chiếu khi dịch vụ bị ngắt kết nối đột ngột
            canBusService = null
            Log.d("MainActivity", "Disconnected from CanBusService")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this) {
            // Chủ động kết thúc Activity
            finish()
        }
        
        // Khởi tạo Intent và liên kết (bind) đến CanBusService
        val serviceIntent = Intent(this, CanBusService::class.java)
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)

        enableEdgeToEdge()
        setContent {
            CanBusSimulatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DashboardContent(
                        monitorViewModel = monitorViewModel,
                        injectorViewModel = injectorViewModel,
                        statisticViewModel = statisticViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Hủy liên kết dịch vụ khi Activity bị hủy để tránh rò rỉ tài nguyên (Memory Leak)
        unbindService(serviceConnection)
        Log.d("MainActivity", "Unbound CanBusService on Activity destruction")
    }
}

@Composable
fun DashboardContent(
    monitorViewModel: MonitorViewModel,
    injectorViewModel: InjectorViewModel,
    statisticViewModel: StatisticViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(Tab.MONITOR) }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1318))
    ) {
        NavigationRail(
            containerColor = Color(0xFF161C23),
            contentColor = Color(0xFF90A4AE),
            modifier = Modifier.fillMaxHeight()
        ) {
            NavigationRailItem(
                selected = activeTab == Tab.MONITOR,
                onClick = { activeTab = Tab.MONITOR },
                icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Monitor") },
                label = { Text("Monitor") },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = Color(0xFF0F1318),
                    selectedTextColor = Color(0xFF00E5FF),
                    indicatorColor = Color(0xFF00E5FF),
                    unselectedIconColor = Color(0xFF90A4AE),
                    unselectedTextColor = Color(0xFF90A4AE)
                )
            )
            NavigationRailItem(
                selected = activeTab == Tab.INJECTOR,
                onClick = { activeTab = Tab.INJECTOR },
                icon = { Icon(Icons.Default.Build, contentDescription = "Injector") },
                label = { Text("Injector") },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = Color(0xFF0F1318),
                    selectedTextColor = Color(0xFF00E5FF),
                    indicatorColor = Color(0xFF00E5FF),
                    unselectedIconColor = Color(0xFF90A4AE),
                    unselectedTextColor = Color(0xFF90A4AE)
                )
            )
            NavigationRailItem(
                selected = activeTab == Tab.STATISTIC,
                onClick = { activeTab = Tab.STATISTIC },
                icon = { Icon(Icons.Default.Info, contentDescription = "Statistic") },
                label = { Text("Statistic") },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = Color(0xFF0F1318),
                    selectedTextColor = Color(0xFF00E5FF),
                    indicatorColor = Color(0xFF00E5FF),
                    unselectedIconColor = Color(0xFF90A4AE),
                    unselectedTextColor = Color(0xFF90A4AE)
                )
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            when (activeTab) {
                Tab.MONITOR -> MonitorScreen(viewModel = monitorViewModel)
                Tab.INJECTOR -> InjectorScreen(viewModel = injectorViewModel)
                Tab.STATISTIC -> StatisticScreen(viewModel = statisticViewModel)
            }
        }
    }
}