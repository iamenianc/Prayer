package au.prayer.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import au.prayer.app.data.local.PrayerRepository
import au.prayer.app.data.models.*
import au.prayer.app.network.PrayerApiClient
import au.prayer.app.network.PromptGroup
import au.prayer.app.network.RecordedPoint
import au.prayer.app.network.SuggestRequest
import au.prayer.app.ui.gestures.calculateZoomScale
import au.prayer.app.ui.gestures.edgeSwipeRight
import au.prayer.app.ui.gestures.pinchToZoom
import au.prayer.app.ui.navigation.LifoBackStack
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import au.prayer.app.ui.components.LinedNotepad
import au.prayer.app.ui.components.SilkMarkerRibbon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import au.prayer.app.ui.theme.FlatSquareShape
import au.prayer.app.ui.theme.PrayerColors
import au.prayer.app.ui.theme.PrayerSpacing
import au.prayer.app.ui.theme.PrayerTypography
import au.prayer.app.ui.theme.withZoom
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private enum class JournalView {
    OVERVIEW,
    ENTITY_DETAIL,
    EDIT_PRAYER_POINT,
    SETTINGS
}

private fun formatJournalDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH)
    return sdf.format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun JournalScreen(
    initialEntity: IndividualEntity? = null,
    navigationKey: Int = 0,
    entities: List<IndividualEntity>,
    colors: PrayerColors,
    typography: PrayerTypography,
    config: AppConfig,
    onUpdateConfig: (AppConfig) -> Unit,
    getPointsForEntity: (String) -> List<PrayerPoint>,
    onUpdatePrayerPoint: (id: String, title: String, description: String, status: PrayerStatus, testimony: String?) -> Unit,
    onDeletePrayerPoint: (String) -> Unit,
    onDeleteEntity: (String) -> Unit,
    onUpdateEntity: (id: String, displayName: String, rootCode: RootCode) -> Unit = { _, _, _ -> },
    onTogglePinEntity: ((id: String, isPinned: Boolean) -> Unit)? = null,
    onCreateEntity: ((rootCode: RootCode, displayName: String) -> IndividualEntity)? = null,
    onAddForEntity: (IndividualEntity) -> Unit = {},
    onLogForEntity: (IndividualEntity) -> Unit = onAddForEntity,
    onSavePrayerPoints: ((entityId: String, points: List<String>) -> Unit)? = null,
    apiClient: PrayerApiClient? = null,
    repository: PrayerRepository? = null,
    onVaultRestored: ((RestoreSummary) -> Unit)? = null,
    onShowMessage: ((String) -> Unit)? = null,
    onBackToHome: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val journalBackStack = remember {
        if (initialEntity != null) {
            LifoBackStack(JournalView.OVERVIEW).apply { push(JournalView.ENTITY_DETAIL) }
        } else {
            LifoBackStack(JournalView.OVERVIEW)
        }
    }
    val currentView = journalBackStack.current
    val expandedRoots = remember {
        mutableStateMapOf<RootCode, Boolean>(
            RootCode.PEOPLE to true,
            RootCode.GROUPS to true,
            RootCode.MISSION_PARTNERS to true,
            RootCode.GENERAL to true,
            RootCode.HISTORIC to true
        )
    }
    var selectedEntity by remember { mutableStateOf<IndividualEntity?>(initialEntity) }
    var isPinnedFocusExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(navigationKey) {
        if (initialEntity != null) {
            selectedEntity = initialEntity
            if (journalBackStack.current != JournalView.ENTITY_DETAIL) {
                journalBackStack.push(JournalView.ENTITY_DETAIL)
            }
        }
    }

    var localPoints by remember(selectedEntity?.id, navigationKey) {
        mutableStateOf(selectedEntity?.let { getPointsForEntity(it.id) } ?: emptyList())
    }
    var editingPoint by remember { mutableStateOf<PrayerPoint?>(null) }
    val journalPromptsCache = remember {
        mutableStateMapOf<String, List<PromptGroup>>().apply {
            repository?.getAllCachedSuggestions()?.let { putAll(it) }
        }
    }
    val journalPromptsLoading = remember { mutableStateMapOf<String, Boolean>() }
    val refreshedJournalEntities = remember { mutableSetOf<String>() }

    // Long-press context action states for entities (people/groups)
    var entityForContextActions by remember { mutableStateOf<IndividualEntity?>(null) }
    var entityToEdit by remember { mutableStateOf<IndividualEntity?>(null) }
    var editEntityName by remember { mutableStateOf("") }
    var editEntityRoot by remember { mutableStateOf(RootCode.PEOPLE) }
    var entityToDelete by remember { mutableStateOf<IndividualEntity?>(null) }
    var addingToRoot by remember { mutableStateOf<RootCode?>(null) }
    var newEntityName by remember { mutableStateOf("") }
    var newEntityRoot by remember { mutableStateOf(RootCode.PEOPLE) }

    // Long-press context action states for individual prayer records
    var pointForContextActions by remember { mutableStateOf<PrayerPoint?>(null) }
    var pointToDelete by remember { mutableStateOf<PrayerPoint?>(null) }

    // Inline draft point state when viewing an active record in ENTITY_DETAIL
    var isAddingDraftPoint by remember { mutableStateOf(false) }
    var draftPointText by remember { mutableStateOf(TextFieldValue("• ", selection = TextRange(2))) }
    val draftFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun handleJournalBack(): Boolean {
        if (isAddingDraftPoint) {
            isAddingDraftPoint = false
            draftPointText = TextFieldValue("• ", selection = TextRange(2))
            return true
        }
        return if (journalBackStack.canPop) {
            journalBackStack.pop()
            true
        } else {
            onBackToHome()
            true
        }
    }

    BackHandler(enabled = true) {
        handleJournalBack()
    }

    // Editor fields
    var editBody by remember { mutableStateOf(TextFieldValue("")) }
    var editTestimony by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

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

    val typography = remember(typography, zoomScale) {
        typography.withZoom(zoomScale)
    }

    Scaffold(
        containerColor = colors.background,
        contentColor = colors.textPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentView) {
                            JournalView.OVERVIEW -> "Journal"
                            JournalView.ENTITY_DETAIL -> selectedEntity?.displayName ?: "Prayer points"
                            JournalView.EDIT_PRAYER_POINT -> "Edit Prayer Point"
                            JournalView.SETTINGS -> "Settings"
                        },
                        style = typography.prayerPointTitle,
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    if (currentView != JournalView.OVERVIEW) {
                        IconButton(onClick = { handleJournalBack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.textPrimary
                            )
                        }
                    }
                },
                actions = {
                    if (currentView != JournalView.SETTINGS) {
                        if (zoomScale != 1.0f) {
                            Text(
                                text = "${(zoomScale * 100).roundToInt()}% ↺",
                                style = typography.marginStatus.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.leatherActive,
                                modifier = Modifier
                                    .clickable {
                                        zoomScale = 1.0f
                                        repository?.saveTextZoomScale(1.0f)
                                        showZoomPill = true
                                    }
                                    .padding(horizontal = PrayerSpacing.small, vertical = PrayerSpacing.extraSmall)
                            )
                        }
                    }
                    if (currentView == JournalView.OVERVIEW) {
                        IconButton(onClick = { journalBackStack.push(JournalView.SETTINGS) }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = colors.textSubtle
                            )
                        }
                        IconButton(onClick = onBackToHome) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                tint = colors.textSubtle
                            )
                        }
                    } else if (currentView == JournalView.EDIT_PRAYER_POINT) {
                        TextButton(
                            onClick = {
                                editingPoint?.let { point ->
                                    val updatedTestimony = if (point.status == PrayerStatus.ANSWERED) editTestimony else null
                                    localPoints = localPoints.map {
                                        if (it.id == point.id) it.copy(description = editBody.text, answeredTestimony = updatedTestimony) else it
                                    }
                                    onUpdatePrayerPoint(
                                        point.id,
                                        point.title,
                                        editBody.text,
                                        point.status,
                                        updatedTestimony
                                    )
                                    journalBackStack.pop()
                                }
                            },
                            shape = FlatSquareShape
                        ) {
                            Text(
                                text = "Save",
                                style = typography.button,
                                color = colors.textPrimary
                            )
                        }
                        TextButton(
                            onClick = onBackToHome,
                            shape = FlatSquareShape
                        ) {
                            Text(
                                text = "Home",
                                style = typography.button,
                                color = colors.textSubtle
                            )
                        }
                    } else {
                        TextButton(
                            onClick = onBackToHome,
                            shape = FlatSquareShape
                        ) {
                            Text(
                                text = "Home",
                                style = typography.button,
                                color = colors.textSubtle
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.textPrimary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .edgeSwipeRight { handleJournalBack() }
                .pinchToZoom(
                    onZoomChange = { factor ->
                        if (currentView != JournalView.SETTINGS) {
                            isZooming = true
                            zoomScale = calculateZoomScale(zoomScale, factor)
                        }
                    },
                    onZoomStart = {
                        if (currentView != JournalView.SETTINGS) {
                            isZooming = true
                        }
                    },
                    onZoomEnd = {
                        if (currentView != JournalView.SETTINGS) {
                            isZooming = false
                            repository?.saveTextZoomScale(zoomScale)
                        }
                    }
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
            HorizontalDivider(thickness = 0.5.dp, color = colors.border)

            // Sub-view Router with directional slide and fade animation
            AnimatedContent(
                targetState = currentView,
                transitionSpec = {
                    val isGoingDeeper = when {
                        initialState == JournalView.OVERVIEW && targetState == JournalView.ENTITY_DETAIL -> true
                        initialState == JournalView.OVERVIEW && targetState == JournalView.SETTINGS -> true
                        initialState == JournalView.ENTITY_DETAIL && targetState == JournalView.EDIT_PRAYER_POINT -> true
                        else -> false
                    }
                    if (isGoingDeeper) {
                        (slideInHorizontally(animationSpec = tween(320, easing = FastOutSlowInEasing)) { width -> width } +
                                fadeIn(animationSpec = tween(250)))
                            .togetherWith(
                                slideOutHorizontally(animationSpec = tween(320, easing = FastOutSlowInEasing)) { width -> -width / 4 } +
                                        fadeOut(animationSpec = tween(150))
                            )
                    } else {
                        (slideInHorizontally(animationSpec = tween(320, easing = FastOutSlowInEasing)) { width -> -width / 4 } +
                                fadeIn(animationSpec = tween(250)))
                            .togetherWith(
                                slideOutHorizontally(animationSpec = tween(320, easing = FastOutSlowInEasing)) { width -> width } +
                                        fadeOut(animationSpec = tween(150))
                            )
                    }
                },
                label = "JournalSubViewTransition",
                modifier = Modifier
                    .fillMaxSize()
            ) { view ->
                when (view) {
                    JournalView.OVERVIEW -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            val pinnedEntities = entities.filter { it.isPinned && it.rootCode != RootCode.NOTES }
                            if (pinnedEntities.isNotEmpty()) {
                                item(key = "header_pinned_focus") {
                                    Surface(
                                        onClick = {
                                            isPinnedFocusExpanded = !isPinnedFocusExpanded
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = PrayerSpacing.primaryActionHeight),
                                        shape = FlatSquareShape,
                                        color = colors.surfaceSubtle,
                                        contentColor = colors.textPrimary,
                                        tonalElevation = PrayerSpacing.elevationNone,
                                        border = BorderStroke(PrayerSpacing.hairlineWidth, colors.borderSubtle)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    horizontal = PrayerSpacing.large,
                                                    vertical = PrayerSpacing.medium
                                                ),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "PINNED FOCUS (${pinnedEntities.size})",
                                                style = typography.categoryLedgerHeader,
                                                color = colors.ribbonPrimary
                                            )
                                            Icon(
                                                imageVector = if (isPinnedFocusExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = if (isPinnedFocusExpanded) "Collapse Pinned Focus" else "Expand Pinned Focus",
                                                tint = colors.ribbonPrimary
                                            )
                                        }
                                    }
                                    HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
                                }

                                if (isPinnedFocusExpanded) {
                                    items(pinnedEntities, key = { "pinned_${it.id}" }) { entity ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .animateItem()
                                                .combinedClickable(
                                                    onClick = {
                                                        selectedEntity = entity
                                                        journalBackStack.push(JournalView.ENTITY_DETAIL)
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        entityForContextActions = entity
                                                    }
                                                ),
                                            shape = FlatSquareShape,
                                            color = colors.surface,
                                            contentColor = colors.textPrimary,
                                            tonalElevation = PrayerSpacing.elevationNone
                                        ) {
                                            Column {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(
                                                            horizontal = PrayerSpacing.large + PrayerSpacing.small,
                                                            vertical = PrayerSpacing.medium
                                                        ),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = entity.displayName,
                                                            style = typography.prayerPointBody,
                                                            color = colors.textPrimary
                                                        )
                                                        Text(
                                                            text = entity.rootCode.displayTitle,
                                                            style = typography.caption.copy(fontSize = 11.sp),
                                                            color = colors.inkMuted
                                                        )
                                                    }
                                                    Text(
                                                        text = "• PINNED",
                                                        style = typography.marginStatus,
                                                        color = colors.ribbonPrimary
                                                    )
                                                }
                                                HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
                                            }
                                        }
                                    }
                                }
                            }

                            RootCode.entries.filter { it != RootCode.NOTES }.forEach { root ->
                                item(key = "header_${root.name}") {
                                    val isExpanded = expandedRoots[root] ?: true
                                    Surface(
                                        onClick = {
                                            expandedRoots[root] = !isExpanded
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = PrayerSpacing.primaryActionHeight),
                                        shape = FlatSquareShape,
                                        color = colors.surfaceSubtle,
                                        contentColor = colors.textPrimary,
                                        tonalElevation = PrayerSpacing.elevationNone,
                                        border = BorderStroke(PrayerSpacing.hairlineWidth, colors.borderSubtle)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    horizontal = PrayerSpacing.large,
                                                    vertical = PrayerSpacing.medium
                                                ),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = root.displayTitle.uppercase(),
                                                style = typography.categoryLedgerHeader,
                                                color = colors.leatherActive
                                            )
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = if (isExpanded) "Collapse ${root.displayTitle}" else "Expand ${root.displayTitle}",
                                                tint = colors.leatherActive
                                            )
                                        }
                                    }
                                    HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
                                }

                                val isExpanded = expandedRoots[root] ?: true
                                if (isExpanded) {
                                    val groupEntities = entities.filter { it.rootCode == root }.sortedWith(
                                        compareByDescending<IndividualEntity> { it.isPinned }
                                            .thenBy { it.displayName.lowercase() }
                                    )

                                    // Option to add at the top of each listing under each group (except Historic)
                                    if (root != RootCode.HISTORIC) {
                                        item(key = "add_${root.name}") {
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(min = PrayerSpacing.primaryActionHeight)
                                                    .clickable {
                                                        addingToRoot = root
                                                        newEntityRoot = root
                                                        newEntityName = ""
                                                    },
                                                shape = FlatSquareShape,
                                                color = colors.surface,
                                                contentColor = colors.textPrimary,
                                                tonalElevation = PrayerSpacing.elevationNone
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(
                                                            horizontal = PrayerSpacing.large + PrayerSpacing.small,
                                                            vertical = PrayerSpacing.medium
                                                        ),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = when (root) {
                                                            RootCode.PEOPLE -> "+ Add person"
                                                            RootCode.GROUPS -> "+ Add group"
                                                            RootCode.MISSION_PARTNERS -> "+ Add mission partner"
                                                            RootCode.GENERAL -> "+ Add topic"
                                                            RootCode.HISTORIC -> ""
                                                            RootCode.NOTES -> ""
                                                        },
                                                        style = typography.button,
                                                        color = colors.leatherActive
                                                    )
                                                }
                                            }
                                            HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
                                        }
                                    }

                                    if (groupEntities.isEmpty()) {
                                        item(key = "empty_${root.name}") {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        horizontal = PrayerSpacing.large + PrayerSpacing.small,
                                                        vertical = PrayerSpacing.medium
                                                    )
                                            ) {
                                                Text(
                                                    text = "No records yet",
                                                    style = typography.caption,
                                                    color = colors.textSubtle
                                                )
                                            }
                                            HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
                                        }
                                    } else {
                                        items(groupEntities, key = { it.id }) { entity ->
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .animateItem()
                                                    .combinedClickable(
                                                        onClick = {
                                                            selectedEntity = entity
                                                            journalBackStack.push(JournalView.ENTITY_DETAIL)
                                                        },
                                                        onLongClick = {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            entityForContextActions = entity
                                                        }
                                                    ),
                                                shape = FlatSquareShape,
                                                color = colors.surface,
                                                contentColor = colors.textPrimary,
                                                tonalElevation = PrayerSpacing.elevationNone
                                            ) {
                                                Column {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(
                                                                horizontal = PrayerSpacing.large + PrayerSpacing.small,
                                                                vertical = PrayerSpacing.medium
                                                            ),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = entity.displayName,
                                                            style = typography.prayerPointBody,
                                                            color = colors.textPrimary,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        if (entity.isPinned) {
                                                            Text(
                                                                text = "• PINNED",
                                                                style = typography.marginStatus,
                                                                color = colors.ribbonPrimary
                                                            )
                                                        }
                                                    }
                                                    HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    JournalView.ENTITY_DETAIL -> {
                        selectedEntity?.let { rawEntity ->
                            val entity = entities.find { it.id == rawEntity.id } ?: rawEntity
                            val points = localPoints
                            var isJournalPromptsExpanded by remember(entity.id) { mutableStateOf(false) }

                            LaunchedEffect(entity.id) {
                                // Ensure cached prompts from last time are immediately in memory
                                if (!journalPromptsCache.containsKey(entity.id)) {
                                    repository?.getCachedSuggestions(entity.id)?.let { cached ->
                                        if (cached.isNotEmpty()) {
                                            journalPromptsCache[entity.id] = cached
                                        }
                                    }
                                }

                                // Refresh in background while showing the last list of prompts
                                if (apiClient != null && points.isNotEmpty() && !entity.isPreloadedHistoric && !refreshedJournalEntities.contains(entity.id)) {
                                    refreshedJournalEntities.add(entity.id)
                                    journalPromptsLoading[entity.id] = true
                                    val contextData = repository?.getTargetContext(entity.id)
                                    val recorded = (contextData?.activePoints.orEmpty() + contextData?.answeredPoints.orEmpty()).ifEmpty {
                                        points
                                    }.map {
                                        RecordedPoint(title = it.title, body = it.description, status = it.status.name)
                                    }
                                    val request = SuggestRequest(
                                        targetName = entity.displayName,
                                        root = entity.rootCode.name,
                                        group = null,
                                        contextDescription = entity.contextDescription?.takeIf { it.isNotBlank() },
                                        recordedPoints = recorded,
                                        journalUpdates = emptyList(),
                                        currentDraft = null,
                                        localeDialect = "EN_AU_UK"
                                    )
                                    val result = apiClient.getSuggestions(request)
                                    journalPromptsLoading[entity.id] = false
                                    result.onSuccess { resp ->
                                        if (resp.promptGroups.isNotEmpty()) {
                                            repository?.saveCachedSuggestions(entity.id, resp)
                                            journalPromptsCache[entity.id] = resp.promptGroups
                                        }
                                    }.onFailure {
                                        journalPromptsLoading[entity.id] = false
                                    }
                                }
                            }

                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Read-Only AI Prompts on past points state
                                    val cachedGroups = journalPromptsCache[entity.id].orEmpty()
                                    val isLoadingPrompts = journalPromptsLoading[entity.id] == true

                                    val groupedPoints = remember(points) {
                                        points.groupBy { formatJournalDate(it.createdAt) }
                                    }

                                    val listState = rememberLazyListState()

                                    LaunchedEffect(isAddingDraftPoint) {
                                        if (isAddingDraftPoint) {
                                            delay(60)
                                            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount.coerceAtLeast(1) - 1)
                                            repeat(5) {
                                                try {
                                                    draftFocusRequester.requestFocus()
                                                    keyboardController?.show()
                                                    return@LaunchedEffect
                                                } catch (_: Exception) {
                                                    delay(50)
                                                }
                                            }
                                        }
                                    }

                                    LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
                                        groupedPoints.forEach { (dateHeader, datePoints) ->
                                            item(key = "date_header_${entity.id}_$dateHeader") {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(
                                                            start = PrayerSpacing.textInset,
                                                            end = 36.dp,
                                                            top = PrayerSpacing.medium,
                                                            bottom = PrayerSpacing.extraSmall
                                                        )
                                                ) {
                                                    Text(
                                                        text = dateHeader,
                                                        style = typography.marginStatus,
                                                        color = colors.inkMuted
                                                    )
                                                }
                                                HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
                                            }

                                            items(datePoints, key = { it.id }) { point ->
                                                val isAnswered = point.status == PrayerStatus.ANSWERED
                                                val isPreloaded = entity.isPreloadedHistoric || point.status == PrayerStatus.HISTORIC
                                                Surface(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .animateItem()
                                                        .combinedClickable(
                                                            onClick = {
                                                                editingPoint = point
                                                                editBody = TextFieldValue(point.description, selection = TextRange(point.description.length))
                                                                editTestimony = point.answeredTestimony ?: ""
                                                                showDeleteConfirm = false
                                                                journalBackStack.push(JournalView.EDIT_PRAYER_POINT)
                                                            },
                                                            onLongClick = {
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                pointForContextActions = point
                                                            }
                                                        ),
                                                    shape = FlatSquareShape,
                                                    color = colors.surface,
                                                    contentColor = colors.textPrimary,
                                                    tonalElevation = PrayerSpacing.elevationNone
                                                ) {
                                                    Column {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(IntrinsicSize.Min)
                                                        ) {
                                                            // Left Track: 37.52dp margin track with [ ACTIVE ] or [ ANSWERED ] pill
                                                            Box(
                                                                modifier = Modifier
                                                                    .width(PrayerSpacing.marginTrackWidth)
                                                                    .fillMaxHeight()
                                                                    .padding(top = PrayerSpacing.medium),
                                                                contentAlignment = Alignment.TopCenter
                                                            ) {
                                                                if (!isPreloaded) {
                                                                    Surface(
                                                                        onClick = {
                                                                            val newStatus = if (isAnswered) PrayerStatus.ACTIVE else PrayerStatus.ANSWERED
                                                                            val testimony = if (newStatus == PrayerStatus.ANSWERED) point.answeredTestimony else null
                                                                            localPoints = localPoints.map {
                                                                                if (it.id == point.id) it.copy(status = newStatus, answeredTestimony = testimony) else it
                                                                            }
                                                                            onUpdatePrayerPoint(point.id, point.title, point.description, newStatus, testimony)
                                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                        },
                                                                        shape = RoundedCornerShape(4.dp),
                                                                        border = BorderStroke(0.5.dp, if (isAnswered) colors.inkAnswered.copy(alpha = 0.5f) else colors.inkMuted.copy(alpha = 0.5f)),
                                                                        color = colors.surface
                                                                    ) {
                                                                        if (isAnswered) {
                                                                            Text(
                                                                                text = "ANSWERED",
                                                                                style = typography.marginStatus,
                                                                                color = colors.inkAnswered,
                                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                                            )
                                                                        } else {
                                                                            Icon(
                                                                                imageVector = Icons.Outlined.Edit,
                                                                                contentDescription = "Active prayer",
                                                                                tint = colors.inkMuted,
                                                                                modifier = Modifier
                                                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                                                                    .size(12.dp)
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            // 0.75dp red/sepia vertical margin guide rule
                                                            Box(
                                                                modifier = Modifier
                                                                    .width(PrayerSpacing.hairlineWidth)
                                                                    .fillMaxHeight()
                                                                    .background(colors.paperMarginRule)
                                                            )

                                                            // Right Track: Prayer Text Canvas (starts 8dp past rule, giving 37.52+8=45.52dp textInset)
                                                            Column(
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .padding(
                                                                        start = 8.dp,
                                                                        end = 36.dp,
                                                                        top = PrayerSpacing.medium,
                                                                        bottom = PrayerSpacing.medium
                                                                    )
                                                            ) {
                                                                val bulletText = if (point.description.trimStart().startsWith("•")) {
                                                                    point.description
                                                                } else {
                                                                    "• ${point.description}"
                                                                }
                                                                Text(
                                                                    text = bulletText,
                                                                    style = if (isAnswered) {
                                                                        typography.answeredThanksgiving.copy(textDecoration = TextDecoration.LineThrough)
                                                                    } else {
                                                                        typography.prayerPointBody
                                                                    },
                                                                    color = if (isAnswered) colors.inkAnswered else colors.textPrimary
                                                                )
                                                                if (isAnswered && !point.answeredTestimony.isNullOrBlank()) {
                                                                    Spacer(modifier = Modifier.height(PrayerSpacing.small))
                                                                    Text(
                                                                        text = "Thanksgiving: ${point.answeredTestimony}",
                                                                        style = typography.caption,
                                                                        color = colors.inkAnswered,
                                                                        modifier = Modifier.padding(start = PrayerSpacing.large)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
                                                    }
                                                }
                                            }
                                        }

                                        // Inline editable draft point below existing points
                                        if (isAddingDraftPoint) {
                                            item(key = "draft_point_${entity.id}") {
                                                Surface(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = FlatSquareShape,
                                                    color = colors.surfaceSubtle,
                                                    contentColor = colors.textPrimary,
                                                    tonalElevation = PrayerSpacing.elevationNone
                                                ) {
                                                    Column {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(IntrinsicSize.Min)
                                                        ) {
                                                            // Left Track: 37.52dp margin track with [ DRAFT ] pill
                                                            Box(
                                                                modifier = Modifier
                                                                    .width(PrayerSpacing.marginTrackWidth)
                                                                    .fillMaxHeight()
                                                                    .padding(top = PrayerSpacing.medium),
                                                                contentAlignment = Alignment.TopCenter
                                                            ) {
                                                                Surface(
                                                                    shape = RoundedCornerShape(4.dp),
                                                                    border = BorderStroke(0.5.dp, colors.leatherPrimary.copy(alpha = 0.5f)),
                                                                    color = colors.surface
                                                                ) {
                                                                    Text(
                                                                        text = "DRAFT",
                                                                        style = typography.marginStatus,
                                                                        color = colors.leatherPrimary,
                                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                                    )
                                                                }
                                                            }

                                                            // 0.75dp red/sepia vertical margin guide rule
                                                            Box(
                                                                modifier = Modifier
                                                                    .width(PrayerSpacing.hairlineWidth)
                                                                    .fillMaxHeight()
                                                                    .background(colors.paperMarginRule)
                                                            )

                                                            // Right Track: Prayer Text Canvas
                                                            Column(
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .padding(
                                                                        start = 8.dp,
                                                                        end = 16.dp,
                                                                        top = PrayerSpacing.medium,
                                                                        bottom = PrayerSpacing.medium
                                                                    )
                                                            ) {
                                                                BasicTextField(
                                                                    value = draftPointText,
                                                                    onValueChange = { draftPointText = it },
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .focusRequester(draftFocusRequester),
                                                                    textStyle = typography.prayerPointBody.copy(color = colors.textPrimary),
                                                                    cursorBrush = SolidColor(colors.textPrimary),
                                                                    decorationBox = { innerTextField ->
                                                                        if (draftPointText.text.isEmpty()) {
                                                                            Text(
                                                                                text = "• Enter prayer point...",
                                                                                style = typography.prayerPointBody,
                                                                                color = colors.textSubtle
                                                                            )
                                                                        }
                                                                        innerTextField()
                                                                    }
                                                                )

                                                                Spacer(modifier = Modifier.height(PrayerSpacing.medium))

                                                                // Inline Action Buttons: Cancel and Save
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.End,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    TextButton(
                                                                        onClick = {
                                                                            isAddingDraftPoint = false
                                                                            draftPointText = TextFieldValue("• ", selection = TextRange(2))
                                                                        },
                                                                        shape = FlatSquareShape
                                                                    ) {
                                                                        Text("Cancel", style = typography.caption, color = colors.textSubtle)
                                                                    }

                                                                    Spacer(modifier = Modifier.width(PrayerSpacing.small))

                                                                    val hasValidDraft = draftPointText.text.replace("•", "").trim().isNotBlank()
                                                                    Button(
                                                                        onClick = {
                                                                            val pointsToSave = splitIntoDotpoints(draftPointText.text)
                                                                            if (pointsToSave.isNotEmpty()) {
                                                                                val saved = repository?.savePrayerPoints(entity.id, pointsToSave)
                                                                                    ?: pointsToSave.map { desc ->
                                                                                        PrayerPoint(entityId = entity.id, title = "", description = desc)
                                                                                    }
                                                                                localPoints = repository?.getPointsForEntity(entity.id) ?: (localPoints + saved)
                                                                                onSavePrayerPoints?.invoke(entity.id, pointsToSave)
                                                                                isAddingDraftPoint = false
                                                                                draftPointText = TextFieldValue("• ", selection = TextRange(2))
                                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                                onShowMessage?.invoke(
                                                                                    if (pointsToSave.size > 1) "Saved ${pointsToSave.size} prayer points"
                                                                                    else "Prayer point saved"
                                                                                )
                                                                            }
                                                                        },
                                                                        enabled = hasValidDraft,
                                                                        shape = FlatSquareShape,
                                                                        colors = ButtonDefaults.buttonColors(
                                                                            containerColor = colors.leatherPrimary,
                                                                            contentColor = Color.White
                                                                        ),
                                                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                                                    ) {
                                                                        Text(
                                                                            text = "Save Point",
                                                                            style = typography.button.copy(fontSize = 13.sp),
                                                                            color = Color.White
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
                                                    }
                                                }
                                            }
                                        }

                                        // Quick Action: Add prayer point button below list when not drafting
                                        if (!isAddingDraftPoint && !entity.isPreloadedHistoric) {
                                            item(key = "add_point_${entity.id}") {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(PrayerSpacing.primaryActionHeight),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            draftPointText = TextFieldValue("• ", selection = TextRange(2))
                                                            isAddingDraftPoint = true
                                                        },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .fillMaxHeight(),
                                                        shape = FlatSquareShape,
                                                        border = BorderStroke(PrayerSpacing.hairlineWidth, colors.border),
                                                        colors = ButtonDefaults.outlinedButtonColors(
                                                            containerColor = colors.surface,
                                                            contentColor = colors.textPrimary
                                                        )
                                                    ) {
                                                        Text("+ Add prayer point", style = typography.button, color = colors.textPrimary)
                                                    }
                                                }
                                                HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
                                                Spacer(modifier = Modifier.height(PrayerSpacing.medium))
                                            }
                                        }

                                        // Read-Only AI Prompts on past points (Grouped into Praise God, Thank God, Ask God) - Collapsed by default
                                        if (!isAddingDraftPoint && !entity.isPreloadedHistoric && (cachedGroups.isNotEmpty() || isLoadingPrompts)) {
                                            item(key = "prompts_${entity.id}") {
                                                Surface(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(
                                                            horizontal = PrayerSpacing.large,
                                                            vertical = PrayerSpacing.small
                                                        )
                                                        .clickable { isJournalPromptsExpanded = !isJournalPromptsExpanded },
                                                    shape = FlatSquareShape,
                                                    color = colors.surfaceSubtle,
                                                    tonalElevation = PrayerSpacing.elevationNone,
                                                    border = BorderStroke(PrayerSpacing.hairlineWidth, colors.borderSubtle)
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = PrayerSpacing.medium, vertical = PrayerSpacing.small)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .heightIn(min = PrayerSpacing.minTouchTarget),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(
                                                                text = "Prompts for Prayer",
                                                                style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
                                                                color = colors.leatherPrimary
                                                            )
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                if (isLoadingPrompts && cachedGroups.isEmpty()) {
                                                                    CircularProgressIndicator(
                                                                        modifier = Modifier.size(12.dp),
                                                                        strokeWidth = 1.dp,
                                                                        color = colors.textSubtle
                                                                    )
                                                                    Spacer(modifier = Modifier.width(PrayerSpacing.small))
                                                                }
                                                                Text(
                                                                    text = if (isJournalPromptsExpanded) "Hide" else "Show",
                                                                    style = typography.marginStatus,
                                                                    color = colors.inkMuted
                                                                )
                                                            }
                                                        }

                                                        if (isJournalPromptsExpanded) {
                                                            Spacer(modifier = Modifier.height(PrayerSpacing.extraSmall))
                                                            HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
                                                            Spacer(modifier = Modifier.height(PrayerSpacing.small))

                                                            if (isLoadingPrompts && cachedGroups.isEmpty()) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .padding(vertical = PrayerSpacing.medium),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    CircularProgressIndicator(
                                                                        modifier = Modifier.size(16.dp),
                                                                        strokeWidth = 1.5.dp,
                                                                        color = colors.textSubtle
                                                                    )
                                                                }
                                                            }
                                                            if (cachedGroups.isNotEmpty()) {
                                                                cachedGroups.forEach { group ->
                                                                    Text(
                                                                        text = group.title,
                                                                        style = typography.categoryLedgerHeader,
                                                                        color = colors.leatherPrimary,
                                                                        modifier = Modifier.padding(top = PrayerSpacing.small, bottom = PrayerSpacing.extraSmall)
                                                                    )
                                                                    group.prompts.forEach { prompt ->
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
                                                }
                                                HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
                                                Spacer(modifier = Modifier.height(PrayerSpacing.large))
                                            }
                                        }
                                    }
                                }

                                // Interactive Silk Marker Ribbon Tab anchored at top margin
                                SilkMarkerRibbon(
                                    isPinned = entity.isPinned,
                                    onTogglePin = {
                                        val newPinned = !entity.isPinned
                                        selectedEntity = entity.copy(isPinned = newPinned)
                                        onTogglePinEntity?.invoke(entity.id, newPinned)
                                    },
                                    color = colors.ribbonPrimary,
                                    modifier = Modifier.align(Alignment.TopEnd)
                                )
                            }
                        }
                    }

                    JournalView.EDIT_PRAYER_POINT -> {
                        editingPoint?.let { point ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(PrayerSpacing.large)
                            ) {
                                LinedNotepad(
                                    text = editBody,
                                    onTextChange = { editBody = it },
                                    colors = colors,
                                    typography = typography,
                                    placeholder = "Prayer point...",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    textStyle = typography.prayerPointBody,
                                    autoFocus = true
                                )

                                if (point.status == PrayerStatus.ANSWERED) {
                                    Spacer(modifier = Modifier.height(PrayerSpacing.medium))
                                    OutlinedTextField(
                                        value = editTestimony,
                                        onValueChange = { editTestimony = it },
                                        label = { Text("Thanksgiving note", style = typography.caption) },
                                        textStyle = typography.prayerPointBody.copy(color = colors.textPrimary),
                                        shape = FlatSquareShape,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = colors.textPrimary,
                                            unfocusedBorderColor = colors.border,
                                            focusedLabelColor = colors.textPrimary,
                                            unfocusedLabelColor = colors.textSubtle,
                                            cursorColor = colors.textPrimary
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(PrayerSpacing.large))

                                // Save Changes Action
                                Button(
                                    onClick = {
                                        val updatedTestimony = if (point.status == PrayerStatus.ANSWERED) editTestimony else null
                                        localPoints = localPoints.map {
                                            if (it.id == point.id) it.copy(description = editBody.text, answeredTestimony = updatedTestimony) else it
                                        }
                                        onUpdatePrayerPoint(
                                            point.id,
                                            point.title,
                                            editBody.text,
                                            point.status,
                                            updatedTestimony
                                        )
                                        journalBackStack.pop()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(PrayerSpacing.primaryActionHeight),
                                    shape = FlatSquareShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.textPrimary,
                                        contentColor = colors.background
                                    )
                                ) {
                                    Text("Save changes", style = typography.button)
                                }

                                Spacer(modifier = Modifier.height(PrayerSpacing.small))

                                // Permanent Delete Action
                                TextButton(
                                    onClick = { showDeleteConfirm = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(PrayerSpacing.minTouchTarget),
                                    shape = FlatSquareShape
                                ) {
                                    Text("Delete prayer point", style = typography.caption, color = colors.textSubtle)
                                }

                                // Material 3 Permanent Deletion Alert Dialog
                                if (showDeleteConfirm) {
                                    AlertDialog(
                                        onDismissRequest = { showDeleteConfirm = false },
                                        title = {
                                             Text(
                                                 text = "Delete prayer point?",
                                                 style = typography.prayerPointTitle,
                                                 color = colors.textPrimary
                                             )
                                        },
                                        text = {
                                            Text(
                                                text = "Delete this prayer point? This cannot be undone.",
                                                style = typography.caption,
                                                color = colors.textSubtle
                                            )
                                        },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    showDeleteConfirm = false
                                                    localPoints = localPoints.filter { it.id != point.id }
                                                    onDeletePrayerPoint(point.id)
                                                    journalBackStack.pop()
                                                },
                                                shape = FlatSquareShape,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = colors.textPrimary,
                                                    contentColor = colors.background
                                                )
                                            ) {
                                                Text("Delete", style = typography.button)
                                            }
                                        },
                                        dismissButton = {
                                            OutlinedButton(
                                                onClick = { showDeleteConfirm = false },
                                                shape = FlatSquareShape,
                                                border = BorderStroke(0.5.dp, colors.border),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = colors.textSubtle
                                                )
                                            ) {
                                                Text("Cancel", style = typography.button)
                                            }
                                        },
                                        shape = FlatSquareShape,
                                        containerColor = colors.surface,
                                        tonalElevation = 0.dp
                                    )
                                }
                            }
                        }
                    }

                    JournalView.SETTINGS -> {
                        SettingsScreen(
                            config = config,
                            colors = colors,
                            typography = typography,
                            onUpdateConfig = onUpdateConfig,
                            repository = repository,
                            onVaultRestored = onVaultRestored,
                            onShowMessage = onShowMessage
                        )
                    }
                }
            }
        }

        // Floating Zoom Indicator Pill
            AnimatedVisibility(
                visible = (isZooming || showZoomPill) && (currentView != JournalView.SETTINGS),
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
                            style = typography.caption.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = colors.background
                        )
                        if (zoomScale != 1.0f) {
                            Text(
                                text = "• Reset",
                                style = typography.caption.copy(
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

    // --- Entity Context Action Dialogs ---

    if (entityForContextActions != null) {
        val entity = entityForContextActions!!
        val isHistoric = entity.isPreloadedHistoric && entity.rootCode == RootCode.HISTORIC
        AlertDialog(
            onDismissRequest = { entityForContextActions = null },
            title = {
                Text(
                    text = entity.displayName,
                    style = typography.prayerPointTitle,
                    color = colors.textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                ) {
                    OutlinedButton(
                        onClick = {
                            val target = entity
                            entityForContextActions = null
                            selectedEntity = target
                            localPoints = getPointsForEntity(target.id)
                            draftPointText = TextFieldValue("• ", selection = TextRange(2))
                            isAddingDraftPoint = true
                            journalBackStack.push(JournalView.ENTITY_DETAIL)
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
                        Text("+ Add prayer point", style = typography.button)
                    }

                    if (!isHistoric) {
                        OutlinedButton(
                            onClick = {
                                entityToEdit = entity
                                editEntityName = entity.displayName
                                editEntityRoot = entity.rootCode
                                entityForContextActions = null
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
                            Text("Edit name & category", style = typography.button)
                        }

                        OutlinedButton(
                            onClick = {
                                entityToDelete = entity
                                entityForContextActions = null
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
                            Text("Delete", style = typography.button)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { entityForContextActions = null },
                    shape = FlatSquareShape
                ) {
                    Text("Cancel", style = typography.button, color = colors.textSubtle)
                }
            },
            shape = FlatSquareShape,
            containerColor = colors.surface,
            tonalElevation = 0.dp
        )
    }

    if (entityToEdit != null) {
        val entity = entityToEdit!!
        AlertDialog(
            onDismissRequest = { entityToEdit = null },
            title = {
                Text(
                    text = "Edit person, group, topic, or mission partner",
                    style = typography.prayerPointTitle,
                    color = colors.textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = editEntityName,
                        onValueChange = { editEntityName = it },
                        label = { Text("Name", style = typography.caption) },
                        textStyle = typography.prayerPointBody.copy(color = colors.textPrimary),
                        shape = FlatSquareShape,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.textPrimary,
                            unfocusedBorderColor = colors.border,
                            focusedLabelColor = colors.textPrimary,
                            unfocusedLabelColor = colors.textSubtle,
                            cursorColor = colors.textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(PrayerSpacing.medium))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                    ) {
                        val isPeople = editEntityRoot == RootCode.PEOPLE
                        val peopleBg by animateColorAsState(
                            targetValue = if (isPeople) colors.textPrimary else colors.surface,
                            label = "EditPeopleBg"
                        )
                        val peopleText by animateColorAsState(
                            targetValue = if (isPeople) colors.background else colors.textPrimary,
                            label = "EditPeopleText"
                        )

                        Button(
                            onClick = { editEntityRoot = RootCode.PEOPLE },
                            modifier = Modifier
                                .weight(1f)
                                .height(PrayerSpacing.minTouchTarget),
                            shape = FlatSquareShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = peopleBg,
                                contentColor = peopleText
                            ),
                            border = BorderStroke(if (isPeople) 1.5.dp else 0.5.dp, colors.border)
                        ) {
                            Text("People", style = typography.button)
                        }

                        val isGroups = editEntityRoot == RootCode.GROUPS
                        val groupsBg by animateColorAsState(
                            targetValue = if (isGroups) colors.textPrimary else colors.surface,
                            label = "EditGroupsBg"
                        )
                        val groupsText by animateColorAsState(
                            targetValue = if (isGroups) colors.background else colors.textPrimary,
                            label = "EditGroupsText"
                        )

                        Button(
                            onClick = { editEntityRoot = RootCode.GROUPS },
                            modifier = Modifier
                                .weight(1f)
                                .height(PrayerSpacing.minTouchTarget),
                            shape = FlatSquareShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = groupsBg,
                                contentColor = groupsText
                            ),
                            border = BorderStroke(if (isGroups) 1.5.dp else 0.5.dp, colors.border)
                        ) {
                            Text("Groups", style = typography.button)
                        }

                        val isMission = editEntityRoot == RootCode.MISSION_PARTNERS
                        val missionBg by animateColorAsState(
                            targetValue = if (isMission) colors.textPrimary else colors.surface,
                            label = "EditMissionBg"
                        )
                        val missionText by animateColorAsState(
                            targetValue = if (isMission) colors.background else colors.textPrimary,
                            label = "EditMissionText"
                        )

                        Button(
                            onClick = { editEntityRoot = RootCode.MISSION_PARTNERS },
                            modifier = Modifier
                                .weight(1f)
                                .height(PrayerSpacing.minTouchTarget),
                            shape = FlatSquareShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = missionBg,
                                contentColor = missionText
                            ),
                            border = BorderStroke(if (isMission) 1.5.dp else 0.5.dp, colors.border)
                        ) {
                            Text("Mission Partners", style = typography.button)
                        }

                        val isGeneral = editEntityRoot == RootCode.GENERAL
                        val generalBg by animateColorAsState(
                            targetValue = if (isGeneral) colors.textPrimary else colors.surface,
                            label = "EditGeneralBg"
                        )
                        val generalText by animateColorAsState(
                            targetValue = if (isGeneral) colors.background else colors.textPrimary,
                            label = "EditGeneralText"
                        )

                        Button(
                            onClick = { editEntityRoot = RootCode.GENERAL },
                            modifier = Modifier
                                .weight(1f)
                                .height(PrayerSpacing.minTouchTarget),
                            shape = FlatSquareShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = generalBg,
                                contentColor = generalText
                            ),
                            border = BorderStroke(if (isGeneral) 1.5.dp else 0.5.dp, colors.border)
                        ) {
                            Text("General", style = typography.button)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editEntityName.isNotBlank()) {
                            onUpdateEntity(entity.id, editEntityName.trim(), editEntityRoot)
                            if (selectedEntity?.id == entity.id) {
                                selectedEntity = entity.copy(displayName = editEntityName.trim(), rootCode = editEntityRoot)
                            }
                            entityToEdit = null
                        }
                    },
                    enabled = editEntityName.isNotBlank(),
                    shape = FlatSquareShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.textPrimary,
                        contentColor = colors.background,
                        disabledContainerColor = colors.border,
                        disabledContentColor = colors.textSubtle
                    )
                ) {
                    Text("Save", style = typography.button)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { entityToEdit = null },
                    shape = FlatSquareShape,
                    border = BorderStroke(0.5.dp, colors.border),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.textSubtle
                    )
                ) {
                    Text("Cancel", style = typography.button)
                }
            },
            shape = FlatSquareShape,
            containerColor = colors.surface,
            tonalElevation = 0.dp
        )
    }

    if (addingToRoot != null) {
        AlertDialog(
            onDismissRequest = {
                addingToRoot = null
                newEntityName = ""
            },
            title = {
                Text(
                    text = "Add to ${newEntityRoot.displayTitle}",
                    style = typography.prayerPointTitle,
                    color = colors.textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = newEntityName,
                        onValueChange = { newEntityName = it },
                        label = { Text("Name", style = typography.caption) },
                        textStyle = typography.prayerPointBody.copy(color = colors.textPrimary),
                        shape = FlatSquareShape,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.textPrimary,
                            unfocusedBorderColor = colors.border,
                            focusedLabelColor = colors.textPrimary,
                            unfocusedLabelColor = colors.textSubtle,
                            cursorColor = colors.textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(PrayerSpacing.medium))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                    ) {
                        val isPeople = newEntityRoot == RootCode.PEOPLE
                        val peopleBg by animateColorAsState(
                            targetValue = if (isPeople) colors.textPrimary else colors.surface,
                            label = "NewPeopleBg"
                        )
                        val peopleText by animateColorAsState(
                            targetValue = if (isPeople) colors.background else colors.textPrimary,
                            label = "NewPeopleText"
                        )

                        Button(
                            onClick = { newEntityRoot = RootCode.PEOPLE },
                            modifier = Modifier
                                .weight(1f)
                                .height(PrayerSpacing.minTouchTarget),
                            shape = FlatSquareShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = peopleBg,
                                contentColor = peopleText
                            ),
                            border = BorderStroke(if (isPeople) 1.5.dp else 0.5.dp, colors.border)
                        ) {
                            Text("People", style = typography.button)
                        }

                        val isGroups = newEntityRoot == RootCode.GROUPS
                        val groupsBg by animateColorAsState(
                            targetValue = if (isGroups) colors.textPrimary else colors.surface,
                            label = "NewGroupsBg"
                        )
                        val groupsText by animateColorAsState(
                            targetValue = if (isGroups) colors.background else colors.textPrimary,
                            label = "NewGroupsText"
                        )

                        Button(
                            onClick = { newEntityRoot = RootCode.GROUPS },
                            modifier = Modifier
                                .weight(1f)
                                .height(PrayerSpacing.minTouchTarget),
                            shape = FlatSquareShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = groupsBg,
                                contentColor = groupsText
                            ),
                            border = BorderStroke(if (isGroups) 1.5.dp else 0.5.dp, colors.border)
                        ) {
                            Text("Groups", style = typography.button)
                        }
                    }

                    Spacer(modifier = Modifier.height(PrayerSpacing.small))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                    ) {
                        val isMission = newEntityRoot == RootCode.MISSION_PARTNERS
                        val missionBg by animateColorAsState(
                            targetValue = if (isMission) colors.textPrimary else colors.surface,
                            label = "NewMissionBg"
                        )
                        val missionText by animateColorAsState(
                            targetValue = if (isMission) colors.background else colors.textPrimary,
                            label = "NewMissionText"
                        )

                        Button(
                            onClick = { newEntityRoot = RootCode.MISSION_PARTNERS },
                            modifier = Modifier
                                .weight(1f)
                                .height(PrayerSpacing.minTouchTarget),
                            shape = FlatSquareShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = missionBg,
                                contentColor = missionText
                            ),
                            border = BorderStroke(if (isMission) 1.5.dp else 0.5.dp, colors.border)
                        ) {
                            Text("Mission Partners", style = typography.button)
                        }

                        val isGeneral = newEntityRoot == RootCode.GENERAL
                        val generalBg by animateColorAsState(
                            targetValue = if (isGeneral) colors.textPrimary else colors.surface,
                            label = "NewGeneralBg"
                        )
                        val generalText by animateColorAsState(
                            targetValue = if (isGeneral) colors.background else colors.textPrimary,
                            label = "NewGeneralText"
                        )

                        Button(
                            onClick = { newEntityRoot = RootCode.GENERAL },
                            modifier = Modifier
                                .weight(1f)
                                .height(PrayerSpacing.minTouchTarget),
                            shape = FlatSquareShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = generalBg,
                                contentColor = generalText
                            ),
                            border = BorderStroke(if (isGeneral) 1.5.dp else 0.5.dp, colors.border)
                        ) {
                            Text("General", style = typography.button)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newEntityName.isNotBlank() && onCreateEntity != null) {
                            val created = onCreateEntity(newEntityRoot, newEntityName.trim())
                            addingToRoot = null
                            newEntityName = ""
                            selectedEntity = created
                            journalBackStack.push(JournalView.ENTITY_DETAIL)
                        }
                    },
                    enabled = newEntityName.isNotBlank(),
                    shape = FlatSquareShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.textPrimary,
                        contentColor = colors.background,
                        disabledContainerColor = colors.border,
                        disabledContentColor = colors.textSubtle
                    )
                ) {
                    Text("Add", style = typography.button)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        addingToRoot = null
                        newEntityName = ""
                    },
                    shape = FlatSquareShape,
                    border = BorderStroke(0.5.dp, colors.border),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.textSubtle
                    )
                ) {
                    Text("Cancel", style = typography.button)
                }
            },
            shape = FlatSquareShape,
            containerColor = colors.surface,
            tonalElevation = 0.dp
        )
    }

    if (entityToDelete != null) {
        val entity = entityToDelete!!
        AlertDialog(
            onDismissRequest = { entityToDelete = null },
            title = {
                Text(
                    text = "Delete ${entity.displayName}?",
                    style = typography.prayerPointTitle,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Delete this person, group, general topic, or mission partner and all associated prayer points? This cannot be undone.",
                    style = typography.caption,
                    color = colors.textSubtle
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idToDelete = entity.id
                        entityToDelete = null
                        if (selectedEntity?.id == idToDelete) {
                            selectedEntity = null
                            if (currentView == JournalView.ENTITY_DETAIL) {
                                journalBackStack.pop()
                            }
                        }
                        onDeleteEntity(idToDelete)
                    },
                    shape = FlatSquareShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.textPrimary,
                        contentColor = colors.background
                    )
                ) {
                    Text("Delete", style = typography.button)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { entityToDelete = null },
                    shape = FlatSquareShape,
                    border = BorderStroke(0.5.dp, colors.border),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.textSubtle
                    )
                ) {
                    Text("Cancel", style = typography.button)
                }
            },
            shape = FlatSquareShape,
            containerColor = colors.surface,
            tonalElevation = 0.dp
        )
    }

    // --- Prayer Point Context Action Dialogs ---

    if (pointForContextActions != null) {
        val point = pointForContextActions!!
        val isAnswered = point.status == PrayerStatus.ANSWERED
        val currentEntity = selectedEntity ?: entities.find { it.id == point.entityId }
        val isPreloaded = currentEntity?.isPreloadedHistoric == true || point.status == PrayerStatus.HISTORIC
        AlertDialog(
            onDismissRequest = { pointForContextActions = null },
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
                    if (!isPreloaded) {
                        OutlinedButton(
                            onClick = {
                                val newStatus = if (isAnswered) PrayerStatus.ACTIVE else PrayerStatus.ANSWERED
                                val testimony = if (newStatus == PrayerStatus.ANSWERED) point.answeredTestimony else null
                                localPoints = localPoints.map {
                                    if (it.id == point.id) it.copy(status = newStatus, answeredTestimony = testimony) else it
                                }
                                onUpdatePrayerPoint(point.id, point.title, point.description, newStatus, testimony)
                                pointForContextActions = null
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

                    OutlinedButton(
                        onClick = {
                            editingPoint = point
                            editBody = TextFieldValue(point.description, selection = TextRange(point.description.length))
                            editTestimony = point.answeredTestimony ?: ""
                            showDeleteConfirm = false
                            pointForContextActions = null
                            journalBackStack.push(JournalView.EDIT_PRAYER_POINT)
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
                        Text("Edit prayer point", style = typography.button)
                    }

                    OutlinedButton(
                        onClick = {
                            pointToDelete = point
                            pointForContextActions = null
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
                        Text("Delete", style = typography.button)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { pointForContextActions = null },
                    shape = FlatSquareShape
                ) {
                    Text("Cancel", style = typography.button, color = colors.textSubtle)
                }
            },
            shape = FlatSquareShape,
            containerColor = colors.surface,
            tonalElevation = 0.dp
        )
    }

    if (pointToDelete != null) {
        val point = pointToDelete!!
        AlertDialog(
            onDismissRequest = { pointToDelete = null },
            title = {
                Text(
                    text = "Delete prayer point?",
                    style = typography.prayerPointTitle,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Delete this prayer point? This cannot be undone.",
                    style = typography.caption,
                    color = colors.textSubtle
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = point.id
                        pointToDelete = null
                        localPoints = localPoints.filter { it.id != id }
                        onDeletePrayerPoint(id)
                    },
                    shape = FlatSquareShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.textPrimary,
                        contentColor = colors.background
                    )
                ) {
                    Text("Delete", style = typography.button)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { pointToDelete = null },
                    shape = FlatSquareShape,
                    border = BorderStroke(0.5.dp, colors.border),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.textSubtle
                    )
                ) {
                    Text("Cancel", style = typography.button)
                }
            },
            shape = FlatSquareShape,
            containerColor = colors.surface,
            tonalElevation = 0.dp
        )
    }
}
