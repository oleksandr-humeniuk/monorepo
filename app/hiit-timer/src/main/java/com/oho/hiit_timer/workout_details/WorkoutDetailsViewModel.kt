package com.oho.hiit_timer.workout_details

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oho.hiit_timer.data.HiitWorkoutsRepository
import com.oho.hiit_timer.domain.totalDurationWithoutPrepareSec
import com.oho.hiit_timer.workouts.add.WorkoutBlockSpec
import com.oho.hiit_timer.workouts.add.WorkoutBlockUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class WorkoutDetailsViewModel(
    private val workoutId: String,
    private val repository: HiitWorkoutsRepository
) : ViewModel() {

    @Immutable
    data class UiState(
        val title: String = "",
        val blocks: List<WorkoutBlockUi> = emptyList(),
        val totalDurationSec: Int = 0,
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeWorkout(workoutId)
                .filterNotNull()
                .collect { workout ->
                    _state.value = UiState(
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
