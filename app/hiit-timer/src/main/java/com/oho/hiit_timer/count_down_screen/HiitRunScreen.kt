package com.oho.hiit_timer.count_down_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oho.core.ui.R
import com.oho.core.ui.components.AnimatedNumberText
import com.oho.core.ui.components.MonoCard
import com.oho.core.ui.components.MonoIcon
import com.oho.core.ui.components.MonoPrimaryButton
import com.oho.core.ui.components.MonoScaffold
import com.oho.core.ui.components.MonoText
import com.oho.core.ui.components.MonoTextStyle
import com.oho.core.ui.components.RoundIconButton
import com.oho.core.ui.theme.MonoTheme
import com.oho.hiit_timer.formatSec

@Composable
fun HiitRunRoute() {
    val viewModel: HiitRunViewModel = viewModel()

    val uiState = viewModel.state.collectAsStateWithLifecycle()

    HiitRunScreen(
        state = uiState.value,
        onPauseResume = viewModel::onPauseResume,
        onNext = viewModel::onNext,
        onPrevious = viewModel::onPrevious,
        onClose = viewModel::onClose,
    )
}


@Composable
private fun HiitRunScreen(
    state: HiitRunUiState,
    onPauseResume: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit,
) {
    val c = MonoTheme.colors

    // WORK/REST state colors come from HiitMonoPalettes via MonoColors:
    val phaseCardBg = when (state.phase) {
        HiitPhase.Work -> c.errorColor      // red
        HiitPhase.Rest -> c.successColor    // green
        HiitPhase.Prepare -> c.accentPrimary
    }

    val onPhaseCardPrimary = c.inverseTextColor
    val onPhaseCardSecondary = c.inverseTextColor.copy(alpha = 0.75f)

    MonoScaffold(
        topBar = {
            RunTopBar(
                totalRemaining = state.totalRemaining,
                onClose = onClose,
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(c.appBackground)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Spacer(Modifier.height(56.dp))

                RunPhaseCard(
                    state = state,
                    cardBackground = phaseCardBg,
                    onCardPrimary = onPhaseCardPrimary,
                    onCardSecondary = onPhaseCardSecondary,
                )

                Spacer(Modifier.weight(1f))

                RunControls(
                    isPaused = state.isPaused,
                    onPauseResume = onPauseResume,
                    onNext = onNext,
                    onPrevious = onPrevious,
                )

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}


@Composable
private fun RunTopBar(
    totalRemaining: Int,
    onClose: () -> Unit,
) {
    val c = MonoTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MonoIcon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = "Close",
            tint = c.secondaryIconColor,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 16.dp)
                .clickable { onClose() }
        )

        MonoText(
            text = "TOTAL REMAINING",
            style = MonoTextStyle.Label,
            color = c.secondaryTextColor,
        )

        Spacer(Modifier.height(4.dp))

        AnimatedNumberText(
            text = formatSec(totalRemaining),
            style = MonoTheme.typography.titleLarge,
            color = c.primaryTextColor,
            intRepresentation = totalRemaining
        )
    }
}


@Composable
private fun RunPhaseCard(
    state: HiitRunUiState,
    cardBackground: Color,
    onCardPrimary: Color,
    onCardSecondary: Color,
) {
    MonoCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        backgroundColor = cardBackground,
        shadowElevation = MonoTheme.elevation.large,
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 36.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            MonoText(
                text = state.phaseLabel.uppercase(),
                style = MonoTextStyle.TitleLarge,
                color = onCardPrimary,
            )

            Spacer(Modifier.height(8.dp))

            AnimatedNumberText(
                intRepresentation = state.phaseRemaining,
                text = formatSec(state.phaseRemaining),
                style = MonoTheme.typography.displayLarge,
                color = onCardPrimary,
            )

            Spacer(Modifier.height(18.dp))

            MonoText(
                text = "Set ${state.setIndex} of ${state.setsTotal}",
                style = MonoTextStyle.TitleMedium,
                color = onCardPrimary,
            )

            state.nextLabel?.let {
                Spacer(Modifier.height(10.dp))
                MonoText(
                    text = "Next: $it",
                    style = MonoTextStyle.BodyPrimary,
                    color = onCardSecondary,
                )
            }
        }
    }
}

@Composable
private fun RunControls(
    isPaused: Boolean,
    onPauseResume: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        RoundIconButton(
            icon = {
                MonoIcon(
                    painterResource(R.drawable.ic_navigate_before),
                    contentDescription = null,
                    tint = MonoTheme.colors.primaryIconColor
                )
            },
            contentDescription = "Previous",
            onClick = onPrevious,
            modifier = Modifier.sizeIn(minWidth = 56.dp, minHeight = 56.dp)
        )

        MonoPrimaryButton(
            text = if (isPaused) "RESUME" else "PAUSE",
            onClick = onPauseResume,
        )

        RoundIconButton(
            icon = {
                MonoIcon(
                    painterResource(R.drawable.ic_navigate_next),
                    contentDescription = null,
                    tint = MonoTheme.colors.primaryIconColor
                )
            },
            contentDescription = "Next",
            onClick = onNext,
            modifier = Modifier.sizeIn(minWidth = 56.dp, minHeight = 56.dp)
        )
    }
}
