package club.hiraeth.flareenough.data.repository

import club.hiraeth.flareenough.data.db.dao.MedicationDao
import club.hiraeth.flareenough.data.db.entity.MedicationEntity
import club.hiraeth.flareenough.data.db.entity.ScheduleTimeEntity
import club.hiraeth.flareenough.data.db.relation.MedicationWithTimes
import kotlinx.coroutines.flow.Flow

/**
 * Reads and writes medications and their schedule times. The reminder engine and
 * the medication screens both go through here.
 */
class MedicationRepository(private val dao: MedicationDao) {

    fun observeAll(): Flow<List<MedicationWithTimes>> = dao.observeAllWithTimes()

    fun observeActive(): Flow<List<MedicationWithTimes>> = dao.observeActiveWithTimes()

    fun observe(id: Long): Flow<MedicationWithTimes?> = dao.observeWithTimes(id)

    suspend fun get(id: Long): MedicationWithTimes? = dao.getWithTimes(id)

    /**
     * Create a medication and its times together. Returns the new id. The times are
     * given without ids and without a medication id, which are filled in here.
     */
    suspend fun create(medication: MedicationEntity, times: List<ScheduleTimeEntity>): Long {
        val id = dao.insert(medication)
        if (times.isNotEmpty()) {
            dao.insertTimes(times.map { it.copy(medicationId = id) })
        }
        return id
    }

    /** Update a medication and replace its times in one go. */
    suspend fun update(medication: MedicationEntity, times: List<ScheduleTimeEntity>) {
        dao.update(medication)
        dao.replaceTimes(medication.id, times.map { it.copy(medicationId = medication.id) })
    }

    suspend fun delete(medication: MedicationEntity) = dao.delete(medication)

    suspend fun setPaused(id: Long, paused: Boolean) = dao.setPaused(id, paused)

    suspend fun setStock(id: Long, stock: Int?) = dao.setStock(id, stock)
}
