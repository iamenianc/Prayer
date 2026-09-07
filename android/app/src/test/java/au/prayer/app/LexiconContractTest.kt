package au.prayer.app

import au.prayer.app.data.models.AppConfig
import au.prayer.app.data.models.LocaleDialect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        "topic 1 of"
    )

    @Test
    fun `test default dialect is English Australian UK`() {
        val defaultConfig = AppConfig()
        assertEquals(LocaleDialect.EN_AU_UK, defaultConfig.localeDialect)
        assertEquals("English (Australian / UK)", defaultConfig.localeDialect.displayName)
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
    fun `test approved devotional phrasing invariants`() {
        val approvedActionPhrases = listOf(
            "Start praying",
            "Open Journal",
            "Log prayer points",
            "Praying for",
            "Who are you praying for?",
            "From your journal",
            "Add a person or group",
            "Direct Entry",
            "Guide me",
            "What is on your heart?",
            "Clarifying question",
            "Skip to petitions",
            "Review petitions",
            "Thanksgiving note"
        )

        approvedActionPhrases.forEach { phrase ->
            forbiddenTerms.forEach { forbidden ->
                assertFalse(
                    "Approved devotional phrase '$phrase' must not contain forbidden term '$forbidden'",
                    phrase.lowercase().contains(forbidden)
                )
            }
        }
    }
}
