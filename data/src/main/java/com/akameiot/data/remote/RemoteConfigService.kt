package com.akameiot.data.remote

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.tasks.await

class RemoteConfigService {

    private val remoteConfig =
        FirebaseRemoteConfig.getInstance()

    init {

        val settings = remoteConfigSettings {


            minimumFetchIntervalInSeconds = 0
        }

        remoteConfig.setConfigSettingsAsync(settings)

        remoteConfig.setDefaultsAsync(
            mapOf(
                "min_supported_version" to 1L,
                "force_update_enabled" to false
            )
        )
    }

    suspend fun fetchAndActivate(): Boolean {

        return remoteConfig
            .fetchAndActivate()
            .await()
    }

    fun getMinSupportedVersion(): Int {

        return remoteConfig
            .getLong("min_supported_version")
            .toInt()
    }

    fun isForceUpdateEnabled(): Boolean {

        return remoteConfig
            .getBoolean("force_update_enabled")
    }
}