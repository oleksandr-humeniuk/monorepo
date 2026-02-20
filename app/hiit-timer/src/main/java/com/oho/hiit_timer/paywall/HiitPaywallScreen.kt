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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.oho.core.ui.R
import com.oho.core.ui.components.MonoCard
import com.oho.core.ui.components.MonoIcon
import com.oho.core.ui.components.MonoText
import com.oho.core.ui.components.MonoTextStyle
import com.oho.core.ui.theme.MonoTheme
import com.oho.utils.R as timerR

@Composable
fun HiitPaywallScreen(
    state: HiitPaywallUiState,
    onClose: () -> Unit,
    onSelectPlan: (PaywallPlan) -> Unit,
    onCta: () -> Unit,
    onRestore: () -> Unit,
    onRetry: () -> Unit,
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
                    text = stringResource(timerR.string.paywall_title),
                    style = MonoTextStyle.TitleLarge,
                    color = c.primaryTextColor,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(10.dp))

                MonoText(
                    text = stringResource(timerR.string.paywall_subtitle),
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
                when (state.phase) {
                    PaywallUiPhase.Loading -> PlansLoadingBlock()
                    PaywallUiPhase.Failed -> PlansErrorBlock(onRetry = onRetry)
                    else -> {
                        PlansBlock(state = state, onSelectPlan = onSelectPlan)

                        Spacer(Modifier.height(16.dp))

                        ProCtaButton(
                            text = if (state.isBusy) stringResource(timerR.string.paywall_processing) else stringResource(timerR.string.paywall_unlock_pro),
                            onClick = onCta,
                            enabled = !state.isBusy && state.products.isNotEmpty(),
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
                    }
                }

                Spacer(Modifier.height(10.dp))

                MonoText(
                    text = stringResource(timerR.string.paywall_disclaimer),
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
                        text = stringResource(timerR.string.paywall_privacy),
                        style = MonoTextStyle.Label,
                        color = c.linkTextColor,
                        modifier = Modifier.clickable { onOpenPrivacy() },
                    )
                    Spacer(Modifier.width(14.dp))
                    MonoText(
                        text = stringResource(timerR.string.paywall_terms),
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
            text = stringResource(timerR.string.paywall_restore),
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
            title = stringResource(timerR.string.paywall_benefit_complex_sets),
            subtitle = stringResource(timerR.string.paywall_benefit_complex_sets_desc),
        )
        BenefitRow(
            icon = R.drawable.ic_fitness_center,
            title = stringResource(timerR.string.paywall_benefit_multiple_exercises),
            subtitle = stringResource(timerR.string.paywall_benefit_multiple_exercises_desc),
        )
        BenefitRow(
            icon = R.drawable.ic_volume_up,
            title = stringResource(timerR.string.paywall_benefit_custom_sounds),
            subtitle = stringResource(timerR.string.paywall_benefit_custom_sounds_desc),
        )
        BenefitRow(
            icon = R.drawable.ic_push_pin,
            title = stringResource(timerR.string.paywall_benefit_unlimited_saves),
            subtitle = stringResource(timerR.string.paywall_benefit_unlimited_saves_desc),
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
                                text = stringResource(timerR.string.paywall_yearly),
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
                            text = stringResource(timerR.string.paywall_best_value),
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
                            text = stringResource(timerR.string.paywall_monthly),
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

@Composable
private fun PlansLoadingBlock() {
    val c = MonoTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(2) { index ->
            MonoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .let { mod -> if (index == 0) mod.padding(top = 14.dp) else mod },
                contentPadding = PaddingValues(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(width = 60.dp, height = 12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(c.secondaryButtonBackground),
                        )
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(width = 120.dp, height = 20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(c.secondaryButtonBackground),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(width = 70.dp, height = 24.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(c.secondaryButtonBackground),
                    )
                }
            }
        }
    }
}

@Composable
private fun PlansErrorBlock(onRetry: () -> Unit) {
    val c = MonoTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MonoText(
            text = stringResource(timerR.string.paywall_load_error),
            style = MonoTextStyle.BodySecondary,
            color = c.secondaryTextColor,
        )
        MonoText(
            text = stringResource(timerR.string.paywall_retry),
            style = MonoTextStyle.Label,
            color = c.linkTextColor,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { onRetry() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
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

@Preview(showBackground = true, name = "Paywall – Yearly selected")
@Composable
private fun HiitPaywallScreenPreview() {
    MonoTheme {
        HiitPaywallScreen(
            state = HiitPaywallUiState(
                phase = PaywallUiPhase.Ready,
                products = mapOf(
                    PaywallPlan.Yearly to PaywallProduct(
                        plan = PaywallPlan.Yearly,
                        priceText = "$39.99 / year",
                        secondaryText = "Save 67%",
                        isBestValue = true,
                    ),
                    PaywallPlan.Monthly to PaywallProduct(
                        plan = PaywallPlan.Monthly,
                        priceText = "$9.99 / month",
                        secondaryText = "Flexible access",
                        isBestValue = false,
                    ),
                ),
                selectedPlan = PaywallPlan.Yearly,
            ),
            onClose = {},
            onSelectPlan = {},
            onCta = {},
            onRestore = {},
            onRetry = {},
            onOpenTerms = {},
            onOpenPrivacy = {},
        )
    }
}

@Preview(showBackground = true, name = "Paywall – Monthly selected")
@Composable
private fun HiitPaywallScreenMonthlyPreview() {
    MonoTheme {
        HiitPaywallScreen(
            state = HiitPaywallUiState(
                phase = PaywallUiPhase.Ready,
                products = mapOf(
                    PaywallPlan.Yearly to PaywallProduct(
                        plan = PaywallPlan.Yearly,
                        priceText = "$39.99 / year",
                        secondaryText = "Save 67%",
                        isBestValue = true,
                    ),
                    PaywallPlan.Monthly to PaywallProduct(
                        plan = PaywallPlan.Monthly,
                        priceText = "$9.99 / month",
                        secondaryText = "Flexible access",
                        isBestValue = false,
                    ),
                ),
                selectedPlan = PaywallPlan.Monthly,
            ),
            onClose = {},
            onSelectPlan = {},
            onCta = {},
            onRestore = {},
            onRetry = {},
            onOpenTerms = {},
            onOpenPrivacy = {},
        )
    }
}

@Preview(showBackground = true, name = "Paywall – Loading")
@Composable
private fun HiitPaywallScreenLoadingPreview() {
    MonoTheme {
        HiitPaywallScreen(
            state = HiitPaywallUiState(phase = PaywallUiPhase.Loading),
            onClose = {},
            onSelectPlan = {},
            onCta = {},
            onRestore = {},
            onRetry = {},
            onOpenTerms = {},
            onOpenPrivacy = {},
        )
    }
}

@Preview(showBackground = true, name = "Paywall – Failed")
@Composable
private fun HiitPaywallScreenFailedPreview() {
    MonoTheme {
        HiitPaywallScreen(
            state = HiitPaywallUiState(phase = PaywallUiPhase.Failed),
            onClose = {},
            onSelectPlan = {},
            onCta = {},
            onRestore = {},
            onRetry = {},
            onOpenTerms = {},
            onOpenPrivacy = {},
        )
    }
}
