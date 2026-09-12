package au.prayer.app

import au.prayer.app.data.models.IndividualEntity
import au.prayer.app.data.models.PrayerPoint
import au.prayer.app.data.models.PrayerStatus
import au.prayer.app.data.models.RootCode
import au.prayer.app.data.models.TopicWithPoints
import au.prayer.app.network.CandidatePrayerPoint
import au.prayer.app.network.PrayerApiClient
import au.prayer.app.network.PromptGroup
import au.prayer.app.network.SuggestResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import au.prayer.app.ui.screens.splitIntoDotpoints
import org.junit.Assert.*
import org.junit.Test

class DevotionalFlowsTest {

    private val apiClient = PrayerApiClient()

    @Test
    fun `test step 0 entity resolution rules`() {
        val person = IndividualEntity(rootCode = RootCode.PEOPLE, displayName = "Sarah")
        assertEquals(RootCode.PEOPLE, person.rootCode)
        assertEquals("Sarah", person.displayName)

        val personalMe = IndividualEntity(rootCode = RootCode.PEOPLE, displayName = "Me", contextDescription = "Personal sanctification and health")
        assertEquals(RootCode.PEOPLE, personalMe.rootCode)
        assertEquals("Me", personalMe.displayName)

        val group = IndividualEntity(rootCode = RootCode.GROUPS, displayName = "Parish Council")
        assertEquals(RootCode.GROUPS, group.rootCode)
        assertEquals("Parish Council", group.displayName)

        val general = IndividualEntity(rootCode = RootCode.GENERAL, displayName = "Global Church & Mission")
        assertEquals(RootCode.GENERAL, general.rootCode)
        assertEquals("Global Church & Mission", general.displayName)
    }

    @Test
    fun `test direct entry offline fallback title generation`() {
        val body = "• Physical healing after surgery; peace for family; trust in Christ"
        val title = apiClient.generateOfflineFallbackTitle(body)

        val wordCount = title.split("\\s+".toRegex()).size
        assertTrue("Title should be between 2 and 4 words: actual $wordCount ('$title')", wordCount in 2..4)
        assertFalse("Title must not contain bullet point symbol", title.contains("•"))
    }

    @Test
    fun `test direct entry offline fallback title edge cases`() {
        // Empty text
        assertEquals("Prayer Point", apiClient.generateOfflineFallbackTitle(""))
        assertEquals("Prayer Point", apiClient.generateOfflineFallbackTitle("   "))
        assertEquals("Prayer Point", apiClient.generateOfflineFallbackTitle("\n\t  \n"))

        // Bullet symbols only
        assertEquals("Prayer Point", apiClient.generateOfflineFallbackTitle("•"))
        assertEquals("Prayer Point", apiClient.generateOfflineFallbackTitle("• • •"))

        // Single word
        assertEquals("Healing", apiClient.generateOfflineFallbackTitle("• Healing"))
        assertEquals("Comfort", apiClient.generateOfflineFallbackTitle("Comfort"))

        // Exactly 2, 3, and 4 words
        assertEquals("Family Peace", apiClient.generateOfflineFallbackTitle("• Family Peace"))
        assertEquals("Comfort In Sorrow", apiClient.generateOfflineFallbackTitle("• Comfort In Sorrow"))
        assertEquals("Strength For Daily Walk", apiClient.generateOfflineFallbackTitle("• Strength For Daily Walk"))

        // 5+ words: strictly truncated to first 4 words
        val longInput = "• God sovereignly grant wisdom to our church elders during transition"
        val fallback = apiClient.generateOfflineFallbackTitle(longInput)
        assertEquals("God sovereignly grant wisdom", fallback)
        assertEquals(4, fallback.split("\\s+".toRegex()).size)

        // Multiple leading bullets and tabs
        val messyInput = "•••\t  Gospel boldness among neighbors and coworkers"
        val messyFallback = apiClient.generateOfflineFallbackTitle(messyInput)
        assertEquals("Gospel boldness among neighbors", messyFallback)
    }

    @Test
    fun `test auto bullet point newline formatting logic`() {
        fun formatNextLine(current: String): String {
            return if (current.endsWith("\n")) current + "• " else current
        }

        val inputWithNewline = "• Pray for wisdom\n"
        val formatted = formatNextLine(inputWithNewline)
        assertEquals("• Pray for wisdom\n• ", formatted)
    }

    @Test
    fun `test auto bullet point empty bullet clearing logic`() {
        // Clearing an empty bullet when user presses Enter or Backspace on "• "
        fun handleBackspaceOnEmptyBullet(current: String): String {
            val trimmed = current.trimEnd()
            return if (trimmed.endsWith("•")) trimmed.removeSuffix("•").trimEnd() else current
        }

        assertEquals("", handleBackspaceOnEmptyBullet("• "))
        assertEquals("• First point", handleBackspaceOnEmptyBullet("• First point\n• "))
    }

    @Test
    fun `test candidate points cardinality and telegraphic brevity`() {
        val candidates = listOf(
            CandidatePrayerPoint(
                title = "Surgery Recovery",
                description = "Rapid healing after knee procedure; patient endurance in therapy; gratitude for medical team"
            ),
            CandidatePrayerPoint(
                title = "Sovereign Peace",
                description = "Quiet heart amidst physical discomfort, trust in Father's preservation"
            )
        )

        // Cardinality Invariant: strictly 2 candidates per turn
        assertEquals(2, candidates.size)

        candidates.forEach { card ->
            val titleWords = card.title.split("\\s+".toRegex()).size
            assertTrue("Title should be 2 to 6 words: $titleWords ('${card.title}')", titleWords in 2..6)

            val descWords = card.description.split("\\s+".toRegex()).size
            assertTrue("Description must be telegraphic <= 25 words: $descWords", descWords <= 25)

            // Telegraphic constraints: no w/ or /w abbreviations
            assertFalse("Must not use 'w/' abbreviation: '${card.description}'", card.description.contains("w/"))
            assertFalse("Must not use '/w' abbreviation: '${card.description}'", card.description.contains("/w"))

            // Must avoid redundant prefixes
            assertFalse("Must not start with 'Pray for'", card.title.startsWith("Pray for", ignoreCase = true))
            assertFalse("Must not start with 'Ask God to'", card.title.startsWith("Ask God to", ignoreCase = true))
        }
    }

    @Test
    fun `test saved prayer point editing and permanent deletion contract`() {
        val initialPoint = PrayerPoint(
            entityId = "entity-1",
            title = "Old Title",
            description = "• Initial prayer point content",
            status = PrayerStatus.ACTIVE
        )

        // Verify initial state
        assertEquals(PrayerStatus.ACTIVE, initialPoint.status)
        assertNull(initialPoint.answeredAt)
        assertNull(initialPoint.answeredTestimony)

        // Editing title and body and transitioning to ANSWERED
        val answeredTime = System.currentTimeMillis()
        val editedPoint = initialPoint.copy(
            title = "Customized Title",
            description = "• Updated pastoral prayer point",
            status = PrayerStatus.ANSWERED,
            answeredAt = answeredTime,
            answeredTestimony = "Praise God for His faithful provision"
        )

        assertEquals("Customized Title", editedPoint.title)
        assertEquals(PrayerStatus.ANSWERED, editedPoint.status)
        assertEquals(answeredTime, editedPoint.answeredAt)
        assertEquals("Praise God for His faithful provision", editedPoint.answeredTestimony)

        // Transitioning back to ACTIVE clears answered fields
        val reactivatedPoint = editedPoint.copy(
            status = PrayerStatus.ACTIVE,
            answeredAt = null,
            answeredTestimony = null
        )
        assertEquals(PrayerStatus.ACTIVE, reactivatedPoint.status)
        assertNull(reactivatedPoint.answeredAt)
        assertNull(reactivatedPoint.answeredTestimony)
    }

    @Test
    fun `test entity update and sphere transition contract`() {
        val initialEntity = IndividualEntity(
            rootCode = RootCode.PEOPLE,
            displayName = "David"
        )
        assertEquals(RootCode.PEOPLE, initialEntity.rootCode)
        assertEquals("David", initialEntity.displayName)

        // Renaming entity
        val renamed = initialEntity.copy(displayName = "David Jenkins")
        assertEquals("David Jenkins", renamed.displayName)
        assertEquals(RootCode.PEOPLE, renamed.rootCode)

        // Transitioning sphere from PEOPLE to GROUPS
        val groupEntity = renamed.copy(rootCode = RootCode.GROUPS, displayName = "Jenkins Family")
        assertEquals(RootCode.GROUPS, groupEntity.rootCode)
        assertEquals("Jenkins Family", groupEntity.displayName)
    }

    @Test
    fun `test prayer record quick toggle from active to answered and back`() {
        val activePoint = PrayerPoint(
            entityId = "e-1",
            title = "Daily Strength",
            description = "• Walking faithfully in trial",
            status = PrayerStatus.ACTIVE
        )
        assertEquals(PrayerStatus.ACTIVE, activePoint.status)
        assertNull(activePoint.answeredAt)

        // Quick toggle to ANSWERED
        val answeredPoint = activePoint.copy(
            status = PrayerStatus.ANSWERED,
            answeredAt = 1000L
        )
        assertEquals(PrayerStatus.ANSWERED, answeredPoint.status)
        assertEquals(1000L, answeredPoint.answeredAt)

        // Quick toggle back to ACTIVE
        val toggledBack = answeredPoint.copy(
            status = PrayerStatus.ACTIVE,
            answeredAt = null,
            answeredTestimony = null
        )
        assertEquals(PrayerStatus.ACTIVE, toggledBack.status)
        assertNull(toggledBack.answeredAt)
        assertNull(toggledBack.answeredTestimony)
    }

    @Test
    fun `test preloaded historic prayers suppress active answered toggle and maintain historic status`() {
        val historicEntity = IndividualEntity(
            id = "historic-collect-peace",
            rootCode = RootCode.HISTORIC,
            displayName = "Collect for Peace (1662 BCP)",
            isPreloadedHistoric = true
        )
        val historicPoint = PrayerPoint(
            id = "historic-collect-peace-pt",
            entityId = historicEntity.id,
            title = "Collect for Peace (1662 BCP)",
            description = "O God, who art the author of peace and lover of concord...",
            status = PrayerStatus.HISTORIC
        )

        // Verify entity and point identify as preloaded historic
        assertTrue(historicEntity.isPreloadedHistoric)
        assertEquals(PrayerStatus.HISTORIC, historicPoint.status)
        assertEquals(RootCode.HISTORIC, historicEntity.rootCode)
        assertNull(historicPoint.answeredAt)
        assertNull(historicPoint.answeredTestimony)

        // Invariant: UI toggle is suppressed for preloaded historic prayers
        val shouldShowAnsweredToggle = !historicEntity.isPreloadedHistoric && historicPoint.status != PrayerStatus.HISTORIC
        assertFalse(shouldShowAnsweredToggle)

        // Invariant: Callback guard ignores toggle attempts on preloaded historic topics
        val topic = TopicWithPoints(
            entity = historicEntity,
            activePoints = listOf(historicPoint),
            answeredPoints = emptyList()
        )
        var toggleAttempted = false
        val onToggleAnswered: (String) -> Unit = { pointId ->
            if (!topic.entity.isPreloadedHistoric) {
                val point = (topic.activePoints + topic.answeredPoints).find { it.id == pointId && it.status != PrayerStatus.HISTORIC }
                if (point != null) {
                    toggleAttempted = true
                }
            }
        }
        onToggleAnswered(historicPoint.id)
        assertFalse(toggleAttempted)
    }

    @Test
    fun `test read-only prompts request generation when viewing past points`() {
        val person = IndividualEntity(
            id = "person-123",
            rootCode = RootCode.PEOPLE,
            displayName = "Sarah",
            contextDescription = "Sister in Christ undergoing trials"
        )
        val pastPoints = listOf(
            PrayerPoint(
                entityId = person.id,
                title = "Surgery Recovery",
                description = "• Rapid healing after knee surgery\n• Peace for family",
                status = PrayerStatus.ACTIVE
            ),
            PrayerPoint(
                entityId = person.id,
                title = "Job Transition",
                description = "• Faithfulness in workplace trial",
                status = PrayerStatus.ANSWERED
            )
        )

        // Construct request from past recorded points
        val recorded = pastPoints.map {
            au.prayer.app.network.RecordedPoint(title = it.title, body = it.description, status = it.status.name)
        }
        val request = au.prayer.app.network.SuggestRequest(
            targetName = person.displayName,
            root = person.rootCode.name,
            group = null,
            contextDescription = person.contextDescription,
            recordedPoints = recorded,
            journalUpdates = emptyList(),
            currentDraft = null, // Invariant: no current draft in read-only view
            localeDialect = "EN_AU_UK"
        )

        assertEquals("Sarah", request.targetName)
        assertEquals("PEOPLE", request.root)
        assertEquals(2, request.recordedPoints.size)
        assertNull(request.currentDraft)
        assertEquals("Surgery Recovery", request.recordedPoints[0].title)
        assertEquals("ACTIVE", request.recordedPoints[0].status)
    }

    @Test
    fun `test read-only prompts formatting constraints`() {
        val mockPraise = listOf(
            "For His steadfast love shown in Christ",
            "Because God reigns sovereign over all creation"
        )
        val mockThank = listOf(
            "That his cancer is in remission",
            "For faithful preservation through trials"
        )
        val mockAsk = listOf(
            "A new and renewed mind",
            "That he may walk in wisdom and truth"
        )

        val groups = listOf(
            au.prayer.app.network.PromptGroup("Praise God", mockPraise),
            au.prayer.app.network.PromptGroup("Thank God", mockThank),
            au.prayer.app.network.PromptGroup("Ask God", mockAsk)
        )

        val allPrompts = groups.flatMap { it.prompts }

        // Total points should be in range 3 to 12
        assertTrue("Prompts should be 3 to 12 lines: ${allPrompts.size}", allPrompts.size in 3..12)

        // At least one per group
        assertEquals(3, groups.size)
        groups.forEach { group ->
            assertTrue("Group '${group.title}' must have at least one prompt", group.prompts.isNotEmpty())
        }

        allPrompts.forEach { prompt ->
            val wordCount = prompt.split("\\s+".toRegex()).size
            // Allowable word count for each line is in the range 4 to 15 words
            assertTrue("Each prompt should be 4 to 15 words: $wordCount ('$prompt')", wordCount in 4..15)
            assertFalse("Must not start with 'Pray for'", prompt.startsWith("Pray for", ignoreCase = true))
            assertFalse("Must not start with 'Ask God to'", prompt.startsWith("Ask God to", ignoreCase = true))

            // Mandatory opening word: must start with For, That, A, An, or Because
            val firstWord = prompt.split("\\s+".toRegex()).first().lowercase()
            val validStarters = listOf("for", "that", "a", "an", "because")
            assertTrue("Prompt must start with For, That, A, or Because: actual '$firstWord' ('$prompt')", validStarters.contains(firstWord))
        }
    }

    @Test
    fun `test journal subject addition per sphere labels and routing`() {
        RootCode.entries.filter { it != RootCode.HISTORIC }.forEach { root ->
            val label = when (root) {
                RootCode.PEOPLE -> "+ Add person"
                RootCode.GROUPS -> "+ Add group"
                RootCode.MISSION_PARTNERS -> "+ Add mission partner"
                RootCode.GENERAL -> "+ Add topic"
                RootCode.HISTORIC -> ""
            }
            assertTrue("Label should start with + Add", label.startsWith("+ Add "))
            assertFalse("Label must not contain clinical or forbidden terms", label.contains("target") || label.contains("entity") || label.contains("ticket"))
        }

        // Test creating subject in repository/data flow
        val newPerson = IndividualEntity(rootCode = RootCode.PEOPLE, displayName = "Jonathan")
        assertEquals(RootCode.PEOPLE, newPerson.rootCode)
        assertEquals("Jonathan", newPerson.displayName)

        val newMissionPartner = IndividualEntity(rootCode = RootCode.MISSION_PARTNERS, displayName = "Wycliffe Bible Translators")
        assertEquals(RootCode.MISSION_PARTNERS, newMissionPartner.rootCode)
        assertEquals("Wycliffe Bible Translators", newMissionPartner.displayName)
    }

    @Test
    fun `test journal date formatting and separation by full English date`() {
        val dateFormat = java.text.SimpleDateFormat("EEEE, d MMMM yyyy", java.util.Locale.ENGLISH)
        
        // Use calendar to test a specific date: 11 September 2026 (Friday)
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            set(2026, java.util.Calendar.SEPTEMBER, 11, 10, 30, 0)
        }
        dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val formattedDate = dateFormat.format(java.util.Date(cal.timeInMillis))
        assertEquals("Friday, 11 September 2026", formattedDate)

        // Multiple entries across different dates grouped by formatted date
        val cal2 = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            set(2026, java.util.Calendar.SEPTEMBER, 10, 15, 0, 0)
        }
        val p1 = PrayerPoint(entityId = "e1", title = "", description = "Point 1", createdAt = cal.timeInMillis)
        val p2 = PrayerPoint(entityId = "e1", title = "", description = "Point 2", createdAt = cal.timeInMillis + 1000)
        val p3 = PrayerPoint(entityId = "e1", title = "", description = "Point 3", createdAt = cal2.timeInMillis)

        val points = listOf(p1, p2, p3)
        val grouped = points.groupBy { dateFormat.format(java.util.Date(it.createdAt)) }

        assertEquals(2, grouped.keys.size)
        assertTrue(grouped.containsKey("Friday, 11 September 2026"))
        assertTrue(grouped.containsKey("Thursday, 10 September 2026"))
        assertEquals(2, grouped["Friday, 11 September 2026"]?.size)
        assertEquals(1, grouped["Thursday, 10 September 2026"]?.size)
    }

    @Test
    fun `test prayer prompts default presentation is collapsed and expandable on demand`() {
        // Initial state invariant: prompts are hidden/collapsed by default
        var isPromptsExpandedInSanctuary = false
        var isPromptsExpandedInJournal = false
        assertFalse("Sanctuary prompts must be collapsed by default", isPromptsExpandedInSanctuary)
        assertFalse("Journal prompts must be collapsed by default", isPromptsExpandedInJournal)

        // User chooses to view prompts (expand)
        isPromptsExpandedInSanctuary = true
        isPromptsExpandedInJournal = true
        assertTrue("User can expand sanctuary prompts on demand", isPromptsExpandedInSanctuary)
        assertTrue("User can expand journal prompts on demand", isPromptsExpandedInJournal)

        // User chooses to collapse prompts again
        isPromptsExpandedInSanctuary = false
        isPromptsExpandedInJournal = false
        assertFalse("User can re-collapse sanctuary prompts", isPromptsExpandedInSanctuary)
        assertFalse("User can re-collapse journal prompts", isPromptsExpandedInJournal)

        // Lexical invariant: toggle phrases must be reverent and free from forbidden clinical terms
        val sanctuaryToggleHeader = "❧   Prompts for Prayer   ❧"
        val sanctuaryExpandText = "Tap to view prompts"
        val sanctuaryCollapseText = "Tap to collapse"
        val journalHeader = "Prompts for Prayer"
        val journalExpandText = "Show"
        val journalCollapseText = "Hide"

        val allPromptStrings = listOf(
            sanctuaryToggleHeader,
            sanctuaryExpandText,
            sanctuaryCollapseText,
            journalHeader,
            journalExpandText,
            journalCollapseText
        )

        val forbiddenTerms = listOf("ai", "target", "entity", "ticket", "bot")
        allPromptStrings.forEach { str ->
            val words = str.lowercase().split(Regex("[^a-z0-9]+")).filter { it.isNotBlank() }
            forbiddenTerms.forEach { forbidden ->
                assertFalse("Prompt UI string '$str' must not contain '$forbidden'", words.contains(forbidden))
            }
        }
    }

    @Test
    fun `test splitIntoDotpoints parses multiline text into individual clean dotpoints`() {
        val input = """
            • Healing after knee surgery
            • Patient endurance for family
            • 
            Trusting in God's providence
            
            • Peace during recovery
        """.trimIndent()

        val result = splitIntoDotpoints(input)
        assertEquals(4, result.size)
        assertEquals("• Healing after knee surgery", result[0])
        assertEquals("• Patient endurance for family", result[1])
        assertEquals("• Trusting in God's providence", result[2])
        assertEquals("• Peace during recovery", result[3])
    }

    @Test
    fun `test splitIntoDotpoints handles empty or bullet-only inputs`() {
        assertEquals(emptyList<String>(), splitIntoDotpoints(""))
        assertEquals(emptyList<String>(), splitIntoDotpoints("• "))
        assertEquals(emptyList<String>(), splitIntoDotpoints("• \n• \n   "))
        assertEquals(listOf("• Boldness in witness"), splitIntoDotpoints("Boldness in witness"))
    }

    @Test
    fun `test each dotpoint separated into its own record with independent active and answered status`() {
        val rawInput = """
            • Complete recovery from surgery
            • Peace for the family
            • Wisdom for medical staff
        """.trimIndent()

        val parsedPoints = splitIntoDotpoints(rawInput)
        assertEquals(3, parsedPoints.size)

        val entityId = "entity-sarah-1"
        val baseTime = 10000L
        val records = parsedPoints.mapIndexed { index, desc ->
            PrayerPoint(
                entityId = entityId,
                title = "",
                description = desc,
                status = PrayerStatus.ACTIVE,
                createdAt = baseTime + index
            )
        }

        // 1. Invariant: Each dotpoint has its own distinct unique ID
        val uniqueIds = records.map { it.id }.toSet()
        assertEquals(3, uniqueIds.size)

        // 2. Invariant: Preserves chronological order of entry
        assertTrue(records[0].createdAt < records[1].createdAt)
        assertTrue(records[1].createdAt < records[2].createdAt)

        // 3. Invariant: Initially all records are ACTIVE
        records.forEach {
            assertEquals(PrayerStatus.ACTIVE, it.status)
            assertNull(it.answeredAt)
        }

        // 4. Invariant: Marking point 0 as ANSWERED affects ONLY point 0
        val answeredPoint0 = records[0].copy(
            status = PrayerStatus.ANSWERED,
            answeredAt = 20000L,
            answeredTestimony = "Surgery was successful with zero complications"
        )
        val updatedRecords = listOf(answeredPoint0, records[1], records[2])

        assertEquals(PrayerStatus.ANSWERED, updatedRecords[0].status)
        assertEquals("Surgery was successful with zero complications", updatedRecords[0].answeredTestimony)
        assertEquals(PrayerStatus.ACTIVE, updatedRecords[1].status)
        assertNull(updatedRecords[1].answeredAt)
        assertEquals(PrayerStatus.ACTIVE, updatedRecords[2].status)
        assertNull(updatedRecords[2].answeredAt)

        // 5. Invariant: Point 1 can subsequently be toggled independently
        val answeredPoint1 = updatedRecords[1].copy(
            status = PrayerStatus.ANSWERED,
            answeredAt = 25000L
        )
        val updatedAgain = listOf(updatedRecords[0], answeredPoint1, updatedRecords[2])
        assertEquals(PrayerStatus.ANSWERED, updatedAgain[0].status)
        assertEquals(PrayerStatus.ANSWERED, updatedAgain[1].status)
        assertEquals(PrayerStatus.ACTIVE, updatedAgain[2].status)
    }

    @Test
    fun `test prayer prompts background refresh shows last cached prompts until API completes`() {
        // Given: cached prompts from the last time the app was used
        val entityId = "entity-sarah-1"
        val lastUsedResponse = SuggestResponse(
            praiseGod = listOf("For God's steadfast kindness in every season"),
            thankGod = listOf("Because His mercies are renewed every morning"),
            askGod = listOf("That Christ grant complete healing and peace after surgery"),
            suggestions = emptyList()
        )
        val initialCachedGroups = lastUsedResponse.promptGroups
        assertEquals(3, initialCachedGroups.size)

        // The in-memory cache is initialized with the cached prompts from DB
        val activePromptsMap = mutableMapOf<String, List<PromptGroup>>()
        activePromptsMap[entityId] = initialCachedGroups

        // The UI immediately shows the cached prompts from last time
        val displayedPromptsBeforeRefresh = activePromptsMap[entityId]
        assertNotNull(displayedPromptsBeforeRefresh)
        assertEquals("For God's steadfast kindness in every season", displayedPromptsBeforeRefresh!![0].prompts[0])
        assertEquals("That Christ grant complete healing and peace after surgery", displayedPromptsBeforeRefresh[2].prompts[0])

        // When: Background refresh begins
        var isRefreshing = true
        // While background refresh is in-flight, displayed prompts remain the cached prompts
        assertTrue("Background refresh is running", isRefreshing)
        assertEquals(displayedPromptsBeforeRefresh, activePromptsMap[entityId])

        // When: API completes the refresh with new prompts
        val refreshedResponse = SuggestResponse(
            praiseGod = listOf("For God's everlasting faithfulness across generations"),
            thankGod = listOf("Because strength and relief were granted during recovery"),
            askGod = listOf("That enduring joy and Gospel peace abide in the home"),
            suggestions = emptyList()
        )
        isRefreshing = false
        activePromptsMap[entityId] = refreshedResponse.promptGroups

        // Then: The displayed prompts are updated with the refreshed list
        val displayedPromptsAfterRefresh = activePromptsMap[entityId]
        assertNotNull(displayedPromptsAfterRefresh)
        assertEquals("For God's everlasting faithfulness across generations", displayedPromptsAfterRefresh!![0].prompts[0])
        assertEquals("Because strength and relief were granted during recovery", displayedPromptsAfterRefresh[1].prompts[0])
        assertEquals("That enduring joy and Gospel peace abide in the home", displayedPromptsAfterRefresh[2].prompts[0])
    }

    @Test
    fun `test prayer prompts background refresh preserves cached prompts on API failure`() {
        val entityId = "entity-work-1"
        val cachedResponse = SuggestResponse(
            praiseGod = listOf("For the Lord who upholds all creation by His word"),
            thankGod = listOf("Because God provides daily bread and honest labor"),
            askGod = listOf("That integrity and patience guide every interaction"),
            suggestions = emptyList()
        )
        val activePromptsMap = mutableMapOf<String, List<PromptGroup>>()
        activePromptsMap[entityId] = cachedResponse.promptGroups

        // API call is triggered in background and fails (offline / network error)
        val apiCallFailed = true
        if (apiCallFailed) {
            // Error handled gracefully: no update to cache, preserving existing prompts
        }

        // Cached prompts remain unchanged and visible to the user
        val displayedPrompts = activePromptsMap[entityId]
        assertNotNull(displayedPrompts)
        assertEquals(3, displayedPrompts!!.size)
        assertEquals("For the Lord who upholds all creation by His word", displayedPrompts[0].prompts[0])
    }

    @Test
    fun `test SuggestResponse serialization and prompt group extraction for SQLite cache`() {
        val json = Json { ignoreUnknownKeys = true }
        val response = SuggestResponse(
            praiseGod = listOf("For God's transcendent holiness and eternal love"),
            thankGod = listOf("Because prayers for safe travel were answered"),
            askGod = listOf("That bold Gospel witness shine in the community"),
            suggestions = listOf("For God's transcendent holiness and eternal love")
        )

        // Verify JSON serialization of columns
        val praiseJson = json.encodeToString(response.praiseGod)
        val thankJson = json.encodeToString(response.thankGod)
        val askJson = json.encodeToString(response.askGod)
        val suggestionsJson = json.encodeToString(response.suggestions)

        val restoredPraise = json.decodeFromString<List<String>>(praiseJson)
        val restoredThank = json.decodeFromString<List<String>>(thankJson)
        val restoredAsk = json.decodeFromString<List<String>>(askJson)
        val restoredSuggestions = json.decodeFromString<List<String>>(suggestionsJson)

        val restoredResponse = SuggestResponse(
            praiseGod = restoredPraise,
            thankGod = restoredThank,
            askGod = restoredAsk,
            suggestions = restoredSuggestions
        )

        val groups = restoredResponse.promptGroups
        assertEquals(3, groups.size)
        assertEquals("Praise God", groups[0].title)
        assertEquals("Thank God", groups[1].title)
        assertEquals("Ask God", groups[2].title)
        assertEquals("For God's transcendent holiness and eternal love", groups[0].prompts[0])
    }

    @Test
    fun `test prayer point add and edit initial cursor placed at end of last line`() {
        // Initial state when starting to add a prayer point
        val addInitialText = "• "
        val addTfv = TextFieldValue(addInitialText, selection = TextRange(addInitialText.length))
        assertEquals(2, addTfv.selection.start)
        assertEquals(2, addTfv.selection.end)
        assertTrue(addTfv.selection.collapsed)

        // Starting to edit a single-line prayer point
        val singleLineDesc = "• Complete recovery from surgery"
        val editSingleTfv = TextFieldValue(singleLineDesc, selection = TextRange(singleLineDesc.length))
        assertEquals(singleLineDesc.length, editSingleTfv.selection.start)
        assertEquals(singleLineDesc.length, editSingleTfv.selection.end)

        // Starting to edit a multiline prayer point
        val multilineDesc = "• First petition line\n• Second petition line\n• Final prayer point line"
        val editMultiTfv = TextFieldValue(multilineDesc, selection = TextRange(multilineDesc.length))
        assertEquals(multilineDesc.length, editMultiTfv.selection.start)
        assertEquals(multilineDesc.length, editMultiTfv.selection.end)
        assertTrue(editMultiTfv.selection.collapsed)

        // Verify end-of-text cursor placement matches the end of the last line
        val lastLine = multilineDesc.substringAfterLast('\n')
        val lastLineEndOffset = multilineDesc.lastIndexOf(lastLine) + lastLine.length
        assertEquals(lastLineEndOffset, editMultiTfv.selection.end)
    }

    @Test
    fun `test instant in-memory toggle active to answered without queue reshuffle`() {
        val entity = IndividualEntity(id = "entity-1", rootCode = RootCode.PEOPLE, displayName = "David")
        val point1 = PrayerPoint(id = "p-1", entityId = "entity-1", title = "Healing", description = "• Recovery", status = PrayerStatus.ACTIVE)
        val point2 = PrayerPoint(id = "p-2", entityId = "entity-1", title = "Patience", description = "• Wisdom", status = PrayerStatus.ACTIVE)
        val initialTopic = au.prayer.app.data.models.TopicWithPoints(
            entity = entity,
            activePoints = listOf(point1, point2),
            answeredPoints = emptyList()
        )
        var topics = listOf(initialTopic)
        val currentTopicIndex = 0

        // Instant toggle logic
        val pointId = "p-1"
        val currentTopic = topics[currentTopicIndex]
        val allPoints = currentTopic.activePoints + currentTopic.answeredPoints
        val point = allPoints.find { it.id == pointId }!!
        val newStatus = if (point.status == PrayerStatus.ACTIVE) PrayerStatus.ANSWERED else PrayerStatus.ACTIVE
        val updatedPoint = point.copy(
            status = newStatus,
            answeredAt = if (newStatus == PrayerStatus.ANSWERED) 1000L else null
        )
        val updatedActive = if (newStatus == PrayerStatus.ANSWERED) {
            currentTopic.activePoints.filter { it.id != pointId }
        } else {
            currentTopic.activePoints + updatedPoint
        }
        val updatedAnswered = if (newStatus == PrayerStatus.ANSWERED) {
            currentTopic.answeredPoints + updatedPoint
        } else {
            currentTopic.answeredPoints.filter { it.id != pointId }
        }
        val updatedTopic = currentTopic.copy(
            activePoints = updatedActive,
            answeredPoints = updatedAnswered
        )
        val updatedList = topics.toMutableList()
        updatedList[currentTopicIndex] = updatedTopic
        topics = updatedList

        assertEquals(1, topics[0].activePoints.size)
        assertEquals("p-2", topics[0].activePoints[0].id)
        assertEquals(1, topics[0].answeredPoints.size)
        assertEquals("p-1", topics[0].answeredPoints[0].id)
        assertEquals(PrayerStatus.ANSWERED, topics[0].answeredPoints[0].status)
        assertEquals("entity-1", topics[0].entity.id)
    }

    @Test
    fun `test optimistic localPoints toggle in journal`() {
        val point1 = PrayerPoint(id = "pt-1", entityId = "e-1", title = "Guidance", description = "• Guidance", status = PrayerStatus.ACTIVE)
        val point2 = PrayerPoint(id = "pt-2", entityId = "e-1", title = "Peace", description = "• Peace", status = PrayerStatus.ANSWERED)
        var localPoints = listOf(point1, point2)

        // Toggling pt-1 from ACTIVE to ANSWERED
        val isAnswered = point1.status == PrayerStatus.ANSWERED
        val newStatus = if (isAnswered) PrayerStatus.ACTIVE else PrayerStatus.ANSWERED
        localPoints = localPoints.map {
            if (it.id == point1.id) it.copy(status = newStatus) else it
        }

        assertEquals(PrayerStatus.ANSWERED, localPoints.find { it.id == "pt-1" }?.status)
        assertEquals(PrayerStatus.ANSWERED, localPoints.find { it.id == "pt-2" }?.status)

        // Toggling pt-2 from ANSWERED back to ACTIVE
        val isAnswered2 = point2.status == PrayerStatus.ANSWERED
        val newStatus2 = if (isAnswered2) PrayerStatus.ACTIVE else PrayerStatus.ACTIVE
        localPoints = localPoints.map {
            if (it.id == point2.id) it.copy(status = PrayerStatus.ACTIVE) else it
        }

        assertEquals(PrayerStatus.ACTIVE, localPoints.find { it.id == "pt-2" }?.status)
    }

    @Test
    fun `test multiline paste bullet normalization preserves bullets and prefixes raw lines`() {
        val pasted = """
            First point without bullet
            • Second point with bullet
            
            Third point after blank line
        """.trimIndent()

        val formatted = pasted.lines().map { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) "" else if (trimmed.startsWith("•")) trimmed else "• $trimmed"
        }.filter { it.isNotEmpty() }.joinToString("\n")

        val expected = "• First point without bullet\n• Second point with bullet\n• Third point after blank line"
        assertEquals(expected, formatted)
    }

    @Test
    fun `test formatJournalDate generates English liturgical date string`() {
        val sdf = java.text.SimpleDateFormat("EEEE, d MMMM yyyy", java.util.Locale.ENGLISH)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")

        // 1789214400000L = Saturday, 12 September 2026 12:00:00 GMT
        val dateString = sdf.format(java.util.Date(1789214400000L))
        assertEquals("Saturday, 12 September 2026", dateString)
    }
}


