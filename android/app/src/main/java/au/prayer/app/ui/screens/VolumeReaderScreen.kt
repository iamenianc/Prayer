package au.prayer.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import au.prayer.app.data.local.PrayerRepository
import au.prayer.app.data.models.LibraryContent
import au.prayer.app.data.models.LibraryVolume
import au.prayer.app.data.models.ReadingProgress
import au.prayer.app.data.models.VolumeSection
import au.prayer.app.ui.components.SilkMarkerRibbon
import au.prayer.app.ui.gestures.calculateZoomScale
import au.prayer.app.ui.gestures.pinchToZoom
import au.prayer.app.ui.gestures.prayerSwipeGestures
import au.prayer.app.ui.theme.FlatSquareShape
import au.prayer.app.ui.theme.PrayerColors
import au.prayer.app.ui.theme.PrayerSpacing
import au.prayer.app.ui.theme.PrayerTypography
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Modern Folio Volume Reader Screen.
 * Immersive, baseline-synchronized reading canvas for classical theological treatises.
 */
@Composable
fun VolumeReaderScreen(
    volumeId: String,
    startSectionNumber: Int,
    colors: PrayerColors,
    typography: PrayerTypography,
    repository: PrayerRepository,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val volume: LibraryVolume = remember(volumeId) {
        try {
            LibraryContent.calvinVolume
        } catch (e: Exception) {
            LibraryContent.getAllVolumes().firstOrNull { it.volumeId == volumeId }
                ?: LibraryVolume(volumeId = volumeId, title = "Volume", author = "Unknown")
        }
    }

    var currentSectionNumber by remember { mutableIntStateOf(startSectionNumber.coerceIn(1, (volume.sections.size).coerceAtLeast(1))) }
    val initialProgress = remember(volume.volumeId) {
        repository.getReadingProgress(volume.volumeId)
    }
    var bookmarkedSectionNumber by remember(volume.volumeId) {
        mutableStateOf(initialProgress?.lastSectionNumber ?: startSectionNumber)
    }
    val isRibbonPinned = (currentSectionNumber == bookmarkedSectionNumber)
    var showTableOfContents by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    val initialZoom = remember { repository.getLibraryZoomScale() }
    var zoomScale by remember { mutableFloatStateOf(initialZoom) }
    var isZooming by remember { mutableStateOf(false) }
    var showZoomPill by remember { mutableStateOf(false) }

    LaunchedEffect(isZooming) {
        if (isZooming) {
            showZoomPill = true
        } else if (showZoomPill) {
            delay(1500)
            showZoomPill = false
        }
    }

    // Baseline cadence scaled dynamically with text zoom scale (Baseline Synchronization Law)
    val baselineSpacingPx = with(density) { (28.sp * zoomScale).toPx() }

    val currentSection: VolumeSection? = remember(currentSectionNumber, volume) {
        volume.sections.find { it.sectionNumber == currentSectionNumber }
            ?: volume.sections.firstOrNull()
    }

    // Persist reading progress when section changes
    LaunchedEffect(currentSectionNumber) {
        scrollState.scrollTo(0)
        repository.saveReadingProgress(
            ReadingProgress(
                volumeId = volume.volumeId,
                lastSectionNumber = currentSectionNumber,
                lastScrollOffset = 0,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.leatherActive)
            .border(width = 1.dp, color = colors.leatherPerimeterBorder, shape = FlatSquareShape)
            .prayerSwipeGestures(
                onSwipeLeft = {
                    if (currentSectionNumber < volume.sections.size) {
                        currentSectionNumber += 1
                    }
                },
                onSwipeRight = {
                    if (currentSectionNumber > 1) {
                        currentSectionNumber -= 1
                    }
                },
                onSwipeDown = onBack,
                onEdgeSwipeRight = onBack
            ),
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
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top App Ledger Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = PrayerSpacing.large),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "‹  Library",
                            style = typography.marginStatus.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = colors.inkMuted,
                            modifier = Modifier
                                .clickable(onClick = onBack)
                                .padding(vertical = PrayerSpacing.small)
                        )

                        // Table of contents trigger in center
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.small),
                            modifier = Modifier
                                .clickable { showTableOfContents = true }
                                .padding(horizontal = PrayerSpacing.small, vertical = PrayerSpacing.extraSmall)
                        ) {
                            Text(
                                text = "§ ${currentSectionNumber} of ${volume.sections.size}",
                                style = typography.marginStatus.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = colors.leatherActive
                            )
                            Text(
                                text = "▾",
                                fontSize = 12.sp,
                                color = colors.inkMuted
                            )
                        }

                        if (zoomScale != 1.0f) {
                            Text(
                                text = "${(zoomScale * 100).roundToInt()}% ↺",
                                style = typography.marginStatus.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.leatherActive,
                                modifier = Modifier
                                    .clickable {
                                        zoomScale = 1.0f
                                        repository.saveLibraryZoomScale(1.0f)
                                        showZoomPill = true
                                    }
                                    .padding(horizontal = PrayerSpacing.extraSmall, vertical = PrayerSpacing.extraSmall)
                            )
                        }

                        // Bookmark Silk Marker Ribbon Tab
                        SilkMarkerRibbon(
                            isPinned = isRibbonPinned,
                            onTogglePin = {
                                if (isRibbonPinned) {
                                    bookmarkedSectionNumber = 0
                                } else {
                                    bookmarkedSectionNumber = currentSectionNumber
                                    repository.saveReadingProgress(
                                        ReadingProgress(
                                            volumeId = volume.volumeId,
                                            lastSectionNumber = currentSectionNumber,
                                            lastScrollOffset = scrollState.value,
                                            updatedAt = System.currentTimeMillis()
                                        )
                                    )
                                }
                            },
                            color = colors.ribbonPrimary
                        )
                    }

                    // Divider Hairline
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.borderSubtle)
                    )

                    // Reading Canvas with feint horizontal rules
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .pinchToZoom(
                                onZoomChange = { factor ->
                                    isZooming = true
                                    zoomScale = calculateZoomScale(zoomScale, factor)
                                },
                                onZoomStart = {
                                    isZooming = true
                                },
                                onZoomEnd = {
                                    isZooming = false
                                    repository.saveLibraryZoomScale(zoomScale)
                                }
                            )
                            .drawBehind {
                                val canvasHeight = size.height
                                val canvasWidth = size.width
                                val ruleColor = colors.borderSubtle.copy(alpha = 0.45f)
                                var currentY = baselineSpacingPx
                                while (currentY < canvasHeight + baselineSpacingPx * 2) {
                                    drawLine(
                                        color = ruleColor,
                                        start = Offset(0f, currentY),
                                        end = Offset(canvasWidth, currentY),
                                        strokeWidth = 1f
                                    )
                                    currentY += baselineSpacingPx
                                }
                            }
                            .verticalScroll(scrollState)
                            .padding(horizontal = PrayerSpacing.extraLarge, vertical = PrayerSpacing.large)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(PrayerSpacing.large)
                        ) {
                            // Section Head
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "SECTION ${currentSectionNumber}",
                                    style = typography.frontispieceHeader.copy(
                                        letterSpacing = 2.sp,
                                        fontSize = (12 * zoomScale).sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = colors.leatherActive,
                                    textAlign = TextAlign.Center
                                )

                                if (currentSection != null && currentSection.outlineSummary.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(PrayerSpacing.small))
                                    Text(
                                        text = currentSection.outlineSummary,
                                        fontFamily = FontFamily.Serif,
                                        fontStyle = FontStyle.Italic,
                                        fontSize = (14.5f * zoomScale).sp,
                                        lineHeight = (22 * zoomScale).sp,
                                        color = colors.inkMuted,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.height(PrayerSpacing.medium))

                                Text(
                                    text = "❧",
                                    fontFamily = FontFamily.Serif,
                                    fontSize = (14 * zoomScale).sp,
                                    color = colors.leatherActive.copy(alpha = 0.50f)
                                )
                            }

                            // Flowing Paragraphs
                            if (currentSection != null) {
                                currentSection.paragraphs.forEach { para ->
                                    Text(
                                        text = para,
                                        fontFamily = FontFamily.Serif,
                                        fontSize = (16.5f * zoomScale).sp,
                                        lineHeight = (28 * zoomScale).sp,
                                        color = colors.textPrimary,
                                        textAlign = TextAlign.Justify
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(PrayerSpacing.extraLarge))
                        }
                    }

                    // Bottom Navigation Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .background(colors.surface)
                            .border(BorderStroke(PrayerSpacing.hairlineWidth, colors.borderSubtle), FlatSquareShape)
                            .padding(horizontal = PrayerSpacing.large),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (currentSectionNumber > 1) {
                            Text(
                                text = "‹  Previous",
                                style = typography.marginStatus.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = colors.leatherActive,
                                modifier = Modifier
                                    .clickable { currentSectionNumber -= 1 }
                                    .padding(vertical = PrayerSpacing.small, horizontal = PrayerSpacing.small)
                            )
                        } else {
                            Spacer(modifier = Modifier.width(60.dp))
                        }

                        Text(
                            text = "Contents",
                            style = typography.caption.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = colors.inkMuted,
                            modifier = Modifier
                                .clickable { showTableOfContents = true }
                                .padding(vertical = PrayerSpacing.small, horizontal = PrayerSpacing.small)
                        )

                        if (currentSectionNumber < volume.sections.size) {
                            Text(
                                text = "Next  ›",
                                style = typography.marginStatus.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = colors.leatherActive,
                                modifier = Modifier
                                    .clickable { currentSectionNumber += 1 }
                                    .padding(vertical = PrayerSpacing.small, horizontal = PrayerSpacing.small)
                            )
                        } else {
                            Spacer(modifier = Modifier.width(60.dp))
                        }
                    }
                }
            }

            // Floating Zoom Indicator Pill
            AnimatedVisibility(
                visible = isZooming || showZoomPill,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 68.dp)
            ) {
                Surface(
                    onClick = {
                        zoomScale = 1.0f
                        repository.saveLibraryZoomScale(1.0f)
                        showZoomPill = true
                    },
                    shape = FlatSquareShape,
                    color = colors.leatherActive.copy(alpha = 0.92f),
                    contentColor = colors.background,
                    tonalElevation = PrayerSpacing.elevationCard,
                    shadowElevation = PrayerSpacing.elevationCard,
                    border = BorderStroke(1.dp, colors.borderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${(zoomScale * 100).roundToInt()}%",
                            style = typography.caption.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = colors.background
                        )
                        if (zoomScale != 1.0f) {
                            Text(
                                text = "• Reset",
                                style = typography.caption.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.background.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Table of Contents Modal Dialog
    if (showTableOfContents) {
        Dialog(
            onDismissRequest = { showTableOfContents = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.leatherActive.copy(alpha = 0.70f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showTableOfContents = false }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.85f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {} // Prevent dismiss when tapping inside
                        ),
                    shape = FlatSquareShape,
                    color = colors.background,
                    contentColor = colors.textPrimary,
                    tonalElevation = PrayerSpacing.elevationModal,
                    shadowElevation = PrayerSpacing.elevationModal,
                    border = BorderStroke(1.5.dp, colors.leatherActive)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(PrayerSpacing.large)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "TABLE OF CONTENTS",
                                style = typography.frontispieceHeader.copy(
                                    letterSpacing = 2.sp,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colors.leatherActive
                            )

                            Text(
                                text = "✕",
                                fontSize = 16.sp,
                                color = colors.inkMuted,
                                modifier = Modifier
                                    .clickable { showTableOfContents = false }
                                    .padding(PrayerSpacing.small)
                            )
                        }

                        Text(
                            text = "${volume.title} (${volume.sections.size} Sections)",
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontSize = 13.sp,
                            color = colors.inkMuted
                        )

                        Spacer(modifier = Modifier.height(PrayerSpacing.medium))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colors.borderSubtle)
                        )

                        Spacer(modifier = Modifier.height(PrayerSpacing.small))

                        // Scrollable list of 52 sections
                        val tocListState = rememberLazyListState(
                            initialFirstVisibleItemIndex = (currentSectionNumber - 1).coerceAtLeast(0)
                        )

                        LazyColumn(
                            state = tocListState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                        ) {
                            items(volume.sections, key = { it.sectionNumber }) { sec ->
                                val isSelected = sec.sectionNumber == currentSectionNumber

                                Surface(
                                    onClick = {
                                        currentSectionNumber = sec.sectionNumber
                                        showTableOfContents = false
                                    },
                                    shape = FlatSquareShape,
                                    color = if (isSelected) colors.surface else colors.background,
                                    border = BorderStroke(
                                        if (isSelected) 1.dp else PrayerSpacing.hairlineWidth,
                                        if (isSelected) colors.leatherActive else colors.borderSubtle
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(PrayerSpacing.medium),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.medium)
                                    ) {
                                        val isBookmarked = sec.sectionNumber == bookmarkedSectionNumber
                                        Column {
                                            Text(
                                                text = "§ ${sec.sectionNumber}",
                                                style = typography.caption.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                ),
                                                color = if (isSelected) colors.leatherActive else colors.textPrimary
                                            )
                                            if (isBookmarked) {
                                                Text(
                                                    text = "• BOOKMARK",
                                                    style = typography.caption.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    ),
                                                    color = colors.ribbonPrimary
                                                )
                                            }
                                        }

                                        Text(
                                            text = sec.outlineSummary.ifBlank { "Section ${sec.sectionNumber}" },
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp,
                                            color = if (isSelected) colors.textPrimary else colors.inkMuted,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
