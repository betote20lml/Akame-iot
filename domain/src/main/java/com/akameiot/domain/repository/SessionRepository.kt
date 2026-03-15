package com.akameiot.domain.repository

import com.akameiot.domain.model.Network

interface SessionRepository {
    suspend fun getUserDevices(token: String): List<Network>
}