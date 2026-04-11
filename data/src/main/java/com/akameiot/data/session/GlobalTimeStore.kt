package com.akameiot.data.session

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.globalTimeDataStore by preferencesDataStore("global_time")

class GlobalTimeStore(private val context: Context) {

    private val KEY_GLOBAL_NOW = longPreferencesKey("global_now")

    val globalNowFlow: Flow<Long> =
        context.globalTimeDataStore.data.map { prefs ->
            prefs[KEY_GLOBAL_NOW] ?: 0L
        }

    suspend fun setGlobalNow(value: Long) {
        context.globalTimeDataStore.edit { prefs ->
            val current = prefs[KEY_GLOBAL_NOW] ?: 0L
            if (value > current) {
                prefs[KEY_GLOBAL_NOW] = value
            }
        }
    }
}