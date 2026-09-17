package club.hiraeth.flareenough.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import club.hiraeth.flareenough.data.db.entity.SymptomEntryEntity
import club.hiraeth.flareenough.data.db.entity.SymptomTrackerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDao {

    // Trackers.

    @Insert
    suspend fun insertTracker(tracker: SymptomTrackerEntity): Long

    @Insert
    suspend fun insertTrackers(trackers: List<SymptomTrackerEntity>)

    @Update
    suspend fun updateTracker(tracker: SymptomTrackerEntity)

    @Delete
    suspend fun deleteTracker(tracker: SymptomTrackerEntity)

    @Query("SELECT * FROM symptom_trackers ORDER BY sortOrder ASC, name COLLATE NOCASE ASC")
    fun observeAllTrackers(): Flow<List<SymptomTrackerEntity>>

    @Query("SELECT * FROM symptom_trackers WHERE hidden = 0 ORDER BY sortOrder ASC, name COLLATE NOCASE ASC")
    fun observeVisibleTrackers(): Flow<List<SymptomTrackerEntity>>

    @Query("SELECT COUNT(*) FROM symptom_trackers")
    suspend fun trackerCount(): Int

    // Entries.

    @Insert
    suspend fun insertEntry(entry: SymptomEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: SymptomEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: SymptomEntryEntity)

    @Query("SELECT * FROM symptom_entries WHERE epochDay = :epochDay")
    fun observeEntriesForDay(epochDay: Long): Flow<List<SymptomEntryEntity>>

    @Query("SELECT * FROM symptom_entries WHERE epochDay = :epochDay")
    suspend fun getEntriesForDay(epochDay: Long): List<SymptomEntryEntity>

    @Query("SELECT * FROM symptom_entries WHERE trackerId = :trackerId AND epochDay = :epochDay LIMIT 1")
    suspend fun getEntry(trackerId: Long, epochDay: Long): SymptomEntryEntity?

    @Query(
        "SELECT * FROM symptom_entries WHERE trackerId = :trackerId " +
            "AND epochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY epochDay ASC",
    )
    fun observeEntriesForTrackerBetween(
        trackerId: Long,
        startEpochDay: Long,
        endEpochDay: Long,
    ): Flow<List<SymptomEntryEntity>>

    @Query(
        "SELECT * FROM symptom_entries WHERE epochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY epochDay ASC",
    )
    fun observeEntriesBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<SymptomEntryEntity>>
}
