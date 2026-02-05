package com.akameiot.app.ui.verification

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavController
import com.akameiot.coreui.components.*
import com.akameiot.coreui.theme.LocalSpacing

@Composable
fun VerificationScreen(
    navController: NavController,
    onVerify: (String) -> Unit = {},
    onResend: () -> Unit = {}
) {

    val spacing = LocalSpacing.current
    var code by remember { mutableStateOf("") }

    AuthScaffold {

        AuthHeader(
            text = "Revisa tu correo"
        )

        Spacer(modifier = Modifier.height(spacing.sm))

        Text(
            text = "Enviamos un código de 6 dígitos a tu correo. " +
                    "Si no lo encuentras, revisa tu carpeta de spam.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(spacing.lg))

        AppTextField(
            value = code,
            onValueChange = { newValue ->

                if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                    code = newValue

                    if (newValue.length == 6) {
                        onVerify(newValue)
                    }
                }
            },
            placeholder = "Código de 6 dígitos",
            keyboardType = KeyboardType.Number
        )


        Spacer(modifier = Modifier.height(spacing.md))

        PrimaryButton(
            text = "Continuar",
            onClick = { onVerify(code) },
            enabled = code.length == 6,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(spacing.lg))

        Text(
            text = "¿No recibiste el código?",
            style = MaterialTheme.typography.bodyMedium
        )

        TextButton(
            onClick = onResend,
        ) {
            Text("Enviar de nuevo")
        }
    }
}
