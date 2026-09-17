package club.hiraeth.flareenough.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single logged dose. One row per taken, skipped, or missed event.
 *
 * [scheduledTimeMillis] is the planned instant this event answers, used to line an
 * event up with a planned slot for adherence. It is null for as needed doses, which
 * have no planned time. [actualTimeMillis] is when the dose was actually taken,
 * which can differ from the planned time (for example "taken earlier").
 */
@Entity(
    tableName = "dose_events",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("medicationId"), Index("scheduledTimeMillis"), Index("actualTimeMillis")],
)
data class DoseEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val scheduledTimeMillis: Long? = null,
    val status: DoseStatus,
    val actualTimeMillis: Long? = null,
    val loggedVia: LoggedVia = LoggedVia.APP,
    val createdAtMillis: Long,
)
