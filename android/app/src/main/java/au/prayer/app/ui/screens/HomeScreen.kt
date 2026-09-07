package au.prayer.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import au.prayer.app.ui.gestures.prayerSwipeGestures
import au.prayer.app.ui.theme.FlatSquareShape
import au.prayer.app.ui.theme.PrayerColors
import au.prayer.app.ui.theme.PrayerSpacing
import au.prayer.app.ui.theme.PrayerTypography

@Composable
fun HomeScreen(
    colors: PrayerColors,
    typography: PrayerTypography,
    onStartPraying: () -> Unit,
    onOpenJournal: () -> Unit,
    onAddPrayerPoints: () -> Unit,
    onLogPrayerPoints: () -> Unit = onAddPrayerPoints
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Gentle staggered entrance animations for liturgical solemnity
    val slab1Alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 350, delayMillis = 0, easing = FastOutSlowInEasing),
        label = "Slab1Alpha"
    )
    val slab1TranslationY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 18f,
        animationSpec = tween(durationMillis = 350, delayMillis = 0, easing = FastOutSlowInEasing),
        label = "Slab1TranslationY"
    )

    val slab2Alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 350, delayMillis = 60, easing = FastOutSlowInEasing),
        label = "Slab2Alpha"
    )
    val slab2TranslationY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 18f,
        animationSpec = tween(durationMillis = 350, delayMillis = 60, easing = FastOutSlowInEasing),
        label = "Slab2TranslationY"
    )

    val slab3Alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 350, delayMillis = 120, easing = FastOutSlowInEasing),
        label = "Slab3Alpha"
    )
    val slab3TranslationY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 18f,
        animationSpec = tween(durationMillis = 350, delayMillis = 120, easing = FastOutSlowInEasing),
        label = "Slab3TranslationY"
    )

    // Blank edge-to-edge canvas with strictly three contiguous slabs
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .safeDrawingPadding()
            .prayerSwipeGestures(
                onSwipeLeft = onOpenJournal
            )
    ) {
        // Slab 1: Start praying
        Surface(
            onClick = onStartPraying,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .graphicsLayer {
                    alpha = slab1Alpha
                    translationY = slab1TranslationY
                },
            shape = FlatSquareShape,
            color = colors.surface,
            contentColor = colors.textPrimary,
            tonalElevation = 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = PrayerSpacing.large, vertical = PrayerSpacing.medium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Start praying",
                    style = typography.homeAction,
                    color = colors.textPrimary
                )
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = colors.border)

        // Slab 2: Open Journal
        Surface(
            onClick = onOpenJournal,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .graphicsLayer {
                    alpha = slab2Alpha
                    translationY = slab2TranslationY
                },
            shape = FlatSquareShape,
            color = colors.surface,
            contentColor = colors.textPrimary,
            tonalElevation = 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = PrayerSpacing.large, vertical = PrayerSpacing.medium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Open Journal",
                    style = typography.homeAction,
                    color = colors.textPrimary
                )
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = colors.border)

        // Slab 3: Add prayer points
        Surface(
            onClick = onAddPrayerPoints,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .graphicsLayer {
                    alpha = slab3Alpha
                    translationY = slab3TranslationY
                },
            shape = FlatSquareShape,
            color = colors.surface,
            contentColor = colors.textPrimary,
            tonalElevation = 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = PrayerSpacing.large, vertical = PrayerSpacing.medium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Add prayer points",
                    style = typography.homeAction,
                    color = colors.textPrimary
                )
            }
        }
    }
}
