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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import au.prayer.app.data.local.PrayerRepository
import au.prayer.app.data.models.IndividualEntity
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
    DETAIL
}

private fun formatNoteDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH)
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
    val keyboardController = LocalSoftwareKeyboardController.current

    val notesBackStack = remember { LifoBackStack(NotesView.OVERVIEW) }
    val currentView = notesBackStack.current

    var allNotes by remember { mutableStateOf(repository.getNotesEntities()) }
    var selectedNoteEntity by remember { mutableStateOf<IndividualEntity?>(null) }
    var noteTextState by remember { mutableStateOf(TextFieldValue("")) }
    var noteContextTopic by remember { mutableStateOf("") }
    var lastSavedText by remember { mutableStateOf("") }
    var isEditingTopic by remember { mutableStateOf(false) }

    var noteToDelete by remember { mutableStateOf<IndividualEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

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

    fun refreshNotes() {
        allNotes = repository.getNotesEntities()
    }

    fun loadPromptsForEntity(entity: IndividualEntity, currentText: String) {
        val cached = repository.getCachedSuggestions(entity.id)
        if (!cached.isNullOrEmpty()) {
            promptGroups = cached
        }

        if (currentText.isNotBlank()) {
            isLoadingPrompts = true
            coroutineScope.launch(Dispatchers.IO) {
                val recordedLines = currentText.lines().map { it.trim() }.filter { it.isNotBlank() }
                val recorded = recordedLines.map { line ->
                    RecordedPoint(title = line.take(40), body = line, status = "active")
                }
                val request = SuggestRequest(
                    targetName = entity.displayName,
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

    fun openNote(entity: IndividualEntity) {
        selectedNoteEntity = entity
        val text = repository.getNoteText(entity.id)
        noteTextState = TextFieldValue(text, TextRange(text.length))
        lastSavedText = text
        noteContextTopic = entity.contextDescription
        isEditingTopic = false
        isPromptsExpanded = false

        // Load existing cache
        promptGroups = repository.getCachedSuggestions(entity.id).orEmpty()

        notesBackStack.push(NotesView.DETAIL)
    }

    fun openOrCreateTodayNote() {
        val todayStr = getTodayDateString()
        val entity = repository.getOrCreateTodayNoteEntity(todayStr)
        refreshNotes()
        openNote(entity)
    }

    fun saveCurrentNote(silent: Boolean = false) {
        val entity = selectedNoteEntity ?: return
        val currentText = noteTextState.text
        repository.saveNoteText(entity.id, currentText)
        if (entity.contextDescription != noteContextTopic) {
            repository.updateEntity(
                id = entity.id,
                displayName = entity.displayName,
                rootCode = RootCode.NOTES,
                contextDescription = noteContextTopic
            )
            selectedNoteEntity = entity.copy(contextDescription = noteContextTopic)
        }
        lastSavedText = currentText
        refreshNotes()
        if (!silent) {
            onShowMessage("Note saved")
        }
    }

    fun handleNotesBack(): Boolean {
        if (currentView == NotesView.DETAIL) {
            // Save on exit
            if (noteTextState.text != lastSavedText || selectedNoteEntity?.contextDescription != noteContextTopic) {
                saveCurrentNote(silent = true)
            }
            notesBackStack.pop()
            return true
        } else {
            onBackToHome()
            return true
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
                        text = when (currentView) {
                            NotesView.OVERVIEW -> "Notes"
                            NotesView.DETAIL -> selectedNoteEntity?.displayName ?: "Notes"
                        },
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
                    if (currentView == NotesView.DETAIL) {
                        val hasUnsavedChanges = noteTextState.text != lastSavedText ||
                                (selectedNoteEntity?.contextDescription != noteContextTopic)
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
                    if (targetState == NotesView.DETAIL) {
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
                        val todayNote = allNotes.find { it.displayName.equals(todayStr, ignoreCase = true) }
                        val pastNotes = allNotes.filter { !it.displayName.equals(todayStr, ignoreCase = true) }

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

                            // Prominent Today's Note Action Card
                            item(key = "action_today_note") {
                                Surface(
                                    onClick = { openOrCreateTodayNote() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 64.dp),
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
                                                    text = "✎",
                                                    fontSize = 16.sp,
                                                    color = colors.leatherActive
                                                )
                                                Spacer(modifier = Modifier.width(PrayerSpacing.small))
                                                Text(
                                                    text = if (todayNote != null) "Today's Note" else "+ Today's Note",
                                                    style = activeTypography.topicTitle.copy(fontWeight = FontWeight.SemiBold),
                                                    color = colors.textPrimary
                                                )
                                            }
                                            Text(
                                                text = todayStr,
                                                style = activeTypography.caption,
                                                color = colors.inkMuted
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
                                HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
                            }

                            // Past Reflections & Notes Section Header
                            item(key = "header_past_notes") {
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
                                            text = "PAST REFLECTIONS (${pastNotes.size})",
                                            style = activeTypography.categoryLedgerHeader,
                                            color = colors.leatherActive
                                        )
                                    }
                                }
                                HorizontalDivider(thickness = PrayerSpacing.hairlineWidth, color = colors.paperFeintRule)
                            }

                            if (pastNotes.isEmpty()) {
                                item(key = "empty_past_notes") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = PrayerSpacing.extraLarge),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No previous study notes yet.\nOpen Today's Note to begin.",
                                            style = activeTypography.caption,
                                            color = colors.inkMuted,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                items(pastNotes, key = { it.id }) { entity ->
                                    val noteText = remember(entity.id) { repository.getNoteText(entity.id) }
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .animateItem()
                                            .combinedClickable(
                                                onClick = { openNote(entity) },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    noteToDelete = entity
                                                    showDeleteConfirm = true
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

                                                if (entity.contextDescription.isNotBlank()) {
                                                    Text(
                                                        text = entity.contextDescription,
                                                        style = activeTypography.caption.copy(fontWeight = FontWeight.Medium),
                                                        color = colors.leatherActive
                                                    )
                                                }

                                                if (noteText.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(PrayerSpacing.extraSmall))
                                                    Text(
                                                        text = noteText,
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
                                                        noteToDelete = entity
                                                        showDeleteConfirm = true
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

                    NotesView.DETAIL -> {
                        selectedNoteEntity?.let { entity ->
                            val scrollState = rememberScrollState()

                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(scrollState)
                                ) {
                                    // Header: Day and Date + Optional Study Topic
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                start = PrayerSpacing.textInset,
                                                end = PrayerSpacing.large + 36.dp, // Clearance for silk ribbon
                                                top = PrayerSpacing.large,
                                                bottom = PrayerSpacing.small
                                            )
                                    ) {
                                        Text(
                                            text = entity.displayName,
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
                                                    value = noteContextTopic,
                                                    onValueChange = { noteContextTopic = it },
                                                    textStyle = activeTypography.caption.copy(
                                                        fontSize = 14.sp,
                                                        color = colors.textPrimary
                                                    ),
                                                    modifier = Modifier.weight(1f),
                                                    decorationBox = { inner ->
                                                        if (noteContextTopic.isEmpty()) {
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
                                                            saveCurrentNote(silent = true)
                                                        }
                                                        .padding(horizontal = 8.dp)
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = if (noteContextTopic.isNotBlank()) noteContextTopic else "+ Add study topic or passage",
                                                style = activeTypography.caption.copy(
                                                    fontWeight = if (noteContextTopic.isNotBlank()) FontWeight.Medium else FontWeight.Normal
                                                ),
                                                color = if (noteContextTopic.isNotBlank()) colors.leatherActive else colors.inkMuted,
                                                modifier = Modifier.clickable { isEditingTopic = true }
                                            )
                                        }
                                    }

                                    HorizontalDivider(
                                        thickness = PrayerSpacing.hairlineWidth,
                                        color = colors.paperFeintRule,
                                        modifier = Modifier.padding(vertical = PrayerSpacing.small)
                                    )

                                    // Lined Notepad Canvas (Standard Notepad Writing)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = 360.dp)
                                    ) {
                                        LinedNotepad(
                                            text = noteTextState,
                                            onTextChange = { newVal ->
                                                noteTextState = newVal
                                            },
                                            colors = colors,
                                            typography = activeTypography,
                                            placeholder = "Write today's reflections, meditation on Scripture, or study notes..."
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(PrayerSpacing.large))

                                    // Prompts for Prayer Card (Collapsed by default)
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                horizontal = PrayerSpacing.large,
                                                vertical = PrayerSpacing.medium
                                            )
                                            .clickable {
                                                isPromptsExpanded = !isPromptsExpanded
                                                if (isPromptsExpanded && promptGroups.isEmpty() && !isLoadingPrompts) {
                                                    loadPromptsForEntity(entity, noteTextState.text)
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
                                                        text = if (noteTextState.text.isBlank()) {
                                                            "Write your reflection or study notes above to generate prayer prompts."
                                                        } else {
                                                            "Tap to generate prompts grounded in your study notes."
                                                        },
                                                        style = activeTypography.caption,
                                                        color = colors.inkMuted,
                                                        modifier = Modifier
                                                            .padding(vertical = PrayerSpacing.small)
                                                            .clickable {
                                                                loadPromptsForEntity(entity, noteTextState.text)
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

                                    Spacer(modifier = Modifier.height(PrayerSpacing.extraLarge))
                                }

                                // Interactive Silk Marker Ribbon Tab anchored at top margin
                                SilkMarkerRibbon(
                                    isPinned = entity.isPinned,
                                    onTogglePin = {
                                        val newPinned = !entity.isPinned
                                        selectedNoteEntity = entity.copy(isPinned = newPinned)
                                        repository.toggleEntityPinned(entity.id, newPinned)
                                        refreshNotes()
                                        onShowMessage(if (newPinned) "Pinned note" else "Unpinned note")
                                    },
                                    color = colors.ribbonPrimary,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(end = 20.dp)
                                )
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

    // Delete Note Confirmation Dialog
    if (showDeleteConfirm && noteToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm = false
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
                Text(
                    text = "Are you sure you want to permanently delete the note for \"${noteToDelete?.displayName}\"?",
                    style = activeTypography.prayerPointBody,
                    color = colors.textSubtle
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toDelete = noteToDelete
                        if (toDelete != null) {
                            repository.deleteEntity(toDelete.id)
                            refreshNotes()
                            onShowMessage("Note deleted")
                            if (currentView == NotesView.DETAIL && selectedNoteEntity?.id == toDelete.id) {
                                notesBackStack.pop()
                            }
                        }
                        showDeleteConfirm = false
                        noteToDelete = null
                    }
                ) {
                    Text("Delete", color = colors.stateAlert)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        noteToDelete = null
                    }
                ) {
                    Text("Cancel", color = colors.textPrimary)
                }
            }
        )
    }
}
