package com.example.habittracker.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.habittracker.domain.Habit
import com.example.habittracker.domain.ScheduleType
import java.time.DayOfWeek
import java.time.LocalDate

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val notes: String = "",
    val emoji: String = "✅",
    val colorIndex: Int = 0,
    val scheduleType: String = ScheduleType.DAILY.name,
    val daysOfWeekMask: Int = ALL_DAYS_MASK,
    val timesPerWeek: Int = 3,
    @ColumnInfo(name = "createdAtEpochDay") val createdAtEpochDay: Long = 0L,
    val isArchived: Boolean = false,
    val sortOrder: Int = 0,
    val reminderEnabled: Boolean = false,
    val reminderMinuteOfDay: Int = Habit.DEFAULT_REMINDER_MINUTE,
) {
    companion object {
        const val ALL_DAYS_MASK = 0b111_1111
    }
}

@Entity(
    tableName = "completions",
    primaryKeys = ["habitId", "dateEpochDay"],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habitId"), Index("dateEpochDay")],
)
data class CompletionEntity(
    val habitId: Long,
    val dateEpochDay: Long,
    val loggedAtMillis: Long = System.currentTimeMillis(),
)

/* ---------- mapping ---------- */

internal fun Set<DayOfWeek>.toMask(): Int = fold(0) { acc, day -> acc or (1 shl (day.value - 1)) }

internal fun Int.toDaysOfWeek(): Set<DayOfWeek> =
    DayOfWeek.values().filterTo(mutableSetOf()) { this and (1 shl (it.value - 1)) != 0 }

fun HabitEntity.toDomain(): Habit = Habit(
    id = id,
    name = name,
    notes = notes,
    emoji = emoji,
    colorIndex = colorIndex,
    scheduleType = runCatching { ScheduleType.valueOf(scheduleType) }.getOrDefault(ScheduleType.DAILY),
    daysOfWeek = daysOfWeekMask.toDaysOfWeek().ifEmpty { Habit.ALL_DAYS },
    timesPerWeek = timesPerWeek.coerceIn(Habit.MIN_TIMES_PER_WEEK, Habit.MAX_TIMES_PER_WEEK),
    createdAt = LocalDate.ofEpochDay(createdAtEpochDay),
    isArchived = isArchived,
    sortOrder = sortOrder,
    reminderEnabled = reminderEnabled,
    reminderMinuteOfDay = reminderMinuteOfDay.coerceIn(0, 24 * 60 - 1),
)

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    name = name.trim(),
    notes = notes.trim(),
    emoji = emoji,
    colorIndex = colorIndex,
    scheduleType = scheduleType.name,
    daysOfWeekMask = daysOfWeek.toMask(),
    timesPerWeek = timesPerWeek,
    createdAtEpochDay = createdAt.toEpochDay(),
    isArchived = isArchived,
    sortOrder = sortOrder,
    reminderEnabled = reminderEnabled,
    reminderMinuteOfDay = reminderMinuteOfDay.coerceIn(0, 24 * 60 - 1),
)
