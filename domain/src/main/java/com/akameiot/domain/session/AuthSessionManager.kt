package com.akameiot.domain.session

interface AuthSessionManager {

    suspend fun isUserLoggedIn(): Boolean

    suspend fun getCurrentUserId(): String?

    suspend fun logout()
}