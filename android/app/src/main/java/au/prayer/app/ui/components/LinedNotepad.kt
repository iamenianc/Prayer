package au.prayer.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TextRange
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.platform.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import au.prayer.app.ui.theme.colors
import au.prayer.app.ui.theme.typography
import au.prayer.app.ui.theme.PrayerSpacing
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.graphics.Color

/**
 * Reusable notepad‑style text input with ruled lines.
 * Mirrors the implementation originally embedded in LogPrayerScreen.kt.
 */
@Composable
fun LinedNotepad(
    text: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    textStyle: TextStyle = typography.prayerPointBody.copy(color = colors.textPrimary)
) {
    val density = LocalDensity.current
    val bodyFontSize = textStyle.fontSize
    // Calculate line height based on font size; fallback to 36.sp if size is zero
    val notepadLineHeight = if (bodyFontSize.value > 0f) {
        (bodyFontSize.value * 1.9f).sp
    } else {
        36.sp
    }

    // Define the final text style used by BasicTextField
    val notepadTextStyle = textStyle.copy(
        lineHeight = notepadLineHeight,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    )

    // Measure a sample line to obtain a fallback line height for drawing when layout is not ready
    val textMeasurer = rememberTextMeasurer()
    val sampleMeasure = remember(notepadTextStyle, density) {
        textMeasurer.measure(
            text = AnnotatedString("• Sample line\\n• Second line"),
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
                .height(viewportHeight)
                .verticalScroll(scrollState)
                .drawBehind {
                    val strokeWidth = 0.75.dp.toPx()
                    val lineColor = colors.border.copy(alpha = 0.45f)
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
                    // Top line above first row
                    val firstLineTop = topMarginPx + (layout?.getLineTop(0) ?: sampleMeasure.getLineTop(0))
                    drawLine(lineColor, start = Offset(0f, firstLineTop), end = Offset(size.width, firstLineTop), strokeWidth = strokeWidth)
                    // Lines above first line if space permits
                    var aboveY = firstLineTop - singleLineHeight
                    while (aboveY >= 0f) {
                        drawLine(lineColor, start = Offset(0f, aboveY), end = Offset(size.width, aboveY), strokeWidth = strokeWidth)
                        aboveY -= singleLineHeight
                    }
                    // Ruled lines at bottom of each line of text
                    val lineCount = layout?.lineCount ?: 1
                    for (i in 0 until lineCount) {
                        val lineBottom = topMarginPx + (layout?.getLineBottom(i) ?: sampleMeasure.getLineBottom(0))
                        drawLine(lineColor, start = Offset(0f, lineBottom), end = Offset(size.width, lineBottom), strokeWidth = strokeWidth)
                    }
                    // Remaining lines below text down to canvas bottom
                    val lastBottom = topMarginPx + (layout?.let { it.getLineBottom(it.lineCount - 1) } ?: sampleMeasure.getLineBottom(0))
                    var belowY = lastBottom + singleLineHeight
                    while (belowY <= size.height) {
                        drawLine(lineColor, start = Offset(0f, belowY), end = Offset(size.width, belowY), strokeWidth = strokeWidth)
                        belowY += singleLineHeight
                    }
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
                    .padding(top = topMargin),
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
