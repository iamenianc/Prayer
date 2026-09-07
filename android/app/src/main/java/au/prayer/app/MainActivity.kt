package au.prayer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import au.prayer.app.data.local.PrayerDatabaseHelper
import au.prayer.app.data.local.PrayerRepository
import au.prayer.app.data.models.*
import au.prayer.app.network.PrayerApiClient
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

        dbHelper = PrayerDatabaseHelper(this)
        repository = PrayerRepository(dbHelper)

        setContent {
            var screenState by remember { mutableStateOf(ScreenState.HOME) }
            var appConfig by remember { mutableStateOf(repository.getConfig()) }
            var allEntities by remember { mutableStateOf(repository.getAllEntities()) }
            var preselectedEntity by remember { mutableStateOf<IndividualEntity?>(null) }

            // Prayer sanctuary session state
            var prayerTopics by remember { mutableStateOf<List<TopicWithPoints>>(emptyList()) }
            var currentTopicIndex by remember { mutableIntStateOf(0) }

            // Dynamic theme and typography resolution
            val colors = if (appConfig.themeMode == ThemeMode.QUIET_NIGHT) QuietNightColors else MorningLightColors
            val typography = getPrayerTypography(appConfig.textScale)

            fun refreshEntities() {
                allEntities = repository.getAllEntities()
            }

            fun startPrayerSession() {
                prayerTopics = repository.getContemplativeTopics(blendHistoric = appConfig.blendHistoricPrayers)
                currentTopicIndex = 0
                if (prayerTopics.isNotEmpty()) {
                    repository.recordTopicInteraction(prayerTopics[0].entity.id)
                }
                screenState = ScreenState.SANCTUARY_PRAYER
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
            ) {
                when (screenState) {
                    ScreenState.HOME -> {
                        HomeScreen(
                            colors = colors,
                            typography = typography,
                            onStartPraying = { startPrayerSession() },
                            onOpenJournal = { screenState = ScreenState.JOURNAL },
                            onLogPrayerPoints = {
                                preselectedEntity = null
                                screenState = ScreenState.LOG_PRAYER
                            }
                        )
                    }

                    ScreenState.SANCTUARY_PRAYER -> {
                        SanctuaryPrayerScreen(
                            topics = prayerTopics,
                            currentIndex = currentTopicIndex,
                            colors = colors,
                            typography = typography,
                            onNextTopic = {
                                if (currentTopicIndex < prayerTopics.size - 1) {
                                    currentTopicIndex += 1
                                    repository.recordTopicInteraction(prayerTopics[currentTopicIndex].entity.id)
                                } else {
                                    // Session complete or loop
                                    screenState = ScreenState.HOME
                                }
                            },
                            onPrevTopic = {
                                if (currentTopicIndex > 0) {
                                    currentTopicIndex -= 1
                                }
                            },
                            onExit = { screenState = ScreenState.HOME }
                        )
                    }

                    ScreenState.LOG_PRAYER -> {
                        LogPrayerScreen(
                            initialEntity = preselectedEntity,
                            allEntities = allEntities,
                            colors = colors,
                            typography = typography,
                            apiClient = apiClient,
                            onCreateEntity = { rootCode, name ->
                                val entity = repository.createEntity(rootCode, name)
                                refreshEntities()
                                entity
                            },
                            onSavePetition = { entityId, body, initialTitle ->
                                val savedPoint = repository.savePrayerPoint(
                                    entityId = entityId,
                                    title = initialTitle ?: apiClient.generateOfflineFallbackTitle(body),
                                    description = body
                                )
                                // Asynchronous post-commit background auto-titling
                                lifecycleScope.launch {
                                    val result = apiClient.generateTitle(body)
                                    result.onSuccess { cleanTitle ->
                                        if (cleanTitle.isNotBlank()) {
                                            repository.updatePrayerPointTitle(savedPoint.id, cleanTitle)
                                        }
                                    }
                                }
                                refreshEntities()
                            },
                            onBackToHome = { screenState = ScreenState.HOME }
                        )
                    }

                    ScreenState.JOURNAL -> {
                        JournalScreen(
                            entities = allEntities,
                            colors = colors,
                            typography = typography,
                            config = appConfig,
                            onUpdateConfig = { updated ->
                                appConfig = updated
                                repository.saveConfig(updated)
                            },
                            getPointsForEntity = { entityId ->
                                repository.getPointsForEntity(entityId)
                            },
                            onUpdatePrayerPoint = { id, title, desc, status, testimony ->
                                repository.updatePrayerPoint(id, title, desc, status, testimony)
                            },
                            onDeletePrayerPoint = { id ->
                                repository.deletePrayerPoint(id)
                            },
                            onDeleteEntity = { id ->
                                repository.deleteEntity(id)
                                refreshEntities()
                            },
                            onLogForEntity = { entity ->
                                preselectedEntity = entity
                                screenState = ScreenState.LOG_PRAYER
                            },
                            onBackToHome = { screenState = ScreenState.HOME }
                        )
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
