package com.akameiot.app.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.akameiot.app.ui.home.formatter.TelemetryFormatter
import com.akameiot.app.ui.home.model.NodeTelemetryUiModel
import java.text.SimpleDateFormat
import java.util.*
import com.akameiot.coreui.theme.LocalAppColors


private data class FormattedMetric(
    val name: String,
    val value: String
)

@Composable
fun TelemetryCard(node: NodeTelemetryUiModel) {

    val latestTimestamp = node.metrics.maxOfOrNull { it.timestamp } ?: 0L
    val locale = LocalConfiguration.current.locales[0]
    val formatter = rememberDateFormatter(locale)
    val colors = LocalAppColors.current
    val cardBg = if (node.isStale) colors.staleBackground else colors.cardBackground
    val cardBorder = if (node.isStale) colors.staleBorder else colors.cardBorder
    val tsColor = colors.timestamp
    val valueColor = colors.metricValue
    val metricsKey = node.metrics.map { it.name to it.latestValue }

    val formattedMetrics = remember(metricsKey, locale) {
        node.metrics.map { metric ->
            FormattedMetric(
                name = TelemetryFormatter.formatName(metric.name, locale),
                value = TelemetryFormatter.formatValue(metric.name, metric.latestValue, locale)
            )
        }
    }

    val formattedDate = remember(latestTimestamp, formatter) {
        if (latestTimestamp == 0L) ""
        else formatter.format(Date(latestTimestamp * 1000))
    }



    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        border = BorderStroke(
            width = if (node.isStale) 1.5.dp else 1.dp,
            color = cardBorder
        ),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${node.networkName} · ${node.nodeId}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodyMedium,
                color = tsColor
            )


            Spacer(modifier = Modifier.height(20.dp))

            formattedMetrics.forEach { metric->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = metric.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = metric.value,
                        style = MaterialTheme.typography.titleMedium,
                        color = valueColor
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

    @Composable
    private fun rememberDateFormatter(locale: Locale): SimpleDateFormat {
        return remember(locale) {
            SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", locale)
        }
    }
