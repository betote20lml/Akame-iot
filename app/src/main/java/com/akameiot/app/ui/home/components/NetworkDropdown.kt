package com.akameiot.app.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.akameiot.app.ui.home.model.TelemetryUiModel
import com.akameiot.domain.model.Network

@Composable
fun NetworkDropdown(
    networks: List<Network>,
    selectedNetwork: TelemetryUiModel?,
    selectedNetworkInfo: Network?,
    onNetworkSelected: (Network) -> Unit,
) {
    if (networks.isEmpty()) {
        Text(
            text = "Telemetría",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = androidx.compose.ui.Modifier
        ) {
            TextButton(
                onClick = { expanded = true },
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Text(
                    text = selectedNetworkInfo?.displayName ?: "Seleccionar red",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Seleccionar red",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            networks.forEach { network ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = network.displayName,
                            style = if (network.thingName == selectedNetwork?.meshId)
                                MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.primary
                                )
                            else MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onNetworkSelected(network)
                        expanded = false
                    }
                )
            }
        }
    }
}