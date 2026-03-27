package com.akameiot.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "telemetry",
    indices = [
        Index(value = ["meshid", "nodeId", "metric", "timestamp"], unique = true),
        Index(value = ["meshid", "timestamp"])
    ]
)

data class TelemetryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val meshid: String,
    val nodeId: Int,
    val timestamp: Long,
    val metric: String,
    val value: Double
)