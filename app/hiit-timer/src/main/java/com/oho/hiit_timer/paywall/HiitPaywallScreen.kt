package com.oho.hiit_timer.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oho.core.ui.R
import com.oho.core.ui.components.MonoCard
import com.oho.core.ui.components.MonoDivider
import com.oho.core.ui.components.MonoIcon
import com.oho.core.ui.components.MonoPrimaryButton
import com.oho.core.ui.components.MonoText
import com.oho.core.ui.components.MonoTextStyle
import com.oho.core.ui.theme.MonoTheme

@Composable
fun HiitPaywallScreen(
    state: HiitPaywallUiState,
    onClose: () -> Unit,
    onSelectPlan: (PaywallPlan) -> Unit,
    onCta: () -> Unit,
    onRestore: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    val c = MonoTheme.colors

    val scroll = rememberScrollState()

    val topGlow = remember(c.isDarkTheme, c.errorColor, c.successColor) {
        // Subtle HIIT energy glow: red + green, very low alpha.
        val red = c.errorColor.copy(alpha = if (c.isDarkTheme) 0.14f else 0.08f)
        val green = c.successColor.copy(alpha = if (c.isDarkTheme) 0.10f else 0.06f)

        Brush.verticalGradient(
            colors = listOf(red, green, Color.Transparent),
            startY = 0f,
            endY = 900f,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.appBackground)
    ) {
        // Top glow background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(topGlow)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            PaywallTopBar(
                onClose = onClose,
                onRestore = onRestore,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(14.dp))

                MonoText(
                    text = "Unlock full\nworkout control",
                    style = MonoTextStyle.TitleLarge,
                    color = c.primaryTextColor,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(10.dp))

                MonoText(
                    text = "Build advanced workouts and\ncustomize your training",
                    style = MonoTextStyle.BodySecondary,
                    color = c.secondaryTextColor,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(22.dp))

                BenefitsBlock()

                Spacer(Modifier.height(22.dp))

                PlansBlock(
                    state = state,
                    onSelectPlan = onSelectPlan,
                )

                Spacer(Modifier.height(16.dp))

                MonoPrimaryButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    text = if (state.isBusy) "Processing..." else "Unlock Pro",
                    onClick = onCta,
                    enabled = !state.isBusy && state.phase != PaywallUiPhase.Loading && state.products.isNotEmpty(),
                )

                Spacer(Modifier.height(10.dp))

                if (state.errorMessage != null) {
                    MonoText(
                        text = state.errorMessage,
                        style = MonoTextStyle.BodySecondary,
                        color = c.errorColor,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(10.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MonoText(
                        text = "Cancel anytime. Google Play subscription.",
                        style = MonoTextStyle.Label,
                        color = c.tertiaryTextColor,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    MonoText(
                        text = "Privacy",
                        style = MonoTextStyle.Label,
                        color = c.linkTextColor,
                        modifier = Modifier.clickable { onOpenPrivacy() }
                    )
                    Spacer(Modifier.width(14.dp))
                    MonoText(
                        text = "Terms",
                        style = MonoTextStyle.Label,
                        color = c.linkTextColor,
                        modifier = Modifier.clickable { onOpenTerms() }
                    )
                }

                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun PaywallTopBar(
    onClose: () -> Unit,
    onRestore: () -> Unit,
) {
    val c = MonoTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
//        MonoIcon(
//            imageVector = Icons.Filled.Close,
//            contentDescription = "Close",
//            tint = c.secondaryIconColor,
//            modifier = Modifier
//                .size(28.dp)
//                .clip(CircleShape)
//                .clickable { onClose() }
//                .padding(4.dp)
//        )

        Spacer(Modifier.weight(1f))

        MonoText(
            text = "RESTORE",
            style = MonoTextStyle.Label,
            color = c.secondaryTextColor,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { onRestore() }
                .padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun BenefitsBlock() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        BenefitRow(
//            icon = Icons.Filled.Layers,
            icon = R.drawable.ic_fitness_center,
            title = "Complex Sets",
            subtitle = "Create nested intervals & loops",
        )
        BenefitRow(
//            icon = Icons.Filled.FitnessCenter,
            icon = R.drawable.ic_fitness_center,
            title = "Multiple Exercises",
            subtitle = "Assign specific moves to timers",
        )
        BenefitRow(
//            icon = Icons.Filled.VolumeUp,
            icon = R.drawable.ic_fitness_center,
            title = "Custom Sounds",
            subtitle = "TTS & custom beeps",
        )
    }
}

@Composable
private fun BenefitRow(
    icon: Int,
    title: String,
    subtitle: String,
) {
    val c = MonoTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(c.secondaryButtonBackground)
                .border(1.dp, c.cardBorderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            MonoIcon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = c.primaryIconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            MonoText(
                text = title,
                style = MonoTextStyle.TitleMedium,
                color = c.primaryTextColor,
            )
            Spacer(Modifier.height(2.dp))
            MonoText(
                text = subtitle,
                style = MonoTextStyle.BodySecondary,
                color = c.secondaryTextColor,
            )
        }
    }
}

@Composable
private fun PlansBlock(
    state: HiitPaywallUiState,
    onSelectPlan: (PaywallPlan) -> Unit,
) {
    val c = MonoTheme.colors

    val yearly = state.products[PaywallPlan.Yearly]
    val monthly = state.products[PaywallPlan.Monthly]

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (yearly != null) {
            PlanCard(
                title = "YEARLY",
                price = yearly.priceText,
                rightBadge = if (yearly.isBestValue) "BEST VALUE" else null,
                rightChip = yearly.secondaryText,
                crossedPrice = if (yearly.isBestValue) "$24.00" else null, // optional, can be removed
                selected = state.selectedPlan == PaywallPlan.Yearly,
                accent = c.errorColor, // HIIT red as highlight
                onClick = { onSelectPlan(PaywallPlan.Yearly) }
            )
        }

        if (monthly != null) {
            PlanCard(
                title = "MONTHLY",
                price = monthly.priceText,
                rightBadge = null,
                rightChip = monthly.secondaryText,
                crossedPrice = null,
                selected = state.selectedPlan == PaywallPlan.Monthly,
                accent = c.errorColor,
                onClick = { onSelectPlan(PaywallPlan.Monthly) }
            )
        }
    }

    Spacer(Modifier.height(8.dp))
    MonoDivider()
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    rightBadge: String?,
    rightChip: String?,
    crossedPrice: String?,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    val c = MonoTheme.colors
    val shape = RoundedCornerShape(18.dp)

    val borderColor = if (selected) accent else c.cardBorderColor
    val borderWidth = if (selected) 2.dp else 1.dp

    MonoCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable { onClick() }
            .border(borderWidth, borderColor, shape),
        backgroundColor = c.cardBackground,
        shadowElevation = MonoTheme.elevation.card,
    ) {
        Box(modifier = Modifier.padding(16.dp)) {

            if (rightBadge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(999.dp))
                        .background(accent)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    MonoText(
                        text = rightBadge,
                        style = MonoTextStyle.Label,
                        color = c.inverseTextColor,
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MonoText(
                    text = title,
                    style = MonoTextStyle.Label,
                    color = if (selected) accent else c.tertiaryTextColor,
                )

                MonoText(
                    text = price,
                    style = MonoTextStyle.TitleLarge,
                    color = c.primaryTextColor,
                )

                if (rightChip != null || crossedPrice != null) {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (rightChip != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(accent.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                MonoText(
                                    text = rightChip,
                                    style = MonoTextStyle.Label,
                                    color = accent,
                                )
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        if (crossedPrice != null) {
                            MonoText(
                                text = crossedPrice,
                                style = MonoTextStyle.Label,
                                color = c.tertiaryTextColor,
                            )
                        }
                    }
                }
            }
        }
    }
}
