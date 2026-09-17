package club.hiraeth.flareenough.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A medication the person tracks.
 *
 * Schedule parameters are stored as plain columns so they are easy to query and
 * migrate. Which columns matter depends on [scheduleType]:
 * - EVERY_DAY: just the child schedule_times rows.
 * - DAYS_OF_WEEK and WEEKLY: [daysOfWeekMask] plus times.
 * - EVERY_N_DAYS: [intervalDays] and [anchorEpochDay] plus times.
 * - CYCLE: [cycleDaysOn], [cycleDaysOff], [anchorEpochDay] plus times.
 * - AS_NEEDED: no times. [asNeededMinGapMinutes] and [asNeededDailyMax] are the
 *   person's own limits. The app only shows the last dose time and a gentle notice
 *   if those limits would be exceeded. It never suggests doses.
 *
 * Times of day are stored in the child schedule_times table as minutes past local
 * midnight, so a dose set for 08:00 stays at 08:00 through UK clock changes.
 */
@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val strength: String? = null,
    val form: MedicationForm = MedicationForm.TABLET,
    val colourTag: String? = null,
    val shapeTag: String? = null,
    val note: String? = null,
    val scheduleType: ScheduleType = ScheduleType.EVERY_DAY,
    /** Bitmask of days, bit 0 Monday to bit 6 Sunday. Null unless day based. */
    val daysOfWeekMask: Int? = null,
    val intervalDays: Int? = null,
    val cycleDaysOn: Int? = null,
    val cycleDaysOff: Int? = null,
    /** Reference date (epoch day) that interval and cycle schedules count from. */
    val anchorEpochDay: Long? = null,
    val startEpochDay: Long? = null,
    val endEpochDay: Long? = null,
    val paused: Boolean = false,
    val asNeededMinGapMinutes: Int? = null,
    val asNeededDailyMax: Int? = null,
    val stockCount: Int? = null,
    val refillThreshold: Int? = null,
    val sortOrder: Int = 0,
    val createdAtMillis: Long,
)

/**
 * A time of day at which a medication is due. A medication can have several.
 * Stored as minutes past local midnight (0 to 1439).
 */
@Entity(
    tableName = "schedule_times",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("medicationId")],
)
data class ScheduleTimeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val minutesPastMidnight: Int,
    val sortOrder: Int = 0,
)
