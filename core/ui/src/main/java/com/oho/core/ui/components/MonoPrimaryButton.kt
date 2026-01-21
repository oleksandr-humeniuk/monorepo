package com.oho.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.oho.core.ui.theme.MonoTheme

@Composable
fun MonoPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: Painter? = null,
) {
    val shape = RoundedCornerShape(MonoTheme.shapes.buttonRadius)
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 50.dp),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MonoTheme.colors.primaryButtonBackground,
            contentColor = MonoTheme.colors.primaryButtonText,
            disabledContainerColor = MonoTheme.colors.disabledButtonBackground,
            disabledContentColor = MonoTheme.colors.disabledButtonText,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
    ) {
        if (leadingIcon != null) {
            MonoIcon(
                painter = leadingIcon,
                contentDescription = null,
                tint = MonoTheme.colors.primaryButtonText,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = text,
            style = MonoTheme.typography.buttonText,
        )
    }
}

@Composable
fun MonoSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: Painter? = null,
) {
    val shape = RoundedCornerShape(MonoTheme.shapes.buttonRadius)
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 50.dp),
        enabled = enabled,
        shape = shape,
        border = BorderStroke(1.dp, MonoTheme.colors.inputBorderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MonoTheme.colors.secondaryButtonBackground,
            contentColor = MonoTheme.colors.secondaryButtonText,
            disabledContainerColor = MonoTheme.colors.disabledButtonBackground,
            disabledContentColor = MonoTheme.colors.disabledButtonText,
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
    ) {
        if (leadingIcon != null) {
            MonoIcon(
                painter = leadingIcon,
                contentDescription = null,
                tint = MonoTheme.colors.secondaryButtonText,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = text,
            style = MonoTheme.typography.buttonText,
        )
    }
}