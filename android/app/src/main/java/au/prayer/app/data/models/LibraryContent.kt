package au.prayer.app.data.models

import android.content.Context
import au.prayer.app.R
import kotlinx.serialization.json.Json

/**
 * Thread-safe loader and cache for Library volumes.
 */
object LibraryContent {

    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var cachedCalvinVolume: LibraryVolume? = null

    /**
     * Initializes and parses the Calvin volume from R.raw.library_calvin_prayer.
     */
    fun initialize(context: Context) {
        if (cachedCalvinVolume == null) {
            synchronized(this) {
                if (cachedCalvinVolume == null) {
                    val text = context.resources.openRawResource(R.raw.library_calvin_prayer)
                        .bufferedReader()
                        .use { it.readText() }
                    cachedCalvinVolume = parseVolume(text)
                }
            }
        }
    }

    /**
     * Test / alternate-source injection: parses volume from json text.
     */
    fun loadFromText(jsonText: String): LibraryVolume {
        synchronized(this) {
            val volume = parseVolume(jsonText)
            cachedCalvinVolume = volume
            return volume
        }
    }

    fun parseVolume(jsonText: String): LibraryVolume {
        return json.decodeFromString<LibraryVolume>(jsonText)
    }

    val calvinVolume: LibraryVolume
        get() = cachedCalvinVolume ?: throw IllegalStateException(
            "LibraryContent has not been initialized. Call initialize(context) or loadFromText(json)."
        )

    fun getAllVolumes(): List<LibraryVolume> {
        return cachedCalvinVolume?.let { listOf(it) } ?: emptyList()
    }
}
