package com.example.habittracker.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.domain.Habit
import com.example.habittracker.domain.HabitStats
import com.example.habittracker.domain.StreakCalculator
import com.example.habittracker.notification.HabitNotifier
import com.example.habittracker.notification.ReminderScheduler
import com.example.habittracker.ui.appContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitDetailUiState(
    val habit: Habit? = null,
    val stats: HabitStats = HabitStats(),
    val completedDates: Set<LocalDate> = emptySet(),
    val today: LocalDate = LocalDate.now(),
    val recentDays: List<RecentDay> = emptyList(),
    val isLoading: Boolean = true,
)

data class RecentDay(
    val date: LocalDate,
    val isCompleted: Boolean,
    val isScheduled: Boolean,
)

class HabitDetailViewModel(
    private val repository: HabitRepository,
    private val reminderScheduler: ReminderScheduler,
    private val notifier: HabitNotifier,
    private val habitId: Long,
) : ViewModel() {

    val uiState: StateFlow<HabitDetailUiState> = combine(
        repository.observeHabit(habitId),
        repository.observeCompletions(habitId),
    ) { habit, dates ->
        val today = LocalDate.now()
        if (habit == null) {
            HabitDetailUiState(habit = null, isLoading = false, today = today)
        } else {
            HabitDetailUiState(
                habit = habit,
                stats = StreakCalculator.stats(habit, dates, today),
                completedDates = dates,
                today = today,
                recentDays = (13L downTo 0L).map { offset ->
                    val date = today.minusDays(offset)
                    RecentDay(
                        date = date,
                        isCompleted = date in dates,
                        isScheduled = habit.isScheduledOn(date) && !date.isBefore(habit.createdAt),
                    )
                },
                isLoading = false,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HabitDetailUiState(),
    )

    fun toggleDate(date: LocalDate) {
        if (date.isAfter(LocalDate.now())) return
        viewModelScope.launch { repository.toggleCompletion(habitId, date) }
    }

    fun setArchived(archived: Boolean) {
        viewModelScope.launch {
            repository.setArchived(habitId, archived)
            if (archived) cancelReminder()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            cancelReminder()
            onDeleted()
        }
    }

    /** An archived or deleted habit must not keep ringing. */
    private fun cancelReminder() {
        reminderScheduler.cancel(habitId)
        notifier.cancel(habitId)
    }

    companion object {
        fun factory(habitId: Long) = viewModelFactory {
            initializer {
                val container = this.appContainer
                HabitDetailViewModel(
                    repository = container.repository,
                    reminderScheduler = container.reminderScheduler,
                    notifier = container.notifier,
                    habitId = habitId,
                )
            }
        }
    }
}
