package com.akameiot.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.akameiot.data.local.dao.TelemetryDao
import com.akameiot.data.local.entity.TelemetryAggEntity
import com.akameiot.data.local.entity.TelemetryEntity

@Database(
    entities = [
        TelemetryEntity::class,
        TelemetryAggEntity::class,
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun telemetryDao(): TelemetryDao
}