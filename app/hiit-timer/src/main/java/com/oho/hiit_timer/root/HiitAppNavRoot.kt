package com.oho.hiit_timer.root

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.oho.hiit_timer.count_down_screen.HiitRunRoute
import com.oho.hiit_timer.paywall.HiitPaywallRoute
import com.oho.hiit_timer.settings.SoundSettingsRoute
import com.oho.hiit_timer.tabs.HiitTabHost
import com.oho.hiit_timer.workouts.add.CreateEditWorkoutRoute
import com.oho.hiit_timer.workouts.add_block.CreateEditIntrervalRoute
import com.oho.hiit_timer.workouts.workout_details.WorkoutDetailsRoute
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiitAppNavRoot(
    viewModel: HiitRootNavViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.bootstrapBilling()
    }

    // Permission launcher — proceed with the run regardless of grant result
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { viewModel.proceedWithPendingRun() }

    // Whenever a run is requested, check permission first
    LaunchedEffect(state.pendingRunWorkoutId) {
        state.pendingRunWorkoutId ?: return@LaunchedEffect
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) !=
                PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            viewModel.onPermissionSheetRequired()
        } else {
            viewModel.proceedWithPendingRun()
        }
    }

    if (state.showNotificationPermissionSheet) {
        NotificationPermissionSheet(
            onAllow = {
                viewModel.onPermissionSheetDismissed()
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            },
            onNotNow = { viewModel.proceedWithPendingRun() },
        )
    }

    NavDisplay(
        backStack = state.backStack,
        onBack = { viewModel.onBack() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = { key ->
            when (key) {
                is HiitRootRoute.Run -> NavEntry(key) {
                    HiitRunRoute(
                        workoutId = key.workoutId,
                        onBack = { viewModel.onBack() },
                    )
                }

                HiitRootRoute.Tabs -> NavEntry(key) {
                    HiitTabHost(
                        isPro = isPro,
                        runWorkout = { viewModel.requestRunWorkout(it) },
                        createWorkout = { viewModel.createWorkout() },
                        openDetails = { viewModel.openWorkoutDetails(it) },
                        openSoundSettings = { viewModel.openSoundSettings() },
                        openPaywall = { viewModel.openPaywall() },
                    )
                }

                is HiitRootRoute.CreateEditWorkout -> NavEntry(key) {
                    CreateEditWorkoutRoute(
                        workoutId = key.workoutId,
                        onBack = { viewModel.onBack() },
                        onAddBlock = { viewModel.onAddBlock() },
                        onEditExercise = { viewModel.onEditExercise(exerciseId = it) },
                    )
                }

                is HiitRootRoute.AddEditExercise -> NavEntry(key) {
                    CreateEditIntrervalRoute(
                        exerciseId = key.id,
                        onBack = { viewModel.onBack() },
                        onSaved = { viewModel.onBack() },
                    )
                }

                is HiitRootRoute.WorkoutDetails -> NavEntry(key) {
                    WorkoutDetailsRoute(
                        workoutId = key.id,
                        onBack = { viewModel.onBack() },
                        onStartWorkout = { viewModel.requestRunWorkout(it) },
                        onEditWorkout = { viewModel.onEditWorkout(workoutId = it) },
                        onDuplicateWorkout = { viewModel.openDuplicatedWorkout() },
                    )
                }

                HiitRootRoute.SoundSettings -> NavEntry(key) {
                    SoundSettingsRoute(
                        onBack = { viewModel.onBack() },
                        onPickSound = {},
                    )
                }

                HiitRootRoute.Paywall -> NavEntry(key) {
                    HiitPaywallRoute(
                        onClose = { viewModel.onBack() },
                    )
                }
            }
        },
    )
}
