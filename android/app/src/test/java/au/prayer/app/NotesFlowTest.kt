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

    @Test
    fun `test date entity acts as a grouping container for multiple notes`() {
        // Date is not a note; it is a grouping container under which any number of notes can be filed
        val dateGroupEntity = IndividualEntity(
            id = "date-group-2026-09-12",
            rootCode = RootCode.NOTES,
            displayName = "Saturday, 12 September 2026"
        )

        val note1 = PrayerPoint(
            id = "note-1",
            entityId = dateGroupEntity.id,
            title = "Morning Meditation",
            description = "Psalm 23 reflection: The Lord is my shepherd, I shall not want.",
            status = PrayerStatus.ACTIVE
        )

        val note2 = PrayerPoint(
            id = "note-2",
            entityId = dateGroupEntity.id,
            title = "Romans 8 Study",
            description = "Contemplating the golden chain of redemption and God's sovereign providence.",
            status = PrayerStatus.ACTIVE
        )

        val note3 = PrayerPoint(
            id = "note-3",
            entityId = dateGroupEntity.id,
            title = "", // Optional title left blank
            description = "Evening thanksgiving for parish fellowship.",
            status = PrayerStatus.ACTIVE
        )

        val filedNotes = listOf(note1, note2, note3)

        assertEquals("date-group-2026-09-12", dateGroupEntity.id)
        assertEquals(3, filedNotes.size)
        assertTrue(filedNotes.all { it.entityId == dateGroupEntity.id })
        assertEquals("Morning Meditation", filedNotes[0].title)
        assertEquals("Romans 8 Study", filedNotes[1].title)
        assertEquals("", filedNotes[2].title)
    }

    @Test
    fun `test notes under date grouping have optional individual titles`() {
        val dateGroupId = "date-group-1"

        // Note with title
        val titledNote = PrayerPoint(
            entityId = dateGroupId,
            title = "Calvin on Prayer Discussion",
            description = "Key points from Book III, Chapter XX.",
            status = PrayerStatus.ACTIVE
        )
        assertEquals("Calvin on Prayer Discussion", titledNote.title)
        assertFalse(titledNote.title.isBlank())

        // Note without title (optional)
        val untitledNote = PrayerPoint(
            entityId = dateGroupId,
            title = "",
            description = "Quick reflection written without a title.",
            status = PrayerStatus.ACTIVE
        )
        assertEquals("", untitledNote.title)
        assertTrue(untitledNote.title.isBlank())
    }

    @Test
    fun `test multiple notes filed under date grouping can be individually modified and filtered`() {
        val dateGroupId = "date-group-multi"

        var notes = listOf(
            PrayerPoint(id = "n1", entityId = dateGroupId, title = "First Note", description = "Draft 1"),
            PrayerPoint(id = "n2", entityId = dateGroupId, title = "Second Note", description = "Draft 2"),
            PrayerPoint(id = "n3", entityId = dateGroupId, title = "", description = "Untitled Draft 3")
        )

        // Deleting note n2 leaves n1 and n3 intact under the date grouping
        notes = notes.filter { it.id != "n2" }
        assertEquals(2, notes.size)
        assertEquals("n1", notes[0].id)
        assertEquals("n3", notes[1].id)

        // Updating note n1 title and body
        val updatedNote1 = notes[0].copy(title = "Updated First Title", description = "Revised content")
        notes = listOf(updatedNote1, notes[1])
        assertEquals("Updated First Title", notes[0].title)
        assertEquals("Revised content", notes[0].description)
    }
}
