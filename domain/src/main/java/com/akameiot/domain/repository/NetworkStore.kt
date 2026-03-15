package com.akameiot.domain.repository

import com.akameiot.domain.model.Network

interface NetworkStore {
    suspend fun addNetwork(network: Network)
}