package club.hiraeth.flareenough.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import club.hiraeth.flareenough.R

/**
 * The four notification channels, kept separate so the person can control each kind
 * on their own: dose reminders, refill warnings, meditation bells, and the daily
 * symptom check in. Channels exist natively from Android 8, which is our minimum.
 */
object NotificationChannels {

    const val DOSE_REMINDERS = "dose_reminders"
    const val REFILL_WARNINGS = "refill_warnings"
    const val MEDITATION_BELLS = "meditation_bells"
    const val DAILY_CHECK_IN = "daily_check_in"

    fun ensureCreated(context: Context) {
        val manager = context.getSystemService<NotificationManager>() ?: return

        val channels = listOf(
            NotificationChannel(
                DOSE_REMINDERS,
                context.getString(R.string.channel_dose_reminders),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.channel_dose_reminders_desc)
            },
            NotificationChannel(
                REFILL_WARNINGS,
                context.getString(R.string.channel_refill_warnings),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.channel_refill_warnings_desc)
            },
            NotificationChannel(
                MEDITATION_BELLS,
                context.getString(R.string.channel_meditation_bells),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.channel_meditation_bells_desc)
            },
            NotificationChannel(
                DAILY_CHECK_IN,
                context.getString(R.string.channel_daily_check_in),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.channel_daily_check_in_desc)
            },
        )
        manager.createNotificationChannels(channels)
    }
}
