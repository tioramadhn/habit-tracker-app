package com.example.habittracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY sortOrder ASC, id ASC")
    fun observeActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isArchived = 1 ORDER BY name ASC")
    fun observeArchivedHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    fun observeHabit(habitId: Long): Flow<HabitEntity?>

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    suspend fun getHabit(habitId: Long): HabitEntity?

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY sortOrder ASC, id ASC")
    suspend fun getActiveHabits(): List<HabitEntity>

    @Query("SELECT * FROM completions")
    fun observeAllCompletions(): Flow<List<CompletionEntity>>

    @Query("SELECT * FROM completions WHERE habitId = :habitId ORDER BY dateEpochDay ASC")
    fun observeCompletions(habitId: Long): Flow<List<CompletionEntity>>

    @Query("SELECT * FROM completions WHERE habitId = :habitId")
    suspend fun getCompletions(habitId: Long): List<CompletionEntity>

    @Query("SELECT COUNT(*) FROM completions")
    fun observeTotalCompletions(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabit(habitId: Long)

    @Query("UPDATE habits SET isArchived = :archived WHERE id = :habitId")
    suspend fun setArchived(habitId: Long, archived: Boolean)

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM habits")
    suspend fun nextSortOrder(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: CompletionEntity)

    @Query("DELETE FROM completions WHERE habitId = :habitId AND dateEpochDay = :epochDay")
    suspend fun deleteCompletion(habitId: Long, epochDay: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM completions WHERE habitId = :habitId AND dateEpochDay = :epochDay)")
    suspend fun isCompleted(habitId: Long, epochDay: Long): Boolean
}
