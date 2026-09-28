package club.hiraeth.flareenough.ui.dayedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import club.hiraeth.flareenough.data.db.entity.BodyRegion
import club.hiraeth.flareenough.data.db.entity.BodyState
import club.hiraeth.flareenough.data.db.entity.DayNoteEntity
import club.hiraeth.flareenough.data.db.entity.DoseEventEntity
import club.hiraeth.flareenough.data.db.entity.DoseStatus
import club.hiraeth.flareenough.data.db.entity.LoggedVia
import club.hiraeth.flareenough.data.db.entity.PeriodFlow
import club.hiraeth.flareenough.data.db.entity.TagEntity
import club.hiraeth.flareenough.data.db.relation.MedicationWithTimes
import club.hiraeth.flareenough.data.repository.DayRepository
import club.hiraeth.flareenough.data.repository.DoseRepository
import club.hiraeth.flareenough.data.repository.MedicationRepository
import club.hiraeth.flareenough.data.repository.SymptomRepository
import club.hiraeth.flareenough.data.settings.SettingsRepository
import club.hiraeth.flareenough.reminders.toSchedule
import club.hiraeth.flareenough.schedule.ScheduleEngine
import club.hiraeth.flareenough.ui.symptoms.TrackerRow
import club.hiraeth.flareenough.ui.today.DoseSlot
import club.hiraeth.flareenough.ui.today.SlotStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Edits a whole past (or present) day at once: the medication doses that were due
 * that day, plus the symptoms, flare, body map, period, notes, and tags. Every write
 * targets the chosen day, so this is the same logging the Today and Log screens do,
 * just pointed at another date.
 */
class DayEditViewModel(
    val epochDay: Long,
    private val medicationRepository: MedicationRepository,
    private val doseRepository: DoseRepository,
    private val symptomRepository: SymptomRepository,
    private val dayRepository: DayRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val dayStart: Instant = LocalDate.ofEpochDay(epochDay).atStartOfDay(zone).toInstant()
    private val dayEnd: Instant = LocalDate.ofEpochDay(epochDay).plusDays(1).atStartOfDay(zone).toInstant()

    // Medication doses for the day, each slot editable to taken, skipped, or clear.

    val doseSlots: StateFlow<List<DoseSlot>> =
        combine(
            medicationRepository.observeActive(),
            doseRepository.observeBetween(dayStart.toEpochMilli(), dayEnd.toEpochMilli()),
        ) { meds, events -> buildSlots(meds, events) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun buildSlots(meds: List<MedicationWithTimes>, events: List<DoseEventEntity>): List<DoseSlot> {
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
        return slots
    }

    /** Set a slot to taken or skipped, or pass null to clear it back to planned. */
    fun setDoseStatus(slot: DoseSlot, status: DoseStatus?) {
        viewModelScope.launch {
            if (status == null) {
                slot.doseEventId?.let { doseRepository.undo(it) }
            } else {
                doseRepository.logForSlot(
                    medicationId = slot.medicationId,
                    scheduledTimeMillis = slot.scheduledTimeMillis,
                    status = status,
                    actualTimeMillis = if (status == DoseStatus.TAKEN) slot.scheduledTimeMillis else null,
                    loggedVia = LoggedVia.APP,
                    nowMillis = System.currentTimeMillis(),
                )
            }
        }
    }

    // Symptoms and the rest of the day, all pointed at this date.

    val trackerRows: StateFlow<List<TrackerRow>> =
        combine(
            symptomRepository.observeVisibleTrackers(),
            symptomRepository.observeEntriesForDay(epochDay),
        ) { trackers, entries ->
            val byTracker = entries.associateBy { it.trackerId }
            trackers.map { TrackerRow(it, byTracker[it.id]) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val flare: StateFlow<Boolean> =
        dayRepository.observeFlare(epochDay)
            .map { it == true }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val bodyMap: StateFlow<Map<BodyRegion, BodyState>> =
        dayRepository.observeBodyMap(epochDay)
            .map { list -> list.associate { it.region to it.state } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val hiddenBodyRegions: StateFlow<Set<BodyRegion>> =
        settingsRepository.observeHiddenBodyRegions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val periodTrackingEnabled: StateFlow<Boolean> =
        settingsRepository.observePeriodTrackingEnabled()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val periodFlow: StateFlow<PeriodFlow?> =
        dayRepository.observePeriodDay(epochDay)
            .map { it?.flow }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val notes: StateFlow<List<DayNoteEntity>> =
        dayRepository.observeNotes(epochDay)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val dayTags: StateFlow<List<TagEntity>> =
        dayRepository.observeTagsForDay(epochDay)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setValue(trackerId: Long, valueInt: Int?) {
        viewModelScope.launch {
            symptomRepository.setValue(trackerId, epochDay, valueInt, null, System.currentTimeMillis())
        }
    }

    fun toggleFlare() {
        val next = !flare.value
        viewModelScope.launch { dayRepository.setFlare(epochDay, next, System.currentTimeMillis()) }
    }

    fun cycleRegion(region: BodyRegion) {
        viewModelScope.launch { dayRepository.cycleBodyRegion(epochDay, region, System.currentTimeMillis()) }
    }

    fun setPeriodFlow(flow: PeriodFlow?) {
        val next = if (flow != null && flow == periodFlow.value) null else flow
        viewModelScope.launch { dayRepository.setPeriodFlow(epochDay, next, System.currentTimeMillis()) }
    }

    fun addNote(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch { dayRepository.addNote(epochDay, text.trim(), System.currentTimeMillis()) }
    }

    fun deleteNote(note: DayNoteEntity) {
        viewModelScope.launch { dayRepository.deleteNote(note) }
    }

    fun addTag(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { dayRepository.addTagToDay(epochDay, name, System.currentTimeMillis()) }
    }

    fun removeTag(tagId: Long) {
        viewModelScope.launch { dayRepository.removeTagFromDay(epochDay, tagId) }
    }
}
