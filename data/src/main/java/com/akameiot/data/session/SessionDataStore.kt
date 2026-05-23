package com.akameiot.data.session

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


private val Context.sessionDataStore by preferencesDataStore(name = "session_prefs")

class SessionDataStore(
    private val context: Context
) {

    private val USER_ID_KEY = stringPreferencesKey("user_id")

    private val IS_LIMITED_KEY = booleanPreferencesKey("is_limited_session")
    private val HAS_SESSION_KEY = booleanPreferencesKey("has_session")

    suspend fun setLimitedSession(isLimited: Boolean) {
        context.sessionDataStore.edit { prefs ->
            prefs[IS_LIMITED_KEY] = isLimited
        }
    }

    suspend fun isLimitedSession(): Boolean {
        return context.sessionDataStore.data
            .map { prefs -> prefs[IS_LIMITED_KEY] ?: false }
            .first()
    }

    suspend fun setHasSession(hasSession: Boolean) {
        context.sessionDataStore.edit { prefs ->
            prefs[HAS_SESSION_KEY] = hasSession
        }
    }


    suspend fun hasSession(): Boolean {
        return context.sessionDataStore.data
            .map { prefs -> prefs[HAS_SESSION_KEY] ?: false }
            .first()
    }

    suspend fun setUserId(userId: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[USER_ID_KEY] = userId
        }
    }

    suspend fun getUserId(): String? {
        return context.sessionDataStore.data
            .map { prefs -> prefs[USER_ID_KEY] }
            .first()
    }
}

