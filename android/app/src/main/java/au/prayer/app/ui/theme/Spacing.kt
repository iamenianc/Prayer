package au.prayer.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standardized 8dp Spacing Grid and Layout Metrics.
 * Adheres strictly to Material Design 3 spacing and touch target standards
 * while honoring liturgical proportion and Samsung Galaxy Flip ergonomics.
 */
data class PrayerSpacingTokens(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val huge: Dp = 48.dp,
    val minTouchTarget: Dp = 48.dp,
    val primaryActionHeight: Dp = 56.dp,
    val topAppBarHeight: Dp = 56.dp,
    val sanctuaryBottom: Dp = 72.dp,
    val cardMargin: Dp = 16.dp,
    val elevationNone: Dp = 0.dp,
    val elevationSubtle: Dp = 1.dp,
    val elevationCard: Dp = 2.dp,
    val elevationFloating: Dp = 4.dp,
    val elevationModal: Dp = 8.dp,
    // Modern Folio Spatial & Stationery Geometry
    val marginTrackWidth: Dp = 56.dp,
    val textInset: Dp = 64.dp,
    val narrativeRightPadding: Dp = 24.dp,
    val hairlineWidth: Dp = 0.75.dp,
    val ribbonWidth: Dp = 18.dp,
    val ribbonRestingHeight: Dp = 40.dp,
    val ribbonPinnedHeight: Dp = 54.dp,
    val spineGutterWidth: Dp = 24.dp
)

val LocalPrayerSpacing = staticCompositionLocalOf { PrayerSpacingTokens() }

object PrayerSpacing {
    val current: PrayerSpacingTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalPrayerSpacing.current

    val extraSmall: Dp = 4.dp
    val small: Dp = 8.dp
    val medium: Dp = 16.dp
    val large: Dp = 24.dp
    val extraLarge: Dp = 32.dp
    val huge: Dp = 48.dp
    val minTouchTarget: Dp = 48.dp
    val primaryActionHeight: Dp = 56.dp
    val topAppBarHeight: Dp = 56.dp
    val sanctuaryBottom: Dp = 72.dp
    val cardMargin: Dp = 16.dp
    val elevationNone: Dp = 0.dp
    val elevationSubtle: Dp = 1.dp
    val elevationCard: Dp = 2.dp
    val elevationFloating: Dp = 4.dp
    val elevationModal: Dp = 8.dp
    val marginTrackWidth: Dp = 56.dp
    val textInset: Dp = 64.dp
    val narrativeRightPadding: Dp = 24.dp
    val hairlineWidth: Dp = 0.75.dp
    val ribbonWidth: Dp = 18.dp
    val ribbonRestingHeight: Dp = 40.dp
    val ribbonPinnedHeight: Dp = 54.dp
    val spineGutterWidth: Dp = 24.dp
}
