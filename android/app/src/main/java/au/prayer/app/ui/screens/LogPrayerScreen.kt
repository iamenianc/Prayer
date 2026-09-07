package au.prayer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import au.prayer.app.data.models.IndividualEntity
import au.prayer.app.data.models.RootCode
import au.prayer.app.network.CandidatePrayerPoint
import au.prayer.app.network.GuideRequest
import au.prayer.app.network.PrayerApiClient
import au.prayer.app.ui.theme.PrayerColors
import au.prayer.app.ui.theme.PrayerTypography
import kotlinx.coroutines.launch

private enum class LogStep {
    SELECT_ENTITY,
    CHOOSE_PATHWAY,
    DIRECT_ENTRY,
    GUIDE_OPEN_HEART,
    GUIDE_CLARIFYING,
    GUIDE_CANDIDATES
}

@Composable
fun LogPrayerScreen(
    initialEntity: IndividualEntity? = null,
    allEntities: List<IndividualEntity>,
    colors: PrayerColors,
    typography: PrayerTypography,
    apiClient: PrayerApiClient,
    onCreateEntity: (RootCode, String) -> IndividualEntity,
    onSavePetition: (entityId: String, text: String, title: String?) -> Unit,
    onBackToHome: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var currentStep by remember {
        mutableStateOf(if (initialEntity != null) LogStep.CHOOSE_PATHWAY else LogStep.SELECT_ENTITY)
    }
    var selectedEntity by remember { mutableStateOf(initialEntity) }

    // State for Adding Person/Group in Step 0
    var newEntityName by remember { mutableStateOf("") }
    var newEntityRoot by remember { mutableStateOf(RootCode.PEOPLE) }

    // Direct Entry State
    var directText by remember { mutableStateOf(TextFieldValue("• ")) }

    // Guide Me State
    var guideInitialReflection by remember { mutableStateOf("") }
    var guideClarifyingQuestion by remember { mutableStateOf<String?>(null) }
    var guideUserAnswer by remember { mutableStateOf("") }
    var guideTurnCount by remember { mutableIntStateOf(0) }
    var candidatePoints by remember { mutableStateOf<List<CandidatePrayerPoint>>(emptyList()) }
    var canRequestMore by remember { mutableStateOf(true) }
    var isAwaitingApi by remember { mutableStateOf(false) }
    var apiErrorMessage by remember { mutableStateOf<String?>(null) }

    // Helper for Direct Entry Auto-Bullets
    fun handleDirectTextChange(newVal: TextFieldValue) {
        val oldStr = directText.text
        val newStr = newVal.text

        // If newly typed newline, auto-insert bullet
        if (newStr.length > oldStr.length && newStr.endsWith("\n")) {
            val updated = newStr + "• "
            directText = TextFieldValue(updated, androidx.compose.ui.text.TextRange(updated.length))
        } else if (newStr.isEmpty()) {
            directText = TextFieldValue("• ", androidx.compose.ui.text.TextRange(2))
        } else {
            directText = newVal
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = when {
                    selectedEntity != null -> "Praying for ${selectedEntity!!.displayName}"
                    else -> "Who are you praying for?"
                },
                style = typography.petitionTitle,
                color = colors.textPrimary
            )
            Text(
                text = "Cancel",
                style = typography.button,
                color = colors.textSubtle,
                modifier = Modifier.clickable { onBackToHome() }
            )
        }

        // Body Content Based on Current Step
        when (currentStep) {
            LogStep.SELECT_ENTITY -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Contiguous "Add a person or group" tile
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.surface)
                                .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Add a person or group",
                                style = typography.petitionTitle,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            BasicTextField(
                                value = newEntityName,
                                onValueChange = { newEntityName = it },
                                textStyle = typography.petitionBody.copy(color = colors.textPrimary),
                                cursorBrush = SolidColor(colors.textPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                                    .padding(12.dp),
                                decorationBox = { innerTextField ->
                                    if (newEntityName.isEmpty()) {
                                        Text("Name", style = typography.petitionBody, color = colors.textSubtle)
                                    }
                                    innerTextField()
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            width = if (newEntityRoot == RootCode.PEOPLE) 1.5.dp else 0.5.dp,
                                            color = if (newEntityRoot == RootCode.PEOPLE) colors.textPrimary else colors.border,
                                            shape = RectangleShape
                                        )
                                        .clickable { newEntityRoot = RootCode.PEOPLE }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("People", style = typography.button, color = colors.textPrimary)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            width = if (newEntityRoot == RootCode.GROUPS) 1.5.dp else 0.5.dp,
                                            color = if (newEntityRoot == RootCode.GROUPS) colors.textPrimary else colors.border,
                                            shape = RectangleShape
                                        )
                                        .clickable { newEntityRoot = RootCode.GROUPS }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Groups", style = typography.button, color = colors.textPrimary)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (newEntityName.isNotBlank()) colors.textPrimary else colors.border)
                                    .clickable(enabled = newEntityName.isNotBlank()) {
                                        val created = onCreateEntity(newEntityRoot, newEntityName)
                                        selectedEntity = created
                                        currentStep = LogStep.CHOOSE_PATHWAY
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Add & continue",
                                    style = typography.button,
                                    color = if (newEntityName.isNotBlank()) colors.background else colors.textSubtle
                                )
                            }
                        }
                    }

                    // Section: From your journal
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.background)
                                .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text("From your journal", style = typography.caption, color = colors.textSubtle)
                        }
                    }

                    items(allEntities.filter { !it.isPreloadedHistoric }) { entity ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.surface)
                                .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                                .clickable {
                                    selectedEntity = entity
                                    currentStep = LogStep.CHOOSE_PATHWAY
                                }
                                .padding(16.dp)
                        ) {
                            Text(entity.displayName, style = typography.petitionTitle, color = colors.textPrimary)
                        }
                    }
                }
            }

            LogStep.CHOOSE_PATHWAY -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(colors.surface)
                            .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                            .clickable { currentStep = LogStep.DIRECT_ENTRY },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Direct Entry", style = typography.homeAction, color = colors.textPrimary)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(colors.surface)
                            .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                            .clickable { currentStep = LogStep.GUIDE_OPEN_HEART },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Guide me", style = typography.homeAction, color = colors.textPrimary)
                    }
                }
            }

            LogStep.DIRECT_ENTRY -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Writing Pad with auto-bullet points
                    BasicTextField(
                        value = directText,
                        onValueChange = { handleDirectTextChange(it) },
                        textStyle = typography.petitionBody.copy(color = colors.textPrimary),
                        cursorBrush = SolidColor(colors.textPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(20.dp)
                    )

                    // Action: Save to [Name]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surface)
                            .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                            .clickable {
                                val text = directText.text.trim()
                                if (text.isNotBlank() && selectedEntity != null) {
                                    val fallbackTitle = apiClient.generateOfflineFallbackTitle(text)
                                    onSavePetition(selectedEntity!!.id, text, fallbackTitle)
                                    onBackToHome()
                                }
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Save to ${selectedEntity?.displayName ?: "Journal"}",
                            style = typography.button,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            LogStep.GUIDE_OPEN_HEART -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Text("What is on your heart?", style = typography.petitionTitle, color = colors.textPrimary)
                    }
                    BasicTextField(
                        value = guideInitialReflection,
                        onValueChange = { guideInitialReflection = it },
                        textStyle = typography.petitionBody.copy(color = colors.textPrimary),
                        cursorBrush = SolidColor(colors.textPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 20.dp)
                    )
                    if (apiErrorMessage != null) {
                        Text(
                            text = apiErrorMessage!!,
                            style = typography.caption,
                            color = colors.textSubtle,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surface)
                            .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                            .clickable(enabled = !isAwaitingApi && guideInitialReflection.isNotBlank()) {
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
                                                currentStep = LogStep.GUIDE_CLARIFYING
                                            } else {
                                                candidatePoints = resp.candidatePrayerPoints
                                                currentStep = LogStep.GUIDE_CANDIDATES
                                            }
                                        },
                                        onFailure = { err ->
                                            apiErrorMessage = "Assistant unavailable. You can record directly."
                                        }
                                    )
                                }
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAwaitingApi) "Attuning..." else "Continue",
                            style = typography.button,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            LogStep.GUIDE_CLARIFYING -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text("Clarifying question", style = typography.caption, color = colors.textSubtle)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = guideClarifyingQuestion ?: "",
                            style = typography.petitionTitle,
                            color = colors.textPrimary
                        )
                    }
                    BasicTextField(
                        value = guideUserAnswer,
                        onValueChange = { guideUserAnswer = it },
                        textStyle = typography.petitionBody.copy(color = colors.textPrimary),
                        cursorBrush = SolidColor(colors.textPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 20.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(colors.surface)
                                .clickable {
                                    // Unconditional Skip to candidate petitions
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
                                                currentStep = LogStep.GUIDE_CANDIDATES
                                            },
                                            onFailure = {
                                                apiErrorMessage = "Assistant unavailable."
                                            }
                                        )
                                    }
                                }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Skip to petitions", style = typography.button, color = colors.textSubtle)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(colors.surface)
                                .clickable(enabled = !isAwaitingApi && guideUserAnswer.isNotBlank()) {
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
                                                currentStep = LogStep.GUIDE_CANDIDATES
                                            },
                                            onFailure = {
                                                apiErrorMessage = "Assistant unavailable."
                                            }
                                        )
                                    }
                                }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isAwaitingApi) "Attuning..." else "Continue",
                                style = typography.button,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }

            LogStep.GUIDE_CANDIDATES -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Text("Review petitions", style = typography.topicTitle, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(candidatePoints) { card ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.surface)
                                    .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                                    .padding(16.dp)
                            ) {
                                Text(card.title, style = typography.petitionTitle, color = colors.textPrimary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(card.description, style = typography.petitionBody, color = colors.textPrimary)
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(colors.textPrimary)
                                        .clickable {
                                            selectedEntity?.let { entity ->
                                                onSavePetition(entity.id, card.description, card.title)
                                                onBackToHome()
                                            }
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Save to ${selectedEntity?.displayName ?: "Journal"}",
                                        style = typography.button,
                                        color = colors.background
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    if (canRequestMore) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                                .clickable {
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
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Suggest 2 more", style = typography.button, color = colors.textPrimary)
                        }
                    }
                }
            }
        }
    }
}
