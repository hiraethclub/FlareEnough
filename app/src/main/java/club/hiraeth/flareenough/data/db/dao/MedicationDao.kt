package club.hiraeth.flareenough.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import club.hiraeth.flareenough.data.db.entity.MedicationEntity
import club.hiraeth.flareenough.data.db.entity.ScheduleTimeEntity
import club.hiraeth.flareenough.data.db.relation.MedicationWithTimes
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Insert
    suspend fun insert(medication: MedicationEntity): Long

    @Update
    suspend fun update(medication: MedicationEntity)

    @Delete
    suspend fun delete(medication: MedicationEntity)

    @Query("UPDATE medications SET paused = :paused WHERE id = :id")
    suspend fun setPaused(id: Long, paused: Boolean)

    @Query("UPDATE medications SET stockCount = :stock WHERE id = :id")
    suspend fun setStock(id: Long, stock: Int?)

    @Query("SELECT * FROM medications WHERE id = :id")
    fun observeById(id: Long): Flow<MedicationEntity?>

    @Transaction
    @Query("SELECT * FROM medications WHERE id = :id")
    fun observeWithTimes(id: Long): Flow<MedicationWithTimes?>

    @Transaction
    @Query("SELECT * FROM medications ORDER BY sortOrder ASC, name COLLATE NOCASE ASC")
    fun observeAllWithTimes(): Flow<List<MedicationWithTimes>>

    @Transaction
    @Query("SELECT * FROM medications WHERE paused = 0 ORDER BY sortOrder ASC, name COLLATE NOCASE ASC")
    fun observeActiveWithTimes(): Flow<List<MedicationWithTimes>>

    @Transaction
    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getWithTimes(id: Long): MedicationWithTimes?

    // Schedule times are managed together with their medication.

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimes(times: List<ScheduleTimeEntity>)

    @Query("DELETE FROM schedule_times WHERE medicationId = :medicationId")
    suspend fun deleteTimesFor(medicationId: Long)

    /**
     * Replace a medication's times in one transaction: clear the old, insert the
     * new. Used when saving an edited schedule.
     */
    @Transaction
    suspend fun replaceTimes(medicationId: Long, times: List<ScheduleTimeEntity>) {
        deleteTimesFor(medicationId)
        if (times.isNotEmpty()) insertTimes(times)
    }
}
