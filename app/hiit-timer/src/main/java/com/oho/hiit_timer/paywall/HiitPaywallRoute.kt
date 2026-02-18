package com.oho.hiit_timer.paywall

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oho.billing_client.contract.ActivityProvider
import org.koin.androidx.compose.koinViewModel

@Composable
fun HiitPaywallRoute(
    onClose: () -> Unit,
) {
    val vm: HiitPaywallViewModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                HiitPaywallNavEvent.Close -> onClose()
                HiitPaywallNavEvent.OpenTerms -> { /* TODO: open terms URL */ }
                HiitPaywallNavEvent.OpenPrivacy -> { /* TODO: open privacy URL */ }
            }
        }
    }

    HiitPaywallScreen(
        state = state,
        onClose = vm::onClose,
        onSelectPlan = vm::onSelectPlan,
        onCta = { vm.onCtaClicked(ActivityProvider { context as Activity }) },
        onRestore = vm::onRestoreClicked,
        onOpenTerms = vm::onOpenTerms,
        onOpenPrivacy = vm::onOpenPrivacy,
    )
}
