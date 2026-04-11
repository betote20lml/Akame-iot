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
import com.akameiot.app.ui.home.components.ChartCard
import androidx.compose.material.icons.filled.Check



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
                color = MaterialTheme.colorScheme.onPrimary
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

            val availableMetrics = uiState.availableMetrics

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

            var showViewMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showViewMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }

                DropdownMenu(
                    expanded         = showViewMenu,
                    onDismissRequest = { showViewMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tarjetas") },
                        onClick = {
                            viewModel.setViewMode(HomeViewMode.CARDS)
                            showViewMenu = false
                        },
                        leadingIcon = {
                            if (uiState.viewMode == HomeViewMode.CARDS)
                                Icon(Icons.Default.Check, contentDescription = null)
                        }
                    )
                    HorizontalDivider()
                    listOf(
                        HomeViewMode.CHARTS_24H to "Gráficas · 24h",
                        HomeViewMode.CHARTS_7D  to "Gráficas · 7 días",
                        HomeViewMode.CHARTS_1M  to "Gráficas · 1 mes",
                        HomeViewMode.CHARTS_3M  to "Gráficas · 3 meses",
                        HomeViewMode.CHARTS_1Y to "Gráficas · 1 año"
                    ).forEach { (mode, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                viewModel.setViewMode(mode)
                                showViewMenu = false
                            },
                            leadingIcon = {
                                if (uiState.viewMode == mode)
                                    Icon(Icons.Default.Check, contentDescription = null)
                            }
                        )
                    }
                }
            }
        },
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {


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

                when {

                    uiState.viewMode == HomeViewMode.CARDS -> {
                        LazyColumn(
                            modifier            = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            contentPadding      = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.visibleNodes,
                                key   = { node -> "${node.networkName}_${node.nodeId}" }
                            ) { node ->
                                TelemetryCard(node = node)
                            }
                        }
                    }

                    uiState.viewMode.chartRange != null -> {
                        LazyColumn(
                            modifier            = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            contentPadding      = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.charts,
                                key   = { chart -> "${chart.meshId}_${chart.nodeId}" }
                            ) { chart ->
                                ChartCard(
                                    chart        = chart,
                                    globalNow = uiState.globalNow,
                                    onLoadPoints = { meshId, nodeId, metric, fromTs ->
                                        viewModel.loadChartPoints(meshId, nodeId, metric, fromTs)
                                    }
                                )
                            }
                        }
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