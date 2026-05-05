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
import com.akameiot.coreui.theme.LocalSpacing

@Composable
fun AppDrawerHeader(
    connectionStatus: String,
    isOnline: Boolean,
    modifier: Modifier = Modifier,
    appIcon: (@Composable () -> Unit)? = null
) {
    val spacing = LocalSpacing.current
    val statusColor = if (isOnline)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.error

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
                val curveStroke = Stroke(width = 1.8f)  // un poco más grueso
                val nodeStroke  = Stroke(width = 1.2f)

                // Nodos de circuito — 10 puntos distribuidos
                val nodes = listOf(
                    Offset(w * 0.12f, h * 0.15f),
                    Offset(w * 0.30f, h * 0.40f),
                    Offset(w * 0.55f, h * 0.20f),
                    Offset(w * 0.75f, h * 0.50f),
                    Offset(w * 0.90f, h * 0.25f),
                    Offset(w * 0.65f, h * 0.75f),
                    Offset(w * 0.45f, h * 0.85f),
                    Offset(w * 0.20f, h * 0.70f),
                    Offset(w * 0.85f, h * 0.88f),
                    Offset(w * 0.38f, h * 0.55f),
                )

                // Líneas entre nodos consecutivos
                for (i in 0 until nodes.size - 1) {
                    drawLine(
                        color = textureColor.copy(alpha = 0.09f),
                        start = nodes[i],
                        end = nodes[i + 1],
                        strokeWidth = 1f
                    )
                }
                // Algunas conexiones extra no consecutivas para dar densidad
                val extraLinks = listOf(0 to 4, 2 to 9, 5 to 8, 1 to 7, 3 to 6)
                extraLinks.forEach { (a, b) ->
                    drawLine(
                        color = textureColor.copy(alpha = 0.07f),
                        start = nodes[a],
                        end = nodes[b],
                        strokeWidth = 1f
                    )
                }

                // Círculos en cada nodo
                nodes.forEach { offset ->
                    drawCircle(
                        color = textureColor.copy(alpha = 0.14f),
                        radius = 4.dp.toPx(),
                        center = offset,
                        style = nodeStroke
                    )
                    drawCircle(
                        color = textureColor.copy(alpha = 0.06f),
                        radius = 10.dp.toPx(),
                        center = offset,
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
                        color = statusColor
                    )
                }
            }
        }
    }
}