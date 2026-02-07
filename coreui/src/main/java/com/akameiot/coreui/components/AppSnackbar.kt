package com.akameiot.coreui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {

    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { data ->

        Snackbar(
            snackbarData = data,

            //  Estilo global
            containerColor = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            actionColor = MaterialTheme.colorScheme.primary
        )
    }
}

