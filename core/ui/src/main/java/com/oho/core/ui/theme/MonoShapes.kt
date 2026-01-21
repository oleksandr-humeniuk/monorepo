package com.oho.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class MonoShapes(
    val cardRadius: Dp,
    val buttonRadius: Dp,
    val sheetRadius: Dp,
    val chipRadius: Dp,
) {
    companion object {
        fun default(): MonoShapes = MonoShapes(
            cardRadius = 18.dp,
            buttonRadius = 16.dp,
            sheetRadius = 22.dp,
            chipRadius = 999.dp,
        )
    }
}

internal val LocalMonoShapes = staticCompositionLocalOf { MonoShapes.default() }