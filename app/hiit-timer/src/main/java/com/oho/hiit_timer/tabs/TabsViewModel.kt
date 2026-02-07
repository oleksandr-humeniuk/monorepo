package com.oho.hiit_timer.tabs

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class Tab(
    val route: HiitTabRoute
)

class TabsViewModel : ViewModel() {
    data class State(
        val backStack: List<HiitTabRoute> = listOf(HiitTabRoute.Quick),
        val tabs: List<Tab> = listOf(
            Tab(HiitTabRoute.Quick),
            Tab(HiitTabRoute.Workouts),
            Tab(HiitTabRoute.Challenges),
            Tab(HiitTabRoute.History),
            )
    ) {
        val selected: HiitTabRoute
            get() = backStack.last()

        val selectedTab
            get() = tabs.first { it.route == selected }
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun selectTab(tab: HiitTabRoute) {
        _state.update { s ->
            if (s.selected == tab) {
                // native pattern: reselect => bring tab root to top (already root here)
                s
            } else {
                s.copy(
                    backStack = bringToTop(s.backStack, tab)
                )
            }
        }
    }

    fun onBack(): Boolean {
        val canPop = _state.value.backStack.size > 1
        if (canPop) {
            _state.update { s ->
                val newStack = s.backStack.dropLast(1)
                s.copy(backStack = newStack)
            }
        }
        return canPop
    }

    private fun bringToTop(stack: List<HiitTabRoute>, tab: HiitTabRoute): List<HiitTabRoute> {
        val filtered = stack.filterNot { it == tab }
        return filtered + tab
    }
}

