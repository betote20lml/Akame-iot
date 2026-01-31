package com.akameiot.app.data.auth.remote


data class LoginRequest(
    val AuthFlow: String = "USER_PASSWORD_AUTH",
    val ClientId: String,
    val AuthParameters: Map<String, String>
)

data class LoginResponse(
    val AuthenticationResult: AuthResult?
)

data class AuthResult(
    val IdToken: String,
    val AccessToken: String,
    val RefreshToken: String
)
