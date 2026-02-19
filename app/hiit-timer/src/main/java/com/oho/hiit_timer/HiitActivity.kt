package com.oho.hiit_timer

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.oho.core.ui.components.MonoScaffold
import com.oho.core.ui.theme.HiitMonoPalettes
import com.oho.core.ui.theme.MonoTheme
import com.oho.hiit_timer.data.store.SettingsRepository
import com.oho.hiit_timer.root.HiitAppNavRoot
import com.oho.hiit_timer.settings.SettingsViewModel.ThemeMode
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class HiitActivity : ComponentActivity() {

    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        observeKeepScreenOn()
        setContent {
            val prefs by settingsRepository.hiitPreferences.collectAsStateWithLifecycle()
            val darkTheme = when (prefs.themeMode) {
                ThemeMode.Dark -> true
                ThemeMode.Light -> false
                ThemeMode.System -> isSystemInDarkTheme()
            }
            val colors = if (darkTheme) HiitMonoPalettes.dark() else HiitMonoPalettes.light()

            val view = LocalView.current
            LaunchedEffect(darkTheme) {
                val wic = WindowCompat.getInsetsController(window, view)
                wic.isAppearanceLightStatusBars = !darkTheme
                wic.isAppearanceLightNavigationBars = !darkTheme
            }

            MonoTheme(darkTheme = darkTheme, colors = colors) {
                MonoScaffold(modifier = Modifier.fillMaxSize()) {
                    HiitAppNavRoot()
                }
            }
        }
    }

    private fun observeKeepScreenOn() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsRepository.hiitPreferences
                    .map { it.keepScreenOn }
                    .distinctUntilChanged()
                    .collect { keepOn ->
                        if (keepOn) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
            }
        }
    }
}