package com.akameiot.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.akameiot.app.fcm.FcmEventBus
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.akameiot.app.ui.home.components.NetworkDropdown
import com.akameiot.app.ui.home.components.TelemetryCard
import kotlinx.coroutines.launch
import com.akameiot.coreui.components.*
import com.akameiot.app.ui.navigation.DrawerDestinations
import com.akameiot.app.ui.navigation.Routes
import com.akameiot.app.ui.navigation.Routes.LOGIN
import com.akameiot.domain.model.AppUser


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


    //aqui si eliminamos el launchedeffect no se procesa el mensaje, podriamos eliminar unicamente la parte de snackbar?
    LaunchedEffect(Unit) {
        FcmEventBus.events.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
        }
    }


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
                is HomeEvent.ShowDeviceId -> {
                    showSheet = false
                    snackbarHostState.showSnackbar(
                        message = "Device ID: ${event.deviceId}",
                        duration = SnackbarDuration.Long
                    )
                }
                is HomeEvent.SubscribedToDevice -> {
                    showSheet = false
                    snackbarHostState.showSnackbar(
                        message = "✅ Suscrito a ${event.thingName}",
                        duration = SnackbarDuration.Long
                    )
                }

            }
        }
    }

    MainScaffold(
        titleContent = {
            NetworkDropdown(
                networks = uiState.networks,
                selectedNetwork = uiState.selectedNetwork,
                selectedNetworkInfo = uiState.selectedNetworkInfo,
                onNetworkSelected = { viewModel.selectNetwork(it) }
            )
        },
        drawerState = drawerState,
        snackbarHostState = snackbarHostState,
        drawerContent = {

            AppDrawerHeader(
                networkName = "Akame Network",
                connectionStatus = "Broker MQTT conectado",
                isOnline = true
            )

            HorizontalDivider()

            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            val currentUser = uiState.appUser
            DrawerDestinations.items.filter { destination ->
                destination.route != Routes.TOKEN || currentUser !is AppUser.Limited
            }.forEach { destination ->

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

            val selectedNetwork = uiState.selectedNetwork

            if (uiState.telemetry.isEmpty()) {

                EmptyStateComponent(
                    title = "Conecta tu red IoT",
                    description = "Vincula tus dispositivos para comenzar a visualizar datos en tiempo real.",
                    actionText = "Iniciar",
                    onActionClick = {
                        showSheet = true
                    }
                )

            } else {

                val nodes = selectedNetwork?.nodes ?: emptyList()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = nodes,
                        key = { it.nodeId }
                    ) { node ->
                        TelemetryCard(node = node)
                    }
                }
            }

            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    sheetState = sheetState
                ) {
                    ActivateDeviceSheet(
                        isLoading = uiState.isLoading,
                        appUser = uiState.appUser,
                        onActivate = { code, displayName ->
                            viewModel.onSheetAction(code, displayName)
                        }
                    )
                }
            }
        }
    }
}