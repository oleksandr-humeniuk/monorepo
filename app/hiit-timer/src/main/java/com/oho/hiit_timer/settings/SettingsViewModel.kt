package com.oho.hiit_timer.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    enum class ThemeMode(
        val value: String
    ) {
        System("system"),
        Light("light"),
        Dark("dark")
    }

    @Immutable
    data class UiState(
        // Workout
        val defaultPrepareSec: Int = 10,
        val showTotalRemaining: Boolean = true,
        val autoStartNextPhase: Boolean = false,

        // General
        val themeMode: ThemeMode = ThemeMode.Dark,
        val keepScreenOn: Boolean = true,

        // Footer
        val versionName: String = "Version 1.2.0",
        val isThemeSheetVisible: Boolean = false
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _events = Channel<SettingsNavEvent>(capacity = Channel.Factory.BUFFERED)
    val events: Flow<SettingsNavEvent> = _events.receiveAsFlow()

    fun onBack() = emit(SettingsNavEvent.Back)

    fun onPrepareMinus() = _state.update {
        it.copy(defaultPrepareSec = (it.defaultPrepareSec - 5).coerceAtLeast(0))
    }

    fun onPreparePlus() = _state.update {
        it.copy(defaultPrepareSec = (it.defaultPrepareSec + 5).coerceAtMost(120))
    }

    fun onToggleShowTotalRemaining() =
        _state.update { it.copy(showTotalRemaining = !it.showTotalRemaining) }

    fun onToggleAutoStartNextPhase() =
        _state.update { it.copy(autoStartNextPhase = !it.autoStartNextPhase) }

    fun onOpenSound() = emit(SettingsNavEvent.OpenSound)
    fun openThemeSheet() = _state.update { it.copy(isThemeSheetVisible = true) }
    fun dismissThemeSheet() = _state.update { it.copy(isThemeSheetVisible = false) }

    fun selectTheme(mode: ThemeMode) = _state.update {
        it.copy(themeMode = mode, isThemeSheetVisible = false)
    }


    fun onToggleKeepScreenOn() = _state.update { it.copy(keepScreenOn = !it.keepScreenOn) }

    fun onContactSupport() = emit(SettingsNavEvent.ContactSupport)
    fun onRateApp() = emit(SettingsNavEvent.RateApp)
    fun onOpenPrivacyPolicy() = emit(SettingsNavEvent.OpenPrivacyPolicy)

    private fun emit(e: SettingsNavEvent) {
        viewModelScope.launch { _events.send(e) }
    }
}

sealed interface SettingsNavEvent {
    data object Back : SettingsNavEvent
    data object OpenSound : SettingsNavEvent
    data object ContactSupport : SettingsNavEvent
    data object RateApp : SettingsNavEvent
    data object OpenPrivacyPolicy : SettingsNavEvent
}