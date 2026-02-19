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
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
) : ViewModel() {

    enum class ThemeMode(val value: String) {
        System("system"),
        Light("light"),
        Dark("dark")
    }

    sealed interface UiState {
        data object Loading : UiState

        @Immutable
        data class Content(
            // Workout
            val defaultPrepareSec: Int,
            val showTotalRemaining: Boolean,
            val autoStartNextPhase: Boolean,

            // General
            val themeMode: ThemeMode,
            val keepScreenOn: Boolean,

            // Footer
            val versionName: String,

            // UI-only
            val isThemeSheetVisible: Boolean = false,
        ) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _events = Channel<SettingsNavEvent>(capacity = Channel.Factory.BUFFERED)
    val events: Flow<SettingsNavEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.hiitPreferences.collect { prefs ->
                val isThemeSheetVisible =
                    (_state.value as? UiState.Content)?.isThemeSheetVisible ?: false
                _state.value = UiState.Content(
                    defaultPrepareSec = prefs.defaultPrepareSec,
                    showTotalRemaining = prefs.showTotalRemaining,
                    autoStartNextPhase = prefs.autoStartNextPhase,
                    themeMode = prefs.themeMode,
                    keepScreenOn = prefs.keepScreenOn,
                    versionName = "Version ${BuildConfig.VERSION_NAME}",
                    isThemeSheetVisible = isThemeSheetVisible,
                )
            }
        }
    }

    fun onBack() = emit(SettingsNavEvent.Back)

    fun onPrepareMinus() = withContent { content ->
        val next = (content.defaultPrepareSec - 5).coerceAtLeast(0)
        viewModelScope.launch { repository.setDefaultPrepareSec(next) }
    }

    fun onPreparePlus() = withContent { content ->
        val next = (content.defaultPrepareSec + 5).coerceAtMost(120)
        viewModelScope.launch { repository.setDefaultPrepareSec(next) }
    }

    fun onToggleShowTotalRemaining() = withContent { content ->
        viewModelScope.launch { repository.setShowTotalRemaining(!content.showTotalRemaining) }
    }

    fun onToggleAutoStartNextPhase() = withContent { content ->
        viewModelScope.launch { repository.setAutoStartNextPhase(!content.autoStartNextPhase) }
    }

    fun onOpenSound() = emit(SettingsNavEvent.OpenSound)

    fun openThemeSheet() = withContent { content ->
        _state.value = content.copy(isThemeSheetVisible = true)
    }

    fun dismissThemeSheet() = withContent { content ->
        _state.value = content.copy(isThemeSheetVisible = false)
    }

    fun selectTheme(mode: ThemeMode) = withContent { content ->
        _state.value = content.copy(isThemeSheetVisible = false)
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun onToggleKeepScreenOn() = withContent { content ->
        viewModelScope.launch { repository.setKeepScreenOn(!content.keepScreenOn) }
    }

    fun onContactSupport() = emit(SettingsNavEvent.ContactSupport)
    fun onRateApp() = emit(SettingsNavEvent.RateApp)
    fun onOpenPrivacyPolicy() = emit(SettingsNavEvent.OpenPrivacyPolicy)
    fun onOpenTerms() = emit(SettingsNavEvent.OpenTerms)

    private inline fun withContent(block: (UiState.Content) -> Unit) {
        val content = _state.value as? UiState.Content ?: return
        block(content)
    }

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
    data object OpenTerms : SettingsNavEvent
}