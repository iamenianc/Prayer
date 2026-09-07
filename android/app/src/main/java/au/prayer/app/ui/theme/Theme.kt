package au.prayer.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import au.prayer.app.data.models.TextScale
import au.prayer.app.data.models.ThemeMode

// Universal 0dp rectilinear geometry - strictly zero curved edges or rounded corners
val FlatSquareShape = RoundedCornerShape(0.dp)

val PrayerShapes = Shapes(
    extraSmall = FlatSquareShape,
    small = FlatSquareShape,
    medium = FlatSquareShape,
    large = FlatSquareShape,
    extraLarge = FlatSquareShape
)

data class PrayerColors(
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSubtle: Color,
    val border: Color,
    val answeredText: Color
)

val MorningLightColors = PrayerColors(
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF111111),
    textSubtle = Color(0xFF666666),
    border = Color(0xFFE0E0E0),
    answeredText = Color(0xFF888888)
)

val QuietNightColors = PrayerColors(
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    textPrimary = Color(0xFFFFFFFF),
    textSubtle = Color(0xFF888888),
    border = Color(0xFF333333),
    answeredText = Color(0xFF666666)
)

val MorningLightColorScheme: ColorScheme = lightColorScheme(
    primary = MorningLightColors.textPrimary,
    onPrimary = MorningLightColors.background,
    primaryContainer = Color(0xFFF5F5F5),
    onPrimaryContainer = MorningLightColors.textPrimary,
    secondary = MorningLightColors.textPrimary,
    onSecondary = MorningLightColors.background,
    secondaryContainer = Color(0xFFEEEEEE),
    onSecondaryContainer = MorningLightColors.textPrimary,
    background = MorningLightColors.background,
    onBackground = MorningLightColors.textPrimary,
    surface = MorningLightColors.surface,
    onSurface = MorningLightColors.textPrimary,
    surfaceVariant = Color(0xFFF9F9F9),
    onSurfaceVariant = MorningLightColors.textSubtle,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAFAFA),
    surfaceContainer = Color(0xFFF5F5F5),
    surfaceContainerHigh = Color(0xFFEEEEEE),
    surfaceContainerHighest = Color(0xFFE0E0E0),
    outline = MorningLightColors.border,
    outlineVariant = Color(0xFFEEEEEE),
    error = Color(0xFFB00020),
    onError = Color(0xFFFFFFFF)
)

val QuietNightColorScheme: ColorScheme = darkColorScheme(
    primary = QuietNightColors.textPrimary,
    onPrimary = QuietNightColors.background,
    primaryContainer = Color(0xFF1E1E1E),
    onPrimaryContainer = QuietNightColors.textPrimary,
    secondary = QuietNightColors.textPrimary,
    onSecondary = QuietNightColors.background,
    secondaryContainer = Color(0xFF242424),
    onSecondaryContainer = QuietNightColors.textPrimary,
    background = QuietNightColors.background,
    onBackground = QuietNightColors.textPrimary,
    surface = QuietNightColors.surface,
    onSurface = QuietNightColors.textPrimary,
    surfaceVariant = Color(0xFF161616),
    onSurfaceVariant = QuietNightColors.textSubtle,
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF121212),
    surfaceContainer = Color(0xFF1A1A1A),
    surfaceContainerHigh = Color(0xFF222222),
    surfaceContainerHighest = Color(0xFF2C2C2C),
    outline = QuietNightColors.border,
    outlineVariant = Color(0xFF222222),
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000)
)

val LocalPrayerColors = staticCompositionLocalOf { MorningLightColors }
val LocalPrayerTypography = staticCompositionLocalOf { getPrayerTypography(TextScale.LARGE) }

object PrayerThemeTokens {
    val colors: PrayerColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPrayerColors.current

    val typography: PrayerTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalPrayerTypography.current

    val spacing: PrayerSpacingTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalPrayerSpacing.current
}

@Composable
fun PrayerTheme(
    themeMode: ThemeMode = ThemeMode.MORNING_LIGHT,
    textScale: TextScale = TextScale.LARGE,
    content: @Composable () -> Unit
) {
    val colors = if (themeMode == ThemeMode.QUIET_NIGHT) QuietNightColors else MorningLightColors
    val colorScheme = if (themeMode == ThemeMode.QUIET_NIGHT) QuietNightColorScheme else MorningLightColorScheme
    val typography = getPrayerTypography(textScale)
    val materialTypography = getMaterialTypography(prayerTypography = typography)

    CompositionLocalProvider(
        LocalPrayerColors provides colors,
        LocalPrayerTypography provides typography,
        LocalPrayerSpacing provides PrayerSpacingTokens()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = PrayerShapes,
            typography = materialTypography,
            content = content
        )
    }
}

