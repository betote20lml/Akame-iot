package com.akameiot.domain.session

interface AuthSessionManager {

    suspend fun isUserLoggedIn(): Boolean

    suspend fun getCurrentUserId(): String?

    suspend fun logout()

    suspend fun fetchIdToken(): String

    suspend fun signInWithCustomAuth(username: String, token: String)


}