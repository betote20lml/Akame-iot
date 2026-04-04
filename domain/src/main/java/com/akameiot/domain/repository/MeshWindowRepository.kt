package com.akameiot.domain.repository

interface MeshWindowRepository {
    suspend fun getWindow(meshId: String): Long?
    suspend fun setWindow(meshId: String, windowSeconds: Long)
    suspend fun getAllWindows(): Map<String, Long>
}