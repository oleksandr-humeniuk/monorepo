package com.oho.hiit_timer.workouts.add_block

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oho.hiit_timer.data.HiitWorkoutsRepository
import com.oho.hiit_timer.data.TempWorkoutRepository
import com.oho.hiit_timer.domain.HiitExercise
import com.oho.hiit_timer.domain.RestAfterLastWorkPolicy
import com.oho.hiit_timer.domain.totalDurationSec
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.max

/**
 * ViewModel for Create/Edit interval block.
 *
 * Notes:
 * - "Last rest" is shown only when sets > 1.
 * - By default lastRestSec tracks restSec until user customizes it.
 * - When sets drops to 1, last rest resets to restSec and becomes not-custom.
 */
class CreateEditIntervalViewModel(
    private val params: Params,
    private val tempWorkoutRepository: TempWorkoutRepository,
    private val hiitWorkoutsRepository: HiitWorkoutsRepository,
    private val context: Context
) : ViewModel() {

    data class Params(
        val exerciseId: String?
    )

    // ---- public API

    data class UiState(
        val name: String = "",
        val sets: Int = 1,
        val workSec: Int = 30,
        val restSec: Int = 10,
        val lastRestSec: Int = 10,
        val isLastRestVisible: Boolean = false,
        val totalDurationSec: Int = 40, // computed
    ) {
        val saveEnabled: Boolean
            get() = restSec != 0 || workSec != 0
    }

    sealed interface Event {
        data object Back : Event
        data object More : Event
        data object Saved : Event

        data class OpenPicker(val target: PillTarget) : Event
    }

    enum class PillTarget { Sets, Work, Rest, LastRest }

    private val _state = MutableStateFlow(UiState())
    private val _events = Channel<Event>(capacity = Channel.Factory.BUFFERED)

    val state: StateFlow<UiState> = _state.asStateFlow()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            params.exerciseId?.let { exerciseId ->
                val fetched = hiitWorkoutsRepository.getExercise(exerciseId)
                fetched?.let {

                    _state.value = UiState(
                        name = fetched.name,
                        sets = fetched.sets,
                        workSec = fetched.workSec,
                        restSec = fetched.restSec,
                        lastRestSec = when (val policy = fetched.restAfterLastWork) {
                            is RestAfterLastWorkPolicy.Custom -> policy.seconds
                            RestAfterLastWorkPolicy.None -> 0
                            RestAfterLastWorkPolicy.SameAsRegular -> fetched.restSec
                        },
                        isLastRestVisible = fetched.sets > 1,
                        totalDurationSec = fetched.totalDurationSec()
                    )
                }
            }
        }
    }

    fun onBackClicked() {
        emit(Event.Back)
    }

    fun onMoreClicked() {
        emit(Event.More)
    }

    fun onNameChanged(text: String) {
        _state.update { it.copy(name = text.take(MAX_NAME_LEN)) }
    }

    fun onSetsMinus() = updateSets(_state.value.sets - 1)
    fun onSetsPlus() = updateSets(_state.value.sets + 1)

    fun onWorkMinus() = updateWork(_state.value.workSec - STEP_SEC)
    fun onWorkPlus() = updateWork(_state.value.workSec + STEP_SEC)

    fun onRestMinus() = updateRest(_state.value.restSec - STEP_SEC)
    fun onRestPlus() = updateRest(_state.value.restSec + STEP_SEC)

    fun onLastRestMinus() = updateLastRest(_state.value.lastRestSec - STEP_SEC)
    fun onLastRestPlus() = updateLastRest(_state.value.lastRestSec + STEP_SEC)

    fun onPillClicked(target: PillTarget) {
        // Hook for your picker/bottomsheet later. For now — just emit.
        emit(Event.OpenPicker(target))
    }

    fun onSaveClicked() {
        val s = _state.value
        val nameTrim = s.name.trim().takeIf { it.isNotEmpty() }


        viewModelScope.launch {
            tempWorkoutRepository.upsertExercise(
                HiitExercise(
                    id = params.exerciseId ?: UUID.randomUUID().toString(),
                    name = nameTrim ?: context.getString(com.oho.utils.R.string.work),
                    sets = s.sets,
                    workSec = s.workSec,
                    restSec = s.restSec,
                    restAfterLastWork = if (s.sets > 1) RestAfterLastWorkPolicy.Custom(s.lastRestSec) else RestAfterLastWorkPolicy.None,
                )
            )
            emit(Event.Saved)
        }
    }

    // ---- internal

    /**
     * Tracks whether user has explicitly customized last rest.
     * - If false: lastRestSec follows restSec.
     */
    private var lastRestIsCustom: Boolean = false

    private fun updateSets(raw: Int) {
        val newSets = raw.coerceAtLeast(MIN_SETS)

        _state.update { prev ->
            val wasVisible = prev.sets > 1
            val willBeVisible = newSets > 1

            // When entering last-rest mode, default it to restSec unless user already had a custom.
            // When leaving (sets == 1), reset customization.
            val next = if (!wasVisible && willBeVisible) {
                val nextLast = if (lastRestIsCustom) prev.lastRestSec else prev.restSec
                prev.copy(sets = newSets, isLastRestVisible = true, lastRestSec = nextLast)
            } else if (wasVisible && !willBeVisible) {
                lastRestIsCustom = false
                prev.copy(sets = newSets, isLastRestVisible = false, lastRestSec = prev.restSec)
            } else {
                prev.copy(sets = newSets, isLastRestVisible = willBeVisible)
            }

            compute(next, lastRestIsCustom)
        }
    }

    private fun updateWork(raw: Int) {
        val v = raw.coerceAtLeast(MIN_SEC)
        _state.update { prev -> compute(prev.copy(workSec = v), lastRestIsCustom) }
    }

    private fun updateRest(raw: Int) {
        val v = raw.coerceAtLeast(MIN_SEC)
        _state.update { prev ->
            val next = if (!lastRestIsCustom) {
                prev.copy(restSec = v, lastRestSec = v)
            } else {
                prev.copy(restSec = v)
            }
            compute(next, lastRestIsCustom)
        }
    }

    private fun updateLastRest(raw: Int) {
        val v = raw.coerceAtLeast(MIN_SEC)
        lastRestIsCustom = true
        _state.update { prev -> compute(prev.copy(lastRestSec = v), lastRestIsCustom) }
    }

    private fun compute(raw: UiState, lastRestIsCustom: Boolean): UiState {
        val sets = raw.sets.coerceAtLeast(MIN_SETS)
        val work = raw.workSec.coerceAtLeast(MIN_SEC)
        val rest = raw.restSec.coerceAtLeast(MIN_SEC)

        val isLastRestVisible = sets > 1
        val lastRest = if (isLastRestVisible) {
            raw.lastRestSec.coerceAtLeast(MIN_SEC)
        } else {
            // hidden mode: keep it synced to rest for consistency
            rest
        }

        // Total for block:
        // sets * work + (sets - 1) * rest + lastRest (only when sets > 1)
        val total = if (sets <= 1) {
            work // single set => just work
        } else {
            (sets * work) + ((sets - 1) * rest) + lastRest
        }

        return raw.copy(
            sets = sets,
            workSec = work,
            restSec = rest,
            lastRestSec = lastRest,
            isLastRestVisible = isLastRestVisible,
            totalDurationSec = max(0, total),
        )
    }

    private fun emit(e: Event) {
        viewModelScope.launch { _events.send(e) }
    }

    private companion object {
        const val MIN_SETS = 1
        const val MIN_SEC = 0
        const val STEP_SEC = 5
        const val MAX_NAME_LEN = 32
    }
}