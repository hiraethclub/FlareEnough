package club.hiraeth.flareenough.reminders

import club.hiraeth.flareenough.data.db.entity.ScheduleType
import club.hiraeth.flareenough.data.db.relation.MedicationWithTimes
import club.hiraeth.flareenough.schedule.MedicationSchedule
import club.hiraeth.flareenough.schedule.Recurrence
import club.hiraeth.flareenough.schedule.TimeOfDay
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Turn a stored medication and its times into the pure schedule model the engine
 * understands. The bit 0 to Monday convention in storage maps to java time's
 * DayOfWeek here.
 */
fun MedicationWithTimes.toSchedule(): MedicationSchedule {
    val m = medication
    val timeList = times
        .sortedBy { it.minutesPastMidnight }
        .map { TimeOfDay(it.minutesPastMidnight) }

    val anchor = m.anchorEpochDay?.let { LocalDate.ofEpochDay(it) }
        ?: m.startEpochDay?.let { LocalDate.ofEpochDay(it) }
        ?: LocalDate.ofEpochDay(0)

    val recurrence = when (m.scheduleType) {
        ScheduleType.EVERY_DAY -> Recurrence.Daily
        ScheduleType.DAYS_OF_WEEK, ScheduleType.WEEKLY ->
            Recurrence.DaysOfWeek(maskToDayOfWeek(m.daysOfWeekMask))
        ScheduleType.EVERY_N_DAYS ->
            Recurrence.EveryNDays((m.intervalDays ?: 1).coerceAtLeast(1), anchor)
        ScheduleType.CYCLE ->
            Recurrence.Cycle(
                daysOn = (m.cycleDaysOn ?: 1).coerceAtLeast(1),
                daysOff = (m.cycleDaysOff ?: 0).coerceAtLeast(0),
                anchor = anchor,
            )
        ScheduleType.AS_NEEDED -> Recurrence.AsNeeded
    }

    return MedicationSchedule(
        recurrence = recurrence,
        times = timeList,
        startDate = m.startEpochDay?.let { LocalDate.ofEpochDay(it) },
        endDate = m.endEpochDay?.let { LocalDate.ofEpochDay(it) },
    )
}

// Storage uses a bitmask with bit 0 for Monday. java time's DayOfWeek numbers
// Monday as 1, so day index i maps to DayOfWeek.of(i + 1).
private fun maskToDayOfWeek(mask: Int?): Set<DayOfWeek> {
    if (mask == null) return emptySet()
    return (0..6).filter { (mask and (1 shl it)) != 0 }.map { DayOfWeek.of(it + 1) }.toSet()
}
