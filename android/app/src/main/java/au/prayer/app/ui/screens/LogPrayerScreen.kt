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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import au.prayer.app.data.local.PrayerRepository
import au.prayer.app.data.models.IndividualEntity
import au.prayer.app.data.models.RootCode
import au.prayer.app.network.PrayerApiClient
import au.prayer.app.ui.components.LinedNotepad
import au.prayer.app.ui.gestures.edgeSwipeRight
import au.prayer.app.ui.navigation.LifoBackStack
import au.prayer.app.ui.theme.FlatSquareShape
import au.prayer.app.ui.theme.PrayerColors
import au.prayer.app.ui.theme.PrayerSpacing
import au.prayer.app.ui.theme.PrayerTypography
import kotlinx.coroutines.launch

private enum class LogStep {
    DRAFT_PRAYER_POINTS,
    SELECT_ENTITY
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LogPrayerScreen(
    initialEntity: IndividualEntity? = null,
    allEntities: List<IndividualEntity>,
    colors: PrayerColors,
    typography: PrayerTypography,
    apiClient: PrayerApiClient,
    repository: PrayerRepository? = null,
    onCreateEntity: (RootCode, String) -> IndividualEntity,
    onSavePrayerPoint: (entityId: String, text: String, title: String?) -> Unit,
    onUpdateEntity: (id: String, displayName: String, rootCode: RootCode) -> Unit = { _, _, _ -> },
    onDeleteEntity: (String) -> Unit = {},
    onSavedEntity: (IndividualEntity) -> Unit = { _ -> },
    onBackToHome: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val logBackStack = remember {
        LifoBackStack(LogStep.DRAFT_PRAYER_POINTS)
    }
    val currentStep = logBackStack.current

    var selectedEntity by remember { mutableStateOf(initialEntity) }
    var selectedRootFilter by remember { mutableStateOf(initialEntity?.rootCode ?: RootCode.PEOPLE) }
    var isRootDropdownExpanded by remember { mutableStateOf(false) }

    var newEntityName by remember { mutableStateOf("") }
    var isCreatingNewEntity by remember { mutableStateOf(false) }

    var directText by remember { mutableStateOf(TextFieldValue("• ")) }

    // Context dialogs for entities
    var entityForContextActions by remember { mutableStateOf<IndividualEntity?>(null) }
    var entityToEdit by remember { mutableStateOf<IndividualEntity?>(null) }
    var editEntityName by remember { mutableStateOf("") }
    var editEntityRoot by remember { mutableStateOf(RootCode.PEOPLE) }
    var entityToDelete by remember { mutableStateOf<IndividualEntity?>(null) }

    fun handleDirectTextChange(newVal: TextFieldValue) {
        val oldStr = directText.text
        val newStr = newVal.text

        if (newStr.length > oldStr.length && newStr.endsWith("\n")) {
            val updated = newStr + "• "
            directText = TextFieldValue(updated, TextRange(updated.length))
        } else if (newStr.isEmpty()) {
            directText = TextFieldValue("• ", TextRange(2))
        } else {
            directText = newVal
        }
    }

    fun handleLogPrayerBack(): Boolean {
        return if (logBackStack.canPop) {
            logBackStack.pop()
            true
        } else {
            onBackToHome()
            true
        }
    }

    BackHandler(enabled = true) {
        handleLogPrayerBack()
    }

    Scaffold(
        containerColor = colors.background,
        contentColor = colors.textPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            selectedEntity != null -> "Praying for ${selectedEntity!!.displayName}"
                            currentStep == LogStep.SELECT_ENTITY -> "Who are you praying for?"
                            else -> "Add prayer points"
                        },
                        style = typography.prayerPointTitle,
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    val canGoBack = logBackStack.canPop || initialEntity != null
                    if (canGoBack) {
                        IconButton(onClick = { handleLogPrayerBack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.textPrimary
                            )
                        }
                    }
                },
                actions = {
                    val hasDraftText = directText.text.replace("•", "").trim().isNotBlank()
                    if (currentStep == LogStep.DRAFT_PRAYER_POINTS && hasDraftText) {
                        TextButton(
                            onClick = {
                                val text = directText.text.trim()
                                if (text.isNotBlank()) {
                                    if (selectedEntity != null) {
                                        onSavePrayerPoint(selectedEntity!!.id, text, null)
                                        onSavedEntity(selectedEntity!!)
                                    } else {
                                        logBackStack.push(LogStep.SELECT_ENTITY)
                                    }
                                }
                            }
                        ) {
                            Text(
                                text = if (selectedEntity != null) "Save" else "Next",
                                style = typography.button,
                                color = colors.textPrimary
                            )
                        }
                    }
                    IconButton(onClick = onBackToHome) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = colors.textSubtle
                        )
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
                .edgeSwipeRight { handleLogPrayerBack() }
        ) {
            HorizontalDivider(thickness = 0.5.dp, color = colors.borderSubtle)

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState == LogStep.SELECT_ENTITY) {
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
                label = "LogStepTransition",
                modifier = Modifier.fillMaxSize()
            ) { step ->
                when (step) {
                    LogStep.DRAFT_PRAYER_POINTS -> {
                        val hasDraftText = directText.text.replace("•", "").trim().isNotBlank()

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .imePadding()
                        ) {
                            // Section 1: Lined Notepad Canvas
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(colors.paperBackground)
                            ) {
                                LinedNotepad(
                                    text = directText,
                                    onTextChange = { handleDirectTextChange(it) },
                                    colors = colors,
                                    typography = typography,
                                    placeholder = "Write prayer points..."
                                )
                            }

                            // Advance or Save Button
                            AnimatedVisibility(
                                visible = hasDraftText,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = PrayerSpacing.large,
                                            vertical = PrayerSpacing.small
                                        ),
                                    shape = FlatSquareShape,
                                    color = colors.textPrimary,
                                    tonalElevation = PrayerSpacing.elevationCard,
                                    shadowElevation = PrayerSpacing.elevationCard
                                ) {
                                    Button(
                                        onClick = {
                                            val text = directText.text.trim()
                                            if (text.isNotBlank()) {
                                                if (selectedEntity != null) {
                                                    onSavePrayerPoint(selectedEntity!!.id, text, null)
                                                    onSavedEntity(selectedEntity!!)
                                                } else {
                                                    logBackStack.push(LogStep.SELECT_ENTITY)
                                                }
                                            }
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
                                        Text(
                                            text = if (selectedEntity != null) "Save to ${selectedEntity!!.displayName}" else "Next",
                                            style = typography.button
                                        )
                                    }
                                }
                            }
                        }
                    }

                    LogStep.SELECT_ENTITY -> {
                        val filteredEntities = allEntities.filter { !it.isPreloadedHistoric && it.rootCode == selectedRootFilter }

                        Column(modifier = Modifier.fillMaxSize()) {
                            // Top Root Sphere Dropdown / Selector Bar
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = FlatSquareShape,
                                color = colors.surface,
                                tonalElevation = PrayerSpacing.elevationSubtle,
                                shadowElevation = PrayerSpacing.elevationSubtle
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = PrayerSpacing.large),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = { isRootDropdownExpanded = true }
                                            ),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedRootFilter.displayTitle,
                                            style = typography.prayerPointTitle,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "▼",
                                            style = typography.caption,
                                            color = colors.textSubtle
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = isRootDropdownExpanded,
                                        onDismissRequest = { isRootDropdownExpanded = false },
                                        modifier = Modifier.background(colors.surfaceElevated)
                                    ) {
                                        RootCode.entries.forEach { root ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = root.displayTitle,
                                                        style = typography.prayerPointBody,
                                                        color = colors.textPrimary
                                                    )
                                                },
                                                onClick = {
                                                    selectedRootFilter = root
                                                    isRootDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(thickness = 0.5.dp, color = colors.borderSubtle)

                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                // Add New Entity Item
                                item {
                                    if (isCreatingNewEntity) {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = FlatSquareShape,
                                            color = colors.surface,
                                            tonalElevation = PrayerSpacing.elevationSubtle,
                                            shadowElevation = PrayerSpacing.elevationSubtle
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(PrayerSpacing.large)
                                            ) {
                                                OutlinedTextField(
                                                    value = newEntityName,
                                                    onValueChange = { newEntityName = it },
                                                    placeholder = {
                                                        Text(
                                                            text = "Name for ${selectedRootFilter.displayTitle}...",
                                                            style = typography.prayerPointBody,
                                                            color = colors.textSubtle
                                                        )
                                                    },
                                                    textStyle = typography.prayerPointBody.copy(color = colors.textPrimary),
                                                    shape = FlatSquareShape,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = colors.textPrimary,
                                                        unfocusedBorderColor = colors.border,
                                                        cursorColor = colors.textPrimary
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(PrayerSpacing.medium))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(PrayerSpacing.small)
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            isCreatingNewEntity = false
                                                            newEntityName = ""
                                                        },
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(PrayerSpacing.primaryActionHeight),
                                                        shape = FlatSquareShape,
                                                        border = BorderStroke(0.5.dp, colors.border),
                                                        colors = ButtonDefaults.outlinedButtonColors(
                                                            contentColor = colors.textSubtle
                                                        )
                                                    ) {
                                                        Text("Cancel", style = typography.button)
                                                    }

                                                    Button(
                                                        onClick = {
                                                            if (newEntityName.isNotBlank()) {
                                                                val created = onCreateEntity(selectedRootFilter, newEntityName.trim())
                                                                val text = directText.text.trim()
                                                                onSavePrayerPoint(created.id, text, null)
                                                                onSavedEntity(created)
                                                            }
                                                        },
                                                        enabled = newEntityName.isNotBlank(),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(PrayerSpacing.primaryActionHeight),
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
                                                }
                                            }
                                        }
                                    } else {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { isCreatingNewEntity = true },
                                            shape = FlatSquareShape,
                                            color = colors.surface,
                                            contentColor = colors.textPrimary,
                                            tonalElevation = PrayerSpacing.elevationSubtle,
                                            shadowElevation = PrayerSpacing.elevationSubtle
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        horizontal = PrayerSpacing.large,
                                                        vertical = PrayerSpacing.medium
                                                    )
                                            ) {
                                                Text(
                                                    text = "+ Add new to ${selectedRootFilter.displayTitle}",
                                                    style = typography.button,
                                                    color = colors.textPrimary
                                                )
                                            }
                                        }
                                    }
                                    HorizontalDivider(thickness = 0.5.dp, color = colors.borderSubtle)
                                }

                                if (filteredEntities.isEmpty() && !isCreatingNewEntity) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(PrayerSpacing.large),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No entries in ${selectedRootFilter.displayTitle}",
                                                style = typography.caption,
                                                color = colors.textSubtle
                                            )
                                        }
                                    }
                                }

                                items(
                                    filteredEntities,
                                    key = { it.id }
                                ) { entity ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .animateItem()
                                            .combinedClickable(
                                                onClick = {
                                                    // Direct 1-tap save without confirmation
                                                    val text = directText.text.trim()
                                                    onSavePrayerPoint(entity.id, text, null)
                                                    onSavedEntity(entity)
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    entityForContextActions = entity
                                                }
                                            ),
                                        shape = FlatSquareShape,
                                        color = colors.surface,
                                        contentColor = colors.textPrimary,
                                        tonalElevation = PrayerSpacing.elevationSubtle,
                                        shadowElevation = PrayerSpacing.elevationSubtle
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
                                            HorizontalDivider(thickness = 0.5.dp, color = colors.borderSubtle)
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

    // --- Entity Context Action Dialogs (Edit & Delete) ---

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
            containerColor = colors.surfaceElevated,
            tonalElevation = PrayerSpacing.elevationModal
        )
    }

    if (entityToEdit != null) {
        val entity = entityToEdit!!
        AlertDialog(
            onDismissRequest = { entityToEdit = null },
            title = {
                Text(
                    text = "Edit entry",
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
                        RootCode.entries.forEach { root ->
                            val isSelected = editEntityRoot == root
                            Button(
                                onClick = { editEntityRoot = root },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(PrayerSpacing.minTouchTarget),
                                shape = FlatSquareShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) colors.textPrimary else colors.surface,
                                    contentColor = if (isSelected) colors.background else colors.textPrimary
                                ),
                                border = BorderStroke(if (isSelected) 1.5.dp else 0.5.dp, colors.border)
                            ) {
                                Text(root.displayTitle.take(6), style = typography.caption)
                            }
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
            containerColor = colors.surfaceElevated,
            tonalElevation = PrayerSpacing.elevationModal
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
                    text = "Delete this record and all associated prayer points? This cannot be undone.",
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
            containerColor = colors.surfaceElevated,
            tonalElevation = PrayerSpacing.elevationModal
        )
    }
}
