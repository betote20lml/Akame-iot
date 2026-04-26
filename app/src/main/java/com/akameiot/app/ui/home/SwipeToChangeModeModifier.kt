package com.akameiot.app.ui.home

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.swipeToChangeMode(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    swipeRightEnabled: Boolean = true,   // ← nuevo
    threshold: Float = 60f,
): Modifier = this.pointerInput(swipeRightEnabled) {
    var totalDrag = 0f
    detectHorizontalDragGestures(
        onDragStart      = { totalDrag = 0f },
        onHorizontalDrag = { change, dragAmount ->
            change.consume()
            totalDrag += dragAmount
        },
        onDragEnd = {
            when {
                totalDrag < -threshold -> onSwipeLeft()
                totalDrag >  threshold && swipeRightEnabled -> onSwipeRight()
            }
            totalDrag = 0f
        },
    )
}