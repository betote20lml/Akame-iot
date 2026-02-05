package com.akameiot.app.ui.verification

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.akameiot.coreui.components.*
import com.akameiot.coreui.theme.LocalSpacing
import androidx.compose.runtime.Composable


@Composable
fun VerificationScreen(
    navController: NavController,
    onVerify: (String) -> Unit = {},
    onResend: () -> Unit = {}
) {

    val spacing = LocalSpacing.current

    AuthScaffold {

        AuthHeader(
            text = "Verifica tu correo"
        )

        Spacer(modifier = Modifier.height(spacing.sm))

        Text(
            text = "Ingresa el código de 6 dígitos que te enviamos. " +
                    "Si no lo ves, revisa tu carpeta de spam.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(spacing.xl))

        // OTP REAL
        OtpField(
            onOtpComplete = { code ->
                onVerify(code)
            }
        )

        Spacer(modifier = Modifier.height(spacing.xl))

        PrimaryButton(
            text = "Continuar",
            onClick = {
            }
        )

        Spacer(modifier = Modifier.height(spacing.lg))

        Text(
            text = "¿No recibiste el código?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        TextButton(
            onClick = onResend
        ) {
            Text("Enviar de nuevo")
        }
    }
}

