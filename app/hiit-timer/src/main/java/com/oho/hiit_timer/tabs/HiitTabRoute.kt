package com.oho.hiit_timer.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.oho.core.ui.R
import com.oho.core.ui.components.MonoIcon
import com.oho.core.ui.components.MonoScaffold
import com.oho.core.ui.theme.MonoTheme
import com.oho.hiit_timer.QuickStartTimerRoute
import com.oho.hiit_timer.workouts.list.WorkoutsRoute
import org.koin.androidx.compose.koinViewModel

sealed interface HiitTabRoute {
    data object Workouts : HiitTabRoute
    data object Quick : HiitTabRoute
    data object History : HiitTabRoute
    data object Challenges : HiitTabRoute
}


@Composable
fun HiitTabHost(
    openWorkout: (String) -> Unit,
    viewModel: TabsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MonoScaffold(
        bottomBar = {
            HiitBottomBar(
                tabs = state.tabs,
                selected = state.selectedTab,
                onSelected = viewModel::selectTab,
            )
        }
    ) { paddingValues ->

        NavDisplay(
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()),
            backStack = state.backStack,
            onBack = { viewModel.onBack() },
            entryProvider = { tab ->
                when (tab) {
                    HiitTabRoute.Quick -> NavEntry(tab) {
                        QuickStartTimerRoute(
                            openWorkout = { workoutId -> openWorkout(workoutId) }
                        )
                    }

                    HiitTabRoute.History -> NavEntry(tab) {
                        MockScreen("History")
                    }

                    HiitTabRoute.Challenges -> NavEntry(tab) {
                        MockScreen("Settings")
                    }

                    HiitTabRoute.Workouts -> NavEntry(tab) {
                        WorkoutsRoute()
                    }
                }
            },
        )
    }
}

@Composable
fun MockScreen(title: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(title)
    }
}

@Composable
fun HiitBottomBar(
    tabs: List<Tab>,
    selected: Tab,
    onSelected: (HiitTabRoute) -> Unit,
) {
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MonoTheme.colors.accentPrimary,     // 0xFF4C6FFF
        selectedTextColor = MonoTheme.colors.accentPrimary,
        unselectedIconColor = MonoTheme.colors.secondaryIconColor, // 0xFFA9B1BC
        unselectedTextColor = MonoTheme.colors.secondaryTextColor,
        indicatorColor = MonoTheme.colors.accentPrimary.copy(alpha = 0.0F),
        disabledIconColor = MonoTheme.colors.secondaryIconColor.copy(alpha = 0.35f),
        disabledTextColor = MonoTheme.colors.secondaryTextColor.copy(alpha = 0.35f),
    )
    val hapticController = LocalHapticFeedback.current


    NavigationBar(
        containerColor = MonoTheme.colors.cardBackground,          // dark(): 0xFF171B21
        contentColor = MonoTheme.colors.primaryTextColor,
        tonalElevation = NavigationBarDefaults.Elevation,
        windowInsets = WindowInsets.navigationBars,
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selected
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    hapticController.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    onSelected(tab.route)
                },
                icon = {
                    MonoIcon(
                        painter = painterResource(tab.iconRes),
                        contentDescription = tab.title,
                        tint = if (isSelected) {
                            MonoTheme.colors.accentIconColor
                        } else {
                            MonoTheme.colors.secondaryIconColor
                        }
                    )
                },
                label = { Text(tab.title) },
                colors = itemColors,
            )
        }
    }
}

private val Tab.iconRes
    get() = when (this.route) {
        HiitTabRoute.Challenges -> R.drawable.ic_emoji_events
        HiitTabRoute.History -> R.drawable.ic_history
        HiitTabRoute.Quick -> R.drawable.ic_timer
        HiitTabRoute.Workouts -> R.drawable.ic_fitness_center
    }

private val Tab.title
    get() = when (this.route) {
        HiitTabRoute.Challenges -> "Challenges"
        HiitTabRoute.History -> "History"
        HiitTabRoute.Quick -> "Quick"
        HiitTabRoute.Workouts -> "Workouts"
    }