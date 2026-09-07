package au.prayer.app.ui.gestures

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Detects a deliberate swipe right gesture starting specifically from the left edge of the screen.
 * - Starts within edgeThreshold (default 25dp)
 * - Displaces rightward by at least distanceThreshold (default 50dp)
 * - Satisfies horizontal discrimination (|ΔX| >= 1.5 * |ΔY|)
 */
fun Modifier.edgeSwipeRight(
    edgeThreshold: Dp = 25.dp,
    distanceThreshold: Dp = 50.dp,
    onSwipeRight: () -> Unit
): Modifier = this.pointerInput(onSwipeRight) {
    val edgeThresholdPx = edgeThreshold.toPx()
    val distanceThresholdPx = distanceThreshold.toPx()

    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        if (down.position.x > edgeThresholdPx) {
            // Touch did not originate within the left edge zone; ignore completely
            return@awaitEachGesture
        }

        var totalDx = 0f
        var totalDy = 0f

        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break

            if (!change.pressed) {
                // Pointer up (release)
                val absDx = abs(totalDx)
                val absDy = abs(totalDy)
                if (totalDx >= distanceThresholdPx && absDx >= 1.5f * absDy) {
                    onSwipeRight()
                }
                break
            }

            val dragAmount = change.positionChange()
            totalDx += dragAmount.x
            totalDy += dragAmount.y

            // Consume drag events once clear rightward edge intent is established
            if (totalDx > 15f && abs(totalDx) >= 1.5f * abs(totalDy)) {
                change.consume()
            }
        }
    }
}

/**
 * Universal gesture handler for passive contemplation and full-screen views.
 * Supports horizontal left/right gestures, vertical down dismissal, and optional left-edge swipe right.
 */
fun Modifier.prayerSwipeGestures(
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onSwipeDown: () -> Unit = {},
    onEdgeSwipeRight: (() -> Unit)? = null
): Modifier = this.pointerInput(onSwipeLeft, onSwipeRight, onSwipeDown, onEdgeSwipeRight) {
    val minThresholdPx = 40.dp.toPx()
    val minVerticalExitPx = 60.dp.toPx()
    val edgeThresholdPx = 25.dp.toPx()
    val edgeDistancePx = 50.dp.toPx()

    var startX = 0f
    var startY = 0f
    var totalDx = 0f
    var totalDy = 0f

    detectDragGestures(
        onDragStart = { offset ->
            startX = offset.x
            startY = offset.y
            totalDx = 0f
            totalDy = 0f
        },
        onDrag = { change, dragAmount ->
            change.consume()
            totalDx += dragAmount.x
            totalDy += dragAmount.y
        },
        onDragEnd = {
            val absDx = abs(totalDx)
            val absDy = abs(totalDy)

            // 1. Edge-swipe right back navigation (takes priority if started within left edge zone)
            if (onEdgeSwipeRight != null && startX <= edgeThresholdPx && totalDx >= edgeDistancePx && absDx >= 1.5f * absDy) {
                onEdgeSwipeRight()
            }
            // 2. Horizontal Intent Discrimination: |ΔX| >= 1.5 * |ΔY| and |ΔX| >= 40dp
            else if (absDx >= 1.5f * absDy && absDx >= minThresholdPx) {
                if (totalDx < 0) {
                    onSwipeLeft()
                } else {
                    onSwipeRight()
                }
            }
            // 3. Vertical Swipe Down Exit (initiated anywhere in upper screen): ΔY >= 60dp
            else if (totalDy >= minVerticalExitPx && totalDy >= 1.5f * absDx) {
                onSwipeDown()
            }
        }
    )
}
