package com.oho.hiit_timer.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.oho.hiit_timer.count_down_screen.HiitRunRoute
import com.oho.hiit_timer.tabs.HiitTabHost
import com.oho.hiit_timer.workouts.add.CreateEditWorkoutRoute
import com.oho.hiit_timer.workouts.add_block.CreateEditIntrervalRoute
import org.koin.androidx.compose.koinViewModel

@Composable
fun HiitAppNavRoot(
    viewModel: HiitRootNavViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    NavDisplay(
        backStack = state.backStack,
        onBack = { viewModel.onBack() },
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
                        createEditWorkout = { workoutId ->
                            viewModel.createEditWorkout(workoutId)
                        }
                    )
                }

                is HiitRootRoute.CreateEditWorkout -> NavEntry(key) {
                    CreateEditWorkoutRoute(
                        workoutId = key.workoutId,
                        onBack = { viewModel.onBack() },
                        onAddBlock = {
                            viewModel.onAddBlock()
                        }
                    )
                }

                HiitRootRoute.AddBlock -> NavEntry(key) {
                    CreateEditIntrervalRoute(
                        onBack = { viewModel.onBack() }
                    )
                }
            }
        },
    )
}