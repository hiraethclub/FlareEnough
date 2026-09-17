package club.hiraeth.flareenough.data.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import club.hiraeth.flareenough.data.db.entity.MedicationEntity
import club.hiraeth.flareenough.data.db.entity.ScheduleTimeEntity

/** A medication together with its times of day, loaded in one query. */
data class MedicationWithTimes(
    @Embedded val medication: MedicationEntity,
    @Relation(parentColumn = "id", entityColumn = "medicationId")
    val times: List<ScheduleTimeEntity>,
)
