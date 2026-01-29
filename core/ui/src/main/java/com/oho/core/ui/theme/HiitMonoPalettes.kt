package com.oho.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
object HiitMonoPalettes {

    /**
     * HIIT — Dark (premium, clean, high-contrast)
     * Based on the mockups: page/surface/outline/text/primary + aggressive state colors.
     */
    fun dark(): MonoColors {
        // Core neutrals (match mockup)
        val page = Color(0xFF0F1216)
        val surface = Color(0xFF171B21)
        val surfaceHighlight = Color(0xFF1D222A)
        val outline = Color(0xFF232834)
        val outlineControl = Color(0xFF2B313D)

        // Text
        val textMain = Color(0xFFE7EBF0)
        val textSecondary = Color(0xFFA9B1BC)
        val textMuted = Color(0xFF7C8592)

        // Accent (config screen CTA)
        val primaryBlue = Color(0xFF4C6FFF)
        val primaryBlue2 = Color(0xFF6E8BFF)

        // State colors (aggressive, matched intensity)
        // WORK: your direction was around #DB253B, but this mockup-style red reads cleaner on dark.
        val workRed = Color(0xFFDB253B)
        // REST: same “energy” as red; bright but not neon.
        val restGreen = Color(0xFF1FAE6A)

        return MonoColors(
            // Background
            appBackground = page,
            surfaceBackground = page,          // page-level surfaces (screens)
            cardBackground = surface,          // cards/containers
            modalBackground = surfaceHighlight,

            // Text
            primaryTextColor = textMain,
            secondaryTextColor = textSecondary,
            tertiaryTextColor = textMuted,
            inverseTextColor = Color(0xFFFFFFFF),
            linkTextColor = primaryBlue,

            // Buttons
            primaryButtonBackground = primaryBlue,
            primaryButtonText = Color(0xFFFFFFFF),
            secondaryButtonBackground = surfaceHighlight,
            secondaryButtonText = textMain,
            disabledButtonBackground = Color(0xFF5E6673),
            disabledButtonText = Color(0xFF2B313D),

            // Icons
            primaryIconColor = textMain,
            secondaryIconColor = textSecondary,
            accentIconColor = primaryBlue,

            // Borders / Dividers / Inputs
            dividerColor = outline,
            cardBorderColor = outline,
            inputBorderColor = outlineControl,

            // Status (repurposed for HIIT state)
            successColor = restGreen,          // REST state
            warningColor = Color(0xFFF59E0B),
            errorColor = workRed,              // WORK state
            infoColor = primaryBlue2,

            // Accent
            accentPrimary = primaryBlue,
            accentSecondary = primaryBlue2,
        )
    }

    /**
     * HIIT — Light (clean, “Material-ish”, not washed out)
     * Keeps the same brand blue and comparable contrast.
     */
    fun light(): MonoColors {
        // Neutrals (clean + slightly cool)
        val page = Color(0xFFF6F7FB)
        val surface = Color(0xFFFFFFFF)
        val surfaceAlt = Color(0xFFF1F4F8)
        val outline = Color(0xFFE3E7EE)
        val inputOutline = Color(0xFFD2D8E2)

        // Text
        val textMain = Color(0xFF0E1116)
        val textSecondary = Color(0xFF5B6472)
        val textMuted = Color(0xFF8A94A4)

        // Accent
        val primaryBlue = Color(0xFF4C6FFF)
        val primaryBlue2 = Color(0xFF6E8BFF)

        // State colors (same as dark for consistency)
        val workRed = Color(0xFFDB253B)
        val restGreen = Color(0xFF1FAE6A)

        return MonoColors(
            // Background
            appBackground = page,
            surfaceBackground = surfaceAlt,
            cardBackground = surface,
            modalBackground = surface,

            // Text
            primaryTextColor = textMain,
            secondaryTextColor = textSecondary,
            tertiaryTextColor = textMuted,
            inverseTextColor = Color(0xFFFFFFFF),
            linkTextColor = primaryBlue,

            // Buttons
            primaryButtonBackground = primaryBlue,
            primaryButtonText = Color(0xFFFFFFFF),
            secondaryButtonBackground = Color(0xFFE9EEFF),
            secondaryButtonText = primaryBlue,
            disabledButtonBackground = Color(0xFFE2E6EE),
            disabledButtonText = Color(0xFF9AA3AF),

            // Icons
            primaryIconColor = textMain,
            secondaryIconColor = textSecondary,
            accentIconColor = primaryBlue,

            // Borders / Dividers / Inputs
            dividerColor = outline,
            cardBorderColor = outline,
            inputBorderColor = inputOutline,

            // Status (repurposed for HIIT state)
            successColor = restGreen,
            warningColor = Color(0xFFF59E0B),
            errorColor = workRed,
            infoColor = primaryBlue2,

            // Accent
            accentPrimary = primaryBlue,
            accentSecondary = primaryBlue2,
        )
    }
}
