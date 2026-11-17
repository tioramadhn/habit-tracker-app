package com.example.habittracker

import com.example.habittracker.domain.Habit
import com.example.habittracker.domain.ScheduleType
import com.example.habittracker.domain.StreakCalculator
import com.example.habittracker.domain.StreakUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class StreakCalculatorTest {

    private val today = LocalDate.of(2026, 11, 17) // a Tuesday

    private fun daily(createdAt: LocalDate = today.minusDays(60)) = Habit(
        id = 1,
        name = "Minum air",
        scheduleType = ScheduleType.DAILY,
        createdAt = createdAt,
    )

    private fun datesBack(vararg offsets: Long): Set<LocalDate> =
        offsets.mapTo(mutableSetOf()) { today.minusDays(it) }

    @Test
    fun `no completions means no streak`() {
        val stats = StreakCalculator.stats(daily(), emptySet(), today)
        assertEquals(0, stats.currentStreak)
        assertEquals(0, stats.bestStreak)
        assertEquals(0f, stats.completionRate, 0.001f)
    }

    @Test
    fun `consecutive days build the current streak`() {
        val stats = StreakCalculator.stats(daily(), datesBack(0, 1, 2, 3), today)
        assertEquals(4, stats.currentStreak)
        assertEquals(4, stats.bestStreak)
        assertEquals(StreakUnit.DAYS, stats.streakUnit)
    }

    @Test
    fun `today is a grace day and does not break the streak`() {
        // Done yesterday and the day before, not yet today.
        val stats = StreakCalculator.stats(daily(), datesBack(1, 2), today)
        assertEquals(2, stats.currentStreak)
    }

    @Test
    fun `a missed day ends the current streak`() {
        // Missed yesterday, so only days before it count as history, not as a live streak.
        val stats = StreakCalculator.stats(daily(), datesBack(2, 3, 4), today)
        assertEquals(0, stats.currentStreak)
        assertEquals(3, stats.bestStreak)
    }

    @Test
    fun `unscheduled weekdays are skipped instead of breaking the streak`() {
        val habit = Habit(
            id = 2,
            name = "Gym",
            scheduleType = ScheduleType.SPECIFIC_DAYS,
            daysOfWeek = setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
            createdAt = today.minusDays(60),
        )
        // Tue 17 Nov, Thu 12 Nov, Tue 10 Nov: three scheduled days in a row.
        val completed = setOf(
            LocalDate.of(2026, 11, 17),
            LocalDate.of(2026, 11, 12),
            LocalDate.of(2026, 11, 10),
        )
        val stats = StreakCalculator.stats(habit, completed, today)
        assertEquals(3, stats.currentStreak)
    }

    @Test
    fun `flexible habits count streaks in weeks`() {
        val habit = Habit(
            id = 3,
            name = "Lari",
            scheduleType = ScheduleType.TIMES_PER_WEEK,
            timesPerWeek = 3,
            createdAt = today.minusDays(60),
        )
        // This week (Mon 16 - Sun 22): 2 of 3 done so far -> still in grace.
        // Previous two weeks hit the target of 3.
        val completed = setOf(
            LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 17),
            LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 13),
            LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 4), LocalDate.of(2026, 11, 6),
        )
        val stats = StreakCalculator.stats(habit, completed, today)
        assertEquals(StreakUnit.WEEKS, stats.streakUnit)
        assertEquals(2, stats.currentStreak)
        assertEquals(2, stats.completedThisWeek)
        assertEquals(3, stats.weeklyTarget)

        // One more completion this week closes the target and extends the streak.
        val withThird = completed + LocalDate.of(2026, 11, 18)
        assertEquals(3, StreakCalculator.currentStreak(habit, withThird, today))
    }

    @Test
    fun `completion rate only counts days after creation`() {
        val habit = daily(createdAt = today.minusDays(3)) // 4 expected days
        val stats = StreakCalculator.stats(habit, datesBack(0, 1), today)
        assertEquals(0.5f, stats.completionRate, 0.001f)
    }

    @Test
    fun `rate is capped for flexible habits that overshoot`() {
        val habit = Habit(
            id = 4,
            name = "Jurnal",
            scheduleType = ScheduleType.TIMES_PER_WEEK,
            timesPerWeek = 2,
            createdAt = today.minusDays(6),
        )
        val stats = StreakCalculator.stats(habit, datesBack(0, 1, 2, 3, 4, 5, 6), today)
        assertEquals(1f, stats.completionRate, 0.001f)
    }
}
