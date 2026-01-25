package com.oho.utils.apps_route

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oho.utils.database.NotificationDatabase
import com.oho.utils.domain.formatTimeLabel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Stable
class AppsViewModel(
    private val db: NotificationDatabase,
) : ViewModel() {

    private val _state = MutableStateFlow<AppsContract.UiState>(AppsContract.UiState.Empty)
    val state: StateFlow<AppsContract.UiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<AppsContract.UiEffect>()
    val effects = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            db.appsDao().observeApps(
                minPostedAt = null
            ).collect { notifications ->
                if (notifications.isEmpty()) {
                    _state.value = AppsContract.UiState.Empty
                } else {
                    _state.value = AppsContract.UiState.Content(
                        searchQuery = "",
                        items = notifications.map { appEntity ->
                            AppsContract.AppItemUi(
                                packageName = appEntity.packageName,
                                appName = appEntity.appName ?: "Unknown App",
                                lastPreview = appEntity.lastTitle ?: "No Title",
                                timeLabel = formatTimeLabel(
                                    lastPostedAt = appEntity.lastPostedAt,
                                    now = System.currentTimeMillis()
                                ).orEmpty(),
                                isPinned = appEntity.isPinned,
                                isLastPreviewLocked = false, // implement locking logic
                                totalCount = appEntity.totalCount
                            )
                        },
                        showProNudge = false,
                    )
                }
            }
        }
    }

    fun onEvent(event: AppsContract.UiEvent) {
        when (event) {
            is AppsContract.UiEvent.SearchChanged -> {
            }

            else -> Unit
        }
    }
}