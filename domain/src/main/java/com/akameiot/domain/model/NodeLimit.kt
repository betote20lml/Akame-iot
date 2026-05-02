package com.akameiot.domain.model

data class NodeLimit(
    val meshId  : String,
    val nodeId  : Int,
    val metric  : String,
    val userMin : Double?,
    val userMax : Double?,
    val updatedAt : Long = System.currentTimeMillis() / 1000L,
)