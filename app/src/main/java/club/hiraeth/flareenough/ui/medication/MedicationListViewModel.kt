package club.hiraeth.flareenough.ui.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import club.hiraeth.flareenough.data.db.relation.MedicationWithTimes
import club.hiraeth.flareenough.data.repository.MedicationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicationListViewModel(
    private val repository: MedicationRepository,
) : ViewModel() {

    val medications: StateFlow<List<MedicationWithTimes>> =
        repository.observeAll().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun setPaused(id: Long, paused: Boolean) {
        viewModelScope.launch { repository.setPaused(id, paused) }
    }
}
