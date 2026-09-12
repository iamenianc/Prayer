package au.prayer.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import au.prayer.app.data.local.PrayerRepository
import au.prayer.app.data.models.LibraryContent
import au.prayer.app.ui.gestures.edgeSwipeRight
import au.prayer.app.ui.theme.FlatSquareShape
import au.prayer.app.ui.theme.PrayerColors
import au.prayer.app.ui.theme.PrayerSpacing
import au.prayer.app.ui.theme.PrayerTypography

/**
 * Theological Library Bookshelf (LibraryScreen).
 * Presents the catalog of classical treatises and historic devotionals
 * in the Modern Leatherbound Folio aesthetic.
 */
@Composable
fun LibraryScreen(
    colors: PrayerColors,
    typography: PrayerTypography,
    repository: PrayerRepository,
    onOpenVolume: (volumeId: String, startSectionNumber: Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val volumes = remember {
        try {
            listOf(LibraryContent.calvinVolume)
        } catch (e: Exception) {
            emptyList()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.leatherActive)
            .border(width = 1.dp, color = colors.leatherPerimeterBorder, shape = FlatSquareShape)
            .edgeSwipeRight { onBack() },
        contentAlignment = Alignment.TopCenter
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = FlatSquareShape,
                color = colors.background,
                contentColor = colors.textPrimary,
                tonalElevation = PrayerSpacing.elevationCard,
                shadowElevation = PrayerSpacing.elevationCard,
                border = BorderStroke(PrayerSpacing.hairlineWidth, colors.borderSubtle)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = PrayerSpacing.large, vertical = PrayerSpacing.medium),
                    verticalArrangement = Arrangement.spacedBy(PrayerSpacing.large)
                ) {
                    // Top Navigation Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = PrayerSpacing.small, bottom = PrayerSpacing.small),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "‹  Home",
                                style = typography.marginStatus.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.inkMuted,
                                modifier = Modifier
                                    .clickable(onClick = onBack)
                                    .padding(vertical = PrayerSpacing.small, horizontal = PrayerSpacing.small)
                            )

                            Text(
                                text = "❧",
                                fontFamily = FontFamily.Serif,
                                fontSize = 18.sp,
                                color = colors.leatherActive.copy(alpha = 0.75f)
                            )
                        }
                    }

                    // Frontispiece Inscription
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = PrayerSpacing.small),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "THEOLOGICAL LIBRARY",
                                style = typography.frontispieceHeader.copy(
                                    letterSpacing = 2.5.sp,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = colors.leatherActive,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(PrayerSpacing.extraSmall))

                            Text(
                                text = "Classical Treatises on Prayer & Devotion",
                                style = typography.subjectHeader.copy(
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = colors.inkMuted,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(PrayerSpacing.medium))

                            // Ornamental Dividing Hairline
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(1.dp)
                                    .background(colors.borderSubtle),
                                verticalAlignment = Alignment.CenterVertically
                            ) {}
                        }
                    }

                    // Volume Shelf / Bookplates
                    items(volumes, key = { it.volumeId }) { volume ->
                        val progress = remember(volume.volumeId) {
                            repository.getReadingProgress(volume.volumeId)
                        }
                        val currentSection = progress?.lastSectionNumber ?: 1

                        Surface(
                            onClick = { onOpenVolume(volume.volumeId, currentSection) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = FlatSquareShape,
                            color = colors.surface,
                            contentColor = colors.textPrimary,
                            tonalElevation = PrayerSpacing.elevationSubtle,
                            shadowElevation = PrayerSpacing.elevationSubtle,
                            border = BorderStroke(PrayerSpacing.hairlineWidth, colors.border)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(PrayerSpacing.large),
                                verticalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                            ) {
                                // Volume Label
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "VOLUME I · CLASSICAL TREATISE",
                                        style = typography.caption.copy(
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = colors.leatherActive
                                    )

                                    if (progress != null && progress.lastSectionNumber > 1) {
                                        Text(
                                            text = "Section ${progress.lastSectionNumber} of 52",
                                            style = typography.marginStatus.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = colors.inkAnswered
                                        )
                                    }
                                }

                                // Volume Title
                                Text(
                                    text = volume.title,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 25.sp,
                                    color = colors.textPrimary
                                )

                                // Subtitle
                                if (volume.subtitle.isNotBlank()) {
                                    Text(
                                        text = volume.subtitle,
                                        fontFamily = FontFamily.Serif,
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 14.sp,
                                        color = colors.inkMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                // Author and Work
                                Text(
                                    text = "${volume.author} · ${volume.work}",
                                    style = typography.caption.copy(fontSize = 12.5.sp),
                                    color = colors.textPrimary.copy(alpha = 0.85f)
                                )

                                // Translator
                                if (volume.translator.isNotBlank()) {
                                    Text(
                                        text = "Translated by ${volume.translator}",
                                        style = typography.caption.copy(fontSize = 11.5.sp),
                                        color = colors.inkMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(PrayerSpacing.small))

                                // Footer with scope metrics and read trigger
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = PrayerSpacing.extraSmall),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${volume.divisions.size} Divisions · ${volume.sections.size} Sections",
                                        style = typography.caption.copy(
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = colors.inkMuted.copy(alpha = 0.80f)
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = if (progress != null && progress.lastSectionNumber > 1) "Continue reading" else "Begin reading",
                                            style = typography.caption.copy(
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = colors.leatherActive
                                        )
                                        Text(
                                            text = "›",
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 16.sp,
                                            color = colors.leatherActive
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Colophon
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = PrayerSpacing.extraLarge),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "❧",
                                fontFamily = FontFamily.Serif,
                                fontSize = 16.sp,
                                color = colors.leatherActive.copy(alpha = 0.50f)
                            )
                            Spacer(modifier = Modifier.height(PrayerSpacing.extraSmall))
                            Text(
                                text = "Public Domain Classical Devotional Literature",
                                style = typography.caption.copy(fontSize = 10.5.sp),
                                color = colors.inkMuted.copy(alpha = 0.60f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
