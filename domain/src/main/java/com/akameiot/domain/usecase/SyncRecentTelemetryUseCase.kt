package com.akameiot.domain.usecase

interface SyncRecentTelemetryUseCase {

    suspend operator fun invoke(
        meshId: String,
        notifTs: Long = 0L
    )

    suspend fun forceSync(
        meshId: String,
        days: Long = 90L
    )

    suspend fun recoverWindow(
        meshId: String,
        fromTs: Long,
        toTs: Long
    )
}