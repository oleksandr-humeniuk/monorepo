package com.oho.utils

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.oho.core.ui.components.MonoScaffold
import com.oho.core.ui.theme.MonoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonoTheme {
                MonoScaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavExample()
                }
            }
        }
    }
}

sealed interface Routes {
    data object Permissions : Routes
}

@Composable
fun NavExample() {

    val backStack = remember { mutableStateListOf<Any>() }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key ->
            when (key) {
                else -> NavEntry(Unit) { Text("Unknown route") }
            }
        }
    )
}

