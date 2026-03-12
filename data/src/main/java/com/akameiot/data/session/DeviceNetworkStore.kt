package com.akameiot.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.akameiot.domain.model.Network
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.networkDataStore by preferencesDataStore(name = "network_prefs")

class DeviceNetworkStore(
    private val context: Context
) {

    private val NETWORKS_KEY = stringPreferencesKey("device_networks")

    suspend fun addNetwork(network: Network) {

        val current = getNetworks().toMutableList()

        if (current.any { it.thingName == network.thingName }) return

        current.add(network)

        saveNetworks(current)
    }

    suspend fun getNetworks(): List<Network> {

        val json = context.networkDataStore.data
            .map { prefs -> prefs[NETWORKS_KEY] ?: "[]" }
            .first()

        val array = JSONArray(json)

        val networks = mutableListOf<Network>()

        for (i in 0 until array.length()) {

            val obj = array.getJSONObject(i)

            networks.add(
                Network(
                    thingName = obj.getString("thingName"),
                    displayName = obj.getString("displayName")
                )
            )
        }

        return networks
    }

    suspend fun removeNetwork(thingName: String) {

        val networks = getNetworks().filterNot {
            it.thingName == thingName
        }

        saveNetworks(networks)
    }

    private suspend fun saveNetworks(networks: List<Network>) {

        val array = JSONArray()

        networks.forEach {

            val obj = JSONObject()

            obj.put("thingName", it.thingName)
            obj.put("displayName", it.displayName)

            array.put(obj)
        }

        context.networkDataStore.edit { prefs ->
            prefs[NETWORKS_KEY] = array.toString()
        }
    }
}