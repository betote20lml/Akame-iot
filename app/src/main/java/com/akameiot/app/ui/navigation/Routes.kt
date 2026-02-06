package com.akameiot.app.ui.navigation

object Routes {

    const val LOGIN = "login"
    const val REGISTER = "register"
    const val TERMS = "terms"
    const val LANDING = "landing"

    const val QR_AUTH = "qr_auth"

    // Ruta base SIN argumentos
    const val VERIFICATION = "verification"

    // Builder seguro
    fun verification(type: VerificationType) =
        "$VERIFICATION/${type.name}"
}
