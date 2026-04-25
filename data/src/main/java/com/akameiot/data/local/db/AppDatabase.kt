package com.akameiot.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.akameiot.data.local.dao.NodeLimitDao
import com.akameiot.data.local.dao.TelemetryDao
import com.akameiot.data.local.entity.NodeLimitEntity
import com.akameiot.data.local.entity.TelemetryAggEntity
import com.akameiot.data.local.entity.TelemetryEntity

@Database(
    entities = [
        TelemetryEntity::class,
        TelemetryAggEntity::class,
        NodeLimitEntity::class,
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun telemetryDao(): TelemetryDao
    abstract fun nodeLimitDao(): NodeLimitDao
}