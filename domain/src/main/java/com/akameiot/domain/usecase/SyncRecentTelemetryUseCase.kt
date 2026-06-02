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
        meshIds: List<String>,
        fromTs: Long,
        toTs: Long
    )

    // Continuar sync inicial fallida: 90 días antes del dato más antiguo local
    suspend fun resumeFailedSync(meshIds: List<String>)

    // Stale normal > 24h: solo el gap desde lastTs, sin histórico
    suspend fun syncStaleWindow(meshId: String, fromTs: Long)


}