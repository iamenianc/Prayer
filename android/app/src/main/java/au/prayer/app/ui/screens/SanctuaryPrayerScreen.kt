package au.prayer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import au.prayer.app.data.models.TopicWithPoints
import au.prayer.app.ui.gestures.prayerSwipeGestures
import au.prayer.app.ui.theme.PrayerColors
import au.prayer.app.ui.theme.PrayerTypography

@Composable
fun SanctuaryPrayerScreen(
    topics: List<TopicWithPoints>,
    currentIndex: Int,
    colors: PrayerColors,
    typography: PrayerTypography,
    onNextTopic: () -> Unit,
    onPrevTopic: () -> Unit,
    onExit: () -> Unit
) {
    if (topics.isEmpty() || currentIndex !in topics.indices) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .clickable { onExit() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No active petitions found",
                style = typography.petitionTitle,
                color = colors.textSubtle
            )
        }
        return
    }

    val currentTopic = topics[currentIndex]
    var isAnsweredExpanded by remember(currentTopic.entity.id) { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Full-Screen Sanctuary: zero buttons, zero cards, zero borders
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .prayerSwipeGestures(
                onSwipeLeft = onNextTopic,
                onSwipeRight = onPrevTopic,
                onSwipeDown = onExit
            )
    ) {
        // Main Content Area with generous whitespace
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 28.dp, vertical = 40.dp)
        ) {
            // Solemn Heading: Praying for [Name]
            Text(
                text = "Praying for ${currentTopic.entity.displayName}",
                style = typography.topicTitle,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Active Petitions: Strictly unnumbered, pure substantive titles & bodies
            currentTopic.activePoints.forEach { point ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 28.dp)
                ) {
                    Text(
                        text = point.title,
                        style = typography.petitionTitle,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = point.description,
                        style = typography.petitionBody,
                        color = colors.textPrimary
                    )
                }
            }

            // Expandable Answered Petitions for Thanksgiving
            if (currentTopic.answeredPoints.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isAnsweredExpanded = !isAnsweredExpanded }
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = if (isAnsweredExpanded) "Answered (${currentTopic.answeredPoints.size}) — tap to hide" else "Answered (${currentTopic.answeredPoints.size})",
                        style = typography.caption,
                        color = colors.textSubtle
                    )
                }

                if (isAnsweredExpanded) {
                    currentTopic.answeredPoints.forEach { answeredPoint ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = answeredPoint.title,
                                style = typography.petitionTitle.copy(
                                    textDecoration = TextDecoration.LineThrough
                                ),
                                color = colors.answeredText,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = answeredPoint.description,
                                style = typography.petitionBody.copy(
                                    textDecoration = TextDecoration.LineThrough
                                ),
                                color = colors.answeredText
                            )
                            if (!answeredPoint.answeredTestimony.isNullOrBlank()) {
                                Text(
                                    text = "Thanksgiving: ${answeredPoint.answeredTestimony}",
                                    style = typography.caption,
                                    color = colors.textSubtle,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }

        // Accessibility tap zones (subtle invisible touch regions):
        // Right 75% advances; Left 25% returns; Top 40dp exits
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.25f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onPrevTopic
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.75f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onNextTopic
                    )
            )
        }
    }
}
