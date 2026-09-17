package club.hiraeth.flareenough.schedule

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TimeOfDayTest {

    @Test
    fun `of builds the right minute count`() {
        assertEquals(0, TimeOfDay.of(0, 0).minutesPastMidnight)
        assertEquals(90, TimeOfDay.of(1, 30).minutesPastMidnight)
        assertEquals(480, TimeOfDay.of(8, 0).minutesPastMidnight)
        assertEquals(1439, TimeOfDay.of(23, 59).minutesPastMidnight)
    }

    @Test
    fun `hour and minute read back correctly`() {
        val t = TimeOfDay.of(1, 30)
        assertEquals(1, t.hour)
        assertEquals(30, t.minute)
    }

    @Test
    fun `toIso pads to two digits`() {
        assertEquals("01:30", TimeOfDay.of(1, 30).toIso())
        assertEquals("08:00", TimeOfDay.of(8, 0).toIso())
        assertEquals("23:05", TimeOfDay.of(23, 5).toIso())
    }

    @Test
    fun `parse round trips with toIso`() {
        val t = TimeOfDay.parse("01:30")
        assertEquals(TimeOfDay.of(1, 30), t)
        assertEquals("01:30", t.toIso())
    }

    @Test
    fun `ordering compares by time`() {
        assertTrue(TimeOfDay.of(1, 30) < TimeOfDay.of(8, 0))
        assertTrue(TimeOfDay.of(23, 59) > TimeOfDay.of(0, 0))
    }

    @Test
    fun `out of range values are rejected`() {
        assertThrows(IllegalArgumentException::class.java) { TimeOfDay(-1) }
        assertThrows(IllegalArgumentException::class.java) { TimeOfDay(1440) }
        assertThrows(IllegalArgumentException::class.java) { TimeOfDay.of(24, 0) }
        assertThrows(IllegalArgumentException::class.java) { TimeOfDay.of(8, 60) }
    }

    @Test
    fun `parse rejects malformed input`() {
        assertThrows(IllegalArgumentException::class.java) { TimeOfDay.parse("8am") }
        assertThrows(IllegalStateException::class.java) { TimeOfDay.parse("aa:bb") }
    }
}
