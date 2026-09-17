package club.hiraeth.flareenough.schedule

/**
 * A wall clock time of day, stored as minutes past local midnight (0 to 1439).
 *
 * We store recurring dose times this way, not as a fixed instant, so that a dose
 * set for 08:00 stays at 08:00 through UK clock changes. The reminder layer turns
 * a [TimeOfDay] into the next real instant when it schedules an alarm, which is
 * where daylight saving is handled.
 */
@JvmInline
value class TimeOfDay(val minutesPastMidnight: Int) : Comparable<TimeOfDay> {

    init {
        require(minutesPastMidnight in 0..MINUTES_PER_DAY - 1) {
            "minutesPastMidnight must be in 0..1439, was $minutesPastMidnight"
        }
    }

    val hour: Int get() = minutesPastMidnight / 60
    val minute: Int get() = minutesPastMidnight % 60

    /** 24 hour form, for example "08:00" or "01:30". Not localised, for storage and logs. */
    fun toIso(): String = "%02d:%02d".format(hour, minute)

    override fun compareTo(other: TimeOfDay): Int =
        minutesPastMidnight.compareTo(other.minutesPastMidnight)

    override fun toString(): String = toIso()

    companion object {
        const val MINUTES_PER_DAY: Int = 24 * 60

        fun of(hour: Int, minute: Int): TimeOfDay {
            require(hour in 0..23) { "hour must be in 0..23, was $hour" }
            require(minute in 0..59) { "minute must be in 0..59, was $minute" }
            return TimeOfDay(hour * 60 + minute)
        }

        /** Parse a 24 hour "HH:mm" string. Throws if the format is wrong. */
        fun parse(text: String): TimeOfDay {
            val parts = text.split(":")
            require(parts.size == 2) { "expected HH:mm, was \"$text\"" }
            val hour = parts[0].toIntOrNull() ?: error("bad hour in \"$text\"")
            val minute = parts[1].toIntOrNull() ?: error("bad minute in \"$text\"")
            return of(hour, minute)
        }
    }
}
