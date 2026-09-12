package au.prayer.app

import au.prayer.app.data.models.AppConfig
import au.prayer.app.data.models.LocaleDialect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LexiconContractTest {

    private val forbiddenTerms = listOf(
        "target",
        "entity",
        "ticket",
        "commit to",
        "sqlite",
        "database",
        "point 1",
        "point 2",
        "item 1",
        "item 2",
        "point 1 of",
        "topic 1 of",
        "petition",
        "petitions",
        "guide me",
        "log prayer"
    )

    private val approvedActionPhrases = listOf(
        "Start praying",
        "Open Journal",
        "Add prayer points",
        "Praying for",
        "Who are you praying for?",
        "From your journal",
        "Add a person, group, general topic, or mission partner",
        "Direct Entry",
        "Prayer Assistant",
        "What is on your heart?",
        "Clarifying question",
        "Skip to prayer points",
        "Review prayer points",
        "Thanksgiving note",
        "Notes"
    )

    @Test
    fun `test default dialect is English Australian UK`() {
        val defaultConfig = AppConfig()
        assertEquals(LocaleDialect.EN_AU_UK, defaultConfig.localeDialect)
        assertEquals("English (Australian / UK)", defaultConfig.localeDialect.displayName)
        assertEquals("EN_AU_UK", defaultConfig.localeDialect.code)

        // US English option verification
        assertEquals(LocaleDialect.EN_US, LocaleDialect.valueOf("EN_US"))
        assertEquals("US English", LocaleDialect.EN_US.displayName)
        assertEquals("EN_US", LocaleDialect.EN_US.code)
    }

    @Test
    fun `test negative lexicon across strings resource file`() {
        // Locate strings.xml file in the project
        val stringsFile = File("src/main/res/values/strings.xml")
        if (stringsFile.exists()) {
            val content = stringsFile.readText().lowercase()

            forbiddenTerms.forEach { term ->
                assertFalse(
                    "User-facing strings.xml must not contain forbidden clinical/ticketing term '$term'",
                    content.contains(term)
                )
            }
        }
    }

    @Test
    fun `test prohibition of sequential or ordinal numbering via regex pattern`() {
        val ordinalRegex = "\\b(point|item|petition|step)\\s*\\d+\\b|\\b\\d+\\s*of\\s*\\d+\\b".toRegex(RegexOption.IGNORE_CASE)

        val stringsFile = File("src/main/res/values/strings.xml")
        if (stringsFile.exists()) {
            val content = stringsFile.readText()
            assertFalse(
                "strings.xml must not contain ordinal numbering patterns (e.g. Point 1, Item 1, 1 of 5)",
                ordinalRegex.containsMatchIn(content)
            )
        }
    }

    @Test
    fun `test approved devotional phrasing invariants`() {
        approvedActionPhrases.forEach { phrase ->
            forbiddenTerms.forEach { forbidden ->
                assertFalse(
                    "Approved devotional phrase '$phrase' must not contain forbidden term '$forbidden'",
                    phrase.lowercase().contains(forbidden)
                )
            }
        }
    }

    @Test
    fun `test strings resource file contains core approved devotional terms`() {
        val stringsFile = File("src/main/res/values/strings.xml")
        if (stringsFile.exists()) {
            val content = stringsFile.readText()
            assertTrue("Must contain 'Start praying'", content.contains("Start praying"))
            assertTrue("Must contain 'Open Journal'", content.contains("Open Journal"))
            assertTrue("Must contain 'Add prayer points'", content.contains("Add prayer points"))
            assertTrue("Must contain 'Prayer Assistant'", content.contains("Prayer Assistant"))
            assertTrue("Must contain 'What is on your heart?'", content.contains("What is on your heart?"))
            assertTrue("Must contain 'Clarifying question'", content.contains("Clarifying question"))
            assertTrue("Must contain 'Skip to prayer points'", content.contains("Skip to prayer points"))
            assertTrue("Must contain 'Review prayer points'", content.contains("Review prayer points"))
            assertTrue("Must contain 'Thanksgiving note'", content.contains("Thanksgiving note"))
        }
    }
}
