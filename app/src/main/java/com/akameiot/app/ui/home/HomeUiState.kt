package com.akameiot.app.ui.home

import com.akameiot.app.ui.home.model.TelemetryUiModel
import com.akameiot.domain.model.AppUser
data class HomeUiState(
    val isLoading: Boolean = false,
    val telemetry: List<TelemetryUiModel> = emptyList(),
    val appUser: AppUser? = null,
)