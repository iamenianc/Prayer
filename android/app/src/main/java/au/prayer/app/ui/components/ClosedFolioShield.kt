package au.prayer.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import au.prayer.app.ui.theme.FlatSquareShape
import au.prayer.app.ui.theme.PrayerColors

/**
 * Closed Folio Privacy Shield.
 * Rendered when the application is paused, backgrounded, or viewed in the Android Recent Apps task switcher.
 * Shields all sacred prayer text behind an authentic flat vector leather folio cover
 * with an understated embossed monogram insignia.
 */
@Composable
fun ClosedFolioShield(
    colors: PrayerColors,
    modifier: Modifier = Modifier
) {
    val leatherColor = colors.leatherActive
    val debossColor = Color.Black.copy(alpha = 0.20f)
    val perimeterBorderColor = Color.Black.copy(alpha = 0.15f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(leatherColor)
            .border(width = 1.dp, color = perimeterBorderColor, shape = FlatSquareShape)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Discreet embossed monogram insignia: PWC (Pray Without Ceasing)
        Text(
            text = "P · W · C",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            letterSpacing = 4.sp,
            color = debossColor
        )
    }
}
