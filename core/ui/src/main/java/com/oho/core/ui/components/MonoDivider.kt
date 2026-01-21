package com.oho.core.ui.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oho.core.ui.theme.MonoTheme

@Composable
fun MonoDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = MonoTheme.colors.dividerColor,
        thickness = 1.dp,
    )
}
