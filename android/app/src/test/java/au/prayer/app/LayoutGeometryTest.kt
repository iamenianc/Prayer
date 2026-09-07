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
    fun `test morning light color tokens`() {
        assertEquals(Color(0xFFFFFFFF), MorningLightColors.background)
        assertEquals(Color(0xFFFFFFFF), MorningLightColors.surface)
        assertEquals(Color(0xFF111111), MorningLightColors.textPrimary)
        assertEquals(Color(0xFF666666), MorningLightColors.textSubtle)
        assertEquals(Color(0xFFE0E0E0), MorningLightColors.border)
    }

    @Test
    fun `test quiet night color tokens`() {
        assertEquals(Color(0xFF000000), QuietNightColors.background)
        assertEquals(Color(0xFF000000), QuietNightColors.surface)
        assertEquals(Color(0xFFFFFFFF), QuietNightColors.textPrimary)
        assertEquals(Color(0xFF888888), QuietNightColors.textSubtle)
        assertEquals(Color(0xFF333333), QuietNightColors.border)
    }

    @Test
    fun `test three-tier typography scale tokens`() {
        val large = getPrayerTypography(TextScale.LARGE)
        assertEquals(26.sp, large.homeAction.fontSize)
        assertEquals(28.sp, large.topicTitle.fontSize)
        assertEquals(22.sp, large.petitionTitle.fontSize)
        assertEquals(18.sp, large.petitionBody.fontSize)

        val regular = getPrayerTypography(TextScale.REGULAR)
        assertEquals(20.sp, regular.homeAction.fontSize)
        assertEquals(22.sp, regular.topicTitle.fontSize)
        assertEquals(18.sp, regular.petitionTitle.fontSize)
        assertEquals(15.sp, regular.petitionBody.fontSize)

        val compact = getPrayerTypography(TextScale.COMPACT)
        assertEquals(16.sp, compact.homeAction.fontSize)
        assertEquals(18.sp, compact.topicTitle.fontSize)
        assertEquals(15.sp, compact.petitionTitle.fontSize)
        assertEquals(13.sp, compact.petitionBody.fontSize)

        // Verify hierarchy: Large > Regular > Compact
        assertTrue(large.homeAction.fontSize > regular.homeAction.fontSize)
        assertTrue(regular.homeAction.fontSize > compact.homeAction.fontSize)
    }
}
