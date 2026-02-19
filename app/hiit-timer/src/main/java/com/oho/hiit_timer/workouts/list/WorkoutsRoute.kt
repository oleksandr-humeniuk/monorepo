package com.oho.hiit_timer.workouts.list

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oho.core.ui.R
import com.oho.core.ui.components.MonoCard
import com.oho.core.ui.components.MonoIcon
import com.oho.core.ui.components.MonoPrimaryButton
import com.oho.core.ui.components.MonoScaffold
import com.oho.core.ui.components.MonoText
import com.oho.core.ui.components.MonoTextStyle
import com.oho.core.ui.theme.MonoTheme
import com.oho.hiit_timer.ProBadgeButton
import com.oho.hiit_timer.ProUpgradeBanner
import com.oho.hiit_timer.formatSec
import org.koin.androidx.compose.koinViewModel
import com.oho.utils.R as timerR

/**
 * Fully self-contained Workouts screen with:
 * - ViewModel
 * - mocked state for debug
 * - route composable that exposes navigation callbacks
 *
 * Drop-in usage:
 *   WorkoutsRoute(
 *      openWorkout = { id -> ... },
 *      startWorkout = { id -> ... },
 *      createWorkout = { ... }
 *   )
 */
@Composable
fun WorkoutsRoute(
    isPro: Boolean = false,
    vm: WorkoutsListViewModel = koinViewModel(),
    openWorkout: (workoutId: String) -> Unit,
    runWrokout: (workoutId: String) -> Unit,
    createEditWorkout: (String?) -> Unit, //workoutId null if new
    onProClick: () -> Unit = {},
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.events.collect { e ->
            when (e) {
                is WorkoutsListViewModel.Event.OpenWorkout -> openWorkout(e.workoutId)
                is WorkoutsListViewModel.Event.StartWorkout -> runWrokout(e.workoutId)
                WorkoutsListViewModel.Event.CreateWorkout -> createEditWorkout(null)
                WorkoutsListViewModel.Event.More -> Unit // hook later if needed
            }
        }
    }

    MonoScaffold(Modifier.fillMaxSize()) {
        WorkoutsListScreen(
            state = state,
            isPro = isPro,
            onWorkoutClick = vm::onWorkoutClicked,
            onStartClick = vm::onStartClicked,
            onCreateWorkout = vm::onCreateClicked,
            onProClick = onProClick,
        )
    }
}

@Composable
private fun WorkoutsListScreen(
    state: WorkoutsListViewModel.UiState,
    isPro: Boolean = false,
    onWorkoutClick: (workoutId: String) -> Unit,
    onStartClick: (workoutId: String) -> Unit,
    onCreateWorkout: () -> Unit,
    onProClick: () -> Unit = {},
) {
    val c = MonoTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.appBackground)
    ) {
        Column(Modifier.fillMaxSize()) {
            WorkoutsTopBar(
                title = stringResource(timerR.string.workouts_title),
                isPro = isPro,
                onProClick = onProClick,
            )

            if (state.items.isEmpty()) {
                WorkoutsEmptyState(
                    onCreateWorkout = onCreateWorkout,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                )
            } else {
                WorkoutsList(
                    items = state.items,
                    isPro = isPro,
                    onWorkoutClick = onWorkoutClick,
                    onStartClick = onStartClick,
                    onProClick = onProClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Rule: FAB only when list is not empty
        if (state.items.isNotEmpty()) {
            AddFab(
                onClick = onCreateWorkout,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 18.dp, bottom = 18.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutsTopBar(
    title: String,
    isPro: Boolean = false,
    onProClick: () -> Unit,
) {
    TopAppBar(
        modifier = Modifier.statusBarsPadding(),
        title = { Text(title) },
        actions = {
            if (!isPro) {
                ProBadgeButton(
                    onClick = onProClick,
                    modifier = Modifier.padding(end = 12.dp).align(Alignment.CenterVertically),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            actionIconContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
        )
    )
}

@Composable
private fun WorkoutsList(
    items: List<WorkoutListItemUi>,
    isPro: Boolean,
    onWorkoutClick: (workoutId: String) -> Unit,
    onStartClick: (workoutId: String) -> Unit,
    onProClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 10.dp,
            bottom = 96.dp, // space for bottom nav / FAB
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (!isPro && items.size > 1) {
            item(key = "pro_banner") {
                ProUpgradeBanner(onClick = onProClick)
            }
        }
        items(items, key = { it.id }) { item ->
            val needsPro = !isPro && !item.isFreeWorkout
            WorkoutRowCard(
                item = item,
                showProBadge = needsPro,
                onClick = { onWorkoutClick(item.id) },
                onStartClick = { if (needsPro) onProClick() else onStartClick(item.id) },
                onProClick = onProClick,
            )
        }
    }
}

@Composable
private fun WorkoutRowCard(
    item: WorkoutListItemUi,
    showProBadge: Boolean = false,
    onClick: () -> Unit,
    onStartClick: () -> Unit,
    onProClick: () -> Unit = {},
) {
    val colors = MonoTheme.colors

    MonoCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        backgroundColor = colors.cardBackground,
        shadowElevation = 0.dp,
        border = BorderStroke(
            width = 1.dp,
            color = colors.cardBorderColor.copy(alpha = 0.70f)
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MonoText(
                        text = item.name,
                        style = MonoTextStyle.TitleMedium,
                        color = colors.primaryTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (showProBadge) {
                        Spacer(Modifier.size(8.dp))
                        ProBadgeButton(onClick = onProClick)
                    }
                }

                Spacer(Modifier.height(8.dp))

                MonoText(
                    text = buildMetaText(item.blocksCount, item.totalDurationSec),
                    style = MonoTextStyle.BodySecondary,
                    color = colors.secondaryTextColor,
                    maxLines = 1,
                )
            }

            Spacer(Modifier.size(12.dp))

            StartPillButton(
                text = stringResource(timerR.string.start_button),
                onClick = onStartClick,
            )
        }
    }
}

@Composable
private fun StartPillButton(
    text: String,
    onClick: () -> Unit,
) {
    MonoPrimaryButton(
        onClick = onClick,
        text = text,
        modifier = Modifier
            .height(44.dp),
    )
}

@Composable
private fun WorkoutsEmptyState(
    onCreateWorkout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = MonoTheme.colors

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Optional minimal hero
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(c.modalBackground),
            contentAlignment = Alignment.Center,
        ) {
            MonoIcon(
                painter = painterResource(R.drawable.ic_timer),
                contentDescription = null,
                tint = c.accentPrimary,
                modifier = Modifier.size(26.dp),
            )
        }

        Spacer(Modifier.height(18.dp))

        MonoText(
            text = stringResource(timerR.string.empty_workouts_title),
            style = MonoTextStyle.TitleLarge,
            color = c.primaryTextColor,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        MonoText(
            text = stringResource(timerR.string.empty_workouts_description),
            style = MonoTextStyle.BodyPrimary,
            color = c.secondaryTextColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(22.dp))

        MonoPrimaryButton(
            text = stringResource(timerR.string.create_workout_button),
            onClick = onCreateWorkout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        )
    }
}

@Composable
private fun AddFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = MonoTheme.colors

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        containerColor = c.modalBackground,
        contentColor = c.accentPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
        ),
    ) {
        MonoIcon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = stringResource(timerR.string.add_workout_fab),
            tint = c.accentPrimary,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun buildMetaText(blocksCount: Int, totalDurationSec: Int): String {
    return pluralStringResource(
        timerR.plurals.workout_meta_info,
        count = blocksCount,
        blocksCount, formatSec(totalDurationSec)
    )
}
