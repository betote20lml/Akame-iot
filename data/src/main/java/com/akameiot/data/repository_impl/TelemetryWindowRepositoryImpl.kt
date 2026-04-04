package com.akameiot.data.repository_impl

import com.akameiot.data.local.dao.TelemetryDao
import com.akameiot.domain.repository.TelemetryWindowRepository

class TelemetryWindowRepositoryImpl(
    private val telemetryDao: TelemetryDao
) : TelemetryWindowRepository {

    override suspend fun getOldestTimestamps(meshId: String): List<Long> {
        return telemetryDao.getOldestTimestamps(meshId)
    }
}