package club.hiraeth.flareenough.ui.support

import android.content.Context
import android.text.format.DateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date

/**
 * Format a time of day (minutes past midnight) using the phone's own 12 or 24 hour
 * setting, so it reads the way the person expects.
 */
fun formatMinutesOfDay(context: Context, minutesPastMidnight: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, minutesPastMidnight / 60)
        set(Calendar.MINUTE, minutesPastMidnight % 60)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return DateFormat.getTimeFormat(context).format(calendar.time)
}

/** Format an instant (epoch millis) as a time of day, using the phone's format. */
fun formatInstantTime(context: Context, epochMillis: Long): String =
    DateFormat.getTimeFormat(context).format(Date(epochMillis))

/** Format a local date (epoch day) using the phone's date format. */
fun formatEpochDay(context: Context, epochDay: Long): String {
    // Build the instant for local midnight of that day, so the shown date is right
    // in every time zone, not just those at or ahead of UTC.
    val millis = LocalDate.ofEpochDay(epochDay)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    return DateFormat.getDateFormat(context).format(Date(millis))
}
