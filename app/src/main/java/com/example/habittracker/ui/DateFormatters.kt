package com.example.habittracker.ui

import com.example.habittracker.domain.Habit
import com.example.habittracker.domain.ScheduleType
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

val AppLocale: Locale = Locale("id", "ID")

fun LocalDate.dayInitial(): String =
    dayOfWeek.getDisplayName(TextStyle.NARROW, AppLocale).uppercase(AppLocale)

fun LocalDate.shortDayName(): String =
    dayOfWeek.getDisplayName(TextStyle.SHORT, AppLocale)

fun LocalDate.fullDateLabel(): String {
    val day = dayOfWeek.getDisplayName(TextStyle.FULL, AppLocale)
    val month = month.getDisplayName(TextStyle.FULL, AppLocale)
    return "$day, $dayOfMonth $month"
}

fun LocalDate.monthShort(): String = month.getDisplayName(TextStyle.SHORT, AppLocale)

/** "Hari ini" / "Kemarin" / a date label, for headings. */
fun relativeDateLabel(date: LocalDate, today: LocalDate): String = when (date) {
    today -> "Hari ini"
    today.minusDays(1) -> "Kemarin"
    today.plusDays(1) -> "Besok"
    else -> date.fullDateLabel()
}

fun Habit.scheduleLabel(): String = when (scheduleType) {
    ScheduleType.DAILY -> "Setiap hari"
    ScheduleType.SPECIFIC_DAYS -> if (daysOfWeek.size == 7) {
        "Setiap hari"
    } else {
        daysOfWeek.sortedBy { it.value }
            .joinToString(" ") { it.getDisplayName(TextStyle.SHORT, AppLocale) }
    }
    ScheduleType.TIMES_PER_WEEK -> "$timesPerWeek× per minggu"
}

/** "08:00" from minutes since midnight. */
fun formatMinuteOfDay(minuteOfDay: Int): String {
    val hour = (minuteOfDay / 60).coerceIn(0, 23)
    val minute = (minuteOfDay % 60).coerceIn(0, 59)
    return "%02d:%02d".format(hour, minute)
}

fun daysSince(date: LocalDate, today: LocalDate): Long = ChronoUnit.DAYS.between(date, today)
