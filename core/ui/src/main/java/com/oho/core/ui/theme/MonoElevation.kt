package com.oho.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class MonoElevation(
    val card: Dp,
    val modal: Dp,
    val floatingButton: Dp,
    val large: Dp
) {
    companion object {
        fun default(): MonoElevation = MonoElevation(
            card = 1.dp,
            modal = 6.dp,
            floatingButton = 8.dp,
            large = 16.dp
        )
    }
}

internal val LocalMonoElevation = staticCompositionLocalOf { MonoElevation.default() }