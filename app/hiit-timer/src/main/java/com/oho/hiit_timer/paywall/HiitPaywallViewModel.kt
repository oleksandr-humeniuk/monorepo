package com.oho.hiit_timer.paywall

import androidx.lifecycle.ViewModel

/**
 * Paywall VM:
 * - Deterministic state machine: Loading -> Ready (or Error)
 * - Plan selection is local-only and always consistent with available products.
 * - No UI-driven business logic: UI only renders UiState.
 */
class HiitPaywallViewModel(
) : ViewModel() {


}


data class HiitPaywallUiState(
    val phase: PaywallUiPhase = PaywallUiPhase.Loading,
    val products: Map<PaywallPlan, PaywallProduct> = emptyMap(),
    val selectedPlan: PaywallPlan = PaywallPlan.Yearly,
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
)

enum class PaywallUiPhase { Loading, Ready, Completed }

enum class PaywallPlan { Monthly, Yearly }

data class PaywallProduct(
    val plan: PaywallPlan,
    val priceText: String,      // "$2 / month"
    val secondaryText: String?, // "Flexible access" / "Save 67%"
    val isBestValue: Boolean,
)
