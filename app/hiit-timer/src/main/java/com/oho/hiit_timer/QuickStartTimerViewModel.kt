package com.oho.hiit_timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oho.hiit_timer.data.QuickStartRepository
import com.oho.hiit_timer.data.storage.HiitRunSessionDao
import com.oho.hiit_timer.domain.QuickStartMapper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

class QuickStartTimerViewModel(
    private val quickStartRepo: QuickStartRepository,
    private val sessionDao: HiitRunSessionDao
) : ViewModel() {

    val state: MutableStateFlow<UiState> = MutableStateFlow(
        UiState(
            sets = 8,
            workSec = 30,
            restSec = 10,
            // keep true if you want total to exclude the final rest
            skipLastRest = true,
        )
    )

    val events: MutableSharedFlow<Event> = MutableSharedFlow()

    sealed interface Event {
        data class OpenWorkout(val workoutId: String) :
            Event
    }

    private var suppressPersist = false
    private var persistJob: Job? = null

    init {
        // 1) Ensure quick_start exists in DB (idempotent)
        viewModelScope.launch {
            quickStartRepo.ensure(defaultState = state.value)
        }

        // 2) Hydrate UI from DB and keep in sync
        viewModelScope.launch {
            quickStartRepo.observe()
                .filterNotNull()
                .collect { dbState ->
                    // Prevent feedback loop: applying DB -> should not re-save immediately
                    suppressPersist = true
                    state.value = dbState.coerceToBounds()
                    suppressPersist = false
                }
        }
    }

    private fun schedulePersist() {
        if (suppressPersist) return

        persistJob?.cancel()
        persistJob = viewModelScope.launch {
            delay(250L) // debounce
            quickStartRepo.save(state.value.coerceToBounds())
        }
    }

    fun onBackClicked() {
        // navigation is out of scope; keep as callback in Route if needed
    }

    fun onMoreClicked() {
        // open menu / settings
    }

    fun onStartClicked() {
        viewModelScope.launch {
            sessionDao.clear()
            quickStartRepo.save(state.value.coerceToBounds())
            events.emit(Event.OpenWorkout(QuickStartMapper.QUICK_START_ID))
        }
    }

    fun onSetsMinus() = updateSets(state.value.sets - 1).also { schedulePersist() }
    fun onSetsPlus() = updateSets(state.value.sets + 1).also { schedulePersist() }

    fun onWorkMinus() = updateWork(state.value.workSec - TIME_STEP_SEC).also { schedulePersist() }
    fun onWorkPlus() = updateWork(state.value.workSec + TIME_STEP_SEC).also { schedulePersist() }

    fun onRestMinus() = updateRest(state.value.restSec - TIME_STEP_SEC).also { schedulePersist() }
    fun onRestPlus() = updateRest(state.value.restSec + TIME_STEP_SEC).also { schedulePersist() }

    fun onSetPillClicked() {

    }

    fun onWorkPillClicked() {

    }

    fun onRestPillClicked() {

    }

    fun onSkipLastRestChanged(value: Boolean) {
        state.update { it.copy(skipLastRest = value) }
        schedulePersist()
    }

    private fun updateSets(newValue: Int) {
        state.update { it.copy(sets = newValue.coerceIn(1, MAX_SETS)) }
    }

    private fun updateWork(newValue: Int) {
        state.update { it.copy(workSec = newValue.coerceIn(MIN_WORK_SEC, MAX_WORK_SEC)) }
    }

    private fun updateRest(newValue: Int) {
        state.update { it.copy(restSec = newValue.coerceIn(MIN_REST_SEC, MAX_REST_SEC)) }
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

        fun coerceToBounds(): UiState = copy(
            sets = sets.coerceIn(1, MAX_SETS),
            workSec = workSec.coerceIn(MIN_WORK_SEC, MAX_WORK_SEC),
            restSec = restSec.coerceIn(MIN_REST_SEC, MAX_REST_SEC),
        )
    }

    companion object Companion {
        private const val TIME_STEP_SEC = 5

        private const val MAX_SETS = 99

        private const val MIN_WORK_SEC = 5
        private const val MAX_WORK_SEC = 60 * 60 // 60 min

        private const val MIN_REST_SEC = 5
        private const val MAX_REST_SEC = 60 * 60 // 60 min
    }
}
