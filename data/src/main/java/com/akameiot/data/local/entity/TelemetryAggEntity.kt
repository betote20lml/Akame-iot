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
    val level       : String,   // "7d" | "1m" | "3m" | "1y"
    val meshId      : String,
    val nodeId      : Int,
    val metric      : String,
    val bucketStart : Long,     // segundos
    val chunkSize   : Long,     // segundos (3600*3, 3600*6, 86400, 86400*7)

    // Valores a preservar forma
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