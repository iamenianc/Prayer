package au.prayer.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import au.prayer.app.data.models.TextScale

data class PrayerTypography(
    val homeAction: TextStyle,
    val topicTitle: TextStyle,
    val prayerPointTitle: TextStyle,
    val prayerPointBody: TextStyle,
    val caption: TextStyle,
    val button: TextStyle
)

fun getPrayerTypography(scale: TextScale): PrayerTypography {
    return when (scale) {
        TextScale.LARGE -> PrayerTypography(
            homeAction = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 26.sp,
                letterSpacing = 0.5.sp
            ),
            topicTitle = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                letterSpacing = 0.3.sp
            ),
            prayerPointTitle = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp
            ),
            prayerPointBody = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 28.sp
            ),
            caption = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp
            ),
            button = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp
            )
        )
        TextScale.REGULAR -> PrayerTypography(
            homeAction = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                letterSpacing = 0.5.sp
            ),
            topicTitle = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                letterSpacing = 0.3.sp
            ),
            prayerPointTitle = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            ),
            prayerPointBody = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 23.sp
            ),
            caption = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp
            ),
            button = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        )
        TextScale.COMPACT -> PrayerTypography(
            homeAction = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
            ),
            topicTitle = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                letterSpacing = 0.3.sp
            ),
            prayerPointTitle = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            ),
            prayerPointBody = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 19.sp
            ),
            caption = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp
            ),
            button = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
        )
    }
}

fun getMaterialTypography(prayerTypography: PrayerTypography): androidx.compose.material3.Typography {
    return androidx.compose.material3.Typography(
        displayLarge = prayerTypography.topicTitle,
        displayMedium = prayerTypography.topicTitle,
        displaySmall = prayerTypography.homeAction,
        headlineLarge = prayerTypography.homeAction,
        headlineMedium = prayerTypography.homeAction,
        headlineSmall = prayerTypography.prayerPointTitle,
        titleLarge = prayerTypography.prayerPointTitle,
        titleMedium = prayerTypography.prayerPointTitle,
        titleSmall = prayerTypography.prayerPointTitle,
        bodyLarge = prayerTypography.prayerPointBody,
        bodyMedium = prayerTypography.prayerPointBody,
        bodySmall = prayerTypography.caption,
        labelLarge = prayerTypography.button,
        labelMedium = prayerTypography.button,
        labelSmall = prayerTypography.caption
    )
}
