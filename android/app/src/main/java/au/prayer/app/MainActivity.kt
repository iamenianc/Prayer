package au.prayer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import au.prayer.app.data.local.PrayerDatabaseHelper
import au.prayer.app.data.local.PrayerRepository
import au.prayer.app.data.models.*
import au.prayer.app.network.PrayerApiClient
import au.prayer.app.ui.components.ClosedFolioShield
import au.prayer.app.ui.navigation.LifoBackStack
import au.prayer.app.ui.screens.*
import au.prayer.app.ui.theme.*
import kotlinx.coroutines.launch

enum class ScreenState {
    HOME,
    SANCTUARY_PRAYER,
    LOG_PRAYER,
    JOURNAL
}

class MainActivity : ComponentActivity() {

    private lateinit var dbHelper: PrayerDatabaseHelper
    private lateinit var repository: PrayerRepository
    private val apiClient = PrayerApiClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        dbHelper = PrayerDatabaseHelper(this)
        repository = PrayerRepository(dbHelper)

        setContent {
            val backStack = remember { LifoBackStack(ScreenState.HOME) }
            val screenState = backStack.current
            var appConfig by remember { mutableStateOf(repository.getConfig()) }
            var allEntities by remember { mutableStateOf(repository.getAllEntities()) }
            var preselectedEntity by remember { mutableStateOf<IndividualEntity?>(null) }
            var journalTargetEntity by remember { mutableStateOf<IndividualEntity?>(null) }
            var journalNavKey by remember { mutableIntStateOf(0) }
            var isInitialColdLaunch by remember { mutableStateOf(true) }

            // Prayer sanctuary session state
            var prayerTopics by remember { mutableStateOf<List<TopicWithPoints>>(emptyList()) }
            var currentTopicIndex by remember { mutableIntStateOf(0) }

            // Global Snackbar Host State for subtle feedback
            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()

            // Dynamic typography and active leather folio theme resolution
            val colors = getFolioColors(appConfig.themeMode, appConfig.highContrastMode)
            val typography = getPrayerTypography(appConfig.textScale)

            // Closed Folio Privacy Shield: concealed when activity is paused / backgrounded
            var isAppBackgrounded by remember { mutableStateOf(false) }
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_PAUSE) {
                        isAppBackgrounded = true
                    } else if (event == Lifecycle.Event.ON_RESUME) {
                        isAppBackgrounded = false
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            fun refreshEntities() {
                allEntities = repository.getAllEntities()
            }

            fun startPrayerSession() {
                prayerTopics = repository.getContemplativeTopics(blendHistoric = appConfig.blendHistoricPrayers)
                currentTopicIndex = 0
                if (prayerTopics.isNotEmpty()) {
                    repository.recordTopicInteraction(prayerTopics[0].entity.id)
                }
                backStack.push(ScreenState.SANCTUARY_PRAYER)
            }

            PrayerTheme(themeMode = appConfig.themeMode, textScale = appConfig.textScale, highContrastMode = appConfig.highContrastMode) {
                if (isAppBackgrounded) {
                    ClosedFolioShield(colors = colors)
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = colors.background,
                        contentColor = colors.textPrimary,
                        snackbarHost = {
                            SnackbarHost(snackbarHostState) { data ->
                                Snackbar(
                                    snackbarData = data,
                                    shape = FlatSquareShape,
                                    containerColor = colors.textPrimary,
                                    contentColor = colors.background
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .background(colors.background)
                        ) {
                            AnimatedContent(
                                targetState = screenState,
                                transitionSpec = {
                                when {
                                    // Entering Sanctuary Prayer from Home: solemn fade and gentle upward settling
                                    targetState == ScreenState.SANCTUARY_PRAYER -> {
                                        (fadeIn(animationSpec = tween(350, easing = FastOutSlowInEasing)) +
                                                slideInVertically(animationSpec = tween(350, easing = FastOutSlowInEasing)) { it / 8 })
                                            .togetherWith(
                                                fadeOut(animationSpec = tween(250, easing = FastOutSlowInEasing))
                                            )
                                    }
                                    // Exiting Sanctuary Prayer to Home: smooth fade out and downward settling
                                    initialState == ScreenState.SANCTUARY_PRAYER -> {
                                        fadeIn(animationSpec = tween(250, easing = FastOutSlowInEasing))
                                            .togetherWith(
                                                fadeOut(animationSpec = tween(250, easing = FastOutSlowInEasing)) +
                                                        slideOutVertically(animationSpec = tween(250, easing = FastOutSlowInEasing)) { it / 8 }
                                            )
                                    }
                                    // Transitioning from Home to Journal or Add Prayer Points: slide in from right with fade
                                    initialState == ScreenState.HOME && (targetState == ScreenState.JOURNAL || targetState == ScreenState.LOG_PRAYER) -> {
                                        (slideInHorizontally(animationSpec = tween(320, easing = FastOutSlowInEasing)) { width -> width } +
                                                fadeIn(animationSpec = tween(300)))
                                            .togetherWith(
                                                slideOutHorizontally(animationSpec = tween(320, easing = FastOutSlowInEasing)) { width -> -width / 3 } +
                                                        fadeOut(animationSpec = tween(200))
                                            )
                                    }
                                    // Returning from Journal or Add Prayer Points to Home: slide in from left with fade
                                    (initialState == ScreenState.JOURNAL || initialState == ScreenState.LOG_PRAYER) && targetState == ScreenState.HOME -> {
                                        (slideInHorizontally(animationSpec = tween(320, easing = FastOutSlowInEasing)) { width -> -width / 3 } +
                                                fadeIn(animationSpec = tween(300)))
                                            .togetherWith(
                                                slideOutHorizontally(animationSpec = tween(320, easing = FastOutSlowInEasing)) { width -> width } +
                                                        fadeOut(animationSpec = tween(200))
                                            )
                                    }
                                    // Transitioning between Journal and Add Prayer Points (sub-stack navigation):
                                    initialState == ScreenState.JOURNAL && targetState == ScreenState.LOG_PRAYER -> {
                                        (slideInHorizontally(animationSpec = tween(320, easing = FastOutSlowInEasing)) { width -> width } +
                                                fadeIn(animationSpec = tween(300)))
                                            .togetherWith(
                                                slideOutHorizontally(animationSpec = tween(320, easing = FastOutSlowInEasing)) { width -> -width / 3 } +
                                                        fadeOut(animationSpec = tween(200))
                                            )
                                    }
                                    initialState == ScreenState.LOG_PRAYER && targetState == ScreenState.JOURNAL -> {
                                        (slideInHorizontally(animationSpec = tween(320, easing = FastOutSlowInEasing)) { width -> -width / 3 } +
                                                fadeIn(animationSpec = tween(300)))
                                            .togetherWith(
                                                slideOutHorizontally(animationSpec = tween(320, easing = FastOutSlowInEasing)) { width -> width } +
                                                        fadeOut(animationSpec = tween(200))
                                            )
                                    }
                                    // Default crossfade
                                    else -> {
                                        fadeIn(animationSpec = tween(250))
                                            .togetherWith(fadeOut(animationSpec = tween(200)))
                                    }
                                }
                            },
                            label = "ScreenStateTransition",
                            modifier = Modifier.fillMaxSize()
                        ) { targetScreen ->
                            when (targetScreen) {
                                ScreenState.HOME -> {
                                    HomeScreen(
                                        colors = colors,
                                        typography = typography,
                                        onStartPraying = { startPrayerSession() },
                                        onOpenJournal = {
                                            journalTargetEntity = null
                                            backStack.push(ScreenState.JOURNAL)
                                        },
                                        onAddPrayerPoints = {
                                            preselectedEntity = null
                                            backStack.push(ScreenState.LOG_PRAYER)
                                        },
                                        isInitialLaunch = isInitialColdLaunch,
                                        onInitialLaunchComplete = { isInitialColdLaunch = false }
                                    )
                                }

                                ScreenState.SANCTUARY_PRAYER -> {
                                    SanctuaryPrayerScreen(
                                        topics = prayerTopics,
                                        currentIndex = currentTopicIndex,
                                        colors = colors,
                                        typography = typography,
                                        apiClient = apiClient,
                                        repository = repository,
                                        onNextTopic = {
                                            if (currentTopicIndex < prayerTopics.size - 1) {
                                                currentTopicIndex += 1
                                                repository.recordTopicInteraction(prayerTopics[currentTopicIndex].entity.id)
                                            } else {
                                                backStack.pop()
                                            }
                                        },
                                        onPrevTopic = {
                                            if (currentTopicIndex > 0) {
                                                currentTopicIndex -= 1
                                            }
                                        },
                                        onExit = { backStack.pop() },
                                        onToggleAnswered = { pointId ->
                                            val currentTopic = prayerTopics.getOrNull(currentTopicIndex)
                                            if (currentTopic != null) {
                                                val allPoints = currentTopic.activePoints + currentTopic.answeredPoints
                                                val point = allPoints.find { it.id == pointId }
                                                if (point != null) {
                                                    val newStatus = if (point.status == PrayerStatus.ACTIVE) PrayerStatus.ANSWERED else PrayerStatus.ACTIVE
                                                    repository.updatePrayerPoint(point.id, point.title, point.description, newStatus, point.answeredTestimony)
                                                    prayerTopics = repository.getContemplativeTopics(blendHistoric = appConfig.blendHistoricPrayers)
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(if (newStatus == PrayerStatus.ANSWERED) "Marked as answered" else "Marked as active")
                                                    }
                                                }
                                            }
                                        },
                                        onTogglePinEntity = { id, isPinned ->
                                            repository.toggleEntityPinned(id, isPinned)
                                            prayerTopics = repository.getContemplativeTopics(blendHistoric = appConfig.blendHistoricPrayers)
                                            refreshEntities()
                                        }
                                    )
                                }

                                ScreenState.LOG_PRAYER -> {
                                    LogPrayerScreen(
                                        initialEntity = preselectedEntity,
                                        allEntities = allEntities,
                                        colors = colors,
                                        typography = typography,
                                        apiClient = apiClient,
                                        repository = repository,
                                        onCreateEntity = { rootCode, name ->
                                            val entity = repository.createEntity(rootCode, name)
                                            refreshEntities()
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Added to ${rootCode.displayTitle}")
                                            }
                                            entity
                                        },
                                        onSavePrayerPoint = { entityId, body, _ ->
                                            val savedPoint = repository.savePrayerPoint(
                                                entityId = entityId,
                                                title = "",
                                                description = body
                                            )
                                            val targetEntity = allEntities.find { it.id == entityId }
                                            val entityName = targetEntity?.displayName ?: "Journal"
                                            refreshEntities()

                                            coroutineScope.launch {
                                                val result = snackbarHostState.showSnackbar(
                                                    message = "Saved to $entityName",
                                                    actionLabel = "Undo",
                                                    duration = SnackbarDuration.Short
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    repository.deletePrayerPoint(savedPoint.id)
                                                    refreshEntities()
                                                }
                                            }
                                        },
                                        onUpdateEntity = { id, name, rootCode ->
                                            repository.updateEntity(id, name, rootCode)
                                            refreshEntities()
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Updated")
                                            }
                                        },
                                        onDeleteEntity = { id ->
                                            repository.deleteEntity(id)
                                            refreshEntities()
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Deleted")
                                            }
                                        },
                                        onSavedEntity = { entity ->
                                            journalTargetEntity = entity
                                            journalNavKey++
                                            if (backStack.items.contains(ScreenState.JOURNAL)) {
                                                backStack.pop()
                                            } else {
                                                backStack.replace(ScreenState.JOURNAL)
                                            }
                                        },
                                        onBackToHome = { backStack.pop() }
                                    )
                                }

                                ScreenState.JOURNAL -> {
                                    JournalScreen(
                                        initialEntity = journalTargetEntity,
                                        navigationKey = journalNavKey,
                                        entities = allEntities,
                                        colors = colors,
                                        typography = typography,
                                        config = appConfig,
                                        apiClient = apiClient,
                                        repository = repository,
                                        onUpdateConfig = { updated ->
                                            appConfig = updated
                                            repository.saveConfig(updated)
                                        },
                                        getPointsForEntity = { entityId ->
                                            repository.getPointsForEntity(entityId)
                                        },
                                        onUpdatePrayerPoint = { id, title, desc, status, testimony ->
                                            repository.updatePrayerPoint(id, title, desc, status, testimony)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Changes saved")
                                            }
                                        },
                                        onDeletePrayerPoint = { id ->
                                            repository.deletePrayerPoint(id)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Prayer point deleted")
                                            }
                                        },
                                        onDeleteEntity = { id ->
                                            repository.deleteEntity(id)
                                            refreshEntities()
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Deleted")
                                            }
                                        },
                                        onUpdateEntity = { id, name, rootCode ->
                                            repository.updateEntity(id, name, rootCode)
                                            refreshEntities()
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Updated")
                                            }
                                        },
                                        onCreateEntity = { rootCode, name ->
                                            val entity = repository.createEntity(rootCode, name)
                                            refreshEntities()
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Added to ${rootCode.displayTitle}")
                                            }
                                            entity
                                        },
                                        onAddForEntity = { entity ->
                                            preselectedEntity = entity
                                            backStack.push(ScreenState.LOG_PRAYER)
                                        },
                                        onTogglePinEntity = { id, isPinned ->
                                            repository.toggleEntityPinned(id, isPinned)
                                            refreshEntities()
                                        },
                                        onBackToHome = { backStack.pop() }
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

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}
