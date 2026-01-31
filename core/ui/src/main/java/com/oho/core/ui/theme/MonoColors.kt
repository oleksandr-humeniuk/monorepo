package com.oho.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class MonoColors(
    val isDarkTheme: Boolean,
    // Background
    val appBackground: Color,
    val surfaceBackground: Color,
    val cardBackground: Color,
    val modalBackground: Color,

    // Text
    val primaryTextColor: Color,
    val secondaryTextColor: Color,
    val tertiaryTextColor: Color,
    val inverseTextColor: Color,
    val linkTextColor: Color,

    // Buttons
    val primaryButtonBackground: Color,
    val primaryButtonText: Color,
    val secondaryButtonBackground: Color,
    val secondaryButtonText: Color,
    val disabledButtonBackground: Color,
    val disabledButtonText: Color,

    // Icons
    val primaryIconColor: Color,
    val secondaryIconColor: Color,
    val accentIconColor: Color,

    // Borders / Dividers / Inputs
    val dividerColor: Color,
    val cardBorderColor: Color,
    val inputBorderColor: Color,

    // Status
    val successColor: Color,
    val warningColor: Color,
    val errorColor: Color,
    val infoColor: Color,

    // Accent
    val accentPrimary: Color,
    val accentSecondary: Color,
) {
    fun toMaterial3ColorScheme(): ColorScheme {
        // Minimal, pragmatic mapping. Keep MonoColors as source of truth.
        return lightColorScheme(
            primary = accentPrimary,
            secondary = accentSecondary,
            background = appBackground,
            surface = surfaceBackground,
            onPrimary = primaryButtonText,
            onSecondary = primaryTextColor,
            onBackground = primaryTextColor,
            onSurface = primaryTextColor,
            error = errorColor,
            onError = Color.White,
            outline = dividerColor,
        )
    }

    companion object {

        fun light(): MonoColors {
            val accentBlue = Color(0xFF2F6BFF)
            val accentBlue2 = Color(0xFF5A8CFF)

            return MonoColors(
                // Background
                appBackground = Color(0xFFF8FAFC),
                surfaceBackground = Color(0xFFF1F5F9),
                cardBackground = Color(0xFFFFFFFF),
                modalBackground = Color(0xFFFFFFFF),

                // Text
                primaryTextColor = Color(0xFF0F172A),
                secondaryTextColor = Color(0xFF475569),
                tertiaryTextColor = Color(0xFF94A3B8),
                inverseTextColor = Color(0xFFFFFFFF),
                linkTextColor = accentBlue,

                // Buttons
                primaryButtonBackground = accentBlue,
                primaryButtonText = Color(0xFFFFFFFF),
                secondaryButtonBackground = Color(0xFFEFF4FF),
                secondaryButtonText = accentBlue,
                disabledButtonBackground = Color(0xFFE2E8F0),
                disabledButtonText = Color(0xFF94A3B8),

                // Icons
                primaryIconColor = Color(0xFF0F172A),
                secondaryIconColor = Color(0xFF64748B),
                accentIconColor = accentBlue,

                // Borders / Dividers / Inputs
                dividerColor = Color(0xFFE5E7EB),
                cardBorderColor = Color(0xFFE5E7EB),
                inputBorderColor = Color(0xFFD1D9E6),

                // Status
                successColor = Color(0xFF16A34A),
                warningColor = Color(0xFFF59E0B),
                errorColor = Color(0xFFDC2626),
                infoColor = Color(0xFF2563EB),

                // Accent
                accentPrimary = accentBlue,
                accentSecondary = accentBlue2,
                isDarkTheme = false
            )
        }

        fun dark(): MonoColors {
            val accentBlue = Color(0xFF5B8DFF)
            val accentBlue2 = Color(0xFF89AEFF)

            return MonoColors(
                // Background
                appBackground = Color(0xFF0B1220),
                surfaceBackground = Color(0xFF0E1628),
                cardBackground = Color(0xFF151F38),
                modalBackground = Color(0xFF121F3A),

                // Text
                primaryTextColor = Color(0xFFE5E7EB),
                secondaryTextColor = Color(0xFFC7D2E1),
                tertiaryTextColor = Color(0xFF8A9BB5),
                inverseTextColor = Color(0xFFFFFFFF),
                linkTextColor = accentBlue,

                // Buttons
                primaryButtonBackground = accentBlue,
                primaryButtonText = Color(0xFFFFFFFF),
                secondaryButtonBackground = Color(0xFF1A2740),
                secondaryButtonText = Color(0xFFE5E7EB),
                disabledButtonBackground = Color(0xFF22324D),
                disabledButtonText = Color(0xFF8A9BB5),

                // Icons
                primaryIconColor = Color(0xFFE5E7EB),
                secondaryIconColor = Color(0xFFB6C2D1),
                accentIconColor = accentBlue,

                // Borders / Dividers / Inputs
                dividerColor = Color(0xFF2B4168),
                cardBorderColor = Color(0xFF223255),
                inputBorderColor = Color(0xFF2A3B5C),

                // Status
                successColor = Color(0xFF22C55E),
                warningColor = Color(0xFFFBBF24),
                errorColor = Color(0xFFEF4444),
                infoColor = Color(0xFF60A5FA),

                // Accent
                accentPrimary = accentBlue,
                accentSecondary = accentBlue2,
                isDarkTheme = true
            )
        }
    }

}

internal val LocalMonoColors = staticCompositionLocalOf { MonoColors.light() }