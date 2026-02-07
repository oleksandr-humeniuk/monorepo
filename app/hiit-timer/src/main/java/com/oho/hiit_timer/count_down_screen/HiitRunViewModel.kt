package com.oho.hiit_timer.count_down_screen


import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ViewState {
    data object Idle : ViewState
    data class Loaded(
        val runUiState: HiitRunUiState
    ) : ViewState
}

class HiitRunViewModel(
    private val application: Application,
    private val workoutId: String,
) : ViewModel() {

    private val _state: MutableStateFlow<ViewState> = MutableStateFlow(ViewState.Idle)
    val state: StateFlow<ViewState> = _state

    private var controller: HiitRunService.HiitRunController? = null
    private var bound: Boolean = false

    private val conn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            controller = binder as? HiitRunService.HiitRunController
            bound = true

            controller?.send(HiitRunService.Cmd.Start(workoutId))

            viewModelScope.launch {
                val service = controller
                while (bound && service != null) {
                    _state.value = service.state.value
                    kotlinx.coroutines.delay(100L)
                }
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
        // Foreground start
        androidx.core.content.ContextCompat.startForegroundService(application, i)
        // Bind
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
    fun onClose() {
        controller?.send(HiitRunService.Cmd.Stop)
    }
}
