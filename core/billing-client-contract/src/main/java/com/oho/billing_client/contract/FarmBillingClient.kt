package com.oho.billing_client.contract

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Universal billing client for Mono "farm base" apps.
 *
 * Subscriptions only for now.
 * - Exposes connection state, products cache, purchase events, and entitlements.
 * - Designed to be UI-agnostic and testable (replace implementation in tests).
 */
interface FarmBillingClient {

    /** Current connection state to Google Play Billing. */
    val connectionState: StateFlow<ConnectionState>

    /** Cached subscription products (queried from Play). */
    val productsState: StateFlow<ProductsState>

    /**
     * Purchase events stream (updates from Play + explicit restore results).
     * - Useful for UI notifications and analytics.
     */
    val events: SharedFlow<BillingEvent>

    /** Derived entitlement state from active purchases (local device view). */
    val entitlements: StateFlow<EntitlementsState>

    /**
     * Start/ensure connection. Safe to call multiple times.
     * Implementation may reconnect with backoff.
     */
    suspend fun start()

    /** Stop and release resources. */
    suspend fun stop()

    /**
     * Query Play for subscription ProductDetails for the given productIds.
     * Calling multiple times updates cache.
     */
    suspend fun refreshProducts(productIds: Set<String>): Result<Unit>

    /**
     * Launch purchase flow for a given subscription product.
     *
     * @param productId Play Console subscription product id.
     * @param offerSelection Optional rule to pick base plan / offer.
     */
    suspend fun launchSubscriptionPurchase(
        activityProvider: ActivityProvider,
        productId: String,
        offerSelection: OfferSelection = OfferSelection.BestPrice,
        obfuscatedAccountId: String? = null,
        obfuscatedProfileId: String? = null,
    ): Result<Unit>

    /**
     * Restore purchases (query active subscriptions) and update entitlements.
     * This does NOT consume or acknowledge; it only syncs local state.
     */
    suspend fun restorePurchases(): Result<Unit>
}

