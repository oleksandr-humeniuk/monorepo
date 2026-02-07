package com.oho.core.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.oho.core.ui.theme.MonoTheme

@Composable
fun MonoText(
    text: String,
    modifier: Modifier = Modifier,
    style: MonoTextStyle = MonoTextStyle.BodyPrimary,
    color: androidx.compose.ui.graphics.Color? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null
) {
    val resolvedStyle = when (style) {
        MonoTextStyle.TitleLarge -> MonoTheme.typography.titleLarge
        MonoTextStyle.TitleMedium -> MonoTheme.typography.titleMedium
        MonoTextStyle.BodyPrimary -> MonoTheme.typography.bodyPrimary
        MonoTextStyle.BodySecondary -> MonoTheme.typography.bodySecondary
        MonoTextStyle.Caption -> MonoTheme.typography.caption
        MonoTextStyle.Button -> MonoTheme.typography.buttonText
        MonoTextStyle.Label -> MonoTheme.typography.label
        MonoTextStyle.DisplayLarge -> MonoTheme.typography.displayLarge
    }
    val resolvedColor = color ?: when (style) {
        MonoTextStyle.BodySecondary, MonoTextStyle.Caption, MonoTextStyle.Label -> MonoTheme.colors.secondaryTextColor
        else -> MonoTheme.colors.primaryTextColor
    }

    Text(
        text = text,
        style = resolvedStyle,
        color = resolvedColor,
        maxLines = maxLines,
        overflow = overflow,
        modifier = modifier,
        textAlign = textAlign
    )
}

enum class MonoTextStyle { TitleLarge, TitleMedium, BodyPrimary, BodySecondary, Caption, Button, Label,DisplayLarge }
