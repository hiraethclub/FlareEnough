package club.hiraeth.flareenough.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import club.hiraeth.flareenough.data.db.entity.DoseEventEntity
import club.hiraeth.flareenough.data.db.entity.DoseStatus
import club.hiraeth.flareenough.data.db.entity.DayNoteEntity
import club.hiraeth.flareenough.data.db.entity.MeditationSessionEntity
import club.hiraeth.flareenough.data.db.entity.SymptomEntryEntity
import club.hiraeth.flareenough.data.db.entity.SymptomTrackerEntity
import club.hiraeth.flareenough.data.db.entity.TrackerType
import club.hiraeth.flareenough.data.repository.DayRepository
import club.hiraeth.flareenough.data.repository.DoseRepository
import club.hiraeth.flareenough.data.repository.MedicationRepository
import club.hiraeth.flareenough.data.repository.MeditationRepository
import club.hiraeth.flareenough.data.repository.SymptomRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/** Raw month data pulled from the database, before it is shaped for the screen. */
private data class MonthRaw(
    val doses: List<DoseEventEntity>,
    val symptoms: List<SymptomEntryEntity>,
    val flareDays: Set<Long>,
    val meditation: List<MeditationSessionEntity>,
    val notes: List<DayNoteEntity>,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    medicationRepository: MedicationRepository,
    private val doseRepository: DoseRepository,
    private val symptomRepository: SymptomRepository,
    private val dayRepository: DayRepository,
    private val meditationRepository: MeditationRepository,
) : ViewModel() {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val month = MutableStateFlow(YearMonth.now())
    private val selected = MutableStateFlow(LocalDate.now())

    private val medNames: StateFlow<Map<Long, String>> =
        medicationRepository.observeAll()
            .map { list -> list.associate { it.medication.id to it.medication.name } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val trackers: StateFlow<Map<Long, SymptomTrackerEntity>> =
        symptomRepository.observeAllTrackers()
            .map { list -> list.associateBy { it.id } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val monthData: StateFlow<MonthRaw> =
        month.flatMapLatest { ym ->
            val startDay = ym.atDay(1).toEpochDay()
            val endDay = ym.atEndOfMonth().toEpochDay()
            val startMillis = ym.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val endMillis = ym.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            combine(
                doseRepository.observeBetween(startMillis, endMillis),
                symptomRepository.observeEntriesBetween(startDay, endDay),
                dayRepository.observeFlareDaysBetween(startDay, endDay),
                meditationRepository.observeBetween(startMillis, endMillis),
                dayRepository.observeNotesBetween(startDay, endDay),
            ) { doses, symptoms, flareDays, meditation, notes ->
                MonthRaw(doses, symptoms, flareDays.toSet(), meditation, notes)
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            MonthRaw(emptyList(), emptyList(), emptySet(), emptyList(), emptyList()),
        )

    val uiState: StateFlow<HistoryUiState> =
        combine(month, selected, monthData, medNames, trackers) { ym, day, raw, names, trackerMap ->
            HistoryUiState(
                yearMonth = ym,
                selectedDay = day,
                summaries = buildSummaries(ym, raw, trackerMap),
                timeline = buildTimeline(day, raw, names, trackerMap),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun selectDay(day: LocalDate) {
        selected.value = day
        month.value = YearMonth.from(day)
    }

    fun showMonth(ym: YearMonth) {
        month.value = ym
    }

    private fun dayOf(millis: Long): Long =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().toEpochDay()

    private fun doseDay(dose: DoseEventEntity): Long =
        dayOf(dose.actualTimeMillis ?: dose.scheduledTimeMillis ?: dose.createdAtMillis)

    private fun buildSummaries(
        ym: YearMonth,
        raw: MonthRaw,
        trackerMap: Map<Long, SymptomTrackerEntity>,
    ): Map<Long, DaySummary> {
        val dosesByDay = raw.doses.groupBy { doseDay(it) }
        val symptomsByDay = raw.symptoms.groupBy { it.epochDay }
        val meditationByDay = raw.meditation.groupBy { dayOf(it.startTimeMillis) }
        val notesByDay = raw.notes.groupBy { it.epochDay }

        val result = mutableMapOf<Long, DaySummary>()
        var date = ym.atDay(1)
        val end = ym.atEndOfMonth()
        while (!date.isAfter(end)) {
            val epochDay = date.toEpochDay()
            val daySymptoms = symptomsByDay[epochDay].orEmpty()
            val maxLevel = daySymptoms
                .filter { trackerMap[it.trackerId]?.type == TrackerType.FIVE_LEVEL }
                .mapNotNull { it.valueInt }
                .maxOrNull()
            val hasActivity = dosesByDay.containsKey(epochDay) ||
                daySymptoms.isNotEmpty() ||
                meditationByDay.containsKey(epochDay) ||
                notesByDay.containsKey(epochDay)
            if (hasActivity || raw.flareDays.contains(epochDay)) {
                result[epochDay] = DaySummary(
                    epochDay = epochDay,
                    hasActivity = hasActivity,
                    flare = raw.flareDays.contains(epochDay),
                    maxSymptomLevel = maxLevel,
                )
            }
            date = date.plusDays(1)
        }
        return result
    }

    private fun buildTimeline(
        day: LocalDate,
        raw: MonthRaw,
        names: Map<Long, String>,
        trackerMap: Map<Long, SymptomTrackerEntity>,
    ): List<TimelineItem> {
        val epochDay = day.toEpochDay()
        val items = mutableListOf<TimelineItem>()

        raw.doses.filter { doseDay(it) == epochDay }.forEach { dose ->
            items.add(
                TimelineItem.Dose(
                    timeMillis = dose.actualTimeMillis ?: dose.scheduledTimeMillis,
                    medicationName = names[dose.medicationId] ?: "",
                    status = dose.status,
                ),
            )
        }
        raw.symptoms.filter { it.epochDay == epochDay }.forEach { entry ->
            val tracker = trackerMap[entry.trackerId]
            if (tracker != null) {
                items.add(
                    TimelineItem.Symptom(
                        timeMillis = entry.createdAtMillis,
                        tracker = tracker,
                        valueInt = entry.valueInt,
                        valueText = entry.valueText,
                    ),
                )
            }
        }
        if (raw.flareDays.contains(epochDay)) {
            items.add(TimelineItem.Flare(timeMillis = null))
        }
        raw.notes.filter { it.epochDay == epochDay }.forEach { note ->
            items.add(TimelineItem.Note(timeMillis = note.createdAtMillis, text = note.text))
        }
        raw.meditation.filter { dayOf(it.startTimeMillis) == epochDay }.forEach { session ->
            items.add(
                TimelineItem.Meditation(
                    timeMillis = session.startTimeMillis,
                    type = session.type,
                    durationSeconds = session.durationSeconds,
                ),
            )
        }

        return items.sortedWith(compareBy(nullsLast<Long>()) { it.timeMillis })
    }
}
