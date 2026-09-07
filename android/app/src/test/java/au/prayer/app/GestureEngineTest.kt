package au.prayer.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

enum class GestureResult {
    SWIPE_LEFT,
    SWIPE_RIGHT,
    SWIPE_DOWN,
    REJECTED
}

fun evaluateGesture(dx: Float, dy: Float, threshold: Float = 40f, exitThreshold: Float = 60f): GestureResult {
    val absDx = abs(dx)
    val absDy = abs(dy)

    return if (absDx >= 1.5f * absDy && absDx >= threshold) {
        if (dx < 0) GestureResult.SWIPE_LEFT else GestureResult.SWIPE_RIGHT
    } else if (dy >= exitThreshold && dy >= 1.5f * absDx) {
        GestureResult.SWIPE_DOWN
    } else {
        GestureResult.REJECTED
    }
}

class GestureEngineTest {

    @Test
    fun `test horizontal swipe left topic progression`() {
        // Pure horizontal swipe left: dx = -100, dy = 10
        val result = evaluateGesture(dx = -100f, dy = 10f)
        assertEquals(GestureResult.SWIPE_LEFT, result)
    }

    @Test
    fun `test horizontal swipe right topic return`() {
        // Pure horizontal swipe right: dx = 100, dy = 10
        val result = evaluateGesture(dx = 100f, dy = 10f)
        assertEquals(GestureResult.SWIPE_RIGHT, result)
    }

    @Test
    fun `test swipe down dismissal`() {
        // Vertical swipe down: dx = 5, dy = 90
        val result = evaluateGesture(dx = 5f, dy = 90f)
        assertEquals(GestureResult.SWIPE_DOWN, result)
    }

    @Test
    fun `test rejection of diagonal scroll`() {
        // Diagonal scroll: dx = 60, dy = 50 (|dx| < 1.5 * |dy|)
        val result = evaluateGesture(dx = 60f, dy = 50f)
        assertEquals(GestureResult.REJECTED, result)
    }

    @Test
    fun `test rejection of sub-threshold displacement`() {
        // Horizontal but under 40dp threshold: dx = 30, dy = 5
        val result = evaluateGesture(dx = 30f, dy = 5f)
        assertEquals(GestureResult.REJECTED, result)
    }

    @Test
    fun `test discrimination ratio mathematical boundary`() {
        // Exactly at ratio 1.5: dx = 60, dy = 40 (60 == 1.5 * 40)
        val validRatio = evaluateGesture(dx = 60f, dy = 40f)
        assertEquals(GestureResult.SWIPE_RIGHT, validRatio)

        // Just below ratio 1.5: dx = 59, dy = 40 (59 < 1.5 * 40 = 60)
        val invalidRatio = evaluateGesture(dx = 59f, dy = 40f)
        assertEquals(GestureResult.REJECTED, invalidRatio)
    }
}
