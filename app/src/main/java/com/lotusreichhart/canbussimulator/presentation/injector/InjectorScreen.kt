package com.lotusreichhart.canbussimulator.presentation.injector

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

private val DarkBg = Color(0xFF0F1318)
private val CardBg = Color(0xFF161C23)
private val CyanNeon = Color(0xFF00E5FF)
private val OrangeNeon = Color(0xFFFF9100)
private val RedNeon = Color(0xFFFF1744)
private val TextWhite = Color(0xFFECEFF1)
private val TextGray = Color(0xFF90A4AE)

@Composable
fun InjectorScreen(
    viewModel: InjectorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is InjectorEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is InjectorEffect.ClearFields -> {
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
                .weight(1.2f)
                .fillMaxHeight()
                .background(CardBg, RoundedCornerShape(12.dp))
                .border(1.dp, CyanNeon.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "MANUAL CAN FRAME INJECTION",
                color = CyanNeon,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Column {
                OutlinedTextField(
                    value = state.inputCanId,
                    onValueChange = { viewModel.handleEvent(InjectorEvent.OnCanIdChanged(it)) },
                    label = { Text("CAN ID (e.g. 0x101 or 257)", color = TextGray) },
                    placeholder = { Text("Decimal or 0xHex format", color = TextGray.copy(alpha = 0.5f)) },
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

                val cleanId = state.inputCanId.trim()
                if (cleanId.isNotEmpty()) {
                    val parsedId = try {
                        if (cleanId.startsWith("0x", ignoreCase = true)) {
                            cleanId.substring(2).toInt(16)
                        } else {
                            cleanId.toInt()
                        }
                    } catch (e: NumberFormatException) {
                        -1
                    }

                    if (parsedId == -1) {
                        Text("Invalid ID format (only numbers or 0xHex allowed)", color = RedNeon, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    } else if (parsedId !in 0..0x7FF) {
                        Text("Standard CAN ID must be between 0 and 0x7FF (2047)", color = RedNeon, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    } else {
                        Text("Valid ID: Decimal=$parsedId, Hex=0x${parsedId.toString(16).uppercase()}", color = CyanNeon.copy(alpha = 0.7f), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Column {
                OutlinedTextField(
                    value = state.inputHexData,
                    onValueChange = { viewModel.handleEvent(InjectorEvent.OnHexDataChanged(it)) },
                    label = { Text("Hex Data (e.g. AABBCC or 010203)", color = TextGray) },
                    placeholder = { Text("Each byte consists of 2 Hex characters (A-F, 0-9)", color = TextGray.copy(alpha = 0.5f)) },
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

                val cleanHex = state.inputHexData.replace(" ", "")
                if (cleanHex.isNotEmpty()) {
                    val hexRegex = Regex("^[0-9a-fA-F]+$")
                    if (!hexRegex.matches(cleanHex)) {
                        Text("Data contains invalid characters (only 0-9, A-F allowed)", color = RedNeon, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    } else if (cleanHex.length % 2 != 0) {
                        Text("Hex string length must be even (2 characters per byte)", color = OrangeNeon, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    } else {
                        val byteSize = cleanHex.length / 2
                        Text("Valid: $byteSize bytes of raw data", color = CyanNeon.copy(alpha = 0.7f), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.submitStatus is SubmitStatus.Submitting) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = CyanNeon, modifier = Modifier.width(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Injecting data into system...", color = CyanNeon, fontSize = 13.sp)
                }
            } else if (state.submitStatus is SubmitStatus.Error) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RedNeon.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .border(1.dp, RedNeon, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "ERROR: ${(state.submitStatus as SubmitStatus.Error).message}",
                        color = RedNeon,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = { viewModel.handleEvent(InjectorEvent.OnSubmitClicked) },
                enabled = state.isFormValid && state.submitStatus !is SubmitStatus.Submitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanNeon,
                    disabledContainerColor = CyanNeon.copy(alpha = 0.15f),
                    contentColor = DarkBg,
                    disabledContentColor = TextGray.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "INJECT FRAME",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(CardBg.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .border(1.dp, TextGray.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "CAN BUS TECHNICAL DOCUMENT",
                color = OrangeNeon,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "1. CAN Frame Structure",
                color = TextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "The simulator uses the CAN Standard (11-bit ID: 0x000 to 0x7FF). The payload data contains up to 8 bytes of raw data.",
                color = TextGray,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "2. JNI Checksum Security Mechanism",
                color = TextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "When you click 'Inject Frame', the app calls the JNI C++ library (NativeCalculator) to compute a CRC-8 checksum based on the SAE J1850 polynomial (Poly: 0x07). The computed checksum is automatically appended to the final byte of the CAN Frame before storage.",
                color = TextGray,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "3. Default Simulation IDs",
                color = TextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "- 0x101: Engine RPM (RPM) - 2 bytes\n" +
                       "- 0x102: Vehicle Speed (Speed) - 1 byte\n" +
                       "- 0x103: Engine Temperature (Temperature) - 1 byte",
                color = TextGray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
