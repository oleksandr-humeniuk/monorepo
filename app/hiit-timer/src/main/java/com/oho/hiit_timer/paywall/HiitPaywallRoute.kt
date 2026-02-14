package com.oho.hiit_timer.paywall

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HiitPaywallRoute(
    onClose: () -> Unit,
) {
//    val vm: HiitPaywallViewModel = viewModel()
//    val state = vm.state.collectAsStateWithLifecycle()
//
//    HiitPaywallScreen(
//        state = state.value,
//        onClose = { vm.onClose(); onClose() },
//        onSelectPlan = vm::onSelectPlan,
//        onCta = vm::onCtaClicked,
//        onRestore = vm::onRestoreClicked,
//        onOpenTerms = vm::onOpenTerms,
//        onOpenPrivacy = vm::onOpenPrivacy,
//    )
}
//TODO: wheel picker https://github.com/commandiron/WheelPickerCompose