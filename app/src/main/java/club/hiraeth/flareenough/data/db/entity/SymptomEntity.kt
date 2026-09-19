package club.hiraeth.flareenough.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A user defined symptom tracker, for example pain or fatigue. All can be renamed,
 * reordered, hidden, or deleted.
 *
 * [levelLabels] holds the five labels for a FIVE_LEVEL tracker (for example
 * None, Mild, Moderate, Bad, Severe). [durationOptions] holds the quick pick bands
 * for a DURATION tracker. [unit] is an optional unit for a NUMBER tracker.
 */
@Entity(tableName = "symptom_trackers")
data class SymptomTrackerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TrackerType = TrackerType.FIVE_LEVEL,
    val levelLabels: List<String>? = null,
    val durationOptions: List<String>? = null,
    val unit: String? = null,
    val hidden: Boolean = false,
    val sortOrder: Int = 0,
    val createdAtMillis: Long,
)

/**
 * One logged value for a tracker on a day.
 *
 * [valueInt] holds the level (1 to 5), a yes or no (1 or 0), a duration band index,
 * or a number. [valueText] holds free text where a type needs it. Kept flexible so
 * one table serves every tracker type.
 */
@Entity(
    tableName = "symptom_entries",
    foreignKeys = [
        ForeignKey(
            entity = SymptomTrackerEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackerId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("trackerId"), Index("epochDay")],
)
data class SymptomEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackerId: Long,
    /** The day this value is for, as a local date epoch day. */
    val epochDay: Long,
    val valueInt: Int? = null,
    val valueText: String? = null,
    val createdAtMillis: Long,
)

/**
 * A marked region on the body map for a day. Tapping cycles sore, then swollen,
 * then clear (clear removes the row).
 */
@Entity(
    tableName = "body_map_entries",
    indices = [Index("epochDay"), Index(value = ["epochDay", "region"], unique = true)],
)
data class BodyMapEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val region: BodyRegion,
    val state: BodyState,
    val createdAtMillis: Long,
)

/** Whether a day was a flare day. Present only for days the person marked. */
@Entity(tableName = "flare_days")
data class FlareDayEntity(
    @PrimaryKey val epochDay: Long,
    val flare: Boolean,
    val createdAtMillis: Long,
)

/**
 * A bleeding day for the optional period tracker, with its flow. A row exists only
 * for days the person marked. There is no row for days without bleeding, and the app
 * never fills days in or predicts them.
 */
@Entity(tableName = "period_days")
data class PeriodDayEntity(
    @PrimaryKey val epochDay: Long,
    val flow: PeriodFlow,
    val createdAtMillis: Long,
)

/** A free text note for a day. Never required. */
@Entity(tableName = "day_notes", indices = [Index("epochDay")])
data class DayNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val text: String,
    val createdAtMillis: Long,
)

/** A user created tag, for example "weather" or "overdid it". */
@Entity(tableName = "tags", indices = [Index(value = ["name"], unique = true)])
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAtMillis: Long,
)

/** Links a tag to a day. Many tags per day, many days per tag. */
@Entity(
    tableName = "day_tags",
    primaryKeys = ["epochDay", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("tagId")],
)
data class DayTagCrossRef(
    val epochDay: Long,
    val tagId: Long,
)
