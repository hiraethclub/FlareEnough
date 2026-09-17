package club.hiraeth.flareenough.ui.medication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import club.hiraeth.flareenough.data.repository.MedicationRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Holds the editable state for adding or editing one medication. [medId] is 0 for
 * a new medication, otherwise the id to load and edit.
 */
class MedicationEditViewModel(
    private val repository: MedicationRepository,
    private val medId: Long,
) : ViewModel() {

    var form by mutableStateOf(MedicationFormState())
        private set

    /** True once an existing medication has finished loading, or when adding new. */
    var ready by mutableStateOf(medId == 0L)
        private set

    private var createdAtMillis: Long = System.currentTimeMillis()

    init {
        if (medId != 0L) {
            viewModelScope.launch {
                repository.get(medId)?.let {
                    form = MedicationFormState.fromEntity(it)
                    createdAtMillis = it.medication.createdAtMillis
                }
                ready = true
            }
        }
    }

    fun update(transform: (MedicationFormState) -> MedicationFormState) {
        form = transform(form)
    }

    fun save(onDone: () -> Unit) {
        if (!form.canSave) return
        val today = LocalDate.now().toEpochDay()
        val now = System.currentTimeMillis()
        val (entity, times) = form.toEntityAndTimes(
            todayEpochDay = today,
            nowMillis = now,
            createdAtMillis = if (form.isNew) now else createdAtMillis,
            sortOrder = 0,
        )
        viewModelScope.launch {
            if (form.isNew) {
                repository.create(entity, times)
            } else {
                repository.update(entity, times)
            }
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        if (form.isNew) {
            onDone()
            return
        }
        viewModelScope.launch {
            repository.get(medId)?.let { repository.delete(it.medication) }
            onDone()
        }
    }
}
