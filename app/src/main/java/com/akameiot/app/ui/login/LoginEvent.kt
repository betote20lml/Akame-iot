package com.akameiot.app.ui.login

sealed class LoginEvent {

    object Success : LoginEvent()

    object NavigateToPasswordRecovery : LoginEvent()

    data class NavigateToVerification(
        val email: String
    ) : LoginEvent()

    data class Error(
        val message: String
    ) : LoginEvent()
}
