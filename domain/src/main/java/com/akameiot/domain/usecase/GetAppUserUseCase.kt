package com.akameiot.domain.usecase

import com.akameiot.domain.model.AppUser
import com.akameiot.domain.session.AuthSessionManager

class GetAppUserUseCase(
    private val authSessionManager: AuthSessionManager
) {
    suspend operator fun invoke(): AppUser {
        val userId = authSessionManager.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        return if (authSessionManager.isLimitedSession()) {
            AppUser.Limited(userId = userId)
        } else {
            AppUser.Owner(userId = userId)
        }
    }
}