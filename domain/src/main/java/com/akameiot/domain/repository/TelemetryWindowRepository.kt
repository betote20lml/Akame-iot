package com.akameiot.domain.repository

interface TelemetryWindowRepository {
    suspend fun getOldestTimestamps(meshId: String): List<Long>
}