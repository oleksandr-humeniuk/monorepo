package com.oho.hiit_timer.count_down_screen


import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
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

    private var service: HiitRunService? = null
    private var bound: Boolean = false

    private val conn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val b = binder as? HiitRunService.LocalBinder ?: return
            service = b.service()
            bound = true

            service?.send(HiitRunService.Cmd.Start(workoutId))

            viewModelScope.launch {
                val service = service
                while (bound && service != null) {
                    _state.value = ViewState.Loaded(service.state.value)
                    kotlinx.coroutines.delay(100L)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
            service = null
        }
    }

    init {
        startAndBind()
    }

    private fun startAndBind() {
        val i = Intent(application, HiitRunService::class.java)
        Log.d("ZXC", "start service vm")
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

    fun onPauseResume() = service?.send(HiitRunService.Cmd.PauseResume)
    fun onNext() = service?.send(HiitRunService.Cmd.Next)
    fun onPrevious() = service?.send(HiitRunService.Cmd.Previous)
    fun onClose() = {
        service?.send(HiitRunService.Cmd.Stop)
    }
}
