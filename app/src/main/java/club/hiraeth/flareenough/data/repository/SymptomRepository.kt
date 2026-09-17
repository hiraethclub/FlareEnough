package club.hiraeth.flareenough.data.repository

import club.hiraeth.flareenough.data.db.dao.SymptomDao
import club.hiraeth.flareenough.data.db.entity.SymptomEntryEntity
import club.hiraeth.flareenough.data.db.entity.SymptomTrackerEntity
import kotlinx.coroutines.flow.Flow

/** Reads and writes symptom trackers and their daily entries. */
class SymptomRepository(private val dao: SymptomDao) {

    fun observeAllTrackers(): Flow<List<SymptomTrackerEntity>> = dao.observeAllTrackers()

    fun observeVisibleTrackers(): Flow<List<SymptomTrackerEntity>> = dao.observeVisibleTrackers()

    suspend fun trackerCount(): Int = dao.trackerCount()

    suspend fun addTracker(tracker: SymptomTrackerEntity): Long = dao.insertTracker(tracker)

    suspend fun addTrackers(trackers: List<SymptomTrackerEntity>) = dao.insertTrackers(trackers)

    suspend fun updateTracker(tracker: SymptomTrackerEntity) = dao.updateTracker(tracker)

    suspend fun deleteTracker(tracker: SymptomTrackerEntity) = dao.deleteTracker(tracker)

    fun observeEntriesForDay(epochDay: Long): Flow<List<SymptomEntryEntity>> =
        dao.observeEntriesForDay(epochDay)

    suspend fun getEntriesForDay(epochDay: Long): List<SymptomEntryEntity> =
        dao.getEntriesForDay(epochDay)

    fun observeEntriesBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<SymptomEntryEntity>> =
        dao.observeEntriesBetween(startEpochDay, endEpochDay)

    fun observeEntriesForTrackerBetween(
        trackerId: Long,
        startEpochDay: Long,
        endEpochDay: Long,
    ): Flow<List<SymptomEntryEntity>> =
        dao.observeEntriesForTrackerBetween(trackerId, startEpochDay, endEpochDay)

    /**
     * Save a value for a tracker on a day. If a value already exists for that
     * tracker and day it is updated, so the latest value wins rather than piling up.
     */
    suspend fun setValue(
        trackerId: Long,
        epochDay: Long,
        valueInt: Int?,
        valueText: String?,
        nowMillis: Long,
    ) {
        val existing = dao.getEntry(trackerId, epochDay)
        if (existing == null) {
            dao.insertEntry(
                SymptomEntryEntity(
                    trackerId = trackerId,
                    epochDay = epochDay,
                    valueInt = valueInt,
                    valueText = valueText,
                    createdAtMillis = nowMillis,
                ),
            )
        } else {
            dao.updateEntry(existing.copy(valueInt = valueInt, valueText = valueText))
        }
    }

    /** Copy one day's entries onto another day, for the "same as yesterday" button. */
    suspend fun copyDay(fromEpochDay: Long, toEpochDay: Long, nowMillis: Long) {
        val source = dao.getEntriesForDay(fromEpochDay)
        for (entry in source) {
            setValue(entry.trackerId, toEpochDay, entry.valueInt, entry.valueText, nowMillis)
        }
    }
}
