package com.akameiot.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.akameiot.domain.repository.MeshWindowRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.meshWindowDataStore by preferencesDataStore(name = "mesh_window_prefs")

class MeshUpdateWindowStore(private val context: Context) : MeshWindowRepository {

    private val windowsKey = stringPreferencesKey("mesh_windows")

    override suspend fun getWindow(meshId: String): Long? {
        val json = context.meshWindowDataStore.data
            .map { it[windowsKey] ?: "{}" }
            .first()
        val obj = JSONObject(json)
        return if (obj.has(meshId)) obj.getLong(meshId) else null
    }

    override suspend fun setWindow(meshId: String, windowSeconds: Long) {
        context.meshWindowDataStore.edit { prefs ->
            val json = prefs[windowsKey] ?: "{}"
            val obj = JSONObject(json)
            obj.put(meshId, windowSeconds)
            prefs[windowsKey] = obj.toString()
        }
    }

    override suspend fun getAllWindows(): Map<String, Long> {
        val json = context.meshWindowDataStore.data
            .map { it[windowsKey] ?: "{}" }
            .first()
        val obj = JSONObject(json)
        return obj.keys().asSequence().associateWith { obj.getLong(it) }
    }
}