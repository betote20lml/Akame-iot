package com.akameiot.data.local.entity

import androidx.room.Entity
import androidx.room.Index


@Entity(
    tableName = "telemetry_agg",
    primaryKeys = ["level", "meshId", "nodeId", "metric", "bucketStart"],
    indices = [
        Index(value = ["level", "meshId", "nodeId", "metric", "bucketStart"], unique = true),
        Index(value = ["level", "meshId", "nodeId", "metric"])
    ]
)
data class TelemetryAggEntity(
    val level       : String,
    val meshId      : String,
    val nodeId      : Int,
    val metric      : String,
    val bucketStart : Long,
    val firstTs     : Long,
    val firstVal    : Double,
    val lastTs      : Long,
    val lastVal     : Double,
    val minTs       : Long,
    val minVal      : Double,
    val maxTs       : Long,
    val maxVal      : Double,
    val count       : Int
)