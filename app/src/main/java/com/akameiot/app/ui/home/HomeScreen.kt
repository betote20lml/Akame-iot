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
import androidx.compose.ui.unit.sp
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
import com.akameiot.app.ui.home.model.ChartPointsKey


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


                AppDropdownMenu(
                    expanded = showViewMenu,
                    onDismiss = { showViewMenu = false }
                ) {
                    AppMenuCheckItem(
                        label = "Tarjetas",
                        checked = uiState.viewMode == HomeViewMode.CARDS,
                        onClick = {
                            viewModel.setViewMode(HomeViewMode.CARDS)
                            showViewMenu = false
                        }
                    )
                    AppMenuDivider()
                    listOf(
                        HomeViewMode.CHARTS_24H to "Gráficas · 24h",
                        HomeViewMode.CHARTS_7D  to "Gráficas · 7 días",
                        HomeViewMode.CHARTS_1M  to "Gráficas · 1 mes",
                        HomeViewMode.CHARTS_3M  to "Gráficas · 3 meses",
                        HomeViewMode.CHARTS_1Y to "Gráficas · 1 año"
                    ).forEach { (mode, label) ->
                        AppMenuCheckItem(
                            label = label,
                            checked = uiState.viewMode == mode,
                            onClick = {
                                viewModel.setViewMode(mode)
                                showViewMenu = false
                            }
                        )
                    }
                    AppMenuDivider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Agregar red",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                )
                            )
                        },
                        onClick = {
                            showViewMenu = false
                            showSheet = true
                        },
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                        modifier = Modifier.height(44.dp)
                    )
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

                if (uiState.viewMode == HomeViewMode.CARDS) {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.visibleNodes,
                            key = { node -> "${node.networkName}_${node.nodeId}" }
                        ) { node ->
                            TelemetryCard(node = node)
                        }
                    }

                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.visibleNodes,
                            key = { node -> "${node.meshId}_${node.nodeId}" }
                        ) { node ->
                            val metric = node.metrics.firstOrNull()
                            if (metric != null) {
                                val key = ChartPointsKey(
                                    meshId = node.meshId,
                                    nodeId = node.nodeId,
                                    metric = metric.name,
                                    range  = uiState.viewMode.chartRange
                                )
                                val pts = uiState.chartPoints[key] ?: emptyList()

                                ChartCard(
                                    chart = com.akameiot.app.ui.home.model.ChartUiModel(
                                        nodeId        = node.nodeId,
                                        meshId        = node.meshId,
                                        networkName   = node.networkName,
                                        metricName    = metric.name,
                                        chartRange    = uiState.viewMode.chartRange,
                                        isStale       = node.isStale,
                                        isStaleByTime = node.isStaleByTime
                                    ),
                                    globalNow = uiState.globalNow,
                                    points    = pts
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