package au.prayer.app.data.models

import kotlinx.serialization.Serializable
import java.util.UUID

enum class RootCode(val displayTitle: String, val sortOrder: Int) {
    PEOPLE("People", 1),
    GROUPS("Groups", 2),
    GENERAL("General", 3)
}

enum class PrayerStatus {
    ACTIVE,
    ANSWERED,
    ARCHIVED,
    HISTORIC
}

enum class LocaleDialect(val code: String, val displayName: String) {
    EN_AU_UK("EN_AU_UK", "English (Australian / UK)"),
    EN_US("EN_US", "US English")
}

enum class ThemeMode(val displayName: String) {
    MORNING_LIGHT("Morning Light"),
    QUIET_NIGHT("Quiet Night")
}

enum class TextScale(val displayName: String) {
    LARGE("Large"),
    REGULAR("Regular"),
    COMPACT("Compact")
}

@Serializable
data class IndividualEntity(
    val id: String = UUID.randomUUID().toString(),
    val rootCode: RootCode,
    val displayName: String,
    val contextDescription: String = "",
    val isPreloadedHistoric: Boolean = false,
    val interactedCount: Int = 0,
    val lastInteractedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class PrayerPoint(
    val id: String = UUID.randomUUID().toString(),
    val entityId: String,
    val title: String,
    val description: String,
    val status: PrayerStatus = PrayerStatus.ACTIVE,
    val interactedCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastInteractedAt: Long? = null,
    val answeredAt: Long? = null,
    val answeredTestimony: String? = null
)

@Serializable
data class TopicWithPoints(
    val entity: IndividualEntity,
    val activePoints: List<PrayerPoint>,
    val answeredPoints: List<PrayerPoint>
)

@Serializable
data class AppConfig(
    val localeDialect: LocaleDialect = LocaleDialect.EN_AU_UK,
    val themeMode: ThemeMode = ThemeMode.MORNING_LIGHT,
    val textScale: TextScale = TextScale.LARGE,
    val blendHistoricPrayers: Boolean = false
)
