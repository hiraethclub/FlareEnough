package club.hiraeth.flareenough.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import club.hiraeth.flareenough.data.db.entity.DoseStatus
import club.hiraeth.flareenough.data.db.entity.LoggedVia
import club.hiraeth.flareenough.data.db.relation.MedicationWithTimes
import club.hiraeth.flareenough.data.repository.DoseRepository
import club.hiraeth.flareenough.data.repository.MedicationRepository
import club.hiraeth.flareenough.reminders.toSchedule
import club.hiraeth.flareenough.schedule.ScheduleEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Builds today's dose list by combining the active medications' planned times with
 * the doses already logged today, so each slot shows the right status. Also handles
 * logging Taken, Taken earlier, and Skipped, each undoable.
 */
class TodayViewModel(
    private val medicationRepository: MedicationRepository,
    private val doseRepository: DoseRepository,
) : ViewModel() {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val today: LocalDate = LocalDate.now()
    private val dayStart: Instant = today.atStartOfDay(zone).toInstant()
    private val dayEnd: Instant = today.plusDays(1).atStartOfDay(zone).toInstant()

    val state: StateFlow<TodayUiState> =
        combine(
            medicationRepository.observeActive(),
            doseRepository.observeBetween(dayStart.toEpochMilli(), dayEnd.toEpochMilli()),
        ) { meds, events ->
            buildState(meds, events)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TodayUiState(loading = true),
        )

    private fun buildState(
        meds: List<MedicationWithTimes>,
        events: List<club.hiraeth.flareenough.data.db.entity.DoseEventEntity>,
    ): TodayUiState {
        val now = System.currentTimeMillis()
        val eventBySlot = events
            .filter { it.scheduledTimeMillis != null }
            .associateBy { it.medicationId to it.scheduledTimeMillis!! }

        val slots = mutableListOf<DoseSlot>()
        for (med in meds) {
            val schedule = med.toSchedule()
            val occurrences = ScheduleEngine.occurrencesBetween(schedule, zone, dayStart, dayEnd)
            for (instant in occurrences) {
                val scheduledMillis = instant.toEpochMilli()
                val event = eventBySlot[med.medication.id to scheduledMillis]
                val status = when (event?.status) {
                    DoseStatus.TAKEN -> SlotStatus.TAKEN
                    DoseStatus.SKIPPED -> SlotStatus.SKIPPED
                    else -> SlotStatus.PLANNED
                }
                slots.add(
                    DoseSlot(
                        medicationId = med.medication.id,
                        medicationName = med.medication.name,
                        scheduledTimeMillis = scheduledMillis,
                        status = status,
                        doseEventId = event?.id,
                    ),
                )
            }
        }
        slots.sortBy { it.scheduledTimeMillis }

        val planned = slots.filter { it.status == SlotStatus.PLANNED }
        val next = planned.filter { it.scheduledTimeMillis <= now }.minByOrNull { it.scheduledTimeMillis }
            ?: planned.filter { it.scheduledTimeMillis > now }.minByOrNull { it.scheduledTimeMillis }

        return TodayUiState(nextDose = next, doses = slots, loading = false)
    }

    /** Log a slot as taken now. Returns the new event id, for undo. */
    suspend fun logTaken(slot: DoseSlot): Long {
        val now = System.currentTimeMillis()
        return doseRepository.logForSlot(
            medicationId = slot.medicationId,
            scheduledTimeMillis = slot.scheduledTimeMillis,
            status = DoseStatus.TAKEN,
            actualTimeMillis = now,
            loggedVia = LoggedVia.APP,
            nowMillis = now,
        )
    }

    /** Log a slot as taken at an earlier time today (minutes past midnight). */
    suspend fun logTakenAt(slot: DoseSlot, minutesPastMidnight: Int): Long {
        val now = System.currentTimeMillis()
        val actual = today.atStartOfDay(zone)
            .plusMinutes(minutesPastMidnight.toLong())
            .toInstant()
            .toEpochMilli()
        return doseRepository.logForSlot(
            medicationId = slot.medicationId,
            scheduledTimeMillis = slot.scheduledTimeMillis,
            status = DoseStatus.TAKEN,
            actualTimeMillis = actual,
            loggedVia = LoggedVia.APP,
            nowMillis = now,
        )
    }

    /** Log a slot as skipped. Recorded neutrally. Returns the event id, for undo. */
    suspend fun logSkipped(slot: DoseSlot): Long {
        val now = System.currentTimeMillis()
        return doseRepository.logForSlot(
            medicationId = slot.medicationId,
            scheduledTimeMillis = slot.scheduledTimeMillis,
            status = DoseStatus.SKIPPED,
            actualTimeMillis = null,
            loggedVia = LoggedVia.APP,
            nowMillis = now,
        )
    }

    suspend fun undo(doseEventId: Long) {
        doseRepository.undo(doseEventId)
    }
}
