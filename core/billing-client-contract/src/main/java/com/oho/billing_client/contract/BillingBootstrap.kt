package com.oho.billing_client.contract

class BillingBootstrap(
    private val billing: FarmBillingClient,
    private val productIdsProvider: () -> Set<String>,
//    private val sessionGate: SessionGate, // your datastore-backed "once per session"
) {
    suspend fun onAppStart() {
        billing.start()

//        // Restore once per session (or once per day).
//        if (sessionGate.shouldRestoreBilling()) {
//            billing.restorePurchases()
//            sessionGate.markBillingRestored()
//        }
//
//        // Optional preload only if your app can show paywall quickly:
//        // billing.refreshProducts(productIdsProvider())
    }
}
