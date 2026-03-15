package com.akameiot.domain.usecase

import com.akameiot.domain.model.Network
import com.akameiot.domain.repository.NetworkStore
import com.akameiot.domain.repository.SessionRepository

class SyncUserDevicesUseCase(
    private val sessionRepository: SessionRepository,
    private val networkStore: NetworkStore,
) {
    suspend operator fun invoke(authToken: String): List<Network> {
        val devices = sessionRepository.getUserDevices(authToken)
        devices.forEach { network ->
            networkStore.addNetwork(network)
        }
        return devices
    }

}