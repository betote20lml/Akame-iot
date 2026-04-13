package com.akameiot.app.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akameiot.app.ui.home.formatter.TelemetryFormatter
import com.akameiot.app.ui.home.model.ChartTimeRange
import com.akameiot.app.ui.home.model.ChartUiModel
import com.akameiot.coreui.theme.LocalAppColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChartCard(
    chart: ChartUiModel,
    globalNow: Long,
    points: List<Pair<Long, Double>>
) {
    val locale = LocalConfiguration.current.locales[0]
    val colors = LocalAppColors.current
    val isLoading = globalNow == 0L

    val maxX = if (globalNow > 0) globalNow else System.currentTimeMillis() / 1000L
    val minX = maxX - chart.chartRange.seconds

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

    // colores del tema
    val lineColor = Color(0xFF2196F3)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val density = LocalDensity.current
    val axisTextSizePx = with(density) { 10.sp.toPx() }
    val axisTextColorInt = android.graphics.Color.argb(
        (axisTextColor.alpha * 255).toInt(),
        (axisTextColor.red * 255).toInt(),
        (axisTextColor.green * 255).toInt(),
        (axisTextColor.blue * 255).toInt()
    )

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
                            text  = "Min: ${"%.2f".format(min.second)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text  = "Max: ${"%.2f".format(max.second)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        val yLabelWidth = 36.dp.toPx()
                        val xLabelHeight = 20.dp.toPx()
                        val plotLeft   = yLabelWidth
                        val plotRight  = size.width
                        val plotTop    = 4.dp.toPx()
                        val plotBottom = size.height - xLabelHeight
                        val plotWidth  = plotRight - plotLeft
                        val plotHeight = plotBottom - plotTop

                        val sortedPoints = points.sortedBy { it.first }

                        val minY = sortedPoints.minOf { it.second }
                        val maxY = sortedPoints.maxOf { it.second }
                        val yRange = if (maxY - minY < 0.001) 1.0 else maxY - minY
                        val yPadding = yRange * 0.1

                        val yMin = minY - yPadding
                        val yMax = maxY + yPadding
                        val ySpan = yMax - yMin

                        fun toX(ts: Long): Float =
                            plotLeft + ((ts - minX).toFloat() / (maxX - minX).toFloat()) * plotWidth

                        fun toY(v: Double): Float =
                            plotBottom - ((v - yMin) / ySpan * plotHeight).toFloat()

                        // grid horizontal — 4 líneas
                        val gridPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.argb(
                                (gridColor.alpha * 255).toInt(),
                                (gridColor.red * 255).toInt(),
                                (gridColor.green * 255).toInt(),
                                (gridColor.blue * 255).toInt()
                            )
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = 1.dp.toPx()
                            pathEffect = android.graphics.DashPathEffect(
                                floatArrayOf(4.dp.toPx(), 4.dp.toPx()), 0f
                            )
                        }

                        val yTextPaint = android.graphics.Paint().apply {
                            color = axisTextColorInt
                            textSize = axisTextSizePx
                            textAlign = android.graphics.Paint.Align.RIGHT
                            isAntiAlias = true
                        }

                        val xTextPaint = android.graphics.Paint().apply {
                            color = axisTextColorInt
                            textSize = axisTextSizePx
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }

                        val gridLines = 4
                        for (i in 0..gridLines) {
                            val fraction = i.toFloat() / gridLines
                            val y = plotBottom - fraction * plotHeight
                            val value = yMin + fraction * ySpan

                            // línea grid
                            drawContext.canvas.nativeCanvas.drawLine(
                                plotLeft, y, plotRight, y, gridPaint
                            )

                            // label eje Y
                            drawContext.canvas.nativeCanvas.drawText(
                                "%.1f".format(value),
                                yLabelWidth - 4.dp.toPx(),
                                y + axisTextSizePx / 3,
                                yTextPaint
                            )
                        }

                        // labels eje X — 5 puntos
                        val xLabels = 5
                        for (i in 0..xLabels) {
                            val fraction = i.toFloat() / xLabels
                            val ts = minX + ((maxX - minX) * fraction).toLong()
                            val x = plotLeft + fraction * plotWidth

                            drawContext.canvas.nativeCanvas.drawText(
                                dateFormatter.format(Date(ts * 1000)),
                                x,
                                size.height - 2.dp.toPx(),
                                xTextPaint
                            )
                        }

                        // línea de datos
                        if (sortedPoints.size >= 2) {
                            val path = Path()
                            path.moveTo(toX(sortedPoints[0].first), toY(sortedPoints[0].second))
                            for (i in 1 until sortedPoints.size) {
                                path.lineTo(toX(sortedPoints[i].first), toY(sortedPoints[i].second))
                            }
                            drawPath(
                                path  = path,
                                color = lineColor,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 2.dp.toPx()
                                )
                            )
                        } else if (sortedPoints.size == 1) {
                            // punto único
                            drawCircle(
                                color  = lineColor,
                                radius = 3.dp.toPx(),
                                center = Offset(
                                    toX(sortedPoints[0].first),
                                    toY(sortedPoints[0].second)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}




