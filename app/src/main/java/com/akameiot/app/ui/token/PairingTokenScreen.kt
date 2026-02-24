package com.akameiot.app.ui.token

import android.content.ClipData
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.akameiot.app.ui.navigation.Routes
import com.akameiot.coreui.components.AuthHeader
import com.akameiot.coreui.components.AuthScaffold
import com.akameiot.coreui.components.SecondaryButton
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import kotlinx.coroutines.launch

@Composable
fun PairingTokenScreen(navController: NavController) {

    val viewModel: PairingTokenViewModel =
        viewModel(factory = PairingTokenViewModelFactory())

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PairingTokenEvent.ShowError ->
                    snackbarHostState.showSnackbar(
                        event.message,
                        duration = SnackbarDuration.Long
                    )

                PairingTokenEvent.NavigateToLogin ->
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
            }
        }
    }


    AuthScaffold(
        snackbarHostState = snackbarHostState,
        topContent = {
            AuthHeader(text = "Token de Acceso")
        }
    ) {

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Este token permite que otro dispositivo se vincule a tu red IoT.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
            return@AuthScaffold
        }

        uiState.token?.let { pairingToken ->

            val qrPainter = rememberQrCodePainter(data = pairingToken.token)

            Image(
                painter = qrPainter,
                contentDescription = "QR del token de acceso",
                modifier = Modifier.size(220.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (uiState.secondsLeft > 0)
                    "Expira en ${uiState.secondsLeft} segundos"
                else
                    "Regenerando...",
                style = MaterialTheme.typography.labelMedium,
                color = if (uiState.secondsLeft > 10)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(24.dp))

            SecondaryButton(
                text = "Copiar token",
                icon = Icons.Default.ContentCopy,
                onClick = {
                    scope.launch {
                        clipboard.setClipEntry(
                            ClipData
                                .newPlainText("token", pairingToken.token)
                                .toClipEntry()
                        )
                        snackbarHostState.showSnackbar("Token copiado")
                    }
                }
            )
        }
    }
}