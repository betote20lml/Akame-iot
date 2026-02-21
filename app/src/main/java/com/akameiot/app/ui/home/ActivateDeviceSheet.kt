package com.akameiot.app.ui.home


import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.akameiot.coreui.components.AppSheetContainer
import com.akameiot.coreui.components.AppTextField
import com.akameiot.coreui.components.PrimaryButton

@Composable
fun ActivateDeviceSheet(
    onActivate: (String, String) -> Unit,
) {

    var code by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }

    AppSheetContainer {

        Text(
            text = "Conectar dispositivos",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant

        )


        AppTextField(
            value = code,
            onValueChange = { code = it },
            placeholder = "Código de activación"
        )

        AppTextField(
            value = displayName,
            onValueChange = { displayName = it },
            placeholder = "Nombre de la red"
        )

        PrimaryButton(
            text = "Conectar",
            enabled = code.isNotBlank(),
            onClick = {
                onActivate(code.trim(), displayName.trim())
            }
        )

    }
}