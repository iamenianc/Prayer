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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
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
import au.prayer.app.ui.gestures.calculateZoomScale
import au.prayer.app.ui.gestures.pinchToZoom
import au.prayer.app.ui.gestures.prayerSwipeGestures
import au.prayer.app.ui.theme.FlatSquareShape
import au.prayer.app.ui.theme.PrayerColors
import au.prayer.app.ui.theme.PrayerSpacing
import au.prayer.app.ui.theme.PrayerTypography
import au.prayer.app.ui.theme.withZoom
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

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

    val initialZoom = remember { repository?.getTextZoomScale() ?: 1.0f }
    var zoomScale by remember { mutableFloatStateOf(initialZoom) }
    var isZooming by remember { mutableStateOf(false) }
    var showZoomPill by remember { mutableStateOf(false) }

    LaunchedEffect(isZooming) {
        if (isZooming) {
            showZoomPill = true
        } else if (showZoomPill) {
            delay(1500)
            showZoomPill = false
        }
    }

    val effectiveTypography = remember(typography, zoomScale) {
        typography.withZoom(zoomScale)
    }

    val promptsCache = remember {
        mutableStateMapOf<String, List<PromptGroup>>().apply {
            repository?.getAllCachedSuggestions()?.let { putAll(it) }
        }
    }
    val promptsLoading = remember { mutableStateMapOf<String, Boolean>() }
    val refreshedEntities = remember { mutableSetOf<String>() }

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
                style = effectiveTypography.prayerPointTitle,
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
            var localIsPinned by remember(pageTopic.entity.id, pageTopic.entity.isPinned) {
                mutableStateOf(pageTopic.entity.isPinned)
            }

            LaunchedEffect(entityId) {
                // Ensure cached prompts from last time are immediately available
                if (!promptsCache.containsKey(entityId)) {
                    repository?.getCachedSuggestions(entityId)?.let { cached ->
                        if (cached.isNotEmpty()) {
                            promptsCache[entityId] = cached
                        }
                    }
                }

                // Refresh in background while showing the last list of prompts
                if (apiClient != null && totalPointsCount > 0 && !pageTopic.entity.isPreloadedHistoric && !refreshedEntities.contains(entityId)) {
                    refreshedEntities.add(entityId)
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
                            repository?.saveCachedSuggestions(entityId, resp)
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

            // Baseline Synchronization: mathematically align typography and line metrics
            @Suppress("DEPRECATION")
            val synchronizedBulletStyle = remember(effectiveTypography.prayerPointBullet) {
                effectiveTypography.prayerPointBullet.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.None
                    )
                )
            }

            @Suppress("DEPRECATION")
            val synchronizedSubjectHeaderStyle = remember(effectiveTypography.subjectHeader) {
                effectiveTypography.subjectHeader.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.None
                    )
                )
            }

            val textMeasurer = rememberTextMeasurer()
            val sampleMeasure = remember(synchronizedBulletStyle, density) {
                textMeasurer.measure(
                    text = AnnotatedString("• Sample\n• Line"),
                    style = synchronizedBulletStyle
                )
            }
            val bodyCadencePx = if (sampleMeasure.lineCount > 1) {
                sampleMeasure.getLineTop(1) - sampleMeasure.getLineTop(0)
            } else {
                sampleMeasure.getLineBottom(0) - sampleMeasure.getLineTop(0)
            }.coerceAtLeast(1f)
            val textToLineGapPx = with(density) { 2.dp.toPx() }
            val bodyBaselineOffsetPx = (sampleMeasure.getLineBaseline(0) - sampleMeasure.getLineTop(0)).coerceAtLeast(0f) + textToLineGapPx
            val bodyCadenceDp = with(density) { bodyCadencePx.toDp() }

            // Dynamic anchor for first prayer point baseline on canvas
            var firstPointBaselineY by remember(pageTopic.entity.id) { mutableStateOf<Float?>(null) }
            val sanctuaryTextPaddingEnd = 36.dp

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
                        .pinchToZoom(
                            onZoomChange = { factor ->
                                isZooming = true
                                zoomScale = calculateZoomScale(zoomScale, factor)
                            },
                            onZoomStart = {
                                isZooming = true
                            },
                            onZoomEnd = {
                                isZooming = false
                                repository?.saveTextZoomScale(zoomScale)
                            }
                        )
                ) {

                    val viewportHeight = maxHeight
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = viewportHeight)
                            .verticalScroll(scrollState)
                            .safeDrawingPadding()
                            .drawBehind {
                                // Draw classic stationery red/sepia vertical margin rule at 37.52dp
                                drawLine(
                                    color = marginRuleColor,
                                    start = Offset(marginXPx, 0f),
                                    end = Offset(marginXPx, size.height),
                                    strokeWidth = strokeWidth
                                )

                                // Draw baseline-synchronized feint horizontal rules matching text spacing
                                val anchor = firstPointBaselineY ?: (bodyCadencePx * 3f + bodyBaselineOffsetPx)
                                val startY = ((anchor % bodyCadencePx) + bodyCadencePx) % bodyCadencePx
                                var y = startY
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
                                    end = sanctuaryTextPaddingEnd,
                                    top = bodyCadenceDp,
                                    bottom = bodyCadenceDp
                                )
                        ) {
                            Text(
                                text = if (pageTopic.entity.isPreloadedHistoric) {
                                    pageTopic.entity.displayName
                                } else {
                                    "Praying for ${pageTopic.entity.displayName}"
                                },
                                style = synchronizedSubjectHeaderStyle,
                                color = colors.inkPrimary
                            )
                        }

                        // Active Prayer Points: Two-track layout (37.52dp status pills on left, narrative text at 45.52dp)
                        pageTopic.activePoints.forEachIndexed { pointIndex, point ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = bodyCadenceDp)
                                    .then(
                                        if (pointIndex == 0) {
                                            Modifier.onGloballyPositioned { coords ->
                                                val yInCol = coords.positionInParent().y
                                                firstPointBaselineY = yInCol + bodyBaselineOffsetPx
                                            }
                                        } else Modifier
                                    )
                                    .combinedClickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onNextTopic,
                                        onLongClick = {
                                            if (!pageTopic.entity.isPreloadedHistoric && point.status != PrayerStatus.HISTORIC) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                actionPoint = point
                                            }
                                        }
                                    )
                            ) {
                                // Left 37.52dp track: Marginal Status Aside [ ACTIVE ] (§8.1 48dp touch target)
                                Box(
                                    modifier = Modifier
                                        .width(PrayerSpacing.marginTrackWidth)
                                        .height(bodyCadenceDp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!pageTopic.entity.isPreloadedHistoric && point.status != PrayerStatus.HISTORIC) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color.Transparent,
                                            border = BorderStroke(0.5.dp, colors.inkMuted.copy(alpha = 0.50f)),
                                            modifier = Modifier
                                                .defaultMinSize(minWidth = 48.dp, minHeight = 24.dp)
                                                .combinedClickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        onToggleAnswered?.invoke(point.id)
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        actionPoint = point
                                                    }
                                                )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Edit,
                                                contentDescription = "Active prayer",
                                                tint = colors.inkMuted,
                                                modifier = Modifier
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                                    .size(12.dp)
                                            )
                                        }
                                    }
                                }

                                // Right track: Narrative Petitions (starts at 64dp inset)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(
                                            start = PrayerSpacing.small,
                                            end = sanctuaryTextPaddingEnd
                                        )
                                ) {
                                    val bulletText = if (point.description.trimStart().startsWith("•")) {
                                        point.description
                                    } else {
                                        "• ${point.description}"
                                    }
                                    Text(
                                        text = bulletText,
                                        style = synchronizedBulletStyle,
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
                                        end = sanctuaryTextPaddingEnd,
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
                                        style = effectiveTypography.caption,
                                        color = colors.inkSecondary
                                    )
                                    Spacer(modifier = Modifier.height(PrayerSpacing.extraSmall))
                                    Text(
                                        text = if (isPromptsExpanded) "Tap to collapse" else "Tap to view prompts",
                                        style = effectiveTypography.marginStatus,
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
                                            end = sanctuaryTextPaddingEnd,
                                            bottom = PrayerSpacing.large
                                        )
                                ) {
                                    cachedGroups.forEach { group ->
                                        Text(
                                            text = group.title,
                                            style = effectiveTypography.categoryLedgerHeader,
                                            color = colors.inkSecondary,
                                            modifier = Modifier.padding(top = PrayerSpacing.medium, bottom = PrayerSpacing.extraSmall)
                                        )
                                        group.prompts.forEach { prompt ->
                                            Text(
                                                text = "• $prompt",
                                                style = effectiveTypography.suggestedIntercession,
                                                color = colors.inkMuted.copy(alpha = 0.85f),
                                                modifier = Modifier.padding(vertical = PrayerSpacing.extraSmall)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Expandable Answered Prayer Points for Thanksgiving (feint rules suppressed inside card, 2dp Celadon rule)
                        if (!pageTopic.entity.isPreloadedHistoric && pageTopic.answeredPoints.isNotEmpty()) {
                            // Typographic Fleuron & Section Divider (§6.3)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isAnsweredExpanded = !isAnsweredExpanded }
                                    .padding(
                                        start = PrayerSpacing.textInset,
                                        end = sanctuaryTextPaddingEnd,
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
                                        style = effectiveTypography.caption,
                                        color = colors.inkAnswered
                                    )
                                    Spacer(modifier = Modifier.height(PrayerSpacing.extraSmall))
                                    Text(
                                        text = if (isAnsweredExpanded) "Hide answered records" else "Review answered records",
                                        style = effectiveTypography.marginStatus,
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
                                                        .combinedClickable(
                                                            interactionSource = remember { MutableInteractionSource() },
                                                            indication = null,
                                                            onClick = {
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                onToggleAnswered?.invoke(answeredPoint.id)
                                                            },
                                                            onLongClick = {
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
                                                        end = sanctuaryTextPaddingEnd
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
                                                            style = effectiveTypography.answeredThanksgiving.copy(
                                                                textDecoration = TextDecoration.LineThrough
                                                            ),
                                                            color = colors.inkAnswered
                                                        )
                                                        if (!answeredPoint.answeredTestimony.isNullOrBlank()) {
                                                            Text(
                                                                text = "Thanksgiving: ${answeredPoint.answeredTestimony}",
                                                                style = effectiveTypography.answeredThanksgiving,
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
                        isPinned = localIsPinned,
                        onTogglePin = {
                            val newPinned = !localIsPinned
                            localIsPinned = newPinned
                            onTogglePinEntity?.invoke(pageTopic.entity.id, newPinned)
                        },
                        color = colors.ribbonPrimary,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )

                    // Top Bar Zoom Reset Indicator (§1.5)
                    if (zoomScale != 1.0f) {
                        Text(
                            text = "${(zoomScale * 100).roundToInt()}% ↺",
                            style = effectiveTypography.marginStatus.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = colors.leatherActive,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 56.dp, top = 8.dp)
                                .clickable {
                                    zoomScale = 1.0f
                                    repository?.saveTextZoomScale(1.0f)
                                    showZoomPill = true
                                }
                                .padding(horizontal = PrayerSpacing.extraSmall, vertical = PrayerSpacing.extraSmall)
                        )
                    }

                    // Floating Zoom Indicator Pill
                    AnimatedVisibility(
                        visible = isZooming || showZoomPill,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 28.dp)
                    ) {
                        Surface(
                            onClick = {
                                zoomScale = 1.0f
                                repository?.saveTextZoomScale(1.0f)
                                showZoomPill = true
                            },
                            shape = FlatSquareShape,
                            color = colors.leatherActive.copy(alpha = 0.92f),
                            contentColor = colors.background,
                            tonalElevation = PrayerSpacing.elevationCard,
                            shadowElevation = PrayerSpacing.elevationCard,
                            border = BorderStroke(1.dp, colors.borderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${(zoomScale * 100).roundToInt()}%",
                                    style = effectiveTypography.caption.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = colors.background
                                )
                                if (zoomScale != 1.0f) {
                                    Text(
                                        text = "• Reset",
                                        style = effectiveTypography.caption.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = colors.background.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
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
                    style = effectiveTypography.prayerPointTitle,
                    color = colors.textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                ) {
                    val currentTopic = topics.getOrNull(currentIndex)
                    val isPreloaded = currentTopic?.entity?.isPreloadedHistoric == true || point.status == PrayerStatus.HISTORIC
                    if (!isPreloaded && onToggleAnswered != null) {
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
                                style = effectiveTypography.button
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
                    Text("Dismiss", style = effectiveTypography.button, color = colors.textSubtle)
                }
            },
            shape = FlatSquareShape,
            containerColor = colors.surface,
            tonalElevation = 0.dp
        )
    }
}

