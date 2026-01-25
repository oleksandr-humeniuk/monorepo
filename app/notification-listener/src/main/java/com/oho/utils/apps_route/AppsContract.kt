package com.oho.utils.apps_route

import androidx.compose.runtime.Immutable

object AppsContract {

    sealed interface UiState {

        data object Empty : UiState

        data class Content(
            val items: List<AppItemUi>,
            val showProNudge: Boolean,
            val sheetState: AppActionsSheetUi? = null
        ) : UiState
    }

    sealed interface UiEvent {
        data object SearchClicked : UiEvent
        data object OpenSettings : UiEvent
        data object OpenFilters : UiEvent
        data class OpenApp(val packageName: String) : UiEvent
        data class OpenAppSheet(val packageName: String) : UiEvent
        data class Pin(val packageName: String, val pinned: Boolean) : UiEvent
        data class Hide(val packageName: String) : UiEvent
        data class Clear(val packageName: String) : UiEvent
        data object GoPro : UiEvent

        data object ActionsSheetDismissed : UiEvent
    }

    sealed interface UiEffect {

    }

    @Immutable
    data class AppItemUi(
        val packageName: String,
        val appName: String,
        val lastPreview: String,
        val timeLabel: String,
        val isPinned: Boolean,
        val totalCount: Long,
    )

    @Immutable
    data class AppActionsSheetUi(
        val packageName: String,
        val isPinned: Boolean,
    )
}