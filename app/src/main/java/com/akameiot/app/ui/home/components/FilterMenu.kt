package com.akameiot.app.ui.home.components

import com.akameiot.coreui.components.AppSelectableSortableItem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.akameiot.domain.model.Network
import com.akameiot.coreui.components.AppDropdownMenu
import com.akameiot.coreui.components.AppMenuSectionHeader
import com.akameiot.coreui.components.AppMenuDivider
import androidx.compose.ui.platform.LocalConfiguration
import com.akameiot.app.ui.home.formatter.TelemetryFormatter
import com.akameiot.coreui.components.AppMenuCheckItem

@Composable
fun FilterMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    networks: List<Network>,
    filterNetworks: List<Network>,
    networksOrder: List<String>,
    filterMetrics: List<String>,
    metricsOrder: List<String>,
    availableMetrics: List<String>,
    onToggleNetwork: (Network, Boolean) -> Unit,
    onMoveNetworkUp: (String) -> Unit,
    onToggleMetric: (String, Boolean) -> Unit,
    onMoveMetricUp: (String) -> Unit,
    sortAscending: Boolean?,
    onSortAscending: (Boolean?) -> Unit,
) {
    var showNetworkOptions by remember { mutableStateOf(false) }
    var showMetricOptions by remember { mutableStateOf(false) }
    val locale = LocalConfiguration.current.locales[0]

    AppDropdownMenu(
        expanded = expanded,
        onDismiss = {
            showNetworkOptions = false
            showMetricOptions = false
            onDismiss()
        }
    ) {
        // ── Filtro de red ──
        AppMenuSectionHeader(
            title = "Filtro de red",
            expanded = showNetworkOptions,
            onClick = {
                showNetworkOptions = !showNetworkOptions
                showMetricOptions = false
            }
        )

        if (showNetworkOptions) {
            // Redes seleccionadas en su orden preferido con opción de subir
            networksOrder.forEach { thingName ->
                val network = networks.find { it.thingName == thingName } ?: return@forEach
                val isFirst = networksOrder.first() == thingName

                AppSelectableSortableItem(
                    label = network.displayName,
                    selected = true,
                    isFirst = isFirst,
                    onClick = { onToggleNetwork(network, false) },
                    onMoveUp = { onMoveNetworkUp(thingName) }
                )
            }

            // Redes no seleccionadas
            networks
                .filter { n -> filterNetworks.none { it.thingName == n.thingName } }
                .forEach { network ->

                    AppSelectableSortableItem(
                        label = network.displayName,
                        selected = false,
                        isFirst = false,
                        onClick = { onToggleNetwork(network, true) }
                    )
                }

            HorizontalDivider()
        }

        // ── Filtro de métrica ──
        AppMenuSectionHeader(
            title = "Filtro de métrica",
            expanded = showMetricOptions,
            onClick = {
                showMetricOptions = !showMetricOptions
                showNetworkOptions = false
            }
        )

        if (showMetricOptions) {
            metricsOrder.forEach { metric ->
                val isFirst = metricsOrder.first() == metric
                val label = TelemetryFormatter.formatName(metric, locale)

                AppSelectableSortableItem(
                    label = label,
                    selected = true,
                    isFirst = isFirst,
                    onClick = { onToggleMetric(metric, false) },
                    onMoveUp = { onMoveMetricUp(metric) }
                )
            }

            availableMetrics
                .filter { !filterMetrics.contains(it) }
                .forEach { metric ->
                    val label = TelemetryFormatter.formatName(metric, locale)

                    AppSelectableSortableItem(
                        label = label,
                        selected = false,
                        isFirst = false,
                        onClick = { onToggleMetric(metric, true) }
                    )
                }

            AppMenuDivider()
        }

        HorizontalDivider()

        // ── Orden ──
        AppMenuCheckItem(
            label = "Orden ascendente",
            checked = sortAscending == true,
            onClick = {
                onSortAscending(if (sortAscending == true) null else true)
                onDismiss()
            }
        )

        AppMenuCheckItem(
            label = "Orden descendente",
            checked = sortAscending == false,
            onClick = {
                onSortAscending(if (sortAscending == false) null else false)
                onDismiss()
            }
        )
    }
}