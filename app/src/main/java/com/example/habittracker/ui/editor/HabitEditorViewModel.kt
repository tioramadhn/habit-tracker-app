package com.example.habittracker.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.domain.Habit
import com.example.habittracker.domain.ScheduleType
import com.example.habittracker.ui.appContainer
import kotlinx.coroutines.launch
import java.time.DayOfWeek

/** Emoji choices offered in the editor. */
val EmojiChoices = listOf(
    "✅", "💧", "🏃", "🧘", "📖", "📝",
    "🏋", "🥗", "😴", "🧼", "💊", "🎸",
    "🌱", "💻", "📚", "🚶", "🙏", "💰",
    "🚭", "🎨",
)

class HabitEditorViewModel(
    private val repository: HabitRepository,
    private val habitId: Long,
) : ViewModel() {

    var name by mutableStateOf("")
        private set
    var notes by mutableStateOf("")
        private set
    var emoji by mutableStateOf(EmojiChoices.first())
        private set
    var colorIndex by mutableIntStateOf(0)
        private set
    var scheduleType by mutableStateOf(ScheduleType.DAILY)
        private set
    var daysOfWeek by mutableStateOf(Habit.ALL_DAYS)
        private set
    var timesPerWeek by mutableIntStateOf(3)
        private set
    var reminderEnabled by mutableStateOf(false)
        private set
    var reminderMinuteOfDay by mutableIntStateOf(Habit.DEFAULT_REMINDER_MINUTE)
        private set
    var nameError by mutableStateOf(false)
        private set
    var isLoading by mutableStateOf(habitId != 0L)
        private set

    val isNewHabit: Boolean = habitId == 0L

    private var original: Habit? = null

    init {
        if (habitId != 0L) {
            viewModelScope.launch {
                repository.getHabit(habitId)?.let { habit ->
                    original = habit
                    name = habit.name
                    notes = habit.notes
                    emoji = habit.emoji
                    colorIndex = habit.colorIndex
                    scheduleType = habit.scheduleType
                    daysOfWeek = habit.daysOfWeek
                    timesPerWeek = habit.timesPerWeek
                    reminderEnabled = habit.reminderEnabled
                    reminderMinuteOfDay = habit.reminderMinuteOfDay
                }
                isLoading = false
            }
        }
    }

    fun onNameChange(value: String) {
        name = value
        if (nameError && value.isNotBlank()) nameError = false
    }

    fun onNotesChange(value: String) {
        notes = value
    }

    fun onEmojiSelected(value: String) {
        emoji = value
    }

    fun onColorSelected(index: Int) {
        colorIndex = index
    }

    fun onScheduleTypeSelected(type: ScheduleType) {
        scheduleType = type
        if (type == ScheduleType.SPECIFIC_DAYS && daysOfWeek.size == 7) {
            daysOfWeek = setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.FRIDAY,
            )
        }
    }

    fun onDayToggled(day: DayOfWeek) {
        val updated = if (day in daysOfWeek) daysOfWeek - day else daysOfWeek + day
        // At least one day has to stay selected.
        if (updated.isNotEmpty()) daysOfWeek = updated
    }

    fun onReminderToggled(enabled: Boolean) {
        reminderEnabled = enabled
    }

    fun onReminderTimeSelected(hour: Int, minute: Int) {
        reminderMinuteOfDay = (hour * 60 + minute).coerceIn(0, 24 * 60 - 1)
    }

    fun onTimesPerWeekChange(value: Int) {
        timesPerWeek = value.coerceIn(Habit.MIN_TIMES_PER_WEEK, Habit.MAX_TIMES_PER_WEEK)
    }

    /** Validates and persists; [onSaved] runs on success. */
    fun save(onSaved: () -> Unit) {
        if (name.isBlank()) {
            nameError = true
            return
        }
        val base = original
        val habit = (base ?: Habit()).copy(
            id = habitId,
            name = name.trim(),
            notes = notes.trim(),
            emoji = emoji,
            colorIndex = colorIndex,
            scheduleType = scheduleType,
            daysOfWeek = if (scheduleType == ScheduleType.SPECIFIC_DAYS) daysOfWeek else Habit.ALL_DAYS,
            timesPerWeek = timesPerWeek,
            reminderEnabled = reminderEnabled,
            reminderMinuteOfDay = reminderMinuteOfDay,
        )
        viewModelScope.launch {
            repository.saveHabit(habit)
            onSaved()
        }
    }

    companion object {
        fun factory(habitId: Long) = viewModelFactory {
            initializer { HabitEditorViewModel(this.appContainer.repository, habitId) }
        }
    }
}
