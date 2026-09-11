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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import au.prayer.app.data.local.PrayerRepository
import au.prayer.app.data.models.PrayerPoint
import au.prayer.app.data.models.PrayerStatus
import au.prayer.app.data.models.TopicWithPoints
import au.prayer.app.network.PrayerApiClient
import au.prayer.app.network.PromptGroup
import au.prayer.app.network.RecordedPoint
import au.prayer.app.network.SuggestRequest
import au.prayer.app.ui.components.SilkMarkerRibbon
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
    onToggleAnswered: ((String) -> Unit)? = null,
    onTogglePinEntity: ((id: String, isPinned: Boolean) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    var actionPoint by remember { mutableStateOf<PrayerPoint?>(null) }
    val promptsCache = remember { mutableStateMapOf<String, List<PromptGroup>>() }
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
        // Devotional Prayer Touch Zones (30% / 40% / 30% spatial zoning)
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Left 30%: Previous Topic
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.30f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onPrevTopic
                    )
            )
            // Center 40%: Reading Area & Long-Press Status Resolution
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.40f)
            )
            // Right 30%: Next Topic
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.30f)
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
            var isPromptsExpanded by remember(pageTopic.entity.id) { mutableStateOf(false) }
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
                        if (resp.promptGroups.isNotEmpty()) {
                            promptsCache[entityId] = resp.promptGroups
                        }
                    }.onFailure {
                        promptsLoading[entityId] = false
                    }
                }
            }

            // Main Content Area: Centered Vellum Sheet (max 720dp) with Fine Ruled Paper Canvas
            val density = LocalDensity.current
            val strokeWidth = with(density) { PrayerSpacing.hairlineWidth.toPx() }
            val marginXPx = with(density) { PrayerSpacing.marginTrackWidth.toPx() }
            val feintRuleColor = colors.paperFeintRule
            val marginRuleColor = colors.paperMarginRule
            val bodyCadencePx = with(density) { 28.sp.toPx() }
            val topHeaderClearancePx = with(density) { 72.dp.toPx() }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.paperBackground),
                contentAlignment = Alignment.TopCenter
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 720.dp)
                        .fillMaxWidth()
                ) {
                    val viewportHeight = maxHeight
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = viewportHeight)
                            .verticalScroll(scrollState)
                            .safeDrawingPadding()
                            .drawBehind {
                                // Draw classic stationery red/sepia vertical margin rule at 56dp
                                drawLine(
                                    color = marginRuleColor,
                                    start = Offset(marginXPx, 0f),
                                    end = Offset(marginXPx, size.height),
                                    strokeWidth = strokeWidth
                                )

                                // Draw baseline-locked feint horizontal rules at 28sp cadence
                                var y = topHeaderClearancePx
                                while (y <= size.height) {
                                    drawLine(
                                        color = feintRuleColor,
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = strokeWidth
                                    )
                                    y += bodyCadencePx
                                }
                            }
                    ) {
                        // Devotional Subject Header: Praying for [Name] (22sp / 32sp, sits on first prominent rule)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = PrayerSpacing.textInset,
                                    end = PrayerSpacing.narrativeRightPadding,
                                    top = PrayerSpacing.large,
                                    bottom = PrayerSpacing.large
                                )
                        ) {
                            Text(
                                text = if (pageTopic.entity.isPreloadedHistoric) {
                                    pageTopic.entity.displayName
                                } else {
                                    "Praying for ${pageTopic.entity.displayName}"
                                },
                                style = typography.subjectHeader,
                                color = colors.inkPrimary
                            )
                        }

                        // Active Prayer Points: Two-track layout (56dp status pills on left, narrative text at 64dp)
                        pageTopic.activePoints.forEach { point ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = PrayerSpacing.large)
                                    .combinedClickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onNextTopic,
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            actionPoint = point
                                        }
                                    )
                            ) {
                                // Left 56dp track: Marginal Status Aside [ ACTIVE ] (§8.1 48dp touch target)
                                Box(
                                    modifier = Modifier
                                        .width(PrayerSpacing.marginTrackWidth)
                                        .defaultMinSize(minHeight = PrayerSpacing.minTouchTarget)
                                        .padding(top = PrayerSpacing.extraSmall),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color.Transparent,
                                        border = BorderStroke(0.5.dp, colors.inkMuted.copy(alpha = 0.50f)),
                                        modifier = Modifier
                                            .defaultMinSize(minWidth = 48.dp, minHeight = 28.dp)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    actionPoint = point
                                                }
                                            )
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            style = typography.marginStatus,
                                            color = colors.inkMuted,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // Right track: Narrative Petitions (starts at 64dp inset)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(
                                            start = PrayerSpacing.small,
                                            end = PrayerSpacing.narrativeRightPadding
                                        )
                                ) {
                                    val bulletText = if (point.description.trimStart().startsWith("•")) {
                                        point.description
                                    } else {
                                        "• ${point.description}"
                                    }
                                    Text(
                                        text = bulletText,
                                        style = typography.prayerPointBullet,
                                        color = colors.inkPrimary
                                    )
                                }
                            }
                        }

                        // Read-Only Suggested Intercessions (grouped into Praise God, Thank God, Ask God) - Collapsed by default
                        val cachedGroups = promptsCache[entityId].orEmpty()

                        if (cachedGroups.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = PrayerSpacing.minTouchTarget)
                                    .clickable { isPromptsExpanded = !isPromptsExpanded }
                                    .padding(
                                        start = PrayerSpacing.textInset,
                                        end = PrayerSpacing.narrativeRightPadding,
                                        top = PrayerSpacing.medium,
                                        bottom = PrayerSpacing.small
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "❧   Prompts for Prayer   ❧",
                                        style = typography.caption,
                                        color = colors.inkSecondary
                                    )
                                    Spacer(modifier = Modifier.height(PrayerSpacing.extraSmall))
                                    Text(
                                        text = if (isPromptsExpanded) "Tap to collapse" else "Tap to view prompts",
                                        style = typography.marginStatus,
                                        color = colors.inkMuted
                                    )
                                }
                            }

                            if (isPromptsExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = PrayerSpacing.textInset,
                                            end = PrayerSpacing.narrativeRightPadding,
                                            bottom = PrayerSpacing.large
                                        )
                                ) {
                                    cachedGroups.forEach { group ->
                                        Text(
                                            text = group.title,
                                            style = typography.categoryLedgerHeader,
                                            color = colors.inkSecondary,
                                            modifier = Modifier.padding(top = PrayerSpacing.medium, bottom = PrayerSpacing.extraSmall)
                                        )
                                        group.prompts.forEach { prompt ->
                                            Text(
                                                text = "• $prompt",
                                                style = typography.suggestedIntercession,
                                                color = colors.inkMuted.copy(alpha = 0.85f),
                                                modifier = Modifier.padding(vertical = PrayerSpacing.extraSmall)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Expandable Answered Prayer Points for Thanksgiving (feint rules suppressed inside card, 2dp Celadon rule)
                        if (pageTopic.answeredPoints.isNotEmpty()) {
                            // Typographic Fleuron & Section Divider (§6.3)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isAnsweredExpanded = !isAnsweredExpanded }
                                    .padding(
                                        start = PrayerSpacing.textInset,
                                        end = PrayerSpacing.narrativeRightPadding,
                                        top = PrayerSpacing.medium,
                                        bottom = PrayerSpacing.medium
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "❧   Answered Prayers & Thanksgiving   ❧",
                                        style = typography.caption,
                                        color = colors.inkAnswered
                                    )
                                    Spacer(modifier = Modifier.height(PrayerSpacing.extraSmall))
                                    Text(
                                        text = if (isAnsweredExpanded) "Hide answered records" else "Review answered records",
                                        style = typography.marginStatus,
                                        color = colors.inkMuted.copy(alpha = 0.70f)
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
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = PrayerSpacing.medium)
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
                                            // Left 56dp track: Marginal Status Aside [ ANSWERED ] (§8.1 48dp touch target)
                                            Box(
                                                modifier = Modifier
                                                    .width(PrayerSpacing.marginTrackWidth)
                                                    .defaultMinSize(minHeight = PrayerSpacing.minTouchTarget)
                                                    .padding(top = PrayerSpacing.extraSmall),
                                                contentAlignment = Alignment.TopCenter
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color.Transparent,
                                                    border = BorderStroke(0.5.dp, colors.inkAnswered.copy(alpha = 0.60f)),
                                                    modifier = Modifier
                                                        .defaultMinSize(minWidth = 48.dp, minHeight = 28.dp)
                                                        .clickable(
                                                            interactionSource = remember { MutableInteractionSource() },
                                                            indication = null,
                                                            onClick = {
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                actionPoint = answeredPoint
                                                            }
                                                        )
                                                ) {
                                                    Text(
                                                        text = "ANSWERED",
                                                        style = typography.marginStatus,
                                                        color = colors.inkAnswered,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            // Right track: Answered thanksgiving card (unruled, 2dp vertical Celadon rule)
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(
                                                        start = PrayerSpacing.small,
                                                        end = PrayerSpacing.narrativeRightPadding
                                                    ),
                                                shape = FlatSquareShape,
                                                color = colors.surfaceSubtle,
                                                border = null
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(PrayerSpacing.small)
                                                ) {
                                                    // 2dp vertical Celadon accent rule
                                                    Box(
                                                        modifier = Modifier
                                                            .width(2.dp)
                                                            .fillMaxHeight()
                                                            .background(colors.inkAnswered)
                                                    )
                                                    Spacer(modifier = Modifier.width(PrayerSpacing.small))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        val bulletText = if (answeredPoint.description.trimStart().startsWith("•")) {
                                                            answeredPoint.description
                                                        } else {
                                                            "• ${answeredPoint.description}"
                                                        }
                                                        Text(
                                                            text = bulletText,
                                                            style = typography.answeredThanksgiving.copy(
                                                                textDecoration = TextDecoration.LineThrough
                                                            ),
                                                            color = colors.inkAnswered
                                                        )
                                                        if (!answeredPoint.answeredTestimony.isNullOrBlank()) {
                                                            Text(
                                                                text = "Thanksgiving: ${answeredPoint.answeredTestimony}",
                                                                style = typography.answeredThanksgiving,
                                                                color = colors.inkAnswered,
                                                                modifier = Modifier.padding(top = PrayerSpacing.extraSmall)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(PrayerSpacing.sanctuaryBottom))
                    }

                    // Interactive Silk Marker Ribbon Tab anchored at top margin (§2.3, §11.2)
                    SilkMarkerRibbon(
                        isPinned = pageTopic.entity.isPinned,
                        onTogglePin = {
                            onTogglePinEntity?.invoke(pageTopic.entity.id, !pageTopic.entity.isPinned)
                        },
                        color = colors.ribbonPrimary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 16.dp)
                    )
                }
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
