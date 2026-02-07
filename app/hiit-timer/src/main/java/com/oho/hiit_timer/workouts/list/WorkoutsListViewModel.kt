package com.oho.hiit_timer.workouts.list

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class WorkoutsListViewModel : ViewModel() {

    @Immutable
    data class UiState(
        val items: List<WorkoutListItemUi> = emptyList(),
        val isDebugMock: Boolean = true,
    )

    sealed interface Event {
        data class OpenWorkout(val workoutId: String) : Event
        data class StartWorkout(val workoutId: String) : Event
        data object CreateWorkout : Event
        data object More : Event
    }

    private val _state = MutableStateFlow(
        UiState(items = mockItems(), isDebugMock = true)
        // For empty state debug, switch to:
        // UiState(items = emptyList(), isDebugMock = true)
    )
    val state = _state.asStateFlow()

    private val _events = Channel<Event>(capacity = Channel.Factory.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    fun onWorkoutClicked(workoutId: String) {
        viewModelScope.launch { _events.send(Event.OpenWorkout(workoutId)) }
    }

    fun onStartClicked(workoutId: String) {
        viewModelScope.launch { _events.send(Event.StartWorkout(workoutId)) }
    }

    fun onCreateClicked() {
        viewModelScope.launch { _events.send(Event.CreateWorkout) }
    }

    fun onMoreClicked() {
        viewModelScope.launch { _events.send(Event.More) }
    }

    private companion object Companion {
        fun mockItems(): List<WorkoutListItemUi> = listOf(
            WorkoutListItemUi(
                id = "w_monday",
                name = "Monday",
                blocksCount = 2,
                totalDurationSec = 6 * 60 + 35
            ),
            WorkoutListItemUi(
                id = "w_tabata",
                name = "Tabata Classic",
                blocksCount = 8,
                totalDurationSec = 4 * 60
            ),
            WorkoutListItemUi(
                id = "w_full_body",
                name = "Full Body HIIT",
                blocksCount = 3,
                totalDurationSec = 12 * 60
            ),
            WorkoutListItemUi(
                id = "w_sprints",
                name = "Sprint Intervals",
                blocksCount = 5,
                totalDurationSec = 15 * 60 + 30
            ),
            WorkoutListItemUi(
                id = "w_core",
                name = "Core Blaster",
                blocksCount = 4,
                totalDurationSec = 8 * 60 + 45
            ),
            WorkoutListItemUi(
                id = "w_mobility",
                name = "Morning Mobility",
                blocksCount = 1,
                totalDurationSec = 10 * 60
            ),
        )
    }
}