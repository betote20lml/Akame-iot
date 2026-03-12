package com.akameiot.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.fcmDataStore by preferencesDataStore(name = "fcm_prefs")

class FcmTokenStore(
    private val context: Context
) {

    private val TOKEN_KEY = stringPreferencesKey("fcm_token")
    private val RESUBSCRIBE_KEY = booleanPreferencesKey("needs_resubscribe")

    suspend fun saveToken(token: String) {
        context.fcmDataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    suspend fun getToken(): String? {
        return context.fcmDataStore.data
            .map { it[TOKEN_KEY] }
            .first()
    }

    suspend fun markNeedsResubscribe() {
        context.fcmDataStore.edit { prefs ->
            prefs[RESUBSCRIBE_KEY] = true
        }
    }

    suspend fun needsResubscribe(): Boolean {
        return context.fcmDataStore.data
            .map { it[RESUBSCRIBE_KEY] ?: false }
            .first()
    }

    suspend fun clearResubscribeFlag() {
        context.fcmDataStore.edit { prefs ->
            prefs[RESUBSCRIBE_KEY] = false
        }
    }
}
