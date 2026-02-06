package com.akameiot.app.ui.qrauth

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.akameiot.coreui.components.AuthScaffold
import com.akameiot.coreui.components.PrimaryButton
import com.akameiot.coreui.theme.LocalSpacing

@Composable
fun QrAuthScreen(
    navController: NavController,
    onPasteToken: () -> Unit = {},
    onQrScanned: (String) -> Unit = {}
) {

    val spacing = LocalSpacing.current

    AuthScaffold {

        Text(
            text = "Acceso rápido",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(spacing.sm))

        Text(
            text = "Escanea el código QR desde el dispositivo principal para copiar la sesión de forma segura.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(spacing.xl))

        // Área de escaneo QR
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
            Text(
                text = "Área de escaneo QR",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(spacing.xl))

        PrimaryButton(
            text = "Pegar token de acceso desde el portapapeles",
            onClick = onPasteToken,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
