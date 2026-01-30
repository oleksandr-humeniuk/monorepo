package com.oho.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedNumberText(
    intRepresentation: Int,
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    fontFamily: FontFamily? = null,
    color: Color = Color.Unspecified,
) {
    var prevInt by remember { mutableIntStateOf(intRepresentation) }
    var prevText by remember { mutableStateOf(text) }

    val direction = when {
        intRepresentation > prevInt -> -1
        intRepresentation < prevInt -> 1
        else -> 0
    }

    LaunchedEffect(intRepresentation, text) {
        prevInt = intRepresentation
        prevText = text
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        val maxLen = maxOf(text.length, prevText.length)

        for (i in 0 until maxLen) {
            key(i) {
                val newCh = text.getOrNull(i) ?: ' '
                val oldCh = prevText.getOrNull(i) ?: ' '

                AnimatedDigit(
                    index = i,
                    newCh = newCh,
                    oldCh = oldCh,
                    direction = direction,
                    style = style,
                    fontFamily = fontFamily,
                    color = color,
                )
            }
        }
    }
}

@Composable
private fun AnimatedDigit(
    index: Int,
    newCh: Char,
    oldCh: Char,
    direction: Int,
    style: TextStyle,
    fontFamily: FontFamily?,
    color: Color,
) {
    val shouldAnimate =
        direction != 0 &&
                newCh.isDigit() && oldCh.isDigit() &&
                newCh != oldCh

    val offsetPx = with(LocalDensity.current) { 8.dp.roundToPx() }
    val slideDur = 110
    val fadeDur = 90

    AnimatedContent(
        targetState = newCh,
        transitionSpec = {
            if (!shouldAnimate) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                (slideInVertically(
                    animationSpec = tween(
                        durationMillis = slideDur,
                        easing = FastOutSlowInEasing,
                    ),
                    initialOffsetY = { direction * offsetPx }
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = fadeDur,
                        easing = LinearOutSlowInEasing,
                    )
                )).togetherWith(
                    slideOutVertically(
                        animationSpec = tween(
                            durationMillis = slideDur,
                            easing = FastOutSlowInEasing,
                        ),
                        targetOffsetY = { -direction * offsetPx }
                    ) + fadeOut(
                        animationSpec = tween(
                            durationMillis = fadeDur,
                            easing = FastOutLinearInEasing,
                        )
                    )
                ).using(SizeTransform(clip = false))
            }
        },
        label = "digit-$index",
    ) { value ->
        Text(
            text = value.toString(),
            style = style,
            fontFamily = fontFamily,
            color = color,
            softWrap = false,
            maxLines = 1,
        )
    }
}
