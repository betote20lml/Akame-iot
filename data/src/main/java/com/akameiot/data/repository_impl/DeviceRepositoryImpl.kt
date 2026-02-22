package com.akameiot.data.repository_impl

import com.akameiot.data.remote.DeviceApi
import com.akameiot.domain.model.DeviceActivationRequest
import com.akameiot.domain.model.DeviceActivationResponse
import com.akameiot.domain.repository.DeviceRepository

class DeviceRepositoryImpl(
    private val api: DeviceApi
): DeviceRepository {
    override suspend fun activateDevice(
        token: String,
        request: DeviceActivationRequest
    ): DeviceActivationResponse {
        return api.activateDevice(authHeader = "Bearer $token", request = request)
    }
}