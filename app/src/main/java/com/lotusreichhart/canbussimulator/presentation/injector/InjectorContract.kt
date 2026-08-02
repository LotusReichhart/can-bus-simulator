package com.lotusreichhart.canbussimulator.presentation.injector

sealed interface SubmitStatus {
    data object Idle : SubmitStatus
    data object Submitting : SubmitStatus
    data class Error(val message: String) : SubmitStatus
}

data class FormUiState(
    val inputCanId: String = "",
    val inputHexData: String = "",
    val submitStatus: SubmitStatus = SubmitStatus.Idle
) {
    val isFormValid: Boolean
        get() {
            if (inputCanId.isBlank() || inputHexData.isBlank()) return false
            
            // Validate CAN ID: Phải phân tích được thành số nguyên
            val canId = try {
                if (inputCanId.startsWith("0x", ignoreCase = true)) {
                    inputCanId.substring(2).toInt(16)
                } else {
                    inputCanId.toInt()
                }
            } catch (e: NumberFormatException) {
                return false
            }
            if (canId !in 0..0x7FF) return false // Standard CAN ID limit

            // Validate Hex Data: Phải có độ dài chẵn và chứa ký tự hex hợp lệ
            val cleanHex = inputHexData.replace(" ", "")
            if (cleanHex.length % 2 != 0) return false
            val hexRegex = Regex("^[0-9a-fA-F]+$")
            if (!hexRegex.matches(cleanHex)) return false
            
            return true
        }
}

sealed interface InjectorEvent {
    data class OnCanIdChanged(val canId: String) : InjectorEvent
    data class OnHexDataChanged(val hexData: String) : InjectorEvent
    data object OnSubmitClicked : InjectorEvent
    data object ClearStatus : InjectorEvent
}

sealed interface InjectorEffect {
    data class ShowToast(val message: String) : InjectorEffect
    data object ClearFields : InjectorEffect
}
