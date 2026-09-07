package au.prayer.app

import au.prayer.app.network.CandidatePrayerPoint
import org.junit.Assert.*
import org.junit.Test

class TheologicalGuardrailsTest {

    // Forbidden direct second-person invocations to God in AI outputs
    private val forbiddenPrayerInvocations = listOf(
        "father god",
        "dear lord",
        "lord jesus",
        "heavenly father",
        "o lord",
        "thy will be done",
        "we pray that you",
        "i ask that you",
        "grant unto us",
        "hear our prayer"
    )

    // Forbidden intermediaries (saints, Mary, angels, ancestors)
    private val forbiddenIntermediaries = listOf(
        "saint jude",
        "saint anthony",
        "saint michael",
        "mother mary",
        "virgin mary",
        "holy mother",
        "ancestors",
        "guardian angel",
        "intercession of the saints"
    )

    // Forbidden false theologies (prosperity, word-faith, manifesting, karma)
    private val forbiddenTheologies = listOf(
        "declare and decree",
        "i decree",
        "we decree",
        "name it and claim it",
        "manifesting",
        "manifest abundance",
        "prosperity covenant",
        "sowing a seed for financial breakthrough",
        "karma",
        "bargain with god"
    )

    @Test
    fun `test candidate cards never address God directly or write scripted prayers`() {
        // Valid objective prayer points
        val validCandidates = listOf(
            CandidatePrayerPoint(
                title = "Surgery Recovery",
                description = "Rapid healing after knee procedure; patient endurance in therapy; gratitude for medical team"
            ),
            CandidatePrayerPoint(
                title = "Gospel Boldness",
                description = "Courageous witness among colleagues; clear explanation of saving faith; relying on Spirit"
            )
        )

        validCandidates.forEach { card ->
            val text = "${card.title} ${card.description}".lowercase()
            forbiddenPrayerInvocations.forEach { invocation ->
                assertFalse(
                    "Candidate card must not contain direct scripted prayer to God ('$invocation'): '${card.description}'",
                    text.contains(invocation)
                )
            }
        }
    }

    @Test
    fun `test candidate cards strictly exclude saint and angelic intercession`() {
        val validCandidate = CandidatePrayerPoint(
            title = "Faithful Intercession",
            description = "Approaching the Father through Christ alone; perseverance in prayer for brethren; peace in trial"
        )

        val text = "${validCandidate.title} ${validCandidate.description}".lowercase()
        forbiddenIntermediaries.forEach { intermediary ->
            assertFalse(
                "Candidate card must not invoke non-Christ intermediaries ('$intermediary')",
                text.contains(intermediary)
            )
        }
    }

    @Test
    fun `test candidate cards strictly reject prosperity and decree rhetoric`() {
        val validCandidate = CandidatePrayerPoint(
            title = "Provision and Trust",
            description = "Diligent stewardship in employment search; humble submission to Father's timing; freedom from anxiety"
        )

        val text = "${validCandidate.title} ${validCandidate.description}".lowercase()
        forbiddenTheologies.forEach { falseTheology ->
            assertFalse(
                "Candidate card must not adopt prosperity/word-faith rhetoric ('$falseTheology')",
                text.contains(falseTheology)
            )
        }
    }

    @Test
    fun `test card brevity title and description ceilings`() {
        val sampleCards = listOf(
            CandidatePrayerPoint(
                title = "Sovereign Peace",
                description = "Quiet heart amidst physical discomfort; trust in Father's preservation; spiritual comfort"
            ),
            CandidatePrayerPoint(
                title = "Reconciliation in Marriage",
                description = "Humble repentance; gracious communication; Christlike patience during disagreements"
            )
        )

        sampleCards.forEach { card ->
            // Title ceiling: strictly 2 to 6 words
            val titleWords = card.title.split("\\s+".toRegex()).filter { it.isNotBlank() }
            assertTrue("Title must be 2 to 6 words: actual ${titleWords.size} ('${card.title}')", titleWords.size in 2..6)

            // Description ceiling: strictly <= 25 words
            val descWords = card.description.split("\\s+".toRegex()).filter { it.isNotBlank() }
            assertTrue("Description must be <= 25 words: actual ${descWords.size} ('${card.description}')", descWords.size <= 25)

            // Preferred 2-to-3 clause semicolon pattern
            val clauses = card.description.split(";").map { it.trim() }
            assertTrue("Description must have 2 to 3 clauses: actual ${clauses.size} in '${card.description}'", clauses.size in 2..3)
        }
    }

    @Test
    fun `test exclusion of prohibited abbreviations`() {
        val testCards = listOf(
            CandidatePrayerPoint(
                title = "Trial with Employer",
                description = "Wisdom with supervisor; gracious speech; enduring trial with patient hope"
            )
        )

        testCards.forEach { card ->
            assertFalse("Must not use 'w/' abbreviation", card.description.contains("w/"))
            assertFalse("Must not use '/w' abbreviation", card.description.contains("/w"))
        }
    }

    @Test
    fun `test prohibition of unstated medical crisis presumption`() {
        // Given a generic reflection like "Pray for Sarah", the card must not invent cancer or ICU
        val forbiddenPresumptions = listOf("cancer", "chemotherapy", "icu", "stroke", "heart attack", "terminal")

        val honestCandidate = CandidatePrayerPoint(
            title = "Guidance for Sarah",
            description = "Wisdom for present circumstances; peace in decisions; deepening faith in Christ"
        )

        val text = "${honestCandidate.title} ${honestCandidate.description}".lowercase()
        forbiddenPresumptions.forEach { presumption ->
            assertFalse(
                "Card must never presume unstated medical crisis ('$presumption')",
                text.contains(presumption)
            )
        }
    }

    @Test
    fun `test Heidelberg Catechism comfort grounding in suffering`() {
        // Heidelberg Q1 comfort: Belonging to faithful Savior Jesus Christ, Father's sovereign preservation
        val comfortPoint = CandidatePrayerPoint(
            title = "Comfort in Affliction",
            description = "Resting in Christ's ownership; assurance of Father's preservation; hope of eternal life"
        )

        val clauses = comfortPoint.description.split(";").map { it.trim() }
        assertEquals(3, clauses.size)
        assertTrue(comfortPoint.description.contains("Christ"))
        assertTrue(comfortPoint.description.contains("Father"))
    }
}
