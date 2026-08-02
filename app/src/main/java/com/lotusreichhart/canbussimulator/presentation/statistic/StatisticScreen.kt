package com.lotusreichhart.canbussimulator.presentation.statistic

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DarkBg = Color(0xFF0F1318)
private val CardBg = Color(0xFF161C23)
private val CyanNeon = Color(0xFF00E5FF)
private val GreenNeon = Color(0xFF00E676)
private val RedNeon = Color(0xFFFF1744)
private val OrangeNeon = Color(0xFFFF9100)
private val TextWhite = Color(0xFFECEFF1)
private val TextGray = Color(0xFF90A4AE)

@Composable
fun StatisticScreen(
    viewModel: StatisticViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    LaunchedEffect(key1 = Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is StatisticEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        when (val uiState = state) {
            is DetailUiState.Loading -> {
                CircularProgressIndicator(
                    color = CyanNeon,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is DetailUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Error loading statistics: ${uiState.message}", color = RedNeon, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.handleEvent(StatisticEvent.RetryClicked) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = DarkBg)
                    ) {
                        Text("Retry")
                    }
                }
            }
            is DetailUiState.Success -> {
                val data = uiState.data
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "TOTAL PROCESSED",
                                value = data.totalFramesCount.toString(),
                                unit = "Frames",
                                color = CyanNeon,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "AVERAGE ENGINE RPM",
                                value = data.averageRpm.toString(),
                                unit = "RPM",
                                color = GreenNeon,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "AVERAGE VEHICLE SPEED",
                                value = data.averageSpeed.toString(),
                                unit = "km/h",
                                color = OrangeNeon,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "AVERAGE ENGINE TEMP",
                                value = data.averageTemp.toString(),
                                unit = "°C",
                                color = RedNeon,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardBg, RoundedCornerShape(12.dp))
                                .border(1.dp, TextGray.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "LATEST CAN FRAME STATUS",
                                color = CyanNeon,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            val latest = data.latestFrame
                            if (latest != null) {
                                val hexData = latest.data.joinToString(" ") { "%02X".format(it) }
                                Text("CAN ID: 0x${latest.canId.toString(16).uppercase()}", color = TextWhite, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Text("Raw Data: $hexData", color = TextWhite, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                Text("Timestamp: ${timeFormat.format(Date(latest.timestamp))}", color = TextGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            } else {
                                Text("No frames recorded in database", color = TextGray, fontSize = 12.sp)
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardBg, RoundedCornerShape(12.dp))
                                .border(1.dp, TextGray.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "FRAME TYPE DISTRIBUTION",
                                color = OrangeNeon,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text("Engine RPM (0x101): ${data.rpmFramesCount} frames", color = TextGray, fontSize = 11.sp)
                            Text("Vehicle Speed (0x102): ${data.speedFramesCount} frames", color = TextGray, fontSize = 11.sp)
                            Text("Engine Temp (0x103): ${data.tempFramesCount} frames", color = TextGray, fontSize = 11.sp)
                            Text("Manual Injection (Other): ${data.customFramesCount} frames", color = TextGray, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.handleEvent(StatisticEvent.ClearDatabaseClicked) },
                            colors = ButtonDefaults.buttonColors(containerColor = RedNeon),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text("RESET DATABASE", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(CardBg, RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = TextGray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                color = color,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = unit,
                color = TextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}
