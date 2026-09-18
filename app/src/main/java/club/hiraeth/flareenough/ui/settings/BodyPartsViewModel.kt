package club.hiraeth.flareenough.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import club.hiraeth.flareenough.data.db.entity.BodyRegion
import club.hiraeth.flareenough.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Lets the person choose which body parts appear in symptom logging. A hidden part
 * is simply not shown when logging, so the list stays short and calm for anyone who
 * only tracks a few places.
 */
class BodyPartsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val hiddenRegions: StateFlow<Set<BodyRegion>> =
        settingsRepository.observeHiddenBodyRegions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun setVisible(region: BodyRegion, visible: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBodyRegionVisible(region, visible)
        }
    }
}
