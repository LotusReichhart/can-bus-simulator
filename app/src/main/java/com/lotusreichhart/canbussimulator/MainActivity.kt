package com.lotusreichhart.canbussimulator

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val serviceIntent = Intent(this, CanBusService::class.java)
        startService(serviceIntent)

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