package com.oho.hiit_timer.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.oho.core.ui.R
import com.oho.core.ui.components.MonoCard
import com.oho.core.ui.components.MonoIcon
import com.oho.core.ui.components.MonoScaffold
import com.oho.core.ui.components.MonoText
import com.oho.core.ui.components.MonoTextStyle
import com.oho.core.ui.theme.MonoTheme
import com.oho.hiit_timer.ProBadgeButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoute(
    isPro: Boolean = false,
    onBack: () -> Unit,
    onOpenSound: () -> Unit,
    onContactSupport: () -> Unit,
    onRateApp: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onProClick: () -> Unit = {},
    vm: SettingsViewModel = org.koin.androidx.compose.koinViewModel(),
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.events.collect { e ->
            when (e) {
                SettingsNavEvent.Back -> onBack()
                SettingsNavEvent.OpenSound -> onOpenSound()
                SettingsNavEvent.ContactSupport -> onContactSupport()
                SettingsNavEvent.RateApp -> onRateApp()
                SettingsNavEvent.OpenPrivacyPolicy -> onOpenPrivacyPolicy()
            }
        }
    }

    val content = state as? SettingsViewModel.UiState.Content

    val themeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    if (content?.isThemeSheetVisible == true) {
        ThemeBottomSheet(
            sheetState = themeSheetState,
            selected = content.themeMode,
            onDismiss = vm::dismissThemeSheet,
            onSelect = { vm.selectTheme(it) },
        )
    }

    MonoScaffold(Modifier.fillMaxSize()) {
        if (content != null) {
            SettingsScreen(
                state = content,
                isPro = isPro,
                onBack = vm::onBack,
                onPrepareMinus = vm::onPrepareMinus,
                onPreparePlus = vm::onPreparePlus,
                onToggleShowTotalRemaining = vm::onToggleShowTotalRemaining,
                onToggleAutoStartNextPhase = vm::onToggleAutoStartNextPhase,
                onOpenSound = vm::onOpenSound,
                onOpenTheme = vm::openThemeSheet,
                onToggleKeepScreenOn = vm::onToggleKeepScreenOn,
                onContactSupport = vm::onContactSupport,
                onRateApp = vm::onRateApp,
                onOpenPrivacyPolicy = vm::onOpenPrivacyPolicy,
                onProClick = onProClick,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    state: SettingsViewModel.UiState.Content,
    isPro: Boolean = false,
    onBack: () -> Unit,

    // Workout
    onPrepareMinus: () -> Unit,
    onPreparePlus: () -> Unit,
    onToggleShowTotalRemaining: () -> Unit,
    onToggleAutoStartNextPhase: () -> Unit,

    // Navigation
    onOpenSound: () -> Unit,
    onOpenTheme: () -> Unit,

    // General
    onToggleKeepScreenOn: () -> Unit,

    // Support
    onContactSupport: () -> Unit,
    onRateApp: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onProClick: () -> Unit = {},
) {
    val c = MonoTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.appBackground)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            TopAppBar(
                title = { Text("Settings") },
                actions = {
                    if (!isPro) {
                        ProBadgeButton(
                            onClick = onProClick,
                            modifier = Modifier.padding(end = 12.dp).align(Alignment.CenterVertically),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = c.appBackground,
                    titleContentColor = c.primaryTextColor,
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp)
                    .padding(top = 10.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                SectionHeader("Workout")
                SettingsCard {
                    RowStepperCenteredValue(
                        title = "Default prepare time",
                        value = "${state.defaultPrepareSec}s",
                        onMinus = onPrepareMinus,
                        onPlus = onPreparePlus,
                    )
                    RowToggle(
                        title = "Show total remaining time",
                        checked = state.showTotalRemaining,
                        onToggle = onToggleShowTotalRemaining,
                    )
                    RowToggle(
                        title = "Auto-start next phase",
                        checked = state.autoStartNextPhase,
                        onToggle = onToggleAutoStartNextPhase,
                        isLast = true,
                    )
                }

                SectionHeader("Sound")
                SettingsCard {
                    RowNav(
                        title = "Sound & vibration",
                        value = null,
                        onClick = onOpenSound,
                        isLast = true,
                    )
                }

                SectionHeader("General")
                SettingsCard {
                    RowNav(
                        title = "Theme",
                        value = state.themeMode.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        onClick = onOpenTheme,
                    )
                    RowToggle(
                        title = "Keep screen on",
                        checked = state.keepScreenOn,
                        onToggle = onToggleKeepScreenOn,
                        isLast = true,
                    )
                }

                SectionHeader("Support")
                SettingsCard {
                    RowAction("Contact support", onClick = onContactSupport)
                    RowAction("Rate app", onClick = onRateApp)
                    RowAction("Privacy policy", onClick = onOpenPrivacyPolicy, isLast = true)
                }

                Spacer(Modifier.height(10.dp))

                MonoText(
                    textAlign = TextAlign.Center,
                    text = state.versionName,
                    style = MonoTextStyle.BodySecondary,
                    color = c.secondaryTextColor.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                )
            }
        }
    }
}

/* =========================
 * Reusable building blocks
 * ========================= */

@Composable
fun SectionHeader(text: String) {
    val c = MonoTheme.colors
    MonoText(
        text = text.uppercase(),
        style = MonoTextStyle.Label,
        color = c.secondaryTextColor.copy(alpha = 0.60f),
        modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
    )
}

@Composable
fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit
) {
    val c = MonoTheme.colors
    MonoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = c.cardBackground,
        border = BorderStroke(1.dp, c.cardBorderColor),
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 0.dp
    ) {
        Column(Modifier.fillMaxWidth()) { content() }
    }
}

@Composable
fun DividerLine(isLast: Boolean = false) {
    val c = MonoTheme.colors
    if (isLast) return
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(c.cardBorderColor.copy(alpha = 0.50f))
    )
}

@Composable
fun RowToggle(
    title: String,
    checked: Boolean,
    onToggle: () -> Unit,
    isLast: Boolean = false,
) {
    val c = MonoTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MonoText(
            text = title,
            style = MonoTextStyle.BodyPrimary,
            color = c.primaryTextColor,
            modifier = Modifier.weight(1f)
        )

        // Material3 Switch = animated by default (cleaner than custom non-animated)
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedTrackColor = c.primaryButtonBackground,
                checkedThumbColor = c.inverseTextColor,
                checkedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                uncheckedTrackColor = c.disabledButtonBackground,
                uncheckedThumbColor = c.secondaryTextColor,
                uncheckedBorderColor = c.inputBorderColor,
            )
        )
    }
    if (!isLast) DividerLine()
}

@Composable
private fun RowStepperCenteredValue(
    title: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    val c = MonoTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MonoText(
            text = title,
            style = MonoTextStyle.BodyPrimary,
            color = c.primaryTextColor,
            modifier = Modifier.weight(1f)
        )

        StepperButton("–", onMinus)
        Spacer(Modifier.width(10.dp))

        // centered value (fixed-ish width)
        Box(
            modifier = Modifier
                .widthIn(min = 46.dp)
                .height(34.dp),
            contentAlignment = Alignment.Center
        ) {
            MonoText(
                text = value,
                style = MonoTextStyle.TitleMedium,
                color = c.primaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.width(10.dp))
        StepperButton("+", onPlus)
    }
    DividerLine()
}

@Composable
private fun StepperButton(
    text: String,
    onClick: () -> Unit,
) {
    val c = MonoTheme.colors
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(c.modalBackground.copy(alpha = 0.55f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        MonoText(
            text = text,
            style = MonoTextStyle.TitleMedium,
            color = c.secondaryTextColor
        )
    }
}

@Composable
fun RowNav(
    title: String,
    value: String?,
    onClick: () -> Unit,
    isLast: Boolean = false,
) {
    val c = MonoTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MonoText(
            text = title,
            style = MonoTextStyle.BodyPrimary,
            color = c.primaryTextColor,
            modifier = Modifier.weight(1f)
        )

        if (value != null) {
            MonoText(
                text = value,
                style = MonoTextStyle.BodySecondary,
                color = c.secondaryTextColor,
            )
            Spacer(Modifier.width(8.dp))
        }

        MonoIcon(
            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_navigate_next),
            contentDescription = null,
            tint = c.secondaryIconColor.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp),
        )
    }
    if (!isLast) DividerLine()
}

@Composable
private fun RowAction(
    title: String,
    onClick: () -> Unit,
    isLast: Boolean = false,
) {
    val c = MonoTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MonoText(
            text = title,
            style = MonoTextStyle.BodyPrimary,
            color = c.primaryTextColor,
            modifier = Modifier.weight(1f)
        )
    }
    if (!isLast) DividerLine()
}

/**
 * Mono-style slider:
 * - no left/right icons (noise)
 * - thicker track feel via colors + spacing
 * - thumb tinted (not pure white)
 */
@Composable
fun RowMonoSlider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    val c = MonoTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MonoText(
            text = title,
            style = MonoTextStyle.BodyPrimary,
            color = c.primaryTextColor,
        )

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = c.primaryButtonBackground,
                activeTrackColor = c.primaryButtonBackground.copy(alpha = 0.85f),
                inactiveTrackColor = c.cardBorderColor.copy(alpha = 0.25f),
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
    DividerLine()
}
