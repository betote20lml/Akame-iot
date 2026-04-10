package com.akameiot.app.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.akameiot.app.ui.home.formatter.TelemetryFormatter
import com.akameiot.app.ui.home.model.ChartTimeRange
import com.akameiot.app.ui.home.model.ChartUiModel
import com.akameiot.coreui.theme.LocalAppColors
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState



@Composable
fun ChartCard(
    chart: ChartUiModel,
    onLoadPoints: suspend (meshId: String, nodeId: Int, metric: String, fromTs: Long) -> List<Pair<Long, Double>>
) {
    val locale = LocalConfiguration.current.locales[0]
    val colors = LocalAppColors.current
    var points    by remember { mutableStateOf<List<Pair<Long, Double>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Recarga cuando cambia el rango global (viene en chart.chartRange)
    LaunchedEffect(chart.chartRange) {
        isLoading = true
        val fromTs = System.currentTimeMillis() / 1000L - chart.chartRange.seconds
        points    = onLoadPoints(chart.meshId, chart.nodeId, chart.metricName, fromTs)
        isLoading = false
    }

    val modelProducer = remember(chart.meshId, chart.nodeId) {
        CartesianChartModelProducer()
    }

    LaunchedEffect(points) {
        if (points.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries {
                    series(
                        x = points.map { it.first.toFloat() },
                        y = points.map { it.second }
                    )
                }
            }
        }
    }

    val timestamps = points.map { it.first }

    val datePattern = when (chart.chartRange) {
        ChartTimeRange.H24 -> "HH:mm"
        ChartTimeRange.D7  -> "EEE dd"
        ChartTimeRange.M1,
        ChartTimeRange.M3  -> "dd/MM"
    }

    val xFormatter = CartesianValueFormatter { _, x, _ ->
        SimpleDateFormat(datePattern, Locale.getDefault())
            .format(Date(x.toLong() * 1000))
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border    = BorderStroke(1.dp, colors.cardBorder),
        colors    = CardDefaults.cardColors(containerColor = colors.cardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text  = "${chart.networkName} · ${chart.nodeId}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = TelemetryFormatter.formatName(chart.metricName, locale), // ← Cambio 1
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ← Chips eliminados — el rango viene del menú global

            when {
                isLoading -> {
                    Box(
                        modifier         = Modifier.fillMaxWidth().height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
                points.isEmpty() -> {
                    Box(
                        modifier         = Modifier.fillMaxWidth().height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = "Sin datos para este rango",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
                else -> {
                    val scrollState = rememberVicoScrollState(scrollEnabled = false)

                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            rememberLineCartesianLayer(),
                            startAxis  = VerticalAxis.rememberStart(),
                            bottomAxis = HorizontalAxis.rememberBottom(
                                valueFormatter = xFormatter
                            )
                        ),
                        modelProducer = modelProducer,
                        scrollState   = scrollState,
                        modifier      = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            }
        }
    }
}





