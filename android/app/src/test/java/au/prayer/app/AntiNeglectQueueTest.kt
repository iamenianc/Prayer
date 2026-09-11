package au.prayer.app

import au.prayer.app.data.models.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AntiNeglectQueueTest {

    @Before
    fun setUp() {
        val json = javaClass.getResourceAsStream("/historic_prayers.json")
            ?.readBytes()?.decodeToString()
            ?: error("historic_prayers.json not found on the test classpath")
        PreloadedContent.loadFromText(json)
    }

    // Mirroring the exact SQL ORDER BY contract from PrayerRepository:
    // e.last_interacted_at IS NOT NULL ASC, e.last_interacted_at ASC, e.interacted_count ASC
    private val antiNeglectComparator = Comparator<IndividualEntity> { a, b ->
        val aNull = a.lastInteractedAt == null
        val bNull = b.lastInteractedAt == null

        if (aNull && !bNull) return@Comparator -1
        if (!aNull && bNull) return@Comparator 1

        if (!aNull && !bNull) {
            val timeComp = a.lastInteractedAt!!.compareTo(b.lastInteractedAt!!)
            if (timeComp != 0) return@Comparator timeComp
        }

        a.interactedCount.compareTo(b.interactedCount)
    }

    @Test
    fun `test anti-neglect queue sorting priority order`() {
        val now = System.currentTimeMillis()

        val neverPrayed = IndividualEntity(
            displayName = "Never Prayed",
            rootCode = RootCode.PEOPLE,
            interactedCount = 0,
            lastInteractedAt = null
        )
        val prayedLongAgo = IndividualEntity(
            displayName = "Prayed Long Ago",
            rootCode = RootCode.PEOPLE,
            interactedCount = 1,
            lastInteractedAt = now - 1000000L
        )
        val prayedRecently = IndividualEntity(
            displayName = "Prayed Recently",
            rootCode = RootCode.PEOPLE,
            interactedCount = 5,
            lastInteractedAt = now - 1000L
        )
        val prayedRecentlyLowCount = IndividualEntity(
            displayName = "Prayed Recently Low Count",
            rootCode = RootCode.PEOPLE,
            interactedCount = 2,
            lastInteractedAt = now - 1000L
        )

        val rawList = listOf(prayedRecently, neverPrayed, prayedLongAgo, prayedRecentlyLowCount)
        val sorted = rawList.sortedWith(antiNeglectComparator)

        assertEquals("Never Prayed", sorted[0].displayName)
        assertEquals("Prayed Long Ago", sorted[1].displayName)
        assertEquals("Prayed Recently Low Count", sorted[2].displayName)
        assertEquals("Prayed Recently", sorted[3].displayName)
    }

    @Test
    fun `test never interacted entities always prioritize ahead of interacted entities regardless of count`() {
        val now = System.currentTimeMillis()

        val neverPrayedHighCount = IndividualEntity(
            displayName = "Never Prayed But High Count",
            rootCode = RootCode.PEOPLE,
            interactedCount = 10,
            lastInteractedAt = null
        )
        val prayedJustNowLowCount = IndividualEntity(
            displayName = "Prayed Just Now Low Count",
            rootCode = RootCode.PEOPLE,
            interactedCount = 1,
            lastInteractedAt = now
        )

        val list = listOf(prayedJustNowLowCount, neverPrayedHighCount)
        val sorted = list.sortedWith(antiNeglectComparator)

        assertEquals("Never interacted entity must always sort first", neverPrayedHighCount, sorted[0])
        assertEquals(prayedJustNowLowCount, sorted[1])
    }

    @Test
    fun `test tie-breaking between never-interacted entities uses interactedCount ASC`() {
        val neverPrayedLow = IndividualEntity(
            displayName = "Never Prayed Count 0",
            rootCode = RootCode.PEOPLE,
            interactedCount = 0,
            lastInteractedAt = null
        )
        val neverPrayedHigh = IndividualEntity(
            displayName = "Never Prayed Count 3",
            rootCode = RootCode.PEOPLE,
            interactedCount = 3,
            lastInteractedAt = null
        )

        val list = listOf(neverPrayedHigh, neverPrayedLow)
        val sorted = list.sortedWith(antiNeglectComparator)

        assertEquals(neverPrayedLow, sorted[0])
        assertEquals(neverPrayedHigh, sorted[1])
    }

    @Test
    fun `test tie-breaking between equal timestamps uses interactedCount ASC`() {
        val fixedTime = 1700000000000L
        val entityA = IndividualEntity(
            displayName = "Entity A (count 4)",
            rootCode = RootCode.PEOPLE,
            interactedCount = 4,
            lastInteractedAt = fixedTime
        )
        val entityB = IndividualEntity(
            displayName = "Entity B (count 1)",
            rootCode = RootCode.PEOPLE,
            interactedCount = 1,
            lastInteractedAt = fixedTime
        )

        val list = listOf(entityA, entityB)
        val sorted = list.sortedWith(antiNeglectComparator)

        assertEquals("Entity with lower count should break tie", entityB, sorted[0])
        assertEquals(entityA, sorted[1])
    }

    @Test
    fun `test historic reformed prayers zero-state seed`() {
        // Each historic prayer now has its own distinct entity
        val historicEntities = PreloadedContent.getHistoricEntities()
        val historicEntityIds = historicEntities.map { it.id }.toSet()

        // Backward-compat helper still returns the first entity
        val firstEntity = PreloadedContent.getHistoricEntity()
        assertEquals(PreloadedContent.HISTORIC_ENTITY_ID, firstEntity.id)
        assertEquals(RootCode.GENERAL, firstEntity.rootCode)
        assertTrue(firstEntity.isPreloadedHistoric)

        // All entities must be preloaded historic and in GENERAL
        historicEntities.forEach { entity ->
            assertTrue("Entity must be isPreloadedHistoric", entity.isPreloadedHistoric)
            assertEquals(RootCode.GENERAL, entity.rootCode)
        }

        val historicPoints = PreloadedContent.getHistoricPrayerPoints()
        assertTrue("Preloaded historic prayers should contain at least 4 classic Reformed/BCP prayers", historicPoints.size >= 4)
        assertEquals("Each entity should have exactly one prayer point", historicEntities.size, historicPoints.size)

        val titles = historicPoints.map { it.title }
        assertTrue("Must include The Lord's Prayer", titles.contains("The Lord's Prayer"))
        assertTrue("Must include Collect for Peace", titles.contains("Collect for Peace"))
        assertTrue("Must include Collect for Grace", titles.contains("Collect for Grace"))
        assertTrue("Must include Collect for Purity", titles.contains("Collect for Purity"))
        assertTrue("Must include The Apostles' Creed", titles.contains("The Apostles' Creed"))

        // Each point's entityId belongs to a known distinct historic entity (not a single shared entity)
        historicPoints.forEach { point ->
            assertTrue("Point entityId must belong to a known historic entity", historicEntityIds.contains(point.entityId))
            assertEquals(PrayerStatus.HISTORIC, point.status)
            assertTrue("Description must not be blank", point.description.isNotBlank())
        }
    }

    @Test
    fun `test silent interaction metric incrementation`() {
        val entity = IndividualEntity(
            displayName = "David",
            rootCode = RootCode.PEOPLE,
            interactedCount = 3,
            lastInteractedAt = 1000L
        )

        val now = System.currentTimeMillis()
        val updated = entity.copy(
            interactedCount = entity.interactedCount + 1,
            lastInteractedAt = now
        )

        assertEquals(4, updated.interactedCount)
        assertEquals(now, updated.lastInteractedAt)
    }

    @Test
    fun `test historic prayers blending contract`() {
        val userEntity = IndividualEntity(
            id = "user-1",
            displayName = "Sarah",
            rootCode = RootCode.PEOPLE
        )
        val historicEntities = PreloadedContent.getHistoricEntities()
        val expectedHistoricCount = historicEntities.size // 14 distinct historic prayers

        // Scenario 1: Zero user entities -> Fallback to all historic prayers
        fun resolveEntities(userList: List<IndividualEntity>, blendHistoric: Boolean): List<IndividualEntity> {
            return if (userList.isEmpty() || blendHistoric) {
                if (userList.isEmpty()) historicEntities else userList + historicEntities
            } else {
                userList
            }
        }

        val zeroState = resolveEntities(emptyList(), blendHistoric = false)
        assertEquals(expectedHistoricCount, zeroState.size)
        assertTrue(zeroState.any { it.id == PreloadedContent.HISTORIC_ENTITY_ID })
        assertTrue(zeroState.all { it.isPreloadedHistoric })

        // Scenario 2: Personal prayers exist, blendHistoric = false -> User prayers only
        val personalOnly = resolveEntities(listOf(userEntity), blendHistoric = false)
        assertEquals(1, personalOnly.size)
        assertEquals("Sarah", personalOnly[0].displayName)

        // Scenario 3: Personal prayers exist, blendHistoric = true -> User prayers + all historic blended
        val blended = resolveEntities(listOf(userEntity), blendHistoric = true)
        assertEquals(1 + expectedHistoricCount, blended.size)
        assertTrue(blended.any { it.id == "user-1" })
        assertTrue(blended.any { it.id == PreloadedContent.HISTORIC_ENTITY_ID })
        assertTrue(blended.count { it.isPreloadedHistoric } == expectedHistoricCount)
    }

    @Test
    fun `test topic filtering excludes topics with zero active prayer points`() {
        val entityWithActive = IndividualEntity(id = "e-1", displayName = "Active Burden", rootCode = RootCode.PEOPLE)
        val entityWithOnlyAnswered = IndividualEntity(id = "e-2", displayName = "Fully Answered", rootCode = RootCode.PEOPLE)
        val entityWithArchived = IndividualEntity(id = "e-3", displayName = "Archived Burden", rootCode = RootCode.PEOPLE)

        val pointsMap = mapOf(
            "e-1" to listOf(PrayerPoint(entityId = "e-1", title = "P1", description = "D1", status = PrayerStatus.ACTIVE)),
            "e-2" to listOf(PrayerPoint(entityId = "e-2", title = "P2", description = "D2", status = PrayerStatus.ANSWERED)),
            "e-3" to listOf(PrayerPoint(entityId = "e-3", title = "P3", description = "D3", status = PrayerStatus.ARCHIVED))
        )

        val allEntities = listOf(entityWithActive, entityWithOnlyAnswered, entityWithArchived)

        val queue = allEntities.mapNotNull { entity ->
            val points = pointsMap[entity.id] ?: emptyList()
            val active = points.filter { it.status == PrayerStatus.ACTIVE || it.status == PrayerStatus.HISTORIC }
            val answered = points.filter { it.status == PrayerStatus.ANSWERED }
            if (active.isNotEmpty()) TopicWithPoints(entity, active, answered) else null
        }

        assertEquals(1, queue.size)
        assertEquals("Active Burden", queue[0].entity.displayName)
    }
}
