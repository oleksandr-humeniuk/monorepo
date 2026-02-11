package com.oho.hiit_timer.workouts.add

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
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
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * Create Workout screen (blocks builder):
 * - In-memory mocked state (no DB)
 * - Blocks list + "Add block"
 * - Bottom bar with total duration + start
 * - Route exposes callbacks via events
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditWorkoutRoute(
    workoutId: String,
    onBack: () -> Unit = {},
    onStartWorkout: (workoutId: String) -> Unit = {},
    // optional: open block editor / menu
    onOpenBlockMenu: (blockId: String) -> Unit = {},
) {
    val vm: CreateEditWorkoutViewModel = koinViewModel {
        parametersOf(workoutId)
    }

    val state by vm.state.collectAsState(initial = CreateEditWorkoutViewModel.UiState())
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
//    BlockMenuBottomSheet(
//        sheetState = sheetState,
//        onDismiss = {},
//        onEdit = {},
//        onDuplicate = {},
//        onDelete = { },
//    )

    LaunchedEffect(Unit) {
        vm.events.collect { e ->
            when (e) {
                CreateEditWorkoutViewModel.Event.Back -> onBack()
                is CreateEditWorkoutViewModel.Event.Start -> onStartWorkout(e.workoutId)
                is CreateEditWorkoutViewModel.Event.OpenBlockMenu -> onOpenBlockMenu(e.blockId)
            }
        }
    }

    MonoScaffold(Modifier.fillMaxSize()) {
        CreateWorkoutScreen(
            state = state,
            onBack = vm::onBackClicked,
            onAddBlock = vm::onAddBlockClicked,
            onBlockMore = vm::onBlockMoreClicked,
            onSave = vm::onSaveClicked,
            onReorder = vm::onReorderBlocks
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateWorkoutScreen(
    state: CreateEditWorkoutViewModel.UiState,
    onBack: () -> Unit,
    onAddBlock: () -> Unit,
    onBlockMore: (blockId: String) -> Unit,
    onSave: () -> Unit,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit
) {
    val c = MonoTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.appBackground)
    ) {
        Column(Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = { Text("Create workout") },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        MonoIcon(
                            painter = painterResource(R.drawable.ic_navigate_before),
                            contentDescription = "Back",
                            tint = c.primaryIconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                actions = {},
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = c.appBackground,
                    titleContentColor = c.primaryTextColor,
                )
            )
            val lazyListState = rememberLazyListState()
            val reorderableLazyListState =
                rememberReorderableLazyListState(lazyListState) { from, to ->
                    onReorder.invoke(from.index, to.index)
                }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                contentPadding = PaddingValues(
                    start = 14.dp,
                    end = 14.dp,
                    top = 14.dp,
                    bottom = 140.dp, // space for bottom bar
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(state.blocks, key = { it.id }) { block ->
                    ReorderableItem(reorderableLazyListState, key = block.id) { isDragging ->
                        BlockRowCard(
                            block = block,
                            onMore = { onBlockMore(block.id) }
                        )
                    }
                }

                item(key = "add_block") {
                    AddBlockCard(onClick = onAddBlock)
                }
            }
        }

        BottomBar(
            totalTime = state.totalDurationSec,
            onSaveClicked = onSave,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ReorderableCollectionItemScope.BlockRowCard(
    block: WorkoutBlockUi,
    onMore: () -> Unit,
) {
    val c = MonoTheme.colors

    MonoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = c.cardBackground,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, c.cardBorderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Drag handle (visual only for now)
            MonoIcon(
                painter = painterResource(R.drawable.ic_drag_handle), // ensure you have it; else replace with ic_more_vert rotated etc.
                contentDescription = null,
                tint = c.secondaryIconColor.copy(alpha = 0.55f),
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 10.dp)
                    .draggableHandle()
            )

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MonoText(
                        text = block.name,
                        style = MonoTextStyle.TitleMedium,
                        color = c.primaryTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(10.dp))

                    TotalChip(text = "Total: ${formatSec(block.totalDurationSec)}")
                }

                Spacer(Modifier.height(8.dp))

                MonoText(
                    text = buildBlockMeta(block.spec),
                    style = MonoTextStyle.BodySecondary,
                    color = c.secondaryTextColor,
//                    fontFamily = if (metaLooksMonospace(block.spec)) FontFamily.Monospace else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onMore),
                contentAlignment = Alignment.Center
            ) {
                MonoIcon(
                    painter = painterResource(R.drawable.ic_more_vert),
                    contentDescription = "Block menu",
                    tint = c.secondaryIconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun TotalChip(text: String) {
    val c = MonoTheme.colors
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = c.modalBackground.copy(alpha = 0.55f),
        contentColor = c.secondaryTextColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, c.cardBorderColor.copy(alpha = 0.6f))
    ) {
        MonoText(
            text = text,
            style = MonoTextStyle.Label,
            color = c.secondaryTextColor,
//            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun AddBlockCard(onClick: () -> Unit) {
    val c = MonoTheme.colors
    val strokeWidthPx = 2.0f
    val dash = floatArrayOf(14f, 10f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .drawBehind {
                val paint = Stroke(
                    width = strokeWidthPx,
                    pathEffect = PathEffect.dashPathEffect(dash, 0f),
                )
                drawRoundRect(
                    color = c.cardBorderColor.copy(alpha = 0.9f),
                    style = paint,
                    cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
                )
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MonoIcon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                tint = c.secondaryIconColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            MonoText(
                text = "Add block",
                style = MonoTextStyle.TitleMedium,
                color = c.secondaryTextColor
            )
        }
    }
}

@Composable
private fun BottomBar(
    totalTime: Int,
    onSaveClicked: () -> Unit,
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
                enabled = totalTime > 0,
                onClick = onSaveClicked,
                text = "Save Workout",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            )
        }
    }
}

private fun buildBlockMeta(spec: WorkoutBlockSpec): String = when (spec) {
    is WorkoutBlockSpec.Single -> {
        val setsPart = if (spec.sets == 1) "1 set" else "${spec.sets} sets"
        "$setsPart • ${formatSec(spec.durationSec)}"
    }

    is WorkoutBlockSpec.Interval -> {
        val setsPart = if (spec.sets == 1) "1 set" else "${spec.sets} sets"
        "$setsPart • ${formatSec(spec.workSec)} / ${formatSec(spec.restSec)}"
    }
}

