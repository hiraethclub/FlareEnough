package club.hiraeth.flareenough.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import club.hiraeth.flareenough.data.db.entity.BodyMapEntryEntity
import club.hiraeth.flareenough.data.db.entity.DayNoteEntity
import club.hiraeth.flareenough.data.db.entity.DayTagCrossRef
import club.hiraeth.flareenough.data.db.entity.FlareDayEntity
import club.hiraeth.flareenough.data.db.entity.TagEntity
import kotlinx.coroutines.flow.Flow

/** Day level data: flare flag, body map, notes, and tags. */
@Dao
interface DayDao {

    // Flare flag.

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setFlareDay(flareDay: FlareDayEntity)

    @Query("SELECT flare FROM flare_days WHERE epochDay = :epochDay")
    fun observeFlare(epochDay: Long): Flow<Boolean?>

    @Query("SELECT epochDay FROM flare_days WHERE flare = 1 AND epochDay BETWEEN :startEpochDay AND :endEpochDay")
    fun observeFlareDaysBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<Long>>

    // Body map.

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBodyRegion(entry: BodyMapEntryEntity)

    @Query("DELETE FROM body_map_entries WHERE epochDay = :epochDay AND region = :region")
    suspend fun clearBodyRegion(epochDay: Long, region: String)

    @Query("SELECT * FROM body_map_entries WHERE epochDay = :epochDay")
    fun observeBodyMap(epochDay: Long): Flow<List<BodyMapEntryEntity>>

    @Query("SELECT * FROM body_map_entries WHERE epochDay = :epochDay")
    suspend fun getBodyMap(epochDay: Long): List<BodyMapEntryEntity>

    // Notes.

    @Insert
    suspend fun insertNote(note: DayNoteEntity): Long

    @Delete
    suspend fun deleteNote(note: DayNoteEntity)

    @Query("SELECT * FROM day_notes WHERE epochDay = :epochDay ORDER BY createdAtMillis ASC")
    fun observeNotes(epochDay: Long): Flow<List<DayNoteEntity>>

    // Tags.

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Query("SELECT * FROM tags ORDER BY name COLLATE NOCASE ASC")
    fun observeAllTags(): Flow<List<TagEntity>>

    @Query("SELECT id FROM tags WHERE name = :name LIMIT 1")
    suspend fun findTagIdByName(name: String): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToDay(link: DayTagCrossRef)

    @Delete
    suspend fun removeTagFromDay(link: DayTagCrossRef)

    @Query(
        "SELECT t.* FROM tags t INNER JOIN day_tags d ON t.id = d.tagId WHERE d.epochDay = :epochDay " +
            "ORDER BY t.name COLLATE NOCASE ASC",
    )
    fun observeTagsForDay(epochDay: Long): Flow<List<TagEntity>>
}
