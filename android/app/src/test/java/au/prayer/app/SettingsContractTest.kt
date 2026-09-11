package au.prayer.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import au.prayer.app.data.models.AppConfig
import au.prayer.app.data.models.LocaleDialect
import au.prayer.app.data.models.TextScale
import au.prayer.app.data.models.ThemeMode
import au.prayer.app.ui.theme.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit test suite verifying Settings contracts, folio leather tokens,
 * text scale cadence, dialect configuration, and touch target invariants.
 */
class SettingsContractTest {

    @Test
    fun `test default app config values`() {
        val config = AppConfig()
        assertEquals(LocaleDialect.EN_AU_UK, config.localeDialect)
        assertEquals(ThemeMode.WARM_VINTAGE_WHITE, config.themeMode)
        assertEquals(TextScale.LARGE, config.textScale)
        assertFalse(config.blendHistoricPrayers)
        assertFalse(config.highContrastMode)
    }

    @Test
    fun `test app config immutability and copy mutation`() {
        val initial = AppConfig()
        val updated = initial.copy(
            themeMode = ThemeMode.HORWEEN_CORDOVAN,
            textScale = TextScale.REGULAR,
            localeDialect = LocaleDialect.EN_US,
            blendHistoricPrayers = true,
            highContrastMode = true
        )

        assertEquals(ThemeMode.HORWEEN_CORDOVAN, updated.themeMode)
        assertEquals(TextScale.REGULAR, updated.textScale)
        assertEquals(LocaleDialect.EN_US, updated.localeDialect)
        assertTrue(updated.blendHistoricPrayers)
        assertTrue(updated.highContrastMode)

        // Verify original instance remains unchanged
        assertEquals(ThemeMode.WARM_VINTAGE_WHITE, initial.themeMode)
        assertFalse(initial.blendHistoricPrayers)
    }

    @Test
    fun `test four leather finishes resolve to canonical folio dye colors`() {
        val saddle = getFolioColors(ThemeMode.SADDLE_TAN)
        assertEquals(Color(0xFF8C532B), saddle.leatherActive)
        assertEquals(Color(0xFF8C532B), saddle.leatherPrimary)

        val cordovan = getFolioColors(ThemeMode.HORWEEN_CORDOVAN)
        assertEquals(Color(0xFF5E2A2B), cordovan.leatherActive)
        assertEquals(Color(0xFF5E2A2B), cordovan.leatherCordovan)

        val forest = getFolioColors(ThemeMode.HUNTER_FOREST)
        assertEquals(Color(0xFF2D483A), forest.leatherActive)
        assertEquals(Color(0xFF2D483A), forest.leatherForest)

        val obsidian = getFolioColors(ThemeMode.OBSIDIAN_HIDE)
        assertEquals(Color(0xFF35322F), obsidian.leatherActive)
        assertEquals(Color(0xFF35322F), obsidian.leatherObsidian)
    }

    @Test
    fun `test high contrast leather finishes resolve to accessible deep dyes`() {
        val saddleHc = getFolioColors(ThemeMode.SADDLE_TAN, highContrastMode = true)
        assertEquals(Color(0xFF5A2800), saddleHc.leatherActive)

        val cordovanHc = getFolioColors(ThemeMode.HORWEEN_CORDOVAN, highContrastMode = true)
        assertEquals(Color(0xFF3D1A1B), cordovanHc.leatherActive)

        val forestHc = getFolioColors(ThemeMode.HUNTER_FOREST, highContrastMode = true)
        assertEquals(Color(0xFF1A2E24), forestHc.leatherActive)

        val obsidianHc = getFolioColors(ThemeMode.OBSIDIAN_HIDE, highContrastMode = true)
        assertEquals(Color(0xFF35322F), obsidianHc.leatherActive)
    }

    @Test
    fun `test minimum touch target for settings action affordances`() {
        // Universal 48dp minimum target guaranteed
        assertTrue(PrayerSpacing.minTouchTarget >= 48.dp)
        assertTrue(PrayerSpacing.primaryActionHeight >= 48.dp)
        assertEquals(56.dp, PrayerSpacing.primaryActionHeight)
    }

    @Test
    fun `test dialect names and codes align with liturgical standards`() {
        assertEquals("English (Australian / UK)", LocaleDialect.EN_AU_UK.displayName)
        assertEquals("EN_AU_UK", LocaleDialect.EN_AU_UK.code)

        assertEquals("US English", LocaleDialect.EN_US.displayName)
        assertEquals("EN_US", LocaleDialect.EN_US.code)
    }

    @Test
    fun `test settings text scales maintain strict monotonic order`() {
        val large = getPrayerTypography(TextScale.LARGE)
        val regular = getPrayerTypography(TextScale.REGULAR)
        val compact = getPrayerTypography(TextScale.COMPACT)

        assertTrue(large.prayerPointBody.fontSize > regular.prayerPointBody.fontSize)
        assertTrue(regular.prayerPointBody.fontSize > compact.prayerPointBody.fontSize)

        assertEquals(18.sp, large.prayerPointBody.fontSize)
        assertEquals(15.sp, regular.prayerPointBody.fontSize)
        assertEquals(13.sp, compact.prayerPointBody.fontSize)
    }
}
