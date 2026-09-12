package au.prayer.app.data.models

import kotlinx.serialization.Serializable

/**
 * Portable payload containing personal entities, prayer points, library progress,
 * and application configuration for password-protected vault backup and restore.
 */
@Serializable
data class VaultBackupPayload(
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0.0",
    val config: AppConfig = AppConfig(),
    val entities: List<IndividualEntity> = emptyList(),
    val prayerPoints: List<PrayerPoint> = emptyList(),
    val libraryProgress: List<ReadingProgress> = emptyList()
)

/**
 * Result summary produced after restoring a vault backup.
 */
@Serializable
data class RestoreSummary(
    val entitiesImported: Int,
    val pointsImported: Int,
    val readingProgressImported: Int,
    val isReplaced: Boolean
)
