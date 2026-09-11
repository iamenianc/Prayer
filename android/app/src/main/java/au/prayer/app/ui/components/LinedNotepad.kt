package au.prayer.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import au.prayer.app.ui.theme.PrayerColors
import au.prayer.app.ui.theme.PrayerSpacing
import au.prayer.app.ui.theme.PrayerTypography

/**
 * Reusable notepad-style text input with ruled lines.
 * Mirrors the implementation originally embedded in LogPrayerScreen.kt.
 */
@Composable
fun LinedNotepad(
    text: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    colors: PrayerColors,
    typography: PrayerTypography,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    textStyle: TextStyle = typography.prayerPointBody
) {
    val density = LocalDensity.current
    val baseStyle = textStyle.copy(color = colors.textPrimary)
    val bodyFontSize = baseStyle.fontSize
    val notepadLineHeight = if (bodyFontSize.value > 0f) {
        (bodyFontSize.value * 1.9f).sp
    } else {
        36.sp
    }

    @Suppress("DEPRECATION")
    val notepadTextStyle = baseStyle.copy(
        lineHeight = notepadLineHeight,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    )

    val textMeasurer = rememberTextMeasurer()
    val sampleMeasure = remember(notepadTextStyle, density) {
        textMeasurer.measure(
            text = AnnotatedString("• Sample line\n• Second line"),
            style = notepadTextStyle
        )
    }
    val fallbackLineHeight = if (sampleMeasure.lineCount > 1) {
        sampleMeasure.getLineTop(1) - sampleMeasure.getLineTop(0)
    } else {
        sampleMeasure.getLineBottom(0) - sampleMeasure.getLineTop(0)
    }.coerceAtLeast(1f)

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val scrollState = rememberScrollState()
    val topMargin = 16.dp
    val topMarginPx = with(density) { topMargin.toPx() }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val viewportHeight = maxHeight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = viewportHeight)
                .verticalScroll(scrollState)
                .drawBehind {
                    val strokeWidth = PrayerSpacing.hairlineWidth.toPx()
                    val lineColor = colors.paperFeintRule

                    val layout = textLayoutResult
                    val singleLineHeight = if (layout != null && layout.lineCount > 0) {
                        if (layout.lineCount > 1) {
                            layout.getLineTop(1) - layout.getLineTop(0)
                        } else {
                            layout.getLineBottom(0) - layout.getLineTop(0)
                        }
                    } else {
                        fallbackLineHeight
                    }

                    val firstLineTop = topMarginPx + if (layout != null && layout.lineCount > 0) {
                        layout.getLineTop(0)
                    } else {
                        sampleMeasure.getLineTop(0)
                    }

                    drawLine(
                        color = lineColor,
                        start = Offset(0f, firstLineTop),
                        end = Offset(size.width, firstLineTop),
                        strokeWidth = strokeWidth
                    )

                    var aboveY = firstLineTop - singleLineHeight
                    while (aboveY >= 0f) {
                        drawLine(
                            color = lineColor,
                            start = Offset(0f, aboveY),
                            end = Offset(size.width, aboveY),
                            strokeWidth = strokeWidth
                        )
                        aboveY -= singleLineHeight
                    }

                    val lineCount = layout?.lineCount ?: 1
                    for (i in 0 until lineCount) {
                        val lineBottom = topMarginPx + if (layout != null) {
                            layout.getLineBottom(i)
                        } else {
                            sampleMeasure.getLineBottom(0)
                        }
                        drawLine(
                            color = lineColor,
                            start = Offset(0f, lineBottom),
                            end = Offset(size.width, lineBottom),
                            strokeWidth = strokeWidth
                        )
                    }

                    val lastBottom = topMarginPx + if (layout != null && layout.lineCount > 0) {
                        layout.getLineBottom(layout.lineCount - 1)
                    } else {
                        sampleMeasure.getLineBottom(0)
                    }
                    var belowY = lastBottom + singleLineHeight
                    while (belowY <= size.height) {
                        drawLine(
                            color = lineColor,
                            start = Offset(0f, belowY),
                            end = Offset(size.width, belowY),
                            strokeWidth = strokeWidth
                        )
                        belowY += singleLineHeight
                    }

                    // Classic stationery red/sepia vertical margin rule at 56dp
                    val marginX = with(density) { PrayerSpacing.marginTrackWidth.toPx() }
                    drawLine(
                        color = colors.paperMarginRule,
                        start = Offset(marginX, 0f),
                        end = Offset(marginX, size.height),
                        strokeWidth = strokeWidth
                    )
                }
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                textStyle = notepadTextStyle,
                cursorBrush = SolidColor(colors.textPrimary),
                onTextLayout = { textLayoutResult = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = topMargin,
                        start = PrayerSpacing.textInset,
                        end = PrayerSpacing.narrativeRightPadding
                    ),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (text.text.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = notepadTextStyle.copy(color = colors.textSubtle.copy(alpha = 0.5f))
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}
