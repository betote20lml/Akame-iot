package com.akameiot.app.ui.qrauth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.akameiot.coreui.components.AuthHeader
import com.akameiot.coreui.components.AuthScaffold
import com.akameiot.coreui.components.PrimaryButton
import com.akameiot.coreui.theme.LocalSpacing
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalClipboard
import com.akameiot.app.ui.navigation.Routes
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import android.Manifest
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrAuthScreen(
    navController: NavController,
    viewModel: QrAuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = QrAuthViewModelFactory()
    )
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val spacing = LocalSpacing.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                QrAuthEvent.Success -> {
                    navController.navigate(Routes.HOME) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }
                is QrAuthEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    AuthScaffold(snackbarHostState = snackbarHostState) {

        AuthHeader(text = "Acceso rápido")

        Spacer(modifier = Modifier.height(spacing.sm))

        Text(
            text = "Escanea el código QR desde el dispositivo principal para copiar la sesión de forma segura.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(spacing.xl))

        val configuration = LocalConfiguration.current
        val scannerHeight =
            if (configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE)
                220.dp
            else
                320.dp

        when {
            cameraPermissionState.status.isGranted -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(scannerHeight)
                        .clip(RoundedCornerShape(24.dp)),
                ) {
                    QrScannerComposable(
                        modifier = Modifier.fillMaxSize(),
                        onQrDetected = { token ->
                            viewModel.onQrScanned(token)
                        }
                    )
                    QrScannerOverlay()
                }
            }

            cameraPermissionState.status.shouldShowRationale -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(scannerHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Se necesita acceso a la cámara para escanear el código QR.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                            Text("Conceder permiso")
                        }
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(scannerHeight),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.xl))

        PrimaryButton(
            text = if (state.isLoading) "Procesando..." else "Pegar token desde portapapeles",
            onClick = {
                scope.launch {
                    val clip = clipboard.getClipEntry()
                    val token = clip?.clipData?.getItemAt(0)?.text?.toString()
                    if (token.isNullOrBlank()) {
                        snackbarHostState.showSnackbar(
                            message = "No se encontró ningún código en el portapapeles"
                        )
                    } else {
                        viewModel.pasteToken(token)
                    }
                }
            },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}