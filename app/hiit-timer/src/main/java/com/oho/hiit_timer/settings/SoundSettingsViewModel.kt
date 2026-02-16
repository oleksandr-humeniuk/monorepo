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

class SoundSettingsViewModel : ViewModel() {

    @Immutable
    data class UiState(
        val soundEnabled: Boolean = true,
        val volume: Float = 0.8f, // 0..1
        val vibrationEnabled: Boolean = true,

        val workSound: String = "Beep 1",
        val restSound: String = "Beep 1",
        val doneSound: String = "Beep 2",
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _events = Channel<SoundNavEvent>(capacity = Channel.Factory.BUFFERED)
    val events: Flow<SoundNavEvent> = _events.receiveAsFlow()

    fun onBack() = emit(SoundNavEvent.Back)

    fun onToggleSoundEnabled() = _state.update { it.copy(soundEnabled = !it.soundEnabled) }
    fun onVolumeChange(v: Float) = _state.update { it.copy(volume = v.coerceIn(0f, 1f)) }
    fun onToggleVibration() = _state.update { it.copy(vibrationEnabled = !it.vibrationEnabled) }

    fun onPickSound(kind: SoundKind) = emit(SoundNavEvent.PickSound(kind))

    private fun emit(e: SoundNavEvent) {
        viewModelScope.launch { _events.send(e) }
    }
}


sealed interface SoundNavEvent {
    data object Back : SoundNavEvent
    data class PickSound(val kind: SoundKind) : SoundNavEvent
}

enum class SoundKind { Work, Rest, Done }