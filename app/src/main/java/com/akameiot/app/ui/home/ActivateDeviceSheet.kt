package com.akameiot.app.ui.home

import androidx.compose.runtime.*
import com.akameiot.coreui.components.AppSheetContainer
import com.akameiot.coreui.components.AppTextField
import com.akameiot.coreui.components.PrimaryButton
import com.akameiot.domain.model.AppUser
import com.akameiot.domain.validation.DeviceInput
import com.akameiot.domain.validation.DeviceInputParser

@Composable
fun ActivateDeviceSheet(
    isLoading: Boolean,
    appUser: AppUser?,
    onActivate: (String, String) -> Unit,
) {
    val isLimited = appUser is AppUser.Limited
    var code by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }


    val parsedInput = remember(code) {
        DeviceInputParser.parse(code)
    }

    val isFormValid = remember(parsedInput, displayName, isLoading) {
        parsedInput !is DeviceInput.Invalid &&
                displayName.trim().isNotBlank() &&
                !isLoading
    }

    AppSheetContainer(title = "Conectar dispositivos") {

        AppTextField(
            value = code,
            onValueChange = { code = it },
            placeholder =
                if (isLimited)
                    "ID de red (gw_...)"
                else
                    "Código de activación"
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