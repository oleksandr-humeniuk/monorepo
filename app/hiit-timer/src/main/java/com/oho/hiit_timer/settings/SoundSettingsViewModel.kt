package com.oho.hiit_timer.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oho.hiit_timer.data.store.SettingsRepository
import com.oho.hiit_timer.data.store.Sound
import com.oho.hiit_timer.data.store.SoundMode
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SoundSettingsViewModel(
    private val repository: SettingsRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState

        @Immutable
        data class Content(
            val soundEnabled: Boolean,
            val volume: Float,
            val vibrationEnabled: Boolean,
            val workSound: Sound,
            val restSound: Sound,
            val doneSound: Sound,
            val soundMode: SoundMode,
        ) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _events = Channel<SoundNavEvent>(capacity = Channel.Factory.BUFFERED)
    val events: Flow<SoundNavEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.hiitPreferences.collect { prefs ->
                _state.value = UiState.Content(
                    soundEnabled = prefs.soundEnabled,
                    volume = prefs.volume,
                    vibrationEnabled = prefs.vibrationEnabled,
                    workSound = prefs.workSound,
                    restSound = prefs.restSound,
                    doneSound = prefs.doneSound,
                    soundMode = prefs.soundMode,
                )
            }
        }
    }

    fun onBack() = emit(SoundNavEvent.Back)

    fun onToggleSoundEnabled() = withContent { content ->
        viewModelScope.launch { repository.setSoundEnabled(!content.soundEnabled) }
    }

    fun onVolumeChange(v: Float) = withContent { content ->
        val coerced = v.coerceIn(0f, 1f)
        // update locally for smooth slider, persist async
        _state.value = content.copy(volume = coerced)
        viewModelScope.launch { repository.setVolume(coerced) }
    }

    fun onToggleVibration() = withContent { content ->
        viewModelScope.launch { repository.setVibrationEnabled(!content.vibrationEnabled) }
    }

    fun onSoundModeChange(mode: SoundMode) {
        viewModelScope.launch { repository.setSoundMode(mode) }
    }

    fun onPickSound(kind: SoundKind) = emit(SoundNavEvent.PickSound(kind))

    private inline fun withContent(block: (UiState.Content) -> Unit) {
        val content = _state.value as? UiState.Content ?: return
        block(content)
    }

    private fun emit(e: SoundNavEvent) {
        viewModelScope.launch { _events.send(e) }
    }
}

sealed interface SoundNavEvent {
    data object Back : SoundNavEvent
    data class PickSound(val kind: SoundKind) : SoundNavEvent
}

enum class SoundKind { Work, Rest, Done }