package club.hiraeth.flareenough.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * App settings, kept in DataStore. Small preferences that are not medical records
 * live here rather than in the database. Body part visibility is a property of each
 * body part now (see BodyPartEntity), not a setting.
 */
class SettingsRepository(context: Context) {

    private val store = context.applicationContext.settingsDataStore

    /**
     * Whether the optional period tracker is on. Off by default, so it appears only
     * for people who choose to turn it on.
     */
    fun observePeriodTrackingEnabled(): Flow<Boolean> =
        store.data.map { prefs -> prefs[PERIOD_TRACKING_ENABLED] ?: false }

    suspend fun setPeriodTrackingEnabled(enabled: Boolean) {
        store.edit { prefs -> prefs[PERIOD_TRACKING_ENABLED] = enabled }
    }

    /**
     * Whether the app asks for the device unlock (biometric, PIN, or pattern) when it
     * opens. Off by default. Nothing about this leaves the device: it uses the system
     * unlock only, and stores no secret of its own.
     */
    fun observeAppLockEnabled(): Flow<Boolean> =
        store.data.map { prefs -> prefs[APP_LOCK_ENABLED] ?: false }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        store.edit { prefs -> prefs[APP_LOCK_ENABLED] = enabled }
    }

    private companion object {
        val PERIOD_TRACKING_ENABLED = booleanPreferencesKey("period_tracking_enabled")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
    }
}
