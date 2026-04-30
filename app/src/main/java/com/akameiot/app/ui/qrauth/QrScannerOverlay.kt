package com.akameiot.app.ui.qrauth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

@Composable
fun QrScannerOverlay() {

    val transition = rememberInfiniteTransition(label = "scanner")

    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lineProgress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineY = size.height * progress
        drawLine(
            color = Color.White.copy(alpha = 0.85f),
            start = Offset(0f, lineY),
            end = Offset(size.width, lineY),
            strokeWidth = 2.dp.toPx()
        )
    }
}