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
import com.akameiot.domain.formatter.MetricFormatter
import com.akameiot.app.ui.home.model.ChartTimeRange
import com.akameiot.app.ui.home.model.ChartUiModel
import com.akameiot.coreui.theme.LocalAppColors
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToLong



private fun formatYAxisValue(value: Double, max: Double): String {
    val absValue = abs(value)
    val absMax = abs(max)

    val isInteger = absValue == absValue.roundToLong().toDouble()

    val decimals = when {
        isInteger -> 0
        absMax < 10  -> 2
        absMax < 100 -> 1
        else         -> 0
    }

    return "%.${decimals}f".format(value)
}


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

    val windowInfo = LocalWindowInfo.current
    val screenWidthDp = with(LocalDensity.current) {
        windowInfo.containerSize.width.toDp()
    }

    val datePattern = when (chart.chartRange) {
        ChartTimeRange.H24 -> "HH:mm"
        ChartTimeRange.D7  -> if (screenWidthDp < 480.dp) "EEE" else "EEE dd"
        ChartTimeRange.M1,
        ChartTimeRange.M3  -> "dd/MM"
        ChartTimeRange.Y1  -> "dd/MM"
    }

    val dateFormatter = remember(datePattern) {
        SimpleDateFormat(datePattern, Locale.getDefault())
    }

    val xLabelsData = remember(minX, maxX, chart.chartRange, datePattern) {
        val count = if (chart.chartRange == ChartTimeRange.D7) 7 else 5

        (0..count).map { i ->
            val fraction = i.toFloat() / count
            val ts = minX + ((maxX - minX) * fraction).toLong()
            ts to dateFormatter.format(Date(ts * 1000))
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

    val sortedPoints = remember(points) {
        points.sortedBy { it.first }
    }

    // colores del tema
    val isAnyStale = chart.isStale || chart.isStaleByTime
    val lineColor = MaterialTheme.colorScheme.primary
    val cardBg = if (isAnyStale) colors.staleBackground else colors.cardBackground
    val cardBorder = if (isAnyStale) colors.staleBorder else colors.cardBorder




    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val density = LocalDensity.current
    val axisTextSizePx = with(density) { 12.sp.toPx() }
    val axisTextColorInt = android.graphics.Color.argb(
        (axisTextColor.alpha * 255).toInt(),
        (axisTextColor.red * 255).toInt(),
        (axisTextColor.green * 255).toInt(),
        (axisTextColor.blue * 255).toInt()
    )

    val gridPaint = remember(gridColor) {
        android.graphics.Paint().apply {
            color = android.graphics.Color.argb(
                (gridColor.alpha * 255).toInt(),
                (gridColor.red * 255).toInt(),
                (gridColor.green * 255).toInt(),
                (gridColor.blue * 255).toInt()
            )
            style = android.graphics.Paint.Style.STROKE
            isAntiAlias = true
        }
    }

    val yTextPaint = remember(axisTextColorInt, axisTextSizePx) {
        android.graphics.Paint().apply {
            color = axisTextColorInt
            textSize = axisTextSizePx
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
        }
    }

    val xTextPaint = remember(axisTextColorInt, axisTextSizePx) {
        android.graphics.Paint().apply {
            color = axisTextColorInt
            textSize = axisTextSizePx
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
    }


    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border    = BorderStroke(
            width = if (isAnyStale) 1.5.dp else 1.dp,
            color = cardBorder
        ),
        colors    = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, end = 16.dp, start = 0.dp)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text  = "${chart.networkName} · ${chart.nodeId}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text  = MetricFormatter.formatName(chart.metricName, locale),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.timestamp
                    )
                }

                minMax?.let { (min, max) ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text  = "Min: ${"%.2f".format(min.second)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text  = "Max: ${"%.2f".format(max.second)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            style = MaterialTheme.typography.bodyMedium,
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
                        val xLabelHeight = 22.dp.toPx()
                        val plotTop    = 8.dp.toPx()
                        val plotBottom = size.height - xLabelHeight

                        val minY = minMax!!.first.second
                        val maxY = minMax.second.second
                        val yRange = if (maxY - minY < 0.001) 1.0 else maxY - minY
                        val yPadding = yRange * 0.1

                        val yMin = minY - yPadding
                        val yMax = maxY + yPadding
                        val ySpan = yMax - yMin

                        val tempPaint = android.graphics.Paint().apply {
                            textSize = axisTextSizePx
                            isAntiAlias = true
                        }
                        val gridLines = 4
                        val widestLabel = (0..gridLines).maxOf { i ->
                            val fraction = i.toFloat() / gridLines
                            val value = yMin + fraction * ySpan
                            val formatted = formatYAxisValue(value, yMax)
                            tempPaint.measureText(formatted)
                        }
                        val yLabelWidth = widestLabel + 22.dp.toPx()
                        val plotRight  = size.width
                        val plotWidth  = plotRight - yLabelWidth
                        val plotHeight = plotBottom - plotTop

                        val xPadding = 12.dp.toPx()
                        val xRange = (maxX - minX).toFloat().coerceAtLeast(1f)
                        val xScale = (plotWidth - xPadding * 2) / xRange

                        fun toX(ts: Long): Float =
                            yLabelWidth + xPadding + (ts - minX) * xScale

                        fun toY(v: Double): Float =
                            plotBottom - ((v - yMin) / ySpan * plotHeight).toFloat()


                        for (i in 0..gridLines) {
                            val fraction = i.toFloat() / gridLines
                            val y = plotBottom - fraction * plotHeight
                            val value = yMin + fraction * ySpan

                            // línea grid
                            drawContext.canvas.nativeCanvas.drawLine(
                                yLabelWidth, y, plotRight, y, gridPaint
                            )

                            // label eje Y
                            val formatted = formatYAxisValue(value, yMax)

                            drawContext.canvas.nativeCanvas.drawText(
                                formatted,
                                yLabelWidth - 4.dp.toPx(),
                                y + axisTextSizePx / 3,
                                yTextPaint
                            )
                        }

                        // labels eje X — 5 puntos
                        xLabelsData.forEach { (ts, label) ->
                            val x = toX(ts)

                            drawContext.canvas.nativeCanvas.drawText(
                                label,
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