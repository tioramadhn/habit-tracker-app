package com.example.habittracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.domain.Habit
import com.example.habittracker.domain.HabitStats
import com.example.habittracker.domain.StreakCalculator
import com.example.habittracker.notification.HabitNotifier
import com.example.habittracker.ui.appContainer
import com.example.habittracker.ui.components.DayCompletion
import com.example.habittracker.ui.home.isVisibleOn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitStatRow(val habit: Habit, val stats: HabitStats)

data class StatsUiState(
    val today: LocalDate = LocalDate.now(),
    val rows: List<HabitStatRow> = emptyList(),
    val archived: List<Habit> = emptyList(),
    val last14Days: List<DayCompletion> = emptyList(),
    val doneToday: Int = 0,
    val scheduledToday: Int = 0,
    val totalCompletions: Int = 0,
    val bestStreak: Int = 0,
    val averageRate: Float = 0f,
    val perfectDays: Int = 0,
    val isLoading: Boolean = true,
) {
    val hasData: Boolean get() = rows.isNotEmpty() || archived.isNotEmpty()
}

class StatsViewModel(
    private val repository: HabitRepository,
    private val notifier: HabitNotifier,
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        repository.observeActiveHabits(),
        repository.observeCompletionsByHabit(),
        repository.observeArchivedHabits(),
    ) { habits, completions, archived ->
        val today = LocalDate.now()
        val rows = habits.map { habit ->
            HabitStatRow(habit, StreakCalculator.stats(habit, completions[habit.id].orEmpty(), today))
        }

        val dayCompletion = { date: LocalDate ->
            val scheduled = habits.count { it.isVisibleOn(date) }
            // Backfilled days can hold more entries than were scheduled back then.
            val done = habits.count { date in completions[it.id].orEmpty() }
            DayCompletion(
                date = date,
                done = done,
                scheduled = maxOf(scheduled, done),
            )
        }

        val last14 = (13L downTo 0L).map { dayCompletion(today.minusDays(it)) }
        val last30 = (29L downTo 0L).map { dayCompletion(today.minusDays(it)) }

        StatsUiState(
            today = today,
            rows = rows.sortedByDescending { it.stats.currentStreak },
            archived = archived,
            last14Days = last14,
            doneToday = last14.last().done,
            scheduledToday = last14.last().scheduled,
            totalCompletions = completions.values.sumOf { dates -> dates.count { !it.isAfter(today) } },
            bestStreak = rows.maxOfOrNull { it.stats.bestStreak } ?: 0,
            averageRate = if (rows.isEmpty()) 0f else rows.map { it.stats.completionRate }.average().toFloat(),
            perfectDays = last30.count { it.scheduled > 0 && it.done == it.scheduled },
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(),
    )

    fun restore(habitId: Long) {
        viewModelScope.launch { repository.setArchived(habitId, false) }
    }

    fun deleteArchived(habitId: Long) {
            viewModelScope.launch { repository.deleteHabit(habitId) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = this.appContainer
                StatsViewModel(container.repository, container.notifier)
            }
        }
    }
}
