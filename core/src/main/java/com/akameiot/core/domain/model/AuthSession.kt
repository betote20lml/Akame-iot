package com.akameiot.core.domain.model

data class AuthSession(
    val idToken: String,
    val accessToken: String?,
    val refreshToken: String?,
    val userId: String,
    val email: String
)