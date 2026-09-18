package club.hiraeth.flareenough.ui.symptoms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import club.hiraeth.flareenough.data.settings.SettingsRepository
import club.hiraeth.flareenough.data.db.entity.BodyRegion
import club.hiraeth.flareenough.data.db.entity.BodyState
import club.hiraeth.flareenough.data.db.entity.DayNoteEntity
import club.hiraeth.flareenough.data.db.entity.SymptomEntryEntity
import club.hiraeth.flareenough.data.db.entity.SymptomTrackerEntity
import club.hiraeth.flareenough.data.db.entity.TagEntity
import club.hiraeth.flareenough.data.repository.DayRepository
import club.hiraeth.flareenough.data.repository.SymptomRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** A tracker paired with today's value for it, if any. */
data class TrackerRow(
    val tracker: SymptomTrackerEntity,
    val entry: SymptomEntryEntity?,
)

/**
 * Drives quick symptom logging for today: the trackers and their values, the flare
 * flag, the body map, notes, and tags. Every write is a single tap or short entry.
 */
class SymptomLogViewModel(
    private val symptomRepository: SymptomRepository,
    private val dayRepository: DayRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val today: Long = LocalDate.now().toEpochDay()

    val hiddenBodyRegions: StateFlow<Set<BodyRegion>> =
        settingsRepository.observeHiddenBodyRegions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val trackerRows: StateFlow<List<TrackerRow>> =
        combine(
            symptomRepository.observeVisibleTrackers(),
            symptomRepository.observeEntriesForDay(today),
        ) { trackers, entries ->
            val byTracker = entries.associateBy { it.trackerId }
            trackers.map { TrackerRow(it, byTracker[it.id]) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val noTrackersAtAll: StateFlow<Boolean> =
        symptomRepository.observeAllTrackers()
            .map { it.isEmpty() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val flare: StateFlow<Boolean> =
        dayRepository.observeFlare(today)
            .map { it == true }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val bodyMap: StateFlow<Map<BodyRegion, BodyState>> =
        dayRepository.observeBodyMap(today)
            .map { list -> list.associate { it.region to it.state } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val dayTags: StateFlow<List<TagEntity>> =
        dayRepository.observeTagsForDay(today)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allTags: StateFlow<List<TagEntity>> =
        dayRepository.observeAllTags()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val notes: StateFlow<List<DayNoteEntity>> =
        dayRepository.observeNotes(today)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addTrackers(trackers: List<SymptomTrackerEntity>) {
        viewModelScope.launch { symptomRepository.addTrackers(trackers) }
    }

    fun addTracker(tracker: SymptomTrackerEntity) {
        viewModelScope.launch { symptomRepository.addTracker(tracker) }
    }

    fun setValue(trackerId: Long, valueInt: Int?, valueText: String?) {
        viewModelScope.launch {
            symptomRepository.setValue(trackerId, today, valueInt, valueText, System.currentTimeMillis())
        }
    }

    fun toggleFlare() {
        val next = !flare.value
        viewModelScope.launch {
            dayRepository.setFlare(today, next, System.currentTimeMillis())
        }
    }

    fun cycleRegion(region: BodyRegion) {
        viewModelScope.launch {
            dayRepository.cycleBodyRegion(today, region, System.currentTimeMillis())
        }
    }

    fun sameAsYesterday() {
        viewModelScope.launch {
            symptomRepository.copyDay(today - 1, today, System.currentTimeMillis())
        }
    }

    fun addNote(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            dayRepository.addNote(today, text.trim(), System.currentTimeMillis())
        }
    }

    fun deleteNote(note: DayNoteEntity) {
        viewModelScope.launch { dayRepository.deleteNote(note) }
    }

    fun addTag(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            dayRepository.addTagToDay(today, name, System.currentTimeMillis())
        }
    }

    fun removeTag(tagId: Long) {
        viewModelScope.launch { dayRepository.removeTagFromDay(today, tagId) }
    }
}
