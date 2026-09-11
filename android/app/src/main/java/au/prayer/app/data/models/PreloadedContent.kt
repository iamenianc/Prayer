package au.prayer.app.data.models

import android.content.Context
import au.prayer.app.R
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class PreloadedHistoricTopic(
    val entity: IndividualEntity,
    val prayerPoint: PrayerPoint
)

@Serializable
data class HistoricPrayerCatalog(
    val schemaVersion: Int = 1,
    val namespace: String = "historic",
    val prayers: List<HistoricPrayerEntry> = emptyList()
)

@Serializable
data class HistoricPrayerEntry(
    val sortOrder: Int = 0,
    val entity: HistoricEntityDto,
    val prayerPoint: HistoricPrayerPointDto
)

@Serializable
data class HistoricEntityDto(
    val id: String,
    val rootCode: String,
    val displayName: String,
    val contextDescription: String = "",
    val createdAt: Long
)

@Serializable
data class HistoricPrayerPointDto(
    val id: String,
    val title: String,
    val description: String
)

object PreloadedContent {

    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var cachedTopics: List<PreloadedHistoricTopic>? = null

    // Loads the standalone catalog document (res/raw/historic_prayers.json) once.
    fun initialize(context: Context) {
        if (cachedTopics == null) {
            synchronized(this) {
                if (cachedTopics == null) {
                    val text = context.resources.openRawResource(R.raw.historic_prayers)
                        .bufferedReader()
                        .use { it.readText() }
                    cachedTopics = parseCatalog(text)
                }
            }
        }
    }

    // Test / alternate-source injection: parses the same standalone document format from text.
    fun loadFromText(jsonText: String) {
        synchronized(this) {
            cachedTopics = parseCatalog(jsonText)
        }
    }

    private fun parseCatalog(jsonText: String): List<PreloadedHistoricTopic> {
        val catalog = json.decodeFromString<HistoricPrayerCatalog>(jsonText)
        return catalog.prayers
            .sortedBy { it.sortOrder }
            .map { entry ->
                val entity = IndividualEntity(
                    id = entry.entity.id,
                    rootCode = RootCode.valueOf(entry.entity.rootCode),
                    displayName = entry.entity.displayName,
                    contextDescription = entry.entity.contextDescription,
                    isPreloadedHistoric = true,
                    createdAt = entry.entity.createdAt
                )
                val point = PrayerPoint(
                    id = entry.prayerPoint.id,
                    entityId = entity.id,
                    title = entry.prayerPoint.title,
                    description = entry.prayerPoint.description,
                    status = PrayerStatus.HISTORIC,
                    createdAt = entry.entity.createdAt
                )
                PreloadedHistoricTopic(entity = entity, prayerPoint = point)
            }
    }

    private fun topics(): List<PreloadedHistoricTopic> =
        cachedTopics ?: throw IllegalStateException(
            "PreloadedContent has not been initialized. Call initialize(context) or loadFromText(json)."
        )

    val HISTORIC_TOPICS: List<PreloadedHistoricTopic> get() = topics()

    val HISTORIC_ENTITY_ID: String get() = topics().first().entity.id

    fun getHistoricEntities(): List<IndividualEntity> = topics().map { it.entity }

    fun getHistoricPrayerPoints(): List<PrayerPoint> = topics().map { it.prayerPoint }

    // Backward-compatible single entity lookup
    fun getHistoricEntity(): IndividualEntity = topics().first().entity
}