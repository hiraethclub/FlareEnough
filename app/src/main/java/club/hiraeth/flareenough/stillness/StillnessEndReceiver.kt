package club.hiraeth.flareenough.stillness

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import club.hiraeth.flareenough.FlareApp
import club.hiraeth.flareenough.data.db.entity.SessionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Fires when a stillness timer reaches its end. Rings the ending bell and logs the
 * session, so both happen even if the screen is off or the app is in the background.
 */
class StillnessEndReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        BellPlayer.play(durationSeconds = 3.0)

        val startTime = intent.getLongExtra(StillnessContract.EXTRA_START_TIME, 0L)
        val durationSeconds = intent.getIntExtra(StillnessContract.EXTRA_DURATION_SECONDS, 0)
        if (durationSeconds <= 0) return

        val container = (context.applicationContext as FlareApp).container
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                container.meditationRepository.log(
                    startTimeMillis = startTime,
                    durationSeconds = durationSeconds,
                    type = SessionType.TIMER,
                    nowMillis = System.currentTimeMillis(),
                )
            } finally {
                pending.finish()
            }
        }
    }
}
