package au.prayer.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import au.prayer.app.data.models.*

class PrayerDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "prayer_vault.db"
        const val DATABASE_VERSION = 4

        const val TABLE_ENTITIES = "individual_entities"
        const val COL_ENTITY_ID = "id"
        const val COL_ENTITY_ROOT = "root_code"
        const val COL_ENTITY_NAME = "display_name"
        const val COL_ENTITY_CONTEXT = "context_description"
        const val COL_ENTITY_HISTORIC = "is_preloaded_historic"
        const val COL_ENTITY_INTERACTED = "interacted_count"
        const val COL_ENTITY_LAST_INTERACTED = "last_interacted_at"
        const val COL_ENTITY_CREATED = "created_at"
        const val COL_ENTITY_PINNED = "is_pinned"

        const val TABLE_POINTS = "prayer_points"
        const val TABLE_SUGGESTION_CACHE = "suggestion_cache"
        const val COL_CACHE_ENTITY_ID = "entity_id"
        const val COL_CACHE_PRAISE = "praise_god"
        const val COL_CACHE_THANK = "thank_god"
        const val COL_CACHE_ASK = "ask_god"
        const val COL_CACHE_SUGGESTIONS = "suggestions"
        const val COL_CACHE_TIMESTAMP = "timestamp"
        const val COL_POINT_ID = "id"
        const val COL_POINT_ENTITY_ID = "entity_id"
        const val COL_POINT_TITLE = "title"
        const val COL_POINT_DESC = "description"
        const val COL_POINT_STATUS = "status"
        const val COL_POINT_INTERACTED = "interacted_count"
        const val COL_POINT_CREATED = "created_at"
        const val COL_POINT_LAST_INTERACTED = "last_interacted_at"
        const val COL_POINT_ANSWERED = "answered_at"
        const val COL_POINT_TESTIMONY = "answered_testimony"

        const val TABLE_CONFIG = "app_config"
        const val COL_CONFIG_KEY = "key"
        const val COL_CONFIG_VAL = "value"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_ENTITIES (
                $COL_ENTITY_ID TEXT PRIMARY KEY,
                $COL_ENTITY_ROOT TEXT NOT NULL,
                $COL_ENTITY_NAME TEXT NOT NULL,
                $COL_ENTITY_CONTEXT TEXT,
                $COL_ENTITY_HISTORIC INTEGER NOT NULL DEFAULT 0,
                $COL_ENTITY_INTERACTED INTEGER NOT NULL DEFAULT 0,
                $COL_ENTITY_LAST_INTERACTED INTEGER,
                $COL_ENTITY_CREATED INTEGER NOT NULL,
                $COL_ENTITY_PINNED INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_POINTS (
                $COL_POINT_ID TEXT PRIMARY KEY,
                $COL_POINT_ENTITY_ID TEXT NOT NULL,
                $COL_POINT_TITLE TEXT NOT NULL,
                $COL_POINT_DESC TEXT NOT NULL,
                $COL_POINT_STATUS TEXT NOT NULL DEFAULT 'ACTIVE',
                $COL_POINT_INTERACTED INTEGER NOT NULL DEFAULT 0,
                $COL_POINT_CREATED INTEGER NOT NULL,
                $COL_POINT_LAST_INTERACTED INTEGER,
                $COL_POINT_ANSWERED INTEGER,
                $COL_POINT_TESTIMONY TEXT,
                FOREIGN KEY ($COL_POINT_ENTITY_ID) REFERENCES $TABLE_ENTITIES ($COL_ENTITY_ID) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_CONFIG (
                $COL_CONFIG_KEY TEXT PRIMARY KEY,
                $COL_CONFIG_VAL TEXT NOT NULL
            )
        """.trimIndent())

        seedHistoricContent(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_ENTITIES ADD COLUMN $COL_ENTITY_PINNED INTEGER NOT NULL DEFAULT 0")
        }
        if (oldVersion < 3) {
            // Remove legacy single-entity historic container and reseed individual historic topics
            db.delete(TABLE_POINTS, "$COL_POINT_ENTITY_ID = 'historic-reformed-prayers'", null)
            db.delete(TABLE_ENTITIES, "$COL_ENTITY_ID = 'historic-reformed-prayers'", null)
            seedHistoricContent(db)
        }
    }

    private fun seedHistoricContent(db: SQLiteDatabase) {
        for (entity in PreloadedContent.getHistoricEntities()) {
            val entityValues = ContentValues().apply {
                put(COL_ENTITY_ID, entity.id)
                put(COL_ENTITY_ROOT, entity.rootCode.name)
                put(COL_ENTITY_NAME, entity.displayName)
                put(COL_ENTITY_CONTEXT, entity.contextDescription)
                put(COL_ENTITY_HISTORIC, if (entity.isPreloadedHistoric) 1 else 0)
                put(COL_ENTITY_INTERACTED, entity.interactedCount)
                put(COL_ENTITY_LAST_INTERACTED, entity.lastInteractedAt)
                put(COL_ENTITY_CREATED, entity.createdAt)
                put(COL_ENTITY_PINNED, if (entity.isPinned) 1 else 0)
            }
            db.insertWithOnConflict(TABLE_ENTITIES, null, entityValues, SQLiteDatabase.CONFLICT_IGNORE)
        }

        for (point in PreloadedContent.getHistoricPrayerPoints()) {
            val pointValues = ContentValues().apply {
                put(COL_POINT_ID, point.id)
                put(COL_POINT_ENTITY_ID, point.entityId)
                put(COL_POINT_TITLE, point.title)
                put(COL_POINT_DESC, point.description)
                put(COL_POINT_STATUS, point.status.name)
                put(COL_POINT_INTERACTED, point.interactedCount)
                put(COL_POINT_CREATED, point.createdAt)
                put(COL_POINT_LAST_INTERACTED, point.lastInteractedAt)
                put(COL_POINT_ANSWERED, point.answeredAt)
                put(COL_POINT_TESTIMONY, point.answeredTestimony)
            }
            db.insertWithOnConflict(TABLE_POINTS, null, pointValues, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }
}
