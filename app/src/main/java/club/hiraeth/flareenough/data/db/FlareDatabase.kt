package club.hiraeth.flareenough.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import club.hiraeth.flareenough.data.db.dao.DayDao
import club.hiraeth.flareenough.data.db.dao.DoseDao
import club.hiraeth.flareenough.data.db.dao.MedicationDao
import club.hiraeth.flareenough.data.db.dao.MeditationDao
import club.hiraeth.flareenough.data.db.dao.StillnessContentDao
import club.hiraeth.flareenough.data.db.dao.SymptomDao
import club.hiraeth.flareenough.data.db.entity.BodyMapEntryEntity
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
    ],
    version = 3,
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

    companion object {
        const val VERSION = 3
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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
    }
}
