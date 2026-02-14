package com.akameiot.app.ui.navigation

object Routes {

    const val HOME = "home"

    const val LOGIN = "login"
    const val REGISTER = "register"
    const val TERMS = "terms"
    const val LANDING = "landing"

    const val QR_AUTH = "qr_auth"

    // Ruta base
    const val VERIFICATION = "verification"

    // Builder
    fun verification(type: VerificationType) =
        "$VERIFICATION/${type.name}"
}
