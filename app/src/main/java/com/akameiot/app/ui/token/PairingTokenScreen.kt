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
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import com.akameiot.coreui.components.MainScaffold
import com.akameiot.data.session.DeviceNetworkStore
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.akameiot.coreui.components.PrimaryButton
import com.akameiot.coreui.theme.LocalAppColors

@Composable
fun PairingTokenScreen(navController: NavController) {

    val viewModel: PairingTokenViewModel =
        viewModel(factory = PairingTokenViewModelFactory())

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboard.current
    val colors = LocalAppColors.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val networkStore = remember { DeviceNetworkStore(context) }
    val networks by networkStore.networksFlow().collectAsState(initial = emptyList())


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


    val drawerState = rememberDrawerState(DrawerValue.Closed)

    MainScaffold(
        titleContent = { Text("Token de Acceso") },
        drawerState = drawerState,
        snackbarHostState = snackbarHostState,
        drawerContent = {},
        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
        onNavigationClick = { navController.popBackStack() }
    ) { padding ->

        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding()
            ),
            modifier = Modifier.fillMaxSize()
        ) {

            // MENSAJE INICIAL
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "1. Copia y comparte este token para permitir que otro dispositivo se vincule a tu cuenta",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))
            }


            item {

                val pairingToken = uiState.token

                // QR / spinner — mismo Box, sin duplicar espacio
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading || pairingToken == null) {
                        CircularProgressIndicator()
                    } else {
                        val qrPainter = rememberQrCodePainter(data = pairingToken.token)
                        Image(
                            painter = qrPainter,
                            contentDescription = "QR del token de acceso",
                            modifier = Modifier.size(220.dp)
                        )
                    }
                }

                if (uiState.isLoading || pairingToken == null) return@item

                Spacer(modifier = Modifier.height(16.dp))

                // EXPIRACIÓN CENTRADA
                Text(
                    text = if (uiState.secondsLeft > 0)
                        "Expira en ${uiState.secondsLeft} segundos"
                    else
                        "Regenerando...",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (uiState.secondsLeft > 10)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // BOTÓN CENTRADO
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PrimaryButton(
                        text = "Copiar token",
                        icon = Icons.Default.ContentCopy,
                        modifier = Modifier.widthIn(max = 240.dp),
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

                Spacer(modifier = Modifier.height(46.dp))
            }

            // 5. SEGUNDO MENSAJE
            item {
                Text(
                    "2. Copia y comparte el ID de la red que deseas autorizar.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // ESTADO VACÍO
            if (networks.isEmpty()) {
                item {
                    Text(
                        "No tienes redes registradas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // LISTA DE REDES
            items(networks) { network ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = colors.cardBorder
                    ),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = network.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                scope.launch {
                                    clipboard.setClipEntry(
                                        ClipData
                                            .newPlainText("meshId", network.thingName)
                                            .toClipEntry()
                                    )
                                    snackbarHostState.showSnackbar("ID copiado")
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Copiar ID",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
