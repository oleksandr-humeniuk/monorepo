package com.oho.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Mono design system entry point.
 *
 * Rules:
 * - Case-based naming (primaryTextColor, primaryButtonBackground, etc.)
 * - Prefer duplication over abstract tokens.
 */
object MonoTheme {
    val colors: MonoColors
        @Composable @ReadOnlyComposable get() = LocalMonoColors.current

    val typography: MonoTypography
        @Composable @ReadOnlyComposable get() = LocalMonoTypography.current

    val shapes: MonoShapes
        @Composable @ReadOnlyComposable get() = LocalMonoShapes.current

    val dimens: MonoDimens
        @Composable @ReadOnlyComposable get() = LocalMonoDimens.current

    val elevation: MonoElevation
        @Composable @ReadOnlyComposable get() = LocalMonoElevation.current
}

@Composable
fun MonoTheme(
    darkTheme: Boolean = true,
    colors: MonoColors = if (darkTheme) MonoColors.dark() else MonoColors.light(),
    typography: MonoTypography = MonoTypography.default(),
    shapes: MonoShapes = MonoShapes.default(),
    dimens: MonoDimens = MonoDimens.default(),
    elevation: MonoElevation = MonoElevation.default(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalMonoColors provides colors,
        LocalMonoTypography provides typography,
        LocalMonoShapes provides shapes,
        LocalMonoDimens provides dimens,
        LocalMonoElevation provides elevation,
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterial3ColorScheme(),
            typography = typography.toMaterial3Typography(),
            content = content,
        )
    }
}
