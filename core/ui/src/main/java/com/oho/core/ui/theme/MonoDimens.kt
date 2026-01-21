package com.oho.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class MonoDimens(
    val screenPadding: Dp,
    val cardPadding: Dp,
    val listItemPadding: Dp,
    val sectionSpacing: Dp,
    val iconSizeSmall: Dp,
    val iconSizeMedium: Dp,
) {
    companion object {
        fun default(): MonoDimens = MonoDimens(
            screenPadding = 16.dp,
            cardPadding = 14.dp,
            listItemPadding = 14.dp,
            sectionSpacing = 12.dp,
            iconSizeSmall = 16.dp,
            iconSizeMedium = 24.dp,
        )
    }
}

internal val LocalMonoDimens = staticCompositionLocalOf { MonoDimens.default() }