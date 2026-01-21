package com.oho.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class MonoTypography(
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val bodyPrimary: TextStyle,
    val bodySecondary: TextStyle,
    val caption: TextStyle,
    val buttonText: TextStyle,
    val label: TextStyle,
) {
    fun toMaterial3Typography(): Typography {
        return Typography(
            headlineSmall = titleLarge,
            titleMedium = titleMedium,
            bodyLarge = bodyPrimary,
            bodyMedium = bodySecondary,
            labelLarge = buttonText,
            labelMedium = label,
        )
    }

    companion object {
        fun default(
            fontFamily: FontFamily = FontFamily.Default,
        ): MonoTypography {
            return MonoTypography(
                titleLarge = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                ),
                titleMedium = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                ),
                bodyPrimary = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                ),
                bodySecondary = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                ),
                caption = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                ),
                buttonText = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                ),
                label = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                ),
            )
        }
    }
}

internal val LocalMonoTypography = staticCompositionLocalOf { MonoTypography.default() }