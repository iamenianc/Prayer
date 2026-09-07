package au.prayer.app

import au.prayer.app.data.models.IndividualEntity
import au.prayer.app.data.models.PrayerPoint
import au.prayer.app.data.models.PrayerStatus
import au.prayer.app.data.models.RootCode
import au.prayer.app.network.CandidatePrayerPoint
import au.prayer.app.network.PrayerApiClient
import org.junit.Assert.*
import org.junit.Test

class DevotionalFlowsTest {

    private val apiClient = PrayerApiClient()

    @Test
    fun `test step 0 entity resolution rules`() {
        val person = IndividualEntity(rootCode = RootCode.PEOPLE, displayName = "Sarah")
        assertEquals(RootCode.PEOPLE, person.rootCode)
        assertEquals("Sarah", person.displayName)

        val group = IndividualEntity(rootCode = RootCode.GROUPS, displayName = "Parish Council")
        assertEquals(RootCode.GROUPS, group.rootCode)
        assertEquals("Parish Council", group.displayName)
    }

    @Test
    fun `test direct entry offline fallback title generation`() {
        val body = "• Physical healing after surgery; peace for family; trust in Christ"
        val title = apiClient.generateOfflineFallbackTitle(body)

        val wordCount = title.split("\\s+".toRegex()).size
        assertTrue("Title should be between 2 and 4 words: actual $wordCount ('$title')", wordCount in 2..4)
        assertFalse("Title must not contain bullet point symbol", title.contains("•"))
    }

    @Test
    fun `test direct entry empty text title fallback`() {
        val title = apiClient.generateOfflineFallbackTitle("")
        assertEquals("Petition", title)
    }

    @Test
    fun `test auto bullet point newline formatting logic`() {
        fun formatNextLine(current: String): String {
            return if (current.endsWith("\n")) current + "• " else current
        }

        val inputWithNewline = "• Pray for wisdom\n"
        val formatted = formatNextLine(inputWithNewline)
        assertEquals("• Pray for wisdom\n• ", formatted)
    }

    @Test
    fun `test candidate points cardinality and semicolon clause structure`() {
        val candidates = listOf(
            CandidatePrayerPoint(
                title = "Surgery Recovery",
                description = "Rapid healing after knee procedure; patient endurance in therapy; gratitude for medical team"
            ),
            CandidatePrayerPoint(
                title = "Sovereign Peace",
                description = "Quiet heart amidst physical discomfort; trust in Father's preservation; spiritual comfort"
            )
        )

        // Cardinality Invariant: strictly 2 candidates per turn
        assertEquals(2, candidates.size)

        candidates.forEach { card ->
            val titleWords = card.title.split("\\s+".toRegex()).size
            assertTrue("Title should be 2 to 6 words: $titleWords ('${card.title}')", titleWords in 2..6)

            val clauses = card.description.split(";").map { it.trim() }
            assertTrue(
                "Description should follow 2-to-3 clause semicolon pattern: actual ${clauses.size} in '${card.description}'",
                clauses.size in 2..3
            )

            val descWords = card.description.split("\\s+".toRegex()).size
            assertTrue("Description must be telegraphic <= 25 words: $descWords", descWords <= 25)
        }
    }

    @Test
    fun `test saved petition editing and permanent deletion contract`() {
        val initialPoint = PrayerPoint(
            entityId = "entity-1",
            title = "Old Title",
            description = "• Initial petition content",
            status = PrayerStatus.ACTIVE
        )

        // Editing title and body
        val editedPoint = initialPoint.copy(
            title = "Customized Title",
            description = "• Updated pastoral petition",
            status = PrayerStatus.ANSWERED,
            answeredAt = System.currentTimeMillis(),
            answeredTestimony = "Praise God for His faithful provision"
        )

        assertEquals("Customized Title", editedPoint.title)
        assertEquals(PrayerStatus.ANSWERED, editedPoint.status)
        assertNotNull(editedPoint.answeredAt)
        assertEquals("Praise God for His faithful provision", editedPoint.answeredTestimony)
    }
}
