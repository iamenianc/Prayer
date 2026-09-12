package au.prayer.app.data.local

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import au.prayer.app.data.models.*
import au.prayer.app.data.security.VaultBackupCrypto
import au.prayer.app.network.PromptGroup
import au.prayer.app.network.SuggestResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PrayerRepository(private val dbHelper: PrayerDatabaseHelper) {

    private val db: SQLiteDatabase get() = dbHelper.writableDatabase

    // --- Entity Operations ---

    fun createEntity(rootCode: RootCode, displayName: String, contextDescription: String = ""): IndividualEntity {
        val entity = IndividualEntity(
            rootCode = rootCode,
            displayName = displayName.trim(),
            contextDescription = contextDescription.trim()
        )
        val values = ContentValues().apply {
            put(PrayerDatabaseHelper.COL_ENTITY_ID, entity.id)
            put(PrayerDatabaseHelper.COL_ENTITY_ROOT, entity.rootCode.name)
            put(PrayerDatabaseHelper.COL_ENTITY_NAME, entity.displayName)
            put(PrayerDatabaseHelper.COL_ENTITY_CONTEXT, entity.contextDescription)
            put(PrayerDatabaseHelper.COL_ENTITY_HISTORIC, 0)
            put(PrayerDatabaseHelper.COL_ENTITY_INTERACTED, 0)
            put(PrayerDatabaseHelper.COL_ENTITY_LAST_INTERACTED, null as Long?)
            put(PrayerDatabaseHelper.COL_ENTITY_CREATED, entity.createdAt)
            put(PrayerDatabaseHelper.COL_ENTITY_PINNED, if (entity.isPinned) 1 else 0)
        }
        db.insert(PrayerDatabaseHelper.TABLE_ENTITIES, null, values)
        return entity
    }

    fun getAllEntities(): List<IndividualEntity> {
        val list = mutableListOf<IndividualEntity>()
        val cursor = db.query(
            PrayerDatabaseHelper.TABLE_ENTITIES,
            null,
            null,
            null,
            null,
            null,
            "${PrayerDatabaseHelper.COL_ENTITY_NAME} COLLATE NOCASE ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToEntity(it))
            }
        }
        return list
    }

    fun getEntitiesByRoot(rootCode: RootCode): List<IndividualEntity> {
        val list = mutableListOf<IndividualEntity>()
        val cursor = db.query(
            PrayerDatabaseHelper.TABLE_ENTITIES,
            null,
            "${PrayerDatabaseHelper.COL_ENTITY_ROOT} = ?",
            arrayOf(rootCode.name),
            null,
            null,
            "${PrayerDatabaseHelper.COL_ENTITY_NAME} COLLATE NOCASE ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToEntity(it))
            }
        }
        return list
    }

    fun getEntity(id: String): IndividualEntity? {
        val cursor = db.query(
            PrayerDatabaseHelper.TABLE_ENTITIES,
            null,
            "${PrayerDatabaseHelper.COL_ENTITY_ID} = ?",
            arrayOf(id),
            null,
            null,
            null
        )
        return cursor.use {
            if (it.moveToNext()) cursorToEntity(it) else null
        }
    }

    fun updateEntity(
        id: String,
        displayName: String,
        rootCode: RootCode? = null,
        contextDescription: String? = null
    ) {
        val values = ContentValues().apply {
            put(PrayerDatabaseHelper.COL_ENTITY_NAME, displayName.trim())
            if (rootCode != null) {
                put(PrayerDatabaseHelper.COL_ENTITY_ROOT, rootCode.name)
            }
            if (contextDescription != null) {
                put(PrayerDatabaseHelper.COL_ENTITY_CONTEXT, contextDescription.trim())
            }
        }
        db.update(
            PrayerDatabaseHelper.TABLE_ENTITIES,
            values,
            "${PrayerDatabaseHelper.COL_ENTITY_ID} = ?",
            arrayOf(id)
        )
    }

    fun toggleEntityPinned(id: String, isPinned: Boolean) {
        val values = ContentValues().apply {
            put(PrayerDatabaseHelper.COL_ENTITY_PINNED, if (isPinned) 1 else 0)
        }
        db.update(
            PrayerDatabaseHelper.TABLE_ENTITIES,
            values,
            "${PrayerDatabaseHelper.COL_ENTITY_ID} = ?",
            arrayOf(id)
        )
    }

    fun deleteEntity(id: String) {
        db.delete(
            PrayerDatabaseHelper.TABLE_POINTS,
            "${PrayerDatabaseHelper.COL_POINT_ENTITY_ID} = ?",
            arrayOf(id)
        )
        db.delete(
            PrayerDatabaseHelper.TABLE_SUGGESTION_CACHE,
            "${PrayerDatabaseHelper.COL_CACHE_ENTITY_ID} = ?",
            arrayOf(id)
        )
        db.delete(
            PrayerDatabaseHelper.TABLE_ENTITIES,
            "${PrayerDatabaseHelper.COL_ENTITY_ID} = ?",
            arrayOf(id)
        )
    }

    // --- Prayer Point Operations ---

    fun savePrayerPoint(
        entityId: String,
        title: String,
        description: String,
        status: PrayerStatus = PrayerStatus.ACTIVE
    ): PrayerPoint {
        val point = PrayerPoint(
            entityId = entityId,
            title = title.trim(),
            description = description.trim(),
            status = status
        )
        val values = ContentValues().apply {
            put(PrayerDatabaseHelper.COL_POINT_ID, point.id)
            put(PrayerDatabaseHelper.COL_POINT_ENTITY_ID, point.entityId)
            put(PrayerDatabaseHelper.COL_POINT_TITLE, point.title)
            put(PrayerDatabaseHelper.COL_POINT_DESC, point.description)
            put(PrayerDatabaseHelper.COL_POINT_STATUS, point.status.name)
            put(PrayerDatabaseHelper.COL_POINT_INTERACTED, point.interactedCount)
            put(PrayerDatabaseHelper.COL_POINT_CREATED, point.createdAt)
            put(PrayerDatabaseHelper.COL_POINT_LAST_INTERACTED, null as Long?)
            put(PrayerDatabaseHelper.COL_POINT_ANSWERED, null as Long?)
            put(PrayerDatabaseHelper.COL_POINT_TESTIMONY, null as String?)
        }
        db.insert(PrayerDatabaseHelper.TABLE_POINTS, null, values)
        return point
    }

    fun savePrayerPoints(
        entityId: String,
        points: List<String>,
        status: PrayerStatus = PrayerStatus.ACTIVE
    ): List<PrayerPoint> {
        val baseTime = System.currentTimeMillis()
        return points.mapIndexed { index, desc ->
            val point = PrayerPoint(
                entityId = entityId,
                title = "",
                description = desc.trim(),
                status = status,
                createdAt = baseTime + index
            )
            val values = ContentValues().apply {
                put(PrayerDatabaseHelper.COL_POINT_ID, point.id)
                put(PrayerDatabaseHelper.COL_POINT_ENTITY_ID, point.entityId)
                put(PrayerDatabaseHelper.COL_POINT_TITLE, point.title)
                put(PrayerDatabaseHelper.COL_POINT_DESC, point.description)
                put(PrayerDatabaseHelper.COL_POINT_STATUS, point.status.name)
                put(PrayerDatabaseHelper.COL_POINT_INTERACTED, point.interactedCount)
                put(PrayerDatabaseHelper.COL_POINT_CREATED, point.createdAt)
                put(PrayerDatabaseHelper.COL_POINT_LAST_INTERACTED, null as Long?)
                put(PrayerDatabaseHelper.COL_POINT_ANSWERED, null as Long?)
                put(PrayerDatabaseHelper.COL_POINT_TESTIMONY, null as String?)
            }
            db.insert(PrayerDatabaseHelper.TABLE_POINTS, null, values)
            point
        }
    }

    fun updatePrayerPoint(
        id: String,
        title: String,
        description: String,
        status: PrayerStatus,
        answeredTestimony: String? = null
    ) {
        val values = ContentValues().apply {
            put(PrayerDatabaseHelper.COL_POINT_TITLE, title.trim())
            put(PrayerDatabaseHelper.COL_POINT_DESC, description.trim())
            put(PrayerDatabaseHelper.COL_POINT_STATUS, status.name)
            if (status == PrayerStatus.ANSWERED) {
                put(PrayerDatabaseHelper.COL_POINT_ANSWERED, System.currentTimeMillis())
                put(PrayerDatabaseHelper.COL_POINT_TESTIMONY, answeredTestimony?.trim())
            } else {
                putNull(PrayerDatabaseHelper.COL_POINT_ANSWERED)
                putNull(PrayerDatabaseHelper.COL_POINT_TESTIMONY)
            }
        }
        db.update(
            PrayerDatabaseHelper.TABLE_POINTS,
            values,
            "${PrayerDatabaseHelper.COL_POINT_ID} = ?",
            arrayOf(id)
        )
    }

    fun updatePrayerPointTitle(id: String, newTitle: String) {
        val values = ContentValues().apply {
            put(PrayerDatabaseHelper.COL_POINT_TITLE, newTitle.trim())
        }
        db.update(
            PrayerDatabaseHelper.TABLE_POINTS,
            values,
            "${PrayerDatabaseHelper.COL_POINT_ID} = ?",
            arrayOf(id)
        )
    }

    fun deletePrayerPoint(id: String) {
        db.delete(
            PrayerDatabaseHelper.TABLE_POINTS,
            "${PrayerDatabaseHelper.COL_POINT_ID} = ?",
            arrayOf(id)
        )
    }

    fun getPointsForEntity(entityId: String): List<PrayerPoint> {
        val list = mutableListOf<PrayerPoint>()
        val cursor = db.query(
            PrayerDatabaseHelper.TABLE_POINTS,
            null,
            "${PrayerDatabaseHelper.COL_POINT_ENTITY_ID} = ?",
            arrayOf(entityId),
            null,
            null,
            "${PrayerDatabaseHelper.COL_POINT_CREATED} ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToPoint(it))
            }
        }
        return list
    }

    data class TargetContextData(
        val entity: IndividualEntity?,
        val activePoints: List<PrayerPoint>,
        val answeredPoints: List<PrayerPoint>
    )

    fun getTargetContext(entityId: String): TargetContextData {
        val entity = getEntity(entityId)
        val allPoints = getPointsForEntity(entityId)
        val active = allPoints.filter { it.status == PrayerStatus.ACTIVE || it.status == PrayerStatus.HISTORIC }
        val answered = allPoints.filter { it.status == PrayerStatus.ANSWERED }
        return TargetContextData(entity, active, answered)
    }

    // --- Notes Operations ---

    fun getNotesEntities(): List<IndividualEntity> {
        val list = mutableListOf<IndividualEntity>()
        val cursor = db.query(
            PrayerDatabaseHelper.TABLE_ENTITIES,
            null,
            "${PrayerDatabaseHelper.COL_ENTITY_ROOT} = ?",
            arrayOf(RootCode.NOTES.name),
            null,
            null,
            "${PrayerDatabaseHelper.COL_ENTITY_PINNED} DESC, ${PrayerDatabaseHelper.COL_ENTITY_CREATED} DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToEntity(it))
            }
        }
        return list
    }

    fun getOrCreateTodayNoteEntity(formattedDate: String): IndividualEntity {
        val cursor = db.query(
            PrayerDatabaseHelper.TABLE_ENTITIES,
            null,
            "${PrayerDatabaseHelper.COL_ENTITY_ROOT} = ? AND ${PrayerDatabaseHelper.COL_ENTITY_NAME} = ?",
            arrayOf(RootCode.NOTES.name, formattedDate),
            null,
            null,
            null
        )
        val existing = cursor.use {
            if (it.moveToNext()) cursorToEntity(it) else null
        }
        if (existing != null) {
            return existing
        }
        return createEntity(
            rootCode = RootCode.NOTES,
            displayName = formattedDate,
            contextDescription = ""
        )
    }

    fun getNotesForDateEntity(entityId: String): List<PrayerPoint> {
        return getPointsForEntity(entityId)
    }

    fun getNote(noteId: String): PrayerPoint? {
        val cursor = db.query(
            PrayerDatabaseHelper.TABLE_POINTS,
            null,
            "${PrayerDatabaseHelper.COL_POINT_ID} = ?",
            arrayOf(noteId),
            null,
            null,
            null
        )
        return cursor.use {
            if (it.moveToNext()) cursorToPoint(it) else null
        }
    }

    fun createNote(entityId: String, title: String = "", description: String): PrayerPoint {
        return savePrayerPoint(entityId, title, description, PrayerStatus.ACTIVE)
    }

    fun updateNote(noteId: String, title: String, description: String): PrayerPoint {
        val existing = getNote(noteId)
        val status = existing?.status ?: PrayerStatus.ACTIVE
        val testimony = existing?.answeredTestimony
        updatePrayerPoint(noteId, title, description, status, testimony)
        return getNote(noteId) ?: PrayerPoint(
            id = noteId,
            entityId = existing?.entityId ?: "",
            title = title,
            description = description,
            status = status
        )
    }

    fun deleteNote(noteId: String) {
        deletePrayerPoint(noteId)
    }

    fun getNoteCountForEntity(entityId: String): Int {
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM ${PrayerDatabaseHelper.TABLE_POINTS} WHERE ${PrayerDatabaseHelper.COL_POINT_ENTITY_ID} = ?",
            arrayOf(entityId)
        )
        return cursor.use {
            if (it.moveToNext()) it.getInt(0) else 0
        }
    }

    fun getNoteText(entityId: String): String {
        val points = getPointsForEntity(entityId)
        return points.joinToString("\n") { it.description }
    }

    fun saveNoteText(entityId: String, text: String): PrayerPoint {
        val points = getPointsForEntity(entityId)
        return if (points.isNotEmpty()) {
            val first = points.first()
            updatePrayerPoint(first.id, first.title, text, first.status, first.answeredTestimony)
            first.copy(description = text)
        } else {
            savePrayerPoint(entityId, "", text, PrayerStatus.ACTIVE)
        }
    }



    fun getHistoricEntities(): List<IndividualEntity> {
        val list = mutableListOf<IndividualEntity>()
        val cursor = db.query(
            PrayerDatabaseHelper.TABLE_ENTITIES,
            null,
            "${PrayerDatabaseHelper.COL_ENTITY_HISTORIC} = 1",
            null,
            null,
            null,
            "${PrayerDatabaseHelper.COL_ENTITY_CREATED} ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToEntity(it))
            }
        }
        return list.ifEmpty { PreloadedContent.getHistoricEntities() }
    }

    // --- Devotional Queue & Anti-Neglect Balancing ---

    fun getContemplativeTopics(blendHistoric: Boolean = false): List<TopicWithPoints> {
        // Query non-historic entities first
        val userEntityQuery = """
            SELECT e.* FROM ${PrayerDatabaseHelper.TABLE_ENTITIES} e
            WHERE e.${PrayerDatabaseHelper.COL_ENTITY_HISTORIC} = 0
            AND e.${PrayerDatabaseHelper.COL_ENTITY_ROOT} != 'NOTES'
            AND EXISTS (
                SELECT 1 FROM ${PrayerDatabaseHelper.TABLE_POINTS} p 
                WHERE p.${PrayerDatabaseHelper.COL_POINT_ENTITY_ID} = e.${PrayerDatabaseHelper.COL_ENTITY_ID} 
                AND p.${PrayerDatabaseHelper.COL_POINT_STATUS} IN ('ACTIVE', 'HISTORIC')
            )
            ORDER BY 
                e.${PrayerDatabaseHelper.COL_ENTITY_PINNED} DESC,
                e.${PrayerDatabaseHelper.COL_ENTITY_LAST_INTERACTED} IS NOT NULL ASC,
                e.${PrayerDatabaseHelper.COL_ENTITY_LAST_INTERACTED} ASC,
                e.${PrayerDatabaseHelper.COL_ENTITY_INTERACTED} ASC,
                RANDOM()
        """.trimIndent()

        val userEntities = mutableListOf<IndividualEntity>()
        val cursor = db.rawQuery(userEntityQuery, null)
        cursor.use {
            while (it.moveToNext()) {
                userEntities.add(cursorToEntity(it))
            }
        }

        // If no user prayers exist, fallback to preloaded historic prayers
        val entitiesToServe = if (userEntities.isEmpty() || blendHistoric) {
            val historicEntities = getHistoricEntities()
            if (userEntities.isEmpty()) {
                historicEntities
            } else {
                (userEntities + historicEntities).sortedWith(
                    compareByDescending<IndividualEntity> { it.isPinned }
                )
            }
        } else {
            userEntities
        }

        return entitiesToServe.map { entity ->
            val points = getPointsForEntity(entity.id)
            val active = points.filter { it.status == PrayerStatus.ACTIVE || it.status == PrayerStatus.HISTORIC }
            val answered = points.filter { it.status == PrayerStatus.ANSWERED }
            TopicWithPoints(entity = entity, activePoints = active, answeredPoints = answered)
        }.filter { it.activePoints.isNotEmpty() }
    }

    fun recordTopicInteraction(entityId: String) {
        val now = System.currentTimeMillis()

        // 1. Increment entity interaction
        db.execSQL("""
            UPDATE ${PrayerDatabaseHelper.TABLE_ENTITIES}
            SET ${PrayerDatabaseHelper.COL_ENTITY_INTERACTED} = ${PrayerDatabaseHelper.COL_ENTITY_INTERACTED} + 1,
                ${PrayerDatabaseHelper.COL_ENTITY_LAST_INTERACTED} = $now
            WHERE ${PrayerDatabaseHelper.COL_ENTITY_ID} = '$entityId'
        """.trimIndent())

        // 2. Increment active points interaction
        db.execSQL("""
            UPDATE ${PrayerDatabaseHelper.TABLE_POINTS}
            SET ${PrayerDatabaseHelper.COL_POINT_INTERACTED} = ${PrayerDatabaseHelper.COL_POINT_INTERACTED} + 1,
                ${PrayerDatabaseHelper.COL_POINT_LAST_INTERACTED} = $now
            WHERE ${PrayerDatabaseHelper.COL_POINT_ENTITY_ID} = '$entityId'
            AND ${PrayerDatabaseHelper.COL_POINT_STATUS} IN ('ACTIVE', 'HISTORIC')
        """.trimIndent())
    }

    // --- App Config Operations ---

    fun getConfig(): AppConfig {
        var locale = LocaleDialect.EN_AU_UK
        var theme = ThemeMode.WARM_VINTAGE_WHITE
        var scale = TextScale.LARGE
        var blendHistoric = false
        var highContrast = false

        val cursor = db.query(PrayerDatabaseHelper.TABLE_CONFIG, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val key = it.getString(it.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_CONFIG_KEY))
                val value = it.getString(it.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_CONFIG_VAL))
                when (key) {
                    "locale_dialect" -> locale = runCatching { LocaleDialect.valueOf(value) }.getOrDefault(LocaleDialect.EN_AU_UK)
                    "theme_mode" -> theme = runCatching { ThemeMode.valueOf(value) }.getOrDefault(ThemeMode.WARM_VINTAGE_WHITE)
                    "text_scale" -> scale = runCatching { TextScale.valueOf(value) }.getOrDefault(TextScale.LARGE)
                    "blend_historic_prayers" -> blendHistoric = value.toBoolean()
                    "high_contrast_mode" -> highContrast = value.toBoolean()
                }
            }
        }
        return AppConfig(locale, theme, scale, blendHistoric, highContrast)
    }

    fun saveConfig(config: AppConfig) {
        saveConfigValue("locale_dialect", config.localeDialect.name)
        saveConfigValue("theme_mode", config.themeMode.name)
        saveConfigValue("text_scale", config.textScale.name)
        saveConfigValue("blend_historic_prayers", config.blendHistoricPrayers.toString())
        saveConfigValue("high_contrast_mode", config.highContrastMode.toString())
    }

    private fun saveConfigValue(key: String, value: String) {
        val values = ContentValues().apply {
            put(PrayerDatabaseHelper.COL_CONFIG_KEY, key)
            put(PrayerDatabaseHelper.COL_CONFIG_VAL, value)
        }
        db.insertWithOnConflict(PrayerDatabaseHelper.TABLE_CONFIG, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getLibraryZoomScale(): Float {
        val cursor = db.query(
            PrayerDatabaseHelper.TABLE_CONFIG,
            arrayOf(PrayerDatabaseHelper.COL_CONFIG_VAL),
            "${PrayerDatabaseHelper.COL_CONFIG_KEY} = ?",
            arrayOf("library_zoom_scale"),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToNext()) {
                val v = it.getString(0)
                return v.toFloatOrNull()?.coerceIn(0.75f, 2.5f) ?: 1.0f
            }
        }
        return 1.0f
    }

    fun saveLibraryZoomScale(scale: Float) {
        val clamped = scale.coerceIn(0.75f, 2.5f)
        saveConfigValue("library_zoom_scale", clamped.toString())
    }

    fun getTextZoomScale(): Float {
        val cursor = db.query(
            PrayerDatabaseHelper.TABLE_CONFIG,
            arrayOf(PrayerDatabaseHelper.COL_CONFIG_VAL),
            "${PrayerDatabaseHelper.COL_CONFIG_KEY} = ?",
            arrayOf("text_zoom_scale"),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToNext()) {
                val v = it.getString(0)
                return v.toFloatOrNull()?.coerceIn(0.75f, 2.5f) ?: 1.0f
            }
        }
        return 1.0f
    }

    fun saveTextZoomScale(scale: Float) {
        val clamped = scale.coerceIn(0.75f, 2.5f)
        saveConfigValue("text_zoom_scale", clamped.toString())
    }

    // --- Library Reading Progress Operations ---

    fun getReadingProgress(volumeId: String): ReadingProgress? {
        val cursor = db.query(
            PrayerDatabaseHelper.TABLE_LIBRARY_PROGRESS,
            null,
            "${PrayerDatabaseHelper.COL_PROGRESS_VOLUME_ID} = ?",
            arrayOf(volumeId),
            null,
            null,
            null
        )
        return cursor.use {
            if (it.moveToNext()) {
                ReadingProgress(
                    volumeId = it.getString(it.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_PROGRESS_VOLUME_ID)),
                    lastSectionNumber = it.getInt(it.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_PROGRESS_LAST_SECTION)),
                    lastScrollOffset = it.getInt(it.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_PROGRESS_LAST_OFFSET)),
                    updatedAt = it.getLong(it.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_PROGRESS_UPDATED))
                )
            } else {
                null
            }
        }
    }

    fun saveReadingProgress(progress: ReadingProgress) {
        val values = ContentValues().apply {
            put(PrayerDatabaseHelper.COL_PROGRESS_VOLUME_ID, progress.volumeId)
            put(PrayerDatabaseHelper.COL_PROGRESS_LAST_SECTION, progress.lastSectionNumber)
            put(PrayerDatabaseHelper.COL_PROGRESS_LAST_OFFSET, progress.lastScrollOffset)
            put(PrayerDatabaseHelper.COL_PROGRESS_UPDATED, progress.updatedAt)
        }
        db.insertWithOnConflict(
            PrayerDatabaseHelper.TABLE_LIBRARY_PROGRESS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    // --- Suggestion Cache Operations ---

    private val json = Json { ignoreUnknownKeys = true }

    fun saveCachedSuggestions(entityId: String, response: SuggestResponse) {
        val values = ContentValues().apply {
            put(PrayerDatabaseHelper.COL_CACHE_ENTITY_ID, entityId)
            put(PrayerDatabaseHelper.COL_CACHE_PRAISE, json.encodeToString(response.praiseGod))
            put(PrayerDatabaseHelper.COL_CACHE_THANK, json.encodeToString(response.thankGod))
            put(PrayerDatabaseHelper.COL_CACHE_ASK, json.encodeToString(response.askGod))
            put(PrayerDatabaseHelper.COL_CACHE_SUGGESTIONS, json.encodeToString(response.suggestions))
            put(PrayerDatabaseHelper.COL_CACHE_TIMESTAMP, System.currentTimeMillis())
        }
        db.insertWithOnConflict(
            PrayerDatabaseHelper.TABLE_SUGGESTION_CACHE,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getCachedSuggestions(entityId: String): List<PromptGroup>? {
        val cursor = db.query(
            PrayerDatabaseHelper.TABLE_SUGGESTION_CACHE,
            null,
            "${PrayerDatabaseHelper.COL_CACHE_ENTITY_ID} = ?",
            arrayOf(entityId),
            null,
            null,
            null
        )
        return cursor.use {
            if (it.moveToNext()) {
                cursorToPromptGroups(it)
            } else {
                null
            }
        }
    }

    fun getAllCachedSuggestions(): Map<String, List<PromptGroup>> {
        val map = mutableMapOf<String, List<PromptGroup>>()
        val cursor = db.query(
            PrayerDatabaseHelper.TABLE_SUGGESTION_CACHE,
            null,
            null,
            null,
            null,
            null,
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                val entityId = it.getString(it.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_CACHE_ENTITY_ID))
                val groups = cursorToPromptGroups(it)
                if (groups.isNotEmpty()) {
                    map[entityId] = groups
                }
            }
        }
        return map
    }

    fun clearCachedSuggestions(entityId: String) {
        db.delete(
            PrayerDatabaseHelper.TABLE_SUGGESTION_CACHE,
            "${PrayerDatabaseHelper.COL_CACHE_ENTITY_ID} = ?",
            arrayOf(entityId)
        )
    }

    // --- Vault Backup & Restore Operations ---

    fun getAllReadingProgress(): List<ReadingProgress> {
        val list = mutableListOf<ReadingProgress>()
        val cursor = db.query(
            PrayerDatabaseHelper.TABLE_LIBRARY_PROGRESS,
            null,
            null,
            null,
            null,
            null,
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    ReadingProgress(
                        volumeId = it.getString(it.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_PROGRESS_VOLUME_ID)),
                        lastSectionNumber = it.getInt(it.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_PROGRESS_LAST_SECTION)),
                        lastScrollOffset = it.getInt(it.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_PROGRESS_LAST_OFFSET)),
                        updatedAt = it.getLong(it.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_PROGRESS_UPDATED))
                    )
                )
            }
        }
        return list
    }

    fun createBackupPayload(): VaultBackupPayload {
        // Query non-historic personal entities
        val entities = mutableListOf<IndividualEntity>()
        val entityCursor = db.query(
            PrayerDatabaseHelper.TABLE_ENTITIES,
            null,
            "${PrayerDatabaseHelper.COL_ENTITY_HISTORIC} = 0",
            null,
            null,
            null,
            "${PrayerDatabaseHelper.COL_ENTITY_CREATED} ASC"
        )
        entityCursor.use {
            while (it.moveToNext()) {
                entities.add(cursorToEntity(it))
            }
        }

        // Query all non-historic prayer points
        val points = mutableListOf<PrayerPoint>()
        val pointCursor = db.query(
            PrayerDatabaseHelper.TABLE_POINTS,
            null,
            "${PrayerDatabaseHelper.COL_POINT_STATUS} != 'HISTORIC'",
            null,
            null,
            null,
            "${PrayerDatabaseHelper.COL_POINT_CREATED} ASC"
        )
        pointCursor.use {
            while (it.moveToNext()) {
                points.add(cursorToPoint(it))
            }
        }

        val readingProgress = getAllReadingProgress()
        val config = getConfig()

        return VaultBackupPayload(
            version = VaultBackupCrypto.CURRENT_VERSION,
            createdAt = System.currentTimeMillis(),
            appVersion = "1.0.0",
            config = config,
            entities = entities,
            prayerPoints = points,
            libraryProgress = readingProgress
        )
    }

    fun restoreBackupPayload(payload: VaultBackupPayload, replaceExisting: Boolean): RestoreSummary {
        db.beginTransaction()
        var entitiesImported = 0
        var pointsImported = 0
        var progressImported = 0

        try {
            if (replaceExisting) {
                // Delete all non-historic prayer points and entities, preserving historic ones
                db.delete(
                    PrayerDatabaseHelper.TABLE_POINTS,
                    "${PrayerDatabaseHelper.COL_POINT_STATUS} != 'HISTORIC'",
                    null
                )
                db.delete(
                    PrayerDatabaseHelper.TABLE_ENTITIES,
                    "${PrayerDatabaseHelper.COL_ENTITY_HISTORIC} = 0",
                    null
                )
            }

            // Restore/merge entities
            for (entity in payload.entities) {
                if (entity.isPreloadedHistoric) continue
                val values = ContentValues().apply {
                    put(PrayerDatabaseHelper.COL_ENTITY_ID, entity.id)
                    put(PrayerDatabaseHelper.COL_ENTITY_ROOT, entity.rootCode.name)
                    put(PrayerDatabaseHelper.COL_ENTITY_NAME, entity.displayName)
                    put(PrayerDatabaseHelper.COL_ENTITY_CONTEXT, entity.contextDescription)
                    put(PrayerDatabaseHelper.COL_ENTITY_HISTORIC, 0)
                    put(PrayerDatabaseHelper.COL_ENTITY_INTERACTED, entity.interactedCount)
                    put(PrayerDatabaseHelper.COL_ENTITY_LAST_INTERACTED, entity.lastInteractedAt)
                    put(PrayerDatabaseHelper.COL_ENTITY_CREATED, entity.createdAt)
                    put(PrayerDatabaseHelper.COL_ENTITY_PINNED, if (entity.isPinned) 1 else 0)
                }
                val rowId = db.insertWithOnConflict(
                    PrayerDatabaseHelper.TABLE_ENTITIES,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
                if (rowId != -1L) entitiesImported++
            }

            // Restore/merge prayer points
            for (point in payload.prayerPoints) {
                if (point.status == PrayerStatus.HISTORIC) continue
                val values = ContentValues().apply {
                    put(PrayerDatabaseHelper.COL_POINT_ID, point.id)
                    put(PrayerDatabaseHelper.COL_POINT_ENTITY_ID, point.entityId)
                    put(PrayerDatabaseHelper.COL_POINT_TITLE, point.title)
                    put(PrayerDatabaseHelper.COL_POINT_DESC, point.description)
                    put(PrayerDatabaseHelper.COL_POINT_STATUS, point.status.name)
                    put(PrayerDatabaseHelper.COL_POINT_INTERACTED, point.interactedCount)
                    put(PrayerDatabaseHelper.COL_POINT_CREATED, point.createdAt)
                    put(PrayerDatabaseHelper.COL_POINT_LAST_INTERACTED, point.lastInteractedAt)
                    put(PrayerDatabaseHelper.COL_POINT_ANSWERED, point.answeredAt)
                    put(PrayerDatabaseHelper.COL_POINT_TESTIMONY, point.answeredTestimony)
                }
                val rowId = db.insertWithOnConflict(
                    PrayerDatabaseHelper.TABLE_POINTS,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
                if (rowId != -1L) pointsImported++
            }

            // Restore library reading progress
            for (progress in payload.libraryProgress) {
                saveReadingProgress(progress)
                progressImported++
            }

            // Restore configuration
            saveConfig(payload.config)

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        return RestoreSummary(
            entitiesImported = entitiesImported,
            pointsImported = pointsImported,
            readingProgressImported = progressImported,
            isReplaced = replaceExisting
        )
    }

    private fun cursorToPromptGroups(c: Cursor): List<PromptGroup> {
        val praiseJson = c.getString(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_CACHE_PRAISE))
        val thankJson = c.getString(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_CACHE_THANK))
        val askJson = c.getString(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_CACHE_ASK))
        val suggestionsJson = c.getString(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_CACHE_SUGGESTIONS))

        val praise = try { json.decodeFromString<List<String>>(praiseJson) } catch (e: Exception) { emptyList() }
        val thank = try { json.decodeFromString<List<String>>(thankJson) } catch (e: Exception) { emptyList() }
        val ask = try { json.decodeFromString<List<String>>(askJson) } catch (e: Exception) { emptyList() }
        val suggestions = try { json.decodeFromString<List<String>>(suggestionsJson) } catch (e: Exception) { emptyList() }

        return SuggestResponse(
            praiseGod = praise,
            thankGod = thank,
            askGod = ask,
            suggestions = suggestions
        ).promptGroups
    }

    // --- Cursor Mappers ---

    private fun cursorToEntity(c: Cursor): IndividualEntity {
        return IndividualEntity(
            id = c.getString(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_ENTITY_ID)),
            rootCode = RootCode.valueOf(c.getString(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_ENTITY_ROOT))),
            displayName = c.getString(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_ENTITY_NAME)),
            contextDescription = c.getString(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_ENTITY_CONTEXT)) ?: "",
            isPreloadedHistoric = c.getInt(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_ENTITY_HISTORIC)) == 1,
            interactedCount = c.getInt(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_ENTITY_INTERACTED)),
            lastInteractedAt = if (c.isNull(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_ENTITY_LAST_INTERACTED))) null else c.getLong(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_ENTITY_LAST_INTERACTED)),
            createdAt = c.getLong(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_ENTITY_CREATED)),
            isPinned = try {
                val pinnedIdx = c.getColumnIndex(PrayerDatabaseHelper.COL_ENTITY_PINNED)
                if (pinnedIdx >= 0 && !c.isNull(pinnedIdx)) c.getInt(pinnedIdx) == 1 else false
            } catch (e: Exception) {
                false
            }
        )
    }

    private fun cursorToPoint(c: Cursor): PrayerPoint {
        return PrayerPoint(
            id = c.getString(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_POINT_ID)),
            entityId = c.getString(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_POINT_ENTITY_ID)),
            title = c.getString(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_POINT_TITLE)),
            description = c.getString(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_POINT_DESC)),
            status = PrayerStatus.valueOf(c.getString(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_POINT_STATUS))),
            interactedCount = c.getInt(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_POINT_INTERACTED)),
            createdAt = c.getLong(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_POINT_CREATED)),
            lastInteractedAt = if (c.isNull(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_POINT_LAST_INTERACTED))) null else c.getLong(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_POINT_LAST_INTERACTED)),
            answeredAt = if (c.isNull(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_POINT_ANSWERED))) null else c.getLong(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_POINT_ANSWERED)),
            answeredTestimony = c.getString(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_POINT_TESTIMONY))
        )
    }
}
