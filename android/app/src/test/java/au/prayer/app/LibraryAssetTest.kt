package au.prayer.app

import au.prayer.app.data.models.LibraryContent
import au.prayer.app.data.models.LibraryVolume
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LibraryAssetTest {

    private lateinit var volume: LibraryVolume

    @Before
    fun setUp() {
        val json = javaClass.getResourceAsStream("/library_calvin_prayer.json")
            ?.readBytes()?.decodeToString()
            ?: error("library_calvin_prayer.json not found on test classpath")
        volume = LibraryContent.loadFromText(json)
    }

    @Test
    fun `test library volume metadata`() {
        assertEquals("calvin-institutes-prayer", volume.volumeId)
        assertEquals("Of Prayer: A Perpetual Exercise of Faith", volume.title)
        assertEquals("The Daily Benefits Derived from It", volume.subtitle)
        assertEquals("John Calvin", volume.author)
        assertEquals("Institutes of the Christian Religion, Book III, Chapter XX", volume.work)
        assertEquals("Henry Beveridge (1845)", volume.translator)
        assertEquals("en", volume.language)
        assertTrue(volume.publicDomain)
        assertTrue(volume.sourceUrl.contains("ccel.org"))
    }

    @Test
    fun `test library volume divisions`() {
        assertEquals(8, volume.divisions.size)

        // Division I: Sections 1-2
        assertEquals("I", volume.divisions[0].division)
        assertEquals(1, volume.divisions[0].startSection)
        assertEquals(2, volume.divisions[0].endSection)

        // Division VII: Exposition of the Lord's Prayer (Sections 34-50)
        assertEquals("VII", volume.divisions[6].division)
        assertTrue(volume.divisions[6].description.contains("exposition of the Lord's Prayer"))
        assertEquals(34, volume.divisions[6].startSection)
        assertEquals(50, volume.divisions[6].endSection)

        // Division VIII: Rules, perseverance, faith (Sections 51-52)
        assertEquals("VIII", volume.divisions[7].division)
        assertEquals(51, volume.divisions[7].startSection)
        assertEquals(52, volume.divisions[7].endSection)
    }

    @Test
    fun `test library volume sections and paragraph integrity`() {
        assertEquals(52, volume.sections.size)

        var totalParagraphs = 0
        for (i in 0 until 52) {
            val section = volume.sections[i]
            assertEquals(i + 1, section.sectionNumber)
            assertTrue("Section ${i + 1} must have an outline summary", section.outlineSummary.isNotBlank())
            assertTrue("Section ${i + 1} must contain paragraphs", section.paragraphs.isNotEmpty())
            section.paragraphs.forEach { para ->
                assertTrue("Paragraph in section ${i + 1} cannot be blank", para.isNotBlank())
                assertFalse("Paragraph should not retain CCEL footnote anchor brackets like [1]", para.contains(Regex("\\[\\d+\\]")))
            }
            totalParagraphs += section.paragraphs.size
        }

        assertEquals(99, totalParagraphs)
    }

    @Test
    fun `test key theological anchors in Calvin treatise`() {
        // Section 1: Faith and prayer connected; Rom. 8:26 (groanings, Abba Father)
        val sec1 = volume.sections[0]
        assertTrue(sec1.paragraphs[0].contains("Abba, Father (Rom. 8:26)"))
        assertTrue(sec1.paragraphs[0].contains("Rom. 10:14"))

        // Section 29 & Section 50: Prayer without ceasing (1 Thess. 5:17)
        val sec29 = volume.sections[28]
        assertTrue(sec29.outlineSummary.contains("Prayer without ceasing"))
        val sec50 = volume.sections[49]
        assertTrue(sec50.paragraphs[0].contains("pray without ceasing"))

        // Section 52: The dignity of faith and persevering prayer
        val sec52 = volume.sections[51]
        assertTrue(sec52.outlineSummary.contains("dignity of faith"))
        val sec52FullText = sec52.paragraphs.joinToString(" ")
        assertTrue(sec52FullText.contains("God will never abandon us"))
    }
}
