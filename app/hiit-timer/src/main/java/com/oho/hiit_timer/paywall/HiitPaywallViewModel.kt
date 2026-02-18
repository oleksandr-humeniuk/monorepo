package com.oho.hiit_timer.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.oho.billing_client.contract.ActivityProvider
import com.oho.billing_client.contract.FarmBillingClient
import com.oho.billing_client.contract.OfferSelection
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Paywall VM:
 * - Deterministic state machine: Loading -> Ready (or Error)
 * - Plan selection is local-only and always consistent with available products.
 * - No UI-driven business logic: UI only renders UiState.
 */
class HiitPaywallViewModel(
    private val billing: FarmBillingClient,
) : ViewModel() {

    companion object {
        const val MONTHLY_ID = "hiit_monthly_sub"
        const val YEARLY_ID = "hiit_yearly_sub"
    }

    private val _state = MutableStateFlow(HiitPaywallUiState())
    val state: StateFlow<HiitPaywallUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<HiitPaywallNavEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<HiitPaywallNavEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            billing.start()
            launch { billing.refreshProducts(setOf(MONTHLY_ID, YEARLY_ID)) }
            launch { billing.restorePurchases() }
        }

        viewModelScope.launch {
            combine(billing.productsState, billing.entitlements) { productsState, entitlements ->
                val products = buildProducts(productsState.products)
                val phase = when {
                    entitlements.hasActiveSubscription() -> PaywallUiPhase.Completed
                    productsState.isRefreshing || products.isEmpty() -> PaywallUiPhase.Loading
                    else -> PaywallUiPhase.Ready
                }
                val current = _state.value
                current.copy(
                    phase = phase,
                    products = products,
                    isBusy = if (entitlements.hasActiveSubscription()) false else current.isBusy,
                    errorMessage = if (entitlements.hasActiveSubscription()) null else current.errorMessage,
                )
            }.collect { _state.value = it }
        }
    }

    fun onSelectPlan(plan: PaywallPlan) {
        _state.update { it.copy(selectedPlan = plan) }
    }

    fun onCtaClicked(activityProvider: ActivityProvider) {
        val current = _state.value
        if (current.isBusy || current.phase == PaywallUiPhase.Loading) return

        val productId = when (current.selectedPlan) {
            PaywallPlan.Monthly -> MONTHLY_ID
            PaywallPlan.Yearly -> YEARLY_ID
        }

        viewModelScope.launch {
            _state.update { it.copy(isBusy = true, errorMessage = null) }
            billing.launchSubscriptionPurchase(
                activityProvider = activityProvider,
                productId = productId,
                offerSelection = OfferSelection.BestPrice,
            ).onFailure {
                _state.update { it.copy(isBusy = false, errorMessage = "Purchase failed. Try again.") }
            }.onSuccess {
                _state.update { it.copy(isBusy = false) }
            }
        }
    }

    fun onRestoreClicked() {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true, errorMessage = null) }
            billing.restorePurchases()
                .onFailure { _state.update { it.copy(isBusy = false, errorMessage = "Restore failed. Try again.") } }
                .onSuccess { _state.update { it.copy(isBusy = false) } }
        }
    }

    fun onClose() {
        viewModelScope.launch { _events.emit(HiitPaywallNavEvent.Close) }
    }

    fun onOpenTerms() {
        viewModelScope.launch { _events.emit(HiitPaywallNavEvent.OpenTerms) }
    }

    fun onOpenPrivacy() {
        viewModelScope.launch { _events.emit(HiitPaywallNavEvent.OpenPrivacy) }
    }

    // -------------------------
    // Product mapping
    // -------------------------

    private fun buildProducts(details: Map<String, ProductDetails>): Map<PaywallPlan, PaywallProduct> {
        val result = mutableMapOf<PaywallPlan, PaywallProduct>()

        val monthlyPhase = details[MONTHLY_ID]?.recurringPhase()
        val yearlyPhase = details[YEARLY_ID]?.recurringPhase()

        if (monthlyPhase != null) {
            result[PaywallPlan.Monthly] = PaywallProduct(
                plan = PaywallPlan.Monthly,
                priceText = "${monthlyPhase.formattedPrice} / month",
                secondaryText = "Flexible access",
                isBestValue = false,
            )
        }

        if (yearlyPhase != null) {
            result[PaywallPlan.Yearly] = PaywallProduct(
                plan = PaywallPlan.Yearly,
                priceText = "${yearlyPhase.formattedPrice} / year",
                secondaryText = computeSavings(monthlyPhase?.priceAmountMicros, yearlyPhase.priceAmountMicros)
                    ?: "Best value",
                isBestValue = true,
            )
        }

        return result
    }

    /** Returns the last (recurring) pricing phase from the first available offer. */
    private fun ProductDetails.recurringPhase(): ProductDetails.PricingPhase? =
        subscriptionOfferDetails
            ?.firstOrNull()
            ?.pricingPhases
            ?.pricingPhaseList
            ?.lastOrNull()

    /** Calculates the percentage saved on yearly vs paying monthly for 12 months. */
    private fun computeSavings(monthlyMicros: Long?, yearlyMicros: Long): String? {
        if (monthlyMicros == null || monthlyMicros == 0L) return null
        val annualEquiv = monthlyMicros * 12
        val saved = annualEquiv - yearlyMicros
        if (saved <= 0) return null
        val pct = (saved * 100L / annualEquiv).toInt()
        return "Save $pct%"
    }
}

sealed interface HiitPaywallNavEvent {
    data object Close : HiitPaywallNavEvent
    data object OpenTerms : HiitPaywallNavEvent
    data object OpenPrivacy : HiitPaywallNavEvent
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
