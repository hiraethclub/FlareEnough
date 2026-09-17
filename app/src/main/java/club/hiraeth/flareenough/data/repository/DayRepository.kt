package club.hiraeth.flareenough.data.repository

import club.hiraeth.flareenough.data.db.dao.DayDao
import club.hiraeth.flareenough.data.db.entity.BodyMapEntryEntity
import club.hiraeth.flareenough.data.db.entity.BodyState
import club.hiraeth.flareenough.data.db.entity.DayNoteEntity
import club.hiraeth.flareenough.data.db.entity.DayTagCrossRef
import club.hiraeth.flareenough.data.db.entity.FlareDayEntity
import club.hiraeth.flareenough.data.db.entity.TagEntity
import club.hiraeth.flareenough.data.db.entity.BodyRegion
import kotlinx.coroutines.flow.Flow

/** Day level data: flare flag, body map, notes, and tags. */
class DayRepository(private val dao: DayDao) {

    // Flare.

    fun observeFlare(epochDay: Long): Flow<Boolean?> = dao.observeFlare(epochDay)

    fun observeFlareDaysBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<Long>> =
        dao.observeFlareDaysBetween(startEpochDay, endEpochDay)

    suspend fun setFlare(epochDay: Long, flare: Boolean, nowMillis: Long) =
        dao.setFlareDay(FlareDayEntity(epochDay = epochDay, flare = flare, createdAtMillis = nowMillis))

    // Body map.

    fun observeBodyMap(epochDay: Long): Flow<List<BodyMapEntryEntity>> = dao.observeBodyMap(epochDay)

    /**
     * Cycle a region's mark: nothing to sore, sore to swollen, swollen to clear.
     * This matches the tap behaviour of the body map and the list fallback.
     */
    suspend fun cycleBodyRegion(epochDay: Long, region: BodyRegion, nowMillis: Long) {
        val current = dao.getBodyMap(epochDay).firstOrNull { it.region == region }
        when (current?.state) {
            null -> dao.setBodyRegion(
                BodyMapEntryEntity(
                    epochDay = epochDay,
                    region = region,
                    state = BodyState.SORE,
                    createdAtMillis = nowMillis,
                ),
            )
            BodyState.SORE -> dao.setBodyRegion(current.copy(state = BodyState.SWOLLEN))
            BodyState.SWOLLEN -> dao.clearBodyRegion(epochDay, region.name)
        }
    }

    // Notes.

    fun observeNotes(epochDay: Long): Flow<List<DayNoteEntity>> = dao.observeNotes(epochDay)

    suspend fun addNote(epochDay: Long, text: String, nowMillis: Long): Long =
        dao.insertNote(DayNoteEntity(epochDay = epochDay, text = text, createdAtMillis = nowMillis))

    suspend fun deleteNote(note: DayNoteEntity) = dao.deleteNote(note)

    // Tags.

    fun observeAllTags(): Flow<List<TagEntity>> = dao.observeAllTags()

    fun observeTagsForDay(epochDay: Long): Flow<List<TagEntity>> = dao.observeTagsForDay(epochDay)

    /** Attach a tag to a day, creating the tag if it does not exist yet. */
    suspend fun addTagToDay(epochDay: Long, tagName: String, nowMillis: Long) {
        val trimmed = tagName.trim()
        if (trimmed.isEmpty()) return
        dao.insertTag(TagEntity(name = trimmed, createdAtMillis = nowMillis))
        val id = dao.findTagIdByName(trimmed) ?: return
        dao.addTagToDay(DayTagCrossRef(epochDay = epochDay, tagId = id))
    }

    suspend fun removeTagFromDay(epochDay: Long, tagId: Long) =
        dao.removeTagFromDay(DayTagCrossRef(epochDay = epochDay, tagId = tagId))
}
