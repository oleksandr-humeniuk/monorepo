package com.oho.hiit_timer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.oho.core.ui.components.MonoScaffold
import com.oho.core.ui.theme.HiitMonoPalettes
import com.oho.core.ui.theme.MonoTheme
import com.oho.hiit_timer.count_down_screen.HiitRunRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HiitActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonoTheme(
                darkTheme = true,
                colors = HiitMonoPalettes.dark()
            ) {
                MonoScaffold(modifier = Modifier.fillMaxSize()) {
                    AppNavRoot()
                }
            }
        }
    }
}

@Composable
private fun AppNavRoot(
    viewModel: HiitMainViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()



    NavDisplay(
        backStack = rememberBackStack(state.backStack),
        onBack = { viewModel.onBack() },
        entryProvider = { key ->
            when (key) {
                is HiitMainViewModel.HiitRoutes.RunRoute -> NavEntry(key) {
                    HiitRunRoute(
                        workoutId = key.id,
                    )
                }

                HiitMainViewModel.HiitRoutes.QuickWorkout -> NavEntry(key) {
                    QuickStartTimerRoute(
                        openWorkout = {
                            viewModel.openWorkout(it)
                        }
                    )
                }
            }
        },
    )
}

@Composable
private fun rememberBackStack(backStack: List<HiitMainViewModel.HiitRoutes>) =
    remember(backStack) {
        mutableStateListOf<HiitMainViewModel.HiitRoutes>().apply {
            addAll(
                backStack
            )
        }
    }


class HiitMainViewModel : ViewModel() {
    private val _state = MutableStateFlow(NavState())
    val state: StateFlow<NavState> = _state.asStateFlow()


    fun onBack() {
        _state.update { s ->
            if (s.backStack.size <= 1) s else s.copy(backStack = s.backStack.dropLast(1))
        }
    }

    fun openWorkout(workoutId: String) {
        _state.update { s ->
            s.copy(backStack = s.backStack + HiitRoutes.RunRoute(workoutId))
        }
    }

    data class NavState(
        val backStack: List<HiitRoutes> = listOf(HiitRoutes.QuickWorkout),
    )

    sealed interface HiitRoutes {
        data object QuickWorkout : HiitRoutes
        data class RunRoute(val id: String) : HiitRoutes
    }
}