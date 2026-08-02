package com.lotusreichhart.canbussimulator.presentation.statistic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lotusreichhart.canbussimulator.domain.usecase.ClearDatabaseUseCase
import com.lotusreichhart.canbussimulator.domain.usecase.GetCanFramesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(
    private val getCanFramesUseCase: GetCanFramesUseCase,
    private val clearDatabaseUseCase: ClearDatabaseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState<StatisticData>>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState<StatisticData>> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<StatisticEffect>()
    val effects: SharedFlow<StatisticEffect> = _effects.asSharedFlow()

    init {
        loadStatistics()
    }

    fun handleEvent(event: StatisticEvent) {
        when (event) {
            is StatisticEvent.LoadData, is StatisticEvent.RetryClicked -> {
                loadStatistics()
            }
            is StatisticEvent.ClearDatabaseClicked -> {
                clearDatabase()
            }
        }
    }

    private fun loadStatistics() {
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            getCanFramesUseCase()
                .catch { exception ->
                    _uiState.value = DetailUiState.Error(exception.message ?: "Error loading statistics")
                }
                .collect { list ->
                    if (list.isEmpty()) {
                        _uiState.value = DetailUiState.Success(StatisticData())
                        return@collect
                    }

                    val total = list.size
                    val rpmFrames = list.filter { it.canId == 0x101 }
                    val speedFrames = list.filter { it.canId == 0x102 }
                    val tempFrames = list.filter { it.canId == 0x103 }
                    val customFrames = list.filter { it.canId !in listOf(0x101, 0x102, 0x103) }

                    val avgRpm = if (rpmFrames.isNotEmpty()) {
                        rpmFrames.map {
                            if (it.data.size >= 2) {
                                ((it.data[0].toInt() and 0xFF) shl 8) or (it.data[1].toInt() and 0xFF)
                            } else 0
                        }.average().toInt()
                    } else 0

                    val avgSpeed = if (speedFrames.isNotEmpty()) {
                        speedFrames.map {
                            if (it.data.isNotEmpty()) it.data[0].toInt() and 0xFF else 0
                        }.average().toInt()
                    } else 0

                    val avgTemp = if (tempFrames.isNotEmpty()) {
                        tempFrames.map {
                            if (it.data.isNotEmpty()) it.data[0].toInt() and 0xFF else 0
                        }.average().toInt()
                    } else 0

                    val latest = list.firstOrNull()

                    val data = StatisticData(
                        totalFramesCount = total,
                        rpmFramesCount = rpmFrames.size,
                        speedFramesCount = speedFrames.size,
                        tempFramesCount = tempFrames.size,
                        customFramesCount = customFrames.size,
                        averageRpm = avgRpm,
                        averageSpeed = avgSpeed,
                        averageTemp = avgTemp,
                        latestFrame = latest
                    )

                    _uiState.value = DetailUiState.Success(data)
                }
        }
    }

    private fun clearDatabase() {
        viewModelScope.launch {
            try {
                clearDatabaseUseCase()
                _effects.emit(StatisticEffect.ShowToast("Cleared all CAN Frames in the database successfully!"))
            } catch (e: Exception) {
                _effects.emit(StatisticEffect.ShowToast("Error clearing database: ${e.message}"))
            }
        }
    }
}
