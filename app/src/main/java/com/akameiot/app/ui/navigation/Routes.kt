package com.akameiot.app.ui.navigation

object Routes {
    const val SPLASH = "splash"

    const val HOME = "home"

    const val LOGIN = "login"

     const val RESET_PASSWORD_WITH_ARG =
        "reset_password/{email}"

    fun resetPassword(email: String) =
        "reset_password/$email"

    const val REGISTER = "register"
    const val TERMS = "terms"
    const val LANDING = "landing"

    const val QR_AUTH = "qr_auth"

    // Ruta base
    const val VERIFICATION_WITH_ARGS =
        "verification/{type}/{email}/{password}"

    fun verification(
        type: VerificationType,
        email: String,
        password: String
    ) =
        "verification/${type.name}/$email/$password"


}
