package club.hiraeth.flareenough.schedule

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * UK daylight saving tests. In 2026 the clocks go forward on 29 March (01:00 GMT
 * becomes 02:00 BST) and back on 25 October (02:00 BST becomes 01:00 GMT). These
 * tests pin down that dose times behave sensibly across both, including the awkward
 * 01:30 that either does not exist or happens twice.
 */
class DstTest {

    private val london = ZoneId.of("Europe/London")

    private fun localTimeOf(instant: Instant): LocalTime =
        instant.atZone(london).toLocalTime()

    @Test
    fun `a normal 8am dose keeps its wall clock time on both sides of spring forward`() {
        val before = ScheduleEngine.instantFor(LocalDate.of(2026, 3, 28), TimeOfDay.of(8, 0), london)
        val after = ScheduleEngine.instantFor(LocalDate.of(2026, 3, 30), TimeOfDay.of(8, 0), london)
        // The person still takes it at 08:00 by the clock, even though the UTC instant
        // shifts by an hour when the clocks change.
        assertEquals(LocalTime.of(8, 0), localTimeOf(before))
        assertEquals(LocalTime.of(8, 0), localTimeOf(after))
        // Winter 08:00 is 08:00 UTC, summer 08:00 is 07:00 UTC.
        assertEquals(Instant.parse("2026-03-28T08:00:00Z"), before)
        assertEquals(Instant.parse("2026-03-30T07:00:00Z"), after)
    }

    @Test
    fun `a 130am dose on spring forward day is moved to 230, not skipped`() {
        // 01:30 does not exist on 29 March 2026. It should move forward to 02:30 BST,
        // which is 01:30 UTC, rather than vanish.
        val instant = ScheduleEngine.instantFor(LocalDate.of(2026, 3, 29), TimeOfDay.of(1, 30), london)
        assertEquals(LocalTime.of(2, 30), localTimeOf(instant))
        assertEquals(Instant.parse("2026-03-29T01:30:00Z"), instant)
    }

    @Test
    fun `a 130am dose on spring forward day still produces exactly one occurrence`() {
        val schedule = MedicationSchedule(Recurrence.Daily, listOf(TimeOfDay.of(1, 30)))
        val occurrences = ScheduleEngine.occurrencesBetween(
            schedule,
            london,
            Instant.parse("2026-03-29T00:00:00Z"),
            Instant.parse("2026-03-30T00:00:00Z"),
        )
        assertEquals(1, occurrences.size)
        assertEquals(Instant.parse("2026-03-29T01:30:00Z"), occurrences.first())
    }

    @Test
    fun `a 130am dose on autumn back day fires once, at the first 130`() {
        // 01:30 happens twice on 25 October 2026. The dose should fire once, at the
        // earlier 01:30 (BST, which is 00:30 UTC), not twice.
        val instant = ScheduleEngine.instantFor(LocalDate.of(2026, 10, 25), TimeOfDay.of(1, 30), london)
        assertEquals(Instant.parse("2026-10-25T00:30:00Z"), instant)
    }

    @Test
    fun `a 130am dose on autumn back day produces exactly one occurrence`() {
        val schedule = MedicationSchedule(Recurrence.Daily, listOf(TimeOfDay.of(1, 30)))
        val occurrences = ScheduleEngine.occurrencesBetween(
            schedule,
            london,
            Instant.parse("2026-10-25T00:00:00Z"),
            Instant.parse("2026-10-26T00:00:00Z"),
        )
        assertEquals(1, occurrences.size)
        assertEquals(Instant.parse("2026-10-25T00:30:00Z"), occurrences.first())
    }

    @Test
    fun `next occurrence steps across the spring forward correctly`() {
        val schedule = MedicationSchedule(Recurrence.Daily, listOf(TimeOfDay.of(8, 0)))
        // Just after 08:00 on 28 March, the next dose is 08:00 on 29 March, which is
        // 07:00 UTC because the clocks went forward.
        val after = Instant.parse("2026-03-28T08:30:00Z")
        val next = ScheduleEngine.nextOccurrence(schedule, london, after)
        assertEquals(Instant.parse("2026-03-29T07:00:00Z"), next)
        assertEquals(LocalTime.of(8, 0), localTimeOf(next!!))
    }
}
