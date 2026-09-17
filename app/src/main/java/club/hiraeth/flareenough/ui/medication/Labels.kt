package club.hiraeth.flareenough.ui.medication

import androidx.annotation.StringRes
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.data.db.entity.MedicationForm
import club.hiraeth.flareenough.data.db.entity.ScheduleType

@StringRes
fun MedicationForm.labelRes(): Int = when (this) {
    MedicationForm.TABLET -> R.string.form_tablet
    MedicationForm.CAPSULE -> R.string.form_capsule
    MedicationForm.INJECTION -> R.string.form_injection
    MedicationForm.LIQUID -> R.string.form_liquid
    MedicationForm.CREAM -> R.string.form_cream
    MedicationForm.INHALER -> R.string.form_inhaler
    MedicationForm.PATCH -> R.string.form_patch
    MedicationForm.OTHER -> R.string.form_other
}

@StringRes
fun ScheduleType.labelRes(): Int = when (this) {
    ScheduleType.EVERY_DAY -> R.string.schedule_type_every_day
    ScheduleType.DAYS_OF_WEEK -> R.string.schedule_type_days_of_week
    ScheduleType.EVERY_N_DAYS -> R.string.schedule_type_every_n_days
    ScheduleType.WEEKLY -> R.string.schedule_type_weekly
    ScheduleType.CYCLE -> R.string.schedule_type_cycle
    ScheduleType.AS_NEEDED -> R.string.schedule_type_as_needed
}
