package club.hiraeth.flareenough.ui.today

/** The state of one planned dose slot for today. */
enum class SlotStatus {
    /** Planned but not yet acted on. Shown neutrally, whether it is due soon or overdue. */
    PLANNED,
    TAKEN,
    SKIPPED,
}

/**
 * One dose in today's list: which medication, when it is planned, and its current
 * status. [doseEventId] is set once it has been logged, so it can be undone.
 */
data class DoseSlot(
    val medicationId: Long,
    val medicationName: String,
    val scheduledTimeMillis: Long,
    val status: SlotStatus,
    val doseEventId: Long?,
)

/** What the Today screen shows. */
data class TodayUiState(
    val nextDose: DoseSlot? = null,
    val doses: List<DoseSlot> = emptyList(),
    val loading: Boolean = true,
)
