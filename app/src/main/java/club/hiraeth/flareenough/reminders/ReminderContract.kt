package club.hiraeth.flareenough.reminders

/**
 * Shared constants for the reminder system: intent actions, extra keys, the snooze
 * default, and how request codes are derived so each alarm and action has its own
 * PendingIntent.
 */
object ReminderContract {

    const val ACTION_DOSE_ALARM = "club.hiraeth.flareenough.action.DOSE_ALARM"
    const val ACTION_TEST_ALARM = "club.hiraeth.flareenough.action.TEST_ALARM"
    const val ACTION_TAKEN = "club.hiraeth.flareenough.action.TAKEN"
    const val ACTION_SNOOZE = "club.hiraeth.flareenough.action.SNOOZE"
    const val ACTION_SKIP = "club.hiraeth.flareenough.action.SKIP"

    const val EXTRA_MEDICATION_ID = "medicationId"
    const val EXTRA_SCHEDULED_TIME = "scheduledTimeMillis"
    const val EXTRA_IS_SNOOZE = "isSnooze"

    /** Default snooze, in minutes. Made configurable when settings storage lands. */
    const val DEFAULT_SNOOZE_MINUTES = 10

    // Request code scheme. Each medication gets a small block of codes so its alarm
    // and its three actions never clash with another medication's.
    private const val KIND_ALARM = 0
    private const val KIND_TAKEN = 1
    private const val KIND_SNOOZE = 2
    private const val KIND_SKIP = 3
    private const val KIND_CONTENT = 4
    private const val BLOCK = 10

    const val TEST_REQUEST_CODE = 999_999
    const val TEST_NOTIFICATION_ID = 999_998

    fun alarmRequestCode(medicationId: Long): Int = (medicationId * BLOCK + KIND_ALARM).toInt()
    fun takenRequestCode(medicationId: Long): Int = (medicationId * BLOCK + KIND_TAKEN).toInt()
    fun snoozeRequestCode(medicationId: Long): Int = (medicationId * BLOCK + KIND_SNOOZE).toInt()
    fun skipRequestCode(medicationId: Long): Int = (medicationId * BLOCK + KIND_SKIP).toInt()
    fun contentRequestCode(medicationId: Long): Int = (medicationId * BLOCK + KIND_CONTENT).toInt()

    /** The notification id for a medication's dose reminder. One active per medication. */
    fun doseNotificationId(medicationId: Long): Int = medicationId.toInt()
}
