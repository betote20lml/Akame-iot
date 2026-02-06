package com.akameiot.app.ui.navigation

object Routes {

    const val LOGIN = "login"
    const val REGISTER = "register"
    const val TERMS = "terms"
    const val LANDING = "landing"

    // Ruta base SIN argumentos
    const val VERIFICATION = "verification"

    // Builder seguro
    fun verification(type: VerificationType) =
        "$VERIFICATION/${type.name}"
}
