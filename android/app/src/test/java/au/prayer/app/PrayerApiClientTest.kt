package au.prayer.app

import au.prayer.app.network.*
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class PrayerApiClientTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val apiClient = PrayerApiClient()

    @Test
    fun `test SuggestRequest serialization matches wire format`() {
        val request = SuggestRequest(
            targetName = "Sarah Jenkins",
            root = "PEOPLE",
            group = "Small Group",
            contextDescription = "Close friend from church",
            recordedPoints = listOf(
                RecordedPoint(title = "Peace in testing", body = "Undergoing biopsy", status = "ACTIVE")
            ),
            journalUpdates = listOf(
                JournalUpdateItem(text = "Met for coffee Tuesday")
            ),
            currentDraft = "Recovery after surgery",
            localeDialect = "EN_AU_UK"
        )

        val jsonString = json.encodeToString(SuggestRequest.serializer(), request)

        // Verify serial name mappings
        assertTrue("JSON must contain 'target_name'", jsonString.contains("\"target_name\""))
        assertTrue("JSON must contain 'context_description'", jsonString.contains("\"context_description\""))
        assertTrue("JSON must contain 'recorded_points'", jsonString.contains("\"recorded_points\""))
        assertTrue("JSON must contain 'journal_updates'", jsonString.contains("\"journal_updates\""))
        assertTrue("JSON must contain 'current_draft'", jsonString.contains("\"current_draft\""))
        assertTrue("JSON must contain 'locale_dialect'", jsonString.contains("\"locale_dialect\""))
        assertTrue("JSON must contain 'root'", jsonString.contains("\"root\""))
    }

    @Test
    fun `test SuggestResponse deserialization`() {
        val wireJson = """
            {
                "suggestions": [
                    "Peace awaiting test results",
                    "Patience in daily exhaustion",
                    "Gratitude for provision",
                    "Renewed strength each morning"
                ]
            }
        """.trimIndent()

        val response = json.decodeFromString(SuggestResponse.serializer(), wireJson)

        assertEquals(4, response.suggestions.size)
        assertEquals("Peace awaiting test results", response.suggestions[0])
        assertEquals("Patience in daily exhaustion", response.suggestions[1])
        assertEquals("Gratitude for provision", response.suggestions[2])
        assertEquals("Renewed strength each morning", response.suggestions[3])
    }

    @Test
    fun `test SuggestResponse grouped deserialization for Praise God, Thank God, Ask God`() {
        val wireJson = """
            {
                "praise_god": [
                    "For His steadfast love shown in Christ",
                    "Because God reigns sovereign over all creation"
                ],
                "thank_god": [
                    "That his cancer is in remission",
                    "For faithful preservation through trials"
                ],
                "ask_god": [
                    "A new and renewed mind",
                    "That he may walk in wisdom and truth"
                ],
                "suggestions": [
                    "For His steadfast love shown in Christ",
                    "Because God reigns sovereign over all creation",
                    "That his cancer is in remission",
                    "For faithful preservation through trials",
                    "A new and renewed mind",
                    "That he may walk in wisdom and truth"
                ]
            }
        """.trimIndent()

        val response = json.decodeFromString(SuggestResponse.serializer(), wireJson)

        assertEquals(2, response.praiseGod.size)
        assertEquals(2, response.thankGod.size)
        assertEquals(2, response.askGod.size)
        assertEquals(6, response.suggestions.size)

        val groups = response.promptGroups
        assertEquals(3, groups.size)
        assertEquals("Praise God", groups[0].title)
        assertEquals("Thank God", groups[1].title)
        assertEquals("Ask God", groups[2].title)

        assertEquals("For His steadfast love shown in Christ", groups[0].prompts[0])
        assertEquals("Because God reigns sovereign over all creation", groups[0].prompts[1])
        assertEquals("That his cancer is in remission", groups[1].prompts[0])
        assertEquals("A new and renewed mind", groups[2].prompts[0])
    }

    @Test
    fun `test SuggestResponse ignores unknown JSON keys gracefully`() {
        val wireJsonWithExtra = """
            {
                "suggestions": [
                    "Steadfast faith today"
                ],
                "extra_server_metadata": "12345",
                "execution_time_ms": 340
            }
        """.trimIndent()

        val response = json.decodeFromString(SuggestResponse.serializer(), wireJsonWithExtra)
        assertEquals(1, response.suggestions.size)
        assertEquals("Steadfast faith today", response.suggestions[0])
    }

    @Test
    fun `test TitleRequest and TitleResponse serialization`() {
        val request = TitleRequest(text = "Physical recovery after medical treatment")
        val reqJson = json.encodeToString(TitleRequest.serializer(), request)
        assertTrue(reqJson.contains("\"text\""))

        val respJson = """{"title": "Medical Recovery"}"""
        val response = json.decodeFromString(TitleResponse.serializer(), respJson)
        assertEquals("Medical Recovery", response.title)
    }

    @Test
    fun `test offline fallback title generator behavior`() {
        // Empty text fallback
        assertEquals("Prayer Point", apiClient.generateOfflineFallbackTitle(""))
        assertEquals("Prayer Point", apiClient.generateOfflineFallbackTitle("   "))

        // Standard bullet text
        assertEquals("Gospel Boldness in workplace", apiClient.generateOfflineFallbackTitle("• Gospel Boldness in workplace"))
        assertEquals("Gospel Boldness", apiClient.generateOfflineFallbackTitle("• Gospel Boldness"))
        assertEquals("Perseverance in Trial", apiClient.generateOfflineFallbackTitle("Perseverance in Trial"))

        // Length restriction: max 4 words
        val title = apiClient.generateOfflineFallbackTitle("Lord give us grace and mercy every day")
        val words = title.split(" ")
        assertTrue("Fallback title should be at most 4 words", words.size <= 4)
        assertEquals("Lord give us grace", title)
    }

    @Test
    fun `test SuggestResponse promptGroups fallback to Ask God when only suggestions array is populated`() {
        val response = SuggestResponse(
            suggestions = listOf("Peace in testing", "Endurance in pain")
        )
        val groups = response.promptGroups
        assertEquals(1, groups.size)
        assertEquals("Ask God", groups[0].title)
        assertEquals(2, groups[0].prompts.size)
        assertEquals("Peace in testing", groups[0].prompts[0])
    }

    @Test
    fun `test SuggestResponse promptGroups with partial category populations`() {
        val response = SuggestResponse(
            praiseGod = listOf("For God's mercy"),
            askGod = listOf("Wisdom in difficult conversations")
        )
        val groups = response.promptGroups
        assertEquals(2, groups.size)
        assertEquals("Praise God", groups[0].title)
        assertEquals("Ask God", groups[1].title)
        assertEquals("For God's mercy", groups[0].prompts[0])
        assertEquals("Wisdom in difficult conversations", groups[1].prompts[0])
    }

    @Test
    fun `test SuggestResponse promptGroups when all categories are empty`() {
        val emptyResponse = SuggestResponse()
        assertTrue("Empty response must produce empty prompt groups", emptyResponse.promptGroups.isEmpty())
    }

    @Test
    fun `test offline fallback title generator with multi-bullet and excessive whitespace`() {
        assertEquals("Faith in Christ", apiClient.generateOfflineFallbackTitle("• • Faith in Christ"))
        assertEquals("Devotional Peace", apiClient.generateOfflineFallbackTitle("\n\t  •  Devotional Peace  \n\t"))
        assertEquals("Psalm 23 verse 1", apiClient.generateOfflineFallbackTitle("• Psalm 23 verse 1 - Lord is my shepherd"))
    }
}

