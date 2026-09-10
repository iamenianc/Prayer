package au.prayer.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import au.prayer.app.data.local.PrayerRepository
import au.prayer.app.data.models.PrayerPoint
import au.prayer.app.data.models.PrayerStatus
import au.prayer.app.data.models.TopicWithPoints
import au.prayer.app.network.PrayerApiClient
import au.prayer.app.network.RecordedPoint
import au.prayer.app.network.SuggestRequest
import au.prayer.app.ui.gestures.prayerSwipeGestures
import au.prayer.app.ui.theme.FlatSquareShape
import au.prayer.app.ui.theme.PrayerColors
import au.prayer.app.ui.theme.PrayerSpacing
import au.prayer.app.ui.theme.PrayerTypography

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SanctuaryPrayerScreen(
    topics: List<TopicWithPoints>,
    currentIndex: Int,
    colors: PrayerColors,
    typography: PrayerTypography,
    apiClient: PrayerApiClient? = null,
    repository: PrayerRepository? = null,
    onNextTopic: () -> Unit,
    onPrevTopic: () -> Unit,
    onExit: () -> Unit,
    onToggleAnswered: ((String) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    var actionPoint by remember { mutableStateOf<PrayerPoint?>(null) }
    val promptsCache = remember { mutableStateMapOf<String, List<String>>() }
    val promptsLoading = remember { mutableStateMapOf<String, Boolean>() }

    BackHandler(enabled = true) {
        onExit()
    }

    if (topics.isEmpty() || currentIndex !in topics.indices) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .clickable { onExit() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No active prayer points found",
                style = typography.prayerPointTitle,
                color = colors.textSubtle
            )
        }
        return
    }

    // Full-Screen Sanctuary: strictly zero buttons, zero cards, zero borders
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .prayerSwipeGestures(
                onSwipeLeft = onNextTopic,
                onSwipeRight = onPrevTopic,
                onSwipeDown = onExit,
                onEdgeSwipeRight = onExit
            )
    ) {
        // Accessibility tap zones (subtle touch regions):
        // Right 75% advances; Left 25% returns
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

        // Page-turning topic progression animation mimicking the deliberate turning of pages in a prayer book
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = {
                if (targetState > initialState) {
                    // Advancing: page slides in from right with soft fade
                    (slideInHorizontally(animationSpec = tween(350, easing = FastOutSlowInEasing)) { width -> width } +
                            fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)))
                        .togetherWith(
                            slideOutHorizontally(animationSpec = tween(350, easing = FastOutSlowInEasing)) { width -> -width } +
                                    fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing))
                        )
                } else {
                    // Returning: page slides in from left with soft fade
                    (slideInHorizontally(animationSpec = tween(350, easing = FastOutSlowInEasing)) { width -> -width } +
                            fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)))
                        .togetherWith(
                            slideOutHorizontally(animationSpec = tween(350, easing = FastOutSlowInEasing)) { width -> width } +
                                    fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing))
                        )
                }
            },
            label = "SanctuaryPageTurn",
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val pageTopic = topics.getOrNull(pageIndex) ?: return@AnimatedContent
            val entityId = pageTopic.entity.id
            val totalPointsCount = pageTopic.activePoints.size + pageTopic.answeredPoints.size
            var isAnsweredExpanded by remember(pageTopic.entity.id) { mutableStateOf(false) }
            val scrollState = rememberScrollState()

            LaunchedEffect(entityId) {
                if (apiClient != null && totalPointsCount > 0 && !promptsCache.containsKey(entityId) && !pageTopic.entity.isPreloadedHistoric) {
                    promptsLoading[entityId] = true
                    val contextData = repository?.getTargetContext(entityId)
                    val recorded = (contextData?.activePoints.orEmpty() + contextData?.answeredPoints.orEmpty()).ifEmpty {
                        pageTopic.activePoints + pageTopic.answeredPoints
                    }.map {
                        RecordedPoint(title = it.title, body = it.description, status = it.status.name)
                    }

                    val request = SuggestRequest(
                        targetName = pageTopic.entity.displayName,
                        root = pageTopic.entity.rootCode.name,
                        group = null,
                        contextDescription = pageTopic.entity.contextDescription?.takeIf { it.isNotBlank() },
                        recordedPoints = recorded,
                        journalUpdates = emptyList(),
                        currentDraft = null,
                        localeDialect = "EN_AU_UK"
                    )
                    val result = apiClient.getSuggestions(request)
                    promptsLoading[entityId] = false
                    result.onSuccess { resp ->
                        if (resp.suggestions.isNotEmpty()) {
                            promptsCache[entityId] = resp.suggestions
                        }
                    }.onFailure {
                        promptsLoading[entityId] = false
                    }
                }
            }

            // Main Content Area with generous liturgical whitespace following the 8dp grid
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .safeDrawingPadding()
                    .padding(
                        horizontal = PrayerSpacing.extraLarge,
                        vertical = PrayerSpacing.extraLarge
                    )
            ) {
                // Solemn Heading: Praying for [Name]
                Text(
                    text = "Praying for ${pageTopic.entity.displayName}",
                    style = typography.topicTitle,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = PrayerSpacing.extraLarge)
                )

                // Active Prayer Points: Strictly unnumbered, pure substantive titles & bodies in elevated cards
                pageTopic.activePoints.forEach { point ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = PrayerSpacing.medium)
                            .combinedClickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onNextTopic,
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    actionPoint = point
                                }
                            ),
                        shape = FlatSquareShape,
                        color = colors.surface,
                        tonalElevation = PrayerSpacing.elevationSubtle,
                        shadowElevation = PrayerSpacing.elevationSubtle,
                        border = BorderStroke(0.5.dp, colors.borderSubtle)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(PrayerSpacing.large)
                        ) {
                            Text(
                                text = point.title,
                                style = typography.prayerPointTitle,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(bottom = PrayerSpacing.small)
                            )
                            Text(
                                text = point.description,
                                style = typography.prayerPointBody,
                                color = colors.textPrimary
                            )
                        }
                    }
                }

                // Read-Only AI Prompts based on past points (Strictly unlabelled in UI)
                val cachedPrompts = promptsCache[entityId].orEmpty()
                val isLoadingPrompts = promptsLoading[entityId] == true

                if (cachedPrompts.isNotEmpty() || isLoadingPrompts) {
                    Spacer(modifier = Modifier.height(PrayerSpacing.small))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = PrayerSpacing.medium),
                        shape = FlatSquareShape,
                        color = colors.surfaceSubtle,
                        tonalElevation = PrayerSpacing.elevationSubtle,
                        shadowElevation = PrayerSpacing.elevationSubtle,
                        border = BorderStroke(0.5.dp, colors.borderSubtle)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(PrayerSpacing.large)
                        ) {
                            if (isLoadingPrompts && cachedPrompts.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 1.5.dp,
                                        color = colors.textSubtle
                                    )
                                }
                            }
                            if (cachedPrompts.isNotEmpty()) {
                                cachedPrompts.forEach { prompt ->
                                    Text(
                                        text = "• $prompt",
                                        style = typography.prayerPointBody,
                                        color = colors.textPrimary,
                                        modifier = Modifier.padding(vertical = PrayerSpacing.extraSmall)
                                    )
                                }
                            }
                        }
                    }
                }

                // Expandable Answered Prayer Points for Thanksgiving
                if (pageTopic.answeredPoints.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(PrayerSpacing.medium))
                    HorizontalDivider(thickness = 0.5.dp, color = colors.border)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAnsweredExpanded = !isAnsweredExpanded }
                            .padding(vertical = PrayerSpacing.medium)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Answered",
                                style = typography.caption,
                                color = colors.textSubtle
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = if (isAnsweredExpanded) "−" else "+",
                                style = typography.caption,
                                color = colors.textSubtle
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isAnsweredExpanded,
                        enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                        exit = shrinkVertically(animationSpec = tween(250)) + fadeOut(animationSpec = tween(200))
                    ) {
                        Column {
                            pageTopic.answeredPoints.forEach { answeredPoint ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = PrayerSpacing.small)
                                        .combinedClickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = onNextTopic,
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                actionPoint = answeredPoint
                                            }
                                        )
                                ) {
                                    Text(
                                        text = answeredPoint.title,
                                        style = typography.prayerPointTitle.copy(
                                            textDecoration = TextDecoration.LineThrough
                                        ),
                                        color = colors.answeredText,
                                        modifier = Modifier.padding(bottom = PrayerSpacing.extraSmall)
                                    )
                                    Text(
                                        text = answeredPoint.description,
                                        style = typography.prayerPointBody.copy(
                                            textDecoration = TextDecoration.LineThrough
                                        ),
                                        color = colors.answeredText
                                    )
                                    if (!answeredPoint.answeredTestimony.isNullOrBlank()) {
                                        Text(
                                            text = "Thanksgiving: ${answeredPoint.answeredTestimony}",
                                            style = typography.caption,
                                            color = colors.textSubtle,
                                            modifier = Modifier.padding(top = PrayerSpacing.extraSmall)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PrayerSpacing.sanctuaryBottom))
            }
        }
    }

    if (actionPoint != null) {
        val point = actionPoint!!
        val isAnswered = point.status == PrayerStatus.ANSWERED
        AlertDialog(
            onDismissRequest = { actionPoint = null },
            title = {
                Text(
                    text = point.title,
                    style = typography.prayerPointTitle,
                    color = colors.textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                ) {
                    if (onToggleAnswered != null) {
                        OutlinedButton(
                            onClick = {
                                val id = point.id
                                actionPoint = null
                                onToggleAnswered(id)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(PrayerSpacing.primaryActionHeight),
                            shape = FlatSquareShape,
                            border = BorderStroke(0.5.dp, colors.border),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = colors.surface,
                                contentColor = colors.textPrimary
                            )
                        ) {
                            Text(
                                text = if (isAnswered) "Mark as Active" else "Mark as Answered",
                                style = typography.button
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { actionPoint = null },
                    shape = FlatSquareShape
                ) {
                    Text("Dismiss", style = typography.button, color = colors.textSubtle)
                }
            },
            shape = FlatSquareShape,
            containerColor = colors.surface,
            tonalElevation = 0.dp
        )
    }
}
