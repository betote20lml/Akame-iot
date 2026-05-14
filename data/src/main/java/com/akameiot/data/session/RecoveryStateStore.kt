package com.akameiot.data.session

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.recoveryDataStore by preferencesDataStore(name = "recovery_prefs")

class RecoveryStateStore(
    private val context: Context
) {
    private val initialSyncFailedKey = booleanPreferencesKey("initial_sync_failed")
    private val pendingInitialSyncKey = booleanPreferencesKey("pending_initial_sync")

    suspend fun setInitialSyncFailed(failed: Boolean) {
        context.recoveryDataStore.edit { prefs ->
            prefs[initialSyncFailedKey] = failed
        }
    }

    suspend fun hasInitialSyncFailed(): Boolean {
        return context.recoveryDataStore.data
            .map { prefs -> prefs[initialSyncFailedKey] ?: false }
            .first()
    }

    suspend fun markPendingInitialSync() {
        context.recoveryDataStore.edit { prefs ->
            prefs[pendingInitialSyncKey] = true
        }
    }

    suspend fun consumePendingInitialSync(): Boolean {
        val pending = context.recoveryDataStore.data
            .map { prefs -> prefs[pendingInitialSyncKey] ?: false }
            .first()
        if (pending) {
            context.recoveryDataStore.edit { prefs ->
                prefs[pendingInitialSyncKey] = false
            }
        }
        return pending
    }
}