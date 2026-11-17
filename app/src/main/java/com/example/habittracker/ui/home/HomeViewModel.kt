package com.example.habittracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.domain.Habit
import com.example.habittracker.domain.HabitStats
import com.example.habittracker.domain.StreakCalculator
import com.example.habittracker.ui.appContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** A habit as rendered in the day list. */
data class HabitDayUi(
    val habit: Habit,
    val stats: HabitStats,
    val isCompleted: Boolean,
)

data class HomeUiState(
    val today: LocalDate = LocalDate.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val weekDates: List<LocalDate> = emptyList(),
    val dayRatios: Map<LocalDate, Float> = emptyMap(),
    val pending: List<HabitDayUi> = emptyList(),
    val completed: List<HabitDayUi> = emptyList(),
    val hasAnyHabit: Boolean = false,
    val isLoading: Boolean = true,
) {
    val scheduledCount: Int get() = pending.size + completed.size
    val progress: Float
        get() = if (scheduledCount == 0) 0f else completed.size.toFloat() / scheduledCount
}

class HomeViewModel(private val repository: HabitRepository) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeActiveHabits(),
        repository.observeCompletionsByHabit(),
        selectedDate,
    ) { habits, completions, selected ->
        buildState(habits, completions, selected)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun selectDate(date: LocalDate) {
        if (!date.isAfter(LocalDate.now())) selectedDate.value = date
    }

    fun toggleHabit(habitId: Long) {
        val date = selectedDate.value
        viewModelScope.launch { repository.toggleCompletion(habitId, date) }
    }

    private fun buildState(
        habits: List<Habit>,
        completions: Map<Long, Set<LocalDate>>,
        selected: LocalDate,
    ): HomeUiState {
        val today = LocalDate.now()
        val weekDates = (6L downTo 0L).map { today.minusDays(it) }

        val visible = habits.filter { it.isVisibleOn(selected) }
        val rows = visible.map { habit ->
            val dates = completions[habit.id].orEmpty()
            HabitDayUi(
                habit = habit,
                stats = StreakCalculator.stats(habit, dates, today),
                isCompleted = selected in dates,
            )
        }

        val ratios = weekDates.associateWith { date ->
            val scheduled = habits.count { it.isVisibleOn(date) }
            val done = habits.count { date in completions[it.id].orEmpty() }
            // Days backfilled from the detail screen can exceed what was scheduled then.
            val total = maxOf(scheduled, done)
            if (total == 0) 0f else done.toFloat() / total
        }

        return HomeUiState(
            today = today,
            selectedDate = selected,
            weekDates = weekDates,
            dayRatios = ratios,
            pending = rows.filterNot { it.isCompleted },
            completed = rows.filter { it.isCompleted },
            hasAnyHabit = habits.isNotEmpty(),
            isLoading = false,
        )
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { HomeViewModel(this.appContainer.repository) }
        }
    }
}

/** Habits show up on a date once they exist and the schedule allows that day. */
fun Habit.isVisibleOn(date: LocalDate): Boolean =
    !date.isBefore(createdAt) && isScheduledOn(date)
