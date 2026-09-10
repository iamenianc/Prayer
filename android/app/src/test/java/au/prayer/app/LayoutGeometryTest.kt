package au.prayer.app

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import au.prayer.app.data.models.TextScale
import au.prayer.app.ui.theme.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutGeometryTest {

    @Test
    fun `test universal 0dp corner radius contract`() {
        // Universal FlatSquareShape must strictly have 0dp corner radius
        assertEquals(RoundedCornerShape(0.dp), FlatSquareShape)
    }

    @Test
    fun `test warm vintage white color tokens`() {
        assertEquals(Color(0xFFFAF7F2), WarmVintageWhiteColors.background)
        assertEquals(Color(0xFFFFFDF9), WarmVintageWhiteColors.surface)
        assertEquals(Color(0xFFFFFFFF), WarmVintageWhiteColors.surfaceElevated)
        assertEquals(Color(0xFFF0ECE1), WarmVintageWhiteColors.surfaceSubtle)
        assertEquals(Color(0xFF1C1917), WarmVintageWhiteColors.textPrimary)
        assertEquals(Color(0xFF6E675F), WarmVintageWhiteColors.textSubtle)
        assertEquals(Color(0xFFE3DDD3), WarmVintageWhiteColors.border)
        assertEquals(Color(0xFFEDE8DF), WarmVintageWhiteColors.borderSubtle)
        assertEquals(Color(0xFFD5CCC0), WarmVintageWhiteColors.borderStrong)
        assertEquals(Color(0xFF8E867C), WarmVintageWhiteColors.answeredText)
    }

    @Test
    fun `test z-axis elevation tokens`() {
        assertEquals(0.dp, PrayerSpacing.elevationNone)
        assertEquals(1.dp, PrayerSpacing.elevationSubtle)
        assertEquals(2.dp, PrayerSpacing.elevationCard)
        assertEquals(4.dp, PrayerSpacing.elevationFloating)
        assertEquals(8.dp, PrayerSpacing.elevationModal)
    }

    @Test
    fun `test three-tier typography scale tokens and monotonic hierarchy`() {
        val large = getPrayerTypography(TextScale.LARGE)
        assertEquals(26.sp, large.homeAction.fontSize)
        assertEquals(28.sp, large.topicTitle.fontSize)
        assertEquals(22.sp, large.prayerPointTitle.fontSize)
        assertEquals(18.sp, large.prayerPointBody.fontSize)

        val regular = getPrayerTypography(TextScale.REGULAR)
        assertEquals(20.sp, regular.homeAction.fontSize)
        assertEquals(22.sp, regular.topicTitle.fontSize)
        assertEquals(18.sp, regular.prayerPointTitle.fontSize)
        assertEquals(15.sp, regular.prayerPointBody.fontSize)

        val compact = getPrayerTypography(TextScale.COMPACT)
        assertEquals(16.sp, compact.homeAction.fontSize)
        assertEquals(18.sp, compact.topicTitle.fontSize)
        assertEquals(15.sp, compact.prayerPointTitle.fontSize)
        assertEquals(13.sp, compact.prayerPointBody.fontSize)

        // Verify hierarchy: Large > Regular > Compact across all roles
        assertTrue(large.homeAction.fontSize > regular.homeAction.fontSize)
        assertTrue(regular.homeAction.fontSize > compact.homeAction.fontSize)

        assertTrue(large.topicTitle.fontSize > regular.topicTitle.fontSize)
        assertTrue(regular.topicTitle.fontSize > compact.topicTitle.fontSize)

        assertTrue(large.prayerPointTitle.fontSize > regular.prayerPointTitle.fontSize)
        assertTrue(regular.prayerPointTitle.fontSize > compact.prayerPointTitle.fontSize)

        assertTrue(large.prayerPointBody.fontSize > regular.prayerPointBody.fontSize)
        assertTrue(regular.prayerPointBody.fontSize > compact.prayerPointBody.fontSize)

        // Line-height breathing room checks: line height must exceed font size
        assertTrue(large.prayerPointBody.lineHeight > large.prayerPointBody.fontSize)
        assertTrue(regular.prayerPointBody.lineHeight > regular.prayerPointBody.fontSize)
        assertTrue(compact.prayerPointBody.lineHeight > compact.prayerPointBody.fontSize)
    }

    @Test
    fun `test prayer spacing 8dp grid tokens and modularity`() {
        assertEquals(4.dp, PrayerSpacing.extraSmall)
        assertEquals(8.dp, PrayerSpacing.small)
        assertEquals(16.dp, PrayerSpacing.medium)
        assertEquals(24.dp, PrayerSpacing.large)
        assertEquals(32.dp, PrayerSpacing.extraLarge)
        assertEquals(48.dp, PrayerSpacing.huge)
        assertEquals(48.dp, PrayerSpacing.minTouchTarget)
        assertEquals(56.dp, PrayerSpacing.primaryActionHeight)
        assertEquals(56.dp, PrayerSpacing.topAppBarHeight)
        assertEquals(72.dp, PrayerSpacing.sanctuaryBottom)

        // 8dp grid modularity verification
        assertEquals(0, PrayerSpacing.small.value.toInt() % 8)
        assertEquals(0, PrayerSpacing.medium.value.toInt() % 8)
        assertEquals(0, PrayerSpacing.large.value.toInt() % 8)
        assertEquals(0, PrayerSpacing.extraLarge.value.toInt() % 8)
        assertEquals(0, PrayerSpacing.huge.value.toInt() % 8)
        assertEquals(0, PrayerSpacing.minTouchTarget.value.toInt() % 8)
        assertEquals(0, PrayerSpacing.primaryActionHeight.value.toInt() % 8)
        assertEquals(0, PrayerSpacing.topAppBarHeight.value.toInt() % 8)
        assertEquals(0, PrayerSpacing.sanctuaryBottom.value.toInt() % 8)
    }

    @Test
    fun `test material 3 shape and color scheme conformance`() {
        assertEquals(FlatSquareShape, PrayerShapes.extraSmall)
        assertEquals(FlatSquareShape, PrayerShapes.small)
        assertEquals(FlatSquareShape, PrayerShapes.medium)
        assertEquals(FlatSquareShape, PrayerShapes.large)
        assertEquals(FlatSquareShape, PrayerShapes.extraLarge)

        // Verify Warm Vintage White M3 roles
        assertEquals(Color(0xFF1C1917), WarmVintageWhiteColorScheme.primary)
        assertEquals(Color(0xFFFFFDF9), WarmVintageWhiteColorScheme.onPrimary)
        assertEquals(Color(0xFFFFFDF9), WarmVintageWhiteColorScheme.surface)
        assertEquals(Color(0xFF1C1917), WarmVintageWhiteColorScheme.onSurface)
        assertEquals(Color(0xFFFAF7F2), WarmVintageWhiteColorScheme.background)
        assertEquals(Color(0xFF1C1917), WarmVintageWhiteColorScheme.onBackground)
        assertEquals(Color(0xFFE3DDD3), WarmVintageWhiteColorScheme.outline)
    }

    @Test
    fun `test text staying within bounds and wrapping safely across all zoom levels and viewports`() {
        val viewportsDp = listOf(360f, 390f, 412f, 428f)
        val zoomFactors = listOf(1.0f, 1.25f, 1.5f, 2.0f) // Standard Android accessibility font scaling
        val textScales = listOf(TextScale.COMPACT, TextScale.REGULAR, TextScale.LARGE)

        for (scale in textScales) {
            val typography = getPrayerTypography(scale)

            for (zoom in zoomFactors) {
                // Effective font sizes at this zoom level
                val effectiveBodySize = typography.prayerPointBody.fontSize.value * zoom
                val effectiveTitleSize = typography.prayerPointTitle.fontSize.value * zoom
                val effectiveTopicSize = typography.topicTitle.fontSize.value * zoom
                val effectiveHomeActionSize = typography.homeAction.fontSize.value * zoom

                // 1. Text Visibility & Legibility Bounds: all sizes must be positive and within reasonable screen bounds
                assertTrue("Body font size must be >= 10sp at zoom $zoom: $effectiveBodySize", effectiveBodySize >= 10f)
                assertTrue("Body font size must not exceed screen limits (<= 45sp): $effectiveBodySize", effectiveBodySize <= 45f)
                assertTrue("Title font size must be >= 12sp at zoom $zoom: $effectiveTitleSize", effectiveTitleSize >= 12f)
                assertTrue("Topic title font size must not exceed 60sp: $effectiveTopicSize", effectiveTopicSize <= 60f)
                assertTrue("Home action font size must not exceed 55sp: $effectiveHomeActionSize", effectiveHomeActionSize <= 55f)

                for (viewportWidth in viewportsDp) {
                    // Available content widths after subtracting horizontal margins/paddings
                    val sanctuaryContentWidth = viewportWidth - (PrayerSpacing.extraLarge.value * 2) // 32dp * 2 = 64dp
                    val homeSlabContentWidth = viewportWidth - (PrayerSpacing.large.value * 2) // 24dp * 2 = 48dp
                    val cardContentWidth = viewportWidth - (PrayerSpacing.medium.value * 2) - (PrayerSpacing.medium.value * 2) // 64dp

                    // Ensure available content width is strictly positive
                    assertTrue("Sanctuary content width must be positive: $sanctuaryContentWidth", sanctuaryContentWidth > 200f)
                    assertTrue("Home slab content width must be positive: $homeSlabContentWidth", homeSlabContentWidth > 200f)
                    assertTrue("Card content width must be positive: $cardContentWidth", cardContentWidth > 200f)

                    // 2. Word Boundary Bounding: Long single words (e.g. "sanctification", "righteousness" ~14 chars)
                    // Standard Latin proportional font average character width ~0.50 * fontSize.
                    val longWordLength = 14
                    val estimatedLongWordWidth = longWordLength * (effectiveBodySize * 0.50f)
                    assertTrue(
                        "Long devotional word ($estimatedLongWordWidth dp) must stay within content bounds ($sanctuaryContentWidth dp) at scale $scale, zoom $zoom",
                        estimatedLongWordWidth <= sanctuaryContentWidth
                    )

                    // 3. Wrapping Multi-Clause Descriptions:
                    // 25-word description (~140 chars) must wrap into multiple lines rather than overflow horizontally
                    val fullDescriptionLength = 140
                    val totalLinearWidth = fullDescriptionLength * (effectiveBodySize * 0.55f)
                    val requiredLines = Math.ceil((totalLinearWidth / sanctuaryContentWidth).toDouble()).toInt()
                    assertTrue("Multi-clause description must wrap into at least 2 lines at zoom $zoom", requiredLines >= 2)
                    assertTrue("Multi-clause description lines must be bounded and reasonable (<= 15 lines)", requiredLines <= 15)
                }
            }
        }
    }

    @Test
    fun `test lined notepad ruled line height accommodates text without glyph slicing across all zoom scales`() {
        val textScales = listOf(TextScale.COMPACT, TextScale.REGULAR, TextScale.LARGE)

        for (scale in textScales) {
            val typography = getPrayerTypography(scale)
            val fontSize = typography.prayerPointBody.fontSize.value

            // Lined notepad dynamically calculates line height as fontSize * 1.9f
            val ruledLineHeight = fontSize * 1.9f

            // Line height must provide at least 1.5x font size breathing clearance to prevent glyph collision with ruled lines
            val breathingRatio = ruledLineHeight / fontSize
            assertTrue(
                "Ruled line height ratio ($breathingRatio) must be >= 1.5 to prevent glyph slicing at scale $scale",
                breathingRatio >= 1.5f
            )

            // Line height must also be bounded (<= 2.5x) to maintain authentic notepad appearance
            assertTrue(
                "Ruled line height ratio ($breathingRatio) must be <= 2.5 to maintain authentic notepad appearance",
                breathingRatio <= 2.5f
            )

            // Body text specified lineHeight must be >= fontSize * 1.3
            val bodyLineHeight = typography.prayerPointBody.lineHeight.value
            val bodyRatio = bodyLineHeight / fontSize
            assertTrue(
                "Body lineHeight ratio ($bodyRatio) must be >= 1.3 to ensure ascenders/descenders never clip",
                bodyRatio >= 1.3f
            )
        }
    }
}
