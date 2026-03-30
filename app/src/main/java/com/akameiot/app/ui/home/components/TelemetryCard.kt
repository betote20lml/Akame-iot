package com.akameiot.app.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.akameiot.app.ui.home.model.NodeTelemetryUiModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalConfiguration
import com.akameiot.app.ui.home.formatter.TelemetryFormatter
import java.util.Locale

@Composable
fun TelemetryCard(
    node: NodeTelemetryUiModel,
) {

    val latestTimestamp = node.metrics.maxOfOrNull { it.timestamp } ?: 0L
    val locale = LocalConfiguration.current.locales[0]

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = MaterialTheme.shapes.large,

        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "Sensor ${node.nodeId}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFeatureSettings = "liga=0"
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatTimestamp(latestTimestamp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Metrics
            node.metrics.forEach { metric ->

                val formattedName = TelemetryFormatter.formatName(metric.name, locale)
                val formattedValue = TelemetryFormatter.formatValue(metric.name, metric.latestValue, locale)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = formattedName,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFeatureSettings = "liga=0"
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = formattedValue,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFeatureSettings = "lnum"
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp)) // ↑ un poco más de aire
            }
        }
    }
}

private fun formatTimestamp(ts: Long): String {
    if (ts == 0L) return ""

    val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault())
    return sdf.format(Date(ts * 1000))
}