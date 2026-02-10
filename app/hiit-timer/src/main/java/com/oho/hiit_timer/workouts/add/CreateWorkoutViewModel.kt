package com.oho.hiit_timer.workouts.add

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

class CreateWorkoutViewModel : ViewModel() {

    @Immutable
    data class UiState(
        val workoutId: String = "w_mock",
        val title: String = "Create workout",
        val blocks: List<WorkoutBlockUi> = emptyList(),
        val totalDurationSec: Int = 0,
        val isDebugMock: Boolean = false,
    )

    sealed interface Event {
        data object Back : Event
        data class Start(val workoutId: String) : Event
        data class OpenBlockMenu(val blockId: String) : Event
        data object OpenScreenMenu : Event
    }

    private val _state = MutableStateFlow(mockState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _events = Channel<Event>(capacity = Channel.Factory.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    fun onBackClicked() {
        viewModelScope.launch { _events.send(Event.Back) }
    }

    fun onMoreClicked() {
        viewModelScope.launch { _events.send(Event.OpenScreenMenu) }
    }

    fun onBlockMoreClicked(blockId: String) {
        viewModelScope.launch { _events.send(Event.OpenBlockMenu(blockId)) }
    }

    fun onAddBlockClicked() {
        // mocked in-memory mutation: append a new interval-like block
        _state.update { cur ->
            val idx = cur.blocks.size + 1
            val newBlock = WorkoutBlockUi(
                id = "b_added_$idx",
                name = "New block $idx",
                spec = WorkoutBlockSpec.Interval(sets = 6, workSec = 40, restSec = 20),
            )
            val blocks = cur.blocks + newBlock
            cur.copy(
                blocks = blocks,
                totalDurationSec = blocks.sumOf { it.totalDurationSec }
            )
        }
    }

    fun onStartClicked() {
        val id = _state.value.workoutId
        viewModelScope.launch { _events.send(Event.Start(id)) }
    }

    fun onReorderBlocks(fromIndex: Int, toIndex: Int) {
        _state.update {
            it.copy(
                blocks = it.blocks.toMutableList().apply {
                    add(toIndex, removeAt(fromIndex))
                }
            )
        }
    }

    private companion object {
        fun mockState(): UiState {
            val blocks = listOf(
                // "Warm Up" is not a new phase. It’s just a named single-duration block preset.
                WorkoutBlockUi(
                    id = "b_warmup",
                    name = "Warm Up",
                    spec = WorkoutBlockSpec.Single(sets = 1, durationSec = 5 * 60),
                ),
                WorkoutBlockUi(
                    id = "b_interval",
                    name = "Interval block",
                    spec = WorkoutBlockSpec.Interval(sets = 7, workSec = 30, restSec = 10),
                ),
                WorkoutBlockUi(
                    id = "b_core",
                    name = "Core Strength",
                    spec = WorkoutBlockSpec.Interval(sets = 4, workSec = 90, restSec = 30),
                ),
                WorkoutBlockUi(
                    id = "b_cooldown",
                    name = "Cool Down",
                    spec = WorkoutBlockSpec.Single(sets = 1, durationSec = 5 * 60),
                ),
            )
            return UiState(
                workoutId = "w_mock_builder",
                title = "Create workout",
                blocks = blocks,
                totalDurationSec = blocks.sumOf { it.totalDurationSec },
                isDebugMock = true,
            )
        }
    }
}