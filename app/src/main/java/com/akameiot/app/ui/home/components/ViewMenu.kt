package com.akameiot.app.ui.home.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akameiot.app.ui.home.HomeViewMode
import com.akameiot.coreui.components.*

@Composable
fun ViewMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    currentMode: HomeViewMode,
    onChangeMode: (HomeViewMode) -> Unit,
    onAddNetwork: () -> Unit,
) {
    AppDropdownMenu(
        expanded = expanded,
        onDismiss = onDismiss,
        width = 180.dp
    ) {
        // ── Vista tarjetas ──
        AppMenuCheckItem(
            label   = "Tarjetas",
            checked = currentMode == HomeViewMode.CARDS,
            onClick = {
                onChangeMode(HomeViewMode.CARDS)
                onDismiss()
            },
        )

        AppMenuDivider()

        // ── Gráficas ──
        listOf(
            HomeViewMode.CHARTS_24H to "Gráficas · 24h",
            HomeViewMode.CHARTS_7D  to "Gráficas · 7 días",
            HomeViewMode.CHARTS_1M  to "Gráficas · 1 mes",
            HomeViewMode.CHARTS_3M  to "Gráficas · 3 meses",
            HomeViewMode.CHARTS_1Y  to "Gráficas · 1 año",
        ).forEach { (mode, label) ->
            AppMenuCheckItem(
                label   = label,
                checked = currentMode == mode,
                onClick = {
                    onChangeMode(mode)
                    onDismiss()
                },
            )
        }

        AppMenuDivider()

        // ── Acción ──
        DropdownMenuItem(
            text = {
                Text(
                    text  = "Agregar red",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    ),
                )
            },
            onClick = {
                onDismiss()
                onAddNetwork()
            },
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
            modifier = Modifier.height(44.dp),
        )
    }
}