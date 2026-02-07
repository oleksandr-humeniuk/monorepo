package com.oho.hiit_timer.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.oho.hiit_timer.count_down_screen.HiitRunRoute
import com.oho.hiit_timer.tabs.HiitTabHost

@Composable
fun HiitAppNavRoot(
    viewModel: HiitRootNavViewModel = viewModel(),
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
                        openWorkout = { workoutId->
                            viewModel.openWorkout(workoutId)
                        }
                    )
                }
            }
        },
    )
}