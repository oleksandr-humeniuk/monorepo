package com.oho.utils

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.oho.core.ui.utils.ObserveOnResume
import com.oho.utils.apps_route.AppsRoute
import com.oho.utils.permission.NotificationAccessRoute

@Composable
fun AppNavRoot(
    viewModel: MainViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.refreshGate(context) }

    ObserveOnResume {
        viewModel.refreshGate(context)
    }
    NavDisplay(
        backStack = rememberBackStack(state.backStack),
        onBack = { viewModel.onBack() },
        entryProvider = { key ->
            when (key) {
                Routes.Gate -> NavEntry(key) { GateScreen() }
                Routes.Permissions -> NavEntry(key) {
                    NotificationAccessRoute(
                        openPushNotificationListenerSettings = {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                                putExtra(":settings:show_fragment_args", android.os.Bundle().apply {
                                    putString("package", context.packageName)
                                })
                            })
                        },
                        modifier = Modifier.Companion.fillMaxSize(),
                    )
                }

                Routes.Home -> NavEntry(key) {
                    AppsRoute(
                        modifier = Modifier.Companion.fillMaxSize(),
                        openSettings = {},
                        openFilters = {},
                        openAppDetails = {},
                        onGoProClick = {}
                    )
                }
            }
        },
    )
}

@Composable
private fun rememberBackStack(backStack: List<Routes>) =
    remember(backStack) { mutableStateListOf<Routes>().apply { addAll(backStack) } }


@Composable
private fun GateScreen() { /* optional splash/blank */
}
