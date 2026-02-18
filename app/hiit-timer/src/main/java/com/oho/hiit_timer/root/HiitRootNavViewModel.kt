package com.oho.hiit_timer.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oho.hiit_timer.data.HiitWorkoutsRepository
import com.oho.hiit_timer.data.TempWorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HiitRootNavViewModel(
    private val tempWorkoutRepository: TempWorkoutRepository
) : ViewModel() {
    private val _state = MutableStateFlow(NavState())
    val state: StateFlow<NavState> = _state.asStateFlow()


    fun onBack() {
        _state.update { s ->
            if (s.backStack.size <= 1) s else s.copy(backStack = s.backStack.dropLast(1))
        }
    }

    fun requestRunWorkout(workoutId: String) {
        _state.update { s -> s.copy(pendingRunWorkoutId = workoutId) }
    }

    fun proceedWithPendingRun() {
        val id = _state.value.pendingRunWorkoutId ?: return
        _state.update { s ->
            s.copy(
                pendingRunWorkoutId = null,
                backStack = s.backStack + HiitRootRoute.Run(id),
            )
        }
    }

    fun cancelPendingRun() {
        _state.update { s -> s.copy(pendingRunWorkoutId = null) }
    }

    private fun runWorkout(workoutId: String) {
        _state.update { s ->
            s.copy(backStack = s.backStack + HiitRootRoute.Run(workoutId))
        }
    }

    fun createWorkout() {
        viewModelScope.launch {
            tempWorkoutRepository.ensureExist()
            _state.update { s ->
                s.copy(
                    backStack = s.backStack + HiitRootRoute.CreateEditWorkout(
                        workoutId = HiitWorkoutsRepository.TEMP_WORKOUT_ID
                    )
                )
            }
        }
    }

    fun onAddBlock() {
        _state.update { s ->
            s.copy(backStack = s.backStack + HiitRootRoute.AddEditExercise())
        }
    }

    fun onEditExercise(exerciseId: String) {
        _state.update { s ->
            s.copy(backStack = s.backStack + HiitRootRoute.AddEditExercise(exerciseId))
        }
    }

    fun openWorkoutDetails(workoutId: String) {
        _state.update { s ->
            s.copy(
                backStack = s.backStack + HiitRootRoute.WorkoutDetails(workoutId)
            )
        }
    }

    fun onEditWorkout(workoutId: String) {
        viewModelScope.launch {
            tempWorkoutRepository.duplicateToTemp(workoutId)
            _state.update { s ->
                s.copy(
                    backStack = s.backStack + HiitRootRoute.CreateEditWorkout(
                        workoutId = workoutId
                    )
                )
            }
        }
    }

    fun openDuplicatedWorkout() {
        _state.update { s ->
            val trimmed = s.backStack.dropLast(1)
            s.copy(
                backStack = trimmed + HiitRootRoute.CreateEditWorkout(
                    workoutId = HiitWorkoutsRepository.TEMP_WORKOUT_ID
                )
            )
        }
    }

    fun openSoundSettings() {
        _state.update { s ->
            s.copy(
                backStack = s.backStack + HiitRootRoute.SoundSettings
            )
        }
    }

    data class NavState(
        val backStack: List<HiitRootRoute> = listOf(HiitRootRoute.Tabs),
        val pendingRunWorkoutId: String? = null,
    )

}