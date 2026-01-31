package com.oho.hiit_timer.count_down_screen

import androidx.compose.runtime.Immutable

@Immutable
data class HiitRunUiState(
    val phase: HiitPhase,
    val totalRemaining: Int,
    val phaseLabel: String,
    val phaseRemaining: Int,
    val setIndex: Int,
    val setsTotal: Int,
    val nextLabel: String?,
    val isPaused: Boolean,
)

enum class HiitPhase { Prepare, Work, Rest }
