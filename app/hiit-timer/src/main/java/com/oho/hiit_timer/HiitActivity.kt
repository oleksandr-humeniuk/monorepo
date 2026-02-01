package com.oho.hiit_timer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.oho.core.ui.components.MonoScaffold
import com.oho.core.ui.theme.HiitMonoPalettes
import com.oho.core.ui.theme.MonoTheme

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
fun AppNavRoot() {
    IntervalTimerConfigRoute()
}