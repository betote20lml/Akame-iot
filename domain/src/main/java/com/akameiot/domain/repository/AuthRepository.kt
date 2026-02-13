package com.akameiot.domain.repository

import com.akameiot.domain.model.RegisterResult

interface AuthRepository {

    suspend fun register(
        email: String,
        password: String
    ): RegisterResult
}