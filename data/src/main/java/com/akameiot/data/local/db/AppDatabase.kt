package com.akameiot.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.akameiot.data.local.dao.TelemetryDao
import com.akameiot.data.local.entity.TelemetryEntity

@Database(
    entities = [TelemetryEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun telemetryDao(): TelemetryDao
}