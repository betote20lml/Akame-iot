package com.akameiot.app.ui.home

import androidx.compose.runtime.*
import com.akameiot.coreui.components.AppSheetContainer
import com.akameiot.coreui.components.AppTextField
import com.akameiot.coreui.components.PrimaryButton

@Composable
fun ActivateDeviceSheet(
    isLoading: Boolean,
    onActivate: (String, String) -> Unit,
) {

    var code by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }


    val isFormValid =
        code.trim().isNotBlank() &&
                displayName.trim().isNotBlank() &&
                !isLoading

    AppSheetContainer(title = "Conectar dispositivos") {

        AppTextField(
            value = code,
            onValueChange = { code = it },
            placeholder = "Código de activación"
        )

        AppTextField(
            value = displayName,
            onValueChange = { displayName = it },
            placeholder = "Asignar nombre a la red"
        )

        PrimaryButton(
            text = if (isLoading) "Conectando..." else "Conectar",
            enabled = isFormValid,
            onClick = {
                onActivate(
                    code.trim(),
                    displayName.trim()
                )
            }
        )
    }
}