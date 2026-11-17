package com.example.habittracker

import com.example.habittracker.domain.Habit
import com.example.habittracker.domain.ReminderPlanner
import com.example.habittracker.domain.ScheduleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ReminderPlannerTest {

    private val today = LocalDate.of(2026, 11, 17) // Tuesday
    private val morning = 8 * 60 // 08:00

    private fun habit(
        schedule: ScheduleType = ScheduleType.DAILY,
        days: Set<DayOfWeek> = Habit.ALL_DAYS,
        enabled: Boolean = true,
        minute: Int = morning,
        archived: Boolean = false,
    ) = Habit(
        id = 1,
        name = "Olahraga",
        scheduleType = schedule,
        daysOfWeek = days,
        reminderEnabled = enabled,
        reminderMinuteOfDay = minute,
        isArchived = archived,
        createdAt = today.minusDays(30),
    )

    @Test
    fun `fires later today when the time has not passed yet`() {
        val now = LocalDateTime.of(today, LocalTime.of(6, 30))
        assertEquals(
            LocalDateTime.of(today, LocalTime.of(8, 0)),
            ReminderPlanner.nextTrigger(habit(), now),
        )
    }

    @Test
    fun `rolls over to tomorrow once the time has passed`() {
        val now = LocalDateTime.of(today, LocalTime.of(9, 0))
        assertEquals(
            LocalDateTime.of(today.plusDays(1), LocalTime.of(8, 0)),
            ReminderPlanner.nextTrigger(habit(), now),
        )
    }

    @Test
    fun `skips days the habit is not scheduled on`() {
        val gym = habit(
            schedule = ScheduleType.SPECIFIC_DAYS,
            days = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
        )
        val now = LocalDateTime.of(today, LocalTime.of(9, 0)) // Tuesday, after the time
        assertEquals(
            // Next scheduled day is Friday 20 Nov.
            LocalDateTime.of(LocalDate.of(2026, 11, 20), LocalTime.of(8, 0)),
            ReminderPlanner.nextTrigger(gym, now),
        )
    }

    @Test
    fun `no trigger when the reminder is off or the habit is archived`() {
        val now = LocalDateTime.of(today, LocalTime.of(6, 0))
        assertNull(ReminderPlanner.nextTrigger(habit(enabled = false), now))
        assertNull(ReminderPlanner.nextTrigger(habit(archived = true), now))
    }

    @Test
    fun `does not notify when the habit is already done`() {
        assertFalse(ReminderPlanner.shouldNotify(habit(), setOf(today), today))
        assertTrue(ReminderPlanner.shouldNotify(habit(), emptySet(), today))
    }

    @Test
    fun `does not notify on a day the habit is not due`() {
        val gym = habit(schedule = ScheduleType.SPECIFIC_DAYS, days = setOf(DayOfWeek.MONDAY))
        assertFalse(ReminderPlanner.shouldNotify(gym, emptySet(), today))
    }

    @Test
    fun `flexible habits stop nagging once the weekly target is met`() {
        val flexible = Habit(
            id = 2,
            name = "Lari",
            scheduleType = ScheduleType.TIMES_PER_WEEK,
            timesPerWeek = 2,
            reminderEnabled = true,
            createdAt = today.minusDays(30),
        )
        // Week of Mon 16 Nov: two completions already logged.
        val done = setOf(LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 18))
        assertFalse(ReminderPlanner.shouldNotify(flexible, done, today))
        assertTrue(ReminderPlanner.shouldNotify(flexible, setOf(LocalDate.of(2026, 11, 16)), today))
    }
}
