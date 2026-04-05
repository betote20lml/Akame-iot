package com.akameiot.data.session

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray

private val Context.filterDataStore by preferencesDataStore(name = "filter_prefs")

class FilterPreferencesStore(private val context: Context) {

    private val filterNetworksKey = stringPreferencesKey("filter_networks")
    private val networksOrderKey  = stringPreferencesKey("networks_order")
    private val filterMetricsKey  = stringPreferencesKey("filter_metrics")
    private val metricsOrderKey   = stringPreferencesKey("metrics_order")
    private val sortAscendingKey  = stringPreferencesKey("sort_ascending_v2")

    data class FilterPrefs(
        val filterNetworks: List<String> = emptyList(),
        val networksOrder: List<String>  = emptyList(),
        val filterMetrics: List<String>  = emptyList(),
        val metricsOrder: List<String>   = emptyList(),
        val sortAscending: Boolean?      = null,
    )

    val prefsFlow: Flow<FilterPrefs> = context.filterDataStore.data.map { prefs ->

        val sortAscending: Boolean? = try {
            when (val value = prefs[sortAscendingKey]) {
                is String -> when (value) {
                    "true" -> true
                    "false" -> false
                    else -> null
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }

        FilterPrefs(
            filterNetworks = prefs[filterNetworksKey]?.toListSafe() ?: emptyList(),
            networksOrder  = prefs[networksOrderKey]?.toListSafe() ?: emptyList(),
            filterMetrics  = prefs[filterMetricsKey]?.toListSafe() ?: emptyList(),
            metricsOrder   = prefs[metricsOrderKey]?.toListSafe() ?: emptyList(),
            sortAscending  = sortAscending
        )
    }

    private fun String.toListSafe(): List<String> {
        return try {
            val arr = JSONArray(this)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }



    suspend fun save(prefs: FilterPrefs) {
        context.filterDataStore.edit { p ->
            p[filterNetworksKey] = prefs.filterNetworks.toJson()
            p[networksOrderKey]  = prefs.networksOrder.toJson()
            p[filterMetricsKey]  = prefs.filterMetrics.toJson()
            p[metricsOrderKey]   = prefs.metricsOrder.toJson()
            p[sortAscendingKey]  = prefs.sortAscending?.toString() ?: "null"
        }
    }

    private fun List<String>.toJson(): String {
        val arr = JSONArray()
        forEach { arr.put(it) }
        return arr.toString()
    }

    private fun String.toList(): List<String> {
        val arr = JSONArray(this)
        return (0 until arr.length()).map { arr.getString(it) }
    }
}