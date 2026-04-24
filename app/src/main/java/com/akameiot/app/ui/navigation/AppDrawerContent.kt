package com.akameiot.app.ui.navigation

import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.akameiot.coreui.components.AppDrawerHeader
import com.akameiot.coreui.components.AppDrawerItem
import com.akameiot.domain.model.AppUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Single source of truth for the navigation drawer content.
 * Used by HomeScreen and IndexFactoryScreen (and any future screens)
 * so drawer logic is never duplicated.
 */
@Composable
fun AppDrawerContent(
    navController: NavController,
    scope: CoroutineScope,
    drawerState: DrawerState,
    drawerUiState: DrawerUiState,
    appUser: AppUser? = null,
    networkName: String = "Akame Network",
    connectionStatus: String = "Broker MQTT conectado",
    isOnline: Boolean = true,
) {
    AppDrawerHeader(
        networkName = networkName,
        connectionStatus = connectionStatus,
        isOnline = isOnline,
    )

    HorizontalDivider()

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    var showIndexFactorySubmenu by remember { mutableStateOf(false) }

    DrawerDestinations.items
        .filter { destination ->
            // Hide token screen for limited users
            destination.route != Routes.TOKEN || appUser !is AppUser.Limited
        }
        .forEach { destination ->
            if (destination.isIndexFactory) {

                AppDrawerItem(
                    label    = destination.label,
                    selected = false,
                    onClick  = { showIndexFactorySubmenu = !showIndexFactorySubmenu },
                    icon     = destination.icon,
                )

                if (showIndexFactorySubmenu) {
                    drawerUiState.metrics.forEach { metricKey ->
                        val label = drawerUiState.metricsDisplay[metricKey] ?: metricKey
                        AppDrawerItem(
                            label    = "   $label",
                            selected = currentRoute == Routes.indexFactory(metricKey),
                            onClick  = {
                                navController.navigate(Routes.indexFactory(metricKey))
                                scope.launch { drawerState.close() }
                            },
                            icon = null,
                        )
                    }
                }

            } else {
                AppDrawerItem(
                    label    = destination.label,
                    selected = currentRoute == destination.route,
                    onClick  = {
                        navController.navigate(destination.route)
                        scope.launch { drawerState.close() }
                    },
                    icon = destination.icon,
                )
            }
        }
}