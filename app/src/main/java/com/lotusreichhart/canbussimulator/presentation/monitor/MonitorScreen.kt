package com.lotusreichhart.canbussimulator.presentation.monitor

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.lotusreichhart.canbussimulator.domain.model.CanFrame
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

private val DarkBg = Color(0xFF0F1318)
private val CardBg = Color(0xFF161C23)
private val CyanNeon = Color(0xFF00E5FF)
private val GreenNeon = Color(0xFF00E676)
private val RedNeon = Color(0xFFFF1744)
private val OrangeNeon = Color(0xFFFF9100)
private val TextWhite = Color(0xFFECEFF1)
private val TextGray = Color(0xFF90A4AE)

@Composable
fun MonitorScreen(
    viewModel: MonitorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", LocalLocale.current.platformLocale)

    LaunchedEffect(key1 = Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MonitorEffect.NavigateToDetail -> {
                    val hexData = effect.frame.data.joinToString(" ") { "%02X".format(it) }
                    val details = "ID: 0x${effect.frame.canId.toString(16).uppercase()}\n" +
                            "Data: $hexData\n" +
                            "Time: ${timeFormat.format(Date(effect.frame.timestamp))}"
                    Toast.makeText(context, "CAN Frame Details:\n$details", Toast.LENGTH_LONG).show()
                }
                is MonitorEffect.ShowSnackbar -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
                .padding(end = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SYSTEM FILTERS",
                    color = CyanNeon,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { viewModel.handleEvent(MonitorEvent.Refresh) },
                    modifier = Modifier
                        .background(CardBg, RoundedCornerShape(8.dp))
                        .border(1.dp, CyanNeon.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = CyanNeon)
                }
            }

            OutlinedTextField(
                value = state.interaction.searchQuery,
                onValueChange = { viewModel.handleEvent(MonitorEvent.UpdateSearch(it)) },
                label = { Text("Search ID or Hex Data", color = TextGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanNeon) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanNeon,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                    focusedLabelColor = CyanNeon,
                    unfocusedLabelColor = TextGray,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Filter by CAN ID",
                color = TextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val filters = listOf(
                "ALL" to "All Frames",
                "0x101" to "0x101 (Engine RPM)",
                "0x102" to "0x102 (Vehicle Speed)",
                "0x103" to "0x103 (Engine Temp)"
            )

            filters.forEach { (filterVal, label) ->
                val isSelected = state.interaction.activeFilter == filterVal
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.handleEvent(MonitorEvent.UpdateFilter(filterVal)) },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        labelColor = TextGray,
                        selectedLabelColor = DarkBg,
                        selectedContainerColor = CyanNeon,
                        containerColor = CardBg
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = CyanNeon.copy(alpha = 0.5f),
                        selectedBorderColor = CyanNeon
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .border(1.dp, CyanNeon.copy(alpha = 0.2f), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("TIMESTAMP", color = TextGray, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                Text("CAN ID", color = TextGray, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text("HEX DATA (CRC-8 End)", color = TextGray, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(2.5f))
                Text("METRIC", color = TextGray, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1.5f))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(CardBg.copy(alpha = 0.5f), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .border(
                        1.dp,
                        CyanNeon.copy(alpha = 0.1f),
                        RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
            ) {
                if (state.status is ScreenStatus.InitialLoading || state.status is ScreenStatus.Refreshing) {
                    CircularProgressIndicator(
                        color = CyanNeon,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (state.content.items.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No CAN Frames found", color = TextGray, fontSize = 14.sp)
                        Text("Please start Simulator Service", color = TextGray.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.content.items) { frame ->
                            CanFrameRow(
                                frame = frame,
                                timeFormat = timeFormat,
                                onClick = { viewModel.handleEvent(MonitorEvent.ItemClicked(frame)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CanFrameRow(
    frame: CanFrame,
    timeFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    val hexData = frame.data.joinToString(" ") { "%02X".format(it) }
    val hexId = "0x" + frame.canId.toString(16).uppercase()

    val (deviceLabel, deviceColor, parsedVal) = when (frame.canId) {
        0x101 -> {
            if (frame.data.size >= 2) {
                val rpm = ((frame.data[0].toInt() and 0xFF) shl 8) or (frame.data[1].toInt() and 0xFF)
                Triple("Engine RPM", GreenNeon, "$rpm RPM")
            } else {
                Triple("Engine RPM", GreenNeon, "N/A")
            }
        }
        0x102 -> {
            if (frame.data.isNotEmpty()) {
                val speed = frame.data[0].toInt() and 0xFF
                Triple("Vehicle Speed", OrangeNeon, "$speed km/h")
            } else {
                Triple("Vehicle Speed", OrangeNeon, "N/A")
            }
        }
        0x103 -> {
            if (frame.data.isNotEmpty()) {
                val temp = frame.data[0].toInt() and 0xFF
                Triple("Engine Temp", RedNeon, "$temp °C")
            } else {
                Triple("Engine Temp", RedNeon, "N/A")
            }
        }
        else -> Triple("Manual Injection", CyanNeon, "Custom Frame")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = timeFormat.format(Date(frame.timestamp)),
                color = TextWhite,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1.2f)
            )

            Text(
                text = hexId,
                color = CyanNeon,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = hexData,
                color = TextWhite,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(2.5f)
            )

            Column(
                modifier = Modifier.weight(1.5f)
            ) {
                Text(
                    text = deviceLabel,
                    color = deviceColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = parsedVal,
                    color = TextWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        HorizontalDivider(color = Color(0xFF1E252D), thickness = 1.dp)
    }
}
