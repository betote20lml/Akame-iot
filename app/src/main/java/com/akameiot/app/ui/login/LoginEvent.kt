package com.akameiot.app.ui.login

sealed class LoginEvent {

    object Success : LoginEvent()

    object NavigateToPasswordRecovery : LoginEvent()

    object NavigateToVerification : LoginEvent()

    data class Error(
        val message: String
    ) : LoginEvent()
}
