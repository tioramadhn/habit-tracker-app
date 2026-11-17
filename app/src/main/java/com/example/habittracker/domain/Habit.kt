package com.example.habittracker.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/** How often a habit is expected to be performed. */
enum class ScheduleType {
    /** Every single day. */
    DAILY,

    /** Only on the selected weekdays. */
    SPECIFIC_DAYS,

    /** A flexible target of N completions per calendar week. */
    TIMES_PER_WEEK,
}

/** Core domain model of a habit. */
data class Habit(
    val id: Long = 0L,
    val name: String = "",
    val notes: String = "",
    val emoji: String = "✅",
    val colorIndex: Int = 0,
    val scheduleType: ScheduleType = ScheduleType.DAILY,
    val daysOfWeek: Set<DayOfWeek> = ALL_DAYS,
    val timesPerWeek: Int = 3,
    val createdAt: LocalDate = LocalDate.now(),
    val isArchived: Boolean = false,
    val sortOrder: Int = 0,
    val reminderEnabled: Boolean = false,
    /** Minutes since midnight, e.g. 480 for 08:00. */
    val reminderMinuteOfDay: Int = DEFAULT_REMINDER_MINUTE,
) {
    val reminderTime: LocalTime
        get() = LocalTime.of(reminderMinuteOfDay / 60, reminderMinuteOfDay % 60)

    /** True when the habit is expected to be done on [date]. */
    fun isScheduledOn(date: LocalDate): Boolean = when (scheduleType) {
        ScheduleType.DAILY -> true
        ScheduleType.SPECIFIC_DAYS -> date.dayOfWeek in daysOfWeek
        // Flexible habits can be logged on any day of the week.
        ScheduleType.TIMES_PER_WEEK -> true
    }

    /** True when [date] counts towards the habit's required workload. */
    fun countsAsTarget(date: LocalDate): Boolean = when (scheduleType) {
        ScheduleType.DAILY -> true
        ScheduleType.SPECIFIC_DAYS -> date.dayOfWeek in daysOfWeek
        ScheduleType.TIMES_PER_WEEK -> false
    }

    companion object {
        val ALL_DAYS: Set<DayOfWeek> = DayOfWeek.values().toSet()
        const val DEFAULT_REMINDER_MINUTE = 8 * 60
        const val MIN_TIMES_PER_WEEK = 1
        const val MAX_TIMES_PER_WEEK = 7
    }
}
