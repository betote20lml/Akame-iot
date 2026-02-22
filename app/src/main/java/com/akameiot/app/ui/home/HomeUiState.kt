package com.akameiot.app.ui.home

data class HomeUiState(
    val isLoading: Boolean = false,
    val telemetry: List<Any> = emptyList(),
)