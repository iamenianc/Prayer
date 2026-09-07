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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import au.prayer.app.data.models.*
import au.prayer.app.ui.gestures.edgeSwipeRight
import au.prayer.app.ui.navigation.LifoBackStack
import androidx.compose.ui.text.input.TextFieldValue
import au.prayer.app.ui.theme.FlatSquareShape
import au.prayer.app.ui.theme.PrayerColors
import au.prayer.app.ui.theme.PrayerSpacing
import au.prayer.app.ui.theme.PrayerTypography

private enum class JournalView {
    ROOT_SELECTION,
    ENTITY_LIST,
    ENTITY_DETAIL,
    EDIT_PRAYER_POINT,
    SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun JournalScreen(
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
    onAddForEntity: (IndividualEntity) -> Unit = {},
    onLogForEntity: (IndividualEntity) -> Unit = onAddForEntity,
    onBackToHome: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val journalBackStack = remember { LifoBackStack(JournalView.ROOT_SELECTION) }
    val currentView = journalBackStack.current
    var selectedRoot by remember { mutableStateOf(RootCode.PEOPLE) }
    var selectedEntity by remember { mutableStateOf<IndividualEntity?>(null) }
    var editingPoint by remember { mutableStateOf<PrayerPoint?>(null) }

    // Long-press context action states for entities (people/groups)
    var entityForContextActions by remember { mutableStateOf<IndividualEntity?>(null) }
    var entityToEdit by remember { mutableStateOf<IndividualEntity?>(null) }
    var editEntityName by remember { mutableStateOf("") }
    var editEntityRoot by remember { mutableStateOf(RootCode.PEOPLE) }
    var entityToDelete by remember { mutableStateOf<IndividualEntity?>(null) }

    // Long-press context action states for individual prayer records
    var pointForContextActions by remember { mutableStateOf<PrayerPoint?>(null) }
    var pointToDelete by remember { mutableStateOf<PrayerPoint?>(null) }

    fun handleJournalBack(): Boolean {
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
    var editTitle by remember { mutableStateOf("") }
    var editBody by remember { mutableStateOf(TextFieldValue("")) }
    var editStatus by remember { mutableStateOf(PrayerStatus.ACTIVE) }
    var editTestimony by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = colors.background,
        contentColor = colors.textPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentView) {
                            JournalView.ROOT_SELECTION -> "Journal"
                            JournalView.ENTITY_LIST -> selectedRoot.displayTitle
                            JournalView.ENTITY_DETAIL -> selectedEntity?.displayName ?: "Prayer points"
                            JournalView.EDIT_PRAYER_POINT -> "Edit Prayer Point"
                            JournalView.SETTINGS -> "Settings"
                        },
                        style = typography.prayerPointTitle,
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    if (currentView != JournalView.ROOT_SELECTION) {
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
                    if (currentView == JournalView.ROOT_SELECTION) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .edgeSwipeRight { handleJournalBack() }
        ) {
            HorizontalDivider(thickness = 0.5.dp, color = colors.border)

            // Sub-view Router with directional slide and fade animation
            AnimatedContent(
                targetState = currentView,
                transitionSpec = {
                    val isGoingDeeper = when {
                        initialState == JournalView.ROOT_SELECTION -> true
                        initialState == JournalView.ENTITY_LIST && targetState == JournalView.ENTITY_DETAIL -> true
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
                    JournalView.ROOT_SELECTION -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // People Slab
                            Surface(
                                onClick = {
                                    selectedRoot = RootCode.PEOPLE
                                    journalBackStack.push(JournalView.ENTITY_LIST)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                shape = FlatSquareShape,
                                color = colors.surface,
                                contentColor = colors.textPrimary,
                                tonalElevation = 0.dp
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("People", style = typography.homeAction, color = colors.textPrimary)
                                }
                            }

                            HorizontalDivider(thickness = 0.5.dp, color = colors.border)

                            // Groups Slab
                            Surface(
                                onClick = {
                                    selectedRoot = RootCode.GROUPS
                                    journalBackStack.push(JournalView.ENTITY_LIST)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                shape = FlatSquareShape,
                                color = colors.surface,
                                contentColor = colors.textPrimary,
                                tonalElevation = 0.dp
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Groups", style = typography.homeAction, color = colors.textPrimary)
                                }
                            }

                            HorizontalDivider(thickness = 0.5.dp, color = colors.border)

                            // General Slab
                            Surface(
                                onClick = {
                                    selectedRoot = RootCode.GENERAL
                                    journalBackStack.push(JournalView.ENTITY_LIST)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                shape = FlatSquareShape,
                                color = colors.surface,
                                contentColor = colors.textPrimary,
                                tonalElevation = 0.dp
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("General", style = typography.homeAction, color = colors.textPrimary)
                                }
                            }
                        }
                    }

                    JournalView.ENTITY_LIST -> {
                        val filteredEntities = entities.filter { it.rootCode == selectedRoot }
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            if (filteredEntities.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(PrayerSpacing.extraLarge),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No records yet", style = typography.caption, color = colors.textSubtle)
                                    }
                                }
                            }
                            items(filteredEntities, key = { it.id }) { entity ->
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
                                    tonalElevation = 0.dp
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    horizontal = PrayerSpacing.large,
                                                    vertical = PrayerSpacing.medium
                                                )
                                        ) {
                                            Text(
                                                text = entity.displayName,
                                                style = typography.prayerPointTitle,
                                                color = colors.textPrimary
                                            )
                                        }
                                        HorizontalDivider(thickness = 0.5.dp, color = colors.border)
                                    }
                                }
                            }
                        }
                    }

                    JournalView.ENTITY_DETAIL -> {
                        selectedEntity?.let { entity ->
                            val points = getPointsForEntity(entity.id)
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Quick Action: Add prayer point for this person/group
                                OutlinedButton(
                                    onClick = { onAddForEntity(entity) },
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
                                    Text("+ Add prayer point", style = typography.button, color = colors.textPrimary)
                                }

                                HorizontalDivider(thickness = 0.5.dp, color = colors.border)

                                LazyColumn(modifier = Modifier.weight(1f)) {
                                    items(points, key = { it.id }) { point ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .animateItem()
                                                .combinedClickable(
                                                    onClick = {
                                                        // Click Once to Edit Prayer Point
                                                        editingPoint = point
                                                        editTitle = point.title
                                                        editBody = point.description
                                                        editStatus = point.status
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
                                            tonalElevation = 0.dp
                                        ) {
                                            Column {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(
                                                            horizontal = PrayerSpacing.large,
                                                            vertical = PrayerSpacing.medium
                                                        )
                                                ) {
                                                    val isAnswered = point.status == PrayerStatus.ANSWERED
                                                    Text(
                                                        text = point.title,
                                                        style = if (isAnswered) {
                                                            typography.prayerPointTitle.copy(textDecoration = TextDecoration.LineThrough)
                                                        } else {
                                                            typography.prayerPointTitle
                                                        },
                                                        color = if (isAnswered) colors.answeredText else colors.textPrimary
                                                    )
                                                    Spacer(modifier = Modifier.height(PrayerSpacing.extraSmall))
                                                    Text(
                                                        text = point.description,
                                                        style = if (isAnswered) {
                                                            typography.prayerPointBody.copy(textDecoration = TextDecoration.LineThrough)
                                                        } else {
                                                            typography.prayerPointBody
                                                        },
                                                        color = if (isAnswered) colors.answeredText else colors.textPrimary
                                                    )
                                                    if (isAnswered && !point.answeredTestimony.isNullOrBlank()) {
                                                        Spacer(modifier = Modifier.height(PrayerSpacing.small))
                                                        Text(
                                                            text = "Thanksgiving: ${point.answeredTestimony}",
                                                            style = typography.caption,
                                                            color = colors.textSubtle
                                                        )
                                                    }
                                                }
                                                HorizontalDivider(thickness = 0.5.dp, color = colors.border)
                                            }
                                        }
                                    }
                                }
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
                                OutlinedTextField(
                                    value = editTitle,
                                    onValueChange = { editTitle = it },
                                    label = { Text("Title", style = typography.caption) },
                                    textStyle = typography.prayerPointTitle.copy(color = colors.textPrimary),
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

                                LinedNotepad(
                                    text = editBody,
                                    onTextChange = { editBody = it },
                                    placeholder = "Prayer point...",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    textStyle = typography.prayerPointBody.copy(color = colors.textPrimary)
                                )

                                Spacer(modifier = Modifier.height(PrayerSpacing.medium))

                                // Status Toggle: Active vs Answered with smooth color animation
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                                ) {
                                    val isActive = editStatus == PrayerStatus.ACTIVE
                                    val activeBg by animateColorAsState(
                                        targetValue = if (isActive) colors.textPrimary else colors.surface,
                                        label = "ActiveBg"
                                    )
                                    val activeText by animateColorAsState(
                                        targetValue = if (isActive) colors.background else colors.textPrimary,
                                        label = "ActiveText"
                                    )

                                    Button(
                                        onClick = { editStatus = PrayerStatus.ACTIVE },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(PrayerSpacing.minTouchTarget),
                                        shape = FlatSquareShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = activeBg,
                                            contentColor = activeText
                                        ),
                                        border = BorderStroke(if (isActive) 1.5.dp else 0.5.dp, colors.border)
                                    ) {
                                        Text("Active", style = typography.button)
                                    }

                                    val isAnswered = editStatus == PrayerStatus.ANSWERED
                                    val answeredBg by animateColorAsState(
                                        targetValue = if (isAnswered) colors.textPrimary else colors.surface,
                                        label = "AnsweredBg"
                                    )
                                    val answeredText by animateColorAsState(
                                        targetValue = if (isAnswered) colors.background else colors.textPrimary,
                                        label = "AnsweredText"
                                    )

                                    Button(
                                        onClick = { editStatus = PrayerStatus.ANSWERED },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(PrayerSpacing.minTouchTarget),
                                        shape = FlatSquareShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = answeredBg,
                                            contentColor = answeredText
                                        ),
                                        border = BorderStroke(if (isAnswered) 1.5.dp else 0.5.dp, colors.border)
                                    ) {
                                        Text("Answered", style = typography.button)
                                    }
                                }

                                AnimatedVisibility(
                                    visible = editStatus == PrayerStatus.ANSWERED,
                                    enter = expandVertically(tween(300, easing = FastOutSlowInEasing)) + fadeIn(tween(250)),
                                    exit = shrinkVertically(tween(250, easing = FastOutSlowInEasing)) + fadeOut(tween(150))
                                ) {
                                    Column {
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
                                }

                                Spacer(modifier = Modifier.height(PrayerSpacing.large))

                                // Save Changes Action
                                Button(
                                    onClick = {
                                        onUpdatePrayerPoint(point.id, editTitle, editBody, editStatus, editTestimony)
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
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(PrayerSpacing.large)
                        ) {
                            Text("Theme", style = typography.caption, color = colors.textSubtle)
                            Spacer(modifier = Modifier.height(PrayerSpacing.small))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                            ) {
                                listOf(
                                    ThemeMode.MORNING_LIGHT to "Morning Light",
                                    ThemeMode.QUIET_NIGHT to "Quiet Night"
                                ).forEach { (mode, label) ->
                                    val isSelected = config.themeMode == mode
                                    val bg by animateColorAsState(
                                        targetValue = if (isSelected) colors.textPrimary else colors.surface,
                                        label = "ThemeBg"
                                    )
                                    val textCol by animateColorAsState(
                                        targetValue = if (isSelected) colors.background else colors.textPrimary,
                                        label = "ThemeText"
                                    )
                                    Button(
                                        onClick = { onUpdateConfig(config.copy(themeMode = mode)) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(PrayerSpacing.minTouchTarget),
                                        shape = FlatSquareShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = bg,
                                            contentColor = textCol
                                        ),
                                        border = BorderStroke(if (isSelected) 1.5.dp else 0.5.dp, colors.border)
                                    ) {
                                        Text(label, style = typography.button)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(PrayerSpacing.large))

                            Text("Text Size", style = typography.caption, color = colors.textSubtle)
                            Spacer(modifier = Modifier.height(PrayerSpacing.small))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                            ) {
                                listOf(TextScale.LARGE, TextScale.REGULAR, TextScale.COMPACT).forEach { scale ->
                                    val isSelected = config.textScale == scale
                                    val bg by animateColorAsState(
                                        targetValue = if (isSelected) colors.textPrimary else colors.surface,
                                        label = "ScaleBg"
                                    )
                                    val textCol by animateColorAsState(
                                        targetValue = if (isSelected) colors.background else colors.textPrimary,
                                        label = "ScaleText"
                                    )
                                    Button(
                                        onClick = { onUpdateConfig(config.copy(textScale = scale)) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(PrayerSpacing.minTouchTarget),
                                        shape = FlatSquareShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = bg,
                                            contentColor = textCol
                                        ),
                                        border = BorderStroke(if (isSelected) 1.5.dp else 0.5.dp, colors.border)
                                    ) {
                                        Text(scale.displayName, style = typography.button)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(PrayerSpacing.large))

                            Text("Language", style = typography.caption, color = colors.textSubtle)
                            Spacer(modifier = Modifier.height(PrayerSpacing.small))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                            ) {
                                listOf(
                                    LocaleDialect.EN_AU_UK to "AU / UK",
                                    LocaleDialect.EN_US to "US"
                                ).forEach { (dialect, label) ->
                                    val isSelected = config.localeDialect == dialect
                                    val bg by animateColorAsState(
                                        targetValue = if (isSelected) colors.textPrimary else colors.surface,
                                        label = "DialectBg"
                                    )
                                    val textCol by animateColorAsState(
                                        targetValue = if (isSelected) colors.background else colors.textPrimary,
                                        label = "DialectText"
                                    )
                                    Button(
                                        onClick = { onUpdateConfig(config.copy(localeDialect = dialect)) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(PrayerSpacing.minTouchTarget),
                                        shape = FlatSquareShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = bg,
                                            contentColor = textCol
                                        ),
                                        border = BorderStroke(if (isSelected) 1.5.dp else 0.5.dp, colors.border)
                                    ) {
                                        Text(label, style = typography.button)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(PrayerSpacing.large))

                            Text("Historic Prayers", style = typography.caption, color = colors.textSubtle)
                            Spacer(modifier = Modifier.height(PrayerSpacing.small))
                            val isHistoricOn = config.blendHistoricPrayers
                            val histBg by animateColorAsState(
                                targetValue = if (isHistoricOn) colors.textPrimary else colors.surface,
                                label = "HistBg"
                            )
                            val histText by animateColorAsState(
                                targetValue = if (isHistoricOn) colors.background else colors.textPrimary,
                                label = "HistText"
                            )
                            Button(
                                onClick = { onUpdateConfig(config.copy(blendHistoricPrayers = !config.blendHistoricPrayers)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(PrayerSpacing.minTouchTarget),
                                shape = FlatSquareShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = histBg,
                                    contentColor = histText
                                ),
                                border = BorderStroke(if (isHistoricOn) 1.5.dp else 0.5.dp, colors.border)
                            ) {
                                Text(
                                    text = if (isHistoricOn) "Blend into daily prayer: ON" else "Blend into daily prayer: OFF",
                                    style = typography.button
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Entity Context Action Dialogs ---

    if (entityForContextActions != null) {
        val entity = entityForContextActions!!
        val isHistoric = entity.isPreloadedHistoric
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
                            onAddForEntity(target)
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
                    text = "Edit person or group",
                    style = typography.prayerPointTitle,
                    color = colors.textPrimary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
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
                    text = "Delete this person or group and all associated prayer points? This cannot be undone.",
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
                    OutlinedButton(
                        onClick = {
                            val newStatus = if (isAnswered) PrayerStatus.ACTIVE else PrayerStatus.ANSWERED
                            val testimony = if (newStatus == PrayerStatus.ANSWERED) point.answeredTestimony else null
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

                    OutlinedButton(
                        onClick = {
                            editingPoint = point
                            editTitle = point.title
                            editBody = point.description
                            editStatus = point.status
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
