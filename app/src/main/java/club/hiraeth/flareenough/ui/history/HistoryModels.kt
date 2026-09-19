package club.hiraeth.flareenough.ui.history

import club.hiraeth.flareenough.data.db.entity.BodyRegion
import club.hiraeth.flareenough.data.db.entity.BodyState
import club.hiraeth.flareenough.data.db.entity.DoseStatus
import club.hiraeth.flareenough.data.db.entity.PeriodFlow
import club.hiraeth.flareenough.data.db.entity.SessionType
import club.hiraeth.flareenough.data.db.entity.SymptomTrackerEntity
import java.time.LocalDate
import java.time.YearMonth

/** A compact summary of one day, for the calendar dots and shading. */
data class DaySummary(
    val epochDay: Long,
    val hasActivity: Boolean,
    val flare: Boolean,
    /** Highest five level symptom value that day, 1 to 5, for a soft severity shade. */
    val maxSymptomLevel: Int?,
)

/**
 * One entry in a day's timeline. A sealed set so the screen can format each kind
 * with the right icon and words. Charts show data only, and so does this: no
 * interpretation.
 */
sealed interface TimelineItem {
    val timeMillis: Long?

    data class Dose(
        override val timeMillis: Long?,
        val medicationName: String,
        val status: DoseStatus,
    ) : TimelineItem

    data class Symptom(
        override val timeMillis: Long?,
        val tracker: SymptomTrackerEntity,
        val valueInt: Int?,
        val valueText: String?,
    ) : TimelineItem

    data class Flare(override val timeMillis: Long?) : TimelineItem

    data class Period(override val timeMillis: Long?, val flow: PeriodFlow) : TimelineItem

    data class Body(
        override val timeMillis: Long?,
        val region: BodyRegion,
        val state: BodyState,
    ) : TimelineItem

    data class Note(override val timeMillis: Long?, val text: String) : TimelineItem

    data class Meditation(
        override val timeMillis: Long?,
        val type: SessionType,
        val durationSeconds: Int,
    ) : TimelineItem
}

/** Everything the History screen needs to draw the calendar and the selected day. */
data class HistoryUiState(
    val yearMonth: YearMonth = YearMonth.now(),
    val selectedDay: LocalDate = LocalDate.now(),
    val summaries: Map<Long, DaySummary> = emptyMap(),
    val timeline: List<TimelineItem> = emptyList(),
)
