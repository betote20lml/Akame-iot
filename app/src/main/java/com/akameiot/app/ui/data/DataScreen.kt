package com.akameiot.app.ui.data

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.akameiot.app.ui.navigation.AppDrawerContent
import com.akameiot.app.ui.navigation.DrawerViewModel
import com.akameiot.app.ui.navigation.DrawerViewModelFactory
import com.akameiot.coreui.components.MainScaffold
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.ui.text.style.TextAlign
import com.akameiot.coreui.components.PrimaryButton
import com.akameiot.coreui.components.SecondaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataScreen(navController: NavController) {

    val viewModel: DataViewModel = viewModel(factory = DataViewModelFactory())
    val uiState by viewModel.uiState.collectAsState()

    val drawerViewModel: DrawerViewModel = viewModel(factory = DrawerViewModelFactory())
    val drawerUiState by drawerViewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.exportCsv(context)
    }

    fun requestExport() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            viewModel.exportCsv(context)
        }
    }

    // Eventos
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DataEvent.ExportSuccess ->
                    snackbarHostState.showSnackbar(
                        "Guardado en Descargas: ${event.path}",
                        duration = SnackbarDuration.Long,
                    )
                is DataEvent.ShowError ->
                    snackbarHostState.showSnackbar(
                        event.message,
                        duration = SnackbarDuration.Long,
                    )
            }
        }
    }

    MainScaffold(
        navigationIcon    = Icons.AutoMirrored.Filled.ArrowBack,
        onNavigationClick = { navController.popBackStack() },
        titleContent = {
            Text(
                text  = "Datos",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        },
        drawerState       = drawerState,
        snackbarHostState = snackbarHostState,
        drawerContent = {
            AppDrawerContent(
                navController = navController,
                scope         = scope,
                drawerState   = drawerState,
                drawerUiState = drawerUiState,
            )
        },
        actions = {},
    ) { padding ->

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@MainScaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {

            FilterDropdown(
                label       = "Red",
                allLabel    = "Todas las redes",
                options     = uiState.networks.map { it.thingName to it.displayName },
                selected    = uiState.selectedNetworkId,
                onSelect    = { viewModel.selectNetwork(it) },
            )


            FilterDropdown(
                label    = "Variable",
                allLabel = "Todas las variables",
                options  = uiState.metrics.map { key ->
                    key to (uiState.metricsDisplay[key] ?: key)
                },
                selected = uiState.selectedMetric,
                onSelect = { viewModel.selectMetric(it) },
            )


            PrimaryButton(
                text    = "Respaldo de datos",
                onClick = { requestExport() },
                enabled = !uiState.isExporting,
                loading = uiState.isExporting,
                icon    = Icons.Default.Download,
            )


            Spacer(Modifier.weight(1f))


            if (uiState.canRecoverHistoricalData) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    Text(
                        text  = "Si las sincronizaciones automáticas fallaron, puedes recuperar los datos históricos desde la nube.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    SecondaryButton(
                        text    = "Recuperar datos históricos",
                        onClick = { /* TODO */ },
                        icon    = Icons.Default.CloudDownload,
                    )
                }

             }
        }
    }
}

// ── Componente: Dropdown de filtro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(
    label    : String,
    allLabel : String,
    options  : List<Pair<String, String>>,
    selected : String?,
    onSelect : (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val displaySelected = if (selected == null) allLabel
    else options.firstOrNull { it.first == selected }?.second ?: selected

    ExposedDropdownMenuBox(
        expanded         = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value         = displaySelected,
            onValueChange = {},
            readOnly      = true,
            label         = { Text(label) },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors        = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedBorderColor      = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor    = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                focusedLabelColor       = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor     = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTextColor        = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedTextColor      = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            shape    = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
        )

        ExposedDropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text    = { Text(allLabel, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                onClick = { onSelect(null); expanded = false },
            )
            HorizontalDivider()
            options.forEach { (key, display) ->
                DropdownMenuItem(
                    text    = { Text(display, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = { onSelect(key); expanded = false },
                )
            }
        }
    }
}


