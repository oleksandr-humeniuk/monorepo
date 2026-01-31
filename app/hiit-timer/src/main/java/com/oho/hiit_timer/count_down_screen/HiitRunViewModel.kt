package com.oho.hiit_timer.count_down_screen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HiitRunViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        HiitRunUiState(
            phase = HiitPhase.Rest,
            phaseLabel = "REST",
            phaseRemaining = 10,
            totalRemaining = 60,
            setIndex = 3,
            setsTotal = 8,
            nextLabel = "Work 00:45",
            isPaused = true,
        )
    )
    val state: StateFlow<HiitRunUiState> = _state.asStateFlow()

    fun onPauseResume() {}
    fun onNext() {}
    fun onPrevious() {}
    fun onClose() {}
}