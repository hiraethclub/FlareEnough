package club.hiraeth.flareenough.reminders.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import club.hiraeth.flareenough.FlareApp
import club.hiraeth.flareenough.data.db.entity.DoseStatus
import club.hiraeth.flareenough.data.db.entity.LoggedVia
import club.hiraeth.flareenough.reminders.DoseNotifications
import club.hiraeth.flareenough.reminders.ReminderContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles the Taken, Snooze, and Skip actions from a dose reminder. Acting here logs
 * the dose and dismisses the reminder without opening the app.
 */
class DoseActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getLongExtra(ReminderContract.EXTRA_MEDICATION_ID, -1L)
        if (medicationId < 0) return
        val scheduledTime = intent.getLongExtra(ReminderContract.EXTRA_SCHEDULED_TIME, 0L)
            .takeIf { it != 0L }
        val action = intent.action ?: return

        val container = (context.applicationContext as FlareApp).container
        val now = System.currentTimeMillis()
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    ReminderContract.ACTION_TAKEN -> container.doseRepository.logForSlot(
                        medicationId = medicationId,
                        scheduledTimeMillis = scheduledTime,
                        status = DoseStatus.TAKEN,
                        actualTimeMillis = now,
                        loggedVia = LoggedVia.NOTIFICATION,
                        nowMillis = now,
                    )
                    ReminderContract.ACTION_SKIP -> container.doseRepository.logForSlot(
                        medicationId = medicationId,
                        scheduledTimeMillis = scheduledTime,
                        status = DoseStatus.SKIPPED,
                        actualTimeMillis = null,
                        loggedVia = LoggedVia.NOTIFICATION,
                        nowMillis = now,
                    )
                    ReminderContract.ACTION_SNOOZE -> container.alarmScheduler.scheduleSnooze(
                        medicationId = medicationId,
                        scheduledTimeMillis = scheduledTime ?: now,
                        triggerAtMillis = now + ReminderContract.DEFAULT_SNOOZE_MINUTES * 60_000L,
                    )
                }
                DoseNotifications.cancel(context, medicationId)
            } finally {
                pending.finish()
            }
        }
    }
}
