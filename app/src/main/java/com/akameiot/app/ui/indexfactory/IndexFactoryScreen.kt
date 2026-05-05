package com.akameiot.app.ui.indexfactory

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.akameiot.app.ui.indexfactory.components.LimitEditorCard
import com.akameiot.app.ui.navigation.AppDrawerContent
import com.akameiot.app.ui.navigation.DrawerViewModel
import com.akameiot.app.ui.navigation.DrawerViewModelFactory
import com.akameiot.coreui.components.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexFactoryScreen(
    navController: NavController,
    metricKey: String?,
) {
    val viewModel: IndexFactoryViewModel =
        viewModel(factory = IndexFactoryViewModelFactory())
    val uiState by viewModel.uiState.collectAsState()

    // Drawer data (metrics list for submenu)
    val drawerViewModel: DrawerViewModel = viewModel(factory = DrawerViewModelFactory())
    val drawerUiState by drawerViewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(metricKey) {
        metricKey?.let { viewModel.selectMetric(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is IndexFactoryEvent.ShowError ->
                    snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Long)

                IndexFactoryEvent.ExportChanges ->
                    snackbarHostState.showSnackbar(
                        "Límites exportados correctamente",
                        duration = SnackbarDuration.Short,
                    )

                IndexFactoryEvent.RecoverFromCloud ->
                    snackbarHostState.showSnackbar(
                        "Límites recuperados desde la nube",
                        duration = SnackbarDuration.Short,
                    )

                is IndexFactoryEvent.LimitSaved ->
                    snackbarHostState.showSnackbar(
                        "Límites guardados para ${event.nodeName}",
                        duration = SnackbarDuration.Short,
                    )
            }
        }
    }

    LaunchedEffect(uiState.searchActive) {
        if (uiState.searchActive) searchFocusRequester.requestFocus()
    }

    MainScaffold(
        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
        titleContent = {
            AnimatedContent(
                targetState = uiState.searchActive,
                transitionSpec = {
                    fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically()
                },
                label = "SearchTransition",
            ) { isSearchActive ->
                if (isSearchActive) {
                    TextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        placeholder = {
                            Text(
                                "Buscar sensor…",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor   = MaterialTheme.colorScheme.primary.copy(alpha = 0f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0f),
                            focusedIndicatorColor   = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                            cursorColor             = MaterialTheme.colorScheme.onPrimary,
                            focusedTextColor        = MaterialTheme.colorScheme.onPrimary,
                            unfocusedTextColor      = MaterialTheme.colorScheme.onPrimary,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(searchFocusRequester),
                    )
                } else {
                    // selectedMetricDisplay is already resolved in the ViewModel — no DrawerViewModel needed here
                    Text(
                        text = "Crear Índice",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        drawerState = drawerState,
        snackbarHostState = snackbarHostState,
        drawerContent = {
            // Shared drawer — no duplication
            AppDrawerContent(
                navController  = navController,
                scope          = scope,
                drawerState    = drawerState,
                drawerUiState  = drawerUiState,
            )
        },

        onNavigationClick = { navController.popBackStack() },
        actions = {
            // Search toggle
            IconButton(onClick = { viewModel.setSearchActive(!uiState.searchActive) }) {
                Icon(
                    imageVector = if (uiState.searchActive) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (uiState.searchActive) "Cerrar búsqueda" else "Buscar sensor",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }

            // More menu
            var showMoreMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showMoreMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                AppDropdownMenu(expanded = showMoreMenu, onDismiss = { showMoreMenu = false }) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Exportar cambios",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                ),
                            )
                        },
                        onClick = { showMoreMenu = false; viewModel.exportChanges() },
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                        modifier = Modifier.height(44.dp),
                    )
                    AppMenuDivider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Recuperar desde la nube",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                ),
                            )
                        },
                        onClick = { showMoreMenu = false; viewModel.recoverFromCloud() },
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                        modifier = Modifier.height(44.dp),
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                // Loading — no metric selected yet
                uiState.isLoading && uiState.selectedMetric == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                // Loading items for the selected metric
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                // No nodes match search / metric
                uiState.selectedMetric != null && uiState.visibleItems.isEmpty() -> {
                    NoSensorsState(
                        metricName = uiState.selectedMetricDisplay ?: "",
                        isFiltered = uiState.searchQuery.isNotEmpty(),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(
                            items = uiState.visibleItems,
                            key = { "${it.item.meshId}_${it.item.nodeId}_${it.item.metricKey}" }
                        ) { item ->

                            LimitEditorCard(
                                item            = item.item,
                                userMin         = item.userMin,
                                userMax         = item.userMax,
                                onUserMinChange = { viewModel.onUserMinChange(item.item.nodeId, it) },
                                onUserMaxChange = { viewModel.onUserMaxChange(item.item.nodeId, it) },
                                onSave          = { viewModel.saveLimit(item.item) },
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun NoSensorsState(metricName: String, isFiltered: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.SensorsOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(64.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (isFiltered) "Sin resultados" else "Sin sensores",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isFiltered) "Ningún nodo coincide con la búsqueda."
                else "No hay nodos que reporten $metricName.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}