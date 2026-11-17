package com.example.habittracker.domain

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Pure scheduling math for habit reminders: works out the next moment a habit should
 * ring, honouring its schedule. Kept free of Android APIs so it can be unit tested.
 */
object ReminderPlanner {

    /** How far ahead we are willing to look for the next scheduled day. */
    private const val MAX_LOOKAHEAD_DAYS = 14

    /**
     * Next date-time the reminder should fire, or null when the habit has no reminder
     * or no scheduled day within the lookahead window.
     */
    fun nextTrigger(habit: Habit, now: LocalDateTime): LocalDateTime? {
        if (!habit.reminderEnabled || habit.isArchived) return null
        val time = habit.reminderTime
        var date: LocalDate = now.toLocalDate()
        repeat(MAX_LOOKAHEAD_DAYS) {
            val candidate = LocalDateTime.of(date, time)
            if (candidate.isAfter(now) && habit.isScheduledOn(date) && !date.isBefore(habit.createdAt)) {
                return candidate
            }
            date = date.plusDays(1)
        }
        return null
    }

    /**
     * Whether a reminder that fires on [date] still makes sense: the habit must be due
     * that day, not completed yet, and - for flexible habits - still short of its
     * weekly target.
     */
    fun shouldNotify(habit: Habit, completedDates: Set<LocalDate>, date: LocalDate): Boolean {
        if (habit.isArchived || !habit.reminderEnabled) return false
        if (!habit.isScheduledOn(date) || date.isBefore(habit.createdAt)) return false
        if (date in completedDates) return false
        if (habit.scheduleType == ScheduleType.TIMES_PER_WEEK) {
            return StreakCalculator.completionsInWeek(completedDates, date) < habit.timesPerWeek
        }
        return true
    }
}
