package club.hiraeth.flareenough.ui.medication

import club.hiraeth.flareenough.data.db.entity.MedicationEntity
import club.hiraeth.flareenough.data.db.entity.MedicationForm
import club.hiraeth.flareenough.data.db.entity.ScheduleTimeEntity
import club.hiraeth.flareenough.data.db.entity.ScheduleType
import club.hiraeth.flareenough.data.db.relation.MedicationWithTimes

/**
 * The editable state of the medication add or edit screen. Kept separate from the
 * database entity so the form can hold half finished input without touching stored
 * data. Times are minutes past midnight. Days of week are a set of 0 (Monday) to
 * 6 (Sunday).
 */
data class MedicationFormState(
    val id: Long = 0,
    val name: String = "",
    val strength: String = "",
    val form: MedicationForm = MedicationForm.TABLET,
    val colourTag: String = "",
    val shapeTag: String = "",
    val note: String = "",
    val scheduleType: ScheduleType = ScheduleType.EVERY_DAY,
    val times: List<Int> = listOf(8 * 60),
    val daysOfWeek: Set<Int> = setOf(0, 1, 2, 3, 4, 5, 6),
    val intervalDays: Int = 2,
    val cycleDaysOn: Int = 21,
    val cycleDaysOff: Int = 7,
    val asNeededMinGapHours: Int? = null,
    val asNeededDailyMax: Int? = null,
    val stockEnabled: Boolean = false,
    val stockCount: Int = 0,
    val refillThreshold: Int? = null,
    val startEpochDay: Long? = null,
    val endEpochDay: Long? = null,
) {
    val isNew: Boolean get() = id == 0L

    /** Whether the form has the minimum needed to save. */
    val canSave: Boolean get() = name.isNotBlank() && hasValidScheduleTimes

    private val hasValidScheduleTimes: Boolean
        get() = when (scheduleType) {
            ScheduleType.AS_NEEDED -> true
            ScheduleType.DAYS_OF_WEEK, ScheduleType.WEEKLY ->
                times.isNotEmpty() && daysOfWeek.isNotEmpty()
            else -> times.isNotEmpty()
        }

    companion object {
        fun fromEntity(data: MedicationWithTimes): MedicationFormState {
            val m = data.medication
            return MedicationFormState(
                id = m.id,
                name = m.name,
                strength = m.strength.orEmpty(),
                form = m.form,
                colourTag = m.colourTag.orEmpty(),
                shapeTag = m.shapeTag.orEmpty(),
                note = m.note.orEmpty(),
                scheduleType = m.scheduleType,
                times = data.times.sortedBy { it.minutesPastMidnight }.map { it.minutesPastMidnight }
                    .ifEmpty { listOf(8 * 60) },
                daysOfWeek = maskToDays(m.daysOfWeekMask) ?: setOf(0, 1, 2, 3, 4, 5, 6),
                intervalDays = m.intervalDays ?: 2,
                cycleDaysOn = m.cycleDaysOn ?: 21,
                cycleDaysOff = m.cycleDaysOff ?: 7,
                asNeededMinGapHours = m.asNeededMinGapMinutes?.let { it / 60 },
                asNeededDailyMax = m.asNeededDailyMax,
                stockEnabled = m.stockCount != null,
                stockCount = m.stockCount ?: 0,
                refillThreshold = m.refillThreshold,
                startEpochDay = m.startEpochDay,
                endEpochDay = m.endEpochDay,
            )
        }

        fun daysToMask(days: Set<Int>): Int = days.fold(0) { acc, d -> acc or (1 shl d) }

        fun maskToDays(mask: Int?): Set<Int>? =
            mask?.let { m -> (0..6).filter { (m and (1 shl it)) != 0 }.toSet() }
    }
}

/**
 * Build the entity and its times from the form. [todayEpochDay] anchors interval
 * and cycle schedules when no start date is set, and [nowMillis] stamps creation.
 */
fun MedicationFormState.toEntityAndTimes(
    todayEpochDay: Long,
    nowMillis: Long,
    createdAtMillis: Long,
    sortOrder: Int,
): Pair<MedicationEntity, List<ScheduleTimeEntity>> {
    val usesDays = scheduleType == ScheduleType.DAYS_OF_WEEK || scheduleType == ScheduleType.WEEKLY
    val usesInterval = scheduleType == ScheduleType.EVERY_N_DAYS
    val usesCycle = scheduleType == ScheduleType.CYCLE
    val asNeeded = scheduleType == ScheduleType.AS_NEEDED

    val entity = MedicationEntity(
        id = id,
        name = name.trim(),
        strength = strength.trim().ifBlank { null },
        form = form,
        colourTag = colourTag.trim().ifBlank { null },
        shapeTag = shapeTag.trim().ifBlank { null },
        note = note.trim().ifBlank { null },
        scheduleType = scheduleType,
        daysOfWeekMask = if (usesDays) MedicationFormState.daysToMask(daysOfWeek) else null,
        intervalDays = if (usesInterval) intervalDays else null,
        cycleDaysOn = if (usesCycle) cycleDaysOn else null,
        cycleDaysOff = if (usesCycle) cycleDaysOff else null,
        anchorEpochDay = if (usesInterval || usesCycle) (startEpochDay ?: todayEpochDay) else null,
        startEpochDay = startEpochDay,
        endEpochDay = endEpochDay,
        asNeededMinGapMinutes = if (asNeeded) asNeededMinGapHours?.let { it * 60 } else null,
        asNeededDailyMax = if (asNeeded) asNeededDailyMax else null,
        stockCount = if (stockEnabled) stockCount else null,
        refillThreshold = if (stockEnabled) refillThreshold else null,
        sortOrder = sortOrder,
        createdAtMillis = createdAtMillis,
    )

    val times = if (asNeeded) {
        emptyList()
    } else {
        times.sorted().mapIndexed { index, minutes ->
            ScheduleTimeEntity(medicationId = id, minutesPastMidnight = minutes, sortOrder = index)
        }
    }
    return entity to times
}
