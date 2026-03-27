package com.akameiot.domain.usecase

interface SyncRecentTelemetryUseCase {
    suspend operator fun invoke(meshId: String, notifTs: Long = 0L)
}