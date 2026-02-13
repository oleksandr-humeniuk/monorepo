package com.oho.billing_client.contract

import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

class PlayFarmBillingClient(
    appContext: Context,
    private val externalScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FarmBillingClient {

    private val context = appContext.applicationContext

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Stopped)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _productsState = MutableStateFlow(ProductsState())
    override val productsState: StateFlow<ProductsState> = _productsState.asStateFlow()

    private val _entitlements = MutableStateFlow(EntitlementsState())
    override val entitlements: StateFlow<EntitlementsState> = _entitlements.asStateFlow()

    private val _events = MutableSharedFlow<BillingEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val events: SharedFlow<BillingEvent> = _events.asSharedFlow()

    private val started = AtomicBoolean(false)
    private val connectionMutex = Mutex()
    private val refreshMutex = Mutex()

    private var billingClient: BillingClient? = null
    private var reconnectJob: Job? = null

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        externalScope.launch(dispatcher) {
            if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                _events.emit(BillingEvent.PurchasesUpdated(purchases))
                handlePurchases(purchases)
            } else {
                emitFailure(
                    where = "PurchasesUpdatedListener",
                    billingResult = result,
                    extra = "purchases=${purchases?.size ?: 0}"
                )
            }
        }
    }

    override suspend fun start() {
        if (!started.compareAndSet(false, true)) return
        ensureClient()
        connectWithBackoff()
    }

    override suspend fun stop() {
        started.set(false)
        reconnectJob?.cancel()
        reconnectJob = null

        connectionMutex.withLock {
            _connectionState.value = ConnectionState.Stopped
            billingClient?.endConnection()
            billingClient = null
        }
    }

    override suspend fun refreshProducts(productIds: Set<String>): Result<Unit> {
        if (productIds.isEmpty()) return Result.success(Unit)

        return refreshMutex.withLock {
            _productsState.update {
                it.copy(
                    isRefreshing = true,
                    requestedIds = productIds,
                    lastError = null
                )
            }

            ensureConnectedOrFail("refreshProducts")?.let { failure ->
                _productsState.update { it.copy(isRefreshing = false, lastError = failure) }
                _events.emit(BillingEvent.Failure(failure))
                return@withLock Result.failure(IllegalStateException(failure.where + ": " + failure.debugMessage))
            }

            val client = billingClient ?: run {
                val f =
                    BillingFailure(where = "refreshProducts", debugMessage = "billingClient=null")
                _productsState.update { it.copy(isRefreshing = false, lastError = f) }
                _events.emit(BillingEvent.Failure(f))
                return@withLock Result.failure(IllegalStateException("billingClient=null"))
            }

            val queryList = productIds.map { id ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(id)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            }

            suspendCancellableCoroutine { cont ->
                val params = QueryProductDetailsParams.newBuilder()
                    .setProductList(queryList)
                    .build()

                client.queryProductDetailsAsync(params) { billingResult, queryResult ->
                    val fetched = queryResult.productDetailsList
                    val unfetched = queryResult.unfetchedProductList

                    val map = fetched.associateBy { it.productId }

                    _productsState.update {
                        it.copy(
                            isRefreshing = false,
                            products = map,
                            lastError = if (unfetched.isNotEmpty())
                                BillingFailure(
                                    where = "queryProductDetailsAsync",
                                    code = billingResult.responseCode,
                                    debugMessage = "Unfetched: " + unfetched.joinToString { u ->
                                        "${u.productId}:${u.statusCode}"
                                    }
                                )
                            else null
                        )
                    }

                    if (unfetched.isNotEmpty()) {
                        externalScope.launch(dispatcher) {
                            _events.emit(
                                BillingEvent.Log(
                                    "Unfetched products: " + unfetched.joinToString { "${it.productId}:${it.statusCode}" }
                                )
                            )
                        }
                    }
                }

            }
        }
    }

    override suspend fun launchSubscriptionPurchase(
        activityProvider: ActivityProvider,
        productId: String,
        offerSelection: OfferSelection,
        obfuscatedAccountId: String?,
        obfuscatedProfileId: String?,
    ): Result<Unit> {
        ensureConnectedOrFail("launchSubscriptionPurchase")?.let { failure ->
            _events.emit(BillingEvent.Failure(failure))
            return Result.failure(IllegalStateException(failure.debugMessage ?: "Not connected"))
        }

        val details = productsState.value.products[productId]
            ?: return Result.failure(IllegalArgumentException("ProductDetails not loaded for productId=$productId"))

        val offerToken = pickOfferToken(details, offerSelection)
            ?: return Result.failure(IllegalStateException("No eligible subscription offer for $productId"))

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()

        val builder = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))

        if (!obfuscatedAccountId.isNullOrBlank()) builder.setObfuscatedAccountId(obfuscatedAccountId)
        if (!obfuscatedProfileId.isNullOrBlank()) builder.setObfuscatedProfileId(obfuscatedProfileId)

        val client =
            billingClient ?: return Result.failure(IllegalStateException("billingClient=null"))
        val activity = activityProvider.requireActivity()

        val result = client.launchBillingFlow(activity, builder.build())
        return if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            _events.emit(BillingEvent.PurchaseFlowLaunched(productId))
            Result.success(Unit)
        } else {
            val failure = result.toFailure(where = "launchBillingFlow")
            _events.emit(BillingEvent.Failure(failure))
            Result.failure(
                IllegalStateException(
                    failure.debugMessage ?: "launchBillingFlow failed"
                )
            )
        }
    }

    override suspend fun restorePurchases(): Result<Unit> {
        ensureConnectedOrFail("restorePurchases")?.let { failure ->
            _events.emit(BillingEvent.Failure(failure))
            return Result.failure(IllegalStateException(failure.debugMessage ?: "Not connected"))
        }

        val client =
            billingClient ?: return Result.failure(IllegalStateException("billingClient=null"))

        _entitlements.update { it.copy(isSyncing = true, lastSyncError = null) }

        return suspendCancellableCoroutine { cont ->
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            client.queryPurchasesAsync(params) { result, purchases ->
                externalScope.launch(dispatcher) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        _events.emit(BillingEvent.RestoreCompleted(purchases))
                        handlePurchases(purchases)
                        _entitlements.update { it.copy(isSyncing = false, lastSyncError = null) }
                        cont.resume(Result.success(Unit)) { cause, _, _ -> }
                    } else {
                        val failure = result.toFailure(where = "queryPurchasesAsync(SUBS)")
                        _entitlements.update { it.copy(isSyncing = false, lastSyncError = failure) }
                        _events.emit(BillingEvent.Failure(failure))
                        cont.resume(
                            Result.failure(
                                IllegalStateException(
                                    failure.debugMessage ?: "restore failed"
                                )
                            )
                        ) { cause, _, _ -> }
                    }
                }
            }
        }
    }

    // ----------------------------
    // Internal mechanics
    // ----------------------------

    private fun ensureClient() {
        if (billingClient != null) return
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .build()
    }

    private suspend fun connectWithBackoff() {
        reconnectJob?.cancel()
        reconnectJob = externalScope.launch(dispatcher) {
            var attempt = 0
            while (started.get()) {
                val ok = connectOnce()
                if (ok) {
                    // Optional: do an initial restore to warm entitlements
                    restorePurchases()
                    return@launch
                }
                attempt++
                val delaySec = min(60, (1 shl min(attempt, 6))) // 2,4,8,16,32,64 -> capped
                _events.emit(BillingEvent.Log("Billing connect retry in ${delaySec}s (attempt=$attempt)"))
                delay(delaySec.seconds)
            }
        }
    }

    private suspend fun connectOnce(): Boolean = connectionMutex.withLock {
        if (!started.get()) return false
        ensureClient()

        val client = billingClient ?: return false
        _connectionState.value = ConnectionState.Connecting

        return suspendCancellableCoroutine { cont ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    externalScope.launch(dispatcher) {
                        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                            _connectionState.value = ConnectionState.Connected
                            _events.emit(BillingEvent.Log("Billing connected"))
                            cont.resume(true) { cause, _, _ -> }
                        } else {
                            val failure = result.toFailure(where = "onBillingSetupFinished")
                            _connectionState.value =
                                ConnectionState.Disconnected(result.toDisconnectReason())
                            _events.emit(BillingEvent.Failure(failure))
                            cont.resume(false) { cause, _, _ -> }
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    externalScope.launch(dispatcher) {
                        _connectionState.value =
                            ConnectionState.Disconnected(DisconnectReason.ServiceDisconnected)
                        _events.emit(BillingEvent.Log("Billing service disconnected"))
                        // Trigger reconnect loop:
                        if (started.get()) connectWithBackoff()
                    }
                }
            })
        }
    }

    /**
     * Returns failure if not connected and cannot connect immediately.
     * We do NOT block forever here; caller can decide to retry.
     */
    private suspend fun ensureConnectedOrFail(where: String): BillingFailure? {
        return when (connectionState.value) {
            ConnectionState.Connected -> null
            ConnectionState.Connecting -> BillingFailure(where, debugMessage = "Still connecting")
            ConnectionState.Stopped -> BillingFailure(where, debugMessage = "Client not started")
            is ConnectionState.Disconnected -> BillingFailure(where, debugMessage = "Disconnected")
        }
    }

    private suspend fun handlePurchases(purchases: List<Purchase>) {
        // Keep only purchased or pending (pending affects UI/analytics).
        val purchased = purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        val acknowledgedTokens = mutableSetOf<String>()

        _entitlements.update {
            it.copy(
                activeSubscriptions = purchases,
                acknowledgedTokens = it.acknowledgedTokens, // updated below
                lastSyncError = null,
            )
        }

        // Acknowledge all PURCHASED but not acknowledged.
        for (p in purchased) {
            if (!p.isAcknowledged) {
                val token = p.purchaseToken
                val ok = acknowledge(token)
                if (ok) acknowledgedTokens += token
            } else {
                acknowledgedTokens += p.purchaseToken
            }
        }

        _entitlements.update { it.copy(acknowledgedTokens = it.acknowledgedTokens + acknowledgedTokens) }
    }

    private suspend fun acknowledge(purchaseToken: String): Boolean {
        val client = billingClient ?: return false
        return suspendCancellableCoroutine { cont ->
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()

            client.acknowledgePurchase(params) { result ->
                externalScope.launch(dispatcher) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        _events.emit(BillingEvent.PurchaseAcknowledged(purchaseToken))
                        cont.resume(true) { cause, _, _ -> }
                    } else {
                        val failure = result.toFailure(where = "acknowledgePurchase")
                        _events.emit(BillingEvent.Failure(failure))
                        cont.resume(false) { cause, _, _ -> }
                    }
                }
            }
        }
    }

    private fun pickOfferToken(details: ProductDetails, selection: OfferSelection): String? {
        val offers = details.subscriptionOfferDetails.orEmpty()
        if (offers.isEmpty()) return null

        return when (selection) {
            OfferSelection.BestPrice -> {
                // Pick by minimal price across pricing phases (simple heuristic).
                offers.minByOrNull { offer ->
                    offer.pricingPhases.pricingPhaseList.minOfOrNull { it.priceAmountMicros }
                        ?: Long.MAX_VALUE
                }?.offerToken
            }

            is OfferSelection.PreferTag -> {
                offers.firstOrNull { it.offerTags.contains(selection.tag) }?.offerToken
                    ?: offers.firstOrNull()?.offerToken
            }

            is OfferSelection.ExactOfferToken -> {
                offers.firstOrNull { it.offerToken == selection.offerToken }?.offerToken
            }
        }
    }

    private suspend fun emitFailure(
        where: String,
        billingResult: BillingResult,
        extra: String? = null
    ) {
        val failure = billingResult.toFailure(where = where, extra = extra)
        _events.emit(BillingEvent.Failure(failure))
    }
}

private fun BillingResult.toFailure(where: String, extra: String? = null): BillingFailure =
    BillingFailure(
        where = where,
        code = responseCode,
        debugMessage = listOfNotNull(debugMessage, extra).joinToString(" | ").ifBlank { null },
        throwable = null,
    )

private fun BillingResult.toDisconnectReason(): DisconnectReason = when (responseCode) {
    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> DisconnectReason.ServiceDisconnected
    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> DisconnectReason.ServiceUnavailable
    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> DisconnectReason.BillingUnavailable
    BillingClient.BillingResponseCode.DEVELOPER_ERROR -> DisconnectReason.DeveloperError
    else -> DisconnectReason.Unknown
}
