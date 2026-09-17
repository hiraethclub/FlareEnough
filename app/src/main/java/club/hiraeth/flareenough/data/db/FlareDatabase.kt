package club.hiraeth.flareenough.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import club.hiraeth.flareenough.data.db.dao.DayDao
import club.hiraeth.flareenough.data.db.dao.DoseDao
import club.hiraeth.flareenough.data.db.dao.MedicationDao
import club.hiraeth.flareenough.data.db.dao.MeditationDao
import club.hiraeth.flareenough.data.db.dao.SymptomDao
import club.hiraeth.flareenough.data.db.entity.BodyMapEntryEntity
import club.hiraeth.flareenough.data.db.entity.DayNoteEntity
import club.hiraeth.flareenough.data.db.entity.DayTagCrossRef
import club.hiraeth.flareenough.data.db.entity.DoseEventEntity
import club.hiraeth.flareenough.data.db.entity.FlareDayEntity
import club.hiraeth.flareenough.data.db.entity.MedicationEntity
import club.hiraeth.flareenough.data.db.entity.MeditationSessionEntity
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
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class FlareDatabase : RoomDatabase() {

    abstract fun medicationDao(): MedicationDao
    abstract fun doseDao(): DoseDao
    abstract fun symptomDao(): SymptomDao
    abstract fun dayDao(): DayDao
    abstract fun meditationDao(): MeditationDao

    companion object {
        const val VERSION = 1
        const val NAME = "flare_enough.db"

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
                .build()
    }
}
