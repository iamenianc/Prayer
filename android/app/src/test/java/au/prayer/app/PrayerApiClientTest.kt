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
    fun `test GuideRequest serialization matches wire format`() {
        val request = GuideRequest(
            initialReflection = "Feeling overwhelmed by work deadlines and family pressures",
            root = "PEOPLE",
            group = null,
            clarifyingQuestion = "What is causing the feeling?",
            userResponse = "Heavy workload and expectations",
            requestMore = false
        )

        val jsonString = json.encodeToString(GuideRequest.serializer(), request)

        // Verify serial name mappings
        assertTrue("JSON must contain 'initial_reflection'", jsonString.contains("\"initial_reflection\""))
        assertTrue("JSON must contain 'clarifying_question'", jsonString.contains("\"clarifying_question\""))
        assertTrue("JSON must contain 'user_response'", jsonString.contains("\"user_response\""))
        assertTrue("JSON must contain 'request_more'", jsonString.contains("\"request_more\""))
        assertTrue("JSON must contain 'root'", jsonString.contains("\"root\""))
    }

    @Test
    fun `test GuideResponse deserialization with clarifying question`() {
        val wireJson = """
            {
                "skip_question": false,
                "clarifying_question": "What is making you feel anxious right now?",
                "candidate_prayer_points": []
            }
        """.trimIndent()

        val response = json.decodeFromString(GuideResponse.serializer(), wireJson)

        assertFalse(response.skipQuestion)
        assertEquals("What is making you feel anxious right now?", response.clarifyingQuestion)
        assertTrue(response.candidatePrayerPoints.isEmpty())
    }

    @Test
    fun `test GuideResponse deserialization with candidate prayer points`() {
        val wireJson = """
            {
                "skip_question": true,
                "clarifying_question": null,
                "candidate_prayer_points": [
                    {
                        "title": "Surgery Recovery",
                        "description": "Rapid physical healing after procedure; patient endurance in therapy; gratitude for care",
                        "suggested_root": "PEOPLE",
                        "suggested_group": null
                    },
                    {
                        "title": "Abiding Peace",
                        "description": "Quiet heart amidst physical weakness; trust in Father's sovereign care; spiritual comfort",
                        "suggested_root": "PEOPLE",
                        "suggested_group": null
                    }
                ]
            }
        """.trimIndent()

        val response = json.decodeFromString(GuideResponse.serializer(), wireJson)

        assertTrue(response.skipQuestion)
        assertNull(response.clarifyingQuestion)
        assertEquals(2, response.candidatePrayerPoints.size)

        val first = response.candidatePrayerPoints[0]
        assertEquals("Surgery Recovery", first.title)
        assertEquals("Rapid physical healing after procedure; patient endurance in therapy; gratitude for care", first.description)
        assertEquals("PEOPLE", first.suggestedRoot)
        assertNull(first.suggestedGroup)

        val second = response.candidatePrayerPoints[1]
        assertEquals("Abiding Peace", second.title)
        assertEquals("PEOPLE", second.suggestedRoot)
    }

    @Test
    fun `test GuideResponse ignores unknown JSON keys gracefully`() {
        val wireJsonWithExtra = """
            {
                "skip_question": true,
                "clarifying_question": null,
                "candidate_prayer_points": [],
                "extra_server_metadata": "12345",
                "execution_time_ms": 340
            }
        """.trimIndent()

        val response = json.decodeFromString(GuideResponse.serializer(), wireJsonWithExtra)
        assertTrue(response.skipQuestion)
        assertTrue(response.candidatePrayerPoints.isEmpty())
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
