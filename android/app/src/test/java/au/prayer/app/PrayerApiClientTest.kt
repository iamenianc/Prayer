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
}
