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
    val button: TextStyle,
    // Modern Folio Dual-Engine Typographic Tokens
    val frontispieceHeader: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.0.sp
    ),
    val subjectHeader: TextStyle = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 32.sp
    ),
    val prayerPointBullet: TextStyle = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.25.sp
    ),
    val answeredThanksgiving: TextStyle = TextStyle(
        fontFamily = FontFamily.Serif,
        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.15.sp
    ),
    val suggestedIntercession: TextStyle = TextStyle(
        fontFamily = FontFamily.Serif,
        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 24.sp
    ),
    val categoryLedgerHeader: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.8.sp
    ),
    val marginStatus: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    val historicalFootnote: TextStyle = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
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
            ),
            prayerPointBullet = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 17.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.25.sp
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
            ),
            prayerPointBullet = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 23.sp,
                letterSpacing = 0.25.sp
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
            ),
            prayerPointBullet = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                letterSpacing = 0.25.sp
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

fun PrayerTypography.withZoom(scale: Float): PrayerTypography {
    if (scale == 1.0f) return this
    val clamped = scale.coerceIn(0.75f, 2.5f)
    fun TextStyle.scale(): TextStyle {
        return this.copy(
            fontSize = (this.fontSize.value * clamped).sp,
            lineHeight = if (this.lineHeight.value > 0f) (this.lineHeight.value * clamped).sp else this.lineHeight
        )
    }
    return PrayerTypography(
        homeAction = homeAction.scale(),
        topicTitle = topicTitle.scale(),
        prayerPointTitle = prayerPointTitle.scale(),
        prayerPointBody = prayerPointBody.scale(),
        caption = caption.scale(),
        button = button.scale(),
        frontispieceHeader = frontispieceHeader.scale(),
        subjectHeader = subjectHeader.scale(),
        prayerPointBullet = prayerPointBullet.scale(),
        answeredThanksgiving = answeredThanksgiving.scale(),
        suggestedIntercession = suggestedIntercession.scale(),
        categoryLedgerHeader = categoryLedgerHeader.scale(),
        marginStatus = marginStatus.scale(),
        historicalFootnote = historicalFootnote.scale()
    )
}


