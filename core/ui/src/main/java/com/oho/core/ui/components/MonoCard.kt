package com.oho.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.oho.core.ui.theme.MonoTheme

@Composable
fun MonoCard(
    modifier: Modifier = Modifier,
    border: BorderStroke? = BorderStroke(1.dp, MonoTheme.colors.cardBorderColor),
    contentPadding: PaddingValues = PaddingValues(MonoTheme.dimens.cardPadding),
    backgroundColor: Color = MonoTheme.colors.cardBackground,
    shadowElevation: Dp = MonoTheme.elevation.card,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(MonoTheme.shapes.cardRadius)
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = shape,
        tonalElevation = 0.dp,
        shadowElevation = shadowElevation,
        border = border,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content,
        )
    }
}