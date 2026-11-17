package com.example.habittracker.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Unit a streak is counted in. */
enum class StreakUnit { DAYS, WEEKS }

/** Aggregated statistics for a single habit. */
data class HabitStats(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompleted: Int = 0,
    /** Completion ratio in the trailing window, 0f..1f. */
    val completionRate: Float = 0f,
    /** Completions inside the current calendar week. */
    val completedThisWeek: Int = 0,
    /** Target for the current week (7 for daily habits, N for flexible ones). */
    val weeklyTarget: Int = 0,
    /** Streaks are counted in days for fixed schedules and in weeks for flexible ones. */
    val streakUnit: StreakUnit = StreakUnit.DAYS,
)

/**
 * Pure functions that turn a habit plus its completion dates into statistics.
 *
 * Fixed schedules (daily / specific weekdays) are measured in consecutive scheduled days:
 * days the habit is not scheduled on are skipped instead of breaking the streak.
 * Flexible schedules (N times per week) are measured in consecutive weeks that hit the target.
 */
object StreakCalculator {

    private const val DEFAULT_WINDOW_DAYS = 30

    fun stats(
        habit: Habit,
        completedDates: Set<LocalDate>,
        today: LocalDate = LocalDate.now(),
        windowDays: Int = DEFAULT_WINDOW_DAYS,
    ): HabitStats = HabitStats(
        currentStreak = currentStreak(habit, completedDates, today),
        bestStreak = bestStreak(habit, completedDates, today),
        totalCompleted = completedDates.count { !it.isAfter(today) },
        completionRate = completionRate(habit, completedDates, today, windowDays),
        completedThisWeek = completionsInWeek(completedDates, today),
        weeklyTarget = weeklyTarget(habit),
        streakUnit = if (habit.scheduleType == ScheduleType.TIMES_PER_WEEK) {
            StreakUnit.WEEKS
        } else {
            StreakUnit.DAYS
        },
    )

    fun weeklyTarget(habit: Habit): Int = when (habit.scheduleType) {
        ScheduleType.DAILY -> 7
        ScheduleType.SPECIFIC_DAYS -> habit.daysOfWeek.size
        ScheduleType.TIMES_PER_WEEK -> habit.timesPerWeek
    }

    /** Completions inside the Monday-to-Sunday week that contains [date]. */
    fun completionsInWeek(completedDates: Set<LocalDate>, date: LocalDate): Int {
        val monday = startOfWeek(date)
        return completedDates.count { !it.isBefore(monday) && it.isBefore(monday.plusWeeks(1)) }
    }

    fun startOfWeek(date: LocalDate): LocalDate = date.with(DayOfWeek.MONDAY)

    /**
     * Streak that is still alive today. Today itself is treated as a grace period: a habit that
     * has not been checked off yet today keeps the streak it earned up to yesterday.
     */
    fun currentStreak(habit: Habit, completedDates: Set<LocalDate>, today: LocalDate): Int {
        if (completedDates.isEmpty()) return 0
        return if (habit.scheduleType == ScheduleType.TIMES_PER_WEEK) {
            currentWeeklyStreak(habit, completedDates, today)
        } else {
            currentDailyStreak(habit, completedDates, today)
        }
    }

    private fun currentDailyStreak(habit: Habit, completed: Set<LocalDate>, today: LocalDate): Int {
        val start = minOf(habit.createdAt, completed.min())
        var streak = 0
        var cursor = today
        var isGraceDay = true
        while (!cursor.isBefore(start)) {
            if (habit.countsAsTarget(cursor)) {
                when {
                    cursor in completed -> streak++
                    // Today may still be completed later on, so it does not break the streak.
                    isGraceDay -> Unit
                    else -> return streak
                }
                isGraceDay = false
            }
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    private fun currentWeeklyStreak(habit: Habit, completed: Set<LocalDate>, today: LocalDate): Int {
        val target = habit.timesPerWeek
        val firstWeek = startOfWeek(minOf(habit.createdAt, completed.min()))
        var streak = 0
        var week = startOfWeek(today)
        var isGraceWeek = true
        while (!week.isBefore(firstWeek)) {
            val done = completionsInWeek(completed, week)
            when {
                done >= target -> streak++
                // The running week still has time left to reach its target.
                isGraceWeek -> Unit
                else -> return streak
            }
            isGraceWeek = false
            week = week.minusWeeks(1)
        }
        return streak
    }

    /** Longest streak ever achieved, including the one that is currently running. */
    fun bestStreak(habit: Habit, completedDates: Set<LocalDate>, today: LocalDate): Int {
        if (completedDates.isEmpty()) return 0
        return if (habit.scheduleType == ScheduleType.TIMES_PER_WEEK) {
            bestWeeklyStreak(habit, completedDates, today)
        } else {
            bestDailyStreak(habit, completedDates, today)
        }
    }

    private fun bestDailyStreak(habit: Habit, completed: Set<LocalDate>, today: LocalDate): Int {
        var best = 0
        var running = 0
        var cursor = minOf(habit.createdAt, completed.min())
        while (!cursor.isAfter(today)) {
            if (habit.countsAsTarget(cursor)) {
                if (cursor in completed) {
                    running++
                    best = maxOf(best, running)
                } else if (cursor != today) {
                    running = 0
                }
            }
            cursor = cursor.plusDays(1)
        }
        return best
    }

    private fun bestWeeklyStreak(habit: Habit, completed: Set<LocalDate>, today: LocalDate): Int {
        val target = habit.timesPerWeek
        val currentWeek = startOfWeek(today)
        var best = 0
        var running = 0
        var week = startOfWeek(minOf(habit.createdAt, completed.min()))
        while (!week.isAfter(currentWeek)) {
            if (completionsInWeek(completed, week) >= target) {
                running++
                best = maxOf(best, running)
            } else if (week != currentWeek) {
                running = 0
            }
            week = week.plusWeeks(1)
        }
        return best
    }

    /**
     * Share of the expected workload that was completed over the last [windowDays] days,
     * counting only days after the habit was created.
     */
    fun completionRate(
        habit: Habit,
        completedDates: Set<LocalDate>,
        today: LocalDate,
        windowDays: Int = DEFAULT_WINDOW_DAYS,
    ): Float {
        val from = maxOf(habit.createdAt, today.minusDays((windowDays - 1).toLong()))
        if (from.isAfter(today)) return 0f
        val done = completedDates.count { !it.isBefore(from) && !it.isAfter(today) }
        val expected = when (habit.scheduleType) {
            ScheduleType.TIMES_PER_WEEK -> {
                val days = ChronoUnit.DAYS.between(from, today) + 1
                days * habit.timesPerWeek / 7f
            }
            else -> {
                var cursor = from
                var count = 0
                while (!cursor.isAfter(today)) {
                    if (habit.countsAsTarget(cursor)) count++
                    cursor = cursor.plusDays(1)
                }
                count.toFloat()
            }
        }
        if (expected <= 0f) return 0f
        return (done / expected).coerceIn(0f, 1f)
    }
}
