package club.hiraeth.flareenough.data.repository

import club.hiraeth.flareenough.data.db.dao.MeditationDao
import club.hiraeth.flareenough.data.db.entity.MeditationSessionEntity
import club.hiraeth.flareenough.data.db.entity.SessionType
import kotlinx.coroutines.flow.Flow

/** Records and reads stillness sessions. Duration only, no streaks or targets. */
class MeditationRepository(private val dao: MeditationDao) {

    fun observeAll(): Flow<List<MeditationSessionEntity>> = dao.observeAll()

    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<MeditationSessionEntity>> =
        dao.observeBetween(startMillis, endMillis)

    suspend fun log(
        startTimeMillis: Long,
        durationSeconds: Int,
        type: SessionType,
        nowMillis: Long,
    ): Long =
        dao.insert(
            MeditationSessionEntity(
                startTimeMillis = startTimeMillis,
                durationSeconds = durationSeconds,
                type = type,
                createdAtMillis = nowMillis,
            ),
        )
}
