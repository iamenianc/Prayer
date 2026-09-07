package au.prayer.app

import au.prayer.app.data.models.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AntiNeglectQueueTest {

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

        // Comparator mirroring SQL:
        // e.last_interacted_at IS NOT NULL ASC, e.last_interacted_at ASC, e.interacted_count ASC
        val comparator = Comparator<IndividualEntity> { a, b ->
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

        val sorted = rawList.sortedWith(comparator)

        assertEquals("Never Prayed", sorted[0].displayName)
        assertEquals("Prayed Long Ago", sorted[1].displayName)
        assertEquals("Prayed Recently Low Count", sorted[2].displayName)
        assertEquals("Prayed Recently", sorted[3].displayName)
    }

    @Test
    fun `test historic reformed prayers zero-state seed`() {
        val historicEntity = PreloadedContent.getHistoricEntity()
        assertEquals(PreloadedContent.HISTORIC_ENTITY_ID, historicEntity.id)
        assertEquals(RootCode.GENERAL, historicEntity.rootCode)
        assertTrue(historicEntity.isPreloadedHistoric)

        val historicPoints = PreloadedContent.getHistoricPrayerPoints()
        assertTrue("Preloaded historic prayers should contain at least 4 classic Reformed/BCP prayers", historicPoints.size >= 4)

        val titles = historicPoints.map { it.title }
        assertTrue("Must include The Lord's Prayer", titles.contains("The Lord's Prayer"))
        assertTrue("Must include Collect for Peace", titles.contains("Collect for Peace"))
        assertTrue("Must include Collect for Grace", titles.contains("Collect for Grace"))
        assertTrue("Must include The Apostles' Creed", titles.contains("The Apostles' Creed"))
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
}
