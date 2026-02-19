package com.oho.hiit_timer.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.oho.billing_client.contract.ConnectionState
import com.oho.billing_client.contract.FarmBillingClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubscriptionRepository(
    private val billing: FarmBillingClient,
    private val store: DataStore<Preferences>,
    appScope: CoroutineScope,
) {
    private val isProCachedKey = booleanPreferencesKey("is_pro_cached")

    private val cachedIsPro: StateFlow<Boolean> = store.data
        .map { prefs -> prefs[isProCachedKey] ?: false }
        .stateIn(appScope, SharingStarted.Eagerly, false)

    private val billingIsSynced: StateFlow<Boolean> = combine(
        billing.connectionState,
        billing.entitlements,
    ) { connection, entitlements ->
        connection is ConnectionState.Connected && !entitlements.isSyncing
    }.stateIn(appScope, SharingStarted.Eagerly, false)

    private val billingIsPro: StateFlow<Boolean> = billing.entitlements
        .map { it.hasActiveSubscription() }
        .stateIn(appScope, SharingStarted.Eagerly, false)

    val isPro: StateFlow<Boolean> = combine(
        billingIsSynced,
        billingIsPro,
        cachedIsPro,
    ) { synced, live, cached ->
        if (synced) live else cached
    }.stateIn(appScope, SharingStarted.Eagerly, false)

    init {
        appScope.launch {
            combine(billingIsSynced, billingIsPro) { synced, pro -> synced to pro }
                .distinctUntilChanged()
                .collect { (synced, pro) ->
                    if (synced) {
                        store.edit { it[isProCachedKey] = pro }
                    }
                }
        }
    }
}
