package club.hiraeth.flareenough.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import club.hiraeth.flareenough.data.db.entity.PromptEntity
import club.hiraeth.flareenough.data.db.entity.ReadingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StillnessContentDao {

    // Prompts the person added.

    @Insert
    suspend fun insertPrompt(prompt: PromptEntity): Long

    @Delete
    suspend fun deletePrompt(prompt: PromptEntity)

    @Query("SELECT * FROM stillness_prompts ORDER BY createdAtMillis ASC")
    fun observePrompts(): Flow<List<PromptEntity>>

    // Readings the person added.

    @Insert
    suspend fun insertReading(reading: ReadingEntity): Long

    @Update
    suspend fun updateReading(reading: ReadingEntity)

    @Delete
    suspend fun deleteReading(reading: ReadingEntity)

    @Query("SELECT * FROM readings ORDER BY sortOrder ASC, createdAtMillis ASC")
    fun observeReadings(): Flow<List<ReadingEntity>>

    @Query("SELECT * FROM readings WHERE id = :id")
    fun observeReading(id: Long): Flow<ReadingEntity?>
}
