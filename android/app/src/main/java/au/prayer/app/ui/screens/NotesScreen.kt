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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import au.prayer.app.data.local.PrayerRepository
import au.prayer.app.data.models.IndividualEntity
import au.prayer.app.data.models.PrayerPoint
import au.prayer.app.data.models.PrayerStatus
import au.prayer.app.data.models.RootCode
import au.prayer.app.network.PrayerApiClient
import au.prayer.app.network.PromptGroup
import au.prayer.app.network.RecordedPoint
import au.prayer.app.network.SuggestRequest
import au.prayer.app.ui.components.LinedNotepad
import au.prayer.app.ui.components.SilkMarkerRibbon
import au.prayer.app.ui.gestures.calculateZoomScale
import au.prayer.app.ui.gestures.edgeSwipeRight
import au.prayer.app.ui.gestures.pinchToZoom
import au.prayer.app.ui.navigation.LifoBackStack
import au.prayer.app.ui.theme.FlatSquareShape
import au.prayer.app.ui.theme.PrayerColors
import au.prayer.app.ui.theme.PrayerSpacing
import au.prayer.app.ui.theme.PrayerTypography
import au.prayer.app.ui.theme.withZoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

enum class NotesView {
    OVERVIEW,
    DATE_DETAIL,
    NOTE_EDITOR
}

private fun formatNoteDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH)
    return sdf.format(Date(timestamp))
}

private fun formatNoteTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.ENGLISH)
    return sdf.format(Date(timestamp))
}

private fun getTodayDateString(): String {
    val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH)
    return sdf.format(Date())
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(
    colors: PrayerColors,
    typography: PrayerTypography,
    repository: PrayerRepository,
    apiClient: PrayerApiClient,
    onBackToHome: () -> Unit,
    onShowMessage: (String) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val notesBackStack = remember { LifoBackStack(NotesView.OVERVIEW) }
    val currentView = notesBackStack.current

    var allDateEntities by remember { mutableStateOf(repository.getNotesEntities()) }
    var selectedDateEntity by remember { mutableStateOf<IndividualEntity?>(null) }
    var notesForDate by remember { mutableStateOf<List<PrayerPoint>>(emptyList()) }
    var dateContextTopic by remember { mutableStateOf("") }
    var isEditingTopic by remember { mutableStateOf(false) }

    // Note Editor state
    var selectedNote by remember { mutableStateOf<PrayerPoint?>(null) }
    var noteTitleState by remember { mutableStateOf("") }
    var noteBodyState by remember { mutableStateOf(TextFieldValue("")) }
    var lastSavedTitle by remember { mutableStateOf("") }
    var lastSavedBody by remember { mutableStateOf("") }
    var isNewNote by remember { mutableStateOf(false) }

    // Deletion states
    var dateToDelete by remember { mutableStateOf<IndividualEntity?>(null) }
    var showDeleteDateConfirm by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<PrayerPoint?>(null) }
    var showDeleteNoteConfirm by remember { mutableStateOf(false) }

    // AI Prompts state
    var isPromptsExpanded by remember { mutableStateOf(false) }
    var promptGroups by remember { mutableStateOf<List<PromptGroup>>(emptyList()) }
    var isLoadingPrompts by remember { mutableStateOf(false) }

    // Multi-touch pinch-to-zoom
    val initialZoom = remember { repository.getTextZoomScale() }
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

    val activeTypography = remember(typography, zoomScale) {
        typography.withZoom(zoomScale)
    }

    fun refreshDateEntities() {
        allDateEntities = repository.getNotesEntities()
    }

    fun refreshNotesForSelectedDate() {
        val entityId = selectedDateEntity?.id ?: return
        notesForDate = repository.getNotesForDateEntity(entityId)
    }

    fun openDateDetail(entity: IndividualEntity) {
        selectedDateEntity = entity
        dateContextTopic = entity.contextDescription
        isEditingTopic = false
        notesForDate = repository.getNotesForDateEntity(entity.id)
        notesBackStack.push(NotesView.DATE_DETAIL)
    }

    fun openOrCreateTodayDateDetail() {
        val todayStr = getTodayDateString()
        val entity = repository.getOrCreateTodayNoteEntity(todayStr)
        refreshDateEntities()
        openDateDetail(entity)
    }

    fun openNewNoteEditor(dateEntity: IndividualEntity) {
        selectedDateEntity = dateEntity
        selectedNote = null
        noteTitleState = ""
        noteBodyState = TextFieldValue("")
        lastSavedTitle = ""
        lastSavedBody = ""
        isNewNote = true
        isPromptsExpanded = false
        promptGroups = emptyList()
        notesBackStack.push(NotesView.NOTE_EDITOR)
    }

    fun openNoteEditor(note: PrayerPoint) {
        selectedNote = note
        noteTitleState = note.title
        noteBodyState = TextFieldValue(note.description, TextRange(note.description.length))
        lastSavedTitle = note.title
        lastSavedBody = note.description
        isNewNote = false
        isPromptsExpanded = false
        promptGroups = repository.getCachedSuggestions(selectedDateEntity?.id ?: "").orEmpty()
        notesBackStack.push(NotesView.NOTE_EDITOR)
    }

    fun loadPromptsForNote(entity: IndividualEntity, noteTitle: String, currentText: String) {
        val cached = repository.getCachedSuggestions(entity.id)
        if (!cached.isNullOrEmpty()) {
            promptGroups = cached
        }

        if (currentText.isNotBlank() || noteTitle.isNotBlank()) {
            isLoadingPrompts = true
            coroutineScope.launch(Dispatchers.IO) {
                val recordedLines = currentText.lines().map { it.trim() }.filter { it.isNotBlank() }
                val recorded = recordedLines.map { line ->
                    RecordedPoint(title = line.take(40), body = line, status = "active")
                }
                val effectiveTarget = if (noteTitle.isNotBlank()) "${entity.displayName} • $noteTitle" else entity.displayName
                val request = SuggestRequest(
                    targetName = effectiveTarget,
                    root = RootCode.NOTES.name,
                    group = null,
                    contextDescription = entity.contextDescription.takeIf { it.isNotBlank() },
                    recordedPoints = recorded,
                    journalUpdates = emptyList(),
                    currentDraft = null,
                    localeDialect = "EN_AU_UK"
                )
                val result = apiClient.getSuggestions(request)
                withContext(Dispatchers.Main) {
                    isLoadingPrompts = false
                    result.onSuccess { resp ->
                        if (resp.promptGroups.isNotEmpty()) {
                            repository.saveCachedSuggestions(entity.id, resp)
                            promptGroups = resp.promptGroups
                        }
                    }
                }
            }
        }
    }

    fun saveDateTopic() {
        val entity = selectedDateEntity ?: return
        if (entity.contextDescription != dateContextTopic) {
            repository.updateEntity(
                id = entity.id,
                displayName = entity.displayName,
                rootCode = RootCode.NOTES,
                contextDescription = dateContextTopic
            )
            selectedDateEntity = entity.copy(contextDescription = dateContextTopic)
            refreshDateEntities()
        }
    }

    fun saveCurrentNote(silent: Boolean = false) {
        val dateEntity = selectedDateEntity ?: return
        val title = noteTitleState.trim()
        val body = noteBodyState.text

        if (isNewNote) {
            if (title.isNotBlank() || body.isNotBlank()) {
                val created = repository.createNote(dateEntity.id, title, body)
                selectedNote = created
                isNewNote = false
                lastSavedTitle = title
                lastSavedBody = body
                refreshNotesForSelectedDate()
                refreshDateEntities()
                if (!silent) onShowMessage("Note saved")
            }
        } else {
            val note = selectedNote ?: return
            if (title != lastSavedTitle || body != lastSavedBody) {
                val updated = repository.updateNote(note.id, title, body)
                selectedNote = updated
                lastSavedTitle = title
                lastSavedBody = body
                refreshNotesForSelectedDate()
                refreshDateEntities()
                if (!silent) onShowMessage("Note saved")
            }
        }
    }

    fun handleNotesBack(): Boolean {
        when (currentView) {
            NotesView.NOTE_EDITOR -> {
                saveCurrentNote(silent = true)
                notesBackStack.pop()
                return true
            }
            NotesView.DATE_DETAIL -> {
                saveDateTopic()
                notesBackStack.pop()
                return true
            }
            NotesView.OVERVIEW -> {
                onBackToHome()
                return true
            }
        }
    }

    BackHandler(enabled = true) {
        handleNotesBack()
    }

    Scaffold(
        containerColor = colors.background,
        contentColor = colors.textPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notes",
                        style = activeTypography.prayerPointTitle,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { handleNotesBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                },
                actions = {
                    if (zoomScale != 1.0f) {
                        Text(
                            text = "${(zoomScale * 100).roundToInt()}% ↺",
                            style = activeTypography.marginStatus.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = colors.leatherActive,
                            modifier = Modifier
                                .clickable {
                                    zoomScale = 1.0f
                                    repository.saveTextZoomScale(1.0f)
                                }
                                .padding(horizontal = PrayerSpacing.small)
                        )
                    }
                    if (currentView == NotesView.NOTE_EDITOR) {
                        val hasUnsavedChanges = (noteTitleState.trim() != lastSavedTitle || noteBodyState.text != lastSavedBody) &&
                                (noteTitleState.isNotBlank() || noteBodyState.text.isNotBlank())
                        Text(
                            text = if (hasUnsavedChanges) "Save" else "Saved",
                            style = activeTypography.marginStatus.copy(
                                fontWeight = if (hasUnsavedChanges) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (hasUnsavedChanges) colors.leatherActive else colors.inkMuted,
                            modifier = Modifier
                                .clickable {
                                    saveCurrentNote(silent = false)
                                }
                                .padding(horizontal = PrayerSpacing.medium, vertical = PrayerSpacing.small)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.textPrimary
                )
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .edgeSwipeRight(
                onSwipeRight = { handleNotesBack() }
            )
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
                    repository.saveTextZoomScale(zoomScale)
                }
            )
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalDivider(thickness = 0.5.dp, color = colors.border)

            AnimatedContent(
                targetState = currentView,
                transitionSpec = {
                    val orderMap = mapOf(
                        NotesView.OVERVIEW to 0,
                        NotesView.DATE_DETAIL to 1,
                        NotesView.NOTE_EDITOR to 2
                    )
                    val fromOrder = orderMap[initialState] ?: 0
                    val toOrder = orderMap[targetState] ?: 0

                    if (toOrder > fromOrder) {
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
                label = "NotesSubViewTransition",
                modifier = Modifier.fillMaxSize()
            ) { view ->
                when (view) {
                    NotesView.OVERVIEW -> {
                        val todayStr = getTodayDateString()
                        val todayEntity = allDateEntities.find { it.displayName.equals(todayStr, ignoreCase = true) }
                        val todayNotesCount = todayEntity?.let { repository.getNoteCountForEntity(it.id) } ?: 0
                        val pastDateEntities = allDateEntities.filter { !it.displayName.equals(todayStr, ignoreCase = true) }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Header Banner
                            item(key = "notes_header_banner") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = PrayerSpacing.large,
                                            vertical = PrayerSpacing.medium
                                        ),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "DAILY REFLECTIONS & STUDY",
                                        style = activeTypography.frontispieceHeader.copy(letterSpacing = 1.5.sp),
                                        color = colors.inkMuted,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(PrayerSpacing.extraSmall))
                                    Text(
                                        text = "“Meditate on these things; give yourself entirely to them.”",
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 13.sp,
                                        color = colors.inkSecondary.copy(alpha = 0.80f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.borderSubtle)
                            }

                            // Prominent Today's Date Grouping Card
                            item(key = "action_today_date_group") {
                                Surface(
                                    onClick = { openOrCreateTodayDateDetail() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 68.dp),
                                    shape = FlatSquareShape,
                                    color = colors.surface,
                                    contentColor = colors.textPrimary,
                                    tonalElevation = PrayerSpacing.elevationCard,
                                    shadowElevation = PrayerSpacing.elevationCard,
                                    border = BorderStroke(1.dp, colors.leatherActive.copy(alpha = 0.50f))
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
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "TODAY",
                                                    style = activeTypography.marginStatus.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 1.sp
                                                    ),
                                                    color = colors.leatherActive
                                                )
                                                Spacer(modifier = Modifier.width(PrayerSpacing.small))
                                                Text(
                                                    text = if (todayNotesCount == 0) "• No notes filed yet" else "• $todayNotesCount ${if (todayNotesCount == 1) "note" else "notes"}",
                                                    style = activeTypography.caption,
                                                    color = colors.inkMuted
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = todayStr,
                                                style = activeTypography.topicTitle.copy(fontWeight = FontWeight.SemiBold),
                                                color = colors.textPrimary
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    val entity = todayEntity ?: repository.getOrCreateTodayNoteEntity(todayStr)
                                                    refreshDateEntities()
                                                    selectedDateEntity = entity
                                                    dateContextTopic = entity.contextDescription
                                                    notesForDate = repository.getNotesForDateEntity(entity.id)
                                                    notesBackStack.push(NotesView.DATE_DETAIL)
                                                    openNewNoteEditor(entity)
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "+ Note",
                                                    style = activeTypography.marginStatus.copy(fontWeight = FontWeight.SemiBold),
                                                    color = colors.leatherActive
                                                )
                                            }
                                            Text(
                                                text = "›",
                                                fontFamily = FontFamily.Serif,
                                                fontSize = 22.sp,
                                                color = colors.leatherActive.copy(alpha = 0.65f)
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
                            }

                            // Past Date Groupings Section Header
                            item(key = "header_past_dates") {
                                Surface(
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
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "PAST DATES (${pastDateEntities.size})",
                                            style = activeTypography.categoryLedgerHeader,
                                            color = colors.leatherActive
                                        )
                                    }
                                }
                                HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
                            }

                            if (pastDateEntities.isEmpty()) {
                                item(key = "empty_past_dates") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = PrayerSpacing.extraLarge),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No previous date groupings yet.\nOpen Today to begin filing notes.",
                                            style = activeTypography.caption,
                                            color = colors.inkMuted,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                items(pastDateEntities, key = { it.id }) { entity ->
                                    val count = remember(entity.id) { repository.getNoteCountForEntity(entity.id) }
                                    val previewNotes = remember(entity.id) { repository.getNotesForDateEntity(entity.id) }
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .animateItem()
                                            .combinedClickable(
                                                onClick = { openDateDetail(entity) },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    dateToDelete = entity
                                                    showDeleteDateConfirm = true
                                                }
                                            ),
                                        shape = FlatSquareShape,
                                        color = colors.surface,
                                        contentColor = colors.textPrimary,
                                        tonalElevation = PrayerSpacing.elevationNone
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
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                                                ) {
                                                    if (entity.isPinned) {
                                                        Text(
                                                            text = "❧",
                                                            fontFamily = FontFamily.Serif,
                                                            fontSize = 13.sp,
                                                            color = colors.ribbonPrimary
                                                        )
                                                    }
                                                    Text(
                                                        text = entity.displayName,
                                                        style = activeTypography.prayerPointTitle,
                                                        color = colors.textPrimary
                                                    )
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                                                ) {
                                                    Text(
                                                        text = "$count ${if (count == 1) "note" else "notes"}",
                                                        style = activeTypography.caption.copy(fontWeight = FontWeight.Medium),
                                                        color = colors.leatherActive
                                                    )
                                                    if (entity.contextDescription.isNotBlank()) {
                                                        Text(
                                                            text = "• ${entity.contextDescription}",
                                                            style = activeTypography.caption,
                                                            color = colors.inkMuted,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }

                                                if (previewNotes.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(PrayerSpacing.extraSmall))
                                                    val previewText = previewNotes.mapNotNull {
                                                        if (it.title.isNotBlank()) it.title else it.description.lines().firstOrNull { l -> l.isNotBlank() }
                                                    }.take(2).joinToString(" • ")
                                                    if (previewText.isNotBlank()) {
                                                        Text(
                                                            text = previewText,
                                                            style = activeTypography.prayerPointBody.copy(
                                                                fontSize = 13.sp,
                                                                lineHeight = 18.sp
                                                            ),
                                                            color = colors.textSubtle,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        dateToDelete = entity
                                                        showDeleteDateConfirm = true
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete Date Grouping",
                                                        tint = colors.textSubtle.copy(alpha = 0.5f),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Text(
                                                    text = "›",
                                                    fontFamily = FontFamily.Serif,
                                                    fontSize = 18.sp,
                                                    color = colors.textSubtle.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                    }
                                    HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
                                }
                            }
                        }
                    }

                    NotesView.DATE_DETAIL -> {
                        selectedDateEntity?.let { dateEntity ->
                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    // Folio Canvas Header: Full Date & Optional Passage/Topic
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                start = PrayerSpacing.textInset,
                                                end = PrayerSpacing.large + 36.dp, // Clearance for silk ribbon
                                                top = PrayerSpacing.medium,
                                                bottom = PrayerSpacing.extraSmall
                                            )
                                    ) {
                                        Text(
                                            text = dateEntity.displayName,
                                            style = activeTypography.subjectHeader,
                                            color = colors.textPrimary
                                        )

                                        Spacer(modifier = Modifier.height(PrayerSpacing.extraSmall))

                                        // Optional study topic / passage field
                                        if (isEditingTopic) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                BasicTextField(
                                                    value = dateContextTopic,
                                                    onValueChange = { dateContextTopic = it },
                                                    textStyle = activeTypography.caption.copy(
                                                        fontSize = 14.sp,
                                                        color = colors.textPrimary
                                                    ),
                                                    modifier = Modifier.weight(1f),
                                                    decorationBox = { inner ->
                                                        if (dateContextTopic.isEmpty()) {
                                                            Text(
                                                                text = "e.g. Study • Romans 8:28–39",
                                                                style = activeTypography.caption,
                                                                color = colors.textSubtle
                                                            )
                                                        }
                                                        inner()
                                                    }
                                                )
                                                Text(
                                                    text = "Done",
                                                    style = activeTypography.marginStatus,
                                                    color = colors.leatherActive,
                                                    modifier = Modifier
                                                        .clickable {
                                                            isEditingTopic = false
                                                            saveDateTopic()
                                                        }
                                                        .padding(horizontal = 8.dp)
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = if (dateContextTopic.isNotBlank()) dateContextTopic else "+ Add study topic or passage",
                                                style = activeTypography.caption.copy(
                                                    fontWeight = if (dateContextTopic.isNotBlank()) FontWeight.Medium else FontWeight.Normal
                                                ),
                                                color = if (dateContextTopic.isNotBlank()) colors.leatherActive else colors.inkMuted,
                                                modifier = Modifier.clickable { isEditingTopic = true }
                                            )
                                        }
                                    }

                                    HorizontalDivider(
                                        thickness = PrayerSpacing.hairlineWidth,
                                        color = colors.paperFeintRule,
                                        modifier = Modifier.padding(bottom = PrayerSpacing.extraSmall)
                                    )

                                    // Action Card: + Add Note under this date
                                    Surface(
                                        onClick = { openNewNoteEditor(dateEntity) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                horizontal = PrayerSpacing.large,
                                                vertical = PrayerSpacing.small
                                            ),
                                        shape = FlatSquareShape,
                                        color = colors.surface,
                                        contentColor = colors.textPrimary,
                                        tonalElevation = PrayerSpacing.elevationCard,
                                        border = BorderStroke(1.dp, colors.leatherActive.copy(alpha = 0.40f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    horizontal = PrayerSpacing.medium,
                                                    vertical = PrayerSpacing.medium
                                                ),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "✎",
                                                    fontSize = 16.sp,
                                                    color = colors.leatherActive
                                                )
                                                Spacer(modifier = Modifier.width(PrayerSpacing.small))
                                                Text(
                                                    text = "+ Add Note",
                                                    style = activeTypography.topicTitle.copy(fontWeight = FontWeight.SemiBold),
                                                    color = colors.leatherActive
                                                )
                                            }
                                            Text(
                                                text = "›",
                                                fontFamily = FontFamily.Serif,
                                                fontSize = 20.sp,
                                                color = colors.leatherActive.copy(alpha = 0.65f)
                                            )
                                        }
                                    }

                                    // Filed Notes Section Header
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                horizontal = PrayerSpacing.large,
                                                vertical = PrayerSpacing.small
                                            ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "FILED NOTES (${notesForDate.size})",
                                            style = activeTypography.categoryLedgerHeader,
                                            color = colors.leatherActive
                                        )
                                    }

                                    HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)

                                    if (notesForDate.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                                .padding(PrayerSpacing.large),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No notes filed under this date yet.\nTap '+ Add Note' above to write one.",
                                                style = activeTypography.caption,
                                                color = colors.inkMuted,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                        ) {
                                            items(notesForDate, key = { it.id }) { note ->
                                                Surface(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .animateItem()
                                                        .combinedClickable(
                                                            onClick = { openNoteEditor(note) },
                                                            onLongClick = {
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                noteToDelete = note
                                                                showDeleteNoteConfirm = true
                                                            }
                                                        ),
                                                    shape = FlatSquareShape,
                                                    color = colors.surface,
                                                    contentColor = colors.textPrimary,
                                                    tonalElevation = PrayerSpacing.elevationNone
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
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                                                            ) {
                                                                if (note.title.isNotBlank()) {
                                                                    Text(
                                                                        text = note.title,
                                                                        style = activeTypography.prayerPointTitle,
                                                                        color = colors.textPrimary
                                                                    )
                                                                } else {
                                                                    Text(
                                                                        text = "(Untitled Note)",
                                                                        style = activeTypography.prayerPointTitle.copy(fontStyle = FontStyle.Italic),
                                                                        color = colors.inkMuted
                                                                    )
                                                                }
                                                                Text(
                                                                    text = "• ${formatNoteTime(note.createdAt)}",
                                                                    style = activeTypography.caption,
                                                                    color = colors.inkMuted
                                                                )
                                                            }

                                                            if (note.description.isNotBlank()) {
                                                                Spacer(modifier = Modifier.height(PrayerSpacing.extraSmall))
                                                                Text(
                                                                    text = note.description,
                                                                    style = activeTypography.prayerPointBody.copy(
                                                                        fontSize = 14.sp,
                                                                        lineHeight = 20.sp
                                                                    ),
                                                                    color = colors.textSubtle,
                                                                    maxLines = 2,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                        }

                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                                                            ) {
                                                            IconButton(
                                                                onClick = {
                                                                    noteToDelete = note
                                                                    showDeleteNoteConfirm = true
                                                                }
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Delete,
                                                                    contentDescription = "Delete Note",
                                                                    tint = colors.textSubtle.copy(alpha = 0.5f),
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                            }
                                                            Text(
                                                                text = "›",
                                                                fontFamily = FontFamily.Serif,
                                                                fontSize = 18.sp,
                                                                color = colors.textSubtle.copy(alpha = 0.6f)
                                                            )
                                                        }
                                                    }
                                                }
                                                HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
                                            }
                                        }
                                    }
                                }

                                // Interactive Silk Marker Ribbon Tab anchored at top right
                                SilkMarkerRibbon(
                                    isPinned = dateEntity.isPinned,
                                    onTogglePin = {
                                        val newPinned = !dateEntity.isPinned
                                        selectedDateEntity = dateEntity.copy(isPinned = newPinned)
                                        repository.toggleEntityPinned(dateEntity.id, newPinned)
                                        refreshDateEntities()
                                        onShowMessage(if (newPinned) "Pinned date grouping" else "Unpinned date grouping")
                                    },
                                    color = colors.ribbonPrimary,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(end = 20.dp)
                                )
                            }
                        }
                    }

                    NotesView.NOTE_EDITOR -> {
                        selectedDateEntity?.let { dateEntity ->
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Folio Note Header: Date Indicator & Optional Title Field
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = PrayerSpacing.textInset,
                                            vertical = PrayerSpacing.small
                                        )
                                ) {
                                    Text(
                                        text = dateEntity.displayName,
                                        style = activeTypography.caption.copy(
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = colors.leatherActive
                                    )

                                    Spacer(modifier = Modifier.height(PrayerSpacing.extraSmall))

                                    // Individual Note Title Field (Optional)
                                    BasicTextField(
                                        value = noteTitleState,
                                        onValueChange = { noteTitleState = it },
                                        textStyle = activeTypography.subjectHeader.copy(
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textPrimary
                                        ),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        decorationBox = { innerTextField ->
                                            if (noteTitleState.isEmpty()) {
                                                Text(
                                                    text = "Title (Optional)",
                                                    style = activeTypography.subjectHeader.copy(
                                                        fontSize = 20.sp,
                                                        color = colors.textSubtle.copy(alpha = 0.45f)
                                                    )
                                                )
                                            }
                                            innerTextField()
                                        }
                                    )
                                }

                                HorizontalDivider(
                                    thickness = PrayerSpacing.hairlineWidth,
                                    color = colors.paperFeintRule,
                                    modifier = Modifier.padding(bottom = PrayerSpacing.extraSmall)
                                )

                                // Lined Notepad Canvas (Writing pad fills space)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    LinedNotepad(
                                        text = noteBodyState,
                                        onTextChange = { newVal ->
                                            noteBodyState = newVal
                                        },
                                        colors = colors,
                                        typography = activeTypography,
                                        placeholder = "Write note reflections, meditation on Scripture, or study thoughts..."
                                    )
                                }

                                // Grounded AI Prompts for Prayer Card (Collapsed by default)
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = PrayerSpacing.large,
                                            vertical = PrayerSpacing.small
                                        )
                                        .clickable {
                                            isPromptsExpanded = !isPromptsExpanded
                                            if (isPromptsExpanded && promptGroups.isEmpty() && !isLoadingPrompts) {
                                                loadPromptsForNote(dateEntity, noteTitleState, noteBodyState.text)
                                            }
                                        },
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
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "❧",
                                                    fontFamily = FontFamily.Serif,
                                                    fontSize = 14.sp,
                                                    color = colors.leatherActive
                                                )
                                                Spacer(modifier = Modifier.width(PrayerSpacing.small))
                                                Text(
                                                    text = "Prompts for Prayer",
                                                    style = activeTypography.caption.copy(fontWeight = FontWeight.SemiBold),
                                                    color = colors.leatherActive
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (isLoadingPrompts) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(12.dp),
                                                        strokeWidth = 1.dp,
                                                        color = colors.textSubtle
                                                    )
                                                    Spacer(modifier = Modifier.width(PrayerSpacing.small))
                                                }
                                                Text(
                                                    text = if (isPromptsExpanded) "Hide" else "Show",
                                                    style = activeTypography.marginStatus,
                                                    color = colors.inkMuted
                                                )
                                            }
                                        }

                                        if (isPromptsExpanded) {
                                            Spacer(modifier = Modifier.height(PrayerSpacing.extraSmall))
                                            HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
                                            Spacer(modifier = Modifier.height(PrayerSpacing.small))

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(max = 220.dp)
                                                    .verticalScroll(rememberScrollState())
                                            ) {
                                                if (isLoadingPrompts && promptGroups.isEmpty()) {
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
                                                } else if (promptGroups.isEmpty()) {
                                                    Text(
                                                        text = if (noteBodyState.text.isBlank() && noteTitleState.isBlank()) {
                                                            "Write your reflection or study notes above to generate prayer prompts."
                                                        } else {
                                                            "Tap to generate prompts grounded in this note."
                                                        },
                                                        style = activeTypography.caption,
                                                        color = colors.inkMuted,
                                                        modifier = Modifier
                                                            .padding(vertical = PrayerSpacing.small)
                                                            .clickable {
                                                                loadPromptsForNote(dateEntity, noteTitleState, noteBodyState.text)
                                                            }
                                                    )
                                                } else {
                                                    promptGroups.forEach { group ->
                                                        Text(
                                                            text = group.title,
                                                            style = activeTypography.categoryLedgerHeader,
                                                            color = colors.leatherPrimary,
                                                            modifier = Modifier.padding(top = PrayerSpacing.small, bottom = PrayerSpacing.extraSmall)
                                                        )
                                                        group.prompts.forEach { prompt ->
                                                            Text(
                                                                text = "• $prompt",
                                                                style = activeTypography.prayerPointBody,
                                                                color = colors.textPrimary,
                                                                modifier = Modifier.padding(vertical = PrayerSpacing.extraSmall)
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
                    }
                }
            }
        }
    }

    // Floating Zoom Indicator Pill
    if (showZoomPill) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                onClick = {
                    zoomScale = 1.0f
                    repository.saveTextZoomScale(1.0f)
                    showZoomPill = false
                },
                shape = FlatSquareShape,
                color = colors.textPrimary,
                contentColor = colors.background,
                tonalElevation = PrayerSpacing.elevationFloating,
                shadowElevation = PrayerSpacing.elevationFloating
            ) {
                Text(
                    text = "${(zoomScale * 100).roundToInt()}% • Reset",
                    style = activeTypography.marginStatus.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.background,
                    modifier = Modifier.padding(horizontal = PrayerSpacing.medium, vertical = PrayerSpacing.small)
                )
            }
        }
    }

    // Delete Date Grouping Confirmation Dialog
    if (showDeleteDateConfirm && dateToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDateConfirm = false
                dateToDelete = null
            },
            shape = FlatSquareShape,
            containerColor = colors.surfaceElevated,
            title = {
                Text(
                    text = "Delete Date Grouping",
                    style = activeTypography.topicTitle,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete \"${dateToDelete?.displayName}\" and all of its filed notes?",
                    style = activeTypography.prayerPointBody,
                    color = colors.textSubtle
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toDelete = dateToDelete
                        if (toDelete != null) {
                            repository.deleteEntity(toDelete.id)
                            refreshDateEntities()
                            onShowMessage("Date grouping deleted")
                            if (currentView == NotesView.DATE_DETAIL && selectedDateEntity?.id == toDelete.id) {
                                notesBackStack.pop()
                            }
                        }
                        showDeleteDateConfirm = false
                        dateToDelete = null
                    }
                ) {
                    Text("Delete", color = colors.stateAlert)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDateConfirm = false
                        dateToDelete = null
                    }
                ) {
                    Text("Cancel", color = colors.textPrimary)
                }
            }
        )
    }

    // Delete Individual Note Confirmation Dialog
    if (showDeleteNoteConfirm && noteToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteNoteConfirm = false
                noteToDelete = null
            },
            shape = FlatSquareShape,
            containerColor = colors.surfaceElevated,
            title = {
                Text(
                    text = "Delete Note",
                    style = activeTypography.topicTitle,
                    color = colors.textPrimary
                )
            },
            text = {
                val titlePreview = if (!noteToDelete?.title.isNullOrBlank()) "\"${noteToDelete?.title}\"" else "this untitled note"
                Text(
                    text = "Are you sure you want to permanently delete $titlePreview?",
                    style = activeTypography.prayerPointBody,
                    color = colors.textSubtle
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toDelete = noteToDelete
                        if (toDelete != null) {
                            repository.deleteNote(toDelete.id)
                            refreshNotesForSelectedDate()
                            refreshDateEntities()
                            onShowMessage("Note deleted")
                            if (currentView == NotesView.NOTE_EDITOR && selectedNote?.id == toDelete.id) {
                                notesBackStack.pop()
                            }
                        }
                        showDeleteNoteConfirm = false
                        noteToDelete = null
                    }
                ) {
                    Text("Delete", color = colors.stateAlert)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteNoteConfirm = false
                        noteToDelete = null
                    }
                ) {
                    Text("Cancel", color = colors.textPrimary)
                }
            }
        )
    }
}
