package com.akameiot.data.repository_impl

import com.akameiot.data.repository.TelemetryRepository
import com.akameiot.data.session.DeviceNetworkStore
import com.akameiot.domain.session.AuthSessionManager
import com.akameiot.domain.usecase.SyncRecentTelemetryUseCase
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random
import java.util.concurrent.atomic.AtomicBoolean


class SyncRecentTelemetryUseCaseImpl(
    private val repository: TelemetryRepository,
    private val authSessionManager: AuthSessionManager,
    private val networkStore: DeviceNetworkStore,

) : SyncRecentTelemetryUseCase {

    private val pendingTsMesh = ConcurrentHashMap<String, Long>()
    private val pendingGlobalTs = AtomicBoolean(false)

    private val inFlightMesh = ConcurrentHashMap<String, Boolean>()
    private val inFlightGlobal = AtomicBoolean(false)

    override suspend fun invoke(meshId: String, notifTs: Long) {

        val meshIds = networkStore.getNetworks()
            .map { it.thingName }
            .ifEmpty { listOf(meshId) }

        val lastTs = repository.getLatestTimestamp(meshId)

        if (notifTs in 1..lastTs) return

        val fromTs = if (lastTs > 0L) lastTs else
            System.currentTimeMillis() / 1000L - (30 * 60L)

        val nowSec = System.currentTimeMillis() / 1000L
        val windowSec = nowSec - fromTs


        if (windowSec >= 24 * 3600) {

            //  GLOBAL inFlight
            if (!inFlightGlobal.compareAndSet(false, true)) {
                pendingGlobalTs.set(true)
                return
            }

            try {
                val token = authSessionManager.fetchIdToken()
                repository.fetchAndSaveWindow(
                    bearerToken = token,
                    meshId = meshId,
                    meshIds = meshIds,
                    fromTs = fromTs
                )
            } finally {
                try {
                    if (pendingGlobalTs.getAndSet(false)) {
                        val token = authSessionManager.fetchIdToken()
                        val meshIdsRetry = networkStore.getNetworks()
                            .map { it.thingName }
                            .ifEmpty { listOf(meshId) }
                        val retryFromTs = System.currentTimeMillis() / 1000L - (30 * 60L)
                        repository.fetchAndSaveWindow(
                            bearerToken = token,
                            meshId = meshId,
                            meshIds = meshIdsRetry,
                            fromTs = retryFromTs
                        )
                    }
                } catch (_: Exception) {
                } finally {
                    inFlightGlobal.set(false)
                }
            }

        } else {

            // PER-MESH inFlight
            if (inFlightMesh.putIfAbsent(meshId, true) != null) {
                pendingTsMesh.merge(meshId, notifTs) { old, new -> maxOf(old, new) }
                return
            }

            try {
                val token = authSessionManager.fetchIdToken()
                repository.fetchAndSaveWindow(
                    bearerToken = token,
                    meshId = meshId,
                    meshIds = meshIds,
                    fromTs = fromTs
                )
            } finally {
                inFlightMesh.remove(meshId)
                val pending = pendingTsMesh.remove(meshId)
                if (pending != null) {
                    try {
                        invoke(meshId, pending)
                    } catch (_: Exception) {
                    }
                }
            }
        }

        if (Random.nextInt(10) == 0) {
            repository.cleanOldData(days = 730)
        }
    }
    override suspend fun forceSync(meshId: String, days: Long) {

        if (!inFlightGlobal.compareAndSet(false, true)) {
            pendingGlobalTs.set(true)
            return
        }

        try {
            val token = authSessionManager.fetchIdToken()
            val fromTs = System.currentTimeMillis() / 1000L - (days * 86400L)

            val meshIds = networkStore.getNetworks()
                .map { it.thingName }
                .ifEmpty { listOf(meshId) }

            repository.fetchAndSaveWindow(
                bearerToken = token,
                meshId = meshId,
                meshIds = meshIds,
                fromTs = fromTs
            )
        } finally {
            try {
                if (pendingGlobalTs.getAndSet(false)) {
                    val token = authSessionManager.fetchIdToken()
                    val meshIdsRetry = networkStore.getNetworks()
                        .map { it.thingName }
                        .ifEmpty { listOf(meshId) }
                    val retryFromTs = System.currentTimeMillis() / 1000L - (30 * 60L)
                    repository.fetchAndSaveWindow(
                        bearerToken = token,
                        meshId = meshId,
                        meshIds = meshIdsRetry,
                        fromTs = retryFromTs
                    )
                }
            } catch (_: Exception) {
            } finally {
                inFlightGlobal.set(false)
            }
        }
    }

    override suspend fun recoverWindow(
        meshIds: List<String>,
        fromTs: Long,
        toTs: Long
    ) {

        while (!inFlightGlobal.compareAndSet(false, true)) {
            kotlinx.coroutines.delay(500)
        }

        try {

            val token = authSessionManager.fetchIdToken()

            repository.coldFetchWindow(
                bearerToken = token,
                meshId = meshIds.firstOrNull().orEmpty(),
                meshIds = meshIds,
                fromTs = fromTs,
                toTs = toTs,
            )

        } finally {

            inFlightGlobal.set(false)
        }
    }

    override suspend fun resumeFailedSync(meshIds: List<String>) {
        val oldestTs = repository.getOldestTimestampGlobal()
            ?: (System.currentTimeMillis() / 1000L) // si no hay nada local, toTs = ahora

        val fromTs = oldestTs - (90 * 86400L) // 90 días antes del oldest

        recoverWindow(
            meshIds = meshIds,
            fromTs  = fromTs,
            toTs    = oldestTs  // hasta el oldest, hacia atrás
        )
    }

    override suspend fun syncStaleWindow(meshId: String, fromTs: Long) {
        if (!inFlightGlobal.compareAndSet(false, true)) {
            pendingGlobalTs.set(true)
            return
        }
        try {
            val token = authSessionManager.fetchIdToken()
            val meshIds = networkStore.getNetworks()
                .map { it.thingName }
                .ifEmpty { listOf(meshId) }

            repository.fetchAndSaveWindow(
                bearerToken = token,
                meshId      = meshId,
                meshIds     = meshIds,
                fromTs      = fromTs  // solo el gap real, nunca 90 días
            )
        } finally {
            inFlightGlobal.set(false)
        }
    }


}