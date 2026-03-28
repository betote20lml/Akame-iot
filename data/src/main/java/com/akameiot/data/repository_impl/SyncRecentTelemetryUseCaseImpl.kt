package com.akameiot.data.repository_impl

import com.akameiot.data.repository.TelemetryRepository
import com.akameiot.domain.session.AuthSessionManager
import com.akameiot.domain.usecase.SyncRecentTelemetryUseCase

class SyncRecentTelemetryUseCaseImpl(
    private val repository: TelemetryRepository,
    private val authSessionManager: AuthSessionManager
) : SyncRecentTelemetryUseCase {

    override suspend fun invoke(meshId: String, notifTs: Long) {
        val lastTs = repository.getLatestTimestamp(meshId)

        // Si la notificación no trae datos más recientes, no hacemos nada
        if (notifTs > 0 && notifTs <= lastTs) return

        val token = authSessionManager.fetchIdToken()

        // Si no hay datos locales, pedimos los últimos 30 min como fallback
        val fromTs = if (lastTs > 0L) lastTs else
            System.currentTimeMillis() / 1000 - 30 * 60

        repository.fetchAndSaveWindow(
            bearerToken = token,
            meshId = meshId,
            fromTs = fromTs
        )
        repository.cleanOldData(days = 730)
    }
    override suspend fun forceSync(meshId: String, days: Long) {
        val token = authSessionManager.fetchIdToken()
        val fromTs = System.currentTimeMillis() / 1000L - (days * 86400L)
        repository.fetchAndSaveWindow(
            bearerToken = token,
            meshId = meshId,
            fromTs = fromTs
        )
    }
}