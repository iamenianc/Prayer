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
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
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
    val answeredText: Color,
    // Modern Folio: Four Chromatic Tiers
    val leatherPrimary: Color = Color(0xFF8C532B),
    val leatherCordovan: Color = Color(0xFF5E2A2B),
    val leatherForest: Color = Color(0xFF2D483A),
    val leatherObsidian: Color = Color(0xFF35322F),
    val leatherActive: Color = leatherPrimary,
    val paperBackground: Color = Color(0xFFFAF7F0),
    val paperIvory: Color = Color(0xFFF5EFEB),
    val paperParchment: Color = Color(0xFFEFE8DA),
    val paperFeintRule: Color = Color(0xFFE2DDD5).copy(alpha = 0.70f),
    val paperMarginRule: Color = Color(0xFFE5B4B4),
    val inkPrimary: Color = Color(0xFF1C1A17),
    val inkAnswered: Color = Color(0xFF3D6B52),
    val inkSecondary: Color = Color(0xFF33261F),
    val inkMidnight: Color = Color(0xFF1B2433),
    val inkMuted: Color = Color(0xFF6E675F),
    val ribbonPrimary: Color = Color(0xFF8B2635),
    val ribbonEmerald: Color = Color(0xFF1E4D3B),
    val ribbonGold: Color = Color(0xFFB8860B),
    val selectionAmber: Color = Color(0xFFF5DE88).copy(alpha = 0.45f),
    val stateAlert: Color = Color(0xFF8B2635),
    val leatherPerimeterBorder: Color = Color.Black.copy(alpha = 0.15f)
)

val WarmVintageWhiteColors = PrayerColors(
    background = Color(0xFFFAF7F0),
    surface = Color(0xFFFFFDF9),
    surfaceElevated = Color(0xFFFFFFFF),
    surfaceSubtle = Color(0xFFF0ECE1),
    textPrimary = Color(0xFF1C1917),
    textSubtle = Color(0xFF6E675F),
    border = Color(0xFFE3DDD3),
    borderSubtle = Color(0xFFEDE8DF),
    borderStrong = Color(0xFFD5CCC0),
    answeredText = Color(0xFF8E867C),
    leatherPrimary = Color(0xFF8C532B),
    leatherCordovan = Color(0xFF5E2A2B),
    leatherForest = Color(0xFF2D483A),
    leatherObsidian = Color(0xFF35322F),
    leatherActive = Color(0xFF8C532B),
    paperBackground = Color(0xFFFAF7F0),
    paperIvory = Color(0xFFF5EFEB),
    paperParchment = Color(0xFFEFE8DA),
    paperFeintRule = Color(0xFFE2DDD5).copy(alpha = 0.70f),
    paperMarginRule = Color(0xFFE5B4B4),
    inkPrimary = Color(0xFF1C1A17),
    inkAnswered = Color(0xFF3D6B52),
    inkSecondary = Color(0xFF33261F),
    inkMidnight = Color(0xFF1B2433),
    inkMuted = Color(0xFF6E675F),
    ribbonPrimary = Color(0xFF8B2635),
    ribbonEmerald = Color(0xFF1E4D3B),
    ribbonGold = Color(0xFFB8860B),
    selectionAmber = Color(0xFFF5DE88).copy(alpha = 0.45f),
    stateAlert = Color(0xFF8B2635)
)

val AccessibleHighContrastColors = PrayerColors(
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFFFFFFF),
    surfaceSubtle = Color(0xFFF0ECE1),
    textPrimary = Color(0xFF0F0E0D),
    textSubtle = Color(0xFF4A4642),
    border = Color(0xFF9C9488),
    borderSubtle = Color(0xFFD5CCC0),
    borderStrong = Color(0xFF0F0E0D),
    answeredText = Color(0xFF1E4D3B),
    leatherPrimary = Color(0xFF5A2800),
    leatherCordovan = Color(0xFF3D1A1B),
    leatherForest = Color(0xFF1A2E24),
    leatherObsidian = Color(0xFF35322F),
    leatherActive = Color(0xFF5A2800),
    paperBackground = Color(0xFFFFFFFF),
    paperIvory = Color(0xFFFFFFFF),
    paperParchment = Color(0xFFFFFDF8),
    paperFeintRule = Color(0xFF9C9488),
    paperMarginRule = Color(0xFFA63D40),
    inkPrimary = Color(0xFF0F0E0D),
    inkAnswered = Color(0xFF1E4D3B),
    inkSecondary = Color(0xFF262422),
    inkMidnight = Color(0xFF0F172A),
    inkMuted = Color(0xFF4A4642),
    ribbonPrimary = Color(0xFF8B2635),
    ribbonEmerald = Color(0xFF1E4D3B),
    ribbonGold = Color(0xFF996F00),
    selectionAmber = Color(0xFFFFE066).copy(alpha = 0.60f),
    stateAlert = Color(0xFFA63D40)
)

fun getFolioColors(themeMode: ThemeMode, highContrastMode: Boolean = false): PrayerColors {
    if (highContrastMode) {
        val activeLeather = when (themeMode) {
            ThemeMode.WARM_VINTAGE_WHITE, ThemeMode.SADDLE_TAN -> Color(0xFF5A2800)
            ThemeMode.HORWEEN_CORDOVAN -> Color(0xFF3D1A1B)
            ThemeMode.HUNTER_FOREST -> Color(0xFF1A2E24)
            ThemeMode.OBSIDIAN_HIDE -> Color(0xFF35322F)
        }
        return AccessibleHighContrastColors.copy(
            leatherActive = activeLeather
        )
    }
    val activeLeather = when (themeMode) {
        ThemeMode.WARM_VINTAGE_WHITE, ThemeMode.SADDLE_TAN -> Color(0xFF8C532B)
        ThemeMode.HORWEEN_CORDOVAN -> Color(0xFF5E2A2B)
        ThemeMode.HUNTER_FOREST -> Color(0xFF2D483A)
        ThemeMode.OBSIDIAN_HIDE -> Color(0xFF35322F)
    }
    return WarmVintageWhiteColors.copy(
        leatherActive = activeLeather
    )
}

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
    surfaceContainerLow = Color(0xFFFAF7F0),
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
    highContrastMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = getFolioColors(themeMode, highContrastMode)
    val colorScheme = if (highContrastMode) {
        lightColorScheme(
            primary = colors.textPrimary,
            onPrimary = colors.surface,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            outline = colors.border
        )
    } else {
        WarmVintageWhiteColorScheme
    }
    val typography = getPrayerTypography(textScale)
    val materialTypography = getMaterialTypography(prayerTypography = typography)
    val textSelectionColors = TextSelectionColors(
        handleColor = colors.inkPrimary,
        backgroundColor = colors.selectionAmber
    )

    CompositionLocalProvider(
        LocalPrayerColors provides colors,
        LocalPrayerTypography provides typography,
        LocalPrayerSpacing provides PrayerSpacingTokens(),
        LocalTextSelectionColors provides textSelectionColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = PrayerShapes,
            typography = materialTypography,
            content = content
        )
    }
}

