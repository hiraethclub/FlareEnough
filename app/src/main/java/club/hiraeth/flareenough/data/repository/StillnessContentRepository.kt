package club.hiraeth.flareenough.data.repository

import club.hiraeth.flareenough.data.db.dao.StillnessContentDao
import club.hiraeth.flareenough.data.db.entity.PromptEntity
import club.hiraeth.flareenough.data.db.entity.ReadingEntity
import kotlinx.coroutines.flow.Flow

/** The person's own stillness prompts and readings. */
class StillnessContentRepository(private val dao: StillnessContentDao) {

    fun observePrompts(): Flow<List<PromptEntity>> = dao.observePrompts()

    suspend fun addPrompt(text: String, nowMillis: Long) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        dao.insertPrompt(PromptEntity(text = trimmed, createdAtMillis = nowMillis))
    }

    suspend fun deletePrompt(prompt: PromptEntity) = dao.deletePrompt(prompt)

    fun observeReadings(): Flow<List<ReadingEntity>> = dao.observeReadings()

    fun observeReading(id: Long): Flow<ReadingEntity?> = dao.observeReading(id)

    suspend fun addReading(title: String, body: String, nowMillis: Long): Long {
        return dao.insertReading(
            ReadingEntity(
                title = title.trim(),
                body = body.trim(),
                createdAtMillis = nowMillis,
            ),
        )
    }

    suspend fun updateReading(reading: ReadingEntity) = dao.updateReading(reading)

    suspend fun deleteReading(reading: ReadingEntity) = dao.deleteReading(reading)
}
