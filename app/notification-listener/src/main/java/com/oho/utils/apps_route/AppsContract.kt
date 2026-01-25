package com.oho.utils.apps_route

import androidx.compose.runtime.Immutable

object AppsContract {

    sealed interface UiState {

        data object Empty : UiState

        data class Content(
            val items: List<AppItemUi>,
            val showProNudge: Boolean,
        ) : UiState
    }

    sealed interface UiEvent {
        data object SearchClicked : UiEvent
        data object OpenSettings : UiEvent
        data object OpenFilters : UiEvent
        data class OpenApp(val packageName: String) : UiEvent
        data object OpenNotificationAccess : UiEvent
        data object GoPro : UiEvent
    }

    sealed interface UiEffect {
        // keep for later (snackbar, navigation, etc)
    }

    @Immutable
    data class AppItemUi(
        val packageName: String,
        val appName: String,
        val lastPreview: String,
        val timeLabel: String,
        val isPinned: Boolean,
        val totalCount: Long,
        val isLastPreviewLocked: Boolean,
    )
}