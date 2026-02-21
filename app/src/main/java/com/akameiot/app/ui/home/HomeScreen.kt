package com.akameiot.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.akameiot.coreui.components.*
import com.akameiot.app.ui.navigation.DrawerDestinations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    MainScaffold(
        title = "Telemetría",
        drawerState = drawerState,
        drawerContent = {

            AppDrawerHeader(
                networkName = "Akame Network",
                connectionStatus = "Broker MQTT conectado",
                isOnline = true
            )

            HorizontalDivider()

            val currentRoute = "dashboard"

            DrawerDestinations.items.forEach { destination ->

                AppDrawerItem(
                    label = destination.label,
                    selected = currentRoute == destination.route,
                    onClick = {
                        navController.navigate(destination.route)
                        scope.launch { drawerState.close() }
                    },
                    icon = destination.icon
                )
            }
        },
        onNavigationClick = {
            scope.launch { drawerState.open() }
        },
        actions = {
            IconButton(onClick = { }) {
                Icon(Icons.Default.FilterList, contentDescription = null)
            }

            IconButton(onClick = { }) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            EmptyStateComponent(
                title = "Conecta tu red IoT",
                description = "Vincula tus dispositivos para comenzar a visualizar datos en tiempo real.",
                actionText = "Iniciar",
                onActionClick = {
                    showSheet = true
                }
            )
        }
    }
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            ActivateDeviceSheet(
                onActivate = { code, displayName ->
                    showSheet = false
                },

            )
        }
    }
}