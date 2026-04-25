package com.akameiot.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "node_limits",
    primaryKeys = ["meshId", "nodeId", "metric"],
    indices = [
        Index(value = ["meshId", "metric"]),
    ]
)
data class NodeLimitEntity(
    val meshId    : String,
    val nodeId    : Int,
    val metric    : String,
    val userMin   : Double?,
    val userMax   : Double?,
    val updatedAt : Long,
)