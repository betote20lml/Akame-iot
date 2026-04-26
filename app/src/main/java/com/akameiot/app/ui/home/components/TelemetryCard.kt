package com.akameiot.app.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.akameiot.domain.formatter.MetricFormatter
import com.akameiot.app.ui.home.model.NodeTelemetryUiModel
import java.text.SimpleDateFormat
import java.util.*
import com.akameiot.coreui.theme.LocalAppColors
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.akameiot.app.ui.home.model.MetricTrend


private data class FormattedMetric(
    val name: String,
    val value: String,
    val trend: MetricTrend
)

@Composable
fun TelemetryCard(node: NodeTelemetryUiModel) {

    val latestTimestamp = node.metrics.maxOfOrNull { it.timestamp } ?: 0L
    val locale = LocalConfiguration.current.locales[0]
    val formatter = rememberDateFormatter(locale)
    val colors = LocalAppColors.current

    val isAnyStale = node.isStale || node.isStaleByTime
    val cardBg = if (isAnyStale) colors.staleBackground else colors.cardBackground
    val cardBorder = if (isAnyStale) colors.staleBorder else colors.cardBorder
    val tsColor = colors.timestamp
    val valueColor = colors.metricValue
    val metricsKey = node.metrics.map { it.name to it.latestValue }

    val formattedMetrics = remember(metricsKey, locale) {
        node.metrics.map { metric ->
            FormattedMetric(
                name = MetricFormatter.formatName(metric.name, locale),
                value = MetricFormatter.formatValue(metric.name, metric.latestValue, locale),
                trend = metric.trend
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
            width = if (isAnyStale) 1.5.dp else 1.dp,
            color = cardBorder
        ),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${node.networkName} · ${node.nodeId}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.timestamp
            )


            Spacer(modifier = Modifier.height(20.dp))

            formattedMetrics.forEach { metric ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = metric.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val isNegative = metric.value.trim().startsWith("-")

                        val metricColor = if (isNegative) {
                            MaterialTheme.colorScheme.error
                        } else {
                            valueColor
                        }

                        when (metric.trend) {
                            MetricTrend.UP -> Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = metricColor,
                                modifier = Modifier.size(16.dp)
                            )
                            MetricTrend.DOWN -> Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = metricColor,
                                modifier = Modifier.size(16.dp)
                            )
                            MetricTrend.FLAT -> Spacer(modifier = Modifier.size(16.dp))
                        }

                        Text(
                            text = metric.value,
                            style = MaterialTheme.typography.titleMedium,
                            color = metricColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
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
