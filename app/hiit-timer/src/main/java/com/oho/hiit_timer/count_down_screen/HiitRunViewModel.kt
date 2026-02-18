package com.oho.hiit_timer.count_down_screen

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oho.hiit_timer.data.store.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

// Produced by HiitRunService — kept here since it's the contract between service and ViewModel.
sealed interface ViewState {
    data object Idle : ViewState
    data class Loaded(val runUiState: HiitRunUiState) : ViewState
}

class HiitRunViewModel(
    private val application: Application,
    private val workoutId: String,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    sealed interface RunViewState {
        data object Idle : RunViewState
        data class Ready(
            val runUiState: HiitRunUiState,
            val showTotalRemaining: Boolean,
            val vibrationEnabled: Boolean,
        ) : RunViewState
    }

    private val _state: MutableStateFlow<RunViewState> = MutableStateFlow(RunViewState.Idle)
    val state: StateFlow<RunViewState> = _state

    private var controller: HiitRunService.HiitRunController? = null
    private var bound: Boolean = false

    private val conn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val ctrl = binder as? HiitRunService.HiitRunController ?: return
            controller = ctrl
            bound = true
            ctrl.send(HiitRunService.Cmd.Start(workoutId))

            viewModelScope.launch {
                combine(ctrl.state, settingsRepository.hiitPreferences) { svcState, prefs ->
                    when (svcState) {
                        ViewState.Idle -> RunViewState.Idle
                        is ViewState.Loaded -> RunViewState.Ready(
                            runUiState = svcState.runUiState,
                            showTotalRemaining = prefs.showTotalRemaining,
                            vibrationEnabled = prefs.vibrationEnabled,
                        )
                    }
                }.collect { _state.value = it }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
            controller = null
        }
    }

    init {
        startAndBind()
    }

    private fun startAndBind() {
        val i = Intent(application, HiitRunService::class.java)
        ContextCompat.startForegroundService(application, i)
        application.bindService(i, conn, Context.BIND_AUTO_CREATE)
    }

    override fun onCleared() {
        if (bound) {
            application.unbindService(conn)
            bound = false
        }
        super.onCleared()
    }

    fun onPauseResume() = controller?.send(HiitRunService.Cmd.PauseResume)
    fun onNext() = controller?.send(HiitRunService.Cmd.Next)
    fun onPrevious() = controller?.send(HiitRunService.Cmd.Previous)
    fun onClose() = controller?.send(HiitRunService.Cmd.Stop)
}
