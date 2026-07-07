package com.akameiot.app.ui.navigation


import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.akameiot.coreui.theme.ThemeController
import com.akameiot.di.AppModule
import com.akameiot.app.R


@Composable
fun AppDrawerContent(
    navController: NavController,
    scope: CoroutineScope,
    drawerState: DrawerState,
    drawerUiState: DrawerUiState,
    appUser: AppUser? = null,

) {
    Column(modifier = Modifier.fillMaxSize()) {

        val isDark by ThemeController.isDark.collectAsState()
        AppDrawerHeader(
            connectionStatus = drawerUiState.connectionStatus,
            connectionLevel = drawerUiState.connectionLevel,
            appIcon = {
                Image(
                    painter = painterResource(
                        id = if (isDark == true) {
                            R.drawable.logo_dark
                        } else {
                            R.drawable.logo_light
                        }
                    ),
                    contentDescription = "App logo",
                    modifier = Modifier.size(110.dp)
                )
            }
        )

        HorizontalDivider()

        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            ?.substringBefore("?")
        var showIndexFactorySubmenu by remember { mutableStateOf(false) }

        DrawerDestinations.items
            .filter { destination ->
                if (destination.route == Routes.TOKEN) {
                    appUser is AppUser.Owner
                } else {
                    true
                }
            }
            .forEach { destination ->
                if (destination.isIndexFactory) {

                    AppDrawerItem(
                        label = destination.label,
                        selected = false,
                        onClick = { showIndexFactorySubmenu = !showIndexFactorySubmenu },
                        icon = destination.icon,
                    )

                    if (showIndexFactorySubmenu) {
                        drawerUiState.metrics.forEach { metricKey ->
                            val label = drawerUiState.metricsDisplay[metricKey] ?: metricKey
                            AppDrawerItem(
                                label = "   $label",
                                selected = currentRoute == Routes.indexFactory(metricKey),
                                onClick = {
                                    navController.navigate(Routes.indexFactory(metricKey))
                                    scope.launch { drawerState.close() }
                                },
                                icon = null,
                            )
                        }
                    }

                } else {
                    AppDrawerItem(
                        label = destination.label,
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route)
                            scope.launch { drawerState.close() }
                        },
                        icon = destination.icon,
                    )
                }
            }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp), // ← menos padding vertical
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { 
                    ThemeController.toggle { newValue ->
                        AppModule.themeStore.setDark(newValue)
                    }
                }
            ) {
                Icon(
                    imageVector =
                        if (isDark == true)
                            Icons.Default.LightMode
                        else
                            Icons.Default.DarkMode,
                    contentDescription = "Toggle theme"
                )
            }

            Text(
                text  = "v 1.0.3",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}