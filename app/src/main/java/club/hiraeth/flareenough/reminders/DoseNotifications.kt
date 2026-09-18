package club.hiraeth.flareenough.reminders

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import club.hiraeth.flareenough.MainActivity
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.reminders.receivers.DoseActionReceiver

/**
 * Builds and posts dose reminder notifications. Acting on Taken, Snooze, or Skip
 * from the notification logs the dose without opening the app.
 */
object DoseNotifications {

    private const val IMMUTABLE = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    fun postDoseReminder(
        context: Context,
        medicationId: Long,
        medicationName: String,
        scheduledTimeMillis: Long,
    ) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val contentIntent = PendingIntent.getActivity(
            context,
            ReminderContract.contentRequestCode(medicationId),
            Intent(context, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(context, NotificationChannels.DOSE_REMINDERS)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setContentTitle(medicationName)
            .setContentText(context.getString(R.string.reminder_due_now))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .addAction(
                0,
                context.getString(R.string.action_taken),
                actionIntent(context, ReminderContract.ACTION_TAKEN, medicationId, scheduledTimeMillis, ReminderContract.takenRequestCode(medicationId)),
            )
            .addAction(
                0,
                context.getString(R.string.action_snooze),
                actionIntent(context, ReminderContract.ACTION_SNOOZE, medicationId, scheduledTimeMillis, ReminderContract.snoozeRequestCode(medicationId)),
            )
            .addAction(
                0,
                context.getString(R.string.action_skip),
                actionIntent(context, ReminderContract.ACTION_SKIP, medicationId, scheduledTimeMillis, ReminderContract.skipRequestCode(medicationId)),
            )

        try {
            manager.notify(ReminderContract.doseNotificationId(medicationId), builder.build())
        } catch (_: SecurityException) {
            // Notifications not permitted. Nothing to post.
        }
    }

    fun postTestReminder(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        val builder = NotificationCompat.Builder(context, NotificationChannels.DOSE_REMINDERS)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setContentTitle(context.getString(R.string.reminder_test_title))
            .setContentText(context.getString(R.string.reminder_test_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        try {
            manager.notify(ReminderContract.TEST_NOTIFICATION_ID, builder.build())
        } catch (_: SecurityException) {
        }
    }

    fun cancel(context: Context, medicationId: Long) {
        NotificationManagerCompat.from(context).cancel(ReminderContract.doseNotificationId(medicationId))
    }

    private fun actionIntent(
        context: Context,
        action: String,
        medicationId: Long,
        scheduledTimeMillis: Long,
        requestCode: Int,
    ): PendingIntent {
        val intent = Intent(context, DoseActionReceiver::class.java)
            .setAction(action)
            .putExtra(ReminderContract.EXTRA_MEDICATION_ID, medicationId)
            .putExtra(ReminderContract.EXTRA_SCHEDULED_TIME, scheduledTimeMillis)
        return PendingIntent.getBroadcast(context, requestCode, intent, IMMUTABLE)
    }
}
