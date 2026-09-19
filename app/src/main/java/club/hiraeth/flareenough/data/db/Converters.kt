package club.hiraeth.flareenough.data.db

import androidx.room.TypeConverter
import club.hiraeth.flareenough.data.db.entity.BodyRegion
import club.hiraeth.flareenough.data.db.entity.BodyState
import club.hiraeth.flareenough.data.db.entity.DoseStatus
import club.hiraeth.flareenough.data.db.entity.LoggedVia
import club.hiraeth.flareenough.data.db.entity.MedicationForm
import club.hiraeth.flareenough.data.db.entity.PeriodFlow
import club.hiraeth.flareenough.data.db.entity.ScheduleType
import club.hiraeth.flareenough.data.db.entity.SessionType
import club.hiraeth.flareenough.data.db.entity.TrackerType

/**
 * Room type converters.
 *
 * Enums are stored as their name (a stable String), not their ordinal, so that
 * reordering an enum in code never corrupts stored data. String lists are stored
 * joined by the unit separator control character, which does not appear in normal
 * text, so labels can contain commas and other punctuation safely.
 */
class Converters {

    // String lists, for tracker labels and duration options.

    @TypeConverter
    fun fromStringList(list: List<String>?): String? =
        list?.joinToString(SEPARATOR)

    @TypeConverter
    fun toStringList(value: String?): List<String>? =
        when {
            value == null -> null
            value.isEmpty() -> emptyList()
            else -> value.split(SEPARATOR)
        }

    // Enums. Each stored as its name.

    @TypeConverter
    fun fromMedicationForm(value: MedicationForm): String = value.name

    @TypeConverter
    fun toMedicationForm(value: String): MedicationForm = MedicationForm.valueOf(value)

    @TypeConverter
    fun fromScheduleType(value: ScheduleType): String = value.name

    @TypeConverter
    fun toScheduleType(value: String): ScheduleType = ScheduleType.valueOf(value)

    @TypeConverter
    fun fromDoseStatus(value: DoseStatus): String = value.name

    @TypeConverter
    fun toDoseStatus(value: String): DoseStatus = DoseStatus.valueOf(value)

    @TypeConverter
    fun fromLoggedVia(value: LoggedVia): String = value.name

    @TypeConverter
    fun toLoggedVia(value: String): LoggedVia = LoggedVia.valueOf(value)

    @TypeConverter
    fun fromTrackerType(value: TrackerType): String = value.name

    @TypeConverter
    fun toTrackerType(value: String): TrackerType = TrackerType.valueOf(value)

    @TypeConverter
    fun fromBodyRegion(value: BodyRegion): String = value.name

    @TypeConverter
    fun toBodyRegion(value: String): BodyRegion = BodyRegion.valueOf(value)

    @TypeConverter
    fun fromBodyState(value: BodyState): String = value.name

    @TypeConverter
    fun toBodyState(value: String): BodyState = BodyState.valueOf(value)

    @TypeConverter
    fun fromSessionType(value: SessionType): String = value.name

    @TypeConverter
    fun toSessionType(value: String): SessionType = SessionType.valueOf(value)

    @TypeConverter
    fun fromPeriodFlow(value: PeriodFlow): String = value.name

    @TypeConverter
    fun toPeriodFlow(value: String): PeriodFlow = PeriodFlow.valueOf(value)

    private companion object {
        const val SEPARATOR = ""
    }
}
