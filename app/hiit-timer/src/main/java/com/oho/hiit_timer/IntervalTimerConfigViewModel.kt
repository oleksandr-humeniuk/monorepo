package com.oho.hiit_timer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.math.max

class IntervalTimerConfigViewModel : ViewModel() {

    var state by mutableStateOf(
        UiState(
            sets = 8,
            workSec = 30,
            restSec = 10,
            // keep true if you want total to exclude the final rest
            skipLastRest = true,
        )
    )
        private set

    fun onBackClicked() {
        // navigation is out of scope; keep as callback in Route if needed
    }

    fun onMoreClicked() {
        // open menu / settings
    }

    fun onStartClicked() {
        // start flow: pass state.toWorkoutConfig()
    }

    fun onSetsMinus() = updateSets(state.sets - 1)
    fun onSetsPlus() = updateSets(state.sets + 1)

    fun onWorkMinus() = updateWork(state.workSec - TIME_STEP_SEC)
    fun onWorkPlus() = updateWork(state.workSec + TIME_STEP_SEC)

    fun onRestMinus() = updateRest(state.restSec - TIME_STEP_SEC)
    fun onRestPlus() = updateRest(state.restSec + TIME_STEP_SEC)

    fun onSkipLastRestChanged(value: Boolean) {
        state = state.copy(skipLastRest = value)
    }

    private fun updateSets(newValue: Int) {
        state = state.copy(sets = newValue.coerceIn(1, MAX_SETS))
    }

    private fun updateWork(newValue: Int) {
        state = state.copy(workSec = newValue.coerceIn(MIN_WORK_SEC, MAX_WORK_SEC))
    }

    private fun updateRest(newValue: Int) {
        state = state.copy(restSec = newValue.coerceIn(MIN_REST_SEC, MAX_REST_SEC))
    }

    data class UiState(
        val sets: Int,
        val workSec: Int,
        val restSec: Int,
        val skipLastRest: Boolean,
    ) {
        val totalDurationSec: Int
            get() {
                val workTotal = sets * workSec
                val restCount = if (skipLastRest) max(sets - 1, 0) else sets
                val restTotal = restCount * restSec
                return workTotal + restTotal
            }
    }

    companion object {
        private const val TIME_STEP_SEC = 5

        private const val MAX_SETS = 99

        private const val MIN_WORK_SEC = 5
        private const val MAX_WORK_SEC = 60 * 60 // 60 min

        private const val MIN_REST_SEC = 5
        private const val MAX_REST_SEC = 60 * 60 // 60 min
    }
}
