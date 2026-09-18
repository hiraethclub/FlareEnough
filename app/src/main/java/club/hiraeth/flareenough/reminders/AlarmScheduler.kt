package club.hiraeth.flareenough.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.getSystemService
import club.hiraeth.flareenough.data.db.relation.MedicationWithTimes
import club.hiraeth.flareenough.reminders.receivers.DoseAlarmReceiver
import club.hiraeth.flareenough.schedule.ScheduleEngine
import java.time.Instant
import java.time.ZoneId

/**
 * Sets and cancels the exact alarms that drive dose reminders.
 *
 * Each medication has at most one pending alarm, for its next dose. When that alarm
 * fires, the receiver posts the reminder and asks for the next one to be scheduled,
 * so the chain continues on its own.
 *
 * Exact alarms fire on time even in Doze. If the person has not granted the "Alarms
 * and reminders" permission, we fall back to an inexact alarm, which may be late,
 * and the app shows a banner about that elsewhere.
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager = context.getSystemService()!!

    /** Whether exact alarms can be scheduled right now. */
    fun canScheduleExact(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

    /**
     * Schedule the next dose alarm for a medication, or cancel if there is none.
     * [after] is the point to search from, so the receiver can pass the instant of
     * the dose that just fired and get the following one, never the same again.
     */
    fun scheduleNext(data: MedicationWithTimes, after: Instant = Instant.now()) {
        val medId = data.medication.id
        if (data.medication.paused) {
            cancel(medId)
            return
        }
        val schedule = data.toSchedule()
        val next = ScheduleEngine.nextOccurrence(schedule, ZoneId.systemDefault(), after)
        if (next == null) {
            cancel(medId)
            return
        }
        setAlarm(
            triggerAtMillis = next.toEpochMilli(),
            pendingIntent = alarmPendingIntent(
                medicationId = medId,
                scheduledTimeMillis = next.toEpochMilli(),
                isSnooze = false,
                requestCode = ReminderContract.alarmRequestCode(medId),
            ),
        )
    }

    /** Schedule a snoozed reminder to reappear later without advancing the chain. */
    fun scheduleSnooze(medicationId: Long, scheduledTimeMillis: Long, triggerAtMillis: Long) {
        setAlarm(
            triggerAtMillis = triggerAtMillis,
            pendingIntent = alarmPendingIntent(
                medicationId = medicationId,
                scheduledTimeMillis = scheduledTimeMillis,
                isSnooze = true,
                requestCode = ReminderContract.snoozeRequestCode(medicationId),
            ),
        )
    }

    /** Schedule the one minute test reminder from the health check screen. */
    fun scheduleTest(triggerAtMillis: Long) {
        val intent = Intent(context, DoseAlarmReceiver::class.java)
            .setAction(ReminderContract.ACTION_TEST_ALARM)
        val pi = PendingIntent.getBroadcast(
            context,
            ReminderContract.TEST_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        setAlarm(triggerAtMillis, pi)
    }

    fun cancel(medicationId: Long) {
        alarmManager.cancel(
            alarmPendingIntent(
                medicationId = medicationId,
                scheduledTimeMillis = 0,
                isSnooze = false,
                requestCode = ReminderContract.alarmRequestCode(medicationId),
            ),
        )
    }

    private fun setAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        // setExactAndAllowWhileIdle fires on time in Doze, but needs the exact alarm
        // permission on Android 12 and up. Without it, fall back to an inexact alarm.
        if (canScheduleExact()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        }
    }

    private fun alarmPendingIntent(
        medicationId: Long,
        scheduledTimeMillis: Long,
        isSnooze: Boolean,
        requestCode: Int,
    ): PendingIntent {
        val intent = Intent(context, DoseAlarmReceiver::class.java)
            .setAction(ReminderContract.ACTION_DOSE_ALARM)
            .putExtra(ReminderContract.EXTRA_MEDICATION_ID, medicationId)
            .putExtra(ReminderContract.EXTRA_SCHEDULED_TIME, scheduledTimeMillis)
            .putExtra(ReminderContract.EXTRA_IS_SNOOZE, isSnooze)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
