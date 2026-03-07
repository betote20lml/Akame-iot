package com.akameiot.domain.model

sealed class AppUser {

    abstract val userId: String

    data class Owner(
        override val userId: String
    ) : AppUser()

    data class Limited(
        override val userId: String,
    ) : AppUser()
}