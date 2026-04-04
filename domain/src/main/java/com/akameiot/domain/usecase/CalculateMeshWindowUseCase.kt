package com.akameiot.domain.usecase

import com.akameiot.domain.repository.MeshWindowRepository
import com.akameiot.domain.repository.TelemetryWindowRepository

class CalculateMeshWindowUseCase(
    private val telemetryWindowRepository: TelemetryWindowRepository,
    private val meshWindowRepository: MeshWindowRepository
) {
    suspend fun getOrCalculate(meshId: String): Long {
        val existing = meshWindowRepository.getWindow(meshId)
        if (existing != null) return existing

        val timestamps = telemetryWindowRepository.getOldestTimestamps(meshId)

        if (timestamps.size < 2) return DEFAULT_WINDOW_SECONDS

        val minDelta = timestamps
            .zipWithNext { a, b -> b - a }
            .filter { it > 0 }
            .minOrNull() ?: return DEFAULT_WINDOW_SECONDS

        meshWindowRepository.setWindow(meshId, minDelta)
        return minDelta
    }

    companion object {
        const val DEFAULT_WINDOW_SECONDS = 7200L
        const val DEFAULT_FRESHNESS_SECONDS = 1800L
    }
}