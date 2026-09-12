package au.prayer.app

import au.prayer.app.data.models.*
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit test suite verifying the authoritative historic prayer catalog contract
 * (res/raw/historic_prayers.json) and PreloadedContent lifecycle and query APIs.
 */
class PreloadedContentContractTest {

    private lateinit var rawJson: String

    @Before
    fun setUp() {
        rawJson = javaClass.getResourceAsStream("/historic_prayers.json")
            ?.readBytes()?.decodeToString()
            ?: error("historic_prayers.json not found on test classpath")
        PreloadedContent.loadFromText(rawJson)
    }

    @Test
    fun `test historic catalog raw JSON schema and count`() {
        val json = Json { ignoreUnknownKeys = true }
        val catalog = json.decodeFromString<HistoricPrayerCatalog>(rawJson)

        assertEquals(1, catalog.schemaVersion)
        assertEquals("historic", catalog.namespace)
        assertEquals("Catalog must contain exactly 30 public-domain historic prayers", 30, catalog.prayers.size)
    }

    @Test
    fun `test all historic entries have strictly sequential sort orders from 1 to 30`() {
        val json = Json { ignoreUnknownKeys = true }
        val catalog = json.decodeFromString<HistoricPrayerCatalog>(rawJson)

        val sortOrders = catalog.prayers.map { it.sortOrder }
        assertEquals(30, sortOrders.size)
        assertEquals((1..30).toList(), sortOrders)
    }

    @Test
    fun `test all historic entities have unique IDs prefixed with historic-`() {
        val entities = PreloadedContent.getHistoricEntities()
        assertEquals(30, entities.size)

        val ids = entities.map { it.id }
        assertEquals("All entity IDs must be distinct", 30, ids.toSet().size)

        for (entity in entities) {
            assertTrue("Entity ID '${entity.id}' must start with 'historic-'", entity.id.startsWith("historic-"))
            assertEquals("Entity '${entity.displayName}' must be RootCode.HISTORIC", RootCode.HISTORIC, entity.rootCode)
            assertTrue("Entity '${entity.displayName}' must have isPreloadedHistoric = true", entity.isPreloadedHistoric)
            assertTrue("Entity display name must not be blank", entity.displayName.isNotBlank())
            assertTrue("Entity context description must not be blank", entity.contextDescription.isNotBlank())
            assertTrue("Entity createdAt must be positive", entity.createdAt > 0L)
            assertFalse("Preloaded historic entity must default to unpinned", entity.isPinned)
            assertEquals(0, entity.interactedCount)
            assertNull(entity.lastInteractedAt)
        }
    }

    @Test
    fun `test all historic prayer points have valid IDs and non-empty content`() {
        val points = PreloadedContent.getHistoricPrayerPoints()
        assertEquals(30, points.size)

        val pointIds = points.map { it.id }
        assertEquals("All point IDs must be distinct", 30, pointIds.toSet().size)

        for (point in points) {
            assertTrue("Point ID '${point.id}' must start with 'point-historic-'", point.id.startsWith("point-historic-"))
            assertTrue("Point '${point.title}' entityId must start with 'historic-'", point.entityId.startsWith("historic-"))
            assertTrue("Point title must not be blank", point.title.isNotBlank())
            assertTrue("Point description must not be blank", point.description.isNotBlank())
            assertEquals("Point status must be PrayerStatus.HISTORIC", PrayerStatus.HISTORIC, point.status)
            assertEquals(0, point.interactedCount)
            assertNull(point.lastInteractedAt)
            assertNull(point.answeredAt)
            assertNull(point.answeredTestimony)
        }
    }

    @Test
    fun `test HISTORIC_TOPICS composition and entity-prayer point linkage`() {
        val topics = PreloadedContent.HISTORIC_TOPICS
        assertEquals(30, topics.size)

        for (topic in topics) {
            assertEquals(RootCode.HISTORIC, topic.entity.rootCode)
            assertEquals(PrayerStatus.HISTORIC, topic.prayerPoint.status)
            assertEquals("Prayer point entityId must link to entity id", topic.entity.id, topic.prayerPoint.entityId)
            assertTrue("Entity must be preloaded historic", topic.entity.isPreloadedHistoric)
        }

        // Mapping to TopicWithPoints
        val topicWithPointsList = topics.map { topic ->
            TopicWithPoints(
                entity = topic.entity,
                activePoints = listOf(topic.prayerPoint),
                answeredPoints = emptyList()
            )
        }
        assertEquals(30, topicWithPointsList.size)
        for (item in topicWithPointsList) {
            assertEquals(1, item.activePoints.size)
            assertEquals(0, item.answeredPoints.size)
        }
    }

    @Test
    fun `test HISTORIC_ENTITY_ID and getHistoricEntity single lookup`() {
        val entityId = PreloadedContent.HISTORIC_ENTITY_ID
        assertTrue("Historic entity ID must start with historic-", entityId.startsWith("historic-"))

        val singleEntity = PreloadedContent.getHistoricEntity()
        assertEquals(entityId, singleEntity.id)
        assertEquals(RootCode.HISTORIC, singleEntity.rootCode)
    }

    @Test(expected = Exception::class)
    fun `test loadFromText with invalid JSON throws serialization error`() {
        val invalidJson = "{ corrupt json content"
        PreloadedContent.loadFromText(invalidJson)
    }

    @Test
    fun `test presence of canonical bedrock historic prayers`() {
        val entities = PreloadedContent.getHistoricEntities()
        val names = entities.map { it.displayName }

        assertTrue("Must contain The Lord's Prayer", names.any { it.contains("The Lord's Prayer") })
        assertTrue("Must contain Collect for Peace", names.any { it.contains("Collect for Peace") })
        assertTrue("Must contain Collect for Grace", names.any { it.contains("Collect for Grace") })
        assertTrue("Must contain Collect for Purity", names.any { it.contains("Collect for Purity") })
        assertTrue("Must contain A General Thanksgiving", names.any { it.contains("A General Thanksgiving") })
        assertTrue("Must contain The Apostles' Creed", names.any { it.contains("The Apostles' Creed") })
        assertTrue("Must contain Charles Spurgeon", names.any { it.contains("Spurgeon") })
        assertTrue("Must contain Augustine of Hippo", names.any { it.contains("Augustine") })
        assertTrue("Must contain John Chrysostom", names.any { it.contains("Chrysostom") })
        assertTrue("Must contain Polycarp", names.any { it.contains("Polycarp") })
    }

    @Test
    fun `test historic prayers maintain liturgical reverence and doxological endings`() {
        val points = PreloadedContent.getHistoricPrayerPoints()
        for (point in points) {
            val desc = point.description.trim()
            // Devotional historic prayers should conclude reverently with Amen or appropriate closure
            assertTrue(
                "Prayer '${point.title}' should end reverently (ending: '${desc.takeLast(10)}')",
                desc.endsWith("Amen.") || desc.endsWith("Amen") || desc.endsWith(".")
            )
        }
    }
}
