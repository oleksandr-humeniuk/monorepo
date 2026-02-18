package com.oho.hiit_timer.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.oho.hiit_timer.data.store.Sound
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oho.core.ui.R
import com.oho.core.ui.components.MonoIcon
import com.oho.core.ui.components.MonoScaffold
import com.oho.core.ui.theme.MonoTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun SoundSettingsRoute(
    onBack: () -> Unit,
    onPickSound: (SoundKind) -> Unit,
    vm: SoundSettingsViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.events.collect { e ->
            when (e) {
                SoundNavEvent.Back -> onBack()
                is SoundNavEvent.PickSound -> onPickSound(e.kind)
            }
        }
    }

    val content = state as? SoundSettingsViewModel.UiState.Content

    MonoScaffold(Modifier.Companion.fillMaxSize()) {
        if (content != null) {
            SoundSettingsScreen(
                state = content,
                onBack = vm::onBack,
                onToggleSound = vm::onToggleSoundEnabled,
                onVolumeChange = vm::onVolumeChange,
                onToggleVibration = vm::onToggleVibration,
                onPickWork = { vm.onPickSound(SoundKind.Work) },
                onPickRest = { vm.onPickSound(SoundKind.Rest) },
                onPickDone = { vm.onPickSound(SoundKind.Done) },
            )
        }
    }
}


private fun Sound.toDisplayName(): String =
    name.replace(Regex("(?<=[a-z])(?=[A-Z])"), " ")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SoundSettingsScreen(
    state: SoundSettingsViewModel.UiState.Content,
    onBack: () -> Unit,
    onToggleSound: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleVibration: () -> Unit,
    onPickWork: () -> Unit,
    onPickRest: () -> Unit,
    onPickDone: () -> Unit,
) {
    val c = MonoTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.appBackground)
    ) {
        Column(Modifier.fillMaxSize()) {

            CenterAlignedTopAppBar(
                title = { Text("Sound & vibration") },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        MonoIcon(
                            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_navigate_before),
                            contentDescription = "Back",
                            tint = c.primaryIconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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

                SectionHeader("Sound")
                SettingsCard {
                    RowToggle(
                        title = "Sound enabled",
                        checked = state.soundEnabled,
                        onToggle = onToggleSound,
                    )

                    AnimatedVisibility(
                        visible = state.soundEnabled,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Column {
                            RowMonoSlider(
                                title = "Volume",
                                value = state.volume,
                                onValueChange = onVolumeChange,
                            )
                            RowNav("Work sound", value = state.workSound.toDisplayName(), onClick = onPickWork)
                            RowNav("Rest sound", value = state.restSound.toDisplayName(), onClick = onPickRest)
                            RowNav(
                                "Done sound",
                                value = state.doneSound.toDisplayName(),
                                onClick = onPickDone,
                                isLast = true
                            )
                        }
                    }

                    if (!state.soundEnabled) {
                        // keep divider consistency: end card clean
                        DividerLine(isLast = true)
                    }
                }

                SectionHeader("Vibration")
                SettingsCard {
                    RowToggle(
                        title = "Vibration",
                        checked = state.vibrationEnabled,
                        onToggle = onToggleVibration,
                        isLast = true,
                    )
                }
            }
        }
    }
}