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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
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
import com.oho.hiit_timer.formatSec
import org.koin.androidx.compose.koinViewModel

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
    vm: WorkoutsListViewModel = koinViewModel(),
    openWorkout: (workoutId: String) -> Unit = {},
    startWorkout: (workoutId: String) -> Unit = {},
    createWorkout: () -> Unit = {},
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.events.collect { e ->
            when (e) {
                is WorkoutsListViewModel.Event.OpenWorkout -> openWorkout(e.workoutId)
                is WorkoutsListViewModel.Event.StartWorkout -> startWorkout(e.workoutId)
                WorkoutsListViewModel.Event.CreateWorkout -> createWorkout()
                WorkoutsListViewModel.Event.More -> Unit // hook later if needed
            }
        }
    }

    MonoScaffold(Modifier.fillMaxSize()) {
        WorkoutsListScreen(
            state = state,
            onWorkoutClick = vm::onWorkoutClicked,
            onStartClick = vm::onStartClicked,
            onCreateWorkout = vm::onCreateClicked,
            onMoreClicked = vm::onMoreClicked,
        )
    }
}

@Composable
private fun WorkoutsListScreen(
    state: WorkoutsListViewModel.UiState,
    onWorkoutClick: (workoutId: String) -> Unit,
    onStartClick: (workoutId: String) -> Unit,
    onCreateWorkout: () -> Unit,
    onMoreClicked: () -> Unit,
) {
    val c = MonoTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.appBackground)
    ) {
        Column(Modifier.fillMaxSize()) {
            WorkoutsTopBar(
                title = "Workouts",
                onMoreClicked = onMoreClicked,
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
                    onWorkoutClick = onWorkoutClick,
                    onStartClick = onStartClick,
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
    onMoreClicked: () -> Unit,
) {
    TopAppBar(
        modifier = Modifier.statusBarsPadding(),
        title = { Text(title) },
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
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            actionIconContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
        )
    )
}

@Composable
private fun WorkoutsList(
    items: List<WorkoutListItemUi>,
    onWorkoutClick: (workoutId: String) -> Unit,
    onStartClick: (workoutId: String) -> Unit,
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
        items(items, key = { it.id }) { item ->
            WorkoutRowCard(
                item = item,
                onClick = { onWorkoutClick(item.id) },
                onStartClick = { onStartClick(item.id) },
            )
        }
    }
}

@Composable
private fun WorkoutRowCard(
    item: WorkoutListItemUi,
    onClick: () -> Unit,
    onStartClick: () -> Unit,
) {
    val colors = MonoTheme.colors

    MonoCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        backgroundColor = colors.cardBackground,
        shadowElevation = 0.dp,
        // If your MonoCard supports borderColor:
        border = BorderStroke(
            width = 1.dp,
            color = colors.cardBorderColor.copy(alpha = 0.70f)
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                MonoText(
                    text = item.name,
                    style = MonoTextStyle.TitleLarge,
                    color = colors.primaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(8.dp))

                MonoText(
                    // per your decision: no icon, just "blocks"
                    text = buildMetaText(item.blocksCount, item.totalDurationSec),
                    style = MonoTextStyle.BodySecondary,
                    color = colors.secondaryTextColor,
                    maxLines = 1,
                )
            }

            Spacer(Modifier.size(12.dp))

            StartPillButton(
                text = "Start",
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
    val c = MonoTheme.colors

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = c.accentPrimary,
        contentColor = c.inverseTextColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            MonoText(
                text = text,
                style = MonoTextStyle.TitleMedium,
                color = c.inverseTextColor,
            )
        }
    }
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
            text = "Create your first workout",
            style = MonoTextStyle.TitleLarge,
            color = c.primaryTextColor,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        MonoText(
            text = "Build a simple sequence of blocks and start training in seconds.",
            style = MonoTextStyle.BodyPrimary,
            color = c.secondaryTextColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(22.dp))

        MonoPrimaryButton(
            text = "Create workout",
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
            contentDescription = "Add workout",
            tint = c.accentPrimary,
            modifier = Modifier.size(22.dp),
        )
    }
}

private fun buildMetaText(blocksCount: Int, totalDurationSec: Int): String {
    val blocksPart = if (blocksCount == 1) "1 block" else "$blocksCount blocks"
    return "$blocksPart • ${formatSec(totalDurationSec)}"
}
