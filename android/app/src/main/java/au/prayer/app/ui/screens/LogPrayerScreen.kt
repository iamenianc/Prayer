package au.prayer.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import au.prayer.app.data.models.IndividualEntity
import au.prayer.app.data.models.RootCode
import au.prayer.app.network.CandidatePrayerPoint
import au.prayer.app.network.GuideRequest
import au.prayer.app.network.PrayerApiClient
import au.prayer.app.ui.gestures.edgeSwipeRight
import au.prayer.app.ui.navigation.LifoBackStack
import au.prayer.app.ui.theme.FlatSquareShape
import au.prayer.app.ui.theme.PrayerColors
import au.prayer.app.ui.theme.PrayerSpacing
import au.prayer.app.ui.theme.PrayerTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class LogStep {
    SELECT_ENTITY,
    CHOOSE_PATHWAY,
    DIRECT_ENTRY,
    GUIDE_OPEN_HEART,
    GUIDE_CLARIFYING,
    GUIDE_CANDIDATES
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LogPrayerScreen(
    initialEntity: IndividualEntity? = null,
    allEntities: List<IndividualEntity>,
    colors: PrayerColors,
    typography: PrayerTypography,
    apiClient: PrayerApiClient,
    onCreateEntity: (RootCode, String) -> IndividualEntity,
    onSavePrayerPoint: (entityId: String, text: String, title: String?) -> Unit,
    onUpdateEntity: (id: String, displayName: String, rootCode: RootCode) -> Unit = { _, _, _ -> },
    onDeleteEntity: (String) -> Unit = {},
    onBackToHome: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val logBackStack = remember {
        LifoBackStack(if (initialEntity != null) LogStep.CHOOSE_PATHWAY else LogStep.SELECT_ENTITY)
    }
    val currentStep = logBackStack.current

    // Long-press entity context action states
    var entityForContextActions by remember { mutableStateOf<IndividualEntity?>(null) }
    var entityToEdit by remember { mutableStateOf<IndividualEntity?>(null) }
    var editEntityName by remember { mutableStateOf("") }
    var editEntityRoot by remember { mutableStateOf(RootCode.PEOPLE) }
    var entityToDelete by remember { mutableStateOf<IndividualEntity?>(null) }

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

    var selectedEntity by remember { mutableStateOf(initialEntity) }

    var newEntityName by remember { mutableStateOf("") }
    var newEntityRoot by remember { mutableStateOf(RootCode.PEOPLE) }

    var directText by remember { mutableStateOf(TextFieldValue("• ")) }

    var guideInitialReflection by remember { mutableStateOf("") }
    var guideClarifyingQuestion by remember { mutableStateOf<String?>(null) }
    var guideUserAnswer by remember { mutableStateOf("") }
    var guideTurnCount by remember { mutableIntStateOf(0) }
    var candidatePoints by remember { mutableStateOf<List<CandidatePrayerPoint>>(emptyList()) }
    var canRequestMore by remember { mutableStateOf(true) }
    var isAwaitingApi by remember { mutableStateOf(false) }
    var apiErrorMessage by remember { mutableStateOf<String?>(null) }

    // Animated loading copy
    var loadingPromptText by remember { mutableStateOf("Attuning...") }
    LaunchedEffect(isAwaitingApi) {
        if (isAwaitingApi) {
            loadingPromptText = "Attuning..."
            delay(1200)
            if (isAwaitingApi) {
                loadingPromptText = "Distilling thoughts..."
                delay(1500)
                if (isAwaitingApi) {
                    loadingPromptText = "Formulating prayer points..."
                }
            }
        }
    }

    fun handleDirectTextChange(newVal: TextFieldValue) {
        val oldStr = directText.text
        val newStr = newVal.text

        if (newStr.length > oldStr.length && newStr.endsWith("\n")) {
            val updated = newStr + "• "
            directText = TextFieldValue(updated, androidx.compose.ui.text.TextRange(updated.length))
        } else if (newStr.isEmpty()) {
            directText = TextFieldValue("• ", androidx.compose.ui.text.TextRange(2))
        } else {
            directText = newVal
        }
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
                            else -> "Who are you praying for?"
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
                    val canSave = currentStep == LogStep.CHOOSE_PATHWAY &&
                            directText.text.replace("•", "").trim().isNotBlank() &&
                            selectedEntity != null
                    if (canSave) {
                        TextButton(
                            onClick = {
                                val text = directText.text.trim()
                                if (text.isNotBlank() && selectedEntity != null) {
                                    val fallbackTitle = apiClient.generateOfflineFallbackTitle(text)
                                    onSavePrayerPoint(selectedEntity!!.id, text, fallbackTitle)
                                    onBackToHome()
                                }
                            }
                        ) {
                            Text("Save", style = typography.button, color = colors.textPrimary)
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
            HorizontalDivider(thickness = 0.5.dp, color = colors.border)

            if (isAwaitingApi) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = colors.textPrimary,
                    trackColor = colors.border
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PrayerSpacing.large, vertical = PrayerSpacing.small),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = loadingPromptText,
                        transitionSpec = {
                            fadeIn(tween(250)) togetherWith fadeOut(tween(200))
                        },
                        label = "LoadingPromptAnim"
                    ) { prompt ->
                        Text(
                            text = prompt,
                            style = typography.caption,
                            color = colors.textSubtle
                        )
                    }
                }
            }

            val stepOrder = listOf(
                LogStep.SELECT_ENTITY,
                LogStep.CHOOSE_PATHWAY,
                LogStep.DIRECT_ENTRY,
                LogStep.GUIDE_OPEN_HEART,
                LogStep.GUIDE_CLARIFYING,
                LogStep.GUIDE_CANDIDATES
            )

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    val initialIdx = stepOrder.indexOf(initialState)
                    val targetIdx = stepOrder.indexOf(targetState)
                    if (targetIdx >= initialIdx) {
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
                modifier = Modifier
                    .fillMaxSize()
            ) { step ->
                when (step) {
                    LogStep.SELECT_ENTITY -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                OutlinedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(PrayerSpacing.medium),
                                    shape = FlatSquareShape,
                                    border = BorderStroke(0.5.dp, colors.border),
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = colors.surface
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(PrayerSpacing.large)) {
                                        Text(
                                            text = "Add a person or group",
                                            style = typography.prayerPointTitle,
                                            color = colors.textPrimary,
                                            modifier = Modifier.padding(bottom = PrayerSpacing.small)
                                        )

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
                                                label = "PeopleBg"
                                            )
                                            val peopleText by animateColorAsState(
                                                targetValue = if (isPeople) colors.background else colors.textPrimary,
                                                label = "PeopleText"
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
                                                label = "GroupsBg"
                                            )
                                            val groupsText by animateColorAsState(
                                                targetValue = if (isGroups) colors.background else colors.textPrimary,
                                                label = "GroupsText"
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

                                        Spacer(modifier = Modifier.height(PrayerSpacing.large))

                                        Button(
                                            onClick = {
                                                if (newEntityName.isNotBlank()) {
                                                    val created = onCreateEntity(newEntityRoot, newEntityName)
                                                    selectedEntity = created
                                                    logBackStack.push(LogStep.CHOOSE_PATHWAY)
                                                }
                                            },
                                            enabled = newEntityName.isNotBlank(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(PrayerSpacing.primaryActionHeight),
                                            shape = FlatSquareShape,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = colors.textPrimary,
                                                contentColor = colors.background,
                                                disabledContainerColor = colors.border,
                                                disabledContentColor = colors.textSubtle
                                            )
                                        ) {
                                            Text("Add & continue", style = typography.button)
                                        }
                                    }
                                }
                            }

                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = PrayerSpacing.large,
                                            vertical = PrayerSpacing.medium
                                        )
                                ) {
                                    Text("From your journal", style = typography.caption, color = colors.textSubtle)
                                }
                                HorizontalDivider(thickness = 0.5.dp, color = colors.border)
                            }

                            items(
                                allEntities.filter { !it.isPreloadedHistoric },
                                key = { it.id }
                            ) { entity ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem()
                                        .combinedClickable(
                                            onClick = {
                                                selectedEntity = entity
                                                logBackStack.push(LogStep.CHOOSE_PATHWAY)
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

                    LogStep.CHOOSE_PATHWAY -> {
                        val canSave = directText.text.replace("•", "").trim().isNotBlank() && selectedEntity != null

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .imePadding()
                        ) {
                            // Section 1: Lined Notepad with mathematical line-locking via TextLayoutResult
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(colors.surface)
                            ) {
                                val density = LocalDensity.current
                                val bodyFontSize = typography.prayerPointBody.fontSize
                                val notepadLineHeight = if (bodyFontSize.value > 0f) {
                                    (bodyFontSize.value * 1.9f).sp
                                } else {
                                    36.sp
                                }

                                @Suppress("DEPRECATION")
                                val notepadTextStyle = typography.prayerPointBody.copy(
                                    color = colors.textPrimary,
                                    lineHeight = notepadLineHeight,
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    ),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.None
                                    )
                                )

                                val textMeasurer = rememberTextMeasurer()
                                val sampleMeasure = remember(notepadTextStyle, density) {
                                    textMeasurer.measure(
                                        text = AnnotatedString("• Initial sample line\n• Second line"),
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

                                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                    val viewportHeight = maxHeight

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = viewportHeight)
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

                                                // Exact pixel coordinate for the top boundary of line 0
                                                val firstLineTop = topMarginPx + if (layout != null && layout.lineCount > 0) {
                                                    layout.getLineTop(0)
                                                } else {
                                                    sampleMeasure.getLineTop(0)
                                                }

                                                // 1. Draw top line above first row
                                                drawLine(
                                                    color = lineColor,
                                                    start = Offset(0f, firstLineTop),
                                                    end = Offset(size.width, firstLineTop),
                                                    strokeWidth = strokeWidth
                                                )

                                                // 2. Draw any extra ruled lines above the first line if space permits
                                                var aboveY = firstLineTop - singleLineHeight
                                                while (aboveY >= 0f) {
                                                    drawLine(
                                                        color = lineColor,
                                                        start = Offset(0f, aboveY),
                                                        end = Offset(size.width, aboveY),
                                                        strokeWidth = strokeWidth
                                                    )
                                                    aboveY -= singleLineHeight
                                                }

                                                // 3. Draw ruled lines at the exact bottom of every line of text
                                                val lineCount = layout?.lineCount ?: 1
                                                for (i in 0 until lineCount) {
                                                    val lineBottom = topMarginPx + if (layout != null) {
                                                        layout.getLineBottom(i)
                                                    } else {
                                                        sampleMeasure.getLineBottom(0)
                                                    }
                                                    drawLine(
                                                        color = lineColor,
                                                        start = Offset(0f, lineBottom),
                                                        end = Offset(size.width, lineBottom),
                                                        strokeWidth = strokeWidth
                                                    )
                                                }

                                                // 4. Draw remaining ruled lines below the text down to the bottom of the canvas
                                                val lastBottom = topMarginPx + if (layout != null && layout.lineCount > 0) {
                                                    layout.getLineBottom(layout.lineCount - 1)
                                                } else {
                                                    sampleMeasure.getLineBottom(0)
                                                }

                                                var belowY = lastBottom + singleLineHeight
                                                while (belowY <= size.height) {
                                                    drawLine(
                                                        color = lineColor,
                                                        start = Offset(0f, belowY),
                                                        end = Offset(size.width, belowY),
                                                        strokeWidth = strokeWidth
                                                    )
                                                    belowY += singleLineHeight
                                                }
                                            }
                                    ) {
                                        BasicTextField(
                                            value = directText,
                                            onValueChange = { handleDirectTextChange(it) },
                                            textStyle = notepadTextStyle,
                                            cursorBrush = SolidColor(colors.textPrimary),
                                            onTextLayout = { textLayoutResult = it },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = topMargin),
                                            decorationBox = { innerTextField ->
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = PrayerSpacing.large)
                                                ) {
                                                    if (directText.text.isEmpty() || directText.text == "• ") {
                                                        Text(
                                                            text = "• Write prayer points...",
                                                            style = notepadTextStyle.copy(
                                                                color = colors.textSubtle.copy(alpha = 0.5f)
                                                            )
                                                        )
                                                    }
                                                    innerTextField()
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            // Commit button right beneath the notepad when prayer points are typed
                            AnimatedVisibility(
                                visible = canSave,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Button(
                                    onClick = {
                                        val text = directText.text.trim()
                                        if (text.isNotBlank() && selectedEntity != null) {
                                            val fallbackTitle = apiClient.generateOfflineFallbackTitle(text)
                                            onSavePrayerPoint(selectedEntity!!.id, text, fallbackTitle)
                                            onBackToHome()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = PrayerSpacing.large,
                                            vertical = PrayerSpacing.small
                                        )
                                        .height(PrayerSpacing.primaryActionHeight),
                                    shape = FlatSquareShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.textPrimary,
                                        contentColor = colors.background
                                    )
                                ) {
                                    Text(
                                        text = "Save to ${selectedEntity?.displayName ?: "Journal"}",
                                        style = typography.button
                                    )
                                }
                            }

                            HorizontalDivider(thickness = 0.5.dp, color = colors.border)

                            // Section 2: The other button ("Prayer Assistant"), pushed down to the bottom
                            Surface(
                                onClick = { logBackStack.push(LogStep.GUIDE_OPEN_HEART) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                shape = FlatSquareShape,
                                color = colors.surface,
                                contentColor = colors.textPrimary,
                                tonalElevation = 0.dp
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Prayer Assistant", style = typography.homeAction, color = colors.textPrimary)
                                }
                            }
                        }
                    }

                    LogStep.DIRECT_ENTRY -> {
                        LaunchedEffect(Unit) {
                            logBackStack.replace(LogStep.CHOOSE_PATHWAY)
                        }
                    }

                    LogStep.GUIDE_OPEN_HEART -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(PrayerSpacing.large)
                        ) {
                            Text(
                                text = "What is on your heart?",
                                style = typography.prayerPointTitle,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(bottom = PrayerSpacing.medium)
                            )

                            OutlinedTextField(
                                value = guideInitialReflection,
                                onValueChange = { guideInitialReflection = it },
                                placeholder = {
                                    Text(
                                        text = "Write freely about what is on your heart...",
                                        style = typography.prayerPointBody,
                                        color = colors.textSubtle
                                    )
                                },
                                textStyle = typography.prayerPointBody.copy(color = colors.textPrimary),
                                shape = FlatSquareShape,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.textPrimary,
                                    unfocusedBorderColor = colors.border,
                                    cursorColor = colors.textPrimary
                                )
                            )

                            if (apiErrorMessage != null) {
                                Text(
                                    text = apiErrorMessage!!,
                                    style = typography.caption,
                                    color = colors.textSubtle,
                                    modifier = Modifier.padding(vertical = PrayerSpacing.small)
                                )
                            }

                            Spacer(modifier = Modifier.height(PrayerSpacing.medium))

                            Button(
                                onClick = {
                                    if (!isAwaitingApi && guideInitialReflection.isNotBlank()) {
                                        isAwaitingApi = true
                                        apiErrorMessage = null
                                        coroutineScope.launch {
                                            val result = apiClient.getGuidance(
                                                GuideRequest(
                                                    initialReflection = guideInitialReflection,
                                                    root = selectedEntity?.rootCode?.name
                                                )
                                            )
                                            isAwaitingApi = false
                                            result.fold(
                                                onSuccess = { resp ->
                                                    if (!resp.skipQuestion && !resp.clarifyingQuestion.isNullOrBlank()) {
                                                        guideClarifyingQuestion = resp.clarifyingQuestion
                                                        guideTurnCount = 1
                                                        logBackStack.push(LogStep.GUIDE_CLARIFYING)
                                                    } else {
                                                        candidatePoints = resp.candidatePrayerPoints
                                                        logBackStack.push(LogStep.GUIDE_CANDIDATES)
                                                    }
                                                },
                                                onFailure = {
                                                    apiErrorMessage = "Assistant unavailable. You can record directly."
                                                }
                                            )
                                        }
                                    }
                                },
                                enabled = !isAwaitingApi && guideInitialReflection.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(PrayerSpacing.primaryActionHeight),
                                shape = FlatSquareShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.textPrimary,
                                    contentColor = colors.background,
                                    disabledContainerColor = colors.border,
                                    disabledContentColor = colors.textSubtle
                                )
                            ) {
                                Text(
                                    text = if (isAwaitingApi) "Attuning..." else "Continue",
                                    style = typography.button
                                )
                            }
                        }
                    }

                    LogStep.GUIDE_CLARIFYING -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(PrayerSpacing.large)
                        ) {
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = FlatSquareShape,
                                border = BorderStroke(0.5.dp, colors.border),
                                colors = CardDefaults.outlinedCardColors(containerColor = colors.surface)
                            ) {
                                Column(modifier = Modifier.padding(PrayerSpacing.large)) {
                                    Text(
                                        text = "Clarifying question",
                                        style = typography.caption,
                                        color = colors.textSubtle
                                    )
                                    Spacer(modifier = Modifier.height(PrayerSpacing.small))
                                    Text(
                                        text = guideClarifyingQuestion ?: "",
                                        style = typography.prayerPointTitle,
                                        color = colors.textPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(PrayerSpacing.medium))

                            OutlinedTextField(
                                value = guideUserAnswer,
                                onValueChange = { guideUserAnswer = it },
                                placeholder = {
                                    Text(
                                        text = "Your response...",
                                        style = typography.prayerPointBody,
                                        color = colors.textSubtle
                                    )
                                },
                                textStyle = typography.prayerPointBody.copy(color = colors.textPrimary),
                                shape = FlatSquareShape,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
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
                                        isAwaitingApi = true
                                        coroutineScope.launch {
                                            val result = apiClient.getGuidance(
                                                GuideRequest(
                                                    initialReflection = guideInitialReflection,
                                                    root = selectedEntity?.rootCode?.name,
                                                    clarifyingQuestion = guideClarifyingQuestion,
                                                    userResponse = "skip"
                                                )
                                            )
                                            isAwaitingApi = false
                                            result.fold(
                                                onSuccess = { resp ->
                                                    candidatePoints = resp.candidatePrayerPoints
                                                    logBackStack.push(LogStep.GUIDE_CANDIDATES)
                                                },
                                                onFailure = {
                                                    apiErrorMessage = "Assistant unavailable."
                                                }
                                            )
                                        }
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
                                    Text("Skip to prayer points", style = typography.button)
                                }

                                Button(
                                    onClick = {
                                        if (!isAwaitingApi && guideUserAnswer.isNotBlank()) {
                                            isAwaitingApi = true
                                            coroutineScope.launch {
                                                val result = apiClient.getGuidance(
                                                    GuideRequest(
                                                        initialReflection = guideInitialReflection,
                                                        root = selectedEntity?.rootCode?.name,
                                                        clarifyingQuestion = guideClarifyingQuestion,
                                                        userResponse = guideUserAnswer
                                                    )
                                                )
                                                isAwaitingApi = false
                                                result.fold(
                                                    onSuccess = { resp ->
                                                        candidatePoints = resp.candidatePrayerPoints
                                                        logBackStack.push(LogStep.GUIDE_CANDIDATES)
                                                    },
                                                    onFailure = {
                                                        apiErrorMessage = "Assistant unavailable."
                                                    }
                                                )
                                            }
                                        }
                                    },
                                    enabled = !isAwaitingApi && guideUserAnswer.isNotBlank(),
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
                                    Text(
                                        text = if (isAwaitingApi) "Attuning..." else "Continue",
                                        style = typography.button
                                    )
                                }
                            }
                        }
                    }

                    LogStep.GUIDE_CANDIDATES -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(PrayerSpacing.large)
                        ) {
                            Text(
                                text = "Review prayer points",
                                style = typography.topicTitle,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(bottom = PrayerSpacing.medium)
                            )

                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(candidatePoints) { card ->
                                    // Animated entrance for candidate cards
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) +
                                                slideInVertically(animationSpec = tween(400, easing = FastOutSlowInEasing)) { it / 8 }
                                    ) {
                                        OutlinedCard(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = PrayerSpacing.medium),
                                            shape = FlatSquareShape,
                                            border = BorderStroke(0.5.dp, colors.border),
                                            colors = CardDefaults.outlinedCardColors(
                                                containerColor = colors.surface
                                            )
                                        ) {
                                            Column(modifier = Modifier.padding(PrayerSpacing.large)) {
                                                Text(
                                                    text = card.title,
                                                    style = typography.prayerPointTitle,
                                                    color = colors.textPrimary
                                                )
                                                Spacer(modifier = Modifier.height(PrayerSpacing.small))
                                                Text(
                                                    text = card.description,
                                                    style = typography.prayerPointBody,
                                                    color = colors.textPrimary
                                                )
                                                Spacer(modifier = Modifier.height(PrayerSpacing.large))
                                                Button(
                                                    onClick = {
                                                        selectedEntity?.let { entity ->
                                                            onSavePrayerPoint(entity.id, card.description, card.title)
                                                            onBackToHome()
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
                                                        text = "Save to ${selectedEntity?.displayName ?: "Journal"}",
                                                        style = typography.button
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (canRequestMore) {
                                Spacer(modifier = Modifier.height(PrayerSpacing.small))
                                OutlinedButton(
                                    onClick = {
                                        canRequestMore = false
                                        isAwaitingApi = true
                                        coroutineScope.launch {
                                            val result = apiClient.getGuidance(
                                                GuideRequest(
                                                    initialReflection = guideInitialReflection,
                                                    root = selectedEntity?.rootCode?.name,
                                                    requestMore = true
                                                )
                                            )
                                            isAwaitingApi = false
                                            result.onSuccess { resp ->
                                                candidatePoints = candidatePoints + resp.candidatePrayerPoints
                                            }
                                        }
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
                                    Text("Suggest 2 more", style = typography.button)
                                }
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
                            label = "EditLogPeopleBg"
                        )
                        val peopleText by animateColorAsState(
                            targetValue = if (isPeople) colors.background else colors.textPrimary,
                            label = "EditLogPeopleText"
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
                            label = "EditLogGroupsBg"
                        )
                        val groupsText by animateColorAsState(
                            targetValue = if (isGroups) colors.background else colors.textPrimary,
                            label = "EditLogGroupsText"
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
}
