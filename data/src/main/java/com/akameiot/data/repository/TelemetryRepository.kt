package com.akameiot.data.repository

import com.akameiot.data.local.dao.TelemetryDao
import com.akameiot.data.mapper.toEntities
import com.akameiot.data.remote.api.TelemetryApiService
import com.akameiot.data.remote.dto.TelemetryDto
import kotlinx.coroutines.flow.Flow
import com.akameiot.data.local.entity.TelemetryEntity

class TelemetryRepository(
    private val dao: TelemetryDao,
    private val api: TelemetryApiService
) {

    //Devuelve el último timestamp guardado para este mesh, o 0 si no hay registros.
    suspend fun getLatestTimestamp(meshId: String): Long =
        dao.getLatestTimestamp(meshId) ?: 0L

    //Decide si hace una hot query o una cold query según la ventana temporal.
    suspend fun fetchAndSaveWindow(
        bearerToken: String,
        meshId: String,
        meshIds: List<String>,
        fromTs: Long
    ) {
        val nowSec = System.currentTimeMillis() / 1000L
        val windowSec = nowSec - fromTs

        if (windowSec < 24 * 3600) {
            // HOT → SOLO UN MESH
            val response = api.getRecentTelemetry(
                bearerToken = "Bearer $bearerToken",
                meshId = meshId,
                sinceTs = fromTs
            )
            val entities = response.items.flatMap { it.toEntities() }
            dao.insertAll(entities)

        } else {
            // COLD → TODOS LOS MESHES
            coldFetchAndSave(
                bearerToken = bearerToken,
                meshIds = meshIds,
                fromTs = fromTs,
                toTs = nowSec
            )
        }
    }

    private suspend fun coldFetchAndSave(
        bearerToken: String,
        meshIds: List<String>,
        fromTs: Long,
        toTs: Long
    ) {
        val auth = "Bearer $bearerToken"

        // 1. Iniciar query
        val startResp = api.startColdQuery(
            bearerToken = auth,
            meshes = meshIds.joinToString(","),
            fromTs = fromTs,
            toTs = toTs
        )
        val queryId = startResp.queryExecutionId

        // 2. Poll hasta SUCCEEDED
        var status = ""
        var attempts = 0
        val maxAttempts = 300
        while (status != "SUCCEEDED" && attempts < maxAttempts)  {
            kotlinx.coroutines.delay(2_000)
            val statusResp = api.getColdQueryStatus(
                bearerToken = auth,
                queryExecutionId = queryId
            )
            status = statusResp.status

            if (status == "FAILED" || status == "CANCELLED") {
                throw IllegalStateException("Cold query failed: $status")
            }

            attempts++

        }
        if (status != "SUCCEEDED") {
            throw IllegalStateException("Cold query timeout")
        }

        // 3. Paginar resultados
        var nextToken: String? = null
        do {
            val page = api.getColdQueryResults(
                bearerToken = auth,
                queryExecutionId = queryId,
                limit = 200,
                nextToken = nextToken
            )
            val entities = page.items.flatMap { it.toEntities() }
            dao.insertAll(entities)
            nextToken = page.nextToken
        } while (nextToken != null)
    }

    suspend fun saveTelemetry(dtos: List<TelemetryDto>) {
        val entities = dtos.flatMap { it.toEntities() }
        dao.insertAll(entities)
    }

    suspend fun cleanOldData(days: Int) {
        val threshold = System.currentTimeMillis() / 1000L - (days * 86400L)
        dao.deleteOlderThan(threshold)
    }

    fun getMetric(meshId: String, nodeId: Int, metric: String): Flow<List<TelemetryEntity>> =
        dao.getMetric(meshId, nodeId, metric)
}