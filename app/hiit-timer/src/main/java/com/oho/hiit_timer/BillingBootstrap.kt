package com.oho.hiit_timer

import com.oho.billing_client.contract.FarmBillingClient
import com.oho.hiit_timer.paywall.HiitPaywallViewModel

class BillingBootstrap(
    private val billing: FarmBillingClient,
) {
    suspend fun onAppStart() {
        billing.start()

        if (!fetchedPerSession) {
            billing.restorePurchases()
            billing.refreshProducts(
                setOf(
                    HiitPaywallViewModel.YEARLY_ID,
                    HiitPaywallViewModel.MONTHLY_ID
                )
            )
            fetchedPerSession = true
        }
    }

    companion object {
        @Volatile
        private var fetchedPerSession = false
    }
}