package au.prayer.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import au.prayer.app.ui.theme.PrayerSpacing

/**
 * The Silk Marker Ribbon (Interactive Bookmark Tab).
 * Modeled on an authentic textile bookmark in a fine leatherbound journal.
 * - Width: 18dp
 * - Resting Height: 40dp, extending to 54dp when active/pinned
 * - Spring: stiffness 220, damping 0.70
 * - Swallow-Tail Notch: 6dp triangular inset centered at X = 9dp
 * - Touch Envelope: Expanded to 48dp horizontal x 56dp vertical
 */
@Composable
fun SilkMarkerRibbon(
    isPinned: Boolean,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF8B2635), // Garnet Crimson (ribbon.primary)
    touchWidth: Dp = PrayerSpacing.minTouchTarget,
    touchHeight: Dp = 56.dp
) {
    val haptic = LocalHapticFeedback.current

    val ribbonHeight by animateDpAsState(
        targetValue = if (isPinned) PrayerSpacing.ribbonPinnedHeight else PrayerSpacing.ribbonRestingHeight,
        animationSpec = spring(stiffness = 220f, dampingRatio = 0.70f),
        label = "RibbonHeightAnimation"
    )

    Box(
        modifier = modifier
            .size(width = touchWidth, height = touchHeight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTogglePin()
                }
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(modifier = Modifier.size(width = PrayerSpacing.ribbonWidth, height = ribbonHeight)) {
            val w = size.width
            val h = size.height
            val notchDepth = 6.dp.toPx()

            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h)
                lineTo(w / 2f, h - notchDepth)
                lineTo(0f, h)
                close()
            }

            // Clean vector jewel tone with subtle 5% tonal shade toward bottom edge (§11.2)
            val shadedColor = Color(
                red = (color.red * 0.95f).coerceIn(0f, 1f),
                green = (color.green * 0.95f).coerceIn(0f, 1f),
                blue = (color.blue * 0.95f).coerceIn(0f, 1f),
                alpha = color.alpha
            )
            val ribbonBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(color, shadedColor),
                startY = 0f,
                endY = h
            )
            drawPath(path = path, brush = ribbonBrush)
        }
    }
}
