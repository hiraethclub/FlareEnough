package club.hiraeth.flareenough.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import club.hiraeth.flareenough.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Backs the toggles on the Settings screen, such as turning the period tracker on. */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val periodTrackingEnabled: StateFlow<Boolean> =
        settingsRepository.observePeriodTrackingEnabled()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setPeriodTrackingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPeriodTrackingEnabled(enabled)
        }
    }
}
