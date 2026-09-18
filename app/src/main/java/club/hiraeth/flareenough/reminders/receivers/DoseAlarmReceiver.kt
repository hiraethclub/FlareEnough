package club.hiraeth.flareenough.reminders.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import club.hiraeth.flareenough.FlareApp
import club.hiraeth.flareenough.reminders.DoseNotifications
import club.hiraeth.flareenough.reminders.ReminderContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * Fires when a dose alarm goes off. It posts the reminder notification and, unless
 * this was a snooze, schedules the following dose so the chain keeps going.
 */
class DoseAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ReminderContract.ACTION_TEST_ALARM) {
            DoseNotifications.postTestReminder(context)
            return
        }

        val medicationId = intent.getLongExtra(ReminderContract.EXTRA_MEDICATION_ID, -1L)
        if (medicationId < 0) return
        val scheduledTime = intent.getLongExtra(ReminderContract.EXTRA_SCHEDULED_TIME, 0L)
        val isSnooze = intent.getBooleanExtra(ReminderContract.EXTRA_IS_SNOOZE, false)

        val container = (context.applicationContext as FlareApp).container
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = container.medicationRepository.get(medicationId) ?: return@launch
                DoseNotifications.postDoseReminder(
                    context = context,
                    medicationId = medicationId,
                    medicationName = data.medication.name,
                    scheduledTimeMillis = scheduledTime,
                )
                if (!isSnooze) {
                    // Schedule the dose after the one that just fired.
                    container.reminderManager.rescheduleFor(
                        medicationId = medicationId,
                        after = Instant.ofEpochMilli(scheduledTime),
                    )
                }
            } finally {
                pending.finish()
            }
        }
    }
}
