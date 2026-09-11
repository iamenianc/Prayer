package au.prayer.app.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@Serializable
data class RecordedPoint(
    val title: String,
    val body: String,
    val status: String
)

@Serializable
data class JournalUpdateItem(
    val text: String
)

@Serializable
data class SuggestRequest(
    @SerialName("target_name") val targetName: String? = null,
    val root: String,
    val group: String? = null,
    @SerialName("context_description") val contextDescription: String? = null,
    @SerialName("recorded_points") val recordedPoints: List<RecordedPoint> = emptyList(),
    @SerialName("journal_updates") val journalUpdates: List<JournalUpdateItem> = emptyList(),
    @SerialName("current_draft") val currentDraft: String? = null,
    @SerialName("locale_dialect") val localeDialect: String = "EN_AU_UK"
)

@Serializable
data class PromptGroup(
    val title: String,
    val prompts: List<String>
)

@Serializable
data class SuggestResponse(
    @SerialName("praise_god") val praiseGod: List<String> = emptyList(),
    @SerialName("thank_god") val thankGod: List<String> = emptyList(),
    @SerialName("ask_god") val askGod: List<String> = emptyList(),
    val suggestions: List<String> = emptyList()
) {
    val promptGroups: List<PromptGroup>
        get() {
            val list = mutableListOf<PromptGroup>()
            if (praiseGod.isNotEmpty()) list.add(PromptGroup("Praise God", praiseGod))
            if (thankGod.isNotEmpty()) list.add(PromptGroup("Thank God", thankGod))
            if (askGod.isNotEmpty()) list.add(PromptGroup("Ask God", askGod))
            if (list.isEmpty() && suggestions.isNotEmpty()) {
                list.add(PromptGroup("Ask God", suggestions))
            }
            return list
        }
}

@Deprecated("Titles are no longer used for prayer journal entries")
@Serializable
data class TitleRequest(
    val text: String
)

@Deprecated("Titles are no longer used for prayer journal entries")
@Serializable
data class TitleResponse(
    val title: String
)

@Deprecated("Legacy interactive wizard model; superseded by SuggestRequest")
@Serializable
data class CandidatePrayerPoint(
    val title: String,
    val description: String,
    @SerialName("suggested_root") val suggestedRoot: String? = null,
    @SerialName("suggested_group") val suggestedGroup: String? = null
)

@Deprecated("Legacy interactive wizard model; superseded by SuggestRequest")
@Serializable
data class GuideRequest(
    @SerialName("initial_reflection") val initialReflection: String,
    val root: String? = null,
    val group: String? = null,
    @SerialName("clarifying_question") val clarifyingQuestion: String? = null,
    @SerialName("user_response") val userResponse: String? = null,
    @SerialName("request_more") val requestMore: Boolean = false
)

@Deprecated("Legacy interactive wizard model; superseded by SuggestResponse")
@Serializable
data class GuideResponse(
    @SerialName("skip_question") val skipQuestion: Boolean,
    @SerialName("clarifying_question") val clarifyingQuestion: String? = null,
    @SerialName("candidate_prayer_points") val candidatePrayerPoints: List<CandidatePrayerPoint> = emptyList()
)

class PrayerApiClient(
    private val baseUrl: String = "https://pray-proxy.reflex-game.workers.dev",
    private val gatewaySecret: String = "prayer-app-secret-key-2026"
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun getSuggestions(request: SuggestRequest): Result<SuggestResponse> = withContext(Dispatchers.IO) {
        try {
            val bodyString = json.encodeToString(SuggestRequest.serializer(), request)
            val httpRequest = Request.Builder()
                .url("$baseUrl/api/v1/suggest")
                .addHeader("X-Prayer-Gateway-Secret", gatewaySecret)
                .addHeader("Content-Type", "application/json")
                .post(bodyString.toRequestBody(jsonMediaType))
                .build()

            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP ${response.code}"))
                }
                val responseBody = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))
                val suggestResponse = json.decodeFromString(SuggestResponse.serializer(), responseBody)
                Result.success(suggestResponse)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Deprecated("Legacy interactive wizard endpoint; superseded by getSuggestions")
    suspend fun getGuidance(request: GuideRequest): Result<GuideResponse> = withContext(Dispatchers.IO) {
        try {
            val bodyString = json.encodeToString(GuideRequest.serializer(), request)
            val httpRequest = Request.Builder()
                .url("$baseUrl/api/v1/guide")
                .addHeader("X-Prayer-Gateway-Secret", gatewaySecret)
                .addHeader("Content-Type", "application/json")
                .post(bodyString.toRequestBody(jsonMediaType))
                .build()

            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP ${response.code}"))
                }
                val responseBody = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))
                val guideResponse = json.decodeFromString(GuideResponse.serializer(), responseBody)
                Result.success(guideResponse)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Deprecated("Titles are no longer used for prayer journal entries; endpoint has been decommissioned")
    suspend fun generateTitle(text: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bodyString = json.encodeToString(TitleRequest.serializer(), TitleRequest(text))
            val httpRequest = Request.Builder()
                .url("$baseUrl/api/v1/title")
                .addHeader("X-Prayer-Gateway-Secret", gatewaySecret)
                .addHeader("Content-Type", "application/json")
                .post(bodyString.toRequestBody(jsonMediaType))
                .build()

            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP ${response.code}"))
                }
                val responseBody = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))
                val titleResponse = json.decodeFromString(TitleResponse.serializer(), responseBody)
                Result.success(titleResponse.title)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Offline fallback title generator: produces a clean 2–4 word title from text
     */
    @Deprecated("Titles are no longer used for prayer journal entries")
    fun generateOfflineFallbackTitle(text: String): String {
        val clean = text.replace("•", "").trim()
        val words = clean.split("\\s+".toRegex()).filter { it.isNotBlank() }
        return if (words.isEmpty()) {
            "Prayer Point"
        } else {
            words.take(4).joinToString(" ")
        }
    }
}
