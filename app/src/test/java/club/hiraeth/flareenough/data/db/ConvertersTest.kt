package club.hiraeth.flareenough.data.db

import club.hiraeth.flareenough.data.db.entity.BodyRegion
import club.hiraeth.flareenough.data.db.entity.DoseStatus
import club.hiraeth.flareenough.data.db.entity.MedicationForm
import club.hiraeth.flareenough.data.db.entity.ScheduleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pure JVM tests for the Room type converters. These need no Android device, so
 * they run in CI. They guard the round trip for enums and string lists, including
 * the tricky cases of null, empty, and labels that contain commas.
 */
class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `enum round trips by name`() {
        assertEquals(
            MedicationForm.INHALER,
            converters.toMedicationForm(converters.fromMedicationForm(MedicationForm.INHALER)),
        )
        assertEquals(
            ScheduleType.CYCLE,
            converters.toScheduleType(converters.fromScheduleType(ScheduleType.CYCLE)),
        )
        assertEquals(
            DoseStatus.MISSED,
            converters.toDoseStatus(converters.fromDoseStatus(DoseStatus.MISSED)),
        )
        assertEquals(
            BodyRegion.FINGERS_LEFT,
            converters.toBodyRegion(converters.fromBodyRegion(BodyRegion.FINGERS_LEFT)),
        )
    }

    @Test
    fun `enums are stored as their name not ordinal`() {
        assertEquals("INHALER", converters.fromMedicationForm(MedicationForm.INHALER))
        assertEquals("AS_NEEDED", converters.fromScheduleType(ScheduleType.AS_NEEDED))
    }

    @Test
    fun `string list round trips`() {
        val labels = listOf("None", "Mild", "Moderate", "Bad", "Severe")
        val stored = converters.fromStringList(labels)
        assertEquals(labels, converters.toStringList(stored))
    }

    @Test
    fun `string list allows commas and spaces in labels`() {
        val labels = listOf("1 to 2 hours", "over 2 hours, roughly")
        val stored = converters.fromStringList(labels)
        assertEquals(labels, converters.toStringList(stored))
    }

    @Test
    fun `null and empty string lists are handled`() {
        assertNull(converters.fromStringList(null))
        assertNull(converters.toStringList(null))
        assertEquals(emptyList<String>(), converters.toStringList(""))
    }
}
