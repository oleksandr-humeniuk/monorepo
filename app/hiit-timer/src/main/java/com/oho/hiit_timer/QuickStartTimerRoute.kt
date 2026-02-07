package com.oho.hiit_timer

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oho.core.ui.R
import com.oho.core.ui.components.AnimatedNumberText
import com.oho.core.ui.components.MonoPrimaryButton
import com.oho.core.ui.components.RoundIconButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun QuickStartTimerRoute(
    vm: QuickStartTimerViewModel = koinViewModel(),
    onBack: () -> Unit = {},
    openWorkout: (workoutId: String) -> Unit = { _ -> }
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is QuickStartTimerViewModel.Event.OpenWorkout -> {
                    openWorkout(event.workoutId)
                }
            }
        }
    }

    IntervalTimerConfigScreen(
        state = state,
        onBackClicked = { onBack(); vm.onBackClicked() },
        onMoreClicked = vm::onMoreClicked,
        onSetsMinus = vm::onSetsMinus,
        onSetsPlus = vm::onSetsPlus,
        onWorkMinus = vm::onWorkMinus,
        onWorkPlus = vm::onWorkPlus,
        onRestMinus = vm::onRestMinus,
        onRestPlus = vm::onRestPlus,
        onStartClicked = { vm.onStartClicked() },
        onSetPillClicked = vm::onSetPillClicked,
        onWorkPillClicked = vm::onWorkPillClicked,
        onRestPillClicked = vm::onRestPillClicked,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntervalTimerConfigScreen(
    state: QuickStartTimerViewModel.UiState,
    onBackClicked: () -> Unit,
    onMoreClicked: () -> Unit,
    onSetsMinus: () -> Unit,
    onSetsPlus: () -> Unit,
    onWorkMinus: () -> Unit,
    onWorkPlus: () -> Unit,
    onRestMinus: () -> Unit,
    onRestPlus: () -> Unit,
    onStartClicked: () -> Unit,
    onSetPillClicked: () -> Unit,
    onWorkPillClicked: () -> Unit,
    onRestPillClicked: () -> Unit,
) {
    val page = MaterialTheme.colorScheme.background
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(page)
    ) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Quick start") },
                navigationIcon = {
                },
                actions = {
                    IconButton(onClick = onMoreClicked) {
                        Icon(
                            painterResource(R.drawable.ic_more_vert),
                            contentDescription = "More"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = page,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 120.dp),
            ) {
                SectionTitle("STRUCTURE")
                SurfaceCard(
                    shape = RoundedCornerShape(12.dp)
                ) {
                    RowItem(
                        label = "Sets",
                        text = state.sets.toString(),
                        value = state.sets,
                        onMinus = onSetsMinus,
                        onPlus = onSetsPlus,
                        onPillClick = onSetPillClicked
                    )
                }

                Spacer(Modifier.height(18.dp))

                SectionTitle("TIMING")
                SurfaceCard(
                    shape = RoundedCornerShape(12.dp)
                ) {
                    RowItem(
                        label = "Work",
                        text = formatSec(state.workSec),
                        value = state.workSec,
                        onMinus = onWorkMinus,
                        onPlus = onWorkPlus,
                        onPillClick = onWorkPillClicked
                    )
                    Divider(color = outline)
                    RowItem(
                        label = "Rest",
                        text = formatSec(state.restSec),
                        value = state.restSec,
                        onMinus = onRestMinus,
                        onPlus = onRestPlus,
                        onPillClick = onRestPillClicked
                    )
                }
            }
        }

        BottomBar(
            totalTime = state.totalDurationSec,
            onStartClicked = onStartClicked,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
    )
}

@Composable
private fun SurfaceCard(
    shape: Shape = RoundedCornerShape(28.dp),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        )
    ) {
        Column { content() }
    }
}

@Composable
private fun RowItem(
    label: String,
    text: String,
    value: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onPillClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            RoundIconButton(
                icon = { Icon(painterResource(R.drawable.ic_minus), contentDescription = null) },
                contentDescription = "Decrease $label",
                onClick = onMinus,
                modifier = Modifier.sizeIn(minWidth = 56.dp, minHeight = 56.dp)
            )

            Spacer(Modifier.size(12.dp))

            ValuePill(
                value = value,
                text = text,
                onPillClick = onPillClick
            )

            Spacer(Modifier.size(12.dp))

            RoundIconButton(
                icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
                contentDescription = "Increase $label",
                onClick = onPlus,
                modifier = Modifier.sizeIn(minWidth = 56.dp, minHeight = 56.dp)
            )
        }
    }
}

@Composable
private fun ValuePill(
    value: Int,
    text: String,
    onPillClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme

    val pillBg = cs.primary.copy(alpha = 0.08f)
    val pillBorder = cs.primary.copy(alpha = 0.18f)

    Surface(
        modifier = Modifier,
        shape = RoundedCornerShape(14.dp),
        color = pillBg,
        border = BorderStroke(1.dp, pillBorder),
    ) {
        Box(
            modifier = Modifier
                .clickable(onClick = onPillClick)
        ) {
            AnimatedNumberText(
                text = text,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Monospace,
                color = cs.onSurface,
                intRepresentation = value
            )
        }
    }
}

@Composable
private fun BottomBar(
    totalTime: Int,
    onStartClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "TOTAL DURATION",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.60f),
                )
                Spacer(Modifier.size(12.dp))

                AnimatedNumberText(
                    text = formatSec(totalTime),
                    intRepresentation = totalTime
                )
            }

            Spacer(Modifier.height(16.dp))

            MonoPrimaryButton(
                onClick = onStartClicked,
                text = "Start Workout",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            )
        }
    }
}
