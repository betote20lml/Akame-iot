package com.akameiot.app.ui.qrauth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp

@Composable
fun QrScannerOverlay() {

    val transition = rememberInfiniteTransition(label = "scanner")

    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2400,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "lineProgress"
    )

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {

        val frameWidth = size.width
        val frameHeight = size.height

        val left = 0f
        val top = 0f

        // Oscurecer fondo
        drawRect(
            color = Color.Black.copy(alpha = 0.55f)
        )

        // Ventana transparente
        drawRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(frameWidth, frameHeight),
            blendMode = BlendMode.Clear
        )

        // Posición animada de la línea
        val lineY = frameHeight * progress

        drawLine(
            color = Color.White.copy(alpha = 0.8f),
            start = Offset(left, lineY),
            end = Offset(frameWidth, lineY),
            strokeWidth = 2.dp.toPx()
        )
    }
}