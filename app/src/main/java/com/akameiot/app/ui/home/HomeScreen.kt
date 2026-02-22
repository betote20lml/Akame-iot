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
import com.akameiot.app.ui.navigation.Routes.LOGIN


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory())
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = " ${event.message}",
                        duration = SnackbarDuration.Long
                    )
                }
                is HomeEvent.NavigateToDetails -> {
                    showSheet = false
                    snackbarHostState.showSnackbar(
                        message = " meshId: ${event.thingName}",
                        duration = SnackbarDuration.Long
                    )
                }
                is HomeEvent.NavigateToLogin -> {
                    navController.navigate(LOGIN) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
                is HomeEvent.ActivationCodeInvalid -> {

                    showSheet = false
                    scope.launch {
                        sheetState.hide()
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            duration = SnackbarDuration.Long
                        )
                    }
                }
            }
        }
    }

    MainScaffold(
        title = "Telemetría",
        drawerState = drawerState,
        snackbarHostState = snackbarHostState,
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

            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    sheetState = sheetState
                ) {
                    ActivateDeviceSheet(
                        isLoading = uiState.isLoading,
                        onActivate = { code, displayName ->
                            viewModel.activateDevice(code, displayName)
                        }
                    )
                }
            }
        }

    }

}