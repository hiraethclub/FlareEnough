package club.hiraeth.flareenough.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import club.hiraeth.flareenough.data.db.entity.MeditationSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeditationDao {

    @Insert
    suspend fun insert(session: MeditationSessionEntity): Long

    @Delete
    suspend fun delete(session: MeditationSessionEntity)

    @Query("SELECT * FROM meditation_sessions ORDER BY startTimeMillis DESC")
    fun observeAll(): Flow<List<MeditationSessionEntity>>

    @Query(
        "SELECT * FROM meditation_sessions WHERE startTimeMillis BETWEEN :startMillis AND :endMillis " +
            "ORDER BY startTimeMillis ASC",
    )
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<MeditationSessionEntity>>
}
