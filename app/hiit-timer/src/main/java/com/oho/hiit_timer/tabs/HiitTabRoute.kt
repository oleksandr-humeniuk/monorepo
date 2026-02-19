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
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.oho.core.ui.R
import com.oho.core.ui.components.MonoIcon
import com.oho.core.ui.components.MonoScaffold
import com.oho.core.ui.theme.MonoTheme
import com.oho.hiit_timer.QuickStartTimerRoute
import com.oho.hiit_timer.settings.SettingsRoute
import com.oho.hiit_timer.workouts.list.WorkoutsRoute
import org.koin.androidx.compose.koinViewModel
import com.oho.utils.R as timerR

sealed interface HiitTabRoute {
    data object Workouts : HiitTabRoute
    data object Quick : HiitTabRoute
    data object Settings : HiitTabRoute
    data object Challenges : HiitTabRoute
}


@Composable
fun HiitTabHost(
    isPro: Boolean = false,
    runWorkout: (String) -> Unit,
    openDetails: (String) -> Unit,
    createWorkout: (String?) -> Unit,
    openSoundSettings: () -> Unit,
    openPaywall: () -> Unit,
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
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = { tab ->
                when (tab) {
                    HiitTabRoute.Quick -> NavEntry(tab) {
                        QuickStartTimerRoute(
                            isPro = isPro,
                            runWrokout = runWorkout,
                            onProClick = openPaywall,
                        )
                    }

                    HiitTabRoute.Settings -> NavEntry(tab) {
                        val context = LocalContext.current
                        SettingsRoute(
                            isPro = isPro,
                            onBack = {
                                viewModel.onBack()
                            },
                            onRateApp = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.aoh.hiit.tabata.timer"))
                                runCatching { context.startActivity(intent) }.onFailure {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.aoh.hiit.tabata.timer")))
                                }
                            },
                            onContactSupport = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:support@aohstd.com")
                                }
                                context.startActivity(intent)
                            },
                            onOpenSound = openSoundSettings,
                            onOpenPrivacyPolicy = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://aohstd.com/privacy")))
                            },
                            onOpenTerms = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://aohstd.com/terms")))
                            },
                            onProClick = openPaywall,
                            vm = koinViewModel(),
                        )
                    }

                    HiitTabRoute.Challenges -> NavEntry(tab) {
                    }

                    HiitTabRoute.Workouts -> NavEntry(tab) {
                        WorkoutsRoute(
                            isPro = isPro,
                            createEditWorkout = createWorkout,
                            runWrokout = runWorkout,
                            openWorkout = openDetails,
                            onProClick = openPaywall,
                        )
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
        HiitTabRoute.Settings -> R.drawable.ic_settings
        HiitTabRoute.Quick -> R.drawable.ic_timer
        HiitTabRoute.Workouts -> R.drawable.ic_fitness_center
    }

private val Tab.title
    @Composable
    get() = when (this.route) {
        HiitTabRoute.Challenges -> stringResource(timerR.string.tab_challenges)
        HiitTabRoute.Settings -> stringResource(timerR.string.tab_settings)
        HiitTabRoute.Quick -> stringResource(timerR.string.tab_quick)
        HiitTabRoute.Workouts -> stringResource(timerR.string.tab_workouts)
    }