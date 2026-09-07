package au.prayer.app.ui.gestures

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

fun Modifier.prayerSwipeGestures(
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onSwipeDown: () -> Unit = {}
): Modifier = this.pointerInput(Unit) {
    val minThresholdPx = 40.dp.toPx()
    val minVerticalExitPx = 60.dp.toPx()

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

            // 1. Horizontal Intent Discrimination: |ΔX| >= 1.5 * |ΔY| and |ΔX| >= 40dp
            if (absDx >= 1.5f * absDy && absDx >= minThresholdPx) {
                if (totalDx < 0) {
                    onSwipeLeft()
                } else {
                    onSwipeRight()
                }
            }
            // 2. Vertical Swipe Down Exit (initiated anywhere in upper screen): ΔY >= 60dp
            else if (totalDy >= minVerticalExitPx && totalDy >= 1.5f * absDx) {
                onSwipeDown()
            }
        }
    )
}
