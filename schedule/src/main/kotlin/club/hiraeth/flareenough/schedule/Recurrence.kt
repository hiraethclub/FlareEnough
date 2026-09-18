package club.hiraeth.flareenough.schedule

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * How a medication repeats, with no Android or database types. The app layer maps
 * its stored medication to one of these.
 */
sealed interface Recurrence {

    /** Every day. */
    data object Daily : Recurrence

    /** On the given days of the week. Also covers a weekly schedule (one day set). */
    data class DaysOfWeek(val days: Set<DayOfWeek>) : Recurrence

    /**
     * Every [interval] days counted from [anchor]. The anchor is a day the dose is
     * due (usually the start date). Days before the anchor are not due.
     */
    data class EveryNDays(val interval: Int, val anchor: LocalDate) : Recurrence {
        init {
            require(interval >= 1) { "interval must be at least 1, was $interval" }
        }
    }

    /**
     * [daysOn] days due, then [daysOff] days not due, repeating from [anchor].
     * Days before the anchor are not due.
     */
    data class Cycle(val daysOn: Int, val daysOff: Int, val anchor: LocalDate) : Recurrence {
        init {
            require(daysOn >= 1) { "daysOn must be at least 1, was $daysOn" }
            require(daysOff >= 0) { "daysOff must be 0 or more, was $daysOff" }
        }

        val cycleLength: Int get() = daysOn + daysOff
    }

    /** No schedule. Taken only when needed, so it has no planned occurrences. */
    data object AsNeeded : Recurrence
}

/**
 * A complete medication schedule: how it repeats, the times of day it is due, and
 * an optional active window. Times are wall clock, resolved to real instants by the
 * engine so UK clock changes behave.
 */
data class MedicationSchedule(
    val recurrence: Recurrence,
    val times: List<TimeOfDay>,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
) {
    /** Whether a dose is due on [date], ignoring the time of day. */
    fun isDueOn(date: LocalDate): Boolean {
        if (recurrence is Recurrence.AsNeeded) return false
        if (startDate != null && date.isBefore(startDate)) return false
        if (endDate != null && date.isAfter(endDate)) return false
        return when (val r = recurrence) {
            Recurrence.Daily -> true
            is Recurrence.DaysOfWeek -> date.dayOfWeek in r.days
            is Recurrence.EveryNDays -> {
                val diff = date.toEpochDay() - r.anchor.toEpochDay()
                diff >= 0 && diff % r.interval == 0L
            }
            is Recurrence.Cycle -> {
                val diff = date.toEpochDay() - r.anchor.toEpochDay()
                diff >= 0 && (diff % r.cycleLength) < r.daysOn
            }
            Recurrence.AsNeeded -> false
        }
    }
}
