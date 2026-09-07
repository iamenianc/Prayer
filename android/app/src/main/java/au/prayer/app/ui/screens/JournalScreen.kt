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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import au.prayer.app.data.models.*
import au.prayer.app.ui.theme.PrayerColors
import au.prayer.app.ui.theme.PrayerTypography

private enum class JournalView {
    ROOT_SELECTION,
    ENTITY_LIST,
    ENTITY_DETAIL,
    EDIT_PETITION,
    SETTINGS
}

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
    onLogForEntity: (IndividualEntity) -> Unit,
    onBackToHome: () -> Unit
) {
    var currentView by remember { mutableStateOf(JournalView.ROOT_SELECTION) }
    var selectedRoot by remember { mutableStateOf(RootCode.PEOPLE) }
    var selectedEntity by remember { mutableStateOf<IndividualEntity?>(null) }
    var editingPoint by remember { mutableStateOf<PrayerPoint?>(null) }

    // Editor fields
    var editTitle by remember { mutableStateOf("") }
    var editBody by remember { mutableStateOf("") }
    var editStatus by remember { mutableStateOf(PrayerStatus.ACTIVE) }
    var editTestimony by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

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
                text = when (currentView) {
                    JournalView.ROOT_SELECTION -> "Journal"
                    JournalView.ENTITY_LIST -> selectedRoot.displayTitle
                    JournalView.ENTITY_DETAIL -> selectedEntity?.displayName ?: "Petitions"
                    JournalView.EDIT_PETITION -> "Edit Petition"
                    JournalView.SETTINGS -> "Settings"
                },
                style = typography.petitionTitle,
                color = colors.textPrimary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (currentView == JournalView.ROOT_SELECTION) {
                    Text(
                        text = "Settings",
                        style = typography.button,
                        color = colors.textSubtle,
                        modifier = Modifier.clickable { currentView = JournalView.SETTINGS }
                    )
                }
                Text(
                    text = if (currentView == JournalView.ROOT_SELECTION) "Home" else "Back",
                    style = typography.button,
                    color = colors.textSubtle,
                    modifier = Modifier.clickable {
                        when (currentView) {
                            JournalView.ROOT_SELECTION -> onBackToHome()
                            JournalView.ENTITY_LIST -> currentView = JournalView.ROOT_SELECTION
                            JournalView.ENTITY_DETAIL -> currentView = JournalView.ENTITY_LIST
                            JournalView.EDIT_PETITION -> currentView = JournalView.ENTITY_DETAIL
                            JournalView.SETTINGS -> currentView = JournalView.ROOT_SELECTION
                        }
                    }
                )
            }
        }

        // View Router
        when (currentView) {
            JournalView.ROOT_SELECTION -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // People Slab
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(colors.surface)
                            .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                            .clickable {
                                selectedRoot = RootCode.PEOPLE
                                currentView = JournalView.ENTITY_LIST
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("People", style = typography.homeAction, color = colors.textPrimary)
                    }

                    // Groups Slab
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(colors.surface)
                            .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                            .clickable {
                                selectedRoot = RootCode.GROUPS
                                currentView = JournalView.ENTITY_LIST
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Groups", style = typography.homeAction, color = colors.textPrimary)
                    }

                    // General Slab
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(colors.surface)
                            .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                            .clickable {
                                selectedRoot = RootCode.GENERAL
                                currentView = JournalView.ENTITY_LIST
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("General", style = typography.homeAction, color = colors.textPrimary)
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
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No records yet", style = typography.caption, color = colors.textSubtle)
                            }
                        }
                    }
                    items(filteredEntities) { entity ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.surface)
                                .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                                .clickable {
                                    selectedEntity = entity
                                    currentView = JournalView.ENTITY_DETAIL
                                }
                                .padding(18.dp)
                        ) {
                            Text(entity.displayName, style = typography.petitionTitle, color = colors.textPrimary)
                        }
                    }
                }
            }

            JournalView.ENTITY_DETAIL -> {
                selectedEntity?.let { entity ->
                    val points = getPointsForEntity(entity.id)
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Quick Action: Log petition for this entity
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.surface)
                                .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                                .clickable { onLogForEntity(entity) }
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+ Log petition", style = typography.button, color = colors.textPrimary)
                        }

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(points) { point ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(colors.surface)
                                        .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                                        .clickable {
                                            // Click Once to Edit Petition
                                            editingPoint = point
                                            editTitle = point.title
                                            editBody = point.description
                                            editStatus = point.status
                                            editTestimony = point.answeredTestimony ?: ""
                                            showDeleteConfirm = false
                                            currentView = JournalView.EDIT_PETITION
                                        }
                                        .padding(16.dp)
                                ) {
                                    val isAnswered = point.status == PrayerStatus.ANSWERED
                                    Text(
                                        text = point.title,
                                        style = if (isAnswered) typography.petitionTitle.copy(textDecoration = TextDecoration.LineThrough) else typography.petitionTitle,
                                        color = if (isAnswered) colors.answeredText else colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = point.description,
                                        style = if (isAnswered) typography.petitionBody.copy(textDecoration = TextDecoration.LineThrough) else typography.petitionBody,
                                        color = if (isAnswered) colors.answeredText else colors.textPrimary
                                    )
                                    if (isAnswered && !point.answeredTestimony.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Thanksgiving: ${point.answeredTestimony}",
                                            style = typography.caption,
                                            color = colors.textSubtle
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            JournalView.EDIT_PETITION -> {
                editingPoint?.let { point ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text("Title", style = typography.caption, color = colors.textSubtle)
                        BasicTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            textStyle = typography.petitionTitle.copy(color = colors.textPrimary),
                            cursorBrush = SolidColor(colors.textPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                                .padding(10.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Petition", style = typography.caption, color = colors.textSubtle)
                        BasicTextField(
                            value = editBody,
                            onValueChange = { editBody = it },
                            textStyle = typography.petitionBody.copy(color = colors.textPrimary),
                            cursorBrush = SolidColor(colors.textPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                                .padding(10.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Status Toggle: Active vs Answered
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = if (editStatus == PrayerStatus.ACTIVE) 1.5.dp else 0.5.dp,
                                        color = if (editStatus == PrayerStatus.ACTIVE) colors.textPrimary else colors.border,
                                        shape = RectangleShape
                                    )
                                    .clickable { editStatus = PrayerStatus.ACTIVE }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Active", style = typography.button, color = colors.textPrimary)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = if (editStatus == PrayerStatus.ANSWERED) 1.5.dp else 0.5.dp,
                                        color = if (editStatus == PrayerStatus.ANSWERED) colors.textPrimary else colors.border,
                                        shape = RectangleShape
                                    )
                                    .clickable { editStatus = PrayerStatus.ANSWERED }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Answered", style = typography.button, color = colors.textPrimary)
                            }
                        }

                        if (editStatus == PrayerStatus.ANSWERED) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Thanksgiving note", style = typography.caption, color = colors.textSubtle)
                            BasicTextField(
                                value = editTestimony,
                                onValueChange = { editTestimony = it },
                                textStyle = typography.petitionBody.copy(color = colors.textPrimary),
                                cursorBrush = SolidColor(colors.textPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                                    .padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action: Save changes
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.textPrimary)
                                .clickable {
                                    onUpdatePrayerPoint(point.id, editTitle, editBody, editStatus, editTestimony)
                                    currentView = JournalView.ENTITY_DETAIL
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Save changes", style = typography.button, color = colors.background)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Permanent Delete with confirmation
                        if (!showDeleteConfirm) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDeleteConfirm = true }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Delete petition", style = typography.caption, color = colors.textSubtle)
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(width = 0.5.dp, color = colors.border, shape = RectangleShape)
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Delete this petition? This cannot be undone.", style = typography.caption, color = colors.textPrimary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text(
                                        text = "Cancel",
                                        style = typography.button,
                                        color = colors.textSubtle,
                                        modifier = Modifier.clickable { showDeleteConfirm = false }
                                    )
                                    Text(
                                        text = "Delete",
                                        style = typography.button,
                                        color = colors.textPrimary,
                                        modifier = Modifier.clickable {
                                            onDeletePrayerPoint(point.id)
                                            currentView = JournalView.ENTITY_DETAIL
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            JournalView.SETTINGS -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text("Theme", style = typography.caption, color = colors.textSubtle)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = if (config.themeMode == ThemeMode.MORNING_LIGHT) 1.5.dp else 0.5.dp,
                                    color = if (config.themeMode == ThemeMode.MORNING_LIGHT) colors.textPrimary else colors.border,
                                    shape = RectangleShape
                                )
                                .clickable { onUpdateConfig(config.copy(themeMode = ThemeMode.MORNING_LIGHT)) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Morning Light", style = typography.button, color = colors.textPrimary)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = if (config.themeMode == ThemeMode.QUIET_NIGHT) 1.5.dp else 0.5.dp,
                                    color = if (config.themeMode == ThemeMode.QUIET_NIGHT) colors.textPrimary else colors.border,
                                    shape = RectangleShape
                                )
                                .clickable { onUpdateConfig(config.copy(themeMode = ThemeMode.QUIET_NIGHT)) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Quiet Night", style = typography.button, color = colors.textPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Text Size", style = typography.caption, color = colors.textSubtle)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(TextScale.LARGE, TextScale.REGULAR, TextScale.COMPACT).forEach { scale ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = if (config.textScale == scale) 1.5.dp else 0.5.dp,
                                        color = if (config.textScale == scale) colors.textPrimary else colors.border,
                                        shape = RectangleShape
                                    )
                                    .clickable { onUpdateConfig(config.copy(textScale = scale)) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(scale.displayName, style = typography.button, color = colors.textPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Language", style = typography.caption, color = colors.textSubtle)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(LocaleDialect.EN_AU_UK, LocaleDialect.EN_US).forEach { dialect ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = if (config.localeDialect == dialect) 1.5.dp else 0.5.dp,
                                        color = if (config.localeDialect == dialect) colors.textPrimary else colors.border,
                                        shape = RectangleShape
                                    )
                                    .clickable { onUpdateConfig(config.copy(localeDialect = dialect)) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (dialect == LocaleDialect.EN_AU_UK) "AU / UK" else "US", style = typography.button, color = colors.textPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Historic Prayers", style = typography.caption, color = colors.textSubtle)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (config.blendHistoricPrayers) 1.5.dp else 0.5.dp,
                                color = if (config.blendHistoricPrayers) colors.textPrimary else colors.border,
                                shape = RectangleShape
                            )
                            .clickable { onUpdateConfig(config.copy(blendHistoricPrayers = !config.blendHistoricPrayers)) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (config.blendHistoricPrayers) "Blend into daily prayer: ON" else "Blend into daily prayer: OFF",
                            style = typography.button,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}
