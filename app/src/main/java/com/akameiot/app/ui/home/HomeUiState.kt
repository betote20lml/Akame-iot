package com.akameiot.app.ui.home

import com.akameiot.domain.model.AppUser
data class HomeUiState(
    val isLoading: Boolean = false,
    val telemetry: List<Any> = emptyList(),
    val appUser: AppUser? = null,
)