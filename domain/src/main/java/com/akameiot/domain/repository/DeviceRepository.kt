package com.akameiot.domain.repository

import com.akameiot.domain.model.DeviceActivationRequest
import com.akameiot.domain.model.DeviceActivationResponse

interface DeviceRepository {
    suspend fun activateDevice(
        token: String,
        request: DeviceActivationRequest
    ): DeviceActivationResponse
}