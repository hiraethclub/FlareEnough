package club.hiraeth.flareenough.schedule

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Turns a wall clock schedule into real instants, correctly across clock changes.
 *
 * The important detail: a dose time is a wall clock time, like 08:00. To find when
 * it actually fires, we combine the date and the time in the given time zone. Java
 * time then handles the two UK daylight saving edge cases for us:
 * - Spring forward: the clocks jump from 01:00 to 02:00, so 01:30 does not exist.
 *   A dose set for 01:30 that day is moved forward to 02:30, so it is not skipped.
 * - Autumn back: 01:30 happens twice. A dose set for 01:30 fires once, at the first
 *   01:30, not twice.
 * These behaviours are what the tests pin down.
 */
object ScheduleEngine {

    /** A safety cap so a search for the next dose can never loop forever. */
    private const val MAX_SEARCH_DAYS = 800

    /** Resolve a date and wall clock time to a real instant in [zone]. */
    fun instantFor(date: LocalDate, time: TimeOfDay, zone: ZoneId): Instant {
        val local = LocalDateTime.of(date, LocalTime.of(time.hour, time.minute))
        // ZonedDateTime.of applies the zone rules: it shifts across a spring gap and
        // picks the earlier offset in an autumn overlap.
        return local.atZone(zone).toInstant()
    }

    /**
     * All dose instants in the half open window [fromInclusive, toExclusive), in
     * order. Empty for an as needed schedule.
     */
    fun occurrencesBetween(
        schedule: MedicationSchedule,
        zone: ZoneId,
        fromInclusive: Instant,
        toExclusive: Instant,
    ): List<Instant> {
        if (schedule.times.isEmpty()) return emptyList()
        if (!fromInclusive.isBefore(toExclusive)) return emptyList()

        // Look one day either side so a time near midnight, possibly shifted by a
        // clock change, is not missed.
        val startDate = fromInclusive.atZone(zone).toLocalDate().minusDays(1)
        val endDate = toExclusive.atZone(zone).toLocalDate().plusDays(1)
        val sortedTimes = schedule.times.sorted()

        val result = mutableListOf<Instant>()
        var date = startDate
        while (!date.isAfter(endDate)) {
            if (schedule.isDueOn(date)) {
                for (time in sortedTimes) {
                    val instant = instantFor(date, time, zone)
                    if (!instant.isBefore(fromInclusive) && instant.isBefore(toExclusive)) {
                        result.add(instant)
                    }
                }
            }
            date = date.plusDays(1)
        }
        return result.sorted()
    }

    /**
     * The first dose instant strictly after [after], or null if none within the
     * search cap (for example an ended schedule). Empty for as needed.
     */
    fun nextOccurrence(
        schedule: MedicationSchedule,
        zone: ZoneId,
        after: Instant,
    ): Instant? {
        if (schedule.times.isEmpty()) return null
        val sortedTimes = schedule.times.sorted()
        var date = after.atZone(zone).toLocalDate()
        var daysChecked = 0
        while (daysChecked <= MAX_SEARCH_DAYS) {
            if (schedule.isDueOn(date)) {
                for (time in sortedTimes) {
                    val instant = instantFor(date, time, zone)
                    if (instant.isAfter(after)) return instant
                }
            }
            date = date.plusDays(1)
            daysChecked++
        }
        return null
    }
}
