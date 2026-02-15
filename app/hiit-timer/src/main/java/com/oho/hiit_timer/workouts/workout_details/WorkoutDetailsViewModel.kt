package com.oho.hiit_timer.workouts.workout_details

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oho.hiit_timer.data.HiitWorkoutsRepository
import com.oho.hiit_timer.domain.HiitWorkout
import com.oho.hiit_timer.domain.totalDurationWithoutPrepareSec
import com.oho.hiit_timer.workouts.add.WorkoutBlockSpec
import com.oho.hiit_timer.workouts.add.WorkoutBlockUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class WorkoutDetailsViewModel(
    private val workoutId: String,
    private val repository: HiitWorkoutsRepository
) : ViewModel() {

    private val _events = Channel<Event>(capacity = Channel.Factory.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    sealed interface Event {
        data object Back : Event

        data class Edit(val workoutId: String) : Event
    }

    fun onMoreClicked() {
        _state.update { s ->
            s.copy(showMoreSheet = true)
        }
    }

    private fun hideSheet() {
        _state.update { s ->
            s.copy(showMoreSheet = false)
        }
    }

    fun onMoreDismissed() {
        hideSheet()
    }

    fun onEdit() {
        hideSheet()
        viewModelScope.launch {
            _events.send(Event.Edit(workoutId))
        }
    }

    fun onDuplicate() {
        hideSheet()
        viewModelScope.launch {
            repository.duplicateWorkout(
                workoutId = workoutId,
                newId = UUID.randomUUID().toString(),
                source = HiitWorkoutsRepository.SOURCE_USER
            )
            _events.send(Event.Back)
        }
    }

    fun onDelete() {
        hideSheet()
        viewModelScope.launch {
            repository.deleteWorkout(workoutId)
            _events.send(Event.Back)
        }

    }

    @Immutable
    data class UiState(
        val title: String = "",
        val blocks: List<WorkoutBlockUi> = emptyList(),
        val totalDurationSec: Int = 0,
        val showMoreSheet: Boolean = false,
        val domainWorkout: HiitWorkout? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeWorkout(workoutId)
                .filterNotNull()
                .collect { workout ->
                    _state.value = UiState(
                        domainWorkout = workout,
                        title = workout.name,
                        totalDurationSec = workout.totalDurationWithoutPrepareSec(),
                        blocks = workout.exercises.map {
                            WorkoutBlockUi(
                                id = it.id,
                                name = it.name,
                                spec = WorkoutBlockSpec.fromExercise(it)
                            )
                        }
                    )
                }
        }
    }
}
