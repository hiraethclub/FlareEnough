package club.hiraeth.flareenough.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import club.hiraeth.flareenough.data.db.dao.BodyPartDao
import club.hiraeth.flareenough.data.db.dao.DayDao
import club.hiraeth.flareenough.data.db.dao.DoseDao
import club.hiraeth.flareenough.data.db.dao.MedicationDao
import club.hiraeth.flareenough.data.db.dao.MeditationDao
import club.hiraeth.flareenough.data.db.dao.StillnessContentDao
import club.hiraeth.flareenough.data.db.dao.SymptomDao
import club.hiraeth.flareenough.data.db.entity.BodyMapEntryEntity
import club.hiraeth.flareenough.data.db.entity.BodyPartCategoryEntity
import club.hiraeth.flareenough.data.db.entity.BodyPartEntity
import club.hiraeth.flareenough.data.db.entity.DayNoteEntity
import club.hiraeth.flareenough.data.db.entity.DayTagCrossRef
import club.hiraeth.flareenough.data.db.entity.DoseEventEntity
import club.hiraeth.flareenough.data.db.entity.FlareDayEntity
import club.hiraeth.flareenough.data.db.entity.MedicationEntity
import club.hiraeth.flareenough.data.db.entity.MeditationSessionEntity
import club.hiraeth.flareenough.data.db.entity.PeriodDayEntity
import club.hiraeth.flareenough.data.db.entity.PromptEntity
import club.hiraeth.flareenough.data.db.entity.ReadingEntity
import club.hiraeth.flareenough.data.db.entity.ScheduleTimeEntity
import club.hiraeth.flareenough.data.db.entity.SymptomEntryEntity
import club.hiraeth.flareenough.data.db.entity.SymptomTrackerEntity
import club.hiraeth.flareenough.data.db.entity.TagEntity

/**
 * The single Room database for the whole app. All data lives here, on the device.
 *
 * The schema is exported to app/schemas on every build (see the room block in the
 * module build file), so migrations can be written and tested against known
 * schemas. When you change any entity, bump [VERSION] and add a Migration.
 */
@Database(
    entities = [
        MedicationEntity::class,
        ScheduleTimeEntity::class,
        DoseEventEntity::class,
        SymptomTrackerEntity::class,
        SymptomEntryEntity::class,
        BodyMapEntryEntity::class,
        FlareDayEntity::class,
        DayNoteEntity::class,
        TagEntity::class,
        DayTagCrossRef::class,
        MeditationSessionEntity::class,
        PromptEntity::class,
        ReadingEntity::class,
        PeriodDayEntity::class,
        BodyPartCategoryEntity::class,
        BodyPartEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class FlareDatabase : RoomDatabase() {

    abstract fun medicationDao(): MedicationDao
    abstract fun doseDao(): DoseDao
    abstract fun symptomDao(): SymptomDao
    abstract fun dayDao(): DayDao
    abstract fun meditationDao(): MeditationDao
    abstract fun stillnessContentDao(): StillnessContentDao
    abstract fun bodyPartDao(): BodyPartDao

    companion object {
        const val VERSION = 4
        const val NAME = "flare_enough.db"

        /**
         * Version 1 to 2: add the person's own stillness prompts and readings. The
         * table definitions must match exactly what Room expects for these entities.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `stillness_prompts` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`text` TEXT NOT NULL, " +
                        "`createdAtMillis` INTEGER NOT NULL)",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `readings` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`body` TEXT NOT NULL, " +
                        "`sortOrder` INTEGER NOT NULL, " +
                        "`createdAtMillis` INTEGER NOT NULL)",
                )
            }
        }

        /**
         * Version 2 to 3: add the optional period tracker's bleeding days. The table
         * definition must match exactly what Room expects for [PeriodDayEntity].
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `period_days` (" +
                        "`epochDay` INTEGER PRIMARY KEY NOT NULL, " +
                        "`flow` TEXT NOT NULL, " +
                        "`createdAtMillis` INTEGER NOT NULL)",
                )
            }
        }

        /**
         * Version 3 to 4: body parts and their categories become editable. This only
         * adds two tables and seeds them with the defaults; it never touches
         * body_map_entries, whose "region" column already holds the part key, so every
         * mark recorded before this stays exactly as it was.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `body_part_categories` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`key` TEXT NOT NULL, `name` TEXT, " +
                        "`sortOrder` INTEGER NOT NULL, `createdAtMillis` INTEGER NOT NULL)",
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS " +
                        "`index_body_part_categories_key` ON `body_part_categories` (`key`)",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `body_parts` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`categoryId` INTEGER NOT NULL, `key` TEXT NOT NULL, `name` TEXT, " +
                        "`visible` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL, " +
                        "`createdAtMillis` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`categoryId`) REFERENCES `body_part_categories`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE )",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS " +
                        "`index_body_parts_categoryId` ON `body_parts` (`categoryId`)",
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS " +
                        "`index_body_parts_key` ON `body_parts` (`key`)",
                )
                seedBodyParts(db)
            }
        }

        /**
         * Insert the default categories and parts. Names are left null so the app shows
         * the translated default; the keys match the body region names used by marks
         * made before this feature. Used both by the migration (upgrades) and the
         * create callback (fresh installs), so the two never drift apart.
         */
        private fun seedBodyParts(db: SupportSQLiteDatabase) {
            val now = System.currentTimeMillis()
            BodyPartDefaults.categories.forEachIndexed { categoryIndex, category ->
                val categoryId = categoryIndex + 1
                db.execSQL(
                    "INSERT INTO `body_part_categories` " +
                        "(`id`, `key`, `name`, `sortOrder`, `createdAtMillis`) " +
                        "VALUES (?, ?, NULL, ?, ?)",
                    arrayOf<Any>(categoryId, category.key, categoryIndex, now),
                )
                category.partKeys.forEachIndexed { partIndex, partKey ->
                    db.execSQL(
                        "INSERT INTO `body_parts` " +
                            "(`categoryId`, `key`, `name`, `visible`, `sortOrder`, `createdAtMillis`) " +
                            "VALUES (?, ?, NULL, 1, ?, ?)",
                        arrayOf<Any>(categoryId, partKey, partIndex, now),
                    )
                }
            }
        }

        /** Seeds the default body parts when the database is first created (fresh install). */
        private val seedCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                seedBodyParts(db)
            }
        }

        /**
         * Build the database. Foreign key enforcement is turned on so cascades and
         * relationships behave. There is no fallback to destructive migration: data
         * must never be silently dropped, so every version bump needs a real
         * Migration.
         */
        fun build(context: Context): FlareDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                FlareDatabase::class.java,
                NAME,
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .addCallback(seedCallback)
                .build()
    }
}
