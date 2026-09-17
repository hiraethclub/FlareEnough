package club.hiraeth.flareenough.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import club.hiraeth.flareenough.data.db.dao.DoseDao
import club.hiraeth.flareenough.data.db.dao.MedicationDao
import club.hiraeth.flareenough.data.db.entity.DoseEventEntity
import club.hiraeth.flareenough.data.db.entity.DoseStatus
import club.hiraeth.flareenough.data.db.entity.MedicationEntity
import club.hiraeth.flareenough.data.db.entity.ScheduleType
import club.hiraeth.flareenough.data.db.entity.ScheduleTimeEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the database. These run on a device or emulator because
 * they use real SQLite. They check that entities save and read back, that the
 * medication to times relation loads, and that deleting a medication cascades to
 * its child rows.
 */
@RunWith(AndroidJUnit4::class)
class FlareDatabaseTest {

    private lateinit var db: FlareDatabase
    private lateinit var medicationDao: MedicationDao
    private lateinit var doseDao: DoseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, FlareDatabase::class.java).build()
        medicationDao = db.medicationDao()
        doseDao = db.doseDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun medicationWithTimesRoundTrips() = runBlocking {
        val medId = medicationDao.insert(
            MedicationEntity(
                name = "Methotrexate",
                strength = "10 mg",
                scheduleType = ScheduleType.WEEKLY,
                daysOfWeekMask = 1 shl 2,
                createdAtMillis = 1000L,
            ),
        )
        medicationDao.insertTimes(
            listOf(
                ScheduleTimeEntity(medicationId = medId, minutesPastMidnight = 8 * 60, sortOrder = 0),
                ScheduleTimeEntity(medicationId = medId, minutesPastMidnight = 20 * 60, sortOrder = 1),
            ),
        )

        val loaded = medicationDao.observeWithTimes(medId).first()
        assertNotNull(loaded)
        assertEquals("Methotrexate", loaded!!.medication.name)
        assertEquals(ScheduleType.WEEKLY, loaded.medication.scheduleType)
        assertEquals(2, loaded.times.size)
    }

    @Test
    fun deletingMedicationCascadesToTimesAndDoses() = runBlocking {
        val med = MedicationEntity(name = "Test", createdAtMillis = 1L)
        val medId = medicationDao.insert(med)
        medicationDao.insertTimes(
            listOf(ScheduleTimeEntity(medicationId = medId, minutesPastMidnight = 9 * 60)),
        )
        doseDao.insert(
            DoseEventEntity(
                medicationId = medId,
                scheduledTimeMillis = 123L,
                status = DoseStatus.TAKEN,
                actualTimeMillis = 130L,
                createdAtMillis = 130L,
            ),
        )

        medicationDao.delete(med.copy(id = medId))

        assertNull(medicationDao.observeWithTimes(medId).first())
        assertEquals(0, doseDao.observeForMedication(medId).first().size)
    }

    @Test
    fun findForSlotMatchesScheduledTime() = runBlocking {
        val medId = medicationDao.insert(MedicationEntity(name = "Test", createdAtMillis = 1L))
        doseDao.insert(
            DoseEventEntity(
                medicationId = medId,
                scheduledTimeMillis = 555L,
                status = DoseStatus.TAKEN,
                createdAtMillis = 1L,
            ),
        )
        assertNotNull(doseDao.findForSlot(medId, 555L))
        assertNull(doseDao.findForSlot(medId, 999L))
    }
}
