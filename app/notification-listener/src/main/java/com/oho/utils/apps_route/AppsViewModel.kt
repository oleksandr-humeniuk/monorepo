package com.oho.utils.apps_route

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
import kotlinx.coroutines.flow.update
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
            db.appsDao().observeApps().collect { notifications ->
                if (notifications.isEmpty()) {
                    _state.value = AppsContract.UiState.Empty
                } else {
                    _state.value = AppsContract.UiState.Content(
                        items = notifications.map { appEntity ->
                            AppsContract.AppItemUi(
                                packageName = appEntity.packageName,
                                appName = appEntity.appName ?: "Protected App",
                                lastPreview = appEntity.lastTitle ?: "Hidden title",
                                timeLabel = formatTimeLabel(
                                    lastPostedAt = appEntity.lastPostedAt,
                                    now = System.currentTimeMillis()
                                ).orEmpty(),
                                isPinned = appEntity.isPinned,
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
            is AppsContract.UiEvent.SearchClicked -> {
            }

            is AppsContract.UiEvent.OpenAppSheet -> {
                _state.update { current ->
                    when (current) {
                        is AppsContract.UiState.Content -> current.copy(
                            sheetState = AppsContract.AppActionsSheetUi(
                                packageName = event.packageName,
                                isPinned = current.items.firstOrNull { it.packageName == event.packageName }?.isPinned
                                    ?: false
                            )
                        )

                        is AppsContract.UiState.Empty -> current
                    }
                }
            }

            is AppsContract.UiEvent.ActionsSheetDismissed -> {
                _state.update { current ->
                    when (current) {
                        is AppsContract.UiState.Content -> current.copy(
                            sheetState = null
                        )

                        is AppsContract.UiState.Empty -> current
                    }
                }
            }

            is AppsContract.UiEvent.Pin -> {
                viewModelScope.launch {
                    db.appsDao().setPinned(
                        pkg = event.packageName,
                        pinned = !event.pinned
                    )
                }
            }

            is AppsContract.UiEvent.Hide -> {
                viewModelScope.launch {
                    db.appsDao().setExcluded(
                        pkg = event.packageName,
                        excluded = true
                    )
                }
            }

            is AppsContract.UiEvent.Clear -> {
                viewModelScope.launch {
                    //TODO: implement clear notifications for app
                }
            }

            else -> Unit
        }
    }
}