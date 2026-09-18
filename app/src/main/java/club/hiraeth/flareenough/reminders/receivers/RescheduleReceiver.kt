package club.hiraeth.flareenough.reminders.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import club.hiraeth.flareenough.FlareApp
import club.hiraeth.flareenough.reminders.NotificationChannels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Rebuilds all dose alarms after events that can drop or shift them: device reboot,
 * app update, a manual time change, a time zone change, and the exact alarm
 * permission being granted or removed. This keeps reminders reliable across all of
 * those, which existing apps often fail to do.
 */
class RescheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Channels may need recreating after an update. Cheap and safe to repeat.
        NotificationChannels.ensureCreated(context)

        val container = (context.applicationContext as FlareApp).container
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                container.reminderManager.rescheduleAll()
            } finally {
                pending.finish()
            }
        }
    }
}
