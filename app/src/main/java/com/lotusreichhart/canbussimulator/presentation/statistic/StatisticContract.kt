package com.lotusreichhart.canbussimulator.presentation.statistic

import com.lotusreichhart.canbussimulator.domain.model.CanFrame

sealed interface DetailUiState<out T> {
    data object Loading : DetailUiState<Nothing>
    data class Success<T>(val data: T) : DetailUiState<T>
    data class Error(val message: String) : DetailUiState<Nothing>
}

data class StatisticData(
    val totalFramesCount: Int = 0,
    val rpmFramesCount: Int = 0,
    val speedFramesCount: Int = 0,
    val tempFramesCount: Int = 0,
    val customFramesCount: Int = 0,
    val averageRpm: Int = 0,
    val averageSpeed: Int = 0,
    val averageTemp: Int = 0,
    val latestFrame: CanFrame? = null
)

sealed interface StatisticEvent {
    data object LoadData : StatisticEvent
    data object RetryClicked : StatisticEvent
    data object ClearDatabaseClicked : StatisticEvent
}

sealed interface StatisticEffect {
    data class ShowToast(val message: String) : StatisticEffect
}
