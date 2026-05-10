package com.akameiot.coreui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.akameiot.coreui.theme.LocalAppColors
import com.akameiot.coreui.theme.LocalSpacing

enum class ConnectionLevel { OK, PARTIAL, OFFLINE }

@Composable
fun AppDrawerHeader(
    connectionStatus: String,
    modifier: Modifier = Modifier,
    connectionLevel: ConnectionLevel = ConnectionLevel.OK,
    appIcon: (@Composable () -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val statusColor = when (connectionLevel) {
        ConnectionLevel.OK      -> MaterialTheme.colorScheme.primary
        ConnectionLevel.PARTIAL -> colors.staleBorder
        ConnectionLevel.OFFLINE -> MaterialTheme.colorScheme.error
    }
    val spacing = LocalSpacing.current
    val textureColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            // ── Textura vectorial semitransparente ──────────────────────────
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                val w = size.width
                val h = size.height
                val curveStroke = Stroke(width = 1.8f)
                val nodeStroke  = Stroke(width = 1.2f)

                // Nodos de circuito — 12 puntos con tamaños variables
                data class Node(val offset: Offset, val radiusOuter: Float, val radiusInner: Float)

                val maxOuter = 10.dp.toPx()
                val maxInner = 4.dp.toPx()

                val nodes = listOf(
                    Node(Offset(w * 0.12f, h * 0.15f), maxOuter * 0.60f, maxInner * 0.55f),
                    Node(Offset(w * 0.30f, h * 0.40f), maxOuter * 1.00f, maxInner * 1.00f),
                    Node(Offset(w * 0.55f, h * 0.20f), maxOuter * 0.75f, maxInner * 0.75f),
                    Node(Offset(w * 0.75f, h * 0.50f), maxOuter * 0.85f, maxInner * 0.85f),
                    Node(Offset(w * 0.90f, h * 0.25f), maxOuter * 0.50f, maxInner * 0.50f),
                    Node(Offset(w * 0.65f, h * 0.75f), maxOuter * 0.90f, maxInner * 0.90f),
                    Node(Offset(w * 0.45f, h * 0.85f), maxOuter * 0.65f, maxInner * 0.65f),
                    Node(Offset(w * 0.20f, h * 0.70f), maxOuter * 0.80f, maxInner * 0.80f),
                    Node(Offset(w * 0.85f, h * 0.88f), maxOuter * 0.55f, maxInner * 0.55f),
                    Node(Offset(w * 0.38f, h * 0.55f), maxOuter * 0.70f, maxInner * 0.70f),
                    Node(Offset(w * 0.05f, h * 0.48f), maxOuter * 0.45f, maxInner * 0.45f),
                    Node(Offset(w * 0.72f, h * 0.08f), maxOuter * 0.60f, maxInner * 0.60f),
                )

                // Helper: extiende una línea más allá de [end] respecto a [start] por un factor
                fun extendedEnd(start: Offset, end: Offset, factor: Float): Offset {
                    val dx = end.x - start.x
                    val dy = end.y - start.y
                    return Offset(start.x + dx * factor, start.y + dy * factor)
                }

                // Líneas entre nodos consecutivos, extendidas 40 % más allá del nodo en cada extremo
                for (i in 0 until nodes.size - 1) {
                    val s = nodes[i].offset
                    val e = nodes[i + 1].offset
                    drawLine(
                        color = textureColor.copy(alpha = 0.09f),
                        start = extendedEnd(e, s, 1.40f), // extensión hacia atrás
                        end   = extendedEnd(s, e, 1.40f), // extensión hacia adelante
                        strokeWidth = 1f
                    )
                }
                // Conexiones extra no consecutivas — también extendidas
                val extraLinks = listOf(0 to 4, 2 to 9, 5 to 8, 1 to 7, 3 to 6, 10 to 3, 11 to 6)
                extraLinks.forEach { (a, b) ->
                    val s = nodes[a].offset
                    val e = nodes[b].offset
                    drawLine(
                        color = textureColor.copy(alpha = 0.07f),
                        start = extendedEnd(e, s, 1.35f),
                        end   = extendedEnd(s, e, 1.35f),
                        strokeWidth = 1f
                    )
                }

                // Círculos en cada nodo con radio variable
                nodes.forEach { node ->
                    drawCircle(
                        color = textureColor.copy(alpha = 0.14f),
                        radius = node.radiusInner,
                        center = node.offset,
                        style = nodeStroke
                    )
                    drawCircle(
                        color = textureColor.copy(alpha = 0.06f),
                        radius = node.radiusOuter,
                        center = node.offset,
                    )
                }

                // Curva orgánica 1 — hoja derecha
                val leafPath1 = Path().apply {
                    moveTo(w * 0.7f, h * 0.05f)
                    cubicTo(
                        w * 1.1f, h * 0.2f,
                        w * 0.9f, h * 0.6f,
                        w * 0.65f, h * 0.85f
                    )
                    cubicTo(
                        w * 0.4f,  h * 0.6f,
                        w * 0.55f, h * 0.2f,
                        w * 0.7f,  h * 0.05f
                    )
                }
                drawPath(leafPath1, textureColor.copy(alpha = 0.07f), style = curveStroke)

                // Curva orgánica 2 — hoja izquierda
                val leafPath2 = Path().apply {
                    moveTo(w * 0.05f, h * 0.1f)
                    cubicTo(
                        w * 0.35f,  h * 0.0f,
                        w * 0.45f,  h * 0.5f,
                        w * 0.1f,   h * 0.9f
                    )
                    cubicTo(
                        w * -0.1f,  h * 0.5f,
                        w * -0.05f, h * 0.2f,
                        w * 0.05f,  h * 0.1f
                    )
                }
                drawPath(leafPath2, textureColor.copy(alpha = 0.06f), style = curveStroke)

                // Curva orgánica 3 — arco central sutil
                val leafPath3 = Path().apply {
                    moveTo(w * 0.0f, h * 0.55f)
                    cubicTo(
                        w * 0.3f, h * 0.2f,
                        w * 0.7f, h * 0.8f,
                        w * 1.0f, h * 0.45f
                    )
                }
                drawPath(leafPath3, textureColor.copy(alpha = 0.05f), style = curveStroke)
            }
            // ────────────────────────────────────────────────────────────────

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(vertical = spacing.xl),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                appIcon?.invoke()

                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = connectionStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}