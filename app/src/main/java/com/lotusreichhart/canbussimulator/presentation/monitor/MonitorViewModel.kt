package com.lotusreichhart.canbussimulator.presentation.monitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lotusreichhart.canbussimulator.domain.model.CanFrame
import com.lotusreichhart.canbussimulator.domain.usecase.GetCanFramesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonitorViewModel @Inject constructor(
    private val getCanFramesUseCase: GetCanFramesUseCase
) : ViewModel() {

    private val _searchFlow = MutableStateFlow("")
    private val _filterFlow = MutableStateFlow("ALL")
    private val _screenStatusFlow = MutableStateFlow<ScreenStatus>(ScreenStatus.InitialLoading)

    private val _effects = MutableSharedFlow<MonitorEffect>()
    val effects: SharedFlow<MonitorEffect> = _effects.asSharedFlow()

    val uiState: StateFlow<HeavyUiState<CanFrame>> = combine(
        getCanFramesUseCase(),
        _searchFlow,
        _filterFlow,
        _screenStatusFlow
    ) { rawFrames, search, filter, status ->
        val filtered = rawFrames.filter { frame ->
            val matchesFilter = if (filter == "ALL") {
                true
            } else {
                val hexId = "0x" + frame.canId.toString(16).uppercase()
                hexId.equals(filter, ignoreCase = true)
            }

            val matchesSearch = if (search.isEmpty()) {
                true
            } else {
                val hexData = frame.data.joinToString("") { "%02X".format(it) }
                val hexId = "0x" + frame.canId.toString(16).uppercase()
                hexId.contains(search, ignoreCase = true) || hexData.contains(search, ignoreCase = true)
            }
            matchesFilter && matchesSearch
        }

        val contentData = ContentData(items = filtered, isEndOfList = true)
        val interactionData = InteractionData(searchQuery = search, activeFilter = filter)

        HeavyUiState(
            status = if (status is ScreenStatus.InitialLoading && rawFrames.isNotEmpty()) ScreenStatus.Idle else status,
            content = contentData,
            interaction = interactionData
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HeavyUiState(status = ScreenStatus.InitialLoading)
    )

    fun handleEvent(event: MonitorEvent) {
        viewModelScope.launch {
            when (event) {
                is MonitorEvent.LoadInitial -> {
                    _screenStatusFlow.value = ScreenStatus.Idle
                }
                is MonitorEvent.Refresh -> {
                    _screenStatusFlow.value = ScreenStatus.Refreshing
                    delay(500)
                    _screenStatusFlow.value = ScreenStatus.Idle
                }
                is MonitorEvent.LoadMore -> {
                    _screenStatusFlow.value = ScreenStatus.LoadingMore
                    delay(500)
                    _screenStatusFlow.value = ScreenStatus.Idle
                }
                is MonitorEvent.UpdateSearch -> {
                    _searchFlow.value = event.query
                }
                is MonitorEvent.UpdateFilter -> {
                    _filterFlow.value = event.filter
                }
                is MonitorEvent.ItemClicked -> {
                    _effects.emit(MonitorEffect.NavigateToDetail(event.frame))
                }
            }
        }
    }
}
