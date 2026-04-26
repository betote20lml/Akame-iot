package com.akameiot.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.ui.graphics.vector.ImageVector

data class DrawerDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val isIndexFactory: Boolean = false,
)

object DrawerDestinations {

    val items = listOf(
        DrawerDestination(
            route = Routes.HOME,
            label = "Dashboard",
            icon  = Icons.Default.Dashboard,
        ),
        DrawerDestination(
            route = "account",
            label = "Cuenta",
            icon  = Icons.Default.Person,
        ),
        DrawerDestination(
            route = "alerts",
            label = "Alertas",
            icon  = Icons.Default.Notifications,
        ),

        DrawerDestination(
            route = Routes.TOKEN,
            label = "Token de Acceso",
            icon  = Icons.Default.VpnKey,
        ),
        DrawerDestination(
            route          = "",
            label          = "Crear Índice",
            icon           = Icons.Default.Tune,
            isIndexFactory = true,
        ),
    )
}