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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.akameiot.app.ui.home.components.FilterMenu
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
                        message = " Suscrito a ${event.thingName}",
                        duration = SnackbarDuration.Long
                    )
                }

            }
        }
    }

    MainScaffold(
        titleContent = {
            val title = when {
                uiState.filterNetworks.size == 1 -> uiState.filterNetworks.first().displayName
                uiState.filterNetworks.size > 1 -> "${uiState.filterNetworks.size} Redes Seleccionadas"
                uiState.networks.size == 1 -> uiState.networks.first().displayName
                else -> "Telemetría"
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            var showFilterMenu by remember { mutableStateOf(false) }

            val availableMetrics = remember(uiState.telemetry) {
                uiState.telemetry.flatMap { it.nodes }.flatMap { it.metrics }
                    .map { it.name }.distinct().sorted()
            }

            IconButton(onClick = { showFilterMenu = true }) {
                Icon(Icons.Default.FilterList, contentDescription = null)
            }

            FilterMenu(
                expanded = showFilterMenu,
                onDismiss = { showFilterMenu = false },
                networks = uiState.networks,
                filterNetworks = uiState.filterNetworks,
                networksOrder = uiState.networksOrder,
                filterMetrics = uiState.filterMetrics,
                metricsOrder = uiState.metricsOrder,
                availableMetrics = availableMetrics,
                sortAscending = uiState.sortAscending,
                onToggleNetwork = { network, selected -> viewModel.filterByNetwork(network, selected) },
                onMoveNetworkUp = { viewModel.moveNetworkUp(it) },
                onToggleMetric = { metric, selected -> viewModel.filterByMetric(metric, selected) },
                onMoveMetricUp = { viewModel.moveMetricUp(it) },
                onSortAscending = { viewModel.setSortAscending(it) }
            )

            IconButton(onClick = { }) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
        },
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            val selectedNetwork = uiState.selectedNetwork

            if (uiState.telemetry.isEmpty() && !uiState.isLoading) {

                EmptyStateComponent(
                    title = "Conecta tu red IoT",
                    description = "Vincula tus dispositivos para comenzar a visualizar datos en tiempo real.",
                    actionText = "Iniciar",
                    onActionClick = {
                        showSheet = true
                    }
                )

            } else {

                // 1. Determinar redes a mostrar en orden preferido
                val networksToShow = if (uiState.filterNetworks.isEmpty() ||
                    uiState.networksOrder.isEmpty()) {
                    uiState.telemetry
                } else {
                    uiState.networksOrder.mapNotNull { thingName ->
                        uiState.telemetry.find { it.meshId == thingName }
                    }
                }

                // 2. Obtener nodos agrupados por red en orden preferido
                val nodes = networksToShow.flatMap { network ->
                    network.nodes.map { node ->
                        val orderedMetrics = if (uiState.metricsOrder.isEmpty()) {
                            node.metrics
                        } else {
                            val selected = uiState.metricsOrder.mapNotNull { name ->
                                node.metrics.find { it.name == name }
                            }
                            val rest = node.metrics.filter { it.name !in uiState.metricsOrder }
                            selected + rest
                        }
                        val filteredMetrics = if (uiState.filterMetrics.isEmpty()) {
                            orderedMetrics
                        } else {
                            orderedMetrics.filter { it.name in uiState.filterMetrics }
                        }
                        node.copy(metrics = filteredMetrics)
                    }.filter { it.metrics.isNotEmpty() }
                }

                // 3. Ordenar por valor de la primera métrica
                val sortedNodes = when (uiState.sortAscending) {
                    true  -> nodes.sortedBy { it.metrics.first().latestValue }
                    false -> nodes.sortedByDescending { it.metrics.first().latestValue }
                    null  -> nodes.sortedWith(compareBy({ it.networkName }, { it.nodeId }))
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = sortedNodes,
                        key = { "${it.networkName}_${it.nodeId}" }
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