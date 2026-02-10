package com.akameiot.app.ui.qrauth

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.akameiot.coreui.components.AuthHeader
import com.akameiot.coreui.components.AuthScaffold
import com.akameiot.coreui.components.PrimaryButton
import com.akameiot.coreui.theme.LocalSpacing
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.getValue
import com.akameiot.app.ui.navigation.Routes


@Composable
fun QrAuthScreen(
    navController: NavController,
    viewModel: QrAuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {

    val spacing = LocalSpacing.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {

        viewModel.events.collectLatest { event ->

            when(event) {

                QrAuthEvent.Success -> {

                    navController.navigate(Routes.LANDING) {
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

    AuthScaffold(
        snackbarHostState = snackbarHostState
    ) {

        AuthHeader(
            text = "Acceso rápido"
        )

        Spacer(modifier = Modifier.height(spacing.sm))

        Text(
            text = "Escanea el código QR desde el dispositivo principal para copiar la sesión de forma segura.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(spacing.xl))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {

            if(state.isLoading) {

                CircularProgressIndicator()

            } else {

                Text(
                    text = "Área de escaneo QR",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.xl))

        PrimaryButton(
            text = if(state.isLoading)
                "Procesando..."
            else
                "Pegar token de acceso desde el portapapeles",

            onClick = { viewModel.pasteToken() },

            enabled = !state.isLoading,

            modifier = Modifier.fillMaxWidth()
        )
    }
}
