package com.oho.hiit_timer.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oho.billing_client.contract.ActivityProvider
import com.oho.billing_client.contract.FarmBillingClient
import com.oho.billing_client.contract.OfferSelection
import kotlinx.coroutines.launch

class PaywallViewModel(
    private val billing: FarmBillingClient,
) : ViewModel() {

    val entitlements = billing.entitlements
    val products = billing.productsState
    val connection = billing.connectionState

    init {
        viewModelScope.launch {
            billing.start()
            billing.refreshProducts(setOf("mono_hiit_weekly", "mono_hiit_monthly"))
            billing.restorePurchases()
        }
    }

    fun onSubscribeClick(activityProvider: ActivityProvider, productId: String) {
        viewModelScope.launch {
            billing.launchSubscriptionPurchase(
                activityProvider = activityProvider,
                productId = productId,
                offerSelection = OfferSelection.PreferTag.Default,
                obfuscatedAccountId = null,
                obfuscatedProfileId = null,
            )
        }
    }

    override fun onCleared() {
        // Optional: keep billing alive at app scope instead.
        super.onCleared()
    }
}