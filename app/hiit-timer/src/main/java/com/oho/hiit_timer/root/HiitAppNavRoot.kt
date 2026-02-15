package com.oho.hiit_timer.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.oho.hiit_timer.count_down_screen.HiitRunRoute
import com.oho.hiit_timer.tabs.HiitTabHost
import com.oho.hiit_timer.workouts.add.CreateEditWorkoutRoute
import com.oho.hiit_timer.workouts.add_block.CreateEditIntrervalRoute
import com.oho.hiit_timer.workouts.workout_details.WorkoutDetailsRoute
import org.koin.androidx.compose.koinViewModel

@Composable
fun HiitAppNavRoot(
    viewModel: HiitRootNavViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    NavDisplay(
        backStack = state.backStack,
        onBack = { viewModel.onBack() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = { key ->
            when (key) {
                is HiitRootRoute.Run -> NavEntry(key) {
                    HiitRunRoute(
                        workoutId = key.workoutId,
                    )
                }

                HiitRootRoute.Tabs -> NavEntry(key) {
                    HiitTabHost(
                        runWorkout = { workoutId ->
                            viewModel.runWorkout(workoutId)
                        },
                        createWorkout = { workoutId ->
                            viewModel.createWorkout()
                        },
                        openDetails = { workoutId ->
                            viewModel.openWorkoutDetails(workoutId)
                        }
                    )
                }

                is HiitRootRoute.CreateEditWorkout -> NavEntry(key) {
                    CreateEditWorkoutRoute(
                        workoutId = key.workoutId,
                        onBack = { viewModel.onBack() },
                        onAddBlock = {
                            viewModel.onAddBlock()
                        },
                        onEditExercise = { exerciseId ->
                            viewModel.onEditExercise(exerciseId = exerciseId)
                        }
                    )
                }

                is HiitRootRoute.AddEditExercise -> NavEntry(key) {
                    CreateEditIntrervalRoute(
                        exerciseId = key.id,
                        onBack = { viewModel.onBack() },
                        onSaved = { viewModel.onBack() }
                    )
                }

                is HiitRootRoute.WorkoutDetails -> NavEntry(key) {
                    WorkoutDetailsRoute(
                        workoutId = key.id,
                        onBack = {
                            viewModel.onBack()
                        },
                        onStartWorkout = {
                            viewModel.runWorkout(workoutId = it)
                        },
                        onEditWorkout = {
                            viewModel.onEditWorkout(workoutId = it)
                        }
                    )
                }
            }
        },
    )
}