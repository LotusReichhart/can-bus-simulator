package com.lotusreichhart.canbussimulator.presentation.injector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lotusreichhart.canbussimulator.domain.model.CanFrame
import com.lotusreichhart.canbussimulator.domain.usecase.CalculateChecksumUseCase
import com.lotusreichhart.canbussimulator.domain.usecase.SaveCanFrameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InjectorViewModel @Inject constructor(
    private val saveCanFrameUseCase: SaveCanFrameUseCase,
    private val calculateChecksumUseCase: CalculateChecksumUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormUiState())
    val uiState: StateFlow<FormUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<InjectorEffect>()
    val effects: SharedFlow<InjectorEffect> = _effects.asSharedFlow()

    fun handleEvent(event: InjectorEvent) {
        when (event) {
            is InjectorEvent.OnCanIdChanged -> {
                _uiState.update { it.copy(inputCanId = event.canId, submitStatus = SubmitStatus.Idle) }
            }
            is InjectorEvent.OnHexDataChanged -> {
                _uiState.update { it.copy(inputHexData = event.hexData, submitStatus = SubmitStatus.Idle) }
            }
            is InjectorEvent.ClearStatus -> {
                _uiState.update { it.copy(submitStatus = SubmitStatus.Idle) }
            }
            is InjectorEvent.OnSubmitClicked -> {
                submitForm()
            }
        }
    }

    private fun submitForm() {
        val currentState = _uiState.value
        if (!currentState.isFormValid) {
            viewModelScope.launch {
                _effects.emit(InjectorEffect.ShowToast("Error: Invalid form input data!"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(submitStatus = SubmitStatus.Submitting) }
            try {
                delay(300)

                val canId = if (currentState.inputCanId.startsWith("0x", ignoreCase = true)) {
                    currentState.inputCanId.substring(2).toInt(16)
                } else {
                    currentState.inputCanId.toInt()
                }

                val cleanHex = currentState.inputHexData.replace(" ", "")
                val rawBytes = cleanHex.chunked(2)
                    .map { it.toInt(16).toByte() }
                    .toByteArray()

                val checksum = calculateChecksumUseCase(rawBytes)
                val finalData = rawBytes + checksum

                val frame = CanFrame(
                    canId = canId,
                    data = finalData,
                    timestamp = System.currentTimeMillis()
                )

                saveCanFrameUseCase(frame)

                _uiState.update {
                    it.copy(
                        inputCanId = "",
                        inputHexData = "",
                        submitStatus = SubmitStatus.Idle
                    )
                }
                _effects.emit(InjectorEffect.ShowToast("CAN Frame injected successfully! (Checksum: 0x%02X)".format(checksum)))
                _effects.emit(InjectorEffect.ClearFields)
            } catch (e: Exception) {
                _uiState.update { it.copy(submitStatus = SubmitStatus.Error(e.message ?: "Unknown error")) }
                _effects.emit(InjectorEffect.ShowToast("Error sending data: ${e.message}"))
            }
        }
    }
}
