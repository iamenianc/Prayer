package au.prayer.app.ui.gestures

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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

/**
 * Evaluates whether two consecutive tap events constitute a valid double tap.
 * Rejects rapid bounces (< 30ms), timeouts (> 350ms), drags, and long presses (> 400ms).
 */
fun evaluateDoubleTap(
    firstDownTime: Long,
    firstUpTime: Long,
    firstUpPosition: Offset,
    secondDownTime: Long,
    secondDownPosition: Offset,
    doubleTapTimeoutMs: Long = 350L,
    touchSlopPx: Float = 40f
): Boolean {
    if (firstDownTime <= 0L || firstUpTime <= 0L) return false

    val tapDuration = firstUpTime - firstDownTime
    if (tapDuration > 400L || tapDuration < 0L) return false

    val timeBetweenTaps = secondDownTime - firstUpTime
    if (timeBetweenTaps !in 30L..doubleTapTimeoutMs) return false

    val distance = (secondDownPosition - firstUpPosition).getDistance()
    return distance <= touchSlopPx
}

/**
 * Identifies if a character is a word constituent (letters, digits, underscore, apostrophes in contractions).
 */
fun isWordConstituent(c: Char): Boolean {
    return c.isLetterOrDigit() || c == '_' || c == '\'' || c == '’'
}

/**
 * Calculates the word boundary TextRange around an offset in a string.
 * Supports contractions ("Peter's", "doesn't"), bullet-prefixed lines, and terminal offsets.
 */
fun findWordBoundary(
    text: String,
    offset: Int,
    textLayoutResult: TextLayoutResult? = null
): TextRange {
    if (text.isEmpty()) return TextRange.Zero

    // 1. If TextLayoutResult is available, attempt platform ICU BreakIterator word boundary
    if (textLayoutResult != null && text.isNotEmpty()) {
        try {
            val clamped = offset.coerceIn(0, (text.length - 1).coerceAtLeast(0))
            val boundary = textLayoutResult.getWordBoundary(clamped)
            val sub = text.substring(boundary.min, boundary.max)
            if (boundary.length > 0 && sub.any { isWordConstituent(it) }) {
                return boundary
            }
            if (clamped > 0) {
                val prevBoundary = textLayoutResult.getWordBoundary(clamped - 1)
                val prevSub = text.substring(prevBoundary.min, prevBoundary.max)
                if (prevBoundary.length > 0 && prevSub.any { isWordConstituent(it) }) {
                    return prevBoundary
                }
            }
        } catch (_: Throwable) {
            // Fallback to pure string calculation
        }
    }

    // 2. Pure string calculation
    val clamped = offset.coerceIn(0, text.length)
    val targetIndex = if (clamped == text.length && clamped > 0) {
        clamped - 1
    } else if (clamped < text.length && !isWordConstituent(text[clamped]) && clamped > 0 && isWordConstituent(text[clamped - 1])) {
        clamped - 1
    } else {
        clamped.coerceIn(0, (text.length - 1).coerceAtLeast(0))
    }

    val targetChar = text[targetIndex]
    return if (isWordConstituent(targetChar)) {
        var start = targetIndex
        while (start > 0 && isWordConstituent(text[start - 1])) {
            start--
        }
        var end = targetIndex + 1
        while (end < text.length && isWordConstituent(text[end])) {
            end++
        }
        TextRange(start, end)
    } else {
        // If symbol/bullet, look ahead on the same line for the first word
        var forward = targetIndex
        while (forward < text.length && text[forward] != '\n' && !isWordConstituent(text[forward])) {
            forward++
        }
        if (forward < text.length && isWordConstituent(text[forward])) {
            var end = forward + 1
            while (end < text.length && isWordConstituent(text[end])) {
                end++
            }
            TextRange(forward, end)
        } else {
            TextRange(targetIndex, (targetIndex + 1).coerceAtMost(text.length))
        }
    }
}

/**
 * Finds the word boundary at click position, or near the typing cursor if click was near cursor.
 */
fun findWordBoundaryAt(
    text: String,
    clickOffset: Int,
    cursorOffset: Int,
    layout: TextLayoutResult? = null
): TextRange {
    val fromClick = findWordBoundary(text, clickOffset, layout)
    val clickSub = if (fromClick.length > 0 && fromClick.max <= text.length) text.substring(fromClick.min, fromClick.max) else ""
    if (clickSub.any { isWordConstituent(it) }) {
        return fromClick
    }

    // If click didn't yield a word (e.g. whitespace or boundary), check the cursor offset
    if (cursorOffset in 0..text.length) {
        val fromCursor = findWordBoundary(text, cursorOffset, layout)
        val cursorSub = if (fromCursor.length > 0 && fromCursor.max <= text.length) text.substring(fromCursor.min, fromCursor.max) else ""
        if (cursorSub.any { isWordConstituent(it) }) {
            return fromCursor
        }
    }

    return fromClick
}

/**
 * Attaches double-tap word selection gesture detection to a text input.
 * On double tap:
 * - Selects the word at the tap location or under the typing cursor.
 * - Consumes the second tap so the text field does not collapse the selection back to a cursor.
 * On single tap, drag, or long press:
 * - Leaves pointer events unconsumed for native text field focus, scrolling, and cursor positioning.
 */
fun Modifier.doubleTapWordSelection(
    getText: () -> TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    textLayoutResult: () -> TextLayoutResult?,
    contentPaddingStart: Float = 0f,
    contentPaddingTop: Float = 0f
): Modifier = this.pointerInput(Unit) {
    var lastTapDownTime = 0L
    var lastTapUpTime = 0L
    var lastTapUpPos = Offset.Zero

    val doubleTapTimeout = viewConfiguration.doubleTapTimeoutMillis
    val touchSlop = viewConfiguration.touchSlop
    val doubleTapSlop = touchSlop * 2.5f

    awaitEachGesture {
        val down = awaitFirstDown(pass = PointerEventPass.Initial, requireUnconsumed = false)
        val downTime = down.uptimeMillis
        val downPos = down.position

        val isDoubleTap = evaluateDoubleTap(
            firstDownTime = lastTapDownTime,
            firstUpTime = lastTapUpTime,
            firstUpPosition = lastTapUpPos,
            secondDownTime = downTime,
            secondDownPosition = downPos,
            doubleTapTimeoutMs = doubleTapTimeout,
            touchSlopPx = doubleTapSlop
        )

        if (isDoubleTap) {
            // Consume the second tap's down event so the text field does not collapse selection
            down.consume()

            val currentTfv = getText()
            val layout = textLayoutResult()
            val layoutPos = Offset(
                (downPos.x - contentPaddingStart).coerceAtLeast(0f),
                (downPos.y - contentPaddingTop).coerceAtLeast(0f)
            )

            val isBelowText = layout != null && layout.lineCount > 0 && layoutPos.y > (layout.getLineBottom(layout.lineCount - 1) + 20f)
            if (isBelowText) {
                onTextChange(currentTfv.copy(selection = TextRange(currentTfv.text.length)))
            } else {
                val charOffset = if (layout != null) {
                    layout.getOffsetForPosition(layoutPos)
                } else {
                    currentTfv.selection.start
                }

                val wordRange = findWordBoundaryAt(
                    text = currentTfv.text,
                    clickOffset = charOffset,
                    cursorOffset = currentTfv.selection.start,
                    layout = layout
                )

                if (wordRange.length > 0) {
                    onTextChange(currentTfv.copy(selection = wordRange))
                }
            }

            // Consume all events until release
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                change.consume()
                if (!change.pressed) break
            }

            lastTapDownTime = 0L
            lastTapUpTime = 0L
            lastTapUpPos = Offset.Zero
        } else {
            var isDrag = false
            var upTime = 0L
            var upPos = downPos

            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if ((change.position - downPos).getDistance() > touchSlop) {
                    isDrag = true
                }
                if (!change.pressed) {
                    upTime = change.uptimeMillis
                    upPos = change.position
                    break
                }
            }

            if (!isDrag && (upTime - downTime) in 10L..400L) {
                lastTapDownTime = downTime
                lastTapUpTime = upTime
                lastTapUpPos = upPos
            } else {
                lastTapDownTime = 0L
                lastTapUpTime = 0L
                lastTapUpPos = Offset.Zero
            }
        }
    }
}

/**
 * Pure calculation function for zoom scaling under pinch gestures.
 * Clamps result strictly within [minScale, maxScale] and rejects invalid/NaN/infinite factors.
 */
fun calculateZoomScale(
    currentScale: Float,
    factor: Float,
    minScale: Float = 0.75f,
    maxScale: Float = 2.5f
): Float {
    if (factor.isNaN() || factor.isInfinite() || factor <= 0f) {
        return currentScale.coerceIn(minScale, maxScale)
    }
    val target = currentScale * factor
    return target.coerceIn(minScale, maxScale)
}

/**
 * Multi-touch pinch-to-zoom gesture modifier.
 * - Detects multi-touch gestures when >= 2 pointers are down.
 * - Dynamically computes pinch distance ratios to adjust zoom scale factor.
 * - Consumes pointer changes ONLY when 2 or more fingers are actively pinching,
 *   preventing single-finger vertical scroll or swipe navigation from firing during zoom.
 * - Single-finger gestures pass through completely unconsumed.
 */
fun Modifier.pinchToZoom(
    onZoomChange: (factor: Float) -> Unit,
    onZoomStart: () -> Unit = {},
    onZoomEnd: () -> Unit = {}
): Modifier = this.pointerInput(onZoomChange, onZoomStart, onZoomEnd) {
    awaitEachGesture {
        do {
            val event = awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
            val downPointers = event.changes.filter { it.pressed }
            if (downPointers.size >= 2) {
                onZoomStart()
                var prevDistance = (downPointers[0].position - downPointers[1].position).getDistance()
                while (true) {
                    val nextEvent = awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                    val activePointers = nextEvent.changes.filter { it.pressed }
                    if (activePointers.size < 2) {
                        activePointers.forEach { it.consume() }
                        break
                    }
                    val currentDistance = (activePointers[0].position - activePointers[1].position).getDistance()
                    if (prevDistance > 0f && currentDistance > 0f) {
                        val factor = currentDistance / prevDistance
                        if (!factor.isNaN() && !factor.isInfinite() && factor > 0f) {
                            onZoomChange(factor)
                        }
                    }
                    prevDistance = currentDistance
                    activePointers.forEach { it.consume() }
                }
                onZoomEnd()
                break
            }
        } while (event.changes.any { it.pressed })
    }
}

