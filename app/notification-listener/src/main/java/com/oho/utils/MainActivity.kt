package com.oho.utils

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.oho.core.ui.components.MonoScaffold
import com.oho.core.ui.theme.MonoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonoTheme(darkTheme = false) {
                MonoScaffold(modifier = Modifier.fillMaxSize()) {
                    AppNavRoot()
                }
            }
        }
    }
}
