package club.hiraeth.flareenough.data.repository

import club.hiraeth.flareenough.data.db.dao.DoseDao
import club.hiraeth.flareenough.data.db.dao.MedicationDao
import club.hiraeth.flareenough.data.db.entity.DoseEventEntity
import club.hiraeth.flareenough.data.db.entity.DoseStatus
import club.hiraeth.flareenough.data.db.entity.LoggedVia
import kotlinx.coroutines.flow.Flow

/**
 * Records and reads dose events. Logging a taken dose also reduces stock when the
 * medication tracks stock. Nothing here judges a missed dose; it is just a record.
 */
class DoseRepository(
    private val doseDao: DoseDao,
    private val medicationDao: MedicationDao,
) {

    fun observeForMedication(medicationId: Long): Flow<List<DoseEventEntity>> =
        doseDao.observeForMedication(medicationId)

    fun observeLastForMedication(medicationId: Long): Flow<DoseEventEntity?> =
        doseDao.observeLastForMedication(medicationId)

    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<DoseEventEntity>> =
        doseDao.observeBetween(startMillis, endMillis)

    suspend fun getById(id: Long): DoseEventEntity? = doseDao.getById(id)

    suspend fun countTakenBetween(medicationId: Long, startMillis: Long, endMillis: Long): Int =
        doseDao.countTakenBetween(medicationId, startMillis, endMillis)

    /**
     * Log a dose for a planned slot. If an event already exists for that slot it is
     * updated, so acting twice (for example from a notification and then the app)
     * does not create duplicates. Returns the event id.
     */
    suspend fun logForSlot(
        medicationId: Long,
        scheduledTimeMillis: Long?,
        status: DoseStatus,
        actualTimeMillis: Long?,
        loggedVia: LoggedVia,
        nowMillis: Long,
    ): Long {
        val existing = scheduledTimeMillis?.let { doseDao.findForSlot(medicationId, it) }
        val id: Long
        if (existing == null) {
            id = doseDao.insert(
                DoseEventEntity(
                    medicationId = medicationId,
                    scheduledTimeMillis = scheduledTimeMillis,
                    status = status,
                    actualTimeMillis = actualTimeMillis,
                    loggedVia = loggedVia,
                    createdAtMillis = nowMillis,
                ),
            )
        } else {
            id = existing.id
            doseDao.update(
                existing.copy(
                    status = status,
                    actualTimeMillis = actualTimeMillis,
                    loggedVia = loggedVia,
                ),
            )
        }
        if (status == DoseStatus.TAKEN) decrementStockIfTracked(medicationId)
        return id
    }

    /** Undo a logged dose within the undo window. */
    suspend fun undo(doseId: Long) {
        val dose = doseDao.getById(doseId) ?: return
        doseDao.delete(dose)
        if (dose.status == DoseStatus.TAKEN) incrementStockIfTracked(dose.medicationId)
    }

    private suspend fun decrementStockIfTracked(medicationId: Long) {
        val med = medicationDao.getWithTimes(medicationId)?.medication ?: return
        val stock = med.stockCount ?: return
        medicationDao.setStock(medicationId, (stock - 1).coerceAtLeast(0))
    }

    private suspend fun incrementStockIfTracked(medicationId: Long) {
        val med = medicationDao.getWithTimes(medicationId)?.medication ?: return
        val stock = med.stockCount ?: return
        medicationDao.setStock(medicationId, stock + 1)
    }
}
