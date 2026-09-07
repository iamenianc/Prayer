package au.prayer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import au.prayer.app.ui.gestures.prayerSwipeGestures
import au.prayer.app.ui.theme.PrayerColors
import au.prayer.app.ui.theme.PrayerTypography

@Composable
fun HomeScreen(
    colors: PrayerColors,
    typography: PrayerTypography,
    onStartPraying: () -> Unit,
    onOpenJournal: () -> Unit,
    onLogPrayerPoints: () -> Unit
) {
    // Blank edge-to-edge canvas with strictly three contiguous slabs
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .prayerSwipeGestures(
                onSwipeLeft = onOpenJournal
            )
    ) {
        // Slab 1: Start praying
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(colors.surface)
                .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onStartPraying
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Start praying",
                style = typography.homeAction,
                color = colors.textPrimary
            )
        }

        // Slab 2: Open Journal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(colors.surface)
                .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOpenJournal
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Open Journal",
                style = typography.homeAction,
                color = colors.textPrimary
            )
        }

        // Slab 3: Log prayer points
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(colors.surface)
                .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onLogPrayerPoints
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Log prayer points",
                style = typography.homeAction,
                color = colors.textPrimary
            )
        }
    }
}
