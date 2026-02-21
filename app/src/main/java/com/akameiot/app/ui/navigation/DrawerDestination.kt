package com.akameiot.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.ui.graphics.vector.ImageVector

data class DrawerDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

object DrawerDestinations {

    val items = listOf(
        DrawerDestination(
            route = "dashboard",
            label = "Dashboard",
            icon = Icons.Default.Dashboard
        ),
        DrawerDestination(
            route = "account",
            label = "Cuenta",
            icon = Icons.Default.Person
        ),
        DrawerDestination(
            route = "alerts",
            label = "Alertas",
            icon = Icons.Default.Notifications
        ),
        DrawerDestination(
            route = "settings",
            label = "Configuración",
            icon = Icons.Default.Settings
        ),
        DrawerDestination(
            route = "token",
            label = "Token de Acceso",
            icon = Icons.Default.VpnKey
        )
    )
}