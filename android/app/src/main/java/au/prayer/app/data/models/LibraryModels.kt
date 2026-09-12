package au.prayer.app.data.models

import kotlinx.serialization.Serializable

/**
 * Represents a volume in the Theological Library.
 */
@Serializable
data class LibraryVolume(
    val schemaVersion: Int = 1,
    val volumeId: String,
    val title: String,
    val subtitle: String = "",
    val author: String,
    val work: String = "",
    val translator: String = "",
    val language: String = "en",
    val publicDomain: Boolean = true,
    val sourceUrl: String = "",
    val divisions: List<VolumeDivision> = emptyList(),
    val sections: List<VolumeSection> = emptyList()
)

/**
 * High-level grouping or division within a volume (e.g., Roman numerals I through VIII).
 */
@Serializable
data class VolumeDivision(
    val division: String,
    val description: String,
    val startSection: Int,
    val endSection: Int
)

/**
 * A discrete section of a treatise with an outline summary and flowing paragraph blocks.
 */
@Serializable
data class VolumeSection(
    val sectionNumber: Int,
    val outlineSummary: String = "",
    val paragraphs: List<String> = emptyList()
)

/**
 * Persisted user reading progress for a library volume.
 */
@Serializable
data class ReadingProgress(
    val volumeId: String,
    val lastSectionNumber: Int = 1,
    val lastScrollOffset: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
