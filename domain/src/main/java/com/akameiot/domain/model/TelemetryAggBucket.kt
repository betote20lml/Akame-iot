package com.akameiot.domain.model

data class TelemetryAggBucket(
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