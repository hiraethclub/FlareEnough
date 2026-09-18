package club.hiraeth.flareenough.schedule

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Tests for the recurrence logic and occurrence search. These use UTC so the focus
 * is on which days and times are due, with no clock change effects. Daylight saving
 * has its own test.
 */
class ScheduleEngineTest {

    private val utc = ZoneId.of("UTC")
    private val at8 = listOf(TimeOfDay.of(8, 0))

    @Test
    fun `daily is due every day`() {
        val schedule = MedicationSchedule(Recurrence.Daily, at8)
        assertTrue(schedule.isDueOn(LocalDate.of(2026, 6, 1)))
        assertTrue(schedule.isDueOn(LocalDate.of(2026, 6, 2)))
    }

    @Test
    fun `days of week is due only on chosen days`() {
        // Monday, Wednesday, Friday.
        val schedule = MedicationSchedule(
            Recurrence.DaysOfWeek(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)),
            at8,
        )
        // 2026-06-01 is a Monday.
        assertTrue(schedule.isDueOn(LocalDate.of(2026, 6, 1)))  // Mon
        assertFalse(schedule.isDueOn(LocalDate.of(2026, 6, 2))) // Tue
        assertTrue(schedule.isDueOn(LocalDate.of(2026, 6, 3)))  // Wed
        assertFalse(schedule.isDueOn(LocalDate.of(2026, 6, 4))) // Thu
        assertTrue(schedule.isDueOn(LocalDate.of(2026, 6, 5)))  // Fri
    }

    @Test
    fun `every n days counts from the anchor`() {
        val anchor = LocalDate.of(2026, 6, 1)
        val schedule = MedicationSchedule(Recurrence.EveryNDays(3, anchor), at8)
        assertTrue(schedule.isDueOn(anchor))
        assertFalse(schedule.isDueOn(anchor.plusDays(1)))
        assertFalse(schedule.isDueOn(anchor.plusDays(2)))
        assertTrue(schedule.isDueOn(anchor.plusDays(3)))
        assertTrue(schedule.isDueOn(anchor.plusDays(6)))
        // Before the anchor is never due.
        assertFalse(schedule.isDueOn(anchor.minusDays(3)))
    }

    @Test
    fun `cycle is due during the on days and not the off days`() {
        val anchor = LocalDate.of(2026, 6, 1)
        // 3 on, 2 off, cycle length 5.
        val schedule = MedicationSchedule(Recurrence.Cycle(daysOn = 3, daysOff = 2, anchor = anchor), at8)
        assertTrue(schedule.isDueOn(anchor))            // day 0, on
        assertTrue(schedule.isDueOn(anchor.plusDays(2))) // day 2, on
        assertFalse(schedule.isDueOn(anchor.plusDays(3))) // day 3, off
        assertFalse(schedule.isDueOn(anchor.plusDays(4))) // day 4, off
        assertTrue(schedule.isDueOn(anchor.plusDays(5))) // day 5, on again
    }

    @Test
    fun `start and end dates bound the schedule`() {
        val schedule = MedicationSchedule(
            recurrence = Recurrence.Daily,
            times = at8,
            startDate = LocalDate.of(2026, 6, 10),
            endDate = LocalDate.of(2026, 6, 20),
        )
        assertFalse(schedule.isDueOn(LocalDate.of(2026, 6, 9)))
        assertTrue(schedule.isDueOn(LocalDate.of(2026, 6, 10)))
        assertTrue(schedule.isDueOn(LocalDate.of(2026, 6, 20)))
        assertFalse(schedule.isDueOn(LocalDate.of(2026, 6, 21)))
    }

    @Test
    fun `as needed has no occurrences`() {
        val schedule = MedicationSchedule(Recurrence.AsNeeded, emptyList())
        assertNull(ScheduleEngine.nextOccurrence(schedule, utc, Instant.parse("2026-06-01T00:00:00Z")))
        assertTrue(
            ScheduleEngine.occurrencesBetween(
                schedule,
                utc,
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-07-01T00:00:00Z"),
            ).isEmpty(),
        )
    }

    @Test
    fun `next occurrence finds the following day when today's time has passed`() {
        val schedule = MedicationSchedule(Recurrence.Daily, at8)
        val after = Instant.parse("2026-06-01T09:00:00Z") // past 08:00 today
        val next = ScheduleEngine.nextOccurrence(schedule, utc, after)
        assertEquals(Instant.parse("2026-06-02T08:00:00Z"), next)
    }

    @Test
    fun `next occurrence finds today when the time is still ahead`() {
        val schedule = MedicationSchedule(Recurrence.Daily, at8)
        val after = Instant.parse("2026-06-01T07:00:00Z") // before 08:00 today
        val next = ScheduleEngine.nextOccurrence(schedule, utc, after)
        assertEquals(Instant.parse("2026-06-01T08:00:00Z"), next)
    }

    @Test
    fun `occurrences between returns every dose in the window in order`() {
        val schedule = MedicationSchedule(
            Recurrence.Daily,
            listOf(TimeOfDay.of(8, 0), TimeOfDay.of(20, 0)),
        )
        val from = Instant.parse("2026-06-01T00:00:00Z")
        val to = Instant.parse("2026-06-03T00:00:00Z") // two full days
        val result = ScheduleEngine.occurrencesBetween(schedule, utc, from, to)
        assertEquals(
            listOf(
                Instant.parse("2026-06-01T08:00:00Z"),
                Instant.parse("2026-06-01T20:00:00Z"),
                Instant.parse("2026-06-02T08:00:00Z"),
                Instant.parse("2026-06-02T20:00:00Z"),
            ),
            result,
        )
    }
}
