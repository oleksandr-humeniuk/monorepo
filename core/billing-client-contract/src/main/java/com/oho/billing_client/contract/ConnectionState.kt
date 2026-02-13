package com.oho.billing_client.contract

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

sealed interface ConnectionState {
    data object Stopped : ConnectionState
    data object Connecting : ConnectionState
    data object Connected : ConnectionState
    data class Disconnected(val reason: DisconnectReason) : ConnectionState
}

enum class DisconnectReason {
    ServiceDisconnected,
    ServiceUnavailable,
    BillingUnavailable,
    DeveloperError,
    Unknown,
}

data class ProductsState(
    val requestedIds: Set<String> = emptySet(),
    val products: Map<String, ProductDetails> = emptyMap(),
    val lastError: BillingFailure? = null,
    val isRefreshing: Boolean = false,
)

data class EntitlementsState(
    /** Active subscription purchases in local cache. */
    val activeSubscriptions: List<Purchase> = emptyList(),
    val acknowledgedTokens: Set<String> = emptySet(),
    val lastSyncError: BillingFailure? = null,
    val isSyncing: Boolean = false,
) {
    fun hasActiveSubscription(): Boolean =
        activeSubscriptions.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
}

sealed interface BillingEvent {
    data class Log(val message: String) : BillingEvent
    data class PurchaseFlowLaunched(val productId: String) : BillingEvent
    data class PurchasesUpdated(val purchases: List<Purchase>) : BillingEvent
    data class PurchaseAcknowledged(val purchaseToken: String) : BillingEvent
    data class RestoreCompleted(val purchases: List<Purchase>) : BillingEvent
    data class Failure(val failure: BillingFailure) : BillingEvent
}

/** Offer selection policy for subscription purchase. */
sealed interface OfferSelection {
    /** Pick the offer with the lowest formatted price among eligible offers. */
    data object BestPrice : OfferSelection

    /** Prefer offers that contain this tag (Play Console offer tags). */
    enum class PreferTag(
        val tag: String
    ) : OfferSelection {
        Default("default"),
        Promo(tag = "promo"),
        Intro(tag = "intro")
    }

    /** Use an explicit offer token (advanced / deterministic selection). */
    data class ExactOfferToken(val offerToken: String) : OfferSelection
}

data class BillingFailure(
    val where: String,
    val code: Int? = null,
    val debugMessage: String? = null,
    val throwable: Throwable? = null,
)

/**
 * UI/host should provide current Activity to launch billing flow.
 * Avoids coupling billing module to your navigation stack.
 */
fun interface ActivityProvider {
    fun requireActivity(): android.app.Activity
}
