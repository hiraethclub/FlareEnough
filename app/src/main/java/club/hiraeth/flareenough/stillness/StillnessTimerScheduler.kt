package club.hiraeth.flareenough.stillness

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.getSystemService

/**
 * Schedules a single exact alarm for the end of a stillness timer, so the ending
 * bell rings and the session is logged even when the screen is off or the app is in
 * the background. Interval and starting bells are played by the running screen.
 */
class StillnessTimerScheduler(private val context: Context) {

    private val alarmManager: AlarmManager = context.getSystemService()!!

    fun scheduleEnd(endAtMillis: Long, startTimeMillis: Long, durationSeconds: Int) {
        val pi = pendingIntent(startTimeMillis, durationSeconds)
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAtMillis, pi)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAtMillis, pi)
        }
    }

    fun cancel() {
        alarmManager.cancel(pendingIntent(0, 0))
    }

    private fun pendingIntent(startTimeMillis: Long, durationSeconds: Int): PendingIntent {
        val intent = Intent(context, StillnessEndReceiver::class.java)
            .setAction(StillnessContract.ACTION_TIMER_END)
            .putExtra(StillnessContract.EXTRA_START_TIME, startTimeMillis)
            .putExtra(StillnessContract.EXTRA_DURATION_SECONDS, durationSeconds)
        return PendingIntent.getBroadcast(
            context,
            StillnessContract.END_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
