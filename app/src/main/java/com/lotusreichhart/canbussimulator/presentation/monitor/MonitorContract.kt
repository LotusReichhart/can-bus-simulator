package com.lotusreichhart.canbussimulator.presentation.monitor

import com.lotusreichhart.canbussimulator.domain.model.CanFrame

sealed interface ScreenStatus {
    data object Idle : ScreenStatus
    data object InitialLoading : ScreenStatus
    data object Refreshing : ScreenStatus
    data object LoadingMore : ScreenStatus
    data class Error(val message: String, val action: ErrorAction) : ScreenStatus
}

enum class ErrorAction { INITIAL, REFRESH, LOAD_MORE }

data class ContentData<T>(
    val items: List<T> = emptyList(),
    val isEndOfList: Boolean = false
)

data class InteractionData(
    val searchQuery: String = "",
    val activeFilter: String = "ALL" // "ALL", "0x101", "0x102", "0x103", v.v.
)

data class HeavyUiState<T>(
    val status: ScreenStatus = ScreenStatus.Idle,
    val content: ContentData<T> = ContentData(),
    val interaction: InteractionData = InteractionData()
)

sealed interface MonitorEvent {
    data object LoadInitial : MonitorEvent
    data object Refresh : MonitorEvent
    data object LoadMore : MonitorEvent
    data class UpdateSearch(val query: String) : MonitorEvent
    data class UpdateFilter(val filter: String) : MonitorEvent
    data class ItemClicked(val frame: CanFrame) : MonitorEvent
}

sealed interface MonitorEffect {
    data class NavigateToDetail(val frame: CanFrame) : MonitorEffect
    data class ShowSnackbar(val message: String) : MonitorEffect
}
