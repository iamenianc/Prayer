package au.prayer.app

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextRange
import au.prayer.app.ui.gestures.evaluateDoubleTap
import au.prayer.app.ui.gestures.isWordConstituent
import au.prayer.app.ui.gestures.findWordBoundary
import au.prayer.app.ui.gestures.findWordBoundaryAt
import au.prayer.app.ui.gestures.calculateZoomScale
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

    @Test
    fun `test evaluateDoubleTap accepts valid rapid consecutive taps`() {
        // Tap 1: down at t=1000, up at t=1080 at (100, 200)
        // Tap 2: down at t=1200 (120ms later) at (105, 202) (distance ~5.3px <= 40px)
        val isDoubleTap = evaluateDoubleTap(
            firstDownTime = 1000L,
            firstUpTime = 1080L,
            firstUpPosition = Offset(100f, 200f),
            secondDownTime = 1200L,
            secondDownPosition = Offset(105f, 202f),
            doubleTapTimeoutMs = 350L,
            touchSlopPx = 40f
        )
        assertTrue(isDoubleTap)
    }

    @Test
    fun `test evaluateDoubleTap rejects expired timeouts and excessive displacement`() {
        // Expired timeout: second tap 400ms after first up (> 350ms)
        assertFalse(
            evaluateDoubleTap(
                firstDownTime = 1000L,
                firstUpTime = 1080L,
                firstUpPosition = Offset(100f, 200f),
                secondDownTime = 1481L,
                secondDownPosition = Offset(102f, 201f),
                doubleTapTimeoutMs = 350L
            )
        )

        // Excessive displacement: distance 60px > 40px
        assertFalse(
            evaluateDoubleTap(
                firstDownTime = 1000L,
                firstUpTime = 1080L,
                firstUpPosition = Offset(100f, 200f),
                secondDownTime = 1200L,
                secondDownPosition = Offset(160f, 200f),
                touchSlopPx = 40f
            )
        )

        // Bounced touch too fast (< 30ms)
        assertFalse(
            evaluateDoubleTap(
                firstDownTime = 1000L,
                firstUpTime = 1080L,
                firstUpPosition = Offset(100f, 200f),
                secondDownTime = 1090L,
                secondDownPosition = Offset(100f, 200f)
            )
        )

        // First tap was a long press (> 400ms duration)
        assertFalse(
            evaluateDoubleTap(
                firstDownTime = 1000L,
                firstUpTime = 1450L,
                firstUpPosition = Offset(100f, 200f),
                secondDownTime = 1550L,
                secondDownPosition = Offset(100f, 200f)
            )
        )
    }

    @Test
    fun `test isWordConstituent recognizes letters, digits, underscores, and contractions`() {
        assertTrue(isWordConstituent('P'))
        assertTrue(isWordConstituent('e'))
        assertTrue(isWordConstituent('9'))
        assertTrue(isWordConstituent('_'))
        assertTrue(isWordConstituent('\'')) // Straight apostrophe in "Peter's"
        assertTrue(isWordConstituent('’')) // Curly apostrophe in "God’s"
        assertTrue(isWordConstituent('θ')) // Greek
        assertTrue(isWordConstituent('א')) // Hebrew

        assertFalse(isWordConstituent(' '))
        assertFalse(isWordConstituent('\t'))
        assertFalse(isWordConstituent('\n'))
        assertFalse(isWordConstituent('•'))
        assertFalse(isWordConstituent(','))
        assertFalse(isWordConstituent('.'))
        assertFalse(isWordConstituent('!'))
    }

    @Test
    fun `test findWordBoundary selects complete word under cursor or tap`() {
        val text = "• Complete recovery from surgery"
        // "Complete" is indices 2..10
        // "recovery" is indices 11..19
        // "from" is indices 20..24
        // "surgery" is indices 25..32

        // Tapping/cursor inside "recovery" at index 14
        val range1 = findWordBoundary(text, offset = 14)
        assertEquals(TextRange(11, 19), range1)
        assertEquals("recovery", text.substring(range1.min, range1.max))

        // Tapping at start of "recovery" at index 11
        val range2 = findWordBoundary(text, offset = 11)
        assertEquals(TextRange(11, 19), range2)

        // Tapping right at the end of "recovery" at index 19 (adjacent space or terminal)
        val range3 = findWordBoundary(text, offset = 19)
        assertEquals(TextRange(11, 19), range3)

        // Tapping at the very end of string at index 32
        val range4 = findWordBoundary(text, offset = 32)
        assertEquals(TextRange(25, 32), range4)
        assertEquals("surgery", text.substring(range4.min, range4.max))
    }

    @Test
    fun `test findWordBoundary preserves contractions with apostrophes`() {
        val text = "• God's grace and Peter’s faith"
        // "God's" is indices 2..7
        val range1 = findWordBoundary(text, offset = 5) // On the straight apostrophe
        assertEquals(TextRange(2, 7), range1)
        assertEquals("God's", text.substring(range1.min, range1.max))

        // "Peter’s" with curly apostrophe (indices 18..25)
        val range2 = findWordBoundary(text, offset = 23)
        assertEquals(TextRange(18, 25), range2)
        assertEquals("Peter’s", text.substring(range2.min, range2.max))
    }

    @Test
    fun `test findWordBoundary selects first word when double-tapping bullet symbol`() {
        val text = "• Healing and comfort"
        // Bullet is index 0, space is index 1, "Healing" is indices 2..9
        val range = findWordBoundary(text, offset = 0)
        assertEquals(TextRange(2, 9), range)
        assertEquals("Healing", text.substring(range.min, range.max))
    }

    @Test
    fun `test findWordBoundary handles empty and single-character edge cases`() {
        assertEquals(TextRange.Zero, findWordBoundary("", offset = 0))
        assertEquals(TextRange(0, 1), findWordBoundary("a", offset = 0))
        assertEquals(TextRange(0, 1), findWordBoundary("a", offset = 1))
    }

    @Test
    fun `test findWordBoundaryAt uses cursor fallback when click is on whitespace boundary`() {
        val text = "• Pray for peace"
        // Cursor is inside "peace" at index 13
        // Click lands on whitespace at index 10 (between "for" and "peace")
        val range = findWordBoundaryAt(
            text = text,
            clickOffset = 10,
            cursorOffset = 13,
            layout = null
        )
        // Should select "for" from click or "peace" from cursor
        assertTrue(range.length > 0)
        val selected = text.substring(range.min, range.max)
        assertTrue(selected == "for" || selected == "peace")
    }

    @Test
    fun `test calculateZoomScale normal scaling in and out`() {
        val scaledUp = calculateZoomScale(1.0f, 1.25f)
        assertEquals(1.25f, scaledUp, 0.001f)

        val scaledDown = calculateZoomScale(1.25f, 0.8f)
        assertEquals(1.0f, scaledDown, 0.001f)
    }

    @Test
    fun `test calculateZoomScale clamping at minimum bound 0_75f`() {
        val clampedMin = calculateZoomScale(0.80f, 0.5f)
        assertEquals(0.75f, clampedMin, 0.001f)

        val belowMin = calculateZoomScale(0.75f, 0.9f)
        assertEquals(0.75f, belowMin, 0.001f)
    }

    @Test
    fun `test calculateZoomScale clamping at maximum bound 2_5f`() {
        val clampedMax = calculateZoomScale(2.0f, 1.5f)
        assertEquals(2.5f, clampedMax, 0.001f)

        val aboveMax = calculateZoomScale(2.5f, 1.1f)
        assertEquals(2.5f, aboveMax, 0.001f)
    }

    @Test
    fun `test calculateZoomScale rejects invalid non-positive and NaN factors`() {
        assertEquals(1.0f, calculateZoomScale(1.0f, Float.NaN), 0.001f)
        assertEquals(1.0f, calculateZoomScale(1.0f, Float.POSITIVE_INFINITY), 0.001f)
        assertEquals(1.0f, calculateZoomScale(1.0f, Float.NEGATIVE_INFINITY), 0.001f)
        assertEquals(1.0f, calculateZoomScale(1.0f, 0f), 0.001f)
        assertEquals(1.0f, calculateZoomScale(1.0f, -0.5f), 0.001f)
    }

    @Test
    fun `test findWordBoundary on multiline string with leading bullet on second line`() {
        val multiline = "• First line\n• Second line"
        // Index of 'S' in "Second" is 15
        val range = findWordBoundary(multiline, offset = 16)
        assertEquals(TextRange(15, 21), range)
        assertEquals("Second", multiline.substring(range.min, range.max))
    }
}

