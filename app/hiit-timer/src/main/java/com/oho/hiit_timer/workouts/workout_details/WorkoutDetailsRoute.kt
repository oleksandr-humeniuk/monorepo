package com.oho.hiit_timer.workouts.workout_details

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oho.core.ui.R
import com.oho.core.ui.components.AnimatedNumberText
import com.oho.core.ui.components.MonoCard
import com.oho.core.ui.components.MonoIcon
import com.oho.core.ui.components.MonoPrimaryButton
import com.oho.core.ui.components.MonoScaffold
import com.oho.core.ui.components.MonoText
import com.oho.core.ui.components.MonoTextStyle
import com.oho.core.ui.theme.MonoTheme
import com.oho.hiit_timer.formatSec
import com.oho.hiit_timer.workouts.add.MenuBottomSheet
import com.oho.hiit_timer.workouts.add.TotalChip
import com.oho.hiit_timer.workouts.add.WorkoutBlockUi
import com.oho.hiit_timer.workouts.add.buildBlockMeta
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import com.oho.utils.R as timerR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailsRoute(
    workoutId: String,
    onBack: () -> Unit,
    onStartWorkout: (String) -> Unit,
    onEditWorkout: (String) -> Unit,
    onDuplicateWorkout: () -> Unit,
) {
    val vm: WorkoutDetailsViewModel = koinViewModel {
        parametersOf(workoutId)
    }

    val state by vm.state.collectAsState()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    if (state.showMoreSheet) {
        MenuBottomSheet(
            title = state.title,
            sheetState = sheetState,
            onDismiss = {
                vm.onMoreDismissed()
            },
            onEdit = {
                vm.onEdit()
            },
            onDuplicate = {
                vm.onDuplicate()
            },
            onDelete = {
                vm.onDelete()
            },
        )
    }

    MonoScaffold(Modifier.fillMaxSize()) {
        WorkoutDetailsScreen(
            state = state,
            onBack = onBack,
            onStart = { onStartWorkout(workoutId) },
            onMoreClicked = {
                vm.onMoreClicked()
            }
        )
    }

    LaunchedEffect(Unit) {
        vm.events.collect { e ->
            when (e) {
                WorkoutDetailsViewModel.Event.Duplicate -> onDuplicateWorkout()
                is WorkoutDetailsViewModel.Event.Edit -> onEditWorkout(e.workoutId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutDetailsScreen(
    state: WorkoutDetailsViewModel.UiState,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onMoreClicked: () -> Unit
) {
    val c = MonoTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.appBackground)
    ) {
        Column(Modifier.fillMaxSize()) {

            CenterAlignedTopAppBar(
                title = { Text(state.title) },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        MonoIcon(
                            painter = painterResource(R.drawable.ic_navigate_before),
                            contentDescription = null,
                            tint = c.primaryIconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = c.appBackground,
                    titleContentColor = c.primaryTextColor,
                ),
                actions = {
                    IconButton(onClick = onMoreClicked) {
                        Icon(
                            painterResource(R.drawable.ic_more_vert),
                            contentDescription = stringResource(timerR.string.more)
                        )
                    }
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 14.dp,
                    end = 14.dp,
                    top = 14.dp,
                    bottom = 140.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(state.blocks, key = { it.id }) { block ->
                    WorkoutDetailsBlockCard(block)
                }
            }
        }

        DetailsBottomBar(
            totalTime = state.totalDurationSec,
            onStartClicked = onStart,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun WorkoutDetailsBlockCard(
    block: WorkoutBlockUi,
) {
    val c = MonoTheme.colors

    MonoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = c.cardBackground,
        border = BorderStroke(1.dp, c.cardBorderColor),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                MonoText(
                    text = block.name,
                    style = MonoTextStyle.TitleMedium,
                    color = c.primaryTextColor,
                    modifier = Modifier.weight(1f)
                )

                TotalChip(
                    text = stringResource(
                        timerR.string.total_chip,
                        formatSec(block.totalDurationSec)
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            MonoText(
                text = buildBlockMeta(block.spec),
                style = MonoTextStyle.BodySecondary,
                color = c.secondaryTextColor
            )
        }
    }
}

@Composable
private fun DetailsBottomBar(
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
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(timerR.string.total_duration),
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
                enabled = totalTime > 0,
                onClick = onStartClicked,
                text = stringResource(timerR.string.start_button),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            )
        }
    }
}
