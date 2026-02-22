package com.akameiot.domain.usecase

import com.akameiot.domain.model.DeviceActivationRequest
import com.akameiot.domain.model.DeviceActivationResponse
import com.akameiot.domain.repository.DeviceRepository

class ActivateDeviceUseCase(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(token: String, request: DeviceActivationRequest): DeviceActivationResponse {
        return repository.activateDevice(token, request)
    }
}