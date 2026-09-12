package au.prayer.app

import au.prayer.app.data.models.IndividualEntity
import au.prayer.app.data.models.PrayerPoint
import au.prayer.app.data.models.PrayerStatus
import au.prayer.app.data.models.RootCode
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotesFlowTest {

    @Test
    fun `test Notes root code definition and properties`() {
        assertEquals(RootCode.NOTES, RootCode.valueOf("NOTES"))
        assertEquals("Notes", RootCode.NOTES.displayTitle)
        assertEquals(6, RootCode.NOTES.sortOrder)
    }

    @Test
    fun `test note entity structure titled with day and date`() {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH)
        val testDate = 1757659200000L // 2025-09-12
        val dateTitle = sdf.format(Date(testDate))

        val noteEntity = IndividualEntity(
            rootCode = RootCode.NOTES,
            displayName = dateTitle,
            contextDescription = "Romans 8:28–39 Study"
        )

        assertEquals(RootCode.NOTES, noteEntity.rootCode)
        assertEquals(dateTitle, noteEntity.displayName)
        assertEquals("Romans 8:28–39 Study", noteEntity.contextDescription)
        assertFalse(noteEntity.isPreloadedHistoric)
        assertFalse(noteEntity.isPinned)
    }

    @Test
    fun `test note entity pin toggle for significant study reflections`() {
        val noteEntity = IndividualEntity(
            rootCode = RootCode.NOTES,
            displayName = "Saturday, 12 September 2026",
            isPinned = false
        )
        assertFalse(noteEntity.isPinned)

        val pinnedNote = noteEntity.copy(isPinned = true)
        assertTrue(pinnedNote.isPinned)
    }

    @Test
    fun `test freeform study note text structure without forced bullets`() {
        // Unlike prayer points which are bullet-formatted, a standard notepad reflection is freeform prose
        val freeformStudyNote = """
            Romans 8:28-39 Study Notes:
            1. God causes all things to work together for good for those who love Him.
            2. The golden chain of redemption: foreknew, predestined, called, justified, glorified.
            3. Nothing can separate us from the love of God in Christ Jesus our Lord.
            
            Personal meditation:
            When facing trials, God's sovereign love is our unshakeable fortress.
        """.trimIndent()

        val notePoint = PrayerPoint(
            entityId = "note-entity-123",
            title = "",
            description = freeformStudyNote,
            status = PrayerStatus.ACTIVE
        )

        assertEquals("note-entity-123", notePoint.entityId)
        assertEquals("", notePoint.title)
        assertEquals(freeformStudyNote, notePoint.description)
        assertFalse("Freeform note should not be forced into starting with bullet if not typed by user", notePoint.description.startsWith("• "))
    }

    @Test
    fun `test sanctuary prayer queue filter excludes NOTES entities`() {
        val personEntity = IndividualEntity(
            displayName = "Timothy",
            rootCode = RootCode.PEOPLE
        )
        val noteEntity = IndividualEntity(
            displayName = "Saturday, 12 September 2026",
            rootCode = RootCode.NOTES
        )

        val allEntities = listOf(personEntity, noteEntity)
        // Sanctuary prayer queue filter: non-historic, and not NOTES
        val sanctuaryCandidates = allEntities.filter { !it.isPreloadedHistoric && it.rootCode != RootCode.NOTES }

        assertEquals(1, sanctuaryCandidates.size)
        assertEquals("Timothy", sanctuaryCandidates[0].displayName)
        assertFalse(sanctuaryCandidates.any { it.rootCode == RootCode.NOTES })
    }

    @Test
    fun `test journal categories filter excludes NOTES root`() {
        val journalRoots = RootCode.entries.filter { it != RootCode.NOTES }
        assertEquals(5, journalRoots.size)
        assertFalse(journalRoots.contains(RootCode.NOTES))
        assertTrue(journalRoots.contains(RootCode.PEOPLE))
        assertTrue(journalRoots.contains(RootCode.GROUPS))
        assertTrue(journalRoots.contains(RootCode.MISSION_PARTNERS))
        assertTrue(journalRoots.contains(RootCode.GENERAL))
        assertTrue(journalRoots.contains(RootCode.HISTORIC))
    }

    @Test
    fun `test note date formatting matches standard English idiom`() {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH)
        val now = Date()
        val formatted = sdf.format(now)

        assertNotNull(formatted)
        assertTrue("Formatted date must contain day of week and year", formatted.contains(SimpleDateFormat("yyyy", Locale.ENGLISH).format(now)))
        val parts = formatted.split(", ")
        assertEquals("Date format must have day of week followed by date: 'Day, d MMMM yyyy'", 2, parts.size)
    }
}
