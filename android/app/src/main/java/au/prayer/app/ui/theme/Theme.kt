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
    val surfaceElevated: Color = surface,
    val surfaceSubtle: Color = surface,
    val textPrimary: Color,
    val textSubtle: Color,
    val border: Color,
    val borderSubtle: Color = border,
    val borderStrong: Color = border,
    val answeredText: Color
)

val WarmVintageWhiteColors = PrayerColors(
    background = Color(0xFFFAF7F2),
    surface = Color(0xFFFFFDF9),
    surfaceElevated = Color(0xFFFFFFFF),
    surfaceSubtle = Color(0xFFF0ECE1),
    textPrimary = Color(0xFF1C1917),
    textSubtle = Color(0xFF6E675F),
    border = Color(0xFFE3DDD3),
    borderSubtle = Color(0xFFEDE8DF),
    borderStrong = Color(0xFFD5CCC0),
    answeredText = Color(0xFF8E867C)
)

// Legacy alias for compatibility
val MorningLightColors = WarmVintageWhiteColors

val WarmVintageWhiteColorScheme: ColorScheme = lightColorScheme(
    primary = WarmVintageWhiteColors.textPrimary,
    onPrimary = WarmVintageWhiteColors.surface,
    primaryContainer = WarmVintageWhiteColors.surfaceSubtle,
    onPrimaryContainer = WarmVintageWhiteColors.textPrimary,
    secondary = WarmVintageWhiteColors.textPrimary,
    onSecondary = WarmVintageWhiteColors.surface,
    secondaryContainer = WarmVintageWhiteColors.surfaceSubtle,
    onSecondaryContainer = WarmVintageWhiteColors.textPrimary,
    background = WarmVintageWhiteColors.background,
    onBackground = WarmVintageWhiteColors.textPrimary,
    surface = WarmVintageWhiteColors.surface,
    onSurface = WarmVintageWhiteColors.textPrimary,
    surfaceVariant = WarmVintageWhiteColors.surfaceSubtle,
    onSurfaceVariant = WarmVintageWhiteColors.textSubtle,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAF7F2),
    surfaceContainer = Color(0xFFF4F0E8),
    surfaceContainerHigh = Color(0xFFEBE6DC),
    surfaceContainerHighest = Color(0xFFE2DDD2),
    outline = WarmVintageWhiteColors.border,
    outlineVariant = WarmVintageWhiteColors.borderSubtle,
    error = Color(0xFFB00020),
    onError = Color(0xFFFFFFFF)
)

// Legacy alias for compatibility
val MorningLightColorScheme: ColorScheme = WarmVintageWhiteColorScheme

val LocalPrayerColors = staticCompositionLocalOf { WarmVintageWhiteColors }
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
    themeMode: ThemeMode = ThemeMode.WARM_VINTAGE_WHITE,
    textScale: TextScale = TextScale.LARGE,
    content: @Composable () -> Unit
) {
    val colors = WarmVintageWhiteColors
    val colorScheme = WarmVintageWhiteColorScheme
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

