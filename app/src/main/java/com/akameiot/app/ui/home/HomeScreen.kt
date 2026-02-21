package com.akameiot.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import com.akameiot.coreui.components.AppFloatingButton
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.akameiot.coreui.components.MainScaffold
import com.akameiot.coreui.components.AppDrawerHeader

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {

    MainScaffold(
        title = "Telemetría",
        drawerContent = {

            AppDrawerHeader(
                networkName = "Akame Sensor Network",
                connectionStatus = "Broker MQTT conectado",
                isOnline = true
            )

            HorizontalDivider()

            NavigationDrawerItem(
                label = { Text("Dashboard") },
                selected = true,
                onClick = { }
            )

            NavigationDrawerItem(
                label = { Text("Sensores") },
                selected = false,
                onClick = { }
            )

            NavigationDrawerItem(
                label = { Text("Configuración") },
                selected = false,
                onClick = { }
            )
        },
        onMenuClick = { drawerState ->
            drawerState.open()
        },
        actions = {
            IconButton(onClick = { }) {
                Icon(Icons.Default.FilterList, null)
            }

            IconButton(onClick = { }) {
                Icon(Icons.Default.MoreVert, null)
            }
        },
        floatingActionButton = {
            AppFloatingButton(
                onClick = { }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // contenido futuro
        }
    }
}