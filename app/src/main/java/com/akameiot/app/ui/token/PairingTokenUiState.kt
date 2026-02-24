package com.akameiot.app.ui.token

import com.akameiot.domain.model.PairingToken

data class PairingTokenUiState(
    val isLoading: Boolean = false,
    val token: PairingToken? = null,
    val secondsLeft: Int = 0
)