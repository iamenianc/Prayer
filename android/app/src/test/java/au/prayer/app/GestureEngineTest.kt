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

enum class CardSwipeAction {
    TOGGLE_ANSWERED,
    REVEAL_DELETE,
    REJECTED
}

fun evaluateGesture(
    dx: Float,
    dy: Float,
    threshold: Float = 40f,
    exitThreshold: Float = 60f,
    yStart: Float? = null,
    maxExitYStart: Float = 150f
): GestureResult {
    val absDx = abs(dx)
    val absDy = abs(dy)

    return if (absDx >= 1.5f * absDy && absDx >= threshold) {
        if (dx < 0) GestureResult.SWIPE_LEFT else GestureResult.SWIPE_RIGHT
    } else if (dy >= exitThreshold && dy >= 1.5f * absDx) {
        if (yStart == null || yStart <= maxExitYStart) {
            GestureResult.SWIPE_DOWN
        } else {
            GestureResult.REJECTED
        }
    } else {
        GestureResult.REJECTED
    }
}

fun evaluateCardSwipe(dx: Float, dy: Float, threshold: Float = 60f): CardSwipeAction {
    val absDx = abs(dx)
    val absDy = abs(dy)

    return if (absDx >= 1.5f * absDy && absDx >= threshold) {
        if (dx > 0) CardSwipeAction.TOGGLE_ANSWERED else CardSwipeAction.REVEAL_DELETE
    } else {
        CardSwipeAction.REJECTED
    }
}

fun evaluateEdgeBackSwipe(
    xStart: Float,
    dx: Float,
    dy: Float = 0f,
    edgeThreshold: Float = 25f,
    distanceThreshold: Float = 50f
): Boolean {
    val absDx = abs(dx)
    val absDy = abs(dy)
    return xStart <= edgeThreshold && dx >= distanceThreshold && (dy == 0f || absDx >= 1.5f * absDy)
}

fun evaluateLongPress(
    durationMs: Long,
    thresholdMs: Long = 400L,
    displacementPx: Float = 0f,
    maxDisplacementPx: Float = 10f
): Boolean {
    return durationMs >= thresholdMs && displacementPx <= maxDisplacementPx
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
    fun `test swipe down dismissal in upper screen zone`() {
        // Vertical swipe down initiated near top: dx = 5, dy = 90, yStart = 80
        val result = evaluateGesture(dx = 5f, dy = 90f, yStart = 80f)
        assertEquals(GestureResult.SWIPE_DOWN, result)
    }

    @Test
    fun `test swipe down rejected when initiated below upper screen threshold`() {
        // Vertical swipe down initiated mid-screen: dx = 5, dy = 90, yStart = 350 (should reject to allow scroll)
        val result = evaluateGesture(dx = 5f, dy = 90f, yStart = 350f)
        assertEquals(GestureResult.REJECTED, result)
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

    @Test
    fun `test list card swipe right toggles answered status`() {
        val result = evaluateCardSwipe(dx = 80f, dy = 10f)
        assertEquals(CardSwipeAction.TOGGLE_ANSWERED, result)
    }

    @Test
    fun `test list card swipe left reveals delete action`() {
        val result = evaluateCardSwipe(dx = -80f, dy = 10f)
        assertEquals(CardSwipeAction.REVEAL_DELETE, result)
    }

    @Test
    fun `test list card swipe rejects sub-threshold and vertical gestures`() {
        // Below 60px threshold
        assertEquals(CardSwipeAction.REJECTED, evaluateCardSwipe(dx = 45f, dy = 5f))
        assertEquals(CardSwipeAction.REJECTED, evaluateCardSwipe(dx = -45f, dy = 5f))

        // Dominant vertical gesture
        assertEquals(CardSwipeAction.REJECTED, evaluateCardSwipe(dx = 70f, dy = 60f))
    }

    @Test
    fun `test universal edge-swipe back navigation`() {
        // Valid edge-swipe: starts at X=15 <= 25, moves +70 >= 50
        assertTrue(evaluateEdgeBackSwipe(xStart = 15f, dx = 70f))

        // Valid edge-swipe at exact thresholds: starts at X=25, moves +50 with minor dy
        assertTrue(evaluateEdgeBackSwipe(xStart = 25f, dx = 50f, dy = 10f))

        // Invalid: started too far from left edge (X=40 > 25)
        assertFalse(evaluateEdgeBackSwipe(xStart = 40f, dx = 70f))

        // Invalid: displacement too short (dx = 30 < 50)
        assertFalse(evaluateEdgeBackSwipe(xStart = 10f, dx = 30f))

        // Invalid: leftward motion from edge (dx = -60)
        assertFalse(evaluateEdgeBackSwipe(xStart = 10f, dx = -60f))

        // Invalid: dominant vertical swipe (scroll intent) starting at edge (dx = 55, dy = 80)
        assertFalse(evaluateEdgeBackSwipe(xStart = 10f, dx = 55f, dy = 80f))
    }

    @Test
    fun `test long press tactile detection threshold and displacement guard`() {
        // Valid long press: duration 500ms >= 400ms, displacement 2px <= 10px
        assertTrue(evaluateLongPress(durationMs = 500L, displacementPx = 2f))

        // Valid long press exact threshold: duration 400ms, displacement 0px
        assertTrue(evaluateLongPress(durationMs = 400L, displacementPx = 0f))

        // Invalid: short tap (150ms < 400ms)
        assertFalse(evaluateLongPress(durationMs = 150L, displacementPx = 1f))

        // Invalid: drag motion during press (displacement 25px > 10px, scrolls list)
        assertFalse(evaluateLongPress(durationMs = 600L, displacementPx = 25f))
    }
}
