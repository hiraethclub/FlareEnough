package club.hiraeth.flareenough.export

import android.content.Context
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.data.db.entity.BodyState
import club.hiraeth.flareenough.data.db.entity.DoseStatus
import club.hiraeth.flareenough.data.db.entity.PeriodFlow
import club.hiraeth.flareenough.data.db.entity.SymptomEntryEntity
import club.hiraeth.flareenough.data.db.entity.SymptomTrackerEntity
import club.hiraeth.flareenough.data.db.entity.TrackerType
import club.hiraeth.flareenough.ui.symptoms.labelRes

/**
 * Shared, plain word labels for the CSV and PDF exports. Everything reads from string
 * resources so exports can be translated, and nothing here interprets the data: a
 * value is only turned into the same word the app shows for it.
 */

internal fun doseStatusLabel(context: Context, status: DoseStatus): String = context.getString(
    when (status) {
        DoseStatus.TAKEN -> R.string.status_taken
        DoseStatus.SKIPPED -> R.string.status_skipped
        DoseStatus.MISSED -> R.string.status_missed
    },
)

internal fun periodFlowLabel(context: Context, flow: PeriodFlow): String = context.getString(
    when (flow) {
        PeriodFlow.SPOTTING -> R.string.period_flow_spotting
        PeriodFlow.LIGHT -> R.string.period_flow_light
        PeriodFlow.MEDIUM -> R.string.period_flow_medium
        PeriodFlow.HEAVY -> R.string.period_flow_heavy
    },
)

internal fun bodyStateLabel(context: Context, state: BodyState): String =
    context.getString(state.labelRes())

internal fun trackerValueLabel(
    context: Context,
    tracker: SymptomTrackerEntity,
    entry: SymptomEntryEntity,
): String {
    val v = entry.valueInt
    return when (tracker.type) {
        TrackerType.FIVE_LEVEL ->
            if (v == null) entry.valueText.orEmpty()
            else tracker.levelLabels?.getOrNull(v - 1) ?: "$v"
        TrackerType.YES_NO -> when (v) {
            1 -> context.getString(R.string.yes)
            0 -> context.getString(R.string.no)
            else -> ""
        }
        TrackerType.DURATION ->
            tracker.durationOptions?.getOrNull(v ?: -1) ?: entry.valueText ?: v?.toString() ?: ""
        TrackerType.NUMBER ->
            if (v == null) entry.valueText.orEmpty()
            else buildString {
                append(v)
                tracker.unit?.let { append(' ').append(it) }
            }
    }
}
