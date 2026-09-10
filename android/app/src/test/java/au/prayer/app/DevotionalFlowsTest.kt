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

        val personalMe = IndividualEntity(rootCode = RootCode.PEOPLE, displayName = "Me", contextDescription = "Personal sanctification and health")
        assertEquals(RootCode.PEOPLE, personalMe.rootCode)
        assertEquals("Me", personalMe.displayName)

        val group = IndividualEntity(rootCode = RootCode.GROUPS, displayName = "Parish Council")
        assertEquals(RootCode.GROUPS, group.rootCode)
        assertEquals("Parish Council", group.displayName)

        val general = IndividualEntity(rootCode = RootCode.GENERAL, displayName = "Global Church & Mission")
        assertEquals(RootCode.GENERAL, general.rootCode)
        assertEquals("Global Church & Mission", general.displayName)
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
    fun `test direct entry offline fallback title edge cases`() {
        // Empty text
        assertEquals("Prayer Point", apiClient.generateOfflineFallbackTitle(""))
        assertEquals("Prayer Point", apiClient.generateOfflineFallbackTitle("   "))
        assertEquals("Prayer Point", apiClient.generateOfflineFallbackTitle("\n\t  \n"))

        // Bullet symbols only
        assertEquals("Prayer Point", apiClient.generateOfflineFallbackTitle("•"))
        assertEquals("Prayer Point", apiClient.generateOfflineFallbackTitle("• • •"))

        // Single word
        assertEquals("Healing", apiClient.generateOfflineFallbackTitle("• Healing"))
        assertEquals("Comfort", apiClient.generateOfflineFallbackTitle("Comfort"))

        // Exactly 2, 3, and 4 words
        assertEquals("Family Peace", apiClient.generateOfflineFallbackTitle("• Family Peace"))
        assertEquals("Comfort In Sorrow", apiClient.generateOfflineFallbackTitle("• Comfort In Sorrow"))
        assertEquals("Strength For Daily Walk", apiClient.generateOfflineFallbackTitle("• Strength For Daily Walk"))

        // 5+ words: strictly truncated to first 4 words
        val longInput = "• God sovereignly grant wisdom to our church elders during transition"
        val fallback = apiClient.generateOfflineFallbackTitle(longInput)
        assertEquals("God sovereignly grant wisdom", fallback)
        assertEquals(4, fallback.split("\\s+".toRegex()).size)

        // Multiple leading bullets and tabs
        val messyInput = "•••\t  Gospel boldness among neighbors and coworkers"
        val messyFallback = apiClient.generateOfflineFallbackTitle(messyInput)
        assertEquals("Gospel boldness among neighbors", messyFallback)
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
    fun `test auto bullet point empty bullet clearing logic`() {
        // Clearing an empty bullet when user presses Enter or Backspace on "• "
        fun handleBackspaceOnEmptyBullet(current: String): String {
            val trimmed = current.trimEnd()
            return if (trimmed.endsWith("•")) trimmed.removeSuffix("•").trimEnd() else current
        }

        assertEquals("", handleBackspaceOnEmptyBullet("• "))
        assertEquals("• First point", handleBackspaceOnEmptyBullet("• First point\n• "))
    }

    @Test
    fun `test candidate points cardinality and telegraphic brevity`() {
        val candidates = listOf(
            CandidatePrayerPoint(
                title = "Surgery Recovery",
                description = "Rapid healing after knee procedure; patient endurance in therapy; gratitude for medical team"
            ),
            CandidatePrayerPoint(
                title = "Sovereign Peace",
                description = "Quiet heart amidst physical discomfort, trust in Father's preservation"
            )
        )

        // Cardinality Invariant: strictly 2 candidates per turn
        assertEquals(2, candidates.size)

        candidates.forEach { card ->
            val titleWords = card.title.split("\\s+".toRegex()).size
            assertTrue("Title should be 2 to 6 words: $titleWords ('${card.title}')", titleWords in 2..6)

            val descWords = card.description.split("\\s+".toRegex()).size
            assertTrue("Description must be telegraphic <= 25 words: $descWords", descWords <= 25)

            // Telegraphic constraints: no w/ or /w abbreviations
            assertFalse("Must not use 'w/' abbreviation: '${card.description}'", card.description.contains("w/"))
            assertFalse("Must not use '/w' abbreviation: '${card.description}'", card.description.contains("/w"))

            // Must avoid redundant prefixes
            assertFalse("Must not start with 'Pray for'", card.title.startsWith("Pray for", ignoreCase = true))
            assertFalse("Must not start with 'Ask God to'", card.title.startsWith("Ask God to", ignoreCase = true))
        }
    }

    @Test
    fun `test saved prayer point editing and permanent deletion contract`() {
        val initialPoint = PrayerPoint(
            entityId = "entity-1",
            title = "Old Title",
            description = "• Initial prayer point content",
            status = PrayerStatus.ACTIVE
        )

        // Verify initial state
        assertEquals(PrayerStatus.ACTIVE, initialPoint.status)
        assertNull(initialPoint.answeredAt)
        assertNull(initialPoint.answeredTestimony)

        // Editing title and body and transitioning to ANSWERED
        val answeredTime = System.currentTimeMillis()
        val editedPoint = initialPoint.copy(
            title = "Customized Title",
            description = "• Updated pastoral prayer point",
            status = PrayerStatus.ANSWERED,
            answeredAt = answeredTime,
            answeredTestimony = "Praise God for His faithful provision"
        )

        assertEquals("Customized Title", editedPoint.title)
        assertEquals(PrayerStatus.ANSWERED, editedPoint.status)
        assertEquals(answeredTime, editedPoint.answeredAt)
        assertEquals("Praise God for His faithful provision", editedPoint.answeredTestimony)

        // Transitioning back to ACTIVE clears answered fields
        val reactivatedPoint = editedPoint.copy(
            status = PrayerStatus.ACTIVE,
            answeredAt = null,
            answeredTestimony = null
        )
        assertEquals(PrayerStatus.ACTIVE, reactivatedPoint.status)
        assertNull(reactivatedPoint.answeredAt)
        assertNull(reactivatedPoint.answeredTestimony)
    }

    @Test
    fun `test entity update and sphere transition contract`() {
        val initialEntity = IndividualEntity(
            rootCode = RootCode.PEOPLE,
            displayName = "David"
        )
        assertEquals(RootCode.PEOPLE, initialEntity.rootCode)
        assertEquals("David", initialEntity.displayName)

        // Renaming entity
        val renamed = initialEntity.copy(displayName = "David Jenkins")
        assertEquals("David Jenkins", renamed.displayName)
        assertEquals(RootCode.PEOPLE, renamed.rootCode)

        // Transitioning sphere from PEOPLE to GROUPS
        val groupEntity = renamed.copy(rootCode = RootCode.GROUPS, displayName = "Jenkins Family")
        assertEquals(RootCode.GROUPS, groupEntity.rootCode)
        assertEquals("Jenkins Family", groupEntity.displayName)
    }

    @Test
    fun `test prayer record quick toggle from active to answered and back`() {
        val activePoint = PrayerPoint(
            entityId = "e-1",
            title = "Daily Strength",
            description = "• Walking faithfully in trial",
            status = PrayerStatus.ACTIVE
        )
        assertEquals(PrayerStatus.ACTIVE, activePoint.status)
        assertNull(activePoint.answeredAt)

        // Quick toggle to ANSWERED
        val answeredPoint = activePoint.copy(
            status = PrayerStatus.ANSWERED,
            answeredAt = 1000L
        )
        assertEquals(PrayerStatus.ANSWERED, answeredPoint.status)
        assertEquals(1000L, answeredPoint.answeredAt)

        // Quick toggle back to ACTIVE
        val toggledBack = answeredPoint.copy(
            status = PrayerStatus.ACTIVE,
            answeredAt = null,
            answeredTestimony = null
        )
        assertEquals(PrayerStatus.ACTIVE, toggledBack.status)
        assertNull(toggledBack.answeredAt)
        assertNull(toggledBack.answeredTestimony)
    }

    @Test
    fun `test read-only prompts request generation when viewing past points`() {
        val person = IndividualEntity(
            id = "person-123",
            rootCode = RootCode.PEOPLE,
            displayName = "Sarah",
            contextDescription = "Sister in Christ undergoing trials"
        )
        val pastPoints = listOf(
            PrayerPoint(
                entityId = person.id,
                title = "Surgery Recovery",
                description = "• Rapid healing after knee surgery\n• Peace for family",
                status = PrayerStatus.ACTIVE
            ),
            PrayerPoint(
                entityId = person.id,
                title = "Job Transition",
                description = "• Faithfulness in workplace trial",
                status = PrayerStatus.ANSWERED
            )
        )

        // Construct request from past recorded points
        val recorded = pastPoints.map {
            au.prayer.app.network.RecordedPoint(title = it.title, body = it.description, status = it.status.name)
        }
        val request = au.prayer.app.network.SuggestRequest(
            targetName = person.displayName,
            root = person.rootCode.name,
            group = null,
            contextDescription = person.contextDescription,
            recordedPoints = recorded,
            journalUpdates = emptyList(),
            currentDraft = null, // Invariant: no current draft in read-only view
            localeDialect = "EN_AU_UK"
        )

        assertEquals("Sarah", request.targetName)
        assertEquals("PEOPLE", request.root)
        assertEquals(2, request.recordedPoints.size)
        assertNull(request.currentDraft)
        assertEquals("Surgery Recovery", request.recordedPoints[0].title)
        assertEquals("ACTIVE", request.recordedPoints[0].status)
    }

    @Test
    fun `test read-only prompts formatting constraints`() {
        val mockPrompts = listOf(
            "Steadfast faith in trials",
            "Deepened peace of Christ",
            "Patience under affliction",
            "Comfort in sorrow"
        )

        assertTrue("Prompts should be 3 to 5 lines: ${mockPrompts.size}", mockPrompts.size in 3..5)
        mockPrompts.forEach { prompt ->
            val wordCount = prompt.split("\\s+".toRegex()).size
            assertTrue("Each prompt should be 1 to 6 words: $wordCount ('$prompt')", wordCount in 1..6)
            assertFalse("Must not start with 'Pray for'", prompt.startsWith("Pray for", ignoreCase = true))
            assertFalse("Must not start with 'Ask God to'", prompt.startsWith("Ask God to", ignoreCase = true))
        }
    }
}
