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
import com.akameiot.app.ui.home.components.ChartCard
import com.akameiot.app.ui.home.components.FilterMenu
import com.akameiot.app.ui.home.components.TelemetryCard
import com.akameiot.app.ui.home.model.ChartPointsKey
import com.akameiot.app.ui.home.model.ChartUiModel
import com.akameiot.app.ui.navigation.AppDrawerContent
import com.akameiot.app.ui.navigation.DrawerViewModel
import com.akameiot.app.ui.navigation.DrawerViewModelFactory
import com.akameiot.app.ui.navigation.Routes.LOGIN
import com.akameiot.coreui.components.*
import kotlinx.coroutines.launch
import com.akameiot.app.ui.home.components.ViewMenu
import androidx.compose.animation.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    loginMode: String? = null,
) {

    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory())
    val uiState by viewModel.uiState.collectAsState()

    // Drawer data (metrics list for submenu)
    val drawerViewModel: DrawerViewModel = viewModel(factory = DrawerViewModelFactory())
    val drawerUiState by drawerViewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(loginMode) {
        viewModel.onLoginMode(loginMode)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message  = " ${event.message}",
                        duration = SnackbarDuration.Long,
                    )
                }
                is HomeEvent.NavigateToDetails -> {
                    showSheet = false
                    snackbarHostState.showSnackbar(
                        message  = " meshId: ${event.thingName}",
                        duration = SnackbarDuration.Long,
                    )
                }
                is HomeEvent.NavigateToLogin -> {
                    navController.navigate(LOGIN) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
                is HomeEvent.ActivationCodeInvalid -> {
                    showSheet = false
                    scope.launch {
                        sheetState.hide()
                        snackbarHostState.showSnackbar(
                            message  = event.message,
                            duration = SnackbarDuration.Long,
                        )
                    }
                }
                is HomeEvent.ShowDeviceId -> {
                    showSheet = false
                    snackbarHostState.showSnackbar(
                        message  = "Device ID: ${event.deviceId}",
                        duration = SnackbarDuration.Long,
                    )
                }
                is HomeEvent.SubscribedToDevice -> {
                    showSheet = false
                    snackbarHostState.showSnackbar(
                        message  = " Suscrito a ${event.thingName}",
                        duration = SnackbarDuration.Long,
                    )
                }
            }
        }
    }

    MainScaffold(
        titleContent = {
            val title = when (uiState.viewMode) {

                HomeViewMode.CARDS -> {
                    when {
                        uiState.filterNetworks.size == 1 ->
                            uiState.filterNetworks.first().displayName

                        uiState.filterNetworks.size > 1 ->
                            "${uiState.filterNetworks.size} Redes Seleccionadas"

                        uiState.networks.size == 1 ->
                            uiState.networks.first().displayName

                        else -> "Telemetría"
                    }
                }

                else -> {
                    when (uiState.viewMode) {
                        HomeViewMode.CHARTS_24H -> "Gráficas · 24h"
                        HomeViewMode.CHARTS_7D  -> "Gráficas · 7 días"
                        HomeViewMode.CHARTS_1M  -> "Gráficas · 1 mes"
                        HomeViewMode.CHARTS_3M  -> "Gráficas · 3 meses"
                        HomeViewMode.CHARTS_1Y  -> "Gráficas · 1 año"
                        else -> "Gráficas"
                    }
                }
            }
            Text(
                text  = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        },
        drawerState      = drawerState,
        snackbarHostState = snackbarHostState,
        drawerContent = {
            // Shared drawer — single source of truth
            AppDrawerContent(
                navController = navController,
                scope         = scope,
                drawerState   = drawerState,
                drawerUiState = drawerUiState,
                appUser       = uiState.appUser,
            )
        },
        onNavigationClick = {
            scope.launch {
                drawerViewModel.refreshConnectionStatus()
                drawerState.open()
            }
        },
        actions = {
            var showFilterMenu by remember { mutableStateOf(false) }

            IconButton(onClick = { showFilterMenu = true }) {
                Icon(Icons.Default.FilterList, contentDescription = null)
            }

            FilterMenu(
                expanded        = showFilterMenu,
                onDismiss       = { showFilterMenu = false },
                networks        = uiState.networks,
                filterNetworks  = uiState.filterNetworks,
                networksOrder   = uiState.networksOrder,
                filterMetrics   = uiState.filterMetrics,
                metricsOrder    = uiState.metricsOrder,
                availableMetrics = uiState.availableMetrics,
                sortAscending   = uiState.sortAscending,
                onToggleNetwork = { network, selected -> viewModel.filterByNetwork(network, selected) },
                onMoveNetworkUp = { viewModel.moveNetworkUp(it) },
                onToggleMetric  = { metric, selected -> viewModel.filterByMetric(metric, selected) },
                onMoveMetricUp  = { viewModel.moveMetricUp(it) },
                onSortAscending = { viewModel.setSortAscending(it) },
            )

            var showViewMenu by remember { mutableStateOf(false) }

            Box {
                IconButton(onClick = { showViewMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }

                ViewMenu(
                    expanded = showViewMenu,
                    onDismiss = { showViewMenu = false },
                    currentMode = uiState.viewMode,
                    onChangeMode = { viewModel.setViewMode(it) },
                    onAddNetwork = { showSheet = true },
                )
            }
        },
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (uiState.telemetry.isEmpty() && !uiState.isLoading) {

                EmptyStateComponent(
                    title       = "Conecta tu red IoT",
                    description = "Vincula tus dispositivos para comenzar a visualizar datos en tiempo real.",
                    actionText  = "Iniciar",
                    onActionClick = { showSheet = true },
                )

            } else {

                val currentMode = uiState.viewMode

                AnimatedContent(
                    targetState = currentMode,
                    transitionSpec = {
                        val fromIndex = initialState.indexIn()
                        val toIndex = targetState.indexIn()
                        val lastIndex = VIEW_MODE_ORDER.lastIndex

                        val forward = when {
                            fromIndex == lastIndex && toIndex == 0 -> true
                            fromIndex == 0 && toIndex == lastIndex -> false
                            else -> toIndex > fromIndex
                        }

                        if (forward) {
                            slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith
                                    slideOutHorizontally { it } + fadeOut()
                        }
                    },
                    label = "view_mode_transition",
                ) { mode ->
                    if (mode == HomeViewMode.CARDS) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp)
                                .swipeToChangeMode(
                                    onSwipeLeft  = { viewModel.setViewMode(uiState.viewMode.next()) },
                                    onSwipeRight = { viewModel.setViewMode(uiState.viewMode.previous()) },
                                ),
                            contentPadding      = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(
                                items = uiState.visibleNodes,
                                key   = { node -> "${node.networkName}_${node.nodeId}" },
                            ) { node ->
                                TelemetryCard(node = node)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp)
                                .swipeToChangeMode(
                                    onSwipeLeft  = { viewModel.setViewMode(uiState.viewMode.next()) },
                                    onSwipeRight = { viewModel.setViewMode(uiState.viewMode.previous()) },
                                ),
                            contentPadding      = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(
                                items = uiState.visibleNodes,
                                key   = { node -> "${node.meshId}_${node.nodeId}" },
                            ) { node ->
                                val metric = node.metrics.firstOrNull()
                                if (metric != null) {
                                    val key = ChartPointsKey(
                                        meshId = node.meshId,
                                        nodeId = node.nodeId,
                                        metric = metric.name,
                                        range  = mode.chartRange,
                                    )
                                    val pts = uiState.chartPoints[key] ?: emptyList()
                                    ChartCard(
                                        chart = ChartUiModel(
                                            nodeId        = node.nodeId,
                                            meshId        = node.meshId,
                                            networkName   = node.networkName,
                                            metricName    = metric.name,
                                            chartRange    = mode.chartRange,
                                            isStale       = node.isStale,
                                            isStaleByTime = node.isStaleByTime,
                                        ),
                                        globalNow = uiState.globalNow,
                                        points    = pts,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    sheetState       = sheetState,
                ) {
                    ActivateDeviceSheet(
                        isLoading = uiState.isLoading,
                        appUser   = uiState.appUser,
                        onActivate = { code, displayName ->
                            viewModel.onSheetAction(code, displayName)
                        },
                    )
                }
            }
        }
    }
}