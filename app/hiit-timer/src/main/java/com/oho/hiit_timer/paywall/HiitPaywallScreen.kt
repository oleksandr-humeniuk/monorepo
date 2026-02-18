package com.oho.hiit_timer.paywall

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.oho.core.ui.R
import com.oho.core.ui.components.MonoCard
import com.oho.core.ui.components.MonoIcon
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

    val topGlow = remember(c.isDarkTheme, c.errorColor) {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.0f to c.errorColor.copy(alpha = if (c.isDarkTheme) 0.20f else 0.10f),
                0.40f to Color.Transparent,
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.appBackground),
    ) {
        Box(modifier = Modifier.fillMaxSize().background(topGlow))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            PaywallTopBar(onClose = onClose, onRestore = onRestore)

            // Scrollable benefits section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scroll)
                    .padding(horizontal = 20.dp),
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

                Spacer(Modifier.height(24.dp))

                BenefitsBlock()

                Spacer(Modifier.height(16.dp))
            }

            // Fixed bottom section: plans + CTA + footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp),
            ) {
                PlansBlock(state = state, onSelectPlan = onSelectPlan)

                Spacer(Modifier.height(16.dp))

                ProCtaButton(
                    text = if (state.isBusy) "Processing..." else "Unlock Pro",
                    onClick = onCta,
                    enabled = !state.isBusy &&
                        state.phase != PaywallUiPhase.Loading &&
                        state.products.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                )

                if (state.errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    MonoText(
                        text = state.errorMessage,
                        style = MonoTextStyle.BodySecondary,
                        color = c.errorColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(10.dp))

                MonoText(
                    text = "Cancel anytime. Google Play subscription.",
                    style = MonoTextStyle.Label,
                    color = c.tertiaryTextColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    MonoText(
                        text = "Privacy",
                        style = MonoTextStyle.Label,
                        color = c.linkTextColor,
                        modifier = Modifier.clickable { onOpenPrivacy() },
                    )
                    Spacer(Modifier.width(14.dp))
                    MonoText(
                        text = "Terms",
                        style = MonoTextStyle.Label,
                        color = c.linkTextColor,
                        modifier = Modifier.clickable { onOpenTerms() },
                    )
                }
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
        Spacer(Modifier.weight(1f))
        MonoText(
            text = "RESTORE",
            style = MonoTextStyle.Label,
            color = c.secondaryTextColor,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { onRestore() }
                .padding(horizontal = 10.dp, vertical = 8.dp),
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
            icon = R.drawable.ic_timer,
            title = "Complex Sets",
            subtitle = "Create nested intervals & loops",
        )
        BenefitRow(
            icon = R.drawable.ic_fitness_center,
            title = "Multiple Exercises",
            subtitle = "Assign specific moves to timers",
        )
        BenefitRow(
            icon = R.drawable.ic_volume_up,
            title = "Custom Sounds",
            subtitle = "TTS & custom beeps",
        )
        BenefitRow(
            icon = R.drawable.ic_push_pin,
            title = "Unlimited Saves",
            subtitle = "Keep your entire routine library",
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
                .background(c.secondaryButtonBackground),
            contentAlignment = Alignment.Center,
        ) {
            MonoIcon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = c.primaryIconColor,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            MonoText(text = title, style = MonoTextStyle.TitleMedium, color = c.primaryTextColor)
            Spacer(Modifier.height(2.dp))
            MonoText(text = subtitle, style = MonoTextStyle.BodySecondary, color = c.secondaryTextColor)
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
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (yearly != null) {
            // Outer box provides space for BEST VALUE badge to float above the card
            Box(modifier = Modifier.fillMaxWidth()) {
                val yearlySelected = state.selectedPlan == PaywallPlan.Yearly
                MonoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .clickable { onSelectPlan(PaywallPlan.Yearly) },
                    border = BorderStroke(
                        width = if (yearlySelected) 2.dp else 1.dp,
                        color = if (yearlySelected) c.errorColor else c.cardBorderColor,
                    ),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            MonoText(
                                text = "YEARLY",
                                style = MonoTextStyle.Label,
                                color = if (yearlySelected) c.errorColor else c.tertiaryTextColor,
                            )
                            Spacer(Modifier.height(4.dp))
                            MonoText(
                                text = yearly.priceText,
                                style = MonoTextStyle.TitleLarge,
                                color = c.primaryTextColor,
                            )
                        }
                        if (yearly.secondaryText != null) {
                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(c.errorColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                ) {
                                    MonoText(
                                        text = yearly.secondaryText,
                                        style = MonoTextStyle.Label,
                                        color = c.errorColor,
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                // Crossed-out reference price (monthly × 12)
                                Text(
                                    text = computeMonthlyAnnualPrice(state),
                                    style = MonoTheme.typography.label,
                                    color = c.tertiaryTextColor,
                                    textDecoration = TextDecoration.LineThrough,
                                )
                            }
                        }
                    }
                }

                // BEST VALUE badge floating above the card
                if (yearly.isBestValue) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(c.errorColor)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        MonoText(
                            text = "BEST VALUE",
                            style = MonoTextStyle.Label,
                            color = Color.White,
                        )
                    }
                }
            }
        }

        if (monthly != null) {
            val monthlySelected = state.selectedPlan == PaywallPlan.Monthly
            MonoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectPlan(PaywallPlan.Monthly) },
                border = BorderStroke(
                    width = if (monthlySelected) 2.dp else 1.dp,
                    color = if (monthlySelected) c.errorColor else c.cardBorderColor,
                ),
                contentPadding = PaddingValues(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        MonoText(
                            text = "MONTHLY",
                            style = MonoTextStyle.Label,
                            color = if (monthlySelected) c.errorColor else c.tertiaryTextColor,
                        )
                        Spacer(Modifier.height(4.dp))
                        MonoText(
                            text = monthly.priceText,
                            style = MonoTextStyle.TitleLarge,
                            color = c.primaryTextColor,
                        )
                    }
                    if (monthly.secondaryText != null) {
                        MonoText(
                            text = monthly.secondaryText,
                            style = MonoTextStyle.BodySecondary,
                            color = c.secondaryTextColor,
                        )
                    }
                }
            }
        }
    }
}

/** Derives the annual-if-paying-monthly reference price from the monthly product price text. */
private fun computeMonthlyAnnualPrice(state: HiitPaywallUiState): String {
    val monthlyText = state.products[PaywallPlan.Monthly]?.priceText ?: return ""
    // priceText is like "$2 / month" — extract the part before " /"
    val priceOnly = monthlyText.substringBefore(" /").trim()
    return "$priceOnly × 12"
}

@Composable
private fun ProCtaButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val c = MonoTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (enabled) c.errorColor else c.disabledButtonBackground)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        MonoText(
            text = text,
            style = MonoTextStyle.Button,
            color = if (enabled) Color.White else c.disabledButtonText,
        )
    }
}
