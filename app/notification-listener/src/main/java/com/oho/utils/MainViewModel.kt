package com.oho.utils

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(NavState())
    val state: StateFlow<NavState> = _state.asStateFlow()

    fun refreshGate(context: Context) {
        val enabled = isNotificationListenerEnabled(context)
        _state.update {
            it.copy(
                backStack = listOf(
                    if (enabled) Routes.Home else Routes.Permissions
                )
            )
        }
    }

    fun onBack() {
        _state.update { s ->
            if (s.backStack.size <= 1) s else s.copy(backStack = s.backStack.dropLast(1))
        }
    }

    private fun isNotificationListenerEnabled(context: Context): Boolean {
        val pkg = context.packageName
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ) ?: return false
        return enabled.split(':').any { it.startsWith(pkg) }
    }

    data class NavState(
        val backStack: List<Routes> = listOf(Routes.Gate),
    )

}