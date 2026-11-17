package com.example.habittracker.data

import com.example.habittracker.data.local.CompletionEntity
import com.example.habittracker.data.local.HabitDao
import com.example.habittracker.data.local.toDomain
import com.example.habittracker.data.local.toEntity
import com.example.habittracker.domain.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** Single source of truth for habits and their completion log. */
class HabitRepository(private val dao: HabitDao) {

    fun observeActiveHabits(): Flow<List<Habit>> =
        dao.observeActiveHabits().map { list -> list.map { it.toDomain() } }

    fun observeArchivedHabits(): Flow<List<Habit>> =
        dao.observeArchivedHabits().map { list -> list.map { it.toDomain() } }

    fun observeHabit(habitId: Long): Flow<Habit?> =
        dao.observeHabit(habitId).map { it?.toDomain() }

    /** Completion dates of every habit, keyed by habit id. */
    fun observeCompletionsByHabit(): Flow<Map<Long, Set<LocalDate>>> =
        dao.observeAllCompletions().map { completions ->
            completions.groupBy { it.habitId }
                .mapValues { (_, rows) -> rows.mapTo(mutableSetOf()) { LocalDate.ofEpochDay(it.dateEpochDay) } }
        }

    fun observeCompletions(habitId: Long): Flow<Set<LocalDate>> =
        dao.observeCompletions(habitId).map { rows ->
            rows.mapTo(mutableSetOf()) { LocalDate.ofEpochDay(it.dateEpochDay) }
        }

    fun observeTotalCompletions(): Flow<Int> = dao.observeTotalCompletions()

    suspend fun getHabit(habitId: Long): Habit? = dao.getHabit(habitId)?.toDomain()

    suspend fun getActiveHabits(): List<Habit> = dao.getActiveHabits().map { it.toDomain() }

    suspend fun isCompleted(habitId: Long, date: LocalDate): Boolean =
        dao.isCompleted(habitId, date.toEpochDay())

    /** Completion dates of one habit, read once. */
    suspend fun completionsOnce(habitId: Long): Set<LocalDate> =
        dao.getCompletions(habitId).mapTo(mutableSetOf()) { LocalDate.ofEpochDay(it.dateEpochDay) }

    /** Inserts a new habit or updates an existing one; returns the habit id. */
    suspend fun saveHabit(habit: Habit): Long = if (habit.id == 0L) {
        dao.insertHabit(habit.copy(sortOrder = dao.nextSortOrder()).toEntity())
    } else {
        dao.updateHabit(habit.toEntity())
        habit.id
    }

    suspend fun deleteHabit(habitId: Long) = dao.deleteHabit(habitId)

    suspend fun setArchived(habitId: Long, archived: Boolean) = dao.setArchived(habitId, archived)

    /** Flips the completion state of [habitId] on [date] and returns the new state. */
    suspend fun toggleCompletion(habitId: Long, date: LocalDate): Boolean {
        val epochDay = date.toEpochDay()
        return if (dao.isCompleted(habitId, epochDay)) {
            dao.deleteCompletion(habitId, epochDay)
            false
        } else {
            dao.insertCompletion(CompletionEntity(habitId = habitId, dateEpochDay = epochDay))
            true
        }
    }

    suspend fun setCompletion(habitId: Long, date: LocalDate, completed: Boolean) {
        val epochDay = date.toEpochDay()
        if (completed) {
            dao.insertCompletion(CompletionEntity(habitId = habitId, dateEpochDay = epochDay))
        } else {
            dao.deleteCompletion(habitId, epochDay)
        }
    }
}
