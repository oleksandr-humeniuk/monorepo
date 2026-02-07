package com.oho.hiit_timer.root

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HiitRootNavViewModel : ViewModel() {
    private val _state = MutableStateFlow(NavState())
    val state: StateFlow<NavState> = _state.asStateFlow()


    fun onBack() {
        _state.update { s ->
            if (s.backStack.size <= 1) s else s.copy(backStack = s.backStack.dropLast(1))
        }
    }

    fun openWorkout(workoutId: String) {
        _state.update { s ->
            s.copy(backStack = s.backStack + HiitRootRoute.Run(workoutId))
        }
    }

    data class NavState(
        val backStack: List<HiitRootRoute> = listOf(HiitRootRoute.Tabs),
    )

}