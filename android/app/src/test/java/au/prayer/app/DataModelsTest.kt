package au.prayer.app

import au.prayer.app.data.models.*
import org.junit.Assert.*
import org.junit.Test

class DataModelsTest {

    @Test
    fun `test IndividualEntity default values and immutability`() {
        val entity = IndividualEntity(
            rootCode = RootCode.PEOPLE,
            displayName = "Timothy"
        )

        assertNotNull(entity.id)
        assertTrue(entity.id.isNotBlank())
        assertEquals(RootCode.PEOPLE, entity.rootCode)
        assertEquals("Timothy", entity.displayName)
        assertEquals("", entity.contextDescription)
        assertFalse(entity.isPreloadedHistoric)
        assertEquals(0, entity.interactedCount)
        assertNull(entity.lastInteractedAt)
        assertTrue(entity.createdAt > 0)

        // Test copy immutability
        val updated = entity.copy(
            contextDescription = "Colleague at work",
            interactedCount = 1,
            lastInteractedAt = 123456789L
        )
        assertEquals(entity.id, updated.id)
        assertEquals("Colleague at work", updated.contextDescription)
        assertEquals(1, updated.interactedCount)
        assertEquals(123456789L, updated.lastInteractedAt)
    }

    @Test
    fun `test PrayerPoint default values and lifecycle fields`() {
        val point = PrayerPoint(
            entityId = "entity-123",
            title = "Patience in Suffering",
            description = "• Enduring trial with quiet trust in God"
        )

        assertNotNull(point.id)
        assertEquals("entity-123", point.entityId)
        assertEquals("Patience in Suffering", point.title)
        assertEquals("• Enduring trial with quiet trust in God", point.description)
        assertEquals(PrayerStatus.ACTIVE, point.status)
        assertEquals(0, point.interactedCount)
        assertTrue(point.createdAt > 0)
        assertNull(point.lastInteractedAt)
        assertNull(point.answeredAt)
        assertNull(point.answeredTestimony)

        // Answered state mutation
        val answeredPoint = point.copy(
            status = PrayerStatus.ANSWERED,
            answeredAt = System.currentTimeMillis(),
            answeredTestimony = "The Lord provided peace beyond understanding"
        )
        assertEquals(PrayerStatus.ANSWERED, answeredPoint.status)
        assertNotNull(answeredPoint.answeredAt)
        assertEquals("The Lord provided peace beyond understanding", answeredPoint.answeredTestimony)
    }

    @Test
    fun `test TopicWithPoints composition`() {
        val entity = IndividualEntity(rootCode = RootCode.PEOPLE, displayName = "Hannah")
        val activePoint = PrayerPoint(entityId = entity.id, title = "Active Title", description = "Active Desc", status = PrayerStatus.ACTIVE)
        val answeredPoint = PrayerPoint(entityId = entity.id, title = "Answered Title", description = "Answered Desc", status = PrayerStatus.ANSWERED)

        val topic = TopicWithPoints(
            entity = entity,
            activePoints = listOf(activePoint),
            answeredPoints = listOf(answeredPoint)
        )

        assertEquals("Hannah", topic.entity.displayName)
        assertEquals(1, topic.activePoints.size)
        assertEquals(1, topic.answeredPoints.size)
        assertEquals("Active Title", topic.activePoints[0].title)
        assertEquals("Answered Title", topic.answeredPoints[0].title)
    }

    @Test
    fun `test AppConfig defaults and preferences`() {
        val defaultConfig = AppConfig()

        assertEquals(LocaleDialect.EN_AU_UK, defaultConfig.localeDialect)
        assertEquals(ThemeMode.WARM_VINTAGE_WHITE, defaultConfig.themeMode)
        assertEquals(TextScale.LARGE, defaultConfig.textScale)
        assertFalse(defaultConfig.blendHistoricPrayers)
        assertFalse(defaultConfig.highContrastMode)

        val customConfig = defaultConfig.copy(
            localeDialect = LocaleDialect.EN_US,
            themeMode = ThemeMode.WARM_VINTAGE_WHITE,
            textScale = TextScale.COMPACT,
            blendHistoricPrayers = true,
            highContrastMode = true
        )

        assertEquals(LocaleDialect.EN_US, customConfig.localeDialect)
        assertEquals(ThemeMode.WARM_VINTAGE_WHITE, customConfig.themeMode)
        assertEquals(TextScale.COMPACT, customConfig.textScale)
        assertTrue(customConfig.blendHistoricPrayers)
        assertTrue(customConfig.highContrastMode)
    }

    @Test
    fun `test RootCode enum values and sort order`() {
        val roots = RootCode.values()
        assertEquals(5, roots.size)

        assertEquals(RootCode.PEOPLE, roots[0])
        assertEquals("People", RootCode.PEOPLE.displayTitle)
        assertEquals(1, RootCode.PEOPLE.sortOrder)

        assertEquals(RootCode.GROUPS, roots[1])
        assertEquals("Groups", RootCode.GROUPS.displayTitle)
        assertEquals(2, RootCode.GROUPS.sortOrder)

        assertEquals(RootCode.MISSION_PARTNERS, roots[2])
        assertEquals("Mission Partners", RootCode.MISSION_PARTNERS.displayTitle)
        assertEquals(3, RootCode.MISSION_PARTNERS.sortOrder)

        assertEquals(RootCode.GENERAL, roots[3])
        assertEquals("General", RootCode.GENERAL.displayTitle)
        assertEquals(4, RootCode.GENERAL.sortOrder)

        assertEquals(RootCode.HISTORIC, roots[4])
        assertEquals("Historic", RootCode.HISTORIC.displayTitle)
        assertEquals(5, RootCode.HISTORIC.sortOrder)
    }

    @Test
    fun `test PrayerStatus enum states`() {
        val statuses = PrayerStatus.values()
        assertEquals(4, statuses.size)
        assertTrue(statuses.contains(PrayerStatus.ACTIVE))
        assertTrue(statuses.contains(PrayerStatus.ANSWERED))
        assertTrue(statuses.contains(PrayerStatus.ARCHIVED))
        assertTrue(statuses.contains(PrayerStatus.HISTORIC))
    }

    @Test
    fun `test ThemeMode and TextScale display names`() {
        assertEquals("Warm Vintage White", ThemeMode.WARM_VINTAGE_WHITE.displayName)
        assertEquals("Saddle Tan", ThemeMode.SADDLE_TAN.displayName)
        assertEquals("Horween Cordovan", ThemeMode.HORWEEN_CORDOVAN.displayName)
        assertEquals("Hunter Forest", ThemeMode.HUNTER_FOREST.displayName)
        assertEquals("Obsidian Hide", ThemeMode.OBSIDIAN_HIDE.displayName)

        assertEquals("Large", TextScale.LARGE.displayName)
        assertEquals("Regular", TextScale.REGULAR.displayName)
        assertEquals("Compact", TextScale.COMPACT.displayName)
    }

    @Test
    fun `test IndividualEntity isPinned field and toggle`() {
        val entity = IndividualEntity(
            rootCode = RootCode.PEOPLE,
            displayName = "C.S. Lewis",
            isPinned = false
        )
        assertFalse(entity.isPinned)

        val pinned = entity.copy(isPinned = true)
        assertTrue(pinned.isPinned)
    }

    @Test
    fun `test ReadingProgress model defaults and updates`() {
        val progress = ReadingProgress(
            volumeId = "calvin-institutes-prayer",
            lastSectionNumber = 29,
            lastScrollOffset = 120
        )

        assertEquals("calvin-institutes-prayer", progress.volumeId)
        assertEquals(29, progress.lastSectionNumber)
        assertEquals(120, progress.lastScrollOffset)
        assertTrue(progress.updatedAt > 0)

        val updated = progress.copy(lastSectionNumber = 50, lastScrollOffset = 0)
        assertEquals(50, updated.lastSectionNumber)
        assertEquals(0, updated.lastScrollOffset)
    }
}
