package com.akameiot.domain.repository

interface AuthRepository {

    suspend fun register(
        email: String,
        password: String
    )
}