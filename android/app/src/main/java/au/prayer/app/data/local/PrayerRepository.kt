package au.prayer.app.data.local

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import au.prayer.app.data.models.*

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

    fun deleteEntity(id: String) {
        db.delete(
            PrayerDatabaseHelper.TABLE_POINTS,
            "${PrayerDatabaseHelper.COL_POINT_ENTITY_ID} = ?",
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


    // --- Devotional Queue & Anti-Neglect Balancing ---

    fun getContemplativeTopics(blendHistoric: Boolean = false): List<TopicWithPoints> {
        // Query non-historic entities first
        val userEntityQuery = """
            SELECT e.* FROM ${PrayerDatabaseHelper.TABLE_ENTITIES} e
            WHERE e.${PrayerDatabaseHelper.COL_ENTITY_HISTORIC} = 0
            AND EXISTS (
                SELECT 1 FROM ${PrayerDatabaseHelper.TABLE_POINTS} p 
                WHERE p.${PrayerDatabaseHelper.COL_POINT_ENTITY_ID} = e.${PrayerDatabaseHelper.COL_ENTITY_ID} 
                AND p.${PrayerDatabaseHelper.COL_POINT_STATUS} IN ('ACTIVE', 'HISTORIC')
            )
            ORDER BY 
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
            val historicEntity = getEntity(PreloadedContent.HISTORIC_ENTITY_ID)
            if (userEntities.isEmpty()) {
                listOfNotNull(historicEntity)
            } else {
                userEntities + listOfNotNull(historicEntity)
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
        var theme = ThemeMode.MORNING_LIGHT
        var scale = TextScale.LARGE
        var blendHistoric = false

        val cursor = db.query(PrayerDatabaseHelper.TABLE_CONFIG, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val key = it.getString(it.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_CONFIG_KEY))
                val value = it.getString(it.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_CONFIG_VAL))
                when (key) {
                    "locale_dialect" -> locale = runCatching { LocaleDialect.valueOf(value) }.getOrDefault(LocaleDialect.EN_AU_UK)
                    "theme_mode" -> theme = runCatching { ThemeMode.valueOf(value) }.getOrDefault(ThemeMode.MORNING_LIGHT)
                    "text_scale" -> scale = runCatching { TextScale.valueOf(value) }.getOrDefault(TextScale.LARGE)
                    "blend_historic_prayers" -> blendHistoric = value.toBoolean()
                }
            }
        }
        return AppConfig(locale, theme, scale, blendHistoric)
    }

    fun saveConfig(config: AppConfig) {
        saveConfigValue("locale_dialect", config.localeDialect.name)
        saveConfigValue("theme_mode", config.themeMode.name)
        saveConfigValue("text_scale", config.textScale.name)
        saveConfigValue("blend_historic_prayers", config.blendHistoricPrayers.toString())
    }

    private fun saveConfigValue(key: String, value: String) {
        val values = ContentValues().apply {
            put(PrayerDatabaseHelper.COL_CONFIG_KEY, key)
            put(PrayerDatabaseHelper.COL_CONFIG_VAL, value)
        }
        db.insertWithOnConflict(PrayerDatabaseHelper.TABLE_CONFIG, null, values, SQLiteDatabase.CONFLICT_REPLACE)
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
            createdAt = c.getLong(c.getColumnIndexOrThrow(PrayerDatabaseHelper.COL_ENTITY_CREATED))
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
