package au.prayer.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import au.prayer.app.ui.components.SilkMarkerRibbon
import au.prayer.app.ui.gestures.prayerSwipeGestures
import au.prayer.app.ui.theme.FlatSquareShape
import au.prayer.app.ui.theme.PrayerColors
import au.prayer.app.ui.theme.PrayerSpacing
import au.prayer.app.ui.theme.PrayerTypography
import kotlinx.coroutines.delay

/**
 * Modern Leatherbound Folio Frontispiece (HomeScreen).
 * Captures the quiet dignity and physical romance of opening an authentic leatherbound prayer journal:
 * - Outer Perimeter: Bound in the active leather dye tone (Saddle Tan, Cordovan, Hunter Forest, or Obsidian Hide).
 * - Central Folio Canvas: Cream vellum writing canvas with fine stationery double-hairline bookplate framing.
 * - Upper Half: Contemplative Observation Zone (§8.2) with frontispiece typography, printer's fleuron ornament,
 *   scripture citation, and Silk Marker Ribbon bookmark sewn into the top headband.
 * - Lower Half: Ergonomic Action Zone (§8.2, §8.4) with distinct, tactile bookplate triggers separated by generous 12dp gutters.
 * - Opening Revelation: Serene, interruptible cold-start revelation that parts the leather cover into the illuminated frontispiece.
 */
@Composable
fun HomeScreen(
    colors: PrayerColors,
    typography: PrayerTypography,
    onStartPraying: () -> Unit,
    onOpenJournal: () -> Unit,
    onAddPrayerPoints: () -> Unit,
    onLogPrayerPoints: () -> Unit = onAddPrayerPoints,
    isInitialLaunch: Boolean = false,
    onInitialLaunchComplete: () -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(false) }
    var isRibbonPinned by remember { mutableStateOf(false) }
    var startCoverFade by remember { mutableStateOf(false) }
    var isRevelationComplete by remember { mutableStateOf(!isInitialLaunch) }

    LaunchedEffect(Unit) {
        isVisible = true
        if (isInitialLaunch) {
            delay(200)
            startCoverFade = true
            delay(500)
            isRevelationComplete = true
            onInitialLaunchComplete()
        }
    }

    // Gentle staggered entrance animations for liturgical solemnity
    val titleAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = if (isInitialLaunch) 300 else 0, easing = FastOutSlowInEasing),
        label = "TitleAlpha"
    )
    val titleTranslationY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 12f,
        animationSpec = tween(durationMillis = 400, delayMillis = if (isInitialLaunch) 300 else 0, easing = FastOutSlowInEasing),
        label = "TitleTranslationY"
    )

    val action1Alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 350, delayMillis = if (isInitialLaunch) 400 else 100, easing = FastOutSlowInEasing),
        label = "Action1Alpha"
    )
    val action1TranslationY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 16f,
        animationSpec = tween(durationMillis = 350, delayMillis = if (isInitialLaunch) 400 else 100, easing = FastOutSlowInEasing),
        label = "Action1TranslationY"
    )

    val action2Alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 350, delayMillis = if (isInitialLaunch) 460 else 160, easing = FastOutSlowInEasing),
        label = "Action2Alpha"
    )
    val action2TranslationY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 16f,
        animationSpec = tween(durationMillis = 350, delayMillis = if (isInitialLaunch) 460 else 160, easing = FastOutSlowInEasing),
        label = "Action2TranslationY"
    )

    val action3Alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 350, delayMillis = if (isInitialLaunch) 520 else 220, easing = FastOutSlowInEasing),
        label = "Action3Alpha"
    )
    val action3TranslationY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 16f,
        animationSpec = tween(durationMillis = 350, delayMillis = if (isInitialLaunch) 520 else 220, easing = FastOutSlowInEasing),
        label = "Action3TranslationY"
    )

    val coverAlpha by animateFloatAsState(
        targetValue = if (startCoverFade) 0f else 1f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "CoverAlpha"
    )

    // Root Folio Container: Framed in the authentic active leather dye tone
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.leatherActive)
            .border(width = 1.dp, color = colors.leatherPerimeterBorder, shape = FlatSquareShape),
        contentAlignment = Alignment.TopCenter
    ) {
        // Central Vellum Frontispiece Canvas (constrained measure on large/foldable screens)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .padding(
                    start = 4.dp,
                    end = 4.dp,
                    top = 4.dp,
                    bottom = 4.dp
                )
                .background(colors.paperBackground)
                .border(width = PrayerSpacing.hairlineWidth, color = colors.borderStrong, shape = FlatSquareShape)
                .safeDrawingPadding()
                .prayerSwipeGestures(
                    onSwipeLeft = onOpenJournal
                )
        ) {
            // Classical Stationery Bookplate Framing
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .border(
                        width = PrayerSpacing.hairlineWidth,
                        color = colors.borderSubtle,
                        shape = FlatSquareShape
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = PrayerSpacing.large, vertical = PrayerSpacing.medium),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Half: Contemplative Frontispiece Observation Zone (§8.2)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.2f)
                            .graphicsLayer {
                                alpha = titleAlpha
                                translationY = titleTranslationY
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Frontispiece Inscription
                        Text(
                            text = "PRAY WITHOUT CEASING",
                            style = typography.frontispieceHeader.copy(
                                letterSpacing = 2.0.sp
                            ),
                            color = colors.inkMuted,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(PrayerSpacing.medium))

                        // Printer's Fleuron Ornament with flanking hairline rules (§6.3)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth(0.55f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(PrayerSpacing.hairlineWidth)
                                    .background(colors.leatherActive.copy(alpha = 0.35f))
                            )
                            Text(
                                text = " ❧ ",
                                fontFamily = FontFamily.Serif,
                                fontSize = 18.sp,
                                color = colors.leatherActive.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(PrayerSpacing.hairlineWidth)
                                    .background(colors.leatherActive.copy(alpha = 0.35f))
                            )
                        }

                        Spacer(modifier = Modifier.height(PrayerSpacing.medium))

                        // Evocative Scripture Epigraph
                        Text(
                            text = "“Rejoice always, pray without ceasing,\ngive thanks in all circumstances.”",
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontSize = 16.sp,
                            lineHeight = 26.sp,
                            color = colors.inkSecondary.copy(alpha = 0.90f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(PrayerSpacing.small))

                        Text(
                            text = "1 Thessalonians 5:16–18",
                            style = typography.caption.copy(letterSpacing = 0.5.sp),
                            color = colors.inkMuted.copy(alpha = 0.70f),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Lower Half: Comfortable Thumb-Reach Action Zone (§8.2, §8.4)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(bottom = PrayerSpacing.small),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Pathway 1: Start praying (Devotional Gateway - Elevated Sanctuary Plate)
                        Surface(
                            onClick = onStartPraying,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(62.dp)
                                .graphicsLayer {
                                    alpha = action1Alpha
                                    translationY = action1TranslationY
                                },
                            shape = FlatSquareShape,
                            color = colors.surface,
                            contentColor = colors.textPrimary,
                            tonalElevation = PrayerSpacing.elevationCard,
                            shadowElevation = PrayerSpacing.elevationCard,
                            border = BorderStroke(1.5.dp, colors.leatherActive.copy(alpha = 0.60f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = PrayerSpacing.large),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.medium)
                                ) {
                                    Text(
                                        text = "❧",
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 18.sp,
                                        color = colors.leatherActive.copy(alpha = 0.75f)
                                    )
                                    Text(
                                        text = "Start praying",
                                        style = typography.homeAction.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.textPrimary
                                    )
                                }
                                Text(
                                    text = "›",
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 20.sp,
                                    color = colors.leatherActive.copy(alpha = 0.65f)
                                )
                            }
                        }

                        // Pathway 2: Open Journal (Spiritual Records Directory - Stationery Plate)
                        Surface(
                            onClick = onOpenJournal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .graphicsLayer {
                                    alpha = action2Alpha
                                    translationY = action2TranslationY
                                },
                            shape = FlatSquareShape,
                            color = colors.surface,
                            contentColor = colors.textPrimary,
                            tonalElevation = PrayerSpacing.elevationSubtle,
                            shadowElevation = PrayerSpacing.elevationSubtle,
                            border = BorderStroke(PrayerSpacing.hairlineWidth, colors.border)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = PrayerSpacing.large),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.medium)
                                ) {
                                    Text(
                                        text = "•",
                                        fontSize = 18.sp,
                                        color = colors.inkMuted.copy(alpha = 0.65f)
                                    )
                                    Text(
                                        text = "Open Journal",
                                        style = typography.homeAction,
                                        color = colors.textPrimary
                                    )
                                }
                                Text(
                                    text = "›",
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 18.sp,
                                    color = colors.inkMuted.copy(alpha = 0.50f)
                                )
                            }
                        }

                        // Pathway 3: Add prayer points (Direct Lined Notepad Capture - Stationery Plate)
                        Surface(
                            onClick = onAddPrayerPoints,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .graphicsLayer {
                                    alpha = action3Alpha
                                    translationY = action3TranslationY
                                },
                            shape = FlatSquareShape,
                            color = colors.surface,
                            contentColor = colors.textPrimary,
                            tonalElevation = PrayerSpacing.elevationSubtle,
                            shadowElevation = PrayerSpacing.elevationSubtle,
                            border = BorderStroke(PrayerSpacing.hairlineWidth, colors.border)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = PrayerSpacing.large),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.medium)
                                ) {
                                    Text(
                                        text = "•",
                                        fontSize = 18.sp,
                                        color = colors.inkMuted.copy(alpha = 0.65f)
                                    )
                                    Text(
                                        text = "Add prayer points",
                                        style = typography.homeAction,
                                        color = colors.textPrimary
                                    )
                                }
                                Text(
                                    text = "+",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 18.sp,
                                    color = colors.inkMuted.copy(alpha = 0.50f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(PrayerSpacing.extraSmall))

                        // Discrete Marginal Navigation Hint
                        Text(
                            text = "‹  swipe left for Journal  ›",
                            style = typography.caption.copy(fontSize = 11.sp, letterSpacing = 0.5.sp),
                            color = colors.inkMuted.copy(alpha = 0.60f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Interactive Silk Marker Ribbon Tab anchored at top right (§2.3, §11.2)
            SilkMarkerRibbon(
                isPinned = isRibbonPinned,
                onTogglePin = { isRibbonPinned = !isRibbonPinned },
                color = colors.ribbonPrimary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 20.dp)
            )
        }

        // Serene, interruptible opening folio cover revelation on initial cold start
        if (isInitialLaunch && !isRevelationComplete) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = coverAlpha
                    }
                    .background(colors.leatherActive)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            isRevelationComplete = true
                            onInitialLaunchComplete()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "P · W · C",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        letterSpacing = 6.sp,
                        color = Color.Black.copy(alpha = 0.22f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "❧",
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        color = Color.Black.copy(alpha = 0.20f)
                    )
                }
            }
        }
    }
}
