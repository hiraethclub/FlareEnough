package club.hiraeth.flareenough.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import club.hiraeth.flareenough.data.db.entity.BodyRegion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * App settings, kept in DataStore. Small preferences that are not medical records
 * live here rather than in the database.
 *
 * Body part visibility is stored as the set of hidden region names. Anything not in
 * that set is shown, so a fresh install shows every body part by default.
 */
class SettingsRepository(context: Context) {

    private val store = context.applicationContext.settingsDataStore

    fun observeHiddenBodyRegions(): Flow<Set<BodyRegion>> =
        store.data.map { prefs ->
            (prefs[HIDDEN_BODY_REGIONS] ?: emptySet())
                .mapNotNull { name -> runCatching { BodyRegion.valueOf(name) }.getOrNull() }
                .toSet()
        }

    suspend fun setBodyRegionVisible(region: BodyRegion, visible: Boolean) {
        store.edit { prefs ->
            val current = prefs[HIDDEN_BODY_REGIONS]?.toMutableSet() ?: mutableSetOf()
            if (visible) current.remove(region.name) else current.add(region.name)
            prefs[HIDDEN_BODY_REGIONS] = current
        }
    }

    private companion object {
        val HIDDEN_BODY_REGIONS = stringSetPreferencesKey("hidden_body_regions")
    }
}
