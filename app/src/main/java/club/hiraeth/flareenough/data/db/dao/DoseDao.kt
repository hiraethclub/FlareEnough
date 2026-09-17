package club.hiraeth.flareenough.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import club.hiraeth.flareenough.data.db.entity.DoseEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseDao {

    @Insert
    suspend fun insert(dose: DoseEventEntity): Long

    @Update
    suspend fun update(dose: DoseEventEntity)

    @Delete
    suspend fun delete(dose: DoseEventEntity)

    @Query("SELECT * FROM dose_events WHERE id = :id")
    suspend fun getById(id: Long): DoseEventEntity?

    /** Events for a medication, most recent actual time first. */
    @Query(
        "SELECT * FROM dose_events WHERE medicationId = :medicationId " +
            "ORDER BY COALESCE(actualTimeMillis, scheduledTimeMillis, createdAtMillis) DESC",
    )
    fun observeForMedication(medicationId: Long): Flow<List<DoseEventEntity>>

    /** The single most recent event for a medication, used for as needed gap notices. */
    @Query(
        "SELECT * FROM dose_events WHERE medicationId = :medicationId " +
            "ORDER BY COALESCE(actualTimeMillis, scheduledTimeMillis, createdAtMillis) DESC LIMIT 1",
    )
    fun observeLastForMedication(medicationId: Long): Flow<DoseEventEntity?>

    /** All events whose actual or scheduled time falls in a window, for the timeline and charts. */
    @Query(
        "SELECT * FROM dose_events " +
            "WHERE COALESCE(actualTimeMillis, scheduledTimeMillis, createdAtMillis) BETWEEN :startMillis AND :endMillis " +
            "ORDER BY COALESCE(actualTimeMillis, scheduledTimeMillis, createdAtMillis) ASC",
    )
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<DoseEventEntity>>

    /** Find an existing event for a planned slot, so acting twice updates rather than duplicates. */
    @Query(
        "SELECT * FROM dose_events WHERE medicationId = :medicationId " +
            "AND scheduledTimeMillis = :scheduledTimeMillis LIMIT 1",
    )
    suspend fun findForSlot(medicationId: Long, scheduledTimeMillis: Long): DoseEventEntity?

    /** Count of as needed doses taken for a medication within a time window (its daily max check). */
    @Query(
        "SELECT COUNT(*) FROM dose_events WHERE medicationId = :medicationId AND status = 'TAKEN' " +
            "AND actualTimeMillis BETWEEN :startMillis AND :endMillis",
    )
    suspend fun countTakenBetween(medicationId: Long, startMillis: Long, endMillis: Long): Int
}
