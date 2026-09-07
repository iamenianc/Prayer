package au.prayer.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Universal 0dp rectilinear geometry - strictly zero curved edges or rounded corners
val FlatSquareShape = RoundedCornerShape(0.dp)

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
