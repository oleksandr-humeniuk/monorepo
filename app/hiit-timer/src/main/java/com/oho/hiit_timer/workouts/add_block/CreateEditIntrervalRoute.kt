package com.oho.hiit_timer.workouts.add_block

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.oho.core.ui.R
import com.oho.core.ui.components.AnimatedNumberText
import com.oho.core.ui.components.MonoPrimaryButton
import com.oho.core.ui.components.RoundIconButton
import com.oho.hiit_timer.formatSec
import org.koin.androidx.compose.koinViewModel
import com.oho.utils.R as timerR

/**
 * Create/Edit Interval Block route.
 *
 * - Optional name input (top).
 * - Structure: Sets
 * - Timing: Work / Rest (+ optional Last rest when sets > 1)
 * - Save returns [IntervalBlockDraft] via events.
 */
@Composable
fun CreateEditIntrervalRoute(
    vm: CreateEditIntervalViewModel = koinViewModel(),
    title: String = stringResource(timerR.string.create_edit_interval_add_title),
    saveCta: String = stringResource(timerR.string.create_edit_interval_save_cta),
    onBack: () -> Unit = {},
    onSaved: (IntervalBlockDraft) -> Unit = {},
) {
    val state by vm.state.collectAsState(initial = CreateEditIntervalViewModel.UiState())

    LaunchedEffect(Unit) {
        vm.events.collect { e ->
            when (e) {
                CreateEditIntervalViewModel.Event.Back -> onBack()
                is CreateEditIntervalViewModel.Event.Saved -> onSaved(e.block)
                is CreateEditIntervalViewModel.Event.OpenPicker -> Unit
                CreateEditIntervalViewModel.Event.More -> Unit
            }
        }
    }

    CreateEditIntervalScreen(
        title = title,
        state = state,
        onBackClicked = vm::onBackClicked,
        onMoreClicked = vm::onMoreClicked,

        onNameChanged = vm::onNameChanged,

        onSetsMinus = vm::onSetsMinus,
        onSetsPlus = vm::onSetsPlus,
        onWorkMinus = vm::onWorkMinus,
        onWorkPlus = vm::onWorkPlus,
        onRestMinus = vm::onRestMinus,
        onRestPlus = vm::onRestPlus,

        onLastRestMinus = vm::onLastRestMinus,
        onLastRestPlus = vm::onLastRestPlus,

        onSetPillClicked = { vm.onPillClicked(CreateEditIntervalViewModel.PillTarget.Sets) },
        onWorkPillClicked = { vm.onPillClicked(CreateEditIntervalViewModel.PillTarget.Work) },
        onRestPillClicked = { vm.onPillClicked(CreateEditIntervalViewModel.PillTarget.Rest) },
        onLastRestPillClicked = { vm.onPillClicked(CreateEditIntervalViewModel.PillTarget.LastRest) },

        onSaveClicked = vm::onSaveClicked,
        saveCta = saveCta,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEditIntervalScreen(
    title: String,
    state: CreateEditIntervalViewModel.UiState,

    onBackClicked: () -> Unit,
    onMoreClicked: () -> Unit,

    onNameChanged: (String) -> Unit,

    onSetsMinus: () -> Unit,
    onSetsPlus: () -> Unit,
    onWorkMinus: () -> Unit,
    onWorkPlus: () -> Unit,
    onRestMinus: () -> Unit,
    onRestPlus: () -> Unit,
    onLastRestMinus: () -> Unit,
    onLastRestPlus: () -> Unit,

    onSetPillClicked: () -> Unit,
    onWorkPillClicked: () -> Unit,
    onRestPillClicked: () -> Unit,
    onLastRestPillClicked: () -> Unit,

    onSaveClicked: () -> Unit,
    saveCta: String,
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
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            painterResource(R.drawable.ic_navigate_before),
                            contentDescription = stringResource(timerR.string.back)
                        )
                    }
                },
                actions = {
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
                SectionTitle(stringResource(timerR.string.set_name))
                SurfaceCard(shape = RoundedCornerShape(12.dp)) {
                    Box(Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
                        val cs = MaterialTheme.colorScheme
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = onNameChanged,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = {
                                Text(
                                    text = stringResource(timerR.string.name_placeholder),
                                    color = cs.onSurface.copy(alpha = 0.45f),
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = cs.surface,
                                unfocusedContainerColor = cs.surface,
                                disabledContainerColor = cs.surface,
                                focusedBorderColor = cs.primary.copy(alpha = 0.22f),
                                unfocusedBorderColor = cs.outline.copy(alpha = 0.35f),
                                cursorColor = cs.primary,
                                focusedTextColor = cs.onSurface,
                                unfocusedTextColor = cs.onSurface,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                SectionTitle(stringResource(timerR.string.structure))
                SurfaceCard(shape = RoundedCornerShape(12.dp)) {
                    RowItem(
                        label = stringResource(timerR.string.sets),
                        text = state.sets.toString(),
                        value = state.sets,
                        onMinus = onSetsMinus,
                        onPlus = onSetsPlus,
                        onPillClick = onSetPillClicked
                    )
                }

                Spacer(Modifier.height(18.dp))

                SectionTitle(stringResource(timerR.string.timing))
                SurfaceCard(shape = RoundedCornerShape(12.dp)) {
                    RowItem(
                        label = stringResource(timerR.string.work),
                        text = formatSec(state.workSec),
                        value = state.workSec,
                        onMinus = onWorkMinus,
                        onPlus = onWorkPlus,
                        onPillClick = onWorkPillClicked
                    )
                    Divider(color = outline)
                    RowItem(
                        label = stringResource(timerR.string.rest),
                        text = formatSec(state.restSec),
                        value = state.restSec,
                        onMinus = onRestMinus,
                        onPlus = onRestPlus,
                        onPillClick = onRestPillClicked
                    )

                    if (state.isLastRestVisible) {
                        Divider(color = outline)
                        RowItem(
                            label = stringResource(timerR.string.last_rest),
                            text = formatSec(state.lastRestSec),
                            value = state.lastRestSec,
                            onMinus = onLastRestMinus,
                            onPlus = onLastRestPlus,
                            onPillClick = onLastRestPillClicked
                        )
                    }
                }
            }
        }

        BottomBar(
            totalTime = state.totalDurationSec,
            onSaveClicked = onSaveClicked,
            modifier = Modifier.align(Alignment.BottomCenter),
            saveCta = saveCta,
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
                contentDescription = stringResource(timerR.string.decrease_label, label),
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
                contentDescription = stringResource(com.oho.utils.R.string.increase_label, label),
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
        shape = RoundedCornerShape(14.dp),
        color = pillBg,
        border = BorderStroke(1.dp, pillBorder),
    ) {
        Box(modifier = Modifier.clickable(onClick = onPillClick)) {
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
    onSaveClicked: () -> Unit,
    saveCta: String,
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
                .padding(horizontal = 24.dp, vertical = 18.dp)
                .verticalScroll(rememberScrollState()),
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
                onClick = onSaveClicked,
                text = saveCta,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            )
        }
    }
}