package com.oho.hiit_timer.workouts.list

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oho.hiit_timer.data.HiitWorkoutsRepository
import com.oho.hiit_timer.domain.totalDurationWithoutPrepareSec
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WorkoutsListViewModel(
    private val repository: HiitWorkoutsRepository
) : ViewModel() {

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
        UiState(items = emptyList(), isDebugMock = false)
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observerWorkouts(HiitWorkoutsRepository.Source.User)
                .collect { workouts ->
                    _state.update {
                        it.copy(
                            items = workouts.map { workout ->
                                WorkoutListItemUi(
                                    id = workout.id,
                                    name = workout.name,
                                    blocksCount = workout.exercises.size,
                                    totalDurationSec = workout.totalDurationWithoutPrepareSec()
                                )
                            }
                        )
                    }
                }
        }
    }

    private val _events = Channel<Event>(capacity = Channel.Factory.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    fun onWorkoutClicked(workoutId: String) {
        viewModelScope.launch { _events.send(Event.OpenWorkout(workoutId)) }
    }

    fun onStartClicked(workoutId: String) {
        viewModelScope.launch { _events.send(Event.StartWorkout(workoutId)) }
    }

    fun onCreateClicked() {
        viewModelScope.launch {
            _events.send(Event.CreateWorkout)
        }
    }

    fun onMoreClicked() {
        viewModelScope.launch { _events.send(Event.More) }
    }
}