package com.akameiot.domain.session

interface AuthSessionManager {

    suspend fun isUserLoggedIn(): Boolean

    suspend fun getCurrentUserId(): String?

    suspend fun logout()

    suspend fun fetchIdToken(): String

    suspend fun signInWithCustomAuth(username: String, token: String)

    suspend fun isLimitedSession(): Boolean

    suspend fun setLimitedSession(isLimited: Boolean)

    suspend fun hasLocalSession(): Boolean

    suspend fun setLocalSession(active: Boolean)

    suspend fun getUserId(): String?

    suspend fun setUserId(userId: String)


}