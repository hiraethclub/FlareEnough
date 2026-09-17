package club.hiraeth.flareenough.ui.medication

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.data.db.entity.ScheduleType
import club.hiraeth.flareenough.data.db.relation.MedicationWithTimes
import club.hiraeth.flareenough.ui.support.formatMinutesOfDay

/**
 * A short, plain sentence describing a medication's schedule, for the list. Uses
 * the phone's time format and translated day names. No jargon.
 */
@Composable
fun scheduleSummary(data: MedicationWithTimes): String {
    val context = LocalContext.current
    val m = data.medication
    val timesText = data.times
        .sortedBy { it.minutesPastMidnight }
        .joinToString(", ") { formatMinutesOfDay(context, it.minutesPastMidnight) }

    if (m.scheduleType == ScheduleType.AS_NEEDED) {
        val gapHours = m.asNeededMinGapMinutes?.let { it / 60 }
        return if (gapHours != null && gapHours > 0) {
            stringResource(R.string.schedule_as_needed_gap, gapHours)
        } else {
            stringResource(R.string.schedule_as_needed)
        }
    }

    if (data.times.isEmpty()) return stringResource(R.string.schedule_no_times)

    val dayAbbrev = stringArrayResource(R.array.day_abbreviations)
    val dayFull = stringArrayResource(R.array.day_full_names)

    return when (m.scheduleType) {
        ScheduleType.EVERY_DAY ->
            stringResource(R.string.schedule_every_day, timesText)

        ScheduleType.DAYS_OF_WEEK -> {
            val days = MedicationFormState.maskToDays(m.daysOfWeekMask).orEmpty()
                .sorted().joinToString(", ") { dayAbbrev[it] }
            stringResource(R.string.schedule_days, days, timesText)
        }

        ScheduleType.WEEKLY -> {
            val day = MedicationFormState.maskToDays(m.daysOfWeekMask).orEmpty().minOrNull()
            val dayName = day?.let { dayFull[it] } ?: ""
            stringResource(R.string.schedule_weekly, dayName, timesText)
        }

        ScheduleType.EVERY_N_DAYS -> {
            val n = m.intervalDays ?: 1
            pluralStringResource(R.plurals.schedule_every_n_days, n, n, timesText)
        }

        ScheduleType.CYCLE ->
            stringResource(
                R.string.schedule_cycle,
                m.cycleDaysOn ?: 0,
                m.cycleDaysOff ?: 0,
                timesText,
            )

        ScheduleType.AS_NEEDED -> stringResource(R.string.schedule_as_needed)
    }
}
