package com.oho.hiit_timer.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oho.utils.BuildConfig
import com.oho.hiit_timer.data.store.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
) : ViewModel() {

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
        val versionName: String = "Version ${BuildConfig.VERSION_NAME}",
        val isThemeSheetVisible: Boolean = false
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _events = Channel<SettingsNavEvent>(capacity = Channel.Factory.BUFFERED)
    val events: Flow<SettingsNavEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.hiitPreferences.collect { prefs ->
                _state.update {
                    it.copy(
                        defaultPrepareSec = prefs.defaultPrepareSec,
                        showTotalRemaining = prefs.showTotalRemaining,
                        autoStartNextPhase = prefs.autoStartNextPhase,
                        themeMode = prefs.themeMode,
                        keepScreenOn = prefs.keepScreenOn,
                    )
                }
            }
        }
    }

    fun onBack() = emit(SettingsNavEvent.Back)

    fun onPrepareMinus() {
        val next = (_state.value.defaultPrepareSec - 5).coerceAtLeast(0)
        viewModelScope.launch { repository.setDefaultPrepareSec(next) }
    }

    fun onPreparePlus() {
        val next = (_state.value.defaultPrepareSec + 5).coerceAtMost(120)
        viewModelScope.launch { repository.setDefaultPrepareSec(next) }
    }

    fun onToggleShowTotalRemaining() {
        viewModelScope.launch { repository.setShowTotalRemaining(!_state.value.showTotalRemaining) }
    }

    fun onToggleAutoStartNextPhase() {
        viewModelScope.launch { repository.setAutoStartNextPhase(!_state.value.autoStartNextPhase) }
    }

    fun onOpenSound() = emit(SettingsNavEvent.OpenSound)

    fun openThemeSheet() = _state.update { it.copy(isThemeSheetVisible = true) }
    fun dismissThemeSheet() = _state.update { it.copy(isThemeSheetVisible = false) }

    fun selectTheme(mode: ThemeMode) {
        _state.update { it.copy(isThemeSheetVisible = false) }
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun onToggleKeepScreenOn() {
        viewModelScope.launch { repository.setKeepScreenOn(!_state.value.keepScreenOn) }
    }

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