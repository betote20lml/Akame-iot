package com.akameiot.data.repository_impl

import com.akameiot.data.remote.api.SessionApi
import com.akameiot.data.remote.safeApiCall
import com.akameiot.domain.model.Network
import com.akameiot.domain.repository.SessionRepository

class SessionRepositoryImpl(
    private val sessionApi: SessionApi
) : SessionRepository {

    override suspend fun getUserDevices(token: String): List<Network> {
        val response = safeApiCall { sessionApi.getDevices("Bearer $token") }
        return response.meshes.map {
            Network(thingName = it.thingName, displayName = it.displayName)
        }
    }
}