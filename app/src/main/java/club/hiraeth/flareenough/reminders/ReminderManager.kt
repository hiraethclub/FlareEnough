package club.hiraeth.flareenough.reminders

import android.content.Context
import club.hiraeth.flareenough.data.repository.MedicationRepository
import club.hiraeth.flareenough.schedule.ScheduleEngine
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId

/**
 * Coordinates the reminder alarms. It reads medications and asks the alarm scheduler
 * to set the right alarm for each. The receivers, the medication editor, and the
 * health check screen all go through here.
 */
class ReminderManager(
    private val context: Context,
    private val medicationRepository: MedicationRepository,
    private val alarmScheduler: AlarmScheduler,
) {

    /** Reschedule every medication. Used on boot, time change, and app update. */
    suspend fun rescheduleAll() {
        medicationRepository.observeAll().first().forEach { alarmScheduler.scheduleNext(it) }
    }

    /** Reschedule one medication, from [after]. Cancels its alarm if it has none. */
    suspend fun rescheduleFor(medicationId: Long, after: Instant = Instant.now()) {
        val data = medicationRepository.get(medicationId)
        if (data == null) {
            alarmScheduler.cancel(medicationId)
        } else {
            alarmScheduler.scheduleNext(data, after)
        }
    }

    /** Cancel a medication's alarm and clear any showing reminder. */
    fun cancel(medicationId: Long) {
        alarmScheduler.cancel(medicationId)
        DoseNotifications.cancel(context, medicationId)
    }

    /** The soonest upcoming reminder across all active medications, for the health screen. */
    suspend fun nextReminderTime(): Instant? {
        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        return medicationRepository.observeAll().first()
            .filterNot { it.medication.paused }
            .mapNotNull { ScheduleEngine.nextOccurrence(it.toSchedule(), zone, now) }
            .minOrNull()
    }
}
