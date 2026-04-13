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
    globalNow: Long,
    points: List<Pair<Long, Double>>
) {

    val locale = LocalConfiguration.current.locales[0]
    val colors = LocalAppColors.current
    val isLoading = globalNow == 0L

    val maxX = if (globalNow > 0) {
        globalNow
    } else {
        System.currentTimeMillis() / 1000L
    }
    val minX = maxX - chart.chartRange.seconds


    val modelProducer = remember {
        CartesianChartModelProducer()
    }

    val chartData = remember(points, minX, maxX) {

        val size = points.size + 2

        val x = ArrayList<Float>(size)
        val y = ArrayList<Double>(size)

        val base = minX.toFloat()



        fun normalize(ts: Long): Float {
            val delta = ts - base
            if (delta < 0) return 0f
            if (delta > Int.MAX_VALUE) return Int.MAX_VALUE.toFloat()
            return delta
        }


        // inicio
        x.add(normalize(minX))
        y.add(points.firstOrNull()?.second ?: 0.0)

        val sortedPoints = points.sortedBy { it.first }

        for (p in sortedPoints) {
            x.add(normalize(p.first))
            y.add(p.second)
        }

        // final
        x.add(normalize(maxX))
        y.add(points.lastOrNull()?.second ?: 0.0)

        x to y
    }

    LaunchedEffect(chart.metricName, chartData){
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = chartData.first,
                    y = chartData.second
                )
            }
        }
    }


    val datePattern = when (chart.chartRange) {
        ChartTimeRange.H24 -> "HH:mm"
        ChartTimeRange.D7  -> "EEE dd"
        ChartTimeRange.M1,
        ChartTimeRange.M3  -> "dd/MM"
        ChartTimeRange.Y1  -> "dd/MM"
    }

    val dateFormatter = remember(datePattern) {
        SimpleDateFormat(datePattern, Locale.getDefault())
    }

    val xFormatter = remember(dateFormatter) {
        CartesianValueFormatter { _, x, _ ->
            val realTs = minX + x.toLong()
            dateFormatter.format(Date(realTs * 1000))
        }
    }

    val minMax = remember(points) {
        if (points.isEmpty()) null
        else {
            var min = points[0]
            var max = points[0]

            for (i in 1 until points.size) {
                val p = points[i]
                if (p.second < min.second) min = p
                if (p.second > max.second) max = p
            }
            min to max
        }
    }


    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border    = BorderStroke(1.dp, colors.cardBorder),
        colors    = CardDefaults.cardColors(containerColor = colors.cardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text  = "${chart.networkName} · ${chart.nodeId}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text  = TelemetryFormatter.formatName(chart.metricName, locale),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                minMax?.let { (min, max) ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Min: ${"%.2f".format(min.second)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Max: ${"%.2f".format(max.second)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))



            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Points: ${points.size}")
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
                            startAxis = VerticalAxis.rememberStart(),
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





