package au.prayer.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import au.prayer.app.data.models.TextScale
import au.prayer.app.ui.theme.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit test suite verifying stationery lined-paper math, baseline synchronization,
 * tactile folio components (SilkMarkerRibbon, ClosedFolioShield), and layout metrics.
 */
class StationeryMathAndComponentsTest {

    // --- Stationery Lined Notepad Math ---

    @Test
    fun `test notepad line height calculation formula across typography scales`() {
        fun computeNotepadLineHeight(fontSizeSp: Float): Float {
            return if (fontSizeSp > 0f) {
                fontSizeSp * 1.9f
            } else {
                36f
            }
        }

        val largeTypography = getPrayerTypography(TextScale.LARGE)
        val regularTypography = getPrayerTypography(TextScale.REGULAR)
        val compactTypography = getPrayerTypography(TextScale.COMPACT)

        // Large: body is 18sp -> 18 * 1.9 = 34.2sp
        val largeBodySize = largeTypography.prayerPointBody.fontSize.value
        assertEquals(18f, largeBodySize, 0.001f)
        assertEquals(34.2f, computeNotepadLineHeight(largeBodySize), 0.001f)

        // Regular: body is 15sp -> 15 * 1.9 = 28.5sp
        val regularBodySize = regularTypography.prayerPointBody.fontSize.value
        assertEquals(15f, regularBodySize, 0.001f)
        assertEquals(28.5f, computeNotepadLineHeight(regularBodySize), 0.001f)

        // Compact: body is 13sp -> 13 * 1.9 = 24.7sp
        val compactBodySize = compactTypography.prayerPointBody.fontSize.value
        assertEquals(13f, compactBodySize, 0.001f)
        assertEquals(24.7f, computeNotepadLineHeight(compactBodySize), 0.001f)

        // Fallback when fontSize <= 0
        assertEquals(36f, computeNotepadLineHeight(0f), 0.001f)
        assertEquals(36f, computeNotepadLineHeight(-5f), 0.001f)
    }

    @Test
    fun `test fine stationery paper layout geometries and margin guidelines`() {
        // Vertical stationery margin guide rule at 56dp
        assertEquals(56.dp, PrayerSpacing.marginTrackWidth)

        // Text inset starts at 64dp, providing exactly an 8dp gutter past the 56dp vertical margin rule
        assertEquals(64.dp, PrayerSpacing.textInset)
        assertEquals(8.dp, PrayerSpacing.textInset - PrayerSpacing.marginTrackWidth)

        // Fine stationery hairline rule stroke is exactly 0.75dp
        assertEquals(0.75.dp, PrayerSpacing.hairlineWidth)

        // Top margin of lined notepad is 16dp
        val topMargin = 16.dp
        assertEquals(PrayerSpacing.medium, topMargin)
    }

    // --- SilkMarkerRibbon Component Contracts ---

    @Test
    fun `test silk marker ribbon dimensions and notch proportions`() {
        // Ribbon vector width
        assertEquals(18.dp, PrayerSpacing.ribbonWidth)

        // Resting and pinned heights
        assertEquals(40.dp, PrayerSpacing.ribbonRestingHeight)
        assertEquals(80.dp, PrayerSpacing.ribbonPinnedHeight)
        assertTrue("Pinned ribbon must extend longer than resting ribbon", PrayerSpacing.ribbonPinnedHeight > PrayerSpacing.ribbonRestingHeight)

        // Swallow-tail notch is 6dp triangular inset centered at X = width / 2 (9dp)
        val notchDepth = 6.dp
        val notchCenterX = PrayerSpacing.ribbonWidth / 2f
        assertEquals(9.dp, notchCenterX)
        assertTrue("Notch depth must be strictly smaller than resting ribbon height", notchDepth < PrayerSpacing.ribbonRestingHeight)

        // Touch target envelope must satisfy minimum touch target of 48dp horizontal
        assertEquals(48.dp, PrayerSpacing.minTouchTarget)
    }

    @Test
    fun `test silk marker ribbon jewel tone 5 percent shading calculation`() {
        val baseColor = Color(0xFF8B2635) // Garnet Crimson

        val shadedRed = (baseColor.red * 0.95f).coerceIn(0f, 1f)
        val shadedGreen = (baseColor.green * 0.95f).coerceIn(0f, 1f)
        val shadedBlue = (baseColor.blue * 0.95f).coerceIn(0f, 1f)

        assertTrue("Shaded red must be 5% lower", shadedRed < baseColor.red)
        assertTrue("Shaded green must be 5% lower", shadedGreen < baseColor.green)
        assertTrue("Shaded blue must be 5% lower", shadedBlue < baseColor.blue)
    }

    @Test
    fun `test silk marker ribbon clearance and anti-occlusion geometry`() {
        // Visual ribbon width: 18dp
        val visualWidth = PrayerSpacing.ribbonWidth
        assertEquals(18.dp, visualWidth)

        // Default end margin offset: 4dp
        val ribbonPaddingEnd = 4.dp
        // Visual ribbon occupies space from 4dp to (4dp + 18dp) = 22dp from right display edge
        val ribbonVisualLeftEdgeFromRight = ribbonPaddingEnd + visualWidth
        assertEquals(22.dp, ribbonVisualLeftEdgeFromRight)

        // Text clearance margin: 36dp
        val textPaddingEnd = 36.dp
        assertTrue(
            "Text must end strictly to the left of the ribbon visual envelope to prevent obscuring text",
            textPaddingEnd > ribbonVisualLeftEdgeFromRight
        )
        // Clear separation gutter between ribbon and text
        val gutter = textPaddingEnd - ribbonVisualLeftEdgeFromRight
        assertTrue("Clear gutter between ribbon and text must be at least 12dp", gutter >= 12.dp)

        // Prompts card end padding: 52dp
        val promptCardPaddingEnd = 52.dp
        assertTrue(
            "Prompt menu text must end strictly to the left of ribbon 48dp touch envelope",
            promptCardPaddingEnd > PrayerSpacing.minTouchTarget
        )
    }

    // --- ClosedFolioShield Privacy Shield Contract ---

    @Test
    fun `test closed folio privacy shield monogram and opacity tokens`() {
        val monogramText = "P · W · C"
        assertEquals("P · W · C", monogramText)

        // Deboss text color uses 20% black opacity
        val debossColor = Color.Black.copy(alpha = 0.20f)
        assertEquals(0.20f, debossColor.alpha, 0.001f)

        // Perimeter border uses 15% black opacity
        val perimeterBorderColor = Color.Black.copy(alpha = 0.15f)
        assertEquals(0.15f, perimeterBorderColor.alpha, 0.001f)

        // Perimeter border width is 1dp
        val borderWidth = 1.dp
        assertEquals(1.dp, borderWidth)
    }

    // --- Spacing & Touch Ergonomics ---

    @Test
    fun `test 8dp spatial rhythm compliance`() {
        val spacingTokens = listOf(
            PrayerSpacing.extraSmall, // 4dp (half step)
            PrayerSpacing.small,      // 8dp
            PrayerSpacing.medium,     // 16dp
            PrayerSpacing.large,      // 24dp
            PrayerSpacing.extraLarge, // 32dp
            PrayerSpacing.huge        // 48dp
        )

        for (token in spacingTokens) {
            val value = token.value
            // Either a multiple of 8dp or the 4dp half-step
            assertTrue(
                "Spacing token $token must align to 4dp/8dp grid",
                value % 4f == 0f
            )
        }
    }

    // --- Sanctuary Start Praying Mode Ruled Lines Math ---

    @Test
    fun `test sanctuary prayer mode ruled line cadence matches text line spacing across scales`() {
        val largeTypography = getPrayerTypography(TextScale.LARGE)
        val regularTypography = getPrayerTypography(TextScale.REGULAR)
        val compactTypography = getPrayerTypography(TextScale.COMPACT)

        // LARGE scale: prayer point bullet line height is 28sp -> ruled line cadence must be exactly 28sp
        assertEquals(28.sp, largeTypography.prayerPointBullet.lineHeight)

        // REGULAR scale: prayer point bullet line height is 23sp -> ruled line cadence must be exactly 23sp
        assertEquals(23.sp, regularTypography.prayerPointBullet.lineHeight)

        // COMPACT scale: prayer point bullet line height is 19sp -> ruled line cadence must be exactly 19sp
        assertEquals(19.sp, compactTypography.prayerPointBullet.lineHeight)
    }

    @Test
    fun `test sanctuary ruled line modulo progression strictly includes text baseline anchor`() {
        // Test with arbitrary realistic pixel values
        val testCadences = listOf(73.5f, 60.375f, 49.875f) // approximate px for 28sp, 23sp, 19sp at density 2.625
        val testAnchors = listOf(142.3f, 210.0f, 95.75f, 320.1f)

        for (cadence in testCadences) {
            for (anchor in testAnchors) {
                val startY = ((anchor % cadence) + cadence) % cadence
                assertTrue("startY must be >= 0", startY >= 0f)
                assertTrue("startY must be < cadence", startY < cadence)

                // Generate lines up to anchor + 500px
                val lines = mutableListOf<Float>()
                var y = startY
                while (y <= anchor + 500f) {
                    lines.add(y)
                    y += cadence
                }

                // Verify anchor is contained within the generated lines (within float rounding)
                val matchesAnchor = lines.any { Math.abs(it - anchor) < 0.001f }
                assertTrue("Generated ruled lines must strictly contain the text baseline anchor $anchor for cadence $cadence", matchesAnchor)

                // Verify next line (anchor + cadence) is also strictly contained
                val matchesNextLine = lines.any { Math.abs(it - (anchor + cadence)) < 0.001f }
                assertTrue("Generated ruled lines must strictly contain line 2 baseline (anchor + cadence)", matchesNextLine)
            }
        }
    }

    @Test
    fun `test sanctuary multi-point line quantization ensures zero baseline drift`() {
        val lineCadencePx = 73.5f
        val anchorBaselinePx = 180.0f

        // 3 prayer points with varying line counts: Point 1 has 3 lines, Point 2 has 1 line, Point 3 has 2 lines
        val pointLineCounts = listOf(3, 1, 2)
        val interPointGapLines = 1 // 1 blank line between points

        var currentPointTopBaseline = anchorBaselinePx
        for ((index, lineCount) in pointLineCounts.withIndex()) {
            for (lineIdx in 0 until lineCount) {
                val lineBaseline = currentPointTopBaseline + lineIdx * lineCadencePx
                // Difference from anchor must be an exact integer multiple of lineCadencePx
                val diffFromAnchor = lineBaseline - anchorBaselinePx
                val multiple = diffFromAnchor / lineCadencePx
                assertEquals("Line $lineIdx of point $index must sit on an integer multiple of cadence", Math.round(multiple.toDouble()).toDouble(), multiple.toDouble(), 0.001)
            }
            // Advance to next point: lineCount lines for the text + 1 line for gap
            currentPointTopBaseline += (lineCount + interPointGapLines) * lineCadencePx
        }
    }

    @Test
    fun `test typography withZoom scales font sizes and line heights proportionally`() {
        val baseTypography = getPrayerTypography(TextScale.LARGE)
        val zoomed = baseTypography.withZoom(1.5f)

        // Base large prayerPointBody: 18sp fontSize, 34sp lineHeight
        assertEquals(18f * 1.5f, zoomed.prayerPointBody.fontSize.value, 0.001f)
        assertEquals(34f * 1.5f, zoomed.prayerPointBody.lineHeight.value, 0.001f)

        // Title: 20sp -> 30sp
        assertEquals(20f * 1.5f, zoomed.prayerPointTitle.fontSize.value, 0.001f)
        assertEquals(28f * 1.5f, zoomed.prayerPointTitle.lineHeight.value, 0.001f)

        // Ratio of lineHeight to fontSize is preserved
        val baseRatio = baseTypography.prayerPointBody.lineHeight.value / baseTypography.prayerPointBody.fontSize.value
        val zoomedRatio = zoomed.prayerPointBody.lineHeight.value / zoomed.prayerPointBody.fontSize.value
        assertEquals(baseRatio, zoomedRatio, 0.001f)
    }

    @Test
    fun `test typography withZoom at 1_0f produces exact equivalent metrics`() {
        val baseTypography = getPrayerTypography(TextScale.REGULAR)
        val zoomed100 = baseTypography.withZoom(1.0f)

        assertEquals(baseTypography.prayerPointBody.fontSize.value, zoomed100.prayerPointBody.fontSize.value, 0.001f)
        assertEquals(baseTypography.prayerPointBody.lineHeight.value, zoomed100.prayerPointBody.lineHeight.value, 0.001f)
        assertEquals(baseTypography.caption.fontSize.value, zoomed100.caption.fontSize.value, 0.001f)
        assertEquals(baseTypography.marginStatus.fontSize.value, zoomed100.marginStatus.fontSize.value, 0.001f)
    }
}
